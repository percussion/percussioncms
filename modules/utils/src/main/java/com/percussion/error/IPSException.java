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
 * This interface is used to define all of our exception classes.
 */
public interface IPSException
{
   /**
    * Returns the localized detail message of this exception.
    *
    * @param   locale      the locale to generate the message in
    *
    * @return               the localized detail message
    */
   String getLocalizedMessage(java.util.Locale locale);

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage();

   /**
    * Returns the detail message of this exception.
    *
    * @return               the detail message
    */
   String getMessage();

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return   the error code
    */
   int getErrorCode();

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return   the error arguments
    */
    Object[] getErrorArguments();

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArg         the argument to use as the sole argument in
    *                           the error message
    */
    void setArgs(int msgCode, Object errorArg);

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArgs      the array of arguments to use as the arguments
    *                           in the error message
    */
    void setArgs(int msgCode, Object[] errorArgs);
}

