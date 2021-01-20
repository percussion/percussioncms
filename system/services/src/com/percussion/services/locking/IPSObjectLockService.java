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
package com.percussion.services.locking;

import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;

import java.util.List;

/**
 * This interface specifies all services used to manage object locks 
 * represented through the <code>PSObjectLock</code> object type.
 */
public interface IPSObjectLockService
{
   /**
    * Create a new lock or extend an existing one for the requested object(s).
    * 
    * @param ids the id of the object to lock, never <code>null</code> or empty
    * and no <code>null</code> entries.
    * @param session the session of the lock holder, not <code>null</code> or
    * empty.
    * @param locker the name of the lock holder, not <code>null</code> or
    * empty.
    * @param versions the version of the locked object for each entry in the
    * <code>ids</code> list, never <code>null</code> but entries can be
    * <code>null</code> if the locked object does not use a version.
    * @param overrideLock <code>true</code> to override existing locks for the
    * same user but different session.
    * @return the new or extended object lock, never <code>null</code>. The
    * order of the results is the same as the supplied ids.
    * 
    * @throws PSLockException if any of the requested locks could not be created
    * for any reason. In this case, use the {@link PSLockException#getResults()}
    * and {@link PSLockException#getErrors()} methods to retrieve any successes
    * and the exception for each failure.
    */
   public List<PSObjectLock> createLocks(List<IPSGuid> ids,
         String session, String locker, List<Integer> versions,
         boolean overrideLock) throws PSLockException;
   
   /**
    * Find the lock for the supplied object id.
    * 
    * @param id the id of the object for which to find the lock, not
    *    <code>null</code> or empty.
    * @return the requested lock if found, <code>null</code> otherwise.
    */
   public PSObjectLock findLockByObjectId(IPSGuid id);
   
   /**
    * Find the lock for the supplied object id, session and locker.
    * 
    * @param id the id of the object for which to find the lock, not
    *    <code>null</code> or empty.
    * @param lockSession the session for which to find the locks, may be
    *    <code>null</code> or empty.
    * @param locker the user for which to find the locks, may be 
    *    <code>null</code> or empty.
    * @return the requested lock if found, <code>null</code> otherwise.
    */
   public PSObjectLock findLockByObjectId(IPSGuid id, String lockSession, 
      String locker);
   
   /**
    * Find all locks for the supplied object ids.
    * 
    * @param ids the ids of all objects for which to find the locks, not
    *    <code>null</code> or empty.
    * @param lockSession the session for which to find the locks, may be
    *    <code>null</code> or empty.
    * @param locker the user for which to find the locks, may be 
    *    <code>null</code> or empty.
    * @return a list with all locks found for the supplied object ids,
    *    never <code>null</code>, may be empty. The returned list may be
    *    shorter than the supplied list. The order of the returned list does not
    *    necessarily match that of the supplied list.
    */
   public List<PSObjectLock> findLocksByObjectIds(List<IPSGuid> ids, 
      String lockSession, String locker);
   
   /**
    * Find all locks for the supplied session and locker.
    * 
    * @param lockSession the session for which to find all locks, not
    *    <code>null</code> or empty.
    * @param locker the user for which to find all locks, not
    *    <code>null</code> or empty.
    * @return a list with all found locks for the supplied session and locker,
    *    never <code>null</code>, may be empty.
    */
   public List<PSObjectLock> findLocksByUser(String lockSession, String locker);
   
   /**
    * Load all locks for the supplied ids.
    * 
    * @param ids the ids of all locks to load, not <code>null</code> or empty.
    * @return a list with all locks for the supplied ids, never 
    *    <code>null</code> or empty.
    */
   public List<PSObjectLock> loadLocksByIds(List<IPSGuid> ids);
   
   /**
    * Find all locks that are expired.
    * 
    * @return a list with all locks that are expired, never <code>null</code>,
    *    may be empty.
    */
   public List<PSObjectLock> findExpiredLocks();
   
