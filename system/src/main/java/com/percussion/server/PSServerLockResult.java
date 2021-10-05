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
 
package com.percussion.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the results of an attempt to lock a server resource.
 */
public class PSServerLockResult
{
   
   /**
    * Construct a failed result.
    * 
    * @param lock The requested lock, may not be <code>null</code>.  The id 
    * of the lock must be <code>-1</code>.
    * @param conflicts An list of one or more 
    * <code>PSServerLock</code> objects, each locking one or more resources
    * requested by the supplied <code>lock</code> object.  May not be 
    * <code>null</code> or empty.
    */
   PSServerLockResult(PSServerLock lock, List conflicts)
   {
      if (lock == null)
         throw new IllegalArgumentException("lock may not be null");
      
      if (lock.getLockId() != -1)
         throw new IllegalArgumentException("lock id must be -1");
      
      if (conflicts == null)
         throw new IllegalArgumentException("conflicts may not be null");
      
      if (conflicts.isEmpty())
         throw new IllegalArgumentException("conflicts may not be empty");
      
      m_lock = lock;
      m_conflicts = conflicts;
      m_lockerMap = getLockerMap(conflicts);
   }
   
   /**
    * Construct a successful result.
    * 
    * @param lock The requested lock, may not be <code>null</code>.  The id 
    * of the lock must be greater than or equal to <code>0</code>.
    */
   PSServerLockResult(PSServerLock lock)
   {
      if (lock == null)
         throw new IllegalArgumentException("lock may not be null");
      
      if (lock.getLockId() < 0)
         throw new IllegalArgumentException("lock id must be > 0");
      
      m_lock = lock;
      m_conflicts = new ArrayList();
   }
   
   /**
    * Determine whether or not the lock was successfully acquired.  If 
    * acquired, then call {@link #getLock()} to get the id of the lock, 
    * otherwise call {@link #getLockedResources()} to get the list of
    * locked resources.
    * 
    * @return <code>true</code> if the lock was acquired, <code>false</code>
    * if not.  
    */
   public boolean wasLockAcquired()
   {
      return m_lock.getLockId() != -1;
   }
   
   /**
    * Get the requested lock.
    * 
    * @return The lock, never <code>null</code>. For a failed result, the id
    * will be <code>-1</code>.
    */
   public PSServerLock getLock()
   {
      return m_lock;
   }

   /**
    * Get the list of requested resource flags that were already locked.  
    * Use {@link #getResourceLocker(int)} to determine who has it locked.
    * 
    * @return An array of locked resource flags, each entry is one of the 
    * <code>RESOURCE_xxx</code> flags.  Never <code>null</code>, will be 
    * empty if {@link #wasLockAcquired()} returns <code>true</code>.
    */
   public int[] getLockedResources()
   {
      List locks = new ArrayList();
      if (m_lockerMap != null)
      {
         int[] requested = m_lock.getLockedResources();
         for (int i = 0; i < requested.length; i++) 
         {
            Integer resource = new Integer(requested[i]);
            if (m_lockerMap.containsKey(resource))
               locks.add(resource);
         }
      }
      
      int[] locked = new int[locks.size()];
      for (int i = 0; i < locks.size(); i++) 
         locked[i] = ((Integer)locks.get(i)).intValue();
      
      return locked;
   }
   
   /**
    * Gets the description of the entity that has locked the specified
    * resource.
    * 
    * @param resource The flag indicating the resource that is locked, one of 
    * the <code>PSServerLockManager.RESOURCE_XXX</code> constants.
    * 
    * @return The locker, or <code>null</code> if this result does not
    * indicate the specified resource is locked.  Never empty.
    */
   public String getResourceLocker(int resource)
   {
      Integer key = new Integer(resource);
      String locker = null;
      PSServerLock lock = getConflictLock(resource);
      if (lock != null)
         locker = lock.getLocker();
         
      return locker;
   }
   
