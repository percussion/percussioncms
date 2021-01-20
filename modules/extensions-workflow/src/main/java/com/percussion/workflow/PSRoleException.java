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
package com.percussion.workflow;

import com.percussion.error.PSException;
/**
 * This error class is used to indicate that a an error has occurred when
 * processing state role or adhoc user role information.
 */
public class PSRoleException extends PSException
{
   /**
    * Coonstruct the exception using error message string.
    * @param msg error message.
    */
   public PSRoleException(String msg)
   {
      super(msg);
   }

   /**
    * Construct an exception for messages taking message code.
    * @param msgCode the error string to load.
    */
   public PSRoleException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking locale and msgCode arguments.
    *
    * @param language  language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode the error string to load
    */
   public PSRoleException(String language, int msgCode)
   {
      super(language, msgCode);
   }

   /**
    * Construct an exception for messages taking locale and message code
   * arguments and and a single argument.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode the error string to load
    *
    * @param singleArg the argument to use as the sole argument in
    * the error message. Can be <code>null</code>. 
    */
   public PSRoleException(String language, int msgCode,
                                    Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking language, message code 
    * and an array of arguments. Be sure to store the arguments in the 
    * correct order in the array, where {0} in the string is array 
    * element 0, etc.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode  the error string to load
    *
    * @param arrayArgs   the array of arguments to use as the arguments
    * in the error message. Can be <code>null</code>. 
    */
   public PSRoleException(String language, int msgCode,
                                    Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments and message code. Be sure to store the arguments 
   * in the correct order in the array, where {0} in the string 
   * is array element 0, etc.
    * @param msgCode the error string to load
    *
    * @param arrayArgs   the array of arguments to use as the arguments
    * in the error message. Can be <code>null</code>. 
    */
   public PSRoleException( int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
}
