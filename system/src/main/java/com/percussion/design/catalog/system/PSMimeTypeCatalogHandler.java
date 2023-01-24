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

package com.percussion.design.catalog.system;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Enumeration;
import java.util.Properties;


/**
 * This class implements cataloging of supported mimetypes on the server.
 * <p>
 * MimeType handler catalog requests are sent to the server
 * using the PSXMimeTypeCatalog XML document. Its definition
 * is as follows:
 * <pre>
 * <code>
 *
 *    &lt;!ELEMENT PSXMimeTypeCatalog EMPTY&gt;
 *
 * </code>
 * </pre>
 *
 * The PSXMimeTypeCatalogResults XML document is sent
 * as the response. Its definition is as follows:
 * 
 * <pre>
 * <code>
 *
 *    &lt;!ELEMENT PSXMimeTypeCatalogResults   (mimetype+)&gt;
 *
 *    &lt;!--
 *       Each mime type is returned as a mimetype element.  Each mimetype
 *       element is composed of one value which is the name of the mime type.
 *    --&gt;
 *    &lt;!ELEMENT mimetype (name)&gt;
 *  
 *    &lt;!--
 *       The name of the mime type. 
 *     --&gt;
 *    &lt;!ELEMENT name (#PCDATA)&gt;
 *    
 * </code>
 * </pre>
 */
public class PSMimeTypeCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler. This is used primarily
    * by the cataloger.
    */
   public PSMimeTypeCatalogHandler()
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
    *      <td>MimeType</td>
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
      if ((sTemp == null) || !"MimeType".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid: Locale or null");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXMimeTypeCatalog");

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

