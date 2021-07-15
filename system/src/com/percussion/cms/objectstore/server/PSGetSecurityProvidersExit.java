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


