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

package com.percussion.design.objectstore.server;

import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;

import java.lang.reflect.Method;

/**
 *
 *      This class defines a "secured" method which must check a specified
 *      user's access level to ensure the selected operation can be executed.
 *
 * @author:      David Gennaco
 *   @version:   1.0
 * @since      1.0
 *
 ***************************************************************************/
class PSSecuredMethod
{

   /**
    *
    * Secured method constructor.  Get the method and access level
    * required to be allowed to invoke it
    * this object.
    *
    *   @param      method               The method
    *
    *   @param      requiredAccessLevel   The access level needed to invoke
    *
    */
   public PSSecuredMethod(Method method, int requiredAccessLevel)
   {
      m_method = method;
      m_requiredAccessLevel = requiredAccessLevel;
   }

   /**
    *
    * This function invokes the member method if the user authentication
    * defined in the request meets the required access level criteria of
    * this object.
    *
    *   @param      req      The current request
    *
    *   @param      currentObject   The object containing the method.
    *
    *   @param      args      The arguments to supply to the specified method
    *
    * @return      the return value of the invoked method
    *
    */
   public Object invoke(PSRequest req,
                        Object currentObject, Object[] args)
      throws   com.percussion.security.PSAuthorizationException, 
               com.percussion.security.PSAuthenticationFailedException,
               com.percussion.security.PSAuthenticationRequiredException,
               java.lang.reflect.InvocationTargetException,
               java.lang.IllegalAccessException
   {
      PSServer.checkAccessLevel(req, m_requiredAccessLevel);

      return m_method.invoke(currentObject, args);
   }

   Method   m_method;
   int      m_requiredAccessLevel;
}
