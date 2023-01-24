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
 * PSDataExtractionException is thrown to indicate that the requested
 * data extraction failed.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataExtractionException extends PSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode          the error string to load
    *
    * @param singleArg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSDataExtractionException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
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
   public PSDataExtractionException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSDataExtractionException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param msgCode          the error string to load
    *
    * @param singleArg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSDataExtractionException(String language, int msgCode,
      Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param msgCode       the error string to load
    *
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSDataExtractionException(String language, int msgCode,
      Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param msgCode       the error string to load
    */
   public PSDataExtractionException(String language, int msgCode)
   {
      super(language, msgCode);
   }
}
