/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.design.objectstore;

import com.percussion.error.PSErrorManager;
import com.percussion.error.PSException;


/**
 * PSUnknownDocTypeException is thrown when fromXml is called in one of
 * the IPSComponent implementations and the implementation does not
 * support the specified XML document type.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUnknownDocTypeException extends PSException {
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode       the error string to load
    *
    * @param singleArg      the argument to use as the sole argument in
    *                      the error message
    */
   public PSUnknownDocTypeException(int msgCode, Object singleArg)
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
   public PSUnknownDocTypeException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSUnknownDocTypeException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct this exception using IPSObjectStore.XML_ELEMENT_INVALID_CHILD
    * as the error message. The specified parent node, child element and
    * PSException subclass will be used to construct the detail message.
    *
    * @param parent         the parent node
    *
    * @param child         the child element
    *
    * @param e             the exception to use as the error description
    */
   public PSUnknownDocTypeException(   String parent,
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
    * @param locale         the locale to generate the message in
    *
    * @return               the localized detail message
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