   /**
    * Gets the date and time that the specified resource was locked.
    * 
    * @param resource The flag indicating the resource that is locked, one of 
    * the <code>PSServerLockManager.RESOURCE_XXX</code> constants.
    * 
    * @return The date, or <code>null</code> if this result does not
    * indicate the specified resource is locked.  Never empty.
    */
   public Date getLockTime(int resource)
   {
      Integer key = new Integer(resource);
      Date locktime = null;
      PSServerLock lock = getConflictLock(resource);
      if (lock != null)
         locktime = lock.getLockTime();
      
      return locktime;
   }
   
   /**
    * Get the locks that conflict with the requested lock.
    * 
    * @return An iterator over zero or more <code>PSServerLock</code> 
    * objects.  Will only be empty if {@link #wasLockAcquired()} returns
    * <code>true</code>.
    */
   public Iterator getConflicts()
   {
      return m_conflicts.iterator();
   }

   /**
    * Creates a lock exception with a message indicating the conflicting locks.
    * May only be called if {@link #wasLockAcquired()} returns 
    * <code>false</code>.
    * 
    * @return The exception, never <code>null</code>.
    * 
    * @throws IllegalStateException if {@link #wasLockAcquired()} returns 
    * <code>true</code>.
    */
   public PSServerLockException formatLockException()
   {
      if (wasLockAcquired())
         throw new IllegalArgumentException("Lock was succesfully acquired");
      
      String resources = "";
      int[] flags = m_lock.getLockedResources();
      for (int i = 0; i < flags.length; i++) 
      {
         if (i > 0)
            resources += ", ";
         resources += PSServerLockManager.getResourceName(flags[i]);
      }
      
      
      String[] args = {resources, m_conflicts.toString()};
         
      return new PSServerLockException(IPSServerErrors.SERVER_LOCK_NOT_ACQUIRED, 
         args);
   }
   
   /**
    * Gets the conflicting lock that has the specified resource locked.
    * 
    * @param resource The flag indicating the resource that is locked, one of 
    * the <code>PSServerLockManager.RESOURCE_XXX</code> constants.
    * 
    * @return The lock, may be <code>null</code> if the specified resource is
    * not locked.
    */
   private PSServerLock getConflictLock(int resource)
   {
      PSServerLock lock = null;
      if (m_lockerMap != null)
         lock = (PSServerLock)m_lockerMap.get(new Integer(resource));
      
      return lock;
   }
   
   /**
    * Generates a map of conflicting locks by resource.
    * 
    * @param conflictList The list of conflicting locks, assumed not 
    * <code>null</code>.
    * 
    * @return A map where the key is the resource as an <code>Integer</code>
    * and the value is the <code>PSServerLock</code> object that has that
    * resource locked, never <code>null</code>, may be empty.
    */
   private Map getLockerMap(List conflictList)
   {
      Map map = new HashMap();
      Iterator conflicts = conflictList.iterator();
      while (conflicts.hasNext())
      {
         PSServerLock conflict = (PSServerLock)conflicts.next();
         int[] resources = conflict.getLockedResources();
         for (int i = 0; i < resources.length; i++) 
            map.put(new Integer(resources[i]), conflict);
      }
      return map;
   }
   
   /**
    * The object representing the requested lock.  Initialized during 
    * construction, never <code>null</code> or modified after that.
    */
   private PSServerLock m_lock;
   
   /**
    * List of conflicting <code>PSServerLock</code> objects.  Initialized during 
    * construction, never <code>null</code> after that, will be empty only if 
    * this result indicates a successful lock was acquired 
    * ({@link #wasLockAcquired()} returns <code>true</code>).
    */
   private List m_conflicts = null;
   
   /**
    * Map of locked resources, where the key is the resource as an 
    * <code>Integer</code> and the value is the <code>PSServerLock</code> object 
    * that has that resource locked.  <code>null</code> if the lock was 
    * successfully acquired, otherwise initialized during construction, and
    * not <code>null</code> or empty.  Never modified after construction.
    */
   private Map m_lockerMap = null;
}
