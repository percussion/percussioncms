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

package com.percussion.design.catalog.data;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSIndexCatalogHandler class implements cataloging of
 * indexes. This request type is used to locate the indexes defined
 * on a table. Indexes are used to sort data. This allows for faster
 * access to the data. They can also be used to enforce unique column
 * values.
 * <p>
 * Index catalog requests are sent to the server using the
 * PSXIndexCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXIndexCatalog (datasource?, tableName)&gt;
 *
 *    &lt;!-- the name of the datasource being queried.
 *     --&gt;
 *    &lt;!ELEMENT datasource      (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table for which indexes should be returned.
 *     --&gt;
 *    &lt;!ELEMENT tableName       (#PCDATA)&gt;
 * </pre>
 *
 * The PSXIndexCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXIndexCatalogResults (datasource, tableName, Index*)&gt;
 *
 *    &lt;!-- the name of the datasource which was queried, empty to indicate 
 *    the repository.
 *     --&gt;
 *    &lt;!ELEMENT datasource             (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table containing the indexes.
 *     --&gt;
 *    &lt;!ELEMENT tableName              (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT Index                  (name, indexColumn+)&gt;
 *
 *    &lt;!-- does the index only allow unique values?
 *     --&gt;
 *    &lt;!ATTLIST Index
 *       enforceUniqueness    %PSXIsEnabled;    #REQUIRED
 *    &gt;
 *
 *    &lt;!-- the name of the index.
 *     --&gt;
 *    &lt;!ELEMENT name                   (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the indexed column.
 *     --&gt;
 *    &lt;!ELEMENT indexColumn            (#PCDATA)&gt;
 *
 *    &lt;!--
 *       asc - this is sorted in ascending order.
 *
 *       desc - this is sorted in descending order.
 *
 *       unk - the sorting could not be determined (unknown).
 *     --&gt;
 *    &lt;!ENTITY % PSXSortDirection "(asc | desc | unk)"&gt;
 *
 *    &lt;!-- the referenced column.
 *       position - the position of the column within the index. For
 *       instance, if the index is on firstName + lastName, firstName is
 *       set to 1 and lastName is set to 2.
 *
 *       sorting - the sort direction of the column (see the
 *       PSXSortDirection entity)
 *     --&gt;
 *    &lt;!ATTLIST indexColumn
 *       position     CDATA              #REQUIRED
 *       sorting      %PSXSortDirection  #REQUIRED
 *    &gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSIndexCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSIndexCatalogHandler()
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
    *       <td>Index</td>
    *       <td>yes</td></tr>
    *   <tr><td>Datasource</td>
    *       <td>the name of the datasource being queried, may be omitted to
    *       query the repository</td>
    *       <td>no</td></tr>
    *   <tr><td>TableName</td>
    *       <td>the name of the table for which indexes should be returned</td>
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
      if ((sTemp == null) || !"Index".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid");
      }

      String datasource = (String) req.get("Datasource");

      String tableName = (String) req.get("TableName");
      if (tableName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: TableName");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc, "PSXIndexCatalog");

      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "datasource", 
            datasource);

      PSXmlDocumentBuilder.addElement(reqDoc, root, "tableName", tableName);

      return reqDoc;
   }
}

