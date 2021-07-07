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
package com.percussion.cx;

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      catch (SAXException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
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
