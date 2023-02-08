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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSServerLockManagerTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSServerLockManagerTest(String name)
   {
      super(name);
   }
   
   /**
    * Tests all lock mgr functionality
    * 
    * @throws Exception if there are any errors.
    */
   public void testAll() throws Exception
   {
      boolean didThrow = false;
      
      // test get before create
      try 
      {
         PSServerLockManager.getInstance();
      }
      catch (IllegalStateException e) 
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      // now create
      PSServerLockManager lockMgr = PSServerLockManager.createInstance();
      assertNotNull(lockMgr);
      
      lockMgr = PSServerLockManager.getInstance();
      assertNotNull(lockMgr);
      
      // test 2nd create
      didThrow = false;
      try 
      {
         PSServerLockManager.createInstance();
      }
      catch (IllegalStateException e) 
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      // test acquire
      assertTrue(!lockMgr.isLocked(PSServerLockManager.RESOURCE_PUBLISHER));
      PSServerLockResult result = lockMgr.acquireLock(
         PSServerLockManager.RESOURCE_PUBLISHER, "testLocker");

      assertNotNull(result);
      PSServerLock lock = result.getLock();
      assertNotNull(lock);
      int[] lockedResources = lock.getLockedResources();
      for (int i = 0; i < lockedResources.length; i++) 
      {
         System.err.println("locked: " + lockedResources[i]);
      }
      
      assertTrue(result.wasLockAcquired());
      assertTrue(lock.getLockId() != -1);
      assertTrue(lock.getLocker().equals("testLocker"));
      assertTrue(!result.getConflicts().hasNext());
      assertTrue(lock.isResourceLocked(
         PSServerLockManager.RESOURCE_PUBLISHER));
      assertTrue(lockMgr.isLocked(PSServerLockManager.RESOURCE_PUBLISHER));
      
      // test failed acquire
      PSServerLockResult result2 = lockMgr.acquireLock(
         PSServerLockManager.RESOURCE_PUBLISHER, "testLocker2");
      
      assertNotNull(result2);
      PSServerLock lock2 = result2.getLock();
      assertNotNull(lock2);
      assertTrue(!result2.wasLockAcquired());
      assertTrue(lock2.getLockId() == -1);
      assertTrue(lock2.getLocker().equals("testLocker2"));
      assertTrue(result2.getConflicts().hasNext());
      PSServerLock conflict = 
         (PSServerLock)result2.getConflicts().next();
      assertTrue(conflict.getLockId() == result.getLock().getLockId());
      assertTrue(conflict.isResourceLocked(
         PSServerLockManager.RESOURCE_PUBLISHER));
      
      // test release
      assertTrue(!lockMgr.releaseLock(lock2.getLockId()));
      assertTrue(lockMgr.releaseLock(lock.getLockId()));
      assertTrue(lockMgr.acquireLock(PSServerLockManager.RESOURCE_PUBLISHER, 
         "testLocker2").wasLockAcquired());
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSServerLockManagerTest("testAll"));
      return suite;
   }
   
}
