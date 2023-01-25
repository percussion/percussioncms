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

package com.percussion.design.catalog.security;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.util.Properties;

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
