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
import com.percussion.server.IPSRequestContext;

/**
 * Rhythmyx pre-exit to set a session object from a request parameter.
 *
 * See {@link PSSetSessionVariable#preProcessRequest preProcessRequest} for a
 * description.
 */
public class PSSetSessionVariable extends PSDefaultExtension implements IPSRequestPreProcessor
{
   /**
    * Sets a session variable with the value of a request parameter.
    *
    * @param params An array providing the input paramters to this exit. If
    * <code>null</code> or empty, no processing is done and the method simple
    * returns.  Only the first element in the array will be processed, and must
    * be the name of the request parameter containing the value to store in the
    * session.  This name is also used as the key under which the object is
    * stored in the session. The request parameter's value is stored in the
    * session as a String by calling <code>toString</code> on it.  If the value
    * of the specified request parameter is <code>null</code> or <code>toString
    * </code> returns an empty String, the session variable is not set.
    *
    * @param request The current request context. May not be <code>null</code>.
    *
    * @throws PSAuthorizationException - If the user is not authorized
    * to perform this request.
    *
    * @throws PSRequestValidationException - If the request does not meet
    * the required validation rules.
    *
    * @throws PSParameterMismatchException - If a call to setParamValues was
    * never made, or the runtime parameters specified in that call are
    * incorrect for the usage of this extension.
    *
    * @throws PSExitProcessorException - If any other exception occurs which
    * prevents the proper handling of this request
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      if (null == request)
         throw new IllegalArgumentException("request context must not be null");

      // check for blank parameter array
      if (null == params || params.length < 1 || null == params[0])
      {
         return;
      }

      // retrieve first (and only valid) parameter
      String psxparam = params[0].toString().trim();

      // retrieve the parameter value
      String param = request.getParameter(psxparam);

      // check for valid id return
      if (param == null || param.length() < 1)
      {
         request.printTraceMessage("No HTML parameter by that name");
      }
      else
      {
         // set session with key/value pair
         request.setSessionPrivateObject(psxparam, param);
      }
      return;
   }
}


