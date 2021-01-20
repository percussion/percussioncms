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
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSColumnCatalogHandler class implements cataloging of
 * columns. This request type is used to locate the columns
 * defined in a specific back-end table.
 * <p>
 * Column catalog requests are sent to the server using the PSXColumnCatalog
 * XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXColumnCatalog (datasource?, tableName)&gt;
 *
 *    &lt;-- the name of the datasource being queried.
 *     --&gt;
 *    &lt;!ELEMENT datasource      (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table for which columns should be returned.
 *     --&gt;
 *    &lt;!ELEMENT tableName        (#PCDATA)&gt;
 * </pre>
 *
 * The PSXColumnCatalogResults XML document is sent as the response. Its
 * definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXColumnCatalogResults (datasource, tableName, Column*)&gt;
 *
 *    &lt;!-- the name of the datasource which was queried, empty if the
 *       repository was queried.
 *     --&gt;
 *    &lt;!ELEMENT datasource      (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table containing the columns.
 *     --&gt;
 *    &lt;!ELEMENT tableName               (#PCDATA)&gt;
 *
 *    &lt;!--
 *     --&gt;
 *    &lt;!ELEMENT Column (name, backEndDataType, jdbcDataType, size, 
 *       allowsNull)&gt;
 *
 *    &lt;!-- the name of the column.
 *     --&gt;
 *    &lt;!ELEMENT name                    (#PCDATA)&gt;
 *
 *    &lt;!-- the back-end data type of the column.
 *     --&gt;
 *    &lt;!ELEMENT backEndDataType         (#PCDATA)&gt;
 *
 *    &lt;!-- the JDBC data type code (defined in java.sql.Types).
 *     --&gt;
 *    &lt;!ELEMENT jdbcDataType            (#PCDATA)&gt;
 *
 *    &lt;!--
 *       the maximum size of the column. This is the number of characters
 *       for text based types. For numeric or decimal types, it is stored
 *       as [digits].[decimalDigits]. For instance, Sybase money can support
 *       up to 15 digits to the right of the decimal, and 4 to the right of
 *       the decimal, thus 15.4 is specified as the size.
 *     --&gt;
 *    &lt;!ELEMENT size                    (#PCDATA)&gt;
 *
 *    &lt;!-- does the column allow NULL values to be stored?
 *     --&gt;
 *    &lt;!ELEMENT allowsNull              (yes | no | unknown)&gt;
 * <pre>
 *
 */
public class PSColumnCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSColumnCatalogHandler()
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
    *       <td>Column</td>
    *       <td>yes</td></tr>
    *   <tr><td>Datasource</td>
    *       <td>the name of the datasource being queried, may be omitted to
    *       query the respository</td>
    *       <td>no</td></tr>
    *   <tr><td>TableName</td>
    *       <td>the name of the table for which columns should be returned</td>
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
      if ((sTemp == null) || !"Column".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid");
      }

      String datasource = (String) req.get("Datasource");

      String tableName = (String) req.get("TableName");
      if (tableName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: TableName");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc, 
         "PSXColumnCatalog");
      
      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "datasource", 
            datasource);

      PSXmlDocumentBuilder.addElement(reqDoc, root, "tableName", tableName);

      return reqDoc;
   }

   /**
    * Convenience method which uses the specified cataloger and back-end table
    * definition to get the column listing for the table.
    */
   public static PSCatalogedColumn[] getCatalog(
      PSCataloger cataloger,
      PSBackEndTable table,
      String loginId,
      String loginPw)
      throws
         com.percussion.conn.PSServerException,
         com.percussion.security.PSAuthenticationFailedException,
         com.percussion.security.PSAuthorizationException,
         java.io.IOException
   {
      // create the properties from the table info
      java.util.Properties req = new java.util.Properties();

      req.put("RequestCategory", "data");
      req.put("RequestType", "Column");

      String sTemp = table.getTable();
      if (sTemp != null)
         req.put("TableName", sTemp);

      if (loginId != null)
         req.put("LoginId", loginId);

      if (loginPw != null)
         req.put("LoginPw", loginPw);

      // perform the catalog request
      Document doc = cataloger.catalog(req);

      // store the column definitions in a list
      List<PSCatalogedColumn> l = new ArrayList<PSCatalogedColumn>();
      PSXmlTreeWalker w = new PSXmlTreeWalker(doc);
      for (Element e = w.getNextElement("Column",
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS); e != null; 
            e = w.getNextElement("Column", 
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS))
      {
         l.add(new PSCatalogedColumn(table, e));
      }

      // and convert the list to an array
      final int size = l.size();
      PSCatalogedColumn[] ret = new PSCatalogedColumn[size];
      if (size > 0)
         l.toArray(ret);

      return ret;
   }
}

