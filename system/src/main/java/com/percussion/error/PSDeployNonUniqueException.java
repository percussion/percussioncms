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

package com.percussion.error;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exception to indicate something that is being processed by the server would
 * violate some kind of non-unique constraint.
 */
public class PSDeployNonUniqueException extends PSDeployException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    */
   public PSDeployNonUniqueException(int msgCode, Object singleArg)
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
   public PSDeployNonUniqueException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    */
   public PSDeployNonUniqueException(int msgCode)
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
   public PSDeployNonUniqueException(PSException ex)
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
   public PSDeployNonUniqueException(Element source) 
      throws PSUnknownNodeTypeException
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