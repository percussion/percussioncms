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

