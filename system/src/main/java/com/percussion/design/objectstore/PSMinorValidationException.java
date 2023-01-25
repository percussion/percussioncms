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

package com.percussion.design.objectstore;



/**
 * PSMinorValidationException is thrown when a recoverable validation error occurs.
 * This usually occurs when an application is being opened by a workbench
 * and an invalid value is detected.
*/
public class PSMinorValidationException extends PSSystemValidationException {
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode       the error string to load
    *
    * @param singleArg      the argument to use as the sole argument in
    *                      the error message
    */
   public PSMinorValidationException(int msgCode, Object singleArg)
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
   public PSMinorValidationException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSMinorValidationException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode       the error string to load
    *
    * @param   arrayArgs   the array of arguments to use as the arguments
    *                      in the error message
    *
    * @param   container   the container object holding the component
    *                        which is the source of the error
    *
    * @param   component   the component which is the source of the error
    *                        or <code>null</code> if the container is in error
    */
   public PSMinorValidationException(int msgCode, Object[] arrayArgs,
      IPSDocument container, IPSComponent component)
   {
      super(msgCode, arrayArgs);

      m_sourceDocument   = container;
      m_sourceComponent   = component;
   }

   /**
    * Get the container (document) holding the component which is the
    * source of the error.
    *
    * @return               the container object holding the component
    *                        which is the source of the error or
    *                        <code>null</code> if one was not specified
    */
   public IPSDocument getSourceContainer()
   {
      return m_sourceDocument;
   }

   /**
    * Get the component which is the source of the error.
    *
    * @return               the component which is the source of the error
    *                        or <code>null</code> if the container is in error
    */
   public IPSComponent getSourceComponent()
   {
      return m_sourceComponent;
   }


   protected IPSDocument   m_sourceDocument   = null;
   protected IPSComponent   m_sourceComponent   = null;
}

