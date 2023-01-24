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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import java.io.File;

/**
 * This pre-exit copies a request parameter from the key specified by the 
 * source exit parameter to the key specified by the destination exit parameter
 */
public class PSCopyParameter implements IPSItemInputTransformer
{
   public PSCopyParameter()
   {
      // nothing to do
   }

   
   // see IPSRequestPreProcessor
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      // expects two string parameters   
      String sourceName = getParameter(params, 0);
      String destinationName = getParameter(params, 1);

      Object o = request.getParameterObject(sourceName);
      if (o != null)
         request.setParameter(destinationName, o);
   }


   // see IPSRequestPreProcessor
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // nothing to do
   }


   /**
    * Get a parameter from the parameter array, and return it as a string.
    *
    * @param params array of parameter objects from the calling function.
    * @param index the integer index into the parameters
    * 
    * @return a not-null, not-empty string which is the value of the parameter
    * @throws PSParameterMismatchException if the parameter is missing or empty
    **/
   private static String getParameter(Object[] params, int index)
         throws PSParameterMismatchException
   {
      if (params.length < index + 1 || null == params[index] ||
            params[index].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException(PSCopyParameter.class +
               ": Missing exit parameter");
      }
      else
      {
         return params[index].toString().trim();
      }
   }

}
