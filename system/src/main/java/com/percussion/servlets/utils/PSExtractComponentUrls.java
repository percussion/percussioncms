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
package com.percussion.servlets.utils;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

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
