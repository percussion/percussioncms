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
package com.percussion.design.objectstore;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.util.Properties;

/**
 * This class is a wrapper around the object store to allow end-user programs
 * access to modify server roles. The main expected use of this interface is
 * for role synchronization (possibly on a scheduled basis). Native Rhythmyx
 * code should not use this interface, and should instead access the roles
 * directly through the object store.
 *
 * @since 4.0
 */
public class PSRoleSynchronizer
{
   /**
    * Constructor that creates a client side interface to access the server
    * roles. Properties allowed in the connection information are:
    * <table border="1">
    *    <tr>
    *       <th>Key</th>
    *       <th>Value</th>
    *       <th>Required?</th>
    *    </tr>
    *    <tr>
    *       <td>hostName</td>
    *         <td>the host name of the E2 server</td>
    *       <td>no, defaults to local machine</td>
    *    </tr>
    *    <tr>
    *       <td>port</td>
    *         <td>the port the E2 server is listening on</td>
    *       <td>no, defaults to 9992</td>
    *    </tr>
    *    <tr>
    *       <td>loginId</td>
    *         <td>the login ID to use when connecting</td>
    *       <td>no</td>
    *    </tr>
    *    <tr>
    *       <td>loginPw</td>
    *         <td>the login password to use when connecting</td>
    *       <td>no</td>
    *    </tr>
    * </table>
    *
    * @param connInfo A valid set of properties specifying how to connect to
    *    the server.
    *
    * @throws PSServerException If the server is not responding.
    *
    * @throws PSAuthorizationException   If the supplied credentials don't have
    *    server access.
    *
    * @throws PSAuthenticationFailedException If userid or password submitted
    *      is invalid.
    */
   public PSRoleSynchronizer( Properties connInfo )
      throws PSServerException, PSAuthorizationException,
             PSAuthenticationFailedException
   {
      if ( null == connInfo )
         throw new IllegalArgumentException( "properties can't be null" );
      try
      {
         if ( null == connInfo.getProperty("hostName"))
            connInfo.setProperty( "hostName", "localhost" );
         if ( null == connInfo.getProperty("port"))
            connInfo.setProperty( "port", "9992" );

         PSDesignerConnection conn = new PSDesignerConnection(connInfo);
         m_objectStore = new PSObjectStore( conn );
      }
      catch ( IllegalArgumentException e )
      {
         // should never happen because we check the param ourselves
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
   }

   /**
    * Provides all roles and related subjects, possibly for editing. If you
    * want to change the roles, you must supply <code>true</code> for the
    * <code>lock</code> flag.
    *
    * @param lock A flag to indicate that the role list is being obtained for
    *    editing. If the collection is not locked, it cannot be saved. The
    *    lock is valid for 30 minutes unless it is renewed or released. If you
    *    have the config locked in another session, that lock will be replaced
    *    with a lock for this session.
    *
    * @param overrideLock If <code>lock</code> is <code>true</code>, and the
    *    role configuration is currently locked, if this flag is <code>true
    *    </code>, then the current lock will be released and this requestor
    *    will obtain the lock. This mechanism is provided to allow a scheduled
    *    program to complete its updates even though someone may have left
    *    an administration session open. If this is going to be used, the
    *    method should always first be called with this flag set to <code>
    *    false</code>, then resent with <code>true</code> if needed, logging
    *    the override.
    *
    * @return A valid configuration containing 0 or more {@link PSRole}
    *    objects and their related PSSubjects.
    *
    * @throws PSServerException If the server is not responding
    *
    * @throws PSAuthorizationException If the user is not permitted to lock
    *    server/role configurations on the server.
    *
    * @throws PSAuthenticationFailedException If the user's session timed out
    *    and they could not be authenticated with the same credentials.
    *
    * @throws PSLockedException If another user already owns the server/role
    *    configuration lock and the lock flag was specified and the override
    *    flag wasn't.
    */
   public PSRoleConfiguration getRoleConfiguration( boolean lock,
         boolean overrideLock )
      throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSLockedException
   {
      return m_objectStore.getRoleConfiguration( lock, lock, overrideLock );
   }

   /**
    * Sends the modified roles back to the server. The role collection is
    * typically obtained with the {@link #getRoleConfiguration(boolean,
    * boolean) getRoleConfiguration} method with any changes.  If the
    * save is successful, the PSRoleConfiguration <b>must</b> be refreshed by
    * fetching from the server.  If the save is a failure, the PSRoleConfiguration
    * is still viable, as the save is a single transaction that has been rolled
    * back.
    *
    * @param roles A valid collection of roles. It must be the same collection
    *    that was obtained with the <code>getServerRoles</code> method, with
    *    any modifications.  Must refresh this collection from server after
    *    a successful save.
    *
    * @param releaseLock If <code>true</code>, the lock on the roles will be
    *    released.
    *
    * @throws PSServerException If the server is not responding
    *
    * @throws PSAuthorizationException If the user is not permitted to lock
    *    server/role configurations on the server.
    *
    * @throws PSAuthenticationFailedException If the user's session timed out
    *    and they could not be authenticated with the same credentials.
    *
    * @throws PSLockedException If another user has acquired the server/role
    *    configuration lock. This usually occurs if the server configuration
    *    was not previously locked or the lock was lost due to a timeout.
    */
   public void saveRoleConfiguration(PSRoleConfiguration roles,
                                     boolean releaseLock)
         throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSLockedException
   {
      if ( null == roles )
      {
         throw new IllegalArgumentException(
               "a valid role config must be supplied" );
      }
      m_objectStore.saveRoleConfiguration( roles, releaseLock );
   }

   /**
    * Adds N minutes to a lock already owned by the caller. If a lock
    * is not owned, an attempt is made to acquire one. Note that you cannot
    * take over a lock owned by someone else with this method. It will however
    * take over a lock owned by you in another session. The lock is released
    * if the supplied minutes is 0 or less.
    *
    * @param minutes The number of minutes to add. If 0 or less, the lock is
    *    released. If > 30, the time is limited to 30.
    *
    * @throws PSServerException If the server is not responding
    *
    * @throws PSAuthorizationException If the user is not permitted to lock
    *    server/role configurations on the server.
    *
    * @throws PSAuthenticationFailedException If the user's session timed out
    *    and they could not be authenticated with the same credentials.
    *
    * @throws PSLockedException If another user has acquired the server/role
    *    configuration lock. This usually occurs if the server configuration
    *    was not previously locked or the lock was lost due to a timeout.
    */
   public void extendLock( int minutes )
      throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSLockedException
   {
      if ( minutes < 0 )
         minutes = 0;
      else if ( minutes > 30 )
         minutes = 30;
      m_objectStore.extendServerConfigurationLock( null, minutes, true );
   }

   /**
    * A convenience method. Calls {@link #extendLock(int) extendLock(0)}. See
    * that method for a description of exceptions.
    */
   public void releaseLock()
      throws PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSLockedException
   {
      extendLock( 0 );
   }

   /**
    * This class uses a PSObjectStore to perform all of its work. It is
    * created in the ctor, never <code>null</code> after that.
    */
   private PSObjectStore m_objectStore;
}
