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
