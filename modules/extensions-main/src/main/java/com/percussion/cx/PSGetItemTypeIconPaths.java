/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.cx;

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Exit to get the icon paths for the supplied locators. Gets the input from the
 * value of ItemLocators parameter from the request. It is a required parameter
 * and returns unmodified result document if it is missing. Expects the input in
 * the form of a string representation of XML consisting of PSXLocator elements
 * as direct children of root element. Gets the content type icon paths from
 * <code>PSItemDefManager</code> and returns the results per the following
 * DTD. See {@link PSItemDefManager#getContentTypeIconPaths(List)} for more
 * details on how paths are generated for items.
 * 
 * <pre>
 *        &lt;!ELEMENT ItemIcons (Item*)&gt;
 *        &lt;!ELEMENT Item (#PCDATA)&gt; 
 *        &lt;!ATTLIST Item cid CDATA #REQUIRED&gt;
 *        &lt;!ATTLIST Item rev CDATA #REQUIRED&gt; 
 *        &lt;!ATTLIST Item path CDATA #REQUIRED&gt; 
 * </pre>
 * 
 * The returned resultDoc consists of an empty ItemIcons element if the input is
 * not as expected by this exit.
 */
public class PSGetItemTypeIconPaths implements IPSResultDocumentProcessor
{
   /*
    * (non-Javadoc)
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[], com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(doc, "ItemIcons");
      resultDoc = doc;
      String itemsXml = request.getParameter("ItemLocators");
      if (StringUtils.isBlank(itemsXml))
      {
         log.warn("Missing ItemLocators parameter in the request. Skipping the execution of exit and returning unmodified result document.");
         return resultDoc;
      }
      try
      {
         List<PSLocator> locs = getLocators(itemsXml);
         if (locs.isEmpty())
         {
            return resultDoc;
         }
         addItemElements(doc,locs);
      }
      catch (IOException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
      catch (SAXException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
      return resultDoc;
   }

   /**
    * Helper method to create a list of locators from the given xml document
    * string consisting of PSXLocator elements.
    * 
    * @param itemsXml assumed not <code>null</code> or empty.
    * @return List of PSLocator objects never <code>null</code>, may be empty.
    * @throws IOException
    * @throws SAXException
    */
   private List<PSLocator> getLocators(String itemsXml) throws IOException,
         SAXException
   {
      List<PSLocator> locs = new ArrayList<>();
      Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
            itemsXml), false);
      NodeList nl = doc.getElementsByTagName("PSXLocator");
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element elem = (Element) nl.item(i);
         try
         {
            locs.add(new PSLocator(elem));
         }
         catch (PSUnknownNodeTypeException e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         }
      }
      return locs;
   }

   /**
    * Helper method to add item elements to supplied document. The item elements
    * are added as per the DTD mentioned in class description.
    * 
    * @param locs assumed not <code>null</code>.
    * @return Document consisting of icon paths.
    */
   private void addItemElements(Document doc, List<PSLocator> locs)
   {
      Element root = doc.getDocumentElement();
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      Map<PSLocator, String> map = defMgr.getContentTypeIconPaths(locs);
      Iterator<PSLocator> iter = map.keySet().iterator();
      while (iter.hasNext())
      {
         PSLocator loc = iter.next();
         String path = StringUtils.defaultString(map.get(loc));
         Element elem = doc.createElement("Item");
         elem.setAttribute("cid", "" + loc.getId());
         elem.setAttribute("rev", "" + loc.getRevision());
         elem.setAttribute("path", path);
         root.appendChild(elem);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger log = LogManager.getLogger(PSGetItemTypeIconPaths.class);
}
