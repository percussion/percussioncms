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
package com.percussion.cx.error;

import com.percussion.error.PSStandaloneException;

/**
 * This class is used when an error occurs during PSOption handling (loading,
 * persisting, creating etc).
 */
public class PSOptionException extends PSStandaloneException
{

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    */
   public PSOptionException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param singleMessage the sole of argument to use as the arguments in the
    *    error message
    */
   public PSOptionException(int msgCode, String singleMessage)
   {
      super(msgCode,singleMessage);
   }
   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *    error message
    */
   public PSOptionException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode,arrayArgs);
   }

   /**
    * @see #com.percussion.error.PSStandaloneException PSStandaloneException
    */
   protected String getResourceBundleBaseName()
   {
      return getClass().getPackage().getName() + "." + STRING_BUNDLE_NAME;
   }

   /**
    * See {@link PSStandaloneException#getXmlNodeName()}
    */
   protected String getXmlNodeName()
   {
      return "PSXOptionException";
   }

   /**
    * The String bundle used with this Exception.
    */
   public static final String STRING_BUNDLE_NAME =
      "PSContentExplorerErrorStringBundle.properties";
}


