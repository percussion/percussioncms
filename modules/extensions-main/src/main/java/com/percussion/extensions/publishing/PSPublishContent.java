/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.publishing;

import com.percussion.extension.*;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * This is a workflow action that runs an edition.
 * <p>
 * One common example of this is when a user has a staging server, and the
 * requirement that all content should show up there immediately upon approval
 * into some staging state.
 * <p>
 * The edition that is run depends on the <code>Workflow ID</code> and
 * <code>Transition Id</code> of the transition that causes this action to
 * execute. The edition id is obtained from the file
 * <code>/Rhythmyx/rxconfig/Workflow/publish.xml</code>
 * <p>
 * Note that the Transition Id is unique only within the Workflow, not across
 * Workflows
 * <p>
 * Note that unlike the PSPublishEditionForPreview, this exit is
 * <em>asynchronous</em>. It initiates the publication and moves on, it does
 * not wait for the publication to complete. Also, the edition can be any
 * edition. Normally, it will be an incremental edition, not a manual edition.
 * <p>
 * If there are many requests to publish the same edition, this extension will
 * ingnore all except for one in addion to that is already running. The idea
 * behind this is that it does not make sense to publish one edition several
 * times in succession. Consider an example of an incremental edition. The
 * worklfow action is attached to a transition that sends the item to a public
 * state. If 10 items go to public state using this transition, there will be
 * 10 requests to publish the same edition. However, it will be adequate to
 * honor the first (which is in progress) and the last requests.
 */
