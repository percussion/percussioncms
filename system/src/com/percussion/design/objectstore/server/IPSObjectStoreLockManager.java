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

import com.percussion.design.objectstore.PSLockedException;
import com.percussion.error.PSIllegalArgumentException;

/**
 * Provides an interface for an object-store's locking services.
 */
public interface IPSObjectStoreLockManager
{
   /** no other locks of any kind can be shared with an exclusive lock */
   public static final int LOCKTYPE_EXCLUSIVE = 0;

   /**
    * Returns the uniquely identifying lock key for this object under
    * the given type of lock, throwing an exception if no lock key
    * can be found for this object.
    *
    * @author  chad loder
    * 
    * @version 1.0 1999/6/18
    * 
    * @param   lockOb The object whose lock key you want to obtain.
    *
    * @param   lockType   The type of lock requested, or 0
    * if all applicable access types are requested at once.
    * 
    * @return  Object The lock key for this object. Keys should be
    * lightweight identifier objects that identify a lock. Keys do not
    * act as a lock; synchronizing on (or finalization of) a key
    * will not interfere with the lock nor with any other keys.
    * 
    * @throws  PSIllegalArgumentException If no lock key can be associated with
    * this object. This may happen if this is not a lockable object, or
    * if the object store is not responsive.
    * 
    */
   public Object getLockKey(Object lockOb, int lockType)
      throws PSIllegalArgumentException;

   /**
    * Returns the uniquely identifying lock key for this object under
    * the given type of lock, throwing an exception if no lock key
    * can be found for this object or if the lock type is not
    * supported.
    *
    * @author  chad loder
    * 
    * @version 1.0 1999/6/18
    * 
    * @param   lockObClass The class of the object whose lock key you
    * want to obtain.
    *
    * @param   unique   A unique indentifier for the object, which
    * may be either the object itself or a String or other identifier,
    * depending on the implementation.
    * 
    * @return  Object The lock key for this object. Keys should be
    * lightweight identifier objects that identify a lock. Keys do not
    * act as a lock; synchronizing on (or finalization of) a key
    * will not interfere with the lock nor with any other keys.
    * 
    * @throws  PSIllegalArgumentexception If no lock key can be associated with
    * this object. This may happen if this is not a lockable object, or
    * if the object store is not responsive.
    * 
    */
   public Object getLockKey(Class lockObClass, Object unique, int lockType)
      throws PSIllegalArgumentException;

   /**
    * Acquires a lock on the object referenced by the given key,
    * which must have been obtained by calling getLockKey for the
    * object you want to lock. If the locker already has a lock,
    * then this method should succeed. If the locker already has
    * a lock and the value of the <CODE>lockExpiresMs</CODE>
    * parameter would cause the lock to expire sooner than it
    * was going to expire before this method was called, then this
    * method should not change the expiration.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/18
    * 
    * @param   lockerId   An object which uniquely identifies the locker.
    * The value should be unique from other IDs.
    *
    * @param   lockKey The lock key, which must have been obtained
    * by calling {@link #getLockKey getLockKey}.
    *
    * @param   lockExpiresMs The period (starting from when the lock
    * is acquired) after which the manager is free
    * release the lock on the session's behalf and grant locks to other
    * sessions that will exclude locks of this type. This parameter
    * should not be construed as a guarantee that the lock will be
    * released in the given amount of time, although implementations
    * should make a best effort to do so.
    *
    * @param   waitTimeoutMs If the lock cannot be acquired immediately,
    * this call will block for approximately the specified number of
    * milliseconds, retrying the acquire at periodic intervals. If this
    * parameter is 0, then this method will return false immediately if
    * the lock cannot be obtained. A value of < 0 means that this
    * call will block until the lock is acquired.
    * 
    * @param   lockResults The manager will fill out this results object
    * with detailed information about the lock acquisition attempt if
    * <CODE>lockResults</CODE> is not <CODE>null</CODE>. If it is
    * <CODE>null</CODE>, no detailed information will be filled out.
    *
    * @return   boolean <CODE>true</CODE>if a lock of the specified type
    * was acquired, <CODE>false</CODE> if not. Note that this method will
    * return true whenever the user session has the lock by the time the
    * method returns, even if the lock was not acquired as a direct consequence
    * of calling this method (for example, if this user session already has
    * the lock of the requested type on the object referenced by the lock key).
    * 
    * @throws   PSLockAcquisitionException
    *
    * @throws   PSIllegalArgumentException   
    */
   public boolean acquireLock(
      IPSLockerId lockerId,
      Object lockKey,
      long lockExpiresMs,
      long waitTimeoutMs,
      PSLockedException lockedResults)
      throws PSLockAcquisitionException, PSIllegalArgumentException;

   /**
    * Releases the lock associated with the given key.
    * <P>IF there is no lock
    * associated with <CODE>lockKey</CODE>
    * <P>OR if there is a lock associated with
    * <CODE>lockKey</CODE> but it does not belong to
    * <CODE>lockerId</CODE>
    * <P>OR if the lock key is of the wrong type
    * <P>THEN nothing will happen and the operation will
    * appear to complete successfully.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/18
    *
    * @param   lockerId   An object which uniquely identifies the
    * user requesting lock release. The lock will only be released
    * if this lockerId is the same id as the original locker.
    * 
    * @param   lockKey The lock key, which must have been obtained
    * by calling {@link #getLockKey getLockKey}.
    */
   public void releaseLock(IPSLockerId lockerId, Object lockKey);

}
