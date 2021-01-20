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

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link #processUdf(Object[], IPSRequestContext)
 * processUdf} for a description. This UDF is sepcifically designed for Content
 * Explorer, though it can be theoretically used elsewhere.
 * <p>
 * This UDF evaluates the assignment type to an image given the contentid of the
 * item and image url options. Assignment value (which is a number) is evaluated
 * based on the current state of the workflow of the item. Currently supported
 * assignment types are defined in {@link com.percussion.workflow.PSWorkflowUtils}.
 * The UDF takes the content id as the first parameter and the rest are the image
 * url options for each possible assignmenttype in asending order by value.
 * <p>
 * The image urls can either be standard Applet Url in the syntax of
 * "../app/resource.gif" or the icon key. An icon key is simple text not starting
 * with "..". In the first case, the Content Explorer loads the image by executing
 * the url request. In latter case, the image is loaded from the Content Explorer
 * archive (com/percussion/cx/images/iconkey.gif). It is recommended that the
 * images match the normal icon images in the content explorer.
 */
public class PSGetAssignmentType extends PSSimpleJavaUdfExtension
   implements IPSUdfProcessor
{
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if ( null == params || params.length < 1 || null == params[0]
         || 0 == params[0].toString().trim().length())
      {
         return "";
      }

      String noneImage = "", readerImage = "", assigneeImage = "", adminImage = "";

      if(params.length > 1)
         noneImage = params[1].toString();
      if(params.length > 2)
         readerImage = params[2].toString();
      if(params.length > 3)
         assigneeImage = params[3].toString();
      if(params.length > 4)
         adminImage = params[4].toString();

      String contentid = params[0].toString().trim();
      //Create a temporary XML result document with key fields.
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element elem = PSXmlDocumentBuilder.createRoot(doc, ELEMENT_ITEM);
      elem.setAttribute(ATTRIB_CONTENTID, contentid);
      String result = "Default";
      String userName = "";
      try
      {
         int assType = PSExitAddPossibleTransitionsEx.getAssignmentType(
               request, contentid);

         switch(assType)
         {
            case PSWorkFlowUtils.ASSIGNMENT_TYPE_NONE:
               result = noneImage;
            break;
            case PSWorkFlowUtils.ASSIGNMENT_TYPE_READER:
               result = readerImage;
            break;
            case PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE:
               result = assigneeImage;
            break;
            case PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN:
               result = adminImage;
            break;
            default:
         }
      }
      catch (PSException e)
      {
         e.printStackTrace();
         throw new PSConversionException(e.getErrorCode(),
            e.getErrorArguments());
      }
      return result;
   }
   //String constants for the temporary XML document
   static private final String ELEMENT_ITEM = "Item";
   static private final String ATTRIB_CONTENTID = "contentid";
}

