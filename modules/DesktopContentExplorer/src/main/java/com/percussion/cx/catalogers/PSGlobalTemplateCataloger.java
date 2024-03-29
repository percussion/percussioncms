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
package com.percussion.cx.catalogers;

import com.percussion.cms.PSCmsException;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * This class catalogs all global template names found in rhythmyx.
 */
public class PSGlobalTemplateCataloger
{
   /**
    * Constructs a new global template cataloger.
    * 
    * @param urlBase the base URL to use for the catalog request, not
    *    <code>null</code>.
    * @throws PSCmsException for any error.
    */
   public PSGlobalTemplateCataloger(URL urlBase) throws PSCmsException
   {
      if (urlBase == null)
         throw new IllegalArgumentException("urlBase cannot be null");
      
      try
      {
         URL url = new URL(urlBase, 
            "sys_psxCataloger/getGlobalTemplates.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            url.openStream(), false);
         
         fromXml(doc.getDocumentElement());
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSContentExplorerErrors.CATALOG_ERROR, e
            .getMessage());
      }
   }
   
   /**
    * Loads the global template names from the supplied XML element. The 
    * expected DTD is:
    * &lt;!ELEMENT GlobalTemplates (Template*)&gt;
    * &lt;!ELEMENT Template EMPTY&gt;
    * &lt;!ATTLIST Template
    *    name CDATA #REQUIRED
    * &gt;
    * 
    * @param elemRoot the XML element from which to load the global template 
    *    names, assumed not <code>null</code>.
    * @throws PSUnknownNodeTypeException for any unknown XML node.
    */
   private void fromXml(Element elemRoot) throws PSUnknownNodeTypeException
   {
      m_globalTemplates.clear();

      PSXMLDomUtil.checkNode(elemRoot, ROOT_ELEM);

      NodeList templates = elemRoot.getElementsByTagName(TEMPLATE_ELEM);
      for (int i = 0; i<templates.getLength(); i++)
      {
         Element template = (Element) templates.item(i);
         m_globalTemplates.add(template.getAttribute(NAME_ATTR));
      }
   }

   /**
    * Get the collection of all defined global template names.
    * 
    * @return a collection of all global template names as <code>String</code>
    *    objects, never <code>null</code>, may be empty.
    */
   public Collection getGlobalTemplates()
   {
      return Collections.unmodifiableCollection(m_globalTemplates);
   }

   /**
    * The collection of all global template names, reset with each call to 
    * {@link #fromXml(Element)}, never <code>null</code>, may be empty.
    */
   private Collection m_globalTemplates = new ArrayList();
   
   // private XML constants
   private static final String ROOT_ELEM = "GlobalTemplates";
   private static final String TEMPLATE_ELEM = "Template";
   private static final String NAME_ATTR = "name";
}