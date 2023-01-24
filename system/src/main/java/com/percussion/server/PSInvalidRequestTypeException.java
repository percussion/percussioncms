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

package com.percussion.server;

import com.percussion.error.PSException;


/**
 * PSInvalidRequestTypeException is thrown to indicate that a request was
 * received by a handler which is not of the appropriate type.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSInvalidRequestTypeException extends PSException
{
   /**
    * Constructs an invalid request type exception with the specified error
    * contents. The error string is formatted by loading the string
    * associated with the error code and passing it the array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   errorCode      the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                           in the error message
    */
   public PSInvalidRequestTypeException(   int errorCode,
                                          Object[] arrayArgs)
   {
      super(errorCode, arrayArgs);
   }
}

