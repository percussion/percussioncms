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

package com.percussion.design.catalog.data;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSUniqueKeyCatalogHandler class implements cataloging of
 * unique keys. This request type is used to locate the column
 * combinations which can be used to uniquely identify rows in
 * the specified back-end table.
 * <p>
 * Unique key catalog requests are sent to the server using the
 * PSXUniqueKeyCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXUniqueKeyCatalog (datasource?, tableName)&gt;
 *
 *    &lt;-- the name of the datasource being queried, may be omitted to 
 *       indicate the repository
 *     --&gt;
 *    &lt;!ELEMENT datasource          (#PCDATA)&gt;
 *
 *    &lt;-- the name of the table for which unique key columns should be returned.
 *     --&gt;
 *    &lt;!ELEMENT tableName           (#PCDATA)&gt;
 * </pre>
 *
 * The PSXUniqueKeyCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXUniqueKeyCatalogResults (datasource, tableName, 
 *       UniqueKey*)&gt;
 *
 *    &lt;-- the name of the datasource which was queried, may be emtpy to 
 *       indicate the repository
 *     --&gt;
 *    &lt;!ELEMENT datasource          (#PCDATA)&gt;
 *
 *
 *    &lt;-- the name of the table containing the unique key columns.
 *     --&gt;
 *    &lt;!ELEMENT tableName                  (#PCDATA)&gt;
 *
 *    &lt;--
 *       each UniqueKey element contains one or more column names which
 *       define a combination of column which can uniquely identify rows.
 *       For instance, a customer table may define two unique indexes. One
 *       which is based upon customerId and the other which is based upon
 *       firstName and lastName. For instance:
 *
 *          &lt;UniqueKey&gt;
 *             &lt;name&gt;customerId&lt;/name&gt;
 *          &lt;/UniqueKey&gt;
 *          &lt;UniqueKey&gt;
 *            &lt;name&gt;firstName&lt;/name&gt;
 *            &lt;name&gt;lastName&lt;/name&gt;
 *          &lt;/UniqueKey&gt;
 *     --&gt;
 *    &lt;!ELEMENT UniqueKey                  (name+)&gt;
 *
 *    &lt;-- the name of the column.
 *     --&gt;
 *    &lt;!ELEMENT name                       (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUniqueKeyCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSUniqueKeyCatalogHandler()
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
    *       <td>UniqueKey</td>
    *       <td>yes</td></tr>
    *   <tr><td>Datasource</td>
    *       <td>the name of the datasource being queried, may be ommited to 
    *          indicate the repository</td>
    *       <td>no</td></tr>
    *   <tr><td>TableName</td>
    *       <td>the name of the table for which unique key columns should
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
      String sTemp = (String) req.get("RequestCategory");
      if ((sTemp == null) || !"data".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String) req.get("RequestType");
      if ((sTemp == null) || !"UniqueKey".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid");
      }

      String datasource = (String)req.get("Datasource");

      String tableName = (String)req.get("TableName");
      if (tableName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: TableName");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
         "PSXUniqueKeyCatalog");

      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "datasource", 
            datasource);

      PSXmlDocumentBuilder.addElement(reqDoc, root, "tableName", tableName);

      return reqDoc;
   }
}

