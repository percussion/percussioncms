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

import com.percussion.extension.*;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Map;

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
    * @param params - array of parameters supplied as exit parameters, may be
    * <code>null</code> but normally not.
    * @param request - the <code>IPSRequestContext</code> object assumed to be
    * not <code>null</code>.
    */
   private void handleParam(Object[] params, IPSRequestContext request)
   {
      request.printTraceMessage("Starting collapseHTMLParameter...");

      Map<String,Object> paramMap = request.getParameters();
      /*
       * if the html param map is empty or the supplied parameter array empty
       * do nothing
       */
      if (paramMap == null || params == null || params.length < 1)
         return;

      Object obj = null;
      String paramName = null;
      for (Object param : params) {
         obj = param;
         if (obj == null)
            continue;
         paramName = obj.toString().trim();

         //map does not have this parameter, skip
         if (!paramMap.containsKey(paramName))
            continue;

         obj = paramMap.get(paramName);
         //The object is not an array, dont worry about it!
         if (!(obj instanceof ArrayList))
            continue;

         ArrayList list = (ArrayList) obj;
         //Replace the parameter with the first value
         paramMap.put(paramName, list.get(0));
      }
   }
}

