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

import com.percussion.server.PSUserSession;
import com.percussion.server.PSUserSessionManager;

/**
 * Class that encapulates the data required to be able to check user access
 * levels at any time.
 */
public class PSSecurityToken
{
   /**
    * Constructor.
    *
    * @param session The users session.  Must already be the user's
    * authenticated entries.  May be <code>null</code> if a session has not
    * yet been created.
    */
   public PSSecurityToken(PSUserSession session)
   {
      m_session = session;
   }
   
   /**
    * Constructs a security token for the supplied session.
    * 
    * @param session the session for which to create the security token, may
    *    be <code>null</code> or empty.
    */
   public PSSecurityToken(String session)
   {
      m_session = PSUserSessionManager.getUserSession(session);      
   }

   /**
    * Returns the user's session.  This is a reference to the session passed
    * into the constructor, so that we have it even if it has expired.
    *
    * @return The session.
    */
   public PSUserSession getUserSession()
   {
      return m_session;
   }

   /**
    * Returns the user's session id.
    *
    * @return The session id.  May be <code>null</code>.
    */
   public String getUserSessionId()
   {
      String sessId = null;

      if(m_session != null)
         sessId = m_session.getId();

      return sessId;
   }

   /**
    * Sets the type of resource that is being checked.  Should be called just
    * before checking security.
    * @param resourceType A String representing the type of the resource that is
    * being accessed.  Used in exception text only, used for backward
    * compatibility with exisiting calls.  May not be <code>null</code>.
    * @throws IllegalArgumentException if resourceType is <code>null</code>.
    */
   public void setResourceType(String resourceType)
   {
      if (resourceType == null)
         throw new IllegalArgumentException("resourceType may not be null.");

      m_resourceType = resourceType;
   }

   /**
    * Sets the name of the resource that is being checked.  Should be called
    * just before checking security.
    * @param resourceName A String representing the name of the resource that is
    * being accessed.  Used in exception text only, used for backward
    * compatibility with exisiting calls.  May not be <code>null</code>.
    * @throws IllegalArgumentException if resourceType is <code>null</code>.
    */
   public void setResourceName(String resourceName)
   {
      if (resourceName == null)
         throw new IllegalArgumentException("resourceName may not be null.");

      m_resourceName = resourceName;
   }

   /**
    * Returns the type of the resource that is being checked.
    * @return The type.  May be <code>null</code>.
    */
   public String getResourceType()
   {
      return m_resourceType;
   }

   /**
    * Returns the name of the resource that is being checked.
    * @return The name.  May be <code>null</code>.
    */
   public String getResourceName()
   {
      return m_resourceName;
   }

   /**
    * Returns the current community id of this security token.
    * 
    * @return current community id, -1 if not found or error
    */
   public int getCommunityId()
   {
      String tmp = (String)m_session.getPrivateObject("sys_community");
      if (tmp == null || tmp.trim().length() == 0)
         return -1;
      
      return Integer.parseInt(tmp);
   }

   /**
    * The user's session.  Initialized in the constructor, may be
    * <code>null</code>.
    */
   private PSUserSession m_session = null;

   /**
    * The name of the resource to be checked.  Used in exception text only,
    * provided for backward compatibility.  May be <code>null</code>.
    */
   private String m_resourceName = null;

   /**
    * The type of the resource to be checked.  Used in exception text only,
    * provided for backward compatibility.  May be <code>null</code>.
    */
   private String m_resourceType = null;

}
