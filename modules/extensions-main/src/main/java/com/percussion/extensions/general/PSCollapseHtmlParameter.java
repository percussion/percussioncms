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
package com.percussion.extensions.general;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

/**
 * This exit collapses the supplied list of HTML parameters (as exit 
 * parameters) in the request in that if it finds more than one value for the 
 * parameter, i.e. array, it replaces the array with the first value from the 
 * array. If the specified parameter does not exist in the request parameters, 
 * it ignores.
 */
public class PSCollapseHtmlParameter extends PSDefaultExtension
   implements IPSRequestPreProcessor, IPSResultDocumentProcessor
{
   /*
    * Implementation of the method required by 
    * <code>IPSRequestPreProcessor</code> interface
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      handleParam(params, request);
   }

   /*
    * Implementation of the method required by 
    * <code>IPSResultDocumentProcessor</code> interface
    */
   public  Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      handleParam(params, request);
      return resultDoc;
   }

   /*
    * Implementation of the method required by 
    * <code>IPSResultDocumentProcessor</code> interface
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * This method does the actual processing of the parameters.
    * @param param - array of parameters supplied as exit parameters, may be
    * <code>null</code> but normally not.
    * @param request - the <code>IPSRequestContext</code> object assumed to be
    * not <code>null</code>.
    */
   private void handleParam(Object params[], IPSRequestContext request)
   {
      request.printTraceMessage("Starting collapseHTMLParameter...");

      HashMap paramMap = request.getParameters();
      /*
       * if the html param map is empty or the supplied parameter array empty
       * do nothing
       */
      if (paramMap == null || params == null || params.length < 1)
         return;

      Object obj = null;
      String paramName = null;
      for(int paramIndex = 0; paramIndex < params.length; paramIndex++)
      {
         obj = params[paramIndex];
         if(obj == null)
            continue;
         paramName = obj.toString().trim();
         
         //map does not have this parameter, skip
         if (!paramMap.containsKey(paramName))
            continue;
         
         obj = paramMap.get(paramName);
         //The object is not an array, dont worry about it!
         if (!(obj instanceof ArrayList))
            continue;

         ArrayList list = (ArrayList)obj;
         //Replace the parameter with the first value
         paramMap.put(paramName, list.get(0));
      }
   }
}

