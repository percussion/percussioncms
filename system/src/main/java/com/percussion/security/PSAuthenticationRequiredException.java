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

package com.percussion.security;



/**
 * PSAuthenticationRequiredException is thrown to indicate that a user
 * must login (authenticate) to gain access to a resource. This usually
 * occurs when the user has not attempted any form of authentication.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSAuthenticationRequiredException extends PSAuthorizationException
{
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                           in the error message
    */
   public PSAuthenticationRequiredException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Constructs an authentication required exception with the default
    * message.
    */
   public PSAuthenticationRequiredException(   java.lang.String resourceType,
                                             java.lang.String resourceName)
   {
      super(IPSSecurityErrors.AUTHENTICATION_REQUIRED,
            new Object[] { resourceType, resourceName} );
   }
}

