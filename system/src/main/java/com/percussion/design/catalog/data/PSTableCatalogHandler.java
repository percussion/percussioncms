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

import java.util.StringTokenizer;


/**
 * The PSTableCatalogHandler class implements cataloging of
 * tables. This request type is used to locate the tables in the
 * specified back-end database.
 * <p>
 * Table catalog requests are sent to the server using the
 * PSXTableCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXTableCatalog (datasource?, filter?, tableType*)&gt;
 *
 *    &lt;-- the name of the datasource being queried, empty to indicate the
 *       repository.
 *     --&gt;
 *    &lt;!ELEMENT datasource      (#PCDATA)&gt;
 *
 *    &lt;--
 *       a filter to use for locating matches. The filter condition must
 *       use the SQL LIKE pattern matching syntax. Use _ to match a
 *       single character and % to match a string of length 0 or more.
 *     --&gt;
 *    &lt;!ELEMENT filter          (#PCDATA)&gt;
 *
 *    &lt;--
 *       the type of table to locate. By specifying multiple tableType
 *       elements, multiple table types can be searched for.
 *     --&gt;
 *    &lt;!ELEMENT tableType       (#PCDATA)&gt;
 * </pre>
 *
 * The PSXTableCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXTableCatalogResults (datasource, Table*)&gt;
 *
 *    &lt;-- the name of the datasource which was queried.
 *     --&gt;
 *    &lt;!ELEMENT datasource             (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT Table                  (name)&gt;
 *
 *    &lt;--
 *       type - the type of back-end table this represents.
 *     --&gt;
 *    &lt;!ATTLIST Table
 *       type        CDATA                #REQUIRED
 *    &gt;
 *
 *    &lt;-- the name of the table.
 *     --&gt;
 *    &lt;!ELEMENT name                   (#PCDATA)&gt;
 * </pre>
 */
public class PSTableCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSTableCatalogHandler()
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
    *       <td>Table</td>
    *       <td>yes</td></tr>
    *   <tr><td>Datasource</td>
    *       <td>the name of the datasource being queried, may be ommited to 
    *       query the repository</td>
    *       <td>no</td></tr>
    *   <tr><td>Filter</td>
    *       <td>a filter to use for locating matches. The filter condition
    *           must use the SQL LIKE pattern matching syntax. Use _ to
    *           match a single character and % to match a string of length
    *           0 or more.
    *       <td>no</td></tr>
    *   <tr><td>TableType</td>
    *       <td>the type(s) of table to locate. Multiple table types can be
    *           specified by using a comma delimited list of types. The
    *           supported types are DBMS specific. Use the TableTypes catalog
    *           for a list of supported types.</td></tr>
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
      if ( (sTemp == null) || !"data".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"Table".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      String datasource = (String)req.get("Datasource");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc, "PSXTableCatalog");

      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "datasource", 
            datasource);

      sTemp = (String) req.get("Filter");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "filter", sTemp);

      sTemp = (String)req.get("TableType");
      if (sTemp != null)
      {
         // table types are comma delimited, so parse it up
         StringTokenizer toks = new StringTokenizer(sTemp, ",");
         String curTok;
         while (toks.hasMoreTokens())
         {
            curTok = toks.nextToken().trim();
            if (curTok.length() > 0)
               PSXmlDocumentBuilder.addElement(reqDoc, root, "tableType",
                  curTok);
         }
      }

      return reqDoc;
   }
}

