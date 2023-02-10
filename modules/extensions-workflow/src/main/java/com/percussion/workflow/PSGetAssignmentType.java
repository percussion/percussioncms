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

package com.percussion.workflow;


import com.percussion.data.PSConversionException;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

   private static final Logger log = LogManager.getLogger(PSGetAssignmentType.class);
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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new PSConversionException(e.getErrorCode(),
            e.getErrorArguments());
      }
      return result;
   }
   //String constants for the temporary XML document
   static private final String ELEMENT_ITEM = "Item";
   static private final String ATTRIB_CONTENTID = "contentid";
}

