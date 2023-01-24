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

package com.percussion.design.catalog.security;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSServerCatalogHandler class implements cataloging of
 * servers. This request type is used to locate the servers available
 * through a back-end driver. Not all drivers are capable of locating
 * servers.
 * <p>
 * Server catalog requests are sent to the server using the
 * PSXSecurityServerCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityServerCatalog      (providverName)&gt;
 *
 *    &lt;-- the name of the driver being queried.
 *     --&gt;
 *    &lt;!ELEMENT providerName         (#PCDATA)&gt;
 * </pre>
 *
 * The PSXSecurityServerCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityServerCatalogResults (providerName, Server*)&gt;
 *
 *    &lt;-- the name of the provider which was queried.
 *     --&gt;
 *    &lt;!ELEMENT providerName               (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT Server                     (name)&gt;
 *
 *    &lt;-- the name of the server.
 *     --&gt;
 *    &lt;!ELEMENT name                        (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSServerCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSServerCatalogHandler()
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
    *       <td>Server</td>
    *       <td>yes</td></tr>
    *   <tr><td>ProviderName</td>
    *       <td>the name of the security provider being queried</td>
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
      if ( (sTemp == null) || !"security".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"Server".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      String providerName = (String)req.get("ProviderName");
      if (providerName == null)
         throw new IllegalArgumentException("reqd prop not specified: ProviderName");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(
                                       reqDoc, "PSXSecurityServerCatalog");

      PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                       "providerName", providerName);

      return reqDoc;
   }
}

