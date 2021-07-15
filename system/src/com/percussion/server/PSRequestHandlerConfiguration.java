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

package com.percussion.server;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Provides the def for each loadable request handler.  Utilizes a configuration
 * file with the following format:
 *
 * <pre><code>
 *  &lt;!--
 *     This file contains the DTD that specifies how the Request handler
 *     configuration file must be organized. The server will attempt to load all
 *     classes specified in the config. Each class must implement the
 *     IPSLoadableRequestHandler interface.
 *  --&gt;
 *
 *  &lt;!ELEMENT RequestHandlerDefs (RequestHandlerDef+)&gt;
 *
 *  &lt;!--
 *
 *     Attributes:
 *     handlerName - the unique name by which this handler is identified.  Used
 *        to identify the handler for non-rooted requests using the Rhythmyx
 *        Reqeust type CGI variable to identify the handler.  This will take the
 *        form of "extdata-&lt;handlerName&gt;".
 *
 *     className - the fully qualified class name that implements the required
 *        interface.
 *
 *     configFile - the relative name of the file. The handler opens the file
 *        and passes a stream to the handler during initialization. The file
 *        should be located in /&lt;serverroot&gt;/config/requestHandlers. If
 *        not present, null is passed to the init method.
 *  --&gt;
 *  &lt;!ELEMENT RequestHandlerDef (RequestRoots)&gt;
 *  &lt;!ATTLIST RequestHandlerDef
 *     handlerName   CDATA #REQUIRED
 *     className   CDATA #REQUIRED
 *     configFile  CDATA #IMPLIED
 *     &gt;
 *
 *
 *
 *  &lt;!--
 *     The set of request names which this handler wants to process. Cannot
 *     include multiple levels or wildcards. To create the full URL,
 *     add the server root to this name.
 *     For example, if the RequestRoot is 'foo', the full URL would be:
 *        /&lt;serverRoot&gt;/foo/[anything else is allowed including
 *        resource name, extensions, query string, anchor etc.]
 *
 *     Request handlers are loaded before applications. Therefore, if a root
 *     like 'foo' is chosen, care must be excercised to make sure the name
 *     is unique. If there also happens to be an application and resource
 *     by this name, and that application is active, the request handler will
 *     never get to process the request, it will always be handled by the
 *     application handler.
 *     For a handler to gain control, not only the request root, but also the
 *     HTTP request type must match one of those supplied for the given root.
 *  --&gt;
 *  &lt;!ELEMENT RequestRoots (RequestRoot+)&gt;
 *
 *  &lt;!--
 *
 *     Attributes:
 *     baseName - The request root that this handler will respond to.
 *       Leading / is optional.
 *  --&gt;
 *  &lt;!ELEMENT RequestRoot (RequestType+)&gt;
 *  &lt;!ATTLIST RequestRoot
 *     baseName CDATA #REQUIRED
 *     &gt;
 *
 *  &lt;!--
 *     One of the HTTP methods, such as GET or POST, case insensitive.
 *  --&gt;
 *  &lt;!ELEMENT RequestType (#PCDATA)&gt;
 *
 *
 *
 *  &lt;!--
 *     Sample XML Doc
 *
 *  &lt;?xml encoding="UTF-8" version="1.0" ?&gt;
 *
 *  &lt;RequestHandlerDefs&gt;
 *     &lt;RequestHandlerDef className="com.percussion.pubs.PublisherBroker"
 *           configFile="pubbroker.xml"&gt;
 *        &lt;RequestRoots&gt;
 *           &lt;RequestRoot baseName="rxpublish"&gt;
 *              &lt;RequestType&gt;POST&lt;/RequestType&gt;
 *              &lt;RequestType&gt;GET&lt;/RequestType&gt;
 *           &lt;/RequestRoot&gt;
 *        &lt;RequestRoots&gt;
 *     &lt;/RequestHandlerDef&gt;
 *  &lt;/RequestHandlerDefs&gt;
 *
 *  --&gt;
 * </code></pre>
 *
 */
public class PSRequestHandlerConfiguration
{
   /**
    * Constructor, loads the config file and creates all handler defs.
    *
    * @throws PSServerException if there are any errors
    */
   public PSRequestHandlerConfiguration() throws PSServerException
   {
      try
      {
         // load the config file
         m_handlerDefs = new ArrayList();
         Document cfgDoc = getConfig();

         // if no config file, we're done
         if (cfgDoc == null)
            return;

         // validate that it is the correct type
         Element root = cfgDoc.getDocumentElement();
         if (root == null)
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, CONFIG_ROOT);

         //make sure we got the correct root node tag
         if (false == CONFIG_ROOT.equals (root.getNodeName()))
         {
            Object[] args = { CONFIG_ROOT, root.getNodeName() };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }


         // walk it and load the handlers
         PSXmlTreeWalker tree = new PSXmlTreeWalker(cfgDoc);

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         String searchEl;
         IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
         String rxRootDir = (String) rxInfo
               .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);

         File rhConfigDir = new File(rxRootDir + File.separator
               + CONFIG_DIR, HANDLER_CONFIG_DIR);

         // find each handler def
         searchEl = "RequestHandlerDef";
         Element defEl = tree.getNextElement(searchEl, firstFlags);
         while (defEl != null)
         {
            // determine handler name
            String handlerName = defEl.getAttribute("handlerName");
            if (handlerName == null || handlerName.length() == 0)
            {
               Object[] args = {searchEl, "handlerName", handlerName};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            // determine class name
            String className = defEl.getAttribute("className");
            if (className == null || className.length() == 0)
            {
               Object[] args = {searchEl, "className", className};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            // may not have a cfg file
            File rhConfigFile = null;
            String cfgFileName = defEl.getAttribute("configFile");
            if (cfgFileName != null && cfgFileName.length() != 0)
               rhConfigFile = new File(rhConfigDir, cfgFileName);
               
            // get the roots
            HashMap roots = new HashMap();
            String rootsElName = "RequestRoots";

            Element rootsEl = tree.getNextElement(rootsElName, firstFlags);
            if (rootsEl == null)
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_NULL, rootsElName);

            String rootElName = "RequestRoot";
            Element rootEl = tree.getNextElement(rootElName, firstFlags);

            // must have at least one root
            if (rootEl == null)
               throw new PSUnknownDocTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_NULL, rootElName);

            while (rootEl != null)
            {
               String rootName = rootEl.getAttribute("baseName");
               if (rootName == null)
               {
                  Object[] args = {rootElName, "baseName", rootName};
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
               }

               // strip off leading "/" if there
               if (rootName.startsWith("/"))
                  rootName = rootName.substring(1);

               // get its methods
               ArrayList methods = new ArrayList();
               String typeElName = "RequestType";
               Element typeEl = tree.getNextElement(typeElName, firstFlags);

               // must have at least one
               if (typeEl == null)
                  throw new PSUnknownDocTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_NULL, typeElName);

               while (typeEl != null)
               {
                  methods.add(tree.getElementData(typeEl));

                  typeEl = tree.getNextElement(typeElName, nextFlags);
               }

               // add to list of roots
               roots.put(rootName, methods);

               // find the next one
               tree.setCurrent(rootEl);
               rootEl = tree.getNextElement(rootElName, nextFlags);
            }

            // create the def
            PSRequestHandlerDef def = new PSRequestHandlerDef(handlerName,
               className, rhConfigFile, roots.keySet().iterator());

            // add the methods
            Iterator i = roots.keySet().iterator();
            while (i.hasNext())
            {
               String rootName = (String)i.next();
               def.addRequestMethods(rootName,
                  ((ArrayList)roots.get(rootName)).iterator());
            }

            // add it to the list
            m_handlerDefs.add(def);

            // find the next one
            tree.setCurrent(defEl);
            defEl = tree.getNextElement(searchEl, nextFlags);
         }

      }
      catch (Exception e)
      {
         if (!(e instanceof PSServerException))
         {
            // wrap it
            e = new PSServerException(
               IPSServerErrors.REQUEST_HANDLER_CONFIG_ERROR, e.toString());
         }
         throw (PSServerException)e;
      }
   }


   /**
    * Returns an iterator over zero or more PSRequestHandlerDef objects.
    */
   public Iterator getHandlerDefs()
   {
      return m_handlerDefs.iterator();
   }


   /**
    * Returns the Document object that contains the loadable request handler
    * definitions.
    * @return The document, may be <code>null</code> if there is no config file.
    * @throws PSServerException if there are any errors.
    */
   private Document getConfig() throws PSServerException
   {
      Document doc = null;

      IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
      String rxRootDir = (String) rxInfo
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
      
      try
      {
         // get the xml file from disk
         File cfg = new File(rxRootDir + File.separator + CONFIG_DIR,
               CONFIG_FILE_NAME);
         if (cfg.exists())
         {
            try(FileInputStream fIn = new FileInputStream(cfg)) {
               doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
            }
         }
      }
      catch (Exception e)
      {
         throw new PSServerException(
            IPSServerErrors.REQUEST_HANDLER_CONFIG_ERROR, e.toString());
      }

      return doc;
   }

   /**
    * List of request handler definitions.  Initialized in constructor,
    * never <code>null</code> after that.
    */
   private ArrayList m_handlerDefs = null;

   /**
    * Directory off the Rhythmyx root where the config file is located.
    */
   private static final String CONFIG_DIR = "rxconfig/Server";

   /**
    * Name of the handler config file
    */
   private static final String CONFIG_FILE_NAME = "RequestHandlers.xml";

   /**
    * Name of the subdirectory of the config directory where the config files
    * for each request handler are located.
    */
   private static final String HANDLER_CONFIG_DIR = "requestHandlers";

   /**
    * Constant for the request handler config doc root element.
    */
   private static final String CONFIG_ROOT = "RequestHandlerDefs"; 
}
