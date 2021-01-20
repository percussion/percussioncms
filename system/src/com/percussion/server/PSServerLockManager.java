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

package com.percussion.server;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Singleton class that will manage all server level locking.  Provides the
 * ability to create locks that encompass one or more subsystems.  This class
 * will create and manage locks, but will not perform any enforcement.  It is up 
 * to the caller to attempt to aquire the appropriate locks at the required
 * times.
 */
public class PSServerLockManager
{
   /**
    * Private ctor to enforce singleton pattern.
    */
   private PSServerLockManager()
   {
      m_locks = new HashMap();
      m_dateFormat = new SimpleDateFormat("yyyyMMdd' 'HH':'mm':'ss");
   }
   
   /**
    * Package private to restrict creation of the singleton instance.  This 
    * method may only be called once per process that retains a reference to
    * the returned instance.  Caller should retain a reference to the returned
    * instance to prevent garbage collection.
    * 
    * @return The lock manager, never <code>null</code>.
    * 
    * @throws IllegalStateException if the instance has already been created.
    */
   static synchronized PSServerLockManager createInstance()
   {
      if (ms_lockMgr != null)
         throw new IllegalStateException("Server lock manager already created");
      
      ms_lockMgr = new PSServerLockManager();
      
      return ms_lockMgr;
   }
   
   /**
    * Gets the singleton instance of this class.  
    * 
    * @return The manager, never <code>null</code>.
    * 
    * @throws IllegalStateException if the instance has not yet been created.
    */
   public static synchronized PSServerLockManager getInstance()
   {
      if (ms_lockMgr == null)
         throw new IllegalStateException("Server lock manager not yet created");
      
      return ms_lockMgr;
   }
   
   /**
    * Attempts to aquire a lock on all resources defined by the supplied flags.
    * 
    * @param flags One or more of the <code>RESOURCE_xxx</code> flags Or'd 
    * together to specify which resource(s) to lock.  All resources must be 
    * available (unlocked) for the lock to succeed.  
    * 
    * @param locker A description of the entity attempting to acquire the lock.
    * This should be as unique as possible, including perhaps the subsystem and
    * if applicable, the username on behalf of which the lock is to be acquired.
    * May not be <code>null</code> or empty.
    * 
    * @return The result of the attempt, will indicate if the attempt was 
    * successful or not, and if not, why not.  Call 
    * {@link PSServerLockResult#wasLockAcquired()} to determine if the attempt 
    * was successful.
    */
   public synchronized PSServerLockResult acquireLock(int flags, String locker)
   {
      PSServerLockResult result;
      List conflicts = new ArrayList();
      List lockedResourceList = new ArrayList();
      Iterator resources = ms_resourceMap.keySet().iterator();
      while (resources.hasNext())
      {
         int flag = ((Integer)resources.next()).intValue();
         if ((flag & flags) == flag)
         {
            PSServerLock lock = getCurrentLock(flag);
            if (lock != null)
               conflicts.add(lock);
            lockedResourceList.add(new Integer(flag));
         }
      }

      // create locked resource array 
      int[] lockedResources = new int[lockedResourceList.size()];
      for (int i = 0; i < lockedResources.length; i++) 
         lockedResources[i] = ((Integer)lockedResourceList.get(i)).intValue();
      
      int lockId = conflicts.isEmpty() ? m_nextLockId++ : -1;
      PSServerLock lock = new PSServerLock(lockId, locker, lockedResources);
         
      if (conflicts.isEmpty())
      {
         m_locks.put(new Integer(lockId), lock);
         result = new PSServerLockResult(lock);
      }
      else
         result = new PSServerLockResult(lock, conflicts);
         
      return result;
   }
   
   /**
    * Releases the lock held by the supplied lock id.
    * 
    * @return <code>true</code> if the lock is released, <code>false</code> if
    * the lock is no longer in effect.  This may happen if the server has been
    * restarted or if the lock has already been released.
    */
   public synchronized boolean releaseLock(int lockId)
   {
      return m_locks.remove(new Integer(lockId)) != null;
   }
   
   /**
    * Determine if the specified resource is currently locked.
    * @param resource The resource to check, must be one of the 
    * <code>RESOURCE_xxx</code> flags.
    * 
    * @return <code>true</code> if the resource is locked, <code>false</code>
    * otherwise.
    */
   public boolean isLocked(int resource)
   {
      return getCurrentLock(resource) != null;
   }
   
   /**
    * Get the object representing the lock on the specified resource.
    * 
    * @param resource One of the <code>RESOURCE_xxx</code> flags specifying a
    * resource for which the current lock is to be returned.  
    * 
    * @return The lock object which has the specified resource locked, or 
    * <code>null</code> if the resource is not locked.
    * 
    * @throws IllegalArgumentException if <code>resource</code> is not one of 
    * the <code>RESOURCE_xxx</code> flags.
    */
   public PSServerLock getCurrentLock(int resource)
   {
      if (!ms_resourceMap.containsKey(new Integer(resource)))
         throw new IllegalArgumentException("invalid lock resource: " + 
            resource);
      
      PSServerLock lock = null;
      Iterator locks = getAllLocks();
      while (locks.hasNext() && lock == null)
      {
         PSServerLock test = (PSServerLock)locks.next();
         if (test.isResourceLocked(resource))
            lock = test;
      }
      
      return lock;
   }
   
