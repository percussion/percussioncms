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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.workflow;


import com.percussion.data.PSConversionException;
import com.percussion.error.PSException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link #processUdf(Object[], IPSRequestContext)
 * processUdf} for a description. This UDF is sepcifically designed for Content
 * Explorer, though it can be theoretically used elsewhere.
 * <p>
 * This UDF evaluates the assigned roles to a semi-colon separated list given
 * the contentid of the item. The UDF takes the content id as the only first
 * parameter.
 */
public class PSGetAssignees extends PSSimpleJavaUdfExtension
   implements IPSUdfProcessor
{
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if ( null == params || params.length < 1 || null == params[0]
         || 0 == params[0].toString().trim().length())
         return "";

      String contentid = params[0].toString().trim();
      //Create a temporary XML result document with key fields.
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element elem = PSXmlDocumentBuilder.createRoot(doc, ELEMENT_ITEM);
      elem.setAttribute(ATTRIB_CONTENTID, contentid);
      String result = "";
      String userName = "";
      try
      {
         userName = request.getUserContextInformation(
            "User/Name", "unknown").toString();
         Object[] paramsEx =
         {
            userName,
            ELEMENT_ITEM,
            "@" + ATTRIB_CONTENTID,
            "yes"
         };
         PSExitAddPossibleTransitionsEx posTransEx =
            new PSExitAddPossibleTransitionsEx();
         doc = posTransEx.processResultDocument(paramsEx, request, doc);
         NodeList nl = doc.getElementsByTagName(
            PSExitAddPossibleTransitionsEx.ELEMENT_ASSIGNEDROLE);
         if(nl.getLength() < 1)
            return result;

         Node node = null;
         for(int i=0; nl != null && i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            node = elem.getFirstChild();
            if(node != null && node instanceof Text)
            {
               result += ((Text)node).getData().trim() + SEPARATOR;
            }
         }
      }
      catch (PSException e)
      {
         e.printStackTrace();
         throw new PSConversionException(e.getErrorCode(),
            e.getErrorArguments());
      }
      //remove the trialing separator
      if(result.endsWith(SEPARATOR) && result.length() >= SEPARATOR.length())
         result = result.substring(0, result.length()-SEPARATOR.length());
      return result;
   }
   //String constants for the temporary XML document
   static private final String ELEMENT_ITEM = "Item";
   static private final String ATTRIB_CONTENTID = "contentid";
   static private final String SEPARATOR = ", ";
}

