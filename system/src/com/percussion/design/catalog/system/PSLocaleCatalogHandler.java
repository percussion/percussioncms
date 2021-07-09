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

package com.percussion.design.catalog.system;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class implements cataloging of locales on the server.
 * <p>
 * Locale handler catalog requests are sent to the server
 * using the PSXLocaleCatalog XML document. Its definition
 * is as follows:
 * <pre>
 * <code>
 *
 *    &lt;!ELEMENT PSXLocaleCatalog EMPTY&gt;
 *
 * </code>
 * </pre>
 *
 * The PSXLocaleCatalogResults XML document is sent
 * as the response. Its definition is as follows:
 * 
 * <pre>
 * <code>
 *
 *    &lt;!ELEMENT PSXLocaleCatalogResults   (locale+)&gt;
 *
 *    &lt;!--
 *       Each locale is returned as a locale element.  Each locale element is
 *       composed of one value which represents the language string used to 
 *       identify the locale. 
 *    --&gt;
 *    &lt;!ELEMENT locale (languageString)&gt;
 *  
 *    &lt;!--
 *       The language string by which the locale should be referenced. 
 *     --&gt;
 *    &lt;!ELEMENT languageString (#PCDATA)&gt;
 *    
 * </code>
 * </pre>
 */
public class PSLocaleCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler. This is used primarily
    * by the cataloger.
    */
   public PSLocaleCatalogHandler()
   {
      super();
   }

   /**
    * Format the catalog request based upon the specified
    * request information. The request information for this
    * request type is:
    *
    * <table border="1">
    * <tr>
    *      <th>Key</th>
    *      <th>Value</th>
    *      <th>Required</th>
    *   </tr>
    * <tr>
    *      <td>RequestCategory</td>
    *      <td>system</td>
    *      <td>yes</td>
    *   </tr>
    * <tr>
    *      <td>RequestType</td>
    *      <td>Locale</td>
    *      <td>yes</td>
    *   </tr>
    * </table>
    *
    * @param   req         the request information
    *
    * @return               an XML document containing the appropriate
    *                        catalog request information
    */
   public org.w3c.dom.Document formatRequest(Properties req)
   {
      String sTemp = (String)req.get("RequestCategory");
      if ((sTemp == null) || !"system".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException(
               "req category invalid: exit or null");
      }

      sTemp = (String)req.get("RequestType");
      if ((sTemp == null) || !"Locale".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid: Locale or null");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXLocaleCatalog");

      // pass all properties in the supplied list thru
      Enumeration keys = req.propertyNames();
      while (keys.hasMoreElements())
      {
         String key = (String) keys.nextElement();
         if (!(key.equals("RequestCategory") || key.equals("RequestType")))
            PSXmlDocumentBuilder.addElement(reqDoc, root, key, req.getProperty(
                  key));
      }

      return reqDoc;
   }   
}

