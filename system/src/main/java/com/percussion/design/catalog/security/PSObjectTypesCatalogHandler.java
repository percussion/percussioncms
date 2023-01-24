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
 * The PSObjectTypesCatalogHandler class implements cataloging of supported
 * object types from the specified cataloger.
 * <p>
 * Object type catalog requests are sent to the server using the
 * PSXSecurityObjectTypesCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityObjectTypesCatalog (catalogerName, catalogerType)&gt;
 *
 *     &lt;!--
 *        the name by which the cataloger should be referenced. 
 *      --&gt;
 *     &lt;!ELEMENT catalogerName (#PCDATA)&gt;
 *     
 *     &lt;!--
 *        the type of the cataloger, which when combined with the name forms a
 *        unique reference 
 *      --&gt;
 *     &lt;!ELEMENT catalogerType (#PCDATA)&gt;
 * </pre>
 *
 * The PSXSecurityObjectTypesCatalogResults XML document is sent as the
 * response. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityObjectTypesCatalogResults (catalogerName, 
 *    catalogerType, ObjectType*)&gt;
 *
 *     &lt;!--
 *        the name by which the cataloger should be referenced. 
 *      --&gt;
 *     &lt;!ELEMENT catalogerName (#PCDATA)&gt;
 *     
 *     &lt;!--
 *        the type of the cataloger, which when combined with the name forms a
 *        unique reference 
 *      --&gt;
 *     &lt;!ELEMENT catalogerType (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT ObjectType         EMPTY&gt;
 *
 *    &lt;--
 *       type - the type of object this represents.
 *     --&gt;
 *    &lt;!ATTLIST ObjectType
 *       type        CDATA                #REQUIRED
 *    &gt;
 * </pre>
 *
 */
public class PSObjectTypesCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSObjectTypesCatalogHandler()
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
    *       <td>ObjectTypes</td>
    *       <td>yes</td></tr>
    *     <tr><td>CatalogerName</td>
    *         <td>the name of the cataloger being queried</td>
    *         <td>yes</td></tr>
    *     <tr><td>CatalogerType</td>
    *         <td>the type of the cataloger being queried</td>
    *         <td>yes</td></tr>

    * </table>
    * 
    * @param      req         the request information
    *
    * @return                 an XML document containing the appropriate
    *                         catalog request information
    */
   public Document formatRequest(java.util.Properties req)
   {
      String sTemp = (String) req.get("RequestCategory");
      if ((sTemp == null) || !"security".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String) req.get("RequestType");
      if ((sTemp == null) || !"ObjectTypes".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid");
      }

      String catalogerName = (String) req.get("CatalogerName");
      if (catalogerName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: CatalogerName");

      String catalogerType = (String) req.get("CatalogerType");
      if (catalogerType == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: CatalogerType");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
         "PSXSecurityObjectTypesCatalog");

      PSXmlDocumentBuilder.addElement(reqDoc, root, "catalogerName",
         catalogerName);

      PSXmlDocumentBuilder.addElement(reqDoc, root,
         "catalogerType", catalogerType);
      
      return reqDoc;
   }
}

