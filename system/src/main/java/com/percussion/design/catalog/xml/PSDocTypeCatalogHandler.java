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

package com.percussion.design.catalog.xml;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.StringTokenizer;


/**
 * The PSDocTypeCatalogHandler class implements cataloging of
 * XML document types supported internally by the E2 server. XML documents
 * are used by E2 for storing various pieces of information. They are also
 * used as a transport mechanism (such as catalog request handling). The
 * following categories of documents are used in E2:
 * <ul>
 *    <li>catalog - DTDs used for sending and receiving catalog requests
 *                  and results</li>
 *    <li>context - DTDs used to provide context information during the
 *                  processing of user requests by an application</li>
 *    <li>error - DTDs used for defining how various errors will be
 *                packaged for processing be a style sheet</li>
 * </ul>
 * DocType catalog requests are sent to the server using the
 * PSXDocTypeCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXDocTypeCatalog   (dtdCategory*)&gt;
 *
 *    &lt;--
 *       the category of DTD to return. If no dtdCategory elements are
 *       defined, all DTDs are returned. If multiple dtdCategory elements
 *       are defined, DTDs in the specified categories will be returned.
 *     --&gt;
 *    &lt;!ELEMENT dtdCategory         (catalog | context | error)&gt;
 * </pre>
 *
 * The PSXDocTypeCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXDocTypeCatalogResults  (DocType*)&gt;
 *
 *    &lt;!ELEMENT DocType                   (name, category, url, description)&gt;
 *
 *    &lt;-- the name of the DTD.
 *     --&gt;
 *    &lt;!ELEMENT name                      (#PCDATA)&gt;
 *
 *    &lt;-- the DTDs category (grouping within E2).
 *     --&gt;
 *    &lt;!ELEMENT category                  (catalog | context | error)&gt;
 *
 *    &lt;--
 *       the URL of the DTD on the E2 server. This can be used to get the
 *       actual DTD definition.
 *     --&gt;
 *    &lt;!ELEMENT url                       (#PCDATA)&gt;
 *
 *    &lt;-- a description of the DTD. In particular, how it is used.
 *     --&gt;
 *    &lt;!ELEMENT description               (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDocTypeCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSDocTypeCatalogHandler()
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
    *       <td>xml</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>DocType</td>
    *       <td>yes</td></tr>
    *   <tr><td>DtdCategory</td>
    *       <td>the category of DTD to return. Do not define this property
    *           to get all DTDs. To get multiple DTDs, pass a comma
    *           separated list of categories.</td>
    *       <td>no</td></tr>
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
      if ( (sTemp == null) || !"xml".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"DocType".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(   reqDoc,
                                                      "PSXDocTypeCatalog");

      sTemp = (String)req.get("DtdCategory");
      if (sTemp != null) {
         // DTD categories are comma delimited, so parse it up
         StringTokenizer toks = new StringTokenizer(sTemp, ",");
         String curTok;
         while (toks.hasMoreTokens()) {
            curTok = toks.nextToken().trim();
            if (curTok.length() > 0)
                 PSXmlDocumentBuilder.addElement(   reqDoc, root,
                                                "dtdCategory", curTok);
         }
      }

      return reqDoc;
   }
}

