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

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * All security providers must implement this interface. Security providers
 * are used to authenticate users and associate attributes with them once
 * authenticated.
 */
public interface IPSSecurityProvider
{
   /**
    * @return the security provider type, never <code>null</code> or empty.
    */
   public String getType();
   
   /**
    * @return the security provider type id. This is a numeric id for the
    *    security provider type <code>String</code>.
    */
   public int getTypeId();
   
   /**
    * @return the security provider instance name, never <code>null<code> or
    *    empty. This name is unique across all security provider instances.
    */
   public String getInstance();

   /**
    * @return the meta data associated with this security provider instance.
    */
   public IPSSecurityProviderMetaData getMetaData();

   /**
    * Tries to authenticate the user for the specified credentials.
    *
    * @param uid the name of the user to be authenticated, may be
    *    <code>null</code> or empty.
    * @param pw the password for the user to be authenticated, may be
    *    <code>null</code> or empty.        
    * @param callbackHandler  Callback handler for providers that need access to
    * data held by the login context, never <code>null</code>.
    * 
    * @return The authenticated user entry, never <code>null</code>.
    *  
    * @throws PSAuthenticationUnsupportedException if authentication is not
    *    supported by this security provider.
    * @throws PSAuthenticationFailedException if uid or pw is incorrect,
    *    causing the authentication of the user to fail.
    * @throws IOException If an IOException occurs invoking the callback handler
    * @throws UnsupportedCallbackException If an invalid callback is attempted.
    */
   public PSUserEntry authenticate(String uid, String pw, 
      CallbackHandler callbackHandler)
      throws PSAuthenticationUnsupportedException,
         PSAuthenticationFailedException, IOException, 
         UnsupportedCallbackException;
}

