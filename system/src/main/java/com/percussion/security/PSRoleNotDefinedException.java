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
 * PSRoleNotDefinedException is thrown to indicate that a role reference
 * was made to a role the role security rovider does not know about.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSRoleNotDefinedException extends PSException
{
   /**
    * Constructs a role not defined exception with the default
    * message.
    *
    * @param   appName            the app name (local prefix)
    *
    * @param   roleName            the name of the role
    */
   public PSRoleNotDefinedException(   java.lang.String appName,
                                    java.lang.String roleName)
   {
      super(IPSSecurityErrors.LOCAL_ROLE_NOT_DEFINED,
            new Object[] { roleName, appName });
   }

   /**
    * Constructs a role not defined exception for non-application ACLs
    * with the default message. If an application ACL is throwing this
    * exception, be sure to use the constructor which takes the name
    * of the application.
    *
    * @param   roleName            the name of the role
    */
   public PSRoleNotDefinedException(java.lang.String roleName)
   {
      super(IPSSecurityErrors.GLOBAL_ROLE_NOT_DEFINED,
            new Object[] { roleName });
   }
}

