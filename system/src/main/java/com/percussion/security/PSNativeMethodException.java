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
 * The PSImpersonationException class is thrown to indicate that a native
 * method call to ImpersonateLoggedOnUser did not excecute successfully.
 *
 * @author    Jian Huang
 * @version   1.0
 * @since     1.0
 */
public class PSNativeMethodException extends PSException
{
   /**
    * This one should only be used when logon failed.
    */
   public PSNativeMethodException(String message){
      super(IPSSecurityErrors.NATIVE_AUTHENTICATION_FAILURE, new Object[] { message });
   }

   /**
    * Construct an exception for messages taking a single arguments.
    *
    * @param   msgCode      the error string indexed by this number to load
    * @param   singleArg   the sole argument in the error message
    */
   public PSNativeMethodException(int msgCode, Object singleArg){
      super(msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking an array of arguments.
    * Be sure to store the arguments in the correct order in the array,
    * where {0} in the string is array element 0, etc.
    *
    * @param   msgCode      the error string indexed by this number to load
    * @param   arrayArgs   the argument array in the error message
    */
   public PSNativeMethodException(int msgCode, Object[] arrayArgs){
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param   msgCode      the error string indexed by this number to load
    */
   public PSNativeMethodException(int msgCode){
      super(msgCode);
   }
}
