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

package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Udf to parse an Xml fragment containing an element and return a Document with 
 * the specified root and the parsed element as a child of that root. May be
 * used to retrieve Xml stored in a database column and include it as part of an
 * Xml document returned by a query.  When used in the mapper of a query 
 * resource, the server will replace Xml element to which the udf is mapped with
 * the returned Xml document and provided that the mapped Xml element name and
 * the specified root name match.
 */
public class PSTextToXml extends PSSimpleJavaUdfExtension
{
   /**
    * Parses the text and returns the document.
    * 
    * @param params May not be <code>null</code>. The following parameters are 
    * expected:
    * <table border="1">
    *   <tr><th>Param #</th><th>Name</th><th>Description</th><th>Required?</th>
    *   <tr>
    *   <tr>
    *     <td>1</td>
    *     <td>RootName</td>
    *     <td>Name of the root element to create.  The parsed Xml fragment is 
    *          appended as a child of this root element.</td>
    *     <td>yes</td>
    *   </tr>
    *   <tr>
    *     <td>2</td>
    *     <td>XmlFragment</td>
    *     <td>String value containing a single well-formed Xml element.  May be
    *       null or empty, in which case the specified root element is
    *       created and returned with no children.</td>
    *     <td>yes</td>
    *   </tr>
    * 
    * @param request The request context, not <code>null</code>.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      Document doc = null;
      
      if (params == null || params.length != 2)
         throw new PSConversionException(0, "Invalid parameters supplied");
      
      String rootName = (params[0] == null ? null : params[0].toString());
      if (rootName == null || rootName.trim().length() == 0)
         throw new IllegalArgumentException(
            "rootName may not be null or emtpy.");

      String fragment = (params[1] == null ? null : params[1].toString());
      boolean isEmpty = (fragment == null || fragment.trim().length() == 0);
         
      try
      {
         if (isEmpty)
         {
            doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.createRoot(doc, rootName);
         }
         else
         {
            doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
               fragment), false);         
            Element newRoot = doc.createElement(rootName);
            PSXmlDocumentBuilder.swapRoot(doc, newRoot);
         }
      }
      catch (Exception e)
      {
         throw new PSConversionException(0, e.getLocalizedMessage());
      }      
      
      return doc;      
   }

}
