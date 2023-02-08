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

package com.percussion.design.catalog.data;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSTableTypesCatalogHandler class implements cataloging of
 * table types in the specified back-end database.
 * <p>
 * Table type catalog requests are sent to the server using the
 * PSXTableTypesCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXTableTypesCatalog (datasource?)&gt;
 *
 *    &lt;-- the name of the datasource being queried, may be omitted to query
 *       the repository.
 *     --&gt;
 *    &lt;!ELEMENT datasource      (#PCDATA)&gt;
 * </pre>
 *
 * The PSXTableTypesCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXTableTypesCatalogResults (datsource, TableType*)&gt;
 *
 *    &lt;-- the name of the datasource queried, may be empty to indicate the
 *       repository.
 *     --&gt;
 *    &lt;!ELEMENT datasource      (#PCDATA)&gt;
 *     
 *    &lt;!ELEMENT TableType              EMPTY&gt;
 *
 *    &lt;--
 *       type - the type of back-end table this represents.
 *     --&gt;
 *    &lt;!ATTLIST TableType
 *       type        CDATA                #REQUIRED
 *    &gt;
 * </pre>
 *
 */
public class PSTableTypesCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSTableTypesCatalogHandler()
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
    *       <td>data</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>TableTypes</td>
    *       <td>yes</td></tr>
    *   <tr><td>Datasource</td>
    *       <td>the name of the datasource being queried, may be omitted to
    *          indicate the respository</td>
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
      String sTemp = (String) req.get("RequestCategory");
      if ((sTemp == null) || !"data".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String) req.get("RequestType");
      if ((sTemp == null) || !"TableTypes".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid");
      }

      String datasource = (String)req.get("Datasource");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
         "PSXTableTypesCatalog");

      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root,"datasource", datasource);

      return reqDoc;
   }
}

