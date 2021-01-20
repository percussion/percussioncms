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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.xml.serialization;

/**
 * Exception that is thrown due to any irrecoverable error during serialization
 * and deserialization of objects. This is a nested exception and will have
 * information about the chain of exceptions.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSObjectSerializerException extends Exception
{
   /**
    * Auto generated serialization id.
    */
   private static final long serialVersionUID = 4176547846912708878L;

   /**
    * Delegates to base class version.
    */
   public PSObjectSerializerException()
   {
   }

   /**
    * Delegates to base class version.
    * 
    * @see java.lang.Exception#Exception(java.lang.String)
    */
   public PSObjectSerializerException(String msg)
   {
      super(msg);
   }

   /**
    * Delegates to base class version.
    * 
    * @see Exception#Exception(java.lang.Throwable)
    */
   public PSObjectSerializerException(Throwable nestedException)
   {
      super(nestedException);
   }

   /**
    * Delegates to base class version.
    * 
    * @see Exception#Exception(java.lang.String, java.lang.Throwable)
    */
   public PSObjectSerializerException(String msg, Throwable nestedException)
   {
      super(msg, nestedException);
   }
}
