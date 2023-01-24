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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.objectstore.PSSecurityProviderInstanceSummary;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a system exit which obtains the security provider information from
 * the server configuration object.
 * See the description of <code>processResultDocument()</code> for details of
 * its functioning.
 */
public class PSGetSecurityProvidersExit extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * See interface for description.
    *
    * @return Always <code>false</code>.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Creates an element ("SecurityProviders") and adds it to the root element
    * of the document. If no root element exists, then makes it the root
    * element.
    * Obtains the security providers from the server configuration object.
    * Then for each security provider creates an element
    * ("PSXSecurityProviderInstanceSummary"). Adds the provider type as attribute and
    * instance name as child element of this element.
    * The returned Xml document is based on the following DTD:
    *
    * <code><pre>
    *
    *  &lt;!ELEMENT SecurityProviders (PSXSecurityProviderInstanceSummary*)&gt;
    *  &lt;!ELEMENT PSXSecurityProviderInstanceSummary (name)&gt;
    *   &lt;!ATTLIST
    *    typeName  CDATA       #REQUIRED
    *    typeId     CDATA       #REQUIRED
    *   &gt;
    *
    *  &lt;!ELEMENT name       (#PCDATA)&gt;
    *
    * </pre></code>
    *
    * <p>This exit expects no html parameter.
    *
    * @param params unused, may be <code>null</code> or empty
    * @param request request context. Guaranteed not <code>null</code> by the
    * interface.
    * @param resultDoc The supplied document. Guaranteed not <code>null</code>
    * by the interface.
    *
    * @return The modified Xml doc, never <code>null</code>
    *
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // add the "SecurityProviders" element
      Element root = resultDoc.getDocumentElement();
      if (root == null)
         root = PSXmlDocumentBuilder.createRoot(resultDoc, XML_NODE_NAME);
      else
      {
         root = PSXmlDocumentBuilder.addEmptyElement(resultDoc, root,
            XML_NODE_NAME);
      }

      // get the server configuration object
      PSServerConfiguration config = PSServer.getServerConfiguration();
      PSCollection securityProviders = config.getSecurityProviderInstances();
      if ((securityProviders != null) && (securityProviders.size() > 0))
      {
         int size = securityProviders.size();
         for(int i=0; i < size; i++)
         {
            PSSecurityProviderInstanceSummary provider =
               new PSSecurityProviderInstanceSummary(
                  (PSSecurityProviderInstance)securityProviders.get(i));

            Element el = provider.toXml(resultDoc);

            // append this provider instance to the root
            root.appendChild(el);
         }
      }
      return resultDoc;
   }

   // XML constants for Node names and attribute names
   private static final String XML_NODE_NAME = "SecurityProviders";


}


