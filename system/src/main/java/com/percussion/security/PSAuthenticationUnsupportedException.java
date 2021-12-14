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
 * PSAuthenticationUnsupportedException is thrown to indicate that AuthenticationUnsupported
 * has been denied to the requested resource. This may occur when connecting to
 * the server, or when attempting to access an application.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSAuthenticationUnsupportedException extends PSException
{
   /**
    * Constructs an authentication unsupported exception with the default
    * message.
    */
   public PSAuthenticationUnsupportedException(java.lang.String provider)
   {
      super(IPSSecurityErrors.AUTHENTICATION_NOT_SUPPORTED,
            new Object[] { provider } );
   }
}

