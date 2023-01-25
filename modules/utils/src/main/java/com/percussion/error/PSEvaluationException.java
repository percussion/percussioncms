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

package com.percussion.error;

/**
 * The PSEvaluationException class is thrown whenever a non-supported evaluation
 * is attempted to perform in a routine. One example is to evaluate the conditional
 * abcde > 12345, which is certainly not comparable.
 *
 * @author   Jian Huang
 * @version  1.0
 * @since    1.0
 */
public class PSEvaluationException extends PSRuntimeException
{
   /**
    * Construct an exception for messages taking only one single argument.
    *
    * @param msgCode      an integer stands for the error string to load
    * @param singleArg   the argument to use as the sole argument in the error message
    */
   public PSEvaluationException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking an array of arguments. Be sure
    * to store the arguments in the correct order in the array, where {0} in the
    * string is array element 0, etc.
    *
    * @param msgCode      an integer stands for the error string to load
    * @param arrayArgs   the array of arguments to use as the arguments in the error
    *                     message
    */
   public PSEvaluationException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no argument.
    *
    * @param msgCode      an integer stands for the error string to load
    */
   public PSEvaluationException(int msgCode)
   {
      super(msgCode);
   }
}