   /**
    * Gets all current locks.
    * 
    * @return An iterator over zero or more <code>PSServerLock</code> objects,
    * never <code>null</code>.  Iterator is over a snapshot of the current list
    * of locks, so that it may be used by the caller while locks coninue to be
    * acquired and released.
    */
   public synchronized Iterator getAllLocks()
   {
      // copy list so caller can traverse without concurrent modification 
      // exceptions
      List locks = new ArrayList(m_locks.values());
      return locks.iterator();
   }
   
   /**
    * Gets the status of all current locks.
    * 
    * @param doc The document to which the returned element will be appended.
    * 
    * @return An element with the following format:
    * <pre><code>
    * &lt;!ELEMENT ServerLocks (Lock*)>
    * &lt;!ELEMENT Lock (Resources*)>
    * &lt;!ATTLIST Lock (id, locker, created)>
    * &lt;!ELEMENT Resources (#PCDATA)>
    * </code></pre>
    */
   public Element getServerLockStatus(Document doc)
   {
      Element serverLocks = doc.createElement("ServerLocks");
      
      // use getAllLocks to get point in time copy of list of locks
      Iterator locks = getAllLocks();
      while (locks.hasNext())
      {
         PSServerLock lock = (PSServerLock)locks.next();
         Element lockEl = PSXmlDocumentBuilder.addEmptyElement(doc, serverLocks, 
            "Lock");
         lockEl.setAttribute("id", String.valueOf(lock.getLockId()));
         lockEl.setAttribute("locker", lock.getLocker());
         lockEl.setAttribute("created", m_dateFormat.format(
            lock.getLockTime()));
         
         Element resourcesEl = PSXmlDocumentBuilder.addEmptyElement(doc, lockEl, 
            "Resources");
         int[] resources = lock.getLockedResources();
         for (int i = 0; i < resources.length; i++) 
         {
            PSXmlDocumentBuilder.addElement(doc, resourcesEl, "Resource", 
               getResourceName(resources[i]));
         }
      }
      
      return serverLocks;
   }
   
   /**
    * Get the display name of the specified resource.
    * 
    * @param resource The resource, one of the <code>RESOURCE_xxx</code>
    * flags.
    * 
    * @return The name, never <code>null</code> or empty.  If the name of the 
    * specified resource cannot be located, the supplied <code>resource</code>
    * value is returned as a <code>String</code>.
    */
   public static String getResourceName(int resource)
   {
      String key = (String)ms_resourceMap.get(new Integer(resource));
      if (key != null)
      {
         try
         {
            key = PSServer.getRes().getString(RESOURCE_NAME_PREFIX + key);
         }
         catch (MissingResourceException e)
         {
            // at least return the key
         }
      }
      
      return key;
   }
   
   /**
    * The next lock id, incremented each time it is used to set a lock.
    */
   private int m_nextLockId = 0;
   
   /**
    * Map of current locks where the key is the lock id as an 
    * <code>Integer</code> and value is the <code>PSServerLock</code> object.
    * Initialized during construction, never <code>null</code> after that. 
    * Locks are added by <code>acquireLock()</code> and removed by
    * <code>releaseLock()</code>.
    */
   private Map m_locks;
   
   /**
    * Date format used to format status messages.  Initialized during 
    * construction, never <code>null</code> or modified after that.
    */
   private SimpleDateFormat m_dateFormat;
   
   /**
    * The singleton instance of the lock manager, <code>null</code> until
    * a call to {@link #createInstance()}, never <code>null</code> after
    * that.
    */
   private static PSServerLockManager ms_lockMgr = null;
   
   /**
    * Flag to represent the publisher.
    */
   public static final int RESOURCE_PUBLISHER = 0x1;
   
   /**
    * Map of resource flags to display names.  Key is the flag as an 
    * <code>Integer</code> object, and value is the key used to retrieve the 
    * display name from the server's resource bundle,  prepending 
    * {@link #RESOURCE_NAME_PREFIX} to create the actual key.  Initialized and
    * entries are added in the static initializer, which must be maintained as
    * new resource flags are added.
    */
   private static Map ms_resourceMap;
   
   /**
    * Prefix prepended onto resource keys to ensure uniqueness in the server's
    * resource bundle.
    */
   private static final String RESOURCE_NAME_PREFIX = "serverLockManager_";
   
   static
   {
      ms_resourceMap = new HashMap(1);
      ms_resourceMap.put(new Integer(RESOURCE_PUBLISHER), "publisher");
   }
   
}