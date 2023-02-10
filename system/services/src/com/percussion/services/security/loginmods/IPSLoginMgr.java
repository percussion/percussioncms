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
