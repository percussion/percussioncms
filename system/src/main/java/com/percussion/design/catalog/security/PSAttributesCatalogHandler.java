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

import java.util.StringTokenizer;


/**
 * The PSAttributesCatalogHandler class implements cataloging of
 * attributes. This request type is used to locate the attributes
 * associated with an object defined in the specified security provider.
 * <p>
 * Attribute catalog requests are sent to the server using the
 * PSXSecurityAttributesCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityAttributesCatalog (instanceName, objectType*)&gt;
 *
 *    &lt;-- the name of the security provider instance to use for the query.
 *     --&gt;
 *    &lt;!ELEMENT instanceName      (#PCDATA)&gt;
 *
 *    &lt;--
 *       the type of object to locate attributes for. By specifying
 *         multiple objectType elements, the attribute list for multiple
 *       object types can be retrieved.
 *     --&gt;
 *    &lt;!ELEMENT objectType       (#PCDATA)&gt;
 * </pre>
 *
 * The PSXSecurityAttributesCatalogResults XML document is sent as the
 * response. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityAttributesCatalogResults (instanceName, Attributes*)&gt;
 *
 *    &lt;-- the name of the security provider instance which was queried.
 *     --&gt;
 *    &lt;!ELEMENT instanceName      (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT Attributes         (name*)&gt;
 *
 *    &lt;--
 *       objectType - the type of security object the attributes are for.
 *     --&gt;
 *    &lt;!ATTLIST Attributes
 *       objectType      CDATA         #REQUIRED
 *    &gt;
 *
 *    &lt;-- the name of the attribute.
 *     --&gt;
 *    &lt;!ELEMENT name               (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSAttributesCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSAttributesCatalogHandler()
   {
      super();
   }

   /**
    * Format the catalog request based upon the specified request
    * information. The request information for this request type is:
    * <TABLE border="2">
    *   <tr><th>Key</th>
    *       <th>Value</th>
    *       <th>Required</th></tr>
    *   <tr><td>RequestCategory</td>
    *       <td>security</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>Attributes</td>
    *       <td>yes</td></tr>
    *   <tr><td>InstanceName</td>
    *       <td>the name of the security provider instance being queried</td>
    *       <td>yes</td></tr>
    *   <tr><td>ObjectType</td>
    *       <td>the type(s) of object(s) to locate attributes for. Multiple
    *         object types can be specified by using a comma delimited list
    *         of types. The supported types are security provider specific. Use
    *         the ObjectTypes catalog for a list of supported types.</td>
    *     <td>yes</td></tr>
    * </TABLE>
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
      if ( (sTemp == null) || !"Attributes".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      String instanceName = (String)req.get("InstanceName");
      if (instanceName == null)
         throw new IllegalArgumentException("reqd prop not specified: InstanceName");

      String objectType = (String)req.get("ObjectType");
      if (objectType == null)
         throw new IllegalArgumentException("reqd prop not specified: ObjectType");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(   reqDoc,
                                                      "PSXSecurityAttributesCatalog");
      PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                       "instanceName", instanceName);

      // object types are comma delimited, so parse it up
      StringTokenizer toks = new StringTokenizer(objectType, ",");
      String curTok;
      while (toks.hasMoreTokens()) {
         curTok = toks.nextToken().trim();
         if (curTok.length() > 0)
              PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                             "objectType", curTok);
      }

      return reqDoc;
   }
}