   /**
    * Convenience method that wraps the <code>id</code> and <code>version</code>
    * in a list and calls
    * {@link #createLocks(List, String, String, List, boolean)}.
    */
   public PSObjectLock createLock(IPSGuid id, String session, String locker, 
      Integer version, boolean overrideLock) throws PSLockException;

   /**
    * Locks or extends the locks for all results for the supplied session 
    * and user. For any lock error the errors and results will be updated in 
    * the provided exception.
    * 
    * @param results the exception which contains all results to be locked, 
    *    not <code>null</code>. For all locks that fail, an error will be added
    *    and the results will be updated.
    * @param session the session for which to lock the results, not 
    *    <code>null</code> or empty.
    * @param user the user for which to lock the results, not <code>null</code>
    *    or empty.
    * @param overrideLock <code>true</code> to override existing locks for 
    *    the same user but different session.
    */
   public void createLocks(PSErrorResultsException results, String session, 
      String user, boolean overrideLock);
   
   /**
    * Convenience method to extend the lock for the default interval time, 
    * calls {@link #extendLock(IPSGuid, String, String, Integer, long) 
    * extendLock(id, session, locker, version, PSObjectLock.LOCK_INTERVAL)}.
    */
   public PSObjectLock extendLock(IPSGuid id, String session, String locker, 
      Integer version) throws PSLockException;

   /**
    * Convenience method that wraps the guid and version in a list and calls
    * {@link #extendLocks(List, String, String, List, long)}.
    */
   public PSObjectLock extendLock(IPSGuid id, String session, String locker, 
      Integer version, long interval) throws PSLockException;
   
   /**
    * Extend the locks associated with the supplied ids for the current locker
    * for specified time.
    * 
    * @param ids the ids of the design object to extend the lock for, not
    * <code>null</code> or empty.
    * @param session the session of the lock holder, not <code>null</code> or
    * empty.
    * @param locker the name of the lock holder, not <code>null</code> or
    * empty.
    * @param versions the new versions of the locked object. Supply
    * <code>null</code> entries to leave the locked version untouched, otherwise
    * provide the new version of the object to extend the lock for. Never
    * <code>null</code>, must have an entry for every entry in <code>ids</code>.
    * @param interval specifies the time in milliseconds how long the requested
    * lock will be held. Must be minimum 1000 ms.
    * @return the extended object locks, never <code>null</code>.
    * @throws PSLockException if the requested lock does not exist or could not
    * be extended for any reason for any of the <code>ids</code>. This is the
    * multi operation version of the exception. It includes all locks that were
    * extended and an exception for each one that couldn't be.
    */
   public List<PSObjectLock> extendLocks(List<IPSGuid> ids,
         String session, String locker, List<Integer> versions, long interval)
      throws PSLockException;
   
   /**
    * Release the supplied lock. We will ignore the case when the supplied 
    * lock does not exist. 
    * 
    * @param lock the lock to be released, may be <code>null</code>.
    */
   public void releaseLock(PSObjectLock lock);
   
   /**
    * Release the locks for all supplied locks. We will ignore the case where
    * any of the supplied locks does not exist.
    * 
    * @param locks all locks to be released, may be <code>null</code> or
    *    empty.
    */
   public void releaseLocks(List<PSObjectLock> locks);
   
   /**
    * Is the object referenced through the supplied id locked for the
    * provided user and session?
    * 
    * @param id the id of the design object to test, not <code>null</code>.
    * @param session the session to test for, not <code>null</code> or empty.
    * @param locker the locker to test for, not <code>null</code> or empty.
    * @return <code>true</code> if the object referenced by the supplied id
    *    is locked for the supplied user and session, <code>false</code>
    *    otherwise.
    */
   public boolean isLockedFor(IPSGuid id, String session, String locker);
   
   /**
    * Get the version of the locked design object.
    * 
    * @param id the id of the locked object for which to get the locked version,
    *    not <code>null</code>.
    * @return the version of the locked object, mey be <code>null</code> or the
    *    design object does not support a version.
    * @throws PSLockException if no lock exists for the supplied id.
    */
   public Integer getLockedVersion(IPSGuid id) throws PSLockException;
}
