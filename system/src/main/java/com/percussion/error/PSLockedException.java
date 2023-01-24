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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exception indicating if there was failure in acquiring the lock.
 */
public class PSLockedException extends PSDeployException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    */
   public PSLockedException(int msgCode, Object singleArg)
   {
      this(msgCode, new Object[] { singleArg });
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */
   public PSLockedException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    */
   public PSLockedException(int msgCode)
   {
      this(msgCode, null);
   }

   /**
    * This constructor is not supported by this exception class.  Use the
    * {@link PSDeployException#PSDeployException(PSException) ctor} from the
    * base class.
    *
    * @throws UnsupportedOperationException always
    */
   public PSLockedException(PSException ex)
   {
      super(ex);
      throw new UnsupportedOperationException("ctor not supported");
   }

   /**
    * Construct an exception from its XML representation.
    *
    * @param source The root element of this object's XML representation.
    * Format expected is defined by the base class's
    * {@link PSDeployException#toXml(Document) toXml} method documentation.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by the class.
    */
   public PSLockedException(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * Sets the original exception class to this class so that when restored from
    * XML as a <code>PSDeployException</code>, it can be converted back to this
    * type of exception.  See {@link PSDeployException#toXml(Document)
    * super.toXml()} for more info.
    */
   public Element toXml(Document doc)
   {
      m_originalExceptionClass = getClass().getName();
      return super.toXml(doc);
   }
}
