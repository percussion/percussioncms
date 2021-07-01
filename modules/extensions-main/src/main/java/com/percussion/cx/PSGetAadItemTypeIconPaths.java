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
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Gets the icon path for the items and sets it as iconPath attribute of item
 * elements in the resultDoc. Expects the item elements as direct children of
 * resultDoc with the following child elements "contentid", "revision",
 * "contentcheckoutusername" and "tiprevision". If the item is checked out by
 * the current user tip revision is used otherwise current revision is used to
 * get the icon path. The iconPath attribute value may be empty if not specified
 * on the content type corresponding to the item or if failed to obtain it.
 * Unmodified resultDoc is returned if the resultDoc does not contain expected
 * input.
 */
public class PSGetAadItemTypeIconPaths implements IPSResultDocumentProcessor
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      NodeList nl = resultDoc.getElementsByTagName("item");
      if (nl.getLength() < 1)
         return resultDoc;
      List<PSLocator> locs = buildLocators(nl, request.getUserName());
      Map<String, String> icmap = getIconPaths(locs);
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element item = (Element) nl.item(i);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(item);
         String cid = tree.getElementData("contentid");
         String icpath = StringUtils.defaultString(icmap.get(cid));
         item.setAttribute("iconPath", icpath);
      }

      return resultDoc;
   }

   /**
    * Creates a list of current or tip locators from the item elements of the
    * supplied node list. Gets the contentid,revision, checkout username and
    * tiprevision from contentid,revision,contentcheckoutusername and
    * tiprevision elements respectively.
    * 
    * @param nl list of item nodes assumed not <code>]null</code>.
    * @param userName name of the logged in user.
    * @return List of locators may be empty, never <code>null</code>.
    * 
    */
   private List<PSLocator> buildLocators(NodeList nl, String userName)
   {
      List<PSLocator> locs = new ArrayList<>();
      String uname = StringUtils.defaultString(userName);
      for (int i = 0; i < nl.getLength(); i++)
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(nl.item(i));
         String cid = tree.getElementData("contentid");
         if (StringUtils.isBlank(cid))
            continue;
         String rid = tree.getElementData("revision");
         String chkUser = tree.getElementData("contentcheckoutusername");
         if (uname.equalsIgnoreCase(chkUser))
         {
            rid = tree.getElementData("tiprevision");
         }
         locs.add(new PSLocator(cid, rid));
      }
      return locs;
   }

   /**
    * Gets the icon path map contentid and icon path for the supplied locators.
    * See {@link PSItemDefManager#getContentTypeIconPaths(List)} for details.
    * 
    * @param locs list of locators assumed not <code>null</code>.
    * @return Map of content id and icon path, never <code>null</code> may be
    *         empty.
    */
   private Map<String, String> getIconPaths(List<PSLocator> locs)
   {
      Map<String, String> result = new HashMap<>();
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      Map<PSLocator, String> icmap = defMgr.getContentTypeIconPaths(locs);
      for (PSLocator loc : icmap.keySet())
      {
         result.put("" + loc.getId(), icmap.get(loc));
      }
      return result;
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

   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger ms_log = LogManager.getLogger(PSGetAadItemTypeIconPaths.class);

}
