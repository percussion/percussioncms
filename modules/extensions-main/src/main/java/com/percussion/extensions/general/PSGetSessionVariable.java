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
 * Rhythmyx pre-exit to fetch an session object and add it to request
 * parameters.
 *
 * See {@link PSGetSessionVariable#preProcessRequest preProcessRequest} for a
 * description.
 */
public class PSGetSessionVariable extends PSDefaultExtension
      implements IPSRequestPreProcessor
{
   /**
    * Gets a session object and adds it to the request parameters as a String.
    *
    * @param params An array providing the input paramters to this exit. Only
    * the first element will be processed, and must be the name of the key under
    * which the object is stored in the session.  This name is also used as the
    * parameter name under which this object is added to the request parameters.
    * The name is obtained by doing a <code>toString</code> on the supplied
    * object.  If <code>null</code> or empty, this method simply returns.  If
    * calling <code>toString</code> on the object returns an empty String, it is
    * not added to the request parameters.
    *
    * @param request The current request context. May not be <code>null</code>.
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

      /* retreive the id from the session object based upon the key of the html
       * parameter
       */
      Object sessObj = request.getSessionPrivateObject(psxparam);
      if (null == sessObj || sessObj.toString().length() < 1)
      {
         request.printTraceMessage("No session object key with that name");
      }
      else
      {
         // set the parameter value of the html request
         request.setParameter(psxparam, sessObj);
      }

      return;
   }


}
