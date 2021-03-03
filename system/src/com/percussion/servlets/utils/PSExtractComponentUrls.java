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
package com.percussion.servlets.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This contains methods that extract the component urls needed to create 
 * the existing l&f of the HTML interface. The object is created with 
 * a document obtained from an internal request, and after creation allows
 * the caller to retrieve the header, user status and left nav urls.
 * <p>
 * This class is primarily intended for use in JSPs to build up a full page
 * 
 * @author dougrand
 *
 */
public class PSExtractComponentUrls
{
   private Map<String,String> m_componentUrls = null;
   
   /**
    * Ctor
    * @param document the document to use for lookups, never <code>null</code>
    */
   public PSExtractComponentUrls(Document document)
   {
      if (document == null)
      {
         throw new IllegalArgumentException("document may not be null");
      }
      m_componentUrls = new HashMap<>();
      NodeList nl = document.getElementsByTagName("component");
      int count = nl.getLength();
      for(int i = 0; i < count; i++)
      {
         Element el = (Element) nl.item(i);
         String name = el.getAttribute("name");
         Element url = (Element) el.getElementsByTagName("url").item(0);
         String urlstr = url.getTextContent();
         m_componentUrls.put(name, urlstr);
      }
   }
   
   /**
    * Extract the named slot's url from the document
    * @param name the name, never <code>null</code> or empty
    * @return the url or <code>null</code> if not found
    */
   public String getComponentUrl(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      return m_componentUrls.get(name);
   }
}
