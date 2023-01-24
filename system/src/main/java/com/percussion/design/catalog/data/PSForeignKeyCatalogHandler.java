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
 * The PSForeignKeyCatalogHandler class implements cataloging of
 * foreign keys. This request type is used to locate the columns which
 * are related to other tables. Foreign key columns usually refer to the
 * primary key of another table. This allows a unique relationship to be
 * defined between the two tables. When a foreign key is defined, values
 * cannot be inserted which do not exist in the table being referenced.
 * <p>
 * Driver catalog requests are sent to the server using the DriverCatalog
 * XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXForeignKeyCatalog (datasource?, tableName)&gt;
 *
 *    &lt;!-- the name of the datasource being queried.
 *     --&gt;
 *    &lt;!ELEMENT datasource           (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table for which foreign key columns should be returned.
 *     --&gt;
 *    &lt;!ELEMENT tableName            (#PCDATA)&gt;
 * </pre>
 *
 * The PSXForeignKeyCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXForeignKeyCatalogResults (datasource, tableName, 
 *       ForeignKey*)&gt;
 *
 *    &lt;!-- the name of the datasource queried, empty to indicate the 
 *       repository.
 *     --&gt;
 *    &lt;!ELEMENT datasource           (#PCDATA)&gt;
 *    
 *    &lt;!ELEMENT ForeignKey                  (name, ExternalColumn)&gt;
 *
 *    &lt;!-- the name of the column in this table which has a foreign key relationship.
 *     --&gt;
 *    &lt;!ELEMENT name                        (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT ExternalColumn              (schemaName?, tableName, columnName)&gt;
 *
 *    &lt;!-- the table containing the referenced column.
 *     --&gt;
 *    &lt;!ELEMENT tableName                   (#PCDATA)&gt;
 *
 *    &lt;!-- the referenced column.
 *     --&gt;
 *    &lt;!ELEMENT columnName                  (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSForeignKeyCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSForeignKeyCatalogHandler()
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
    *       <td>ForeignKey</td>
    *       <td>yes</td></tr>
    *   <tr><td>Datasource</td>
    *       <td>the name of the datasource being queried, may be omitted to
    *       query the respository</td>
    *       <td>yes</td></tr>
    *   <tr><td>TableName</td>
    *       <td>the name of the table for which foreign key columns should
    *           be returned</td>
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
      if ( (sTemp == null) || !"data".equalsIgnoreCase(sTemp) ) 
      {
         throw new IllegalArgumentException(
            "req category invalid: RequestCategory null");
      }

      sTemp = (String) req.get("RequestType");
      if ((sTemp == null) || !"ForeignKey".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException(
            "req type invalid: RequestType null");
      }

      String datasource = (String)req.get("Datasource");

      String tableName = (String) req.get("TableName");
      if (tableName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: TableName");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
         "PSXForeignKeyCatalog");

      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "datasource", 
            datasource);

        PSXmlDocumentBuilder.addElement(reqDoc, root, "tableName", tableName);

      return reqDoc;
   }
}

