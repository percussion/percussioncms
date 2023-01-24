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
import org.w3c.dom.Element;

import java.util.StringTokenizer;


/**
 * This class is the client side of the cataloger. It creates an xml doc
 * appropriate for the request based on a set of supplied properties. The
 * format of the generated xml document can be seen {@link
 * com.percussion.design.catalog.security.server.PSObjectCatalogHandler here}.
 */
public class PSObjectCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSObjectCatalogHandler()
   {
      super();
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
    *     <td>Object</td>
    *     <td>yes</td></tr>
    * <tr><td>catalogerName</td>
    *     <td>the name of the cataloger being queried</td>
    *     <td>yes</td></tr>
    * <tr><td>catalogerType</td>
    *     <td>the type of the cataloger being queried</td>
    *     <td>yes</td></tr>
    * <tr><td>Filter</td>
    *     <td>a filter to use for locating matches. The filter condition
    *       must use the SQL LIKE pattern matching syntax. Use _ to
    *       match a single character and % to match a string of length
    *       0 or more.
    *       Multiple conditions can be included by delimiting them with a semi-
    *       colon. The delimiter can be escaped with itself. For example, to
    *       obtain all names that begin with a or b, set this property to the
    *       following value: a%;b%. An empty filter will match nothing. </td>
    *     <td>no</td></tr>
    * <tr><td>ObjectType</td>
    *     <td>the type(s) of object(s) to locate. Multiple
    *       object types can be specified by using a comma delimited list
    *       of types. The supported types are security provider specific. Use
    *       the ObjectTypes catalog for a list of supported types.</td>
    *     <td>yes</td></tr>
    * </TABLE>
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
      if ((sTemp == null) || !"security".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String) req.get("RequestType");
      if ((sTemp == null) || !"Object".equalsIgnoreCase(sTemp))
      {
         throw new IllegalArgumentException("req type invalid");
      }

      String catalogerName = (String) req.get("CatalogerName");
      if (catalogerName == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: catalogerName");

      String catalogerType = (String) req.get("CatalogerType");
      if (catalogerType == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: catalogerType");

      String objectType = (String) req.get("ObjectType");
      if (objectType == null)
         throw new IllegalArgumentException(
            "reqd prop not specified: ObjectType");

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
         "PSXSecurityObjectCatalog");
      PSXmlDocumentBuilder.addElement(reqDoc, root, "catalogerName",
         catalogerName);

      PSXmlDocumentBuilder.addElement(reqDoc, root, "catalogerType",
         catalogerType);

      sTemp = (String) req.get("Filter");
      if (sTemp != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, "filter", sTemp);

      // object types are comma delimited, so parse it up
      StringTokenizer toks = new StringTokenizer(objectType, ",");
      String curTok;
      while (toks.hasMoreTokens())
      {
         curTok = toks.nextToken().trim();
         if (curTok.length() > 0)
            PSXmlDocumentBuilder.addElement(reqDoc, root, "objectType", curTok);
      }

      return reqDoc;
   }
}

