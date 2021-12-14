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

package com.percussion.security;

import com.percussion.error.PSException;


/**
 * PSAuthenticationFailedException is thrown to indicate that a user
 * failed to authenticate through the specified provider. This usually
 * means that either the user id or the password is incorrect.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSAuthenticationFailedException extends PSException
{
   /**
    * Constructs an authentication failed exception with the default
    * message.
    */
   public PSAuthenticationFailedException( String type,
                                           String instanceName,
                                           String uid)
   {
      super(IPSSecurityErrors.AUTHENTICATION_FAILED,
         new Object[] { type, instanceName, uid } );
   }

   /**
    * Constructs an authentication failed exception describing the cause
    * of the failure.
    */
   public PSAuthenticationFailedException( String type,
                                           String instanceName,
                                           String uid,
                                           String failureMessage)
   {
      super(IPSSecurityErrors.AUTHENTICATION_FAILED_WITH_MSG,
         new Object[] { type, instanceName, uid, failureMessage } );
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                           in the error message
    */
   public PSAuthenticationFailedException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
}

