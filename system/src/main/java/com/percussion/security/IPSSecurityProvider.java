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

