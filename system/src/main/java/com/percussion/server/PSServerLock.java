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

package com.percussion.server;

import java.util.Date;

/**
 * Represents a request to lock the server.  May represent either a requested
 * lock that did not succeed, or a successfully acquired lock.  
 * {@link #getLockId()} will return <code>-1</code> if this lock was not
 * successfully acquired.
 */
public class PSServerLock
{
   /**
    * Constructs a server lock.
    * 
    * @param lockId The lock id, <code>-1</code> to indicate a failed lock.
    * @param locker The owner of the lock, may not be <code>null</code> or 
    * empty.
    * @param lockedResources An array of locked resource flags, each entry is 
    * one of the <code>RESOURCE_xxx</code> flags, may not be 
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   PSServerLock(int lockId, String locker, int[] lockedResources)
   {
      if (locker == null || locker.trim().length() == 0)
         throw new IllegalArgumentException(
            "locker may not be null or empty");
      
      if (lockedResources == null || lockedResources.length == 0)
         throw new IllegalArgumentException(
            "lockedResources may not be null or empty");
      
      m_lockId = lockId;
      m_locker = locker;
      m_resources = lockedResources;
      m_lockTime = new Date();
   }
   
   /**
    * Get the id of this lock.
    * 
    * @return The id, or <code>-1</code> if this lock was not acquired.
    */
   public int getLockId()
   {
      return m_lockId;
   }
   
   /**
    * Get the name of the entity that requested this lock.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getLocker()
   {
      return m_locker;
   }
   
   /**
    * Get the resources that were requested to be locked.
    * 
    * @return An array of resource flags, each one of the 
    * <code>PSServerLockManager.RESOURCE_XXX</code> flags, never 
    * <code>null</code> or empty.
    */
   public int[] getLockedResources()
   {
      int[] result = new int[m_resources.length];
      System.arraycopy(m_resources, 0, result, 0, m_resources.length);
      return result;
   }
   
   /**
    * Determine if the specified resource was to be locked.
    * 
    * @param resource The resource to check, should be one of the 
    * <code>PSServerLockManager.RESOURCE_XXX</code> flags.
    * 
    * @return <code>true</code> if this lock was to lock the specified
    * <code>resource</code>, <code>false</code> otherwise.
    */
   public boolean isResourceLocked(int resource)
   {
      boolean isLocked = false;
      for (int i = 0; i < m_resources.length; i++) 
      {
         if (m_resources[i] == resource)
         {
            isLocked = true;
            break;
         }
      }
      
      return isLocked;
   }
   
   /**
    * Gets the requested resources Or'd together formatted in Hex.
    * 
    * @return The flags, never <code>null</code> or empty.
    */
   public String getResourceFlagString()
   {
      int flags = 0;
      for (int i = 0; i < m_resources.length; i++) 
         flags |= m_resources[i];
      
      return "0x" + Integer.toHexString(flags);
   }
   
   /**
    * Get the time at which this lock was created.
    * 
    * @return The point in time when this object was constructed, never 
    * <code>null</code>.
    */
   public Date getLockTime()
   {
      return m_lockTime;
   }
   
   /**
    * Returns string representation of this lock.  Format is 
    * [lockId]([flags]) - [locker] e.g. "25(0x1) - EnterpriseMgr:admin1"
    * @return The string, never <code>null</code> or empty.
    */
   public String toString()
   {
      return m_lockId + " (" + getResourceFlagString() + ") - " + m_locker;
   }
   
   /**
    * The id of this lock, initialized during construction, 
    * never modified after that.
    */
   private int m_lockId;
   
   /**
    * The name of the entitiy requesting this lock, initialized during 
    * construction, never <code>null</code>, empty, or modified after that.
    */
   private String m_locker;
   
   /**
    * The list of resources to be locked by this lock, initialized during 
    * construction, never <code>null</code>, empty, or modified after that.
    */
   private int[] m_resources;
   
   /**
    * The point in time when this object was created, never <code>null</code>
    * never <code>null</code> or modified after that.
    */
   private Date m_lockTime;
}

