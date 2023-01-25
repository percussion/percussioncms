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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/**
 * Forces the named request parameters' values to <code>null</code>, when their
 * current value is equal to a specified value.  Useful for clearing the
 * "-- Choose --" value of posted HTML &lt;select> tags.
 */
public class PSNullIf extends PSDefaultExtension
   implements IPSRequestPreProcessor
{

   /**
    * Compares the request parameters' values whose names are supplied to the
    * supplied value, and if they match, sets the request parameter's value to
    * <code>null</code>. Comparison is case-sensitive.
    *
    * @param params the parameters for this execution of the extension.  Any
    * number of parameters may be provided.  If <code>null</code>, exit takes
    * no action.<br>
    * <code>params[0]</code> - the value that will be converted to
    * <code>null</code>, if <code>null</code> or empty, exit takes no
    * action.<br>
    * <code>params[1] .. [n]</code> - the name of a request parameter whose
    * value will be set to <code>null</code> if equal to <code>params[0]</code>.
    * Skipped if <code>null</code> or empty.
    *
    * @param request the request context object, used to obtain the parameter
    * values.  If <code>null</code>, exit takes no action.
    *
    * @throws PSParameterMismatchException never
    * @throws PSExtensionProcessingException never
    * @throws PSAuthorizationException never
    * @throws PSRequestValidationException never
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
      PSParameterMismatchException, PSExtensionProcessingException
   {
      if (request == null)
         return;

      if (params == null || params[0] == null)
         return;

      String compareTo = params[0].toString().trim();
      if (0 == compareTo.length())
      {
         // nothing to compare to, just return
         return;
      }

      // loop through the remaining exit parameters, setting the request
      // parameter it names to null if its value matches the compareTo value
      for (int paramIndex = 1;
           paramIndex < params.length && null != params[paramIndex] &&
           params[paramIndex].toString().trim().length() > 0; paramIndex++)
      {
         String paramName = params[paramIndex].toString().trim();
         String paramValue = request.getParameter( paramName );
         if (paramValue != null && compareTo.equals( paramValue ))
         {
            request.printTraceMessage( "...setting " + paramName + " to null" );
            request.setParameter( paramName, null );
         }
      }
   }
}
