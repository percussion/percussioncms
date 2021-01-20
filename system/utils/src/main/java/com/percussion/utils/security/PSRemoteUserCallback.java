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
package com.percussion.utils.security;

import javax.security.auth.callback.Callback;

import org.apache.commons.lang.StringUtils;

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

