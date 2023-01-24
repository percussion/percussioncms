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
package com.percussion.utils.security;

import org.apache.commons.lang.StringUtils;

import javax.security.auth.callback.Callback;

/**
 * Used to get the remote user name of an already authenticated user.
 */
public class PSRemoteUserCallback implements Callback
{
   /**
    * Default ctor
    */
   public PSRemoteUserCallback()
   {
   }

   /**
    * Get the remote user name.
    * 
    * @return The name, <code>null</code> if none identified, never emtpy.
    */
   public String getRemoteUser()
   {
      return m_remoteUser;
   }
   
   /**
    * Set the remote username.
    * 
    * @param userName The user name, may be <code>null</code> or empty if no
    * remote user has been identified.
    */
   public void setRemoteUser(String userName)
   {
      m_remoteUser = StringUtils.isBlank(userName) ? null : userName;
   }
   
   /**
    * The remote user name, may be <code>null</code>, never empty. See 
    * {@link #getRemoteUser()}. 
    */
   private String m_remoteUser = null;
}

