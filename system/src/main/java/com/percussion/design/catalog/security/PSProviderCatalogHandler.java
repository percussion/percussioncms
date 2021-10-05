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

package com.percussion.design.catalog.security;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;


/**
 * The PSProviderCatalogHandler class implements cataloging of security
 * providers. This request type is used to locate the security providers
 * available for authentication and ACL membership on the E2 server.
 * <p>
 * Security provider catalog requests are sent to the server using the
 * PSXSecurityProviderCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityProviderCatalog    EMPTY&gt;
 * </pre>
 *
 * The PSXSecurityProviderCatalogResults XML document is sent as the
 * response. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityProviderCatalogResults (Provider*)&gt;
 *
 *    &lt;!ELEMENT Provider                     (name, fullname, description, ConnectionProperties?)&gt;
 *
 *    &lt;!--
 *       the name by which the provider should be referenced. This is
 *         the name to use wherever a security provider type is required.
 *     --&gt;
 *    &lt;!ELEMENT name                          (#PCDATA)&gt;
 *
 *    &lt;!--
 *       the provider's full name. This is a more presentable form of
 *         the name which can be used in lists, etc.
 *     --&gt;
 *    &lt;!ELEMENT fullname                      (#PCDATA)&gt;
 *
 *    &lt;!-- a brief description of the provider.
 *     --&gt;
 *    &lt;!ELEMENT description                   (#PCDATA)&gt;
 *
 *    &lt;!-- the connection properties required for login.
 *     --&gt;
 *    &lt;!ELEMENT ConnectionProperties           (ConnectionProperty*)&gt;
 *
 *    &lt;!-- a connection property which is required for login.
 *     --&gt;
 *    &lt;!ELEMENT ConnectionProperty             (name, description)&gt;
 *
 *    &lt;!-- the name of the connection property.
 *     --&gt;
 *    &lt;!ELEMENT name                            (#PCDATA)&gt;
 *
 *    &lt;!-- the description of the connection property.
 *     --&gt;
 *    &lt;!ELEMENT description                   (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSProviderCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSProviderCatalogHandler()
   {
      super();
   }

   /**
    * Format the catalog request based upon the specified request
    * information. The request information for this request type is:
    * <table border="2">
    *   <tr><th>Key</th>
    *       <th>Value</th>
    *       <th>Required</th></tr>
    *   <tr><td>RequestCategory</td>
    *       <td>security</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>Provider</td>
    *       <td>yes</td></tr>
    * </table>
    *
    * @param      req         the request information
    *
    * @return                 an XML document containing the appropriate
    *                         catalog request information
    */
   public Document formatRequest(java.util.Properties req)
   {
      String sTemp = (String)req.get("RequestCategory");
      if ( (sTemp == null) || !"security".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"Provider".equalsIgnoreCase(sTemp) ) {
         Object[] args = { "Provider", (sTemp == null ? "" : sTemp) };
         throw new IllegalArgumentException("req type invalid");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      PSXmlDocumentBuilder.createRoot(reqDoc, "PSXSecurityProviderCatalog");

      return reqDoc;
   }
}

