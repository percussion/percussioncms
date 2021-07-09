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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.extensions.publishing;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class publishes a manual edition for preview purposes. This is also
 * known as just in time publishing.
 */
public class PSPublishEditionForPreview extends PSDefaultExtension
      implements
         IPSRequestPreProcessor,
         IPSResultDocumentProcessor
{

   private static final Logger log = LogManager.getLogger(PSPublishEditionForPreview.class);

   /**
    * Inner class to implement a work thread to check on publishing status
    */
   class CheckPub implements Runnable
   {
      /**
       * Path to the rhythmyx application, must never be <code>null</code>
       * after creation.
       */
      URL mi_requestPath = null;

      /**
       * Ctor
       * 
       * @param str url string, must never be <code>null</code>
       */
      public CheckPub(String str) throws MalformedURLException {
         this.mi_requestPath = new URL(str);
      }

      /**
       * Convert data read from passed in reader into a string
       * 
       * @param reader input reader, may be <code>null</code>
       * @return a {@link String} holding the information from the reader,
       *         returns <code>null</code> if the user passed a
       *         <code>null</code> reader.
       */
      public String toString(BufferedReader reader)
      {
         if (reader != null)
         {
            StringBuffer returnValue = new StringBuffer();
            String nextLine = null;
            try
            {
               do
               {
                  nextLine = reader.readLine();
                  if (nextLine != null)
                  {
                     returnValue.append(nextLine);
                  }
               }
               while (nextLine != null);
            }
            catch (java.io.IOException ex)
            {
            }
            return returnValue.toString();
         }
         else
         {
            return null;
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Runnable#run()
       */
      public void run()
      {
         Logger l = LogManager.getLogger(getClass());


            int count = 0;
            String stat = null;
            while (true)
            {
               count++;
               try(InputStream content = mi_requestPath.openStream()) {
                  //possible performance hit, returns an InputStream

                  // JDS - the following MAY be more efficient then PSCopyStream to
                  // read the info.
                  try (InputStreamReader inputReader = new InputStreamReader(content)) {
                     try(BufferedReader reader = new BufferedReader(inputReader)) {
                        String tmpStatus = toString(reader);
                        reader.close();

                        stat = tmpStatus;
                        content.close();
                        // TODO: Ideally this code should check for the edition
                        // being in an in process state and continue rather than
                        // counting down.
                        if (stat != null && stat.indexOf("notInProgress") >= 0)
                           break;
                        Thread.sleep(1000);
                        if (count > 10)
                           break;
                     }
                  }
               } catch (IOException | InterruptedException e) {
                  l.error("Error while reading status from publisher", e);
               }
            }
         return;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSExtensionProcessingException
   {
      preProcessRequest(params, request);
      return resultDoc;
   } //end processResultDocument

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSExtensionProcessingException
   {
      Logger l = LogManager.getLogger(getClass());

      if (params.length < 3)
      {
         l.error("Insufficient parameters specified: {}", params.length);
         throw new IllegalArgumentException("The first three parameters"
               + " are required, see log for details");
      }

      //Required params
      String editionId = params[0].toString();
      //assemblyvariantid will be written to the rxeditionitem table
      String assemblyVariantId = params[1].toString();
      //Optional params if this is not sent in we assume that the sys_variant
      // in the request is the preview variant
      String previewVariantId = params[2].toString();

      // The support Application is used in conjunction with the query and
      // update resources to manipulate a list of content ids to publish
      // for the manual edition
      String supportApplication = getArg(params, 3, "rx_pubPreviewEdition");
      m_queryResource = getArg(params, 4, "queryEdition");
      m_updateResource = getArg(params, 5, "updateEdition");

      // make full paths
      m_queryResource = supportApplication + "/" + m_queryResource;
      m_updateResource = supportApplication + "/" + m_updateResource;

      //Optional http params
      String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      String revisionId = request.getParameter(IPSHtmlParameters.SYS_REVISION);
      String variantId = request.getParameter(IPSHtmlParameters.SYS_VARIANTID);
      String context = request.getParameter(IPSHtmlParameters.SYS_CONTEXT);

      // get session id for the puburl
      String userSession = request.getUserSessionId();
      request.printTraceMessage("usersession " + userSession);
      //System.out.println("usersession " + userSession );

      //Compare the previewvariantid and the sys_variantid in the request
      //if they are equal continue. If not then the user clicked on the wrong
      // variant
      request.printTraceMessage("previewvariant  " + previewVariantId
            + " and sys_variant " + variantId);
      //System.out.println("previewvariant "+ previewVariantId +" and
      // sys_variant " + variantId );
      if (previewVariantId != null)
      {
         if (!(previewVariantId.trim().equalsIgnoreCase(variantId)))
         {
            //System.out.println("previewvariant does not equal "+
            // previewVariantId +" and sys_variant " + variantId );
            request.printTraceMessage("previewvariant does not equal "
                  + previewVariantId + " and sys_variant " + variantId);

            return; //the user clicked on the wrong variant
         }
      }

      //Check to make sure all the above request variables are present
      if (contentId == null || revisionId == null || variantId == null
            || context == null)
      {
         l.warn("One or more necessary parameters were missing");
         return;
      }
      // processing the various methods to run the query
      // and updateedtion making calls to the support app
      try
      {
         // If the required parameters editionId and assemblyvariantid and
         //If context is 0, meaning a preview, then do the publish on demand.
         // Otherwise do nothing
         if (editionId != null || assemblyVariantId != null
               || context.equals("0"))
         {

            String host = request.getOriginalHost();
            String port = "80";
            if (request.getOriginalPort() > 0)
            {
               port = String.valueOf(request.getOriginalPort());
            }

            String appName = "Rhythmyx/sys_pubHandler/publisher.htm";
            StringBuffer pubUrl = getAppURL(host, port, appName);
            pubUrl.append("?editionid=");
            pubUrl.append(editionId);
            pubUrl.append("&PUBAction=publish");
            pubUrl.append("&pssessionid=");
            pubUrl.append(userSession);

            request.printTraceMessage("About to setPreviewPage");

            setPreviewPage(editionId, assemblyVariantId, contentId, revisionId,
                  request);

            // There is no check to see if publish was successful. User must
            // check Rx logs
            try
            {
               URL url = new URL(pubUrl.toString());
               URLConnection httpCon = url.openConnection();
               //Will return an HttpUrlConnection

               httpCon.connect();
               //possible performance hit, could use HttpUrlConnection and call
               // disconnect when done
               httpCon.getContent();
               //Read the url information in, appears to be equiv. to a browser
               // Request
               request.printTraceMessage("The published URL "
                     + pubUrl.toString());
            }
            catch (IOException e)
            {
               l.error("Error while processing exit", e);
               //return resultDoc;
               return;
            }

            // Parse status to determine when publishing is done
            appName = "Rhythmyx/sys_pubHandler/publisher.xml";
            StringBuffer pubStatusUrl = getAppURL(host, port, appName);

            pubStatusUrl.append("?editionid=");
            pubStatusUrl.append(editionId);
            pubStatusUrl.append("&PUBAction=status");
            pubStatusUrl.append("&pssessionid=");
            pubStatusUrl.append(userSession);

            CheckPub cp = new CheckPub(pubStatusUrl.toString());
            Thread th = new Thread(cp);
            th.run();
            //Control will return from this thread after the edition has
            // finished publishing.

            request
                  .printTraceMessage("About to remove Preview page from the RXEDITIONITEM table");
            //Now clean up preview page info
            removePreviewPage(editionId, variantId, contentId, revisionId,
                  request);

         }
         else
         {
            // We are in a publishable context.
            // How can we ensure that once in a publishable context. the
            // entire call stack for this method is flushed?
            request
                  .printTraceMessage("IN A PUBLISHABLE CONTEXT NOT IN PREVIEW");
         }
      } //end of try
      catch (Exception e)
      {
         l.error("Exception while processing exit", e);
      }
   }

   /**
    * Return the given parameter if defined, the default if not defined.
    * 
    * @param params an array of parameters, assumed never <code>null</code>
    * @param index an index into the parameter array, assumed zero or greater.
    * @param defaultValue a default value, may be <code>null</code>
    * @return the parameter value or the default
    */
   private String getArg(Object[] params, int index, String defaultValue)
   {
      if (index >= params.length
            || params[index].toString().trim().length() == 0)
      {
         return defaultValue;
      }
      else
      {
         return params[index].toString();
      }
   }

   /**
    * Generate a url to access an application by name
    * 
    * @param host The hostname, must never be <code>null</code> or empty
    * @param port The port, must never be <code>null</code> or empty
    * @param appName The application oname, must never be <code>null</code> or
    *           empty
    * @return a string buffer, never <code>null</code>, which allows further
    *         manipulation by the caller.
    */
   protected StringBuffer getAppURL(String host, String port, String appName)
   {
      if (host == null || host.trim().length() == 0)
      {
         throw new IllegalArgumentException("host may not be null or empty");
      }
      if (port == null || port.trim().length() == 0)
      {
         throw new IllegalArgumentException("port may not be null or empty");
      }
      if (appName == null || appName.trim().length() == 0)
      {
         throw new IllegalArgumentException("appName may not be null or empty");
      }
      StringBuffer appURL = new StringBuffer();
      appName = "Rhythmyx/sys_pubHandler/publisher.xml";
      appURL.append("http://");
      appURL.append(host);
      appURL.append(":");
      appURL.append(port);
      appURL.append("/");
      appURL.append(appName);

      return appURL;
   }

   // --- START SECTION FOR PERSISTING AND READING PREVIEW INFO
   // -----------------------------------------------------
   /**
    * Persist the information that uniquely identifies the page that is
    * currently being previewed Note that the contentlists that perform preview
    * will, at least initially, be dependent on the specific implemention of
    * these methods.
    * @param editionId the edition id, assumed not <code>null</code> and not 
    * empty
    * @param variantId the variant id, assumed not <code>null</code> and not 
    * empty
    * @param contentId the content id, assumed not <code>null</code> and not 
    * empty
    * @param revisionId the revision id, assumed not <code>null</code> and not 
    * empty
    * @param request the request context, assumed not <code>null</code>
    * @throws PSExtensionProcessingException on internal errors
    * @throws PSConversionException if the row already exists
    */
   private void setPreviewPage(String editionId, String variantId,
         String contentId, String revisionId, IPSRequestContext request)
         throws PSExtensionProcessingException, PSConversionException
   {

      request
            .printTraceMessage("In PSPublishEditionForPreview, above call to rowExists");
      //We do a select prior to the insert in order to avoid a sql error during
      // concurrent use
      if (rowExists(editionId, variantId, contentId, revisionId, request) == true)
         throw new PSConversionException(0, "row exists in query edition.");
      {
         IPSInternalRequest iReq = null;
         try
         {
            Map paramMap = new HashMap();
            paramMap.put(EDITION_PARAM, editionId);
            paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantId);
            paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
            paramMap.put(IPSHtmlParameters.SYS_REVISION, revisionId);
            paramMap.put("DBActionType", "INSERT");

            iReq = request
                  .getInternalRequest(m_updateResource, paramMap, false);
            iReq.performUpdate();
         }
         catch (Exception e) //exception
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }
         finally
         {
            if (iReq != null)
               iReq.cleanUp();
         }

      }

      request.printTraceMessage("Finished setPreviewPage");

   }

   /**
    * Remove the information that uniquely identifies the page that is currently
    * being previewed Note that the contentlists that perform preview will, at
    * least initially, be dependent on the specific implemention of these
    * methods.
    * @param editionId the edition id, assumed not <code>null</code> and not 
    * empty
    * @param variantId the variant id, assumed not <code>null</code> and not 
    * empty
    * @param contentId the content id, assumed not <code>null</code> and not 
    * empty
    * @param revisionId the revision id, assumed not <code>null</code> and not 
    * empty
    * @param request the request context, assumed not <code>null</code>
    * @throws PSExtensionProcessingException on internal errors 
    */
   private void removePreviewPage(String editionId, String variantId,
         String contentId, String revisionId, IPSRequestContext request)
         throws PSExtensionProcessingException
   {
      IPSInternalRequest iReq = null;

      try
      {
         Map paramMap = new HashMap();
         paramMap.put(EDITION_PARAM, editionId);
         paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantId);
         paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
         paramMap.put(IPSHtmlParameters.SYS_REVISION, revisionId);
         paramMap.put("DBActionType", "DELETE");

         iReq = request.getInternalRequest(m_updateResource, paramMap, false);
         iReq.performUpdate();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         if (iReq != null)
            iReq.cleanUp();
      }

      request.printTraceMessage("Finished removePreviewPage");

   }

   /**
    * Check if row exists.
    * @param editionId the edition id, assumed not <code>null</code> and not 
    * empty
    * @param variantId the variant id, assumed not <code>null</code> and not 
    * empty
    * @param contentId the content id, assumed not <code>null</code> and not 
    * empty
    * @param revisionId the revision id, assumed not <code>null</code> and not 
    * empty
    * @param request the request context, assumed not <code>null</code>
    * @throws PSExtensionProcessingException on internal errors
    * @throws PSConversionException if the query application is not found
    */
   private boolean rowExists(String editionId, String variantId,
         String contentId, String revisionId, IPSRequestContext request)
         throws PSExtensionProcessingException, PSConversionException
   {
      boolean returnValue = false;
      IPSInternalRequest internalReq = null;
      Map paramMap = new HashMap();
      paramMap.put(EDITION_PARAM, editionId);
      paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantId);
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
      paramMap.put(IPSHtmlParameters.SYS_REVISION, revisionId);

      internalReq = request
            .getInternalRequest(m_queryResource, paramMap, false);
      if (internalReq == null)
         throw new PSConversionException(0,
               "queryEdition application not found.");

      Document doc = internalReq.getResultDoc();
      NodeList nodes = doc.getElementsByTagName(PARAMS_ELEM);
      if (nodes.getLength() <= 0)
         throw new PSConversionException(0, "No PARAMS_ELEM element found.");

      //checking returned params
      // there is only be one editionid per revision/contentid
      Node currentEdition = nodes.item(0);
      //going to check to see if edition element was returned
      editionId = currentEdition.getNodeValue();
      if (editionId != null)
      {
         returnValue = true; //the element edtionId exists
      }

      request.printTraceMessage("bottom of rowExists and returnValue=="
            + returnValue);

      return returnValue;
   } //end rowExists

   /**
    * The params element used in the XML document returned from the QUERYEDITION
    * resource, never <code>null</code>.
    */
   private static final String PARAMS_ELEM = "Params";

   /**
    * The edition param used in the map of params sent via the internal request
    */
   private static final String EDITION_PARAM = "sys_editionid";

   /**
    * Holds the query resource string that will query the list of content ids
    * for the manual edition. Initialized in
    * {@link #preProcessRequest(Object[], IPSRequestContext)}and never
    * <code>null</code> afterward.
    */
   private String m_queryResource;

   /**
    * Holds the update resource string that will modify the list of content ids
    * for the manual edition. Initialized in
    * {@link #preProcessRequest(Object[], IPSRequestContext)}and never
    * <code>null</code> afterward.
    */
   private String m_updateResource;
}
