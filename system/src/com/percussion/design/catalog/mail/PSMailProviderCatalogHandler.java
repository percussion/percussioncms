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

package com.percussion.design.catalog.mail;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;


/**
 * The PSMailProviderCatalogHandler class implements cataloging of
 * mail providers. This request type is used to locate the mail providers
 * available to the E2 server for sending e-mail messages.
 * <p>
 * Mail provider catalog requests are sent to the server using the
 * PSXMailProviderCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXMailProviderCatalog EMPTY&gt;
 * </pre>
 *
 * The PSXMailProviderCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXMailProviderCatalogResults (Provider*)&gt;
 *
 *    &lt;!ELEMENT Provider   (name, fullName, description, Properties)&gt;
 *
 *    &lt;!--
 *       the name by which the mail provider should be referenced. This is
 *       the name to use wherever a mail provider is required.
 *     --&gt;
 *    &lt;!ELEMENT name           (SMTP)&gt;
 *
 *    &lt;!--
 *       the mail provider's full name. This is a more presentable form of
 *       the name which can be used in lists, etc.
 *     --&gt;
 *    &lt;!ELEMENT fullName       (#PCDATA)&gt;
 *
 *    &lt;!-- a brief description of the mail provider.
 *     --&gt;
 *    &lt;!ELEMENT description    (#PCDATA)&gt;
 *
 *    &lt;!--
 *       the properties required by this provider
 *     --&gt;
 *    &lt;!ELEMENT Properties     (Property*)&gt;
 *
 *    &lt;!-- a property which is required for access to this provider.
 *     --&gt;
 *    &lt;!ELEMENT Property       (name, description)&gt;
 *
 *    &lt;!-- the name of the provider's property.
 *     --&gt;
 *    &lt;!ELEMENT name             (#PCDATA)&gt;
 *
 *    &lt;!-- the description of the provider's property.
 *     --&gt;
 *    &lt;!ELEMENT description    (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSMailProviderCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSMailProviderCatalogHandler()
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
    *       <td>mail</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>MailProvider</td>
    *       <td>yes</td></tr>
    * </table>
    *
    * @param      req         the request information
    *
    * @return                 an XML document containing the appropriate
    *                         catalog request information
    *
    */
   public Document formatRequest(java.util.Properties req)
   {
      String sTemp = (String)req.get("RequestCategory");
      if ( (sTemp == null) || !"mail".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"MailProvider".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      PSXmlDocumentBuilder.createRoot(reqDoc, "PSXMailProviderCatalog");

      return reqDoc;
   }
}

