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

package com.percussion.design.objectstore.server;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSLockedException;

import java.util.Properties;

/**
 * Used by the PSXmlObjectStoreHandler class. Basically stores session,
 * user name, and whether or not to override locks held by the same
 * user under a different session.
 */
public class PSXmlObjectStoreLockerId implements IPSLockerId
{
   public PSXmlObjectStoreLockerId(Properties props)
   {
      m_overrideSameUser = false;
      readFrom(props);
   }

   /**
    * Creates a locker id that can optionally be used to override an existing
    * lock owned by this user in another session.
    *
    * @param userName The name of the user.
    *
    * @param overrideSameUser A flag to indicate whether this user can
    *    acquire an existing lock if it is locked by the same user in a
    *    different session.
    *
    * @param The unique identifier for the session.
    */
   public PSXmlObjectStoreLockerId(
      String userName,
      boolean overrideSameUser,
      String sessionId)
   {
      m_userName = userName;
      m_sessionId = sessionId;
      m_overrideSameUser = overrideSameUser;
   }

   /**
    * A constructor that allows this id to optionally override any existing
    * lock. See {@link #PSXmlObjectStoreLockerId(String,boolean,String) other
    * ctor} for details not described below. Must be used with great
    * discretion.
    *
    * @param overrideDifferentUser If <code>true</code>, when this user
    *    attempts to acquire a lock, they will acquire it no matter who
    *    currently owns the lock.
    */
   public PSXmlObjectStoreLockerId(
      String userName, 
      boolean overrideSameUser,
      boolean overrideDifferentUser,
      String sessionId)
   {
      this( userName, overrideSameUser, sessionId );
      m_overrideDifferentUser = overrideDifferentUser;
   }

   /**
    * Reads all uniquely identifying properties from the given properties
    * object.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/6
    * 
    * 
    * @param   props
    * 
    */
   public void readFrom(Properties props)
   {
      m_userName = props.getProperty("locker");
      m_sessionId = props.getProperty("sessionId");
   }

   /**
    * Writes all uniquely identifying properties to the given properties
    * object.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/6
    * 
    * 
    * @param   props
    * 
    */
   public void writeTo(Properties props)
   {
      if (m_userName != null)
         props.setProperty("locker", m_userName);

      if (m_sessionId != null)
         props.setProperty("sessionId", m_sessionId);
   }

   /**
    * Returns <CODE>true</CODE> if this locker id is the same
    * as the given locker id. This may or may not be consistent
    * with the equals method for this object (for example, under
    * some situations, the implementation is free to treat two
    * distinct ids as the same id).
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/6
    * 
    * @param   other The other id
    *
    * @param   ex An exception to be filled out explaining that
    * the resource is locked by this locked id (not other), and
    * any other applicable error messages. May be <CODE>null</CODE>,
    * in which case no reporting will be done.
    * 
    * @return   boolean
    */
   public boolean sameId(IPSLockerId other, PSLockedException ex)
   {
      if (!(other instanceof PSXmlObjectStoreLockerId))
         return false;

      PSXmlObjectStoreLockerId o = (PSXmlObjectStoreLockerId)other;

      boolean sameSession = (m_sessionId != null && o.m_sessionId != null
         && m_sessionId.equals(o.m_sessionId));
   
      boolean sameUser = (m_userName != null && o.m_userName != null
         && m_userName.equals(o.m_userName));
   
      if (sameUser)
      {
         if (sameSession || m_overrideSameUser || o.m_overrideSameUser)
            return true;
         else
         {
            if (ex != null)
            {
               // same user, different session
               ex.setErrorCode(IPSObjectStoreErrors.LOCK_ALREADY_HELD_SAME_USER);
            }
         }
      }
      else
      {
         if (ex != null)
         {
            // different user
            ex.setErrorCode(IPSObjectStoreErrors.LOCK_ALREADY_HELD);
         }
      }

      if (ex != null)
      {
         ex.setLockingUser(m_userName);
         ex.setLockingSession(m_sessionId);
      }

      return false;
   }

   
   // see IPSLockerId
   public boolean isOverrideDifferentUser()
   {
      return m_overrideDifferentUser;
   }
   
   /**
    * Get the name of the locker
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getUserName()
   {
      return m_userName;
   }
   
   /**
    * Get the session id of the locker
    * 
    * @return The id, never <code>null</code> or empty.
    */
   public String getSessionId()
   {
      return m_sessionId;
   }
   
   private String m_userName;
   private String m_sessionId;
 
   /**
    * A flag that indicates this id should obtain the lock from an id of
    * the same user in a different session.
    */
   private boolean m_overrideSameUser;

   /**
    * A flag that indicates this id should obtain the lock no matter who
    * currently has it locked. It should be used with great discretion.
    * Defaults to <code>false</code>.
    */
   private boolean m_overrideDifferentUser = false;
}
