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
package com.percussion.workflow;

import com.percussion.error.PSException;

public class PSXMLNodeMissingException extends PSException
{
   /**
    * Construct an exception for messages taking locale and msgCode arguments.
    *
    * @param language  language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode the error string to load
    */
   public PSXMLNodeMissingException(String language, int msgCode)
   {
      super(language, msgCode);
   }

   /**
    * Construct an exception for messages taking locale and message code
   * arguments and and a single argument.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode the error string to load
    *
    * @param singleArg the argument to use as the sole argument in
    * the error message.Can be <code>null</code>.
    */
   public PSXMLNodeMissingException(String language, int msgCode,
         Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

  /**
    * Construct an exception for messages taking language, message code 
    * and an array of arguments. Be sure to store the arguments in the 
    * correct order in the array, where {0} in the string is array 
    * element 0, etc.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode  the error string to load
    *
    * @param arrayArgs   the array of arguments to use as the arguments
    * in the error message.Can be <code>null</code>.
    */
   public PSXMLNodeMissingException(String language, int msgCode,
         Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }
}
