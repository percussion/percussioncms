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

package com.percussion.design.catalog.file;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.data.PSCatalogedColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSColumnCatalogHandler class implements cataloging of virtual column
 * data using the file system driver.  
 * <p>
 * Column catalog requests are sent to the server using the PSXColumnCatalog
 * XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXColumnCatalog (driverName, serverName?, loginId?, 
 *    loginPw?, databaseName?, schemaName?, tableName)&gt;
 *
 *    &lt;!-- the name of the driver being queried.
 *     --&gt;
 *    &lt;!ELEMENT driverName       (%PSXBackEndProviderType;)&gt;
 *
 *    &lt;!-- the name of the server being queried.
 *     --&gt;
 *    &lt;!ELEMENT serverName       (#PCDATA)&gt;
 *
 *    &lt;!-- the user name for the back end login.
 *     --&gt;
 *    &lt;!ELEMENT loginId       (#PCDATA)&gt;
 *
 *    &lt;!-- the encoded password for the associated login.
 *     --&gt;
 *    &lt;!ELEMENT loginPw       (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the database being queried.
 *     --&gt;
 *    &lt;!ELEMENT databaseName     (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the schema the table belongs to.
 *     --&gt;
 *    &lt;!ELEMENT schemaName       (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table for which columns should be returned.
 *     --&gt;
 *    &lt;!ELEMENT tableName        (#PCDATA)&gt;
 * </pre>
 *
 * The PSXColumnCatalogResults XML document is sent as the response. Its
 * definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXColumnCatalogResults (driverName, serverName, 
 *    databaseName, schemaName, tableName, Column*)&gt;
 *
 *    &lt;!-- the name of the driver which was queried.
 *     --&gt;
 *    &lt;!ELEMENT driverName              (%PSXBackEndProviderType)&gt;
 *
 *    &lt;!-- the name of the server which was queried (this may be empty).
 *     --&gt;
 *    &lt;!ELEMENT serverName              (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the database which was queried (this may be empty).
 *     --&gt;
 *    &lt;!ELEMENT databaseName            (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the schema the table belongs to (this may be empty).
 *     --&gt;
 *    &lt;!ELEMENT schemaName              (#PCDATA)&gt;
 *
 *    &lt;!-- the name of the table containing the columns.
 *     --&gt;
 *    &lt;!ELEMENT tableName               (#PCDATA)&gt;
 *
 *    &lt;!--
 *     --&gt;
 *    &lt;!ELEMENT Column                  (name, backEndDataType, jdbcDataType, size, allowsNull)&gt;
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
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
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
    *       <td>file</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>Column</td>
    *       <td>yes</td></tr>
    *   <tr><td>DriverName</td>
    *       <td>the name of the driver being queried</td>
    *       <td>yes</td></tr>
    *   <tr><td>ServerName</td>
    *       <td>the name of the server being queried</td>
    *       <td>no</td></tr>
    *   <tr><td>LoginId</td>
    *       <td>the user name for the back end login</td>
    *       <td>no</td></tr>
    *   <tr><td>LoginPw</td>
    *       <td>the encoded password for the associated login</td>
    *       <td>no</td></tr>
    *   <tr><td>DatabaseName</td>
    *       <td>the name of the database being queried</td>
    *       <td>no</td></tr>
    *   <tr><td>SchemaName</td>
    *       <td>the name of the schema the table belongs to</td>
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
      String sTemp = (String)req.get("RequestCategory");
      if ( (sTemp == null) || !"file".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"Column".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      String driverName = (String)req.get("DriverName");
      if (driverName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: DriverName");
      if (!(driverName.equals("psfilesystem") || driverName.equals("psxml")))
         throw new IllegalArgumentException(
            "invalid property value specified for DriverName: " + driverName);

      String tableName = (String)req.get("TableName");
      if (tableName == null)
         throw new IllegalArgumentException("reqd prop not specified: TableName");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(   reqDoc,
                                                      "PSXColumnCatalog");
      PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                       "driverName", driverName);

      sTemp = (String)req.get("ServerName");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                          "serverName", sTemp);

      sTemp = (String)req.get("LoginId");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                          "loginId", sTemp);

      sTemp = (String)req.get("LoginPw");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                          "loginPw", sTemp);

      sTemp = (String)req.get("DatabaseName");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                          "databaseName", sTemp);

      sTemp = (String)req.get("SchemaName");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                          "schemaName", sTemp);

        PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                       "tableName", tableName);

      return reqDoc;
   }
}
