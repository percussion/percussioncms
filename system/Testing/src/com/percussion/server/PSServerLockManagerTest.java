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