public class PSPublishContent
   extends PSDefaultExtension
   implements IPSWorkflowAction
{
   /**
    * Key to lookup an edition id from a workflow and transition
    */
   private static class PSPCKey
   {
      /**
       * Workflow id of the key
       */
      public int mi_workflowId;

      /**
       * Transition id of the key
       */
      public int mi_transitionId;
      
      @Override
      public int hashCode()
      {
         return mi_workflowId * 1000 + mi_transitionId;
      }
      
      @Override
      public boolean equals(Object b)
      {
         if (! (b instanceof PSPCKey)) return false;
         PSPCKey other = (PSPCKey) b;
         
         return other.mi_workflowId == mi_workflowId &&
            other.mi_transitionId == mi_transitionId;
      }
   }

   /**
    * The relative path of the publish.properties file.
    */
   private static final String CONFIG_FILE =
      "rxconfig/Workflow/publish.xml";

   /*
    * Defined strings to search for child nodes
    */
   private static final String PUBLISH = "PSXPublish";
   private static final String WORKFLOW = "PSXWorkflowId";
   private static final String TRANSITION = "PSXTransitionId";
   private static final String EDITION = "PSXEdition";
   private static final String ATTR_POLLING_TIME = "polling-time";
   

   /**
    * The publish properties identifies the edition to publish for a 
    * given pair of transitionId and workflowId. These are read from
    * an XML file in rxconfig/Workflow/publishcontent.xml
    */
   private Map m_publishProps = null;

   /**
    * Default polling time in milli seconds, which is the interval between two
    * consecutive attempts to publish an edition when it is already running.
    */
   private static int m_pollingTime = 1500;

   private static final Logger ms_logger = LogManager.getLogger(PSPublishContent.class);
   
   /**
    * The init method is called when the action is initially loaded.
    * 
    * @param def extension definition, must never be <code>null</code>
    * @param codeRoot code root, ignored
    * @throws PSExtensionException if there is an error reading the properties
    *            file.
    * @see PSDefaultExtension#init(IPSExtensionDef, File)
    */
   @Override
   public void init(IPSExtensionDef def,
         @SuppressWarnings("unused") File codeRoot)
      throws PSExtensionException
   {
      if (def == null)
      {
         throw new IllegalArgumentException("def must never be null");
      }
      
      try
      {
         // Read initial information from the filesystem
         m_publishProps = getPublishProperties();
      }
      catch (PSExtensionException pse)
      {
         // Rethrow
         throw pse;
      }
      catch (Exception ex)
      {
         throw new PSExtensionException(
            IPSExtensionErrors.BAD_PUBLISH_CONTENT_INITIALIZATION_DATA,
            ex.getMessage());
      }

   }

   /**
    * Performs the action. Called by the workflow system when a document makes
    * a particular workflow transition.
    * @param wfContext  the Workflow Context for this transition
    * @param request the Request Context for the calling user
    * @throws PSExtensionProcessingException when an IO or URL error occurs.
    */
   public void performAction(
      IPSWorkFlowContext wfContext,
      IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      request.printTraceMessage("Start of Publish Content Workflow Action");
      int workflowId = wfContext.getWorkflowID();
      int transitionId = wfContext.getTransitionID();
      int editionId;

      request.printTraceMessage(
         "Workflow id: " + workflowId + " transistion id: " + transitionId);
      try
      {
         request.printTraceMessage(
            "User name is: "
               + request.getUserContextInformation("User/Name", ""));
      }
      catch (Exception ex1)
      {
         ms_logger.error("Problem getting user context information", ex1);
      }
      
      PSPCKey key = new PSPCKey();
      key.mi_transitionId = transitionId;
      key.mi_workflowId = workflowId;
      
      List editions = (List)m_publishProps.get(key);
      //Check if we got property value from publish.properties file
      if (editions != null)
      {
         Iterator it = editions.iterator();
         while(it.hasNext())
         {
            Integer edition = (Integer)it.next();
            try
            {
               editionId = edition.intValue();
               request.printTraceMessage("EditionID= ".concat(String
                  .valueOf(editionId)));
               URL pubUrl = getPubHandlerUrl(request, editionId);
               Set inProgress = (Set) request
                  .getSessionPrivateObject(PUB_URLS_IN_PROGRESS);
               if (inProgress == null || !inProgress.contains(pubUrl))
                  publishUrl(request, pubUrl);
            }
            catch (Exception s)
           {
               ms_logger.error(s);
               throw new PSExtensionProcessingException(
                  this.getClass().getName(),
                  s);
            }
         }
      }
      else
      {
         String msg = "Could not find edition information for workflow "
            + workflowId + " and transition " + transitionId + " in "
            + "publish.xml file";
         // this isn't always an error
         request.printTraceMessage(msg);
         ms_logger.warn(msg);
      }
   }
   
   /**
    * Publish the edition by making the request to server using the URL object.
    * The edition that is in progress will be stored as session private object.
    * 
    * @param request request context object, assumed not <code>null</code>.
    * @param pubUrl url object to publish the edition, assumed not
    *           <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void publishUrl(IPSRequestContext request, URL pubUrl)
   {
      Set inProgress = (Set) request
         .getSessionPrivateObject(PUB_URLS_IN_PROGRESS);
      if (inProgress == null)
      {
         inProgress = new HashSet();
         request.setSessionPrivateObject(PUB_URLS_IN_PROGRESS, inProgress);
      }
      // publish the edition in a separate thread.
      // this allows the current request (and the workflow/editor
      // engine to finish its work before the edition starts up.
      Thread worker = new Thread(new PublishEdition(request, pubUrl));
      worker.start();
      inProgress.add(pubUrl);
   }

   /**
    * Load the publisher properties from the file.
    * 
    * @return the publisher properties as an object.
    * @throws PSExtensionException if the required data in the config file is
    *            missing.
    * @throws SAXException if there is any error parsing the config file as an
    *            XML document.
    * @throws IOException when there is an error reading the file
    * @throws ParserConfigurationException if there is any error parsing the
    *            config file as an XML document.
    * @throws FileNotFoundException when the file does not exist
    */
   @SuppressWarnings("unchecked")
   private static Map getPublishProperties()
      throws
         PSExtensionException,
         SAXException,
         IOException,
         ParserConfigurationException
   {
      DocumentBuilderFactory fact = PSSecureXMLUtils
      .getSecuredDocumentBuilderFactory(false);

      DocumentBuilder builder = fact.newDocumentBuilder();
      Document configfile = builder.parse(CONFIG_FILE);
      
      String sPollTime = configfile.getDocumentElement().getAttribute(
         ATTR_POLLING_TIME);
      if (sPollTime.length() > 0)
      {
         try
         {
            m_pollingTime = Integer.parseInt(sPollTime);
         }
         catch (Exception e)
         {
            //ignore exception and keep the default value.
         }
      }
      Map<PSPublishContent.PSPCKey,List<Integer>> rval = new HashMap<>();

      // Find child elements
      NodeList elements = configfile.getElementsByTagName(PUBLISH);

      // Each node should have three subelements
      int len = elements.getLength();
      for (int i = 0; i < len; i++)
      {
         Element el = (Element) elements.item(i);
         Element transition =
            (Element) PSXMLDomUtil.findFirstNamedChildNode(el, TRANSITION);
         Element workflow =
            (Element) PSXMLDomUtil.findFirstNamedChildNode(el, WORKFLOW);
         Element edition =
            (Element) PSXMLDomUtil.findFirstNamedChildNode(el, EDITION);

         if (transition == null)
         {
            throw new PSExtensionException(
               0,
               "Missing transition in specification file");
         }

         if (workflow == null)
         {
            throw new PSExtensionException(
               0,
               "Missing workflow in specification file");
         }

         if (edition == null)
         {
            throw new PSExtensionException(
               0,
               "Missing edition in specification file");
         }

         PSPCKey key = new PSPCKey();
         Integer editionId = null;

         try
         {
            key.mi_workflowId =
               Integer.parseInt(PSXMLDomUtil.getElementData(workflow));
         }
         catch (Exception th)
         {
            throw new PSExtensionException(
               IPSExtensionErrors.BAD_PUBLISH_CONTENT_FILE_DATA,
               "Error while parsing value for workflow id");
         }
         try
         {
            key.mi_transitionId =
               Integer.parseInt(PSXMLDomUtil.getElementData(transition));
         }
         catch (Exception th)
         {
            throw new PSExtensionException(
               IPSExtensionErrors.BAD_PUBLISH_CONTENT_FILE_DATA,
               "Error while parsing value for workflow id");
         }
         try
         {
            editionId = new Integer(PSXMLDomUtil.getElementData(edition));
         }
         catch (Exception th)
         {
            throw new PSExtensionException(
               IPSExtensionErrors.BAD_PUBLISH_CONTENT_FILE_DATA,
               "Error while parsing value for workflow id");
         }
         
         if (rval.containsKey(key))
         {
            List<Integer> editions = rval.get(key);
            editions.add(editionId);
         }
         else
         {
            List<Integer> editions = new ArrayList<>();
            editions.add(editionId);
            rval.put(key, editions);
         }
      }
      return rval;
   }

   /**
    * Build the PubHandler URL. This URL will be used to start the PubHandler.
    * @param req the request context
    * @param editionId the edition to publish
    * @return the PubHandler start URL.
    * @throws MalformedURLException
    */
   private static URL getPubHandlerUrl(IPSRequestContext req, int editionId)
      throws java.net.MalformedURLException
   {
      StringBuilder pubUrl = new StringBuilder();
      pubUrl.append("http://127.0.0.1:");
      pubUrl.append(req.getServerListenerPort());
      pubUrl.append("/Rhythmyx/sys_pubHandler/publisher.xml");
      pubUrl.append("?editionid=");
      pubUrl.append(editionId);
      pubUrl.append("&PUBAction=publish");
      pubUrl.append("&pssessionid=");
      pubUrl.append(req.getUserSessionId());
      req.printTraceMessage("Publisher URL is: " + pubUrl.toString());
      return new URL(pubUrl.toString());
   }

   private static final String PUB_URLS_IN_PROGRESS = "key_pubUrlsInProgress";

   /**
    * A private inner class used to spawn a separate thread.  The Workflow
    * action completes, the main request runs to completion. The separate thread
    * wakes up and initiates the publication of the edition.  The delay allows
    * the workflow to complete the transition and adjust the state of any items
    * <em>before</em> the publication starts.
    *
    */
   private class PublishEdition implements Runnable
   {
      /**
       * The publisher URL. Accessing this URL will start the publisher.
       */
      private URL pubUrl;
      
      /**
       * Reference to request context object that is initialized in the ctor.
       * Used to ccess the session object. Never <code>null</code> after that.
       */
      private IPSRequestContext request = null;

      /**
       * Constructor for the publishEdition runner class.
       * @param req request context object, must notbe <code>null</code>
       * @param url the publisher URL, must not be <code>null</code>
       */
      public PublishEdition(IPSRequestContext req, URL url)
      {
         pubUrl = url;
         request = req;
      }

      /**
       * Implements the method in the interface {@link Runnable#run()}. Runs
       * the publisher by executing the URL. The URL that is published will be
       * removed from the session private object.
       */
      public void run()
      {

         Exception ex = null;
         try
         {
            executeUrl(pubUrl);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         catch (IOException | SAXException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            request.setSessionPrivateObject(PUB_URLS_IN_PROGRESS, null);
         }
         else
         {
            Set inProgress = (Set) request
               .getSessionPrivateObject(PUB_URLS_IN_PROGRESS);
            if (inProgress != null)
               inProgress.remove(pubUrl);
         }
      }

      /**
       * Execute the supplied URL. This assumes the URL points to a publish
       * request and the respose to URL request is an XML document. If the
       * supplied edition is already running, attempt is made continually to
       * initiate publishing with a specifie interval of time.
       * 
       * @param url the URL object to execute, assumed niot <code>null</code>.
       * @throws InterruptedException if this thread is interrupted by system.
       * @throws IOException any error executiong the URL.
       * @throws SAXException if there us an error parsing the response as XML
       *            document.
       */
      private void executeUrl(URL url)
         throws InterruptedException, IOException, SAXException
      {
            String code = "";
            do
            {
               Thread.sleep(m_pollingTime);
               URLConnection httpCon = url.openConnection();
               httpCon.connect();
               Object obj = httpCon.getContent();
               Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  (InputStream) obj, false);
               NodeList nl = doc.getElementsByTagName("response");
               code = ((Element) nl.item(0)).getAttribute("code");
               System.out.println("code = " + code);
            }
            while (code.equalsIgnoreCase("inprogress"));
      }
   }
}
