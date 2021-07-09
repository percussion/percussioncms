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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSHtmlParamDocument;

import org.w3c.dom.Document;

/**
 * This exit extracts named parameters from the standard HTML parameter input
 * document to HTML parameters with same names into the request. The DTD for
 * the input XML document must be the following:
 * &lt;!ELEMENT HtmlParams ANY&gt;
 * The root element <em>HtmlParams</em> can have one or more child elements that
 * are name value pairs. The element name will be the parameter name and the
 * value of the element is the value of the parameter. There can be multiple
 * elements with the same name so that multi-valued HTML parameters are
 * supported.
 * <p>
 * Exit supports a way of specifying to extract the value of a multi-valued
 * parameter suitable for IN clause of a SQL query. Also, we can specify if the
 * the parameter values should beenclosed in quotes.
 * </p>
 * <p>
 * The first parameter for this exit is flag that specifies if the extracted
 * value has to be suitable for IN clause of SQL query. Default is 'n' ("no").
 * </p>
 * <p>
 * The second parameter for this exit is flag that specifies if each value of
 * the parameter in the extracted value string needs to be in quotes. Default
 * is 'y' ("yes"). Applicable only if the first parameter gets a value of 'y'.
 * </p>
 * <p>
 * Rest of the parameters for the exit are the names of the HTML parameters to be
 * extracted out of the input document.
 * The exit itself is generic in that it takes any number parameters though it
 * is specified as 8 now (which means 10 total parameters for the exit).
 */
public class PSParamExtractor extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /*
    * Required by the interface. Actual processing happens in this method.
    * @see IPSRequestPreProcessor#preProcessRequestt()
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         PSParameterMismatchException, PSExtensionProcessingException,
            PSRequestValidationException
   {
      //Three parameters is the minimum.
      if(params == null || params.length < 3)
      {
         throw new PSParameterMismatchException(
            "params must not be null and should be of size greater than 2");
      }
      Document inputDoc = request.getInputDocument();
      if(inputDoc == null)
      {
         request.printTraceMessage(
            "Input document is null. No parameter is extracted");
         return;
      }
      //basic validation of the input document
      if(!inputDoc.getDocumentElement().getNodeName().equals(
         PSHtmlParamDocument.ROOT))
      {
         request.printTraceMessage(
            "Input HTML params document is not a valid one since its root "
            + "element name is not " + PSHtmlParamDocument.ROOT
            + ". No parameter is extracted");
         return;
      }
      //is the extraction for IN clause?
      String temp = (params[0]==null) ? "" : params[0].toString();
      boolean forInClause = false;
      if(temp.equalsIgnoreCase("y"))
         forInClause =true;

      //enclose each value in quotes?
      temp = (params[1]==null) ? "" : params[1].toString();
      boolean encloseInQuotes = true;
      if(temp.equalsIgnoreCase("n"))
         encloseInQuotes =false;

      PSHtmlParamDocument htmlParamDoc = new PSHtmlParamDocument();

      htmlParamDoc.fromXml(inputDoc.getDocumentElement());
      String paramName = null;
      String paramValue = null;
      Object obj = null;
      for(int i=2; i<params.length; i++)
      {
         obj = params[i];
         if(obj == null)
            continue;
         paramName = obj.toString().trim();
         if(paramName.length()<1)
            continue;
         if(forInClause)
         {
            paramValue = htmlParamDoc.getParamForInClause(paramName,
               encloseInQuotes);
            request.setParameter(paramName, paramValue);
         }
         else
         {
            request.setParameter(paramName, htmlParamDoc.getParam(paramName));
         }
         String operator = (String)htmlParamDoc.getParam(
            paramName + PSHtmlParamDocument.OPERATOR_SUFFIX);
         if(operator!=null && operator.trim().length() > 0)
         {
            request.setParameter(paramName + PSHtmlParamDocument.OPERATOR_SUFFIX,
               operator);
         }

      }
   }
}
