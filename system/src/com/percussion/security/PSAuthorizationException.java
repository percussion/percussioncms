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

package com.percussion.security;

import com.percussion.error.PSException;

/**
 * PSAuthorizationException is thrown to indicate that authorization has
 * been denied to the requested resource. This may occur when connecting to
 * the server, or when attempting to access an application.
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSAuthorizationException extends PSException
{

   /**
    * Constructs an authorization exception with the default message.
    * @param resourceType
    * @param resourceName
    * @param sessionId
    */
    public PSAuthorizationException(String resourceType,
    String resourceName, String sessionId)
   {
      super(IPSSecurityErrors.SESS_NOT_AUTHORIZED,
            new Object[] { resourceType, resourceName, sessionId } );
   }

   /**
    * Constructs an authorization exception with the default message.
    *
    * @param language   language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param resourceType
    * @param resourceName
    * @param sessionId
    */
   public PSAuthorizationException(String language, String resourceType,
    String resourceName, String sessionId)
   {
      super(language, IPSSecurityErrors.SESS_NOT_AUTHORIZED,
            new Object[] { resourceType, resourceName, sessionId } );
   }

   /**
    * @param language   language string to use while lookingup for the
    * message text in the resource bundle
    * @param resourceType
    * @param resourceName
    * @param securityProvider
    * @param userName
    */
   public PSAuthorizationException(String language, String resourceType,
    String resourceName, String securityProvider, String userName)
   {
      super(language, IPSSecurityErrors.USER_NOT_AUTHORIZED,
            new Object[] { resourceType, resourceName,
            securityProvider, userName } );
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *   in the error message
    */
   public PSAuthorizationException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param language   language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *   in the error message
    */

   public PSAuthorizationException(String language, int msgCode,
    Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }
}
