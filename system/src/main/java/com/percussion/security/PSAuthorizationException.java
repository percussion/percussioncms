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

import com.percussion.error.PSException;

/**
 * PSAuthorizationException is thrown to indicate that authorization has
 * been denied to the requested resource. This may occur when connecting to
 * the server, or when attempting to access an application.
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSAuthorizationException extends PSException
{

   /**
    * Constructs an authorization exception with the default message.
    * @param resourceType
    * @param resourceName
    * @param sessionId
    */
    public PSAuthorizationException(String resourceType,
    String resourceName, String sessionId)
   {
      super(IPSSecurityErrors.SESS_NOT_AUTHORIZED,
            new Object[] { resourceType, resourceName, sessionId } );
   }

   /**
    * Constructs an authorization exception with the default message.
    *
    * @param language   language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param resourceType
    * @param resourceName
    * @param sessionId
    */
   public PSAuthorizationException(String language, String resourceType,
    String resourceName, String sessionId)
   {
      super(language, IPSSecurityErrors.SESS_NOT_AUTHORIZED,
            new Object[] { resourceType, resourceName, sessionId } );
   }

   /**
    * @param language   language string to use while lookingup for the
    * message text in the resource bundle
    * @param resourceType
    * @param resourceName
    * @param securityProvider
    * @param userName
    */
   public PSAuthorizationException(String language, String resourceType,
    String resourceName, String securityProvider, String userName)
   {
      super(language, IPSSecurityErrors.USER_NOT_AUTHORIZED,
            new Object[] { resourceType, resourceName,
            securityProvider, userName } );
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *   in the error message
    */
   public PSAuthorizationException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param language   language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *   in the error message
    */

   public PSAuthorizationException(String language, int msgCode,
    Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }
}
