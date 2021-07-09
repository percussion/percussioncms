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

package com.percussion.design.catalog.security;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Properties;

import org.w3c.dom.Document;

/**
 * The PSCatalogerCatalogHandler class implements cataloging of 
 * subject and directory catalogers.  This 
 * request type is used to locate the available catalogers.
 * <p>
 * Cataloger catalog requests are sent to the server using the
 * PSXSecurityCatalogerCatalog XML document. Its definition is as follows:
 * 
 * <pre>
 * <code>
 *    &lt;-- 
 *       Root request element.  Returns all cataloger summaries.  
 *    --&gt;
 *    &lt;!ELEMENT PSXSecurityCatalogerCatalog&gt;
 * </code>
 * </pre>
 * 
 * The PSXSecurityCatalogerCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * 
 * <pre>
 * <code>
 *
 *     &lt;!-- 
 *       Contains the results of the catalog request, zero or more cataloger 
 *       elements are returned.  
 *      --&gt;
 *     &lt;!ELEMENT PSXSecurityCatalogerCatalogResults (cataloger*)>
 *     
 *     &lt;!-- 
 *       Describes a cataloger.  
 *      --&gt;
 *     &lt;!ELEMENT cataloger (catalogerName, catalogerType, fullName, 
 *       description)>
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
 *     &lt;!--
 *        the full name of the cataloger, to be presented in the user interface. 
 *      --&gt;
 *     &lt;!ELEMENT fullName (#PCDATA)&gt;
 * 
 *     &lt;!-- a brief description of the cataloger.
 *      --&gt;
 *     &lt;!ELEMENT description (#PCDATA)&gt;
 * 
 * </code>
 * </pre>
 * 
 */
public class PSCatalogerCatalogHandler implements IPSCatalogHandler
{
   /**
    * Default ctor
    */
   public PSCatalogerCatalogHandler()
   {

   }

   /**
    * Format the catalog request based upon the specified request
    * information. The request information for this request type is:
    * <TABLE border="2">
    * <tr><th>Key</th>
    *     <th>Value</th>
    *     <th>Required</th></tr>
    * <tr><td>RequestCategory</td>
    *     <td>security</td>
    *     <td>yes</td></tr>
    * <tr><td>RequestType</td>
    *     <td>Cataloger</td>
    *     <td>yes</td></tr>
    * </TABLE>
    *
    * See base class for more info.
    *
    */
   public Document formatRequest(Properties req)
   {
      String sTemp = (String)req.get("RequestCategory");
      if ( (sTemp == null) || !"security".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"Cataloger".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      PSXmlDocumentBuilder.createRoot(reqDoc, "PSXSecurityCatalogerCatalog");

      return reqDoc;
   }

}
