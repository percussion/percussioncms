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

package com.percussion.data;

import com.percussion.error.PSException;


/**
 * PSInternalRequestCallException is thrown to indicate that the requested
 * conversion is not supported by the converter. This will usually happen
 * when the extension specified in the request URL is not supported.
 *
 */
public class PSInternalRequestCallException extends PSConversionException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode          the error string to load
    *
    * @param singleArg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSInternalRequestCallException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }
   
   /**
    * Construct an exception for messages taking only a single argument.
    * 
    * @param msgCode the error string to load
    * 
    * @param singleArg the argument to use as the sole argument in the error
    *           message
    *           
    * @param exception the original exception, may be <code>null</code>.          
    */
   public PSInternalRequestCallException(int msgCode, String singleArg,
         Exception exception) {
      super(msgCode, exception, singleArg);
   }   
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode       the error string to load
    *
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSInternalRequestCallException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode       the error string to load
    *
    * @param  cause         Root case of the exception is getting passed in
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSInternalRequestCallException(int msgCode, Throwable cause, Object[] arrayArgs)
   {
      super(msgCode, cause, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSInternalRequestCallException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param errorCode  the error string to load
    *
    * @param errorArguments the array of arguments to use as the arguments
    *                      in the error message
    * @param exception  the original exception, may be <code>null</code>.
    */
   public PSInternalRequestCallException(int errorCode, Object[] errorArguments, 
         PSException exception) {
      super(errorCode, exception, errorArguments);
   }
}


