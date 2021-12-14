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
package com.percussion.test.util.itemcreator;

/**
 * An exception that is thrown when an invalid configuration
 * is found when creating a <code>PSItemCreatorConfig</code>
 * object from its xml configuration file.
 */
public class PSInvalidItemCreatorConfigException extends Exception
{

   /**
    * 
    */
   public PSInvalidItemCreatorConfigException()
   {
      super();
      // TODO Auto-generated constructor stub
   }
   /**
    * @param message
    */
   public PSInvalidItemCreatorConfigException(String message)
   {
      super(message);
      // TODO Auto-generated constructor stub
   }
   /**
    * @param message
    * @param cause
    */
   public PSInvalidItemCreatorConfigException(String message, Throwable cause)
   {
      super(message, cause);
      // TODO Auto-generated constructor stub
   }
   /**
    * @param cause
    */
   public PSInvalidItemCreatorConfigException(Throwable cause)
   {
      super(cause);
      // TODO Auto-generated constructor stub
   }
}
