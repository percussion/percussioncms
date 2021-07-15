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
package com.percussion.design.catalog.macro;

import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSMacroDefinitionSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The macro catalog handler.
 */
public class PSMacroCatalogHandler implements IPSCatalogHandler
{
   /**
    * Formats the request for the supplied properties.
    * 
    * @see IPSCatalogHandler#formatRequest(Properties)
    */
   public Document formatRequest(Properties req)
   {
      String test = (String) req.get(REQ_CATEGORY_KEY);
      if ((test == null) || (!(REQ_CATEGORY_VALUE.equalsIgnoreCase(test))))
         throw new IllegalArgumentException(
            "req category invalid: null or invalid");

      test = (String) req.get(REQ_TYPE_KEY);
      if ((test == null) || (!(REQ_TYPE_VALUE.equalsIgnoreCase(test))))
         throw new IllegalArgumentException(
            "req type invalid: null or invalid");

      Document request = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(request, NODE_NAME);

      return request;
   }

   /**
    * Catalogs all known macros from the server.
    * 
    * @param cataloger the cataloger to use, not <code>null</code>.
    * @return a set of macros, never <code>null</code>, may be empty.
    * @throws PSServerException for any server errors.
    * @throws PSAuthenticationFailedException for all authentication failures.
    * @throws PSAuthorizationException for authorization failuers.
    * @throws IOException for all IO errors.
    * @throws PSUnknownNodeTypeException for XML serialization errors.
    */
   public static PSMacroDefinitionSet getMacros(PSCataloger cataloger)
      throws PSServerException,PSAuthenticationFailedException,
         PSAuthorizationException, IOException, PSUnknownNodeTypeException
   {
      if (cataloger == null)
         throw new IllegalArgumentException("cataloger may not be null");

      // create the properties
      Properties req = new Properties();
      req.put(REQ_CATEGORY_KEY, REQ_CATEGORY_VALUE);
      req.put(REQ_TYPE_KEY, REQ_TYPE_VALUE);

      // perform the catalog request
      Document doc = null;
      try
      {
         doc = cataloger.catalog(req);
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }

      PSMacroDefinitionSet macros = new PSMacroDefinitionSet();
      NodeList nodes = doc.getElementsByTagName(
         PSMacroDefinitionSet.XML_NODE_NAME);
      if (nodes != null && nodes.getLength() > 0)
         macros.fromXml((Element) nodes.item(0), null, null);

      return macros;
   }

   /**
    * Constant for the "RequestCategory" key for the properties object
    * specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_CATEGORY_KEY = "RequestCategory";

   /**
    * Constant for the value of the "RequestCategory" key for the properties
    * object specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_CATEGORY_VALUE = "macro";

   /**
    * Constant for the "RequestType" key for the properties object
    * specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_TYPE_KEY = "RequestType";

   /**
    * Constant for the value of the "RequestType" key for the properties
    * object specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_TYPE_VALUE = "Macro";

   // Constants for XML element and attribute names
   public static final String NODE_NAME = "PSXMacroCatalog";
}
