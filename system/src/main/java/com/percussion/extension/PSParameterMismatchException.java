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
package com.percussion.extension;

import com.percussion.error.PSException;

/**
 * PSParameterMismatchException is thrown to indicate that an extension
 * or extension was initialized with the incorrect parameter values.
 * <P>
 * Extensions provide their parameter definitions through the
 * {@link IPSExtensionDef#getRuntimeParameter IPSExtensionDef.getRuntimeParameter}
 * method. The corrsponding run-time values must then be set via an
 * unspecified mechanism (usually a sub-interface will define how params are to
 * be set).
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSParameterMismatchException extends PSException
{
   /**
    * Constructs a parameter mismatch exception when an incorrect number
    * of values was specified.
    *
    * @param expected The number of parameters expected by the extension.
    *
    * @param actual The number of values sent to the extension.
    */
   public PSParameterMismatchException(int expected, int actual)
   {
      super(IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH,
      new Object[] { new Integer(expected), new Integer(actual) });
   }
   
   /**
    * Constructs a parameter mismatch exception for the provided message.
    *
    * @param message the complete display message.
    */
   public PSParameterMismatchException(String message)
   {
      super(IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, message);
   }

   /**
    * Constructs a parameter mismatch exception when an incorrect number
    * of values was specified.
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    * @param expected The number of parameters expected by the extension.
    * @param actual The number of values sent to the extension.
    */
   public PSParameterMismatchException(String language, int expected, int actual)
   {
      super(language, IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH,
      new Object[] { new Integer(expected), new Integer(actual) });
   }

   /**
    * Constructs a parameter mismatch exception for the provided message.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param message the complete display message.
    */
   public PSParameterMismatchException(String language, String message)
   {
      super(language, IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, message);
   }

   /**
    * See {@link com.percussion.error.PSException#PSException(int, Object)} 
    * for documentation.
    */
   public PSParameterMismatchException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * See {@link com.percussion.error.PSException#PSException(int, Object)} 
    * for documentation.
    */
   public PSParameterMismatchException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
}
