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
package com.percussion.services.security.loginmods;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

/**
 * Handle login and logout tasks, authenticate against the back-end table
 * information
 * @author dougrand
 * @version 1.0
 * @updated 31-Oct-2005 8:31:52 AM
 */
public interface IPSLoginMgr
{
   /**
    * Authenticate the credentials
    * 
    * @param user the username, may be <code>null</code> or empty
    * @param password the password, may be <code>null</code>
    * @param callbackHandler The callback handler to use, may not be 
    * <code>null</code>.
    * 
    * @return The user entry if authenticated, <code>null</code> if not.
    * 
    * @throws LoginException if there is an error in authentication
    */
   Subject login(String user, String password, 
      CallbackHandler callbackHandler) throws LoginException;
   
   /**
    * Logout the given user
    * @param user the username, must never be <code>null</code> or empty
    * @return <code>true</code> if this was sucessful
    * @throws LoginException if there's an error in authentication
    */
   boolean logout(String user) throws LoginException;
}
