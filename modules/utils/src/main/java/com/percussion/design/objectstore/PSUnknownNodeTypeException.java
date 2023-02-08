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

import com.percussion.error.PSErrorManager;
import com.percussion.error.PSException;


/**
 * PSUnknownNodeTypeException is thrown when fromXml is called in one of
 * the IPSComponent implementations and the implementation does not
 * support the specified XML node type.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUnknownNodeTypeException extends PSException {
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode       the error string to load
    *
    * @param singleArg      the argument to use as the sole argument in
    *                      the error message
    */
   public PSUnknownNodeTypeException(int msgCode, Object singleArg)
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
   public PSUnknownNodeTypeException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSUnknownNodeTypeException(int msgCode)
   {
      super(msgCode);
   }
   
   /**
    * Construct this exception using IPSObjectStore.XML_ELEMENT_INVALID_CHILD
    * as the error message. The specified parent node, child element and
    * PSException subclass will be used to construct the detail message.
    *
    * @param parent       the parent node
    *
    * @param child       the child element
    *
    * @param e           the exception to use as the error description
    */
   public PSUnknownNodeTypeException(   String parent,
      String child,
      PSException e)
   {
      this(   IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
         new Object[] { parent, child, "" });
      m_exception = e;
   }
   
   /**
    * Returns the localized detail message of this exception.
    *
    * @param locale       the locale to generate the message in
    *
    * @return                the localized detail message
    */
   public java.lang.String getLocalizedMessage(java.util.Locale locale)
   {
      /* use the exception's message as arg[2], the description string */
      if (m_exception != null)
         m_args[2] = m_exception.getLocalizedMessage(locale);
      
      return PSErrorManager.createMessage(m_code, m_args, locale);
   }
   
   
   private PSException m_exception = null;
}

