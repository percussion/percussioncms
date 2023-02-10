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
 * PSAuthenticationFailedException is thrown to indicate that a user
 * failed to authenticate through the specified provider. This usually
 * means that either the user id or the password is incorrect.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSAuthenticationFailedException extends PSException
{
   /**
    * Constructs an authentication failed exception with the default
    * message.
    */
   public PSAuthenticationFailedException( String type,
                                           String instanceName,
                                           String uid)
   {
      super(IPSSecurityErrors.AUTHENTICATION_FAILED,
         new Object[] { type, instanceName, uid } );
   }

   /**
    * Constructs an authentication failed exception describing the cause
    * of the failure.
    */
   public PSAuthenticationFailedException( String type,
                                           String instanceName,
                                           String uid,
                                           String failureMessage)
   {
      super(IPSSecurityErrors.AUTHENTICATION_FAILED_WITH_MSG,
         new Object[] { type, instanceName, uid, failureMessage } );
   }

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
   public PSAuthenticationFailedException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
}

