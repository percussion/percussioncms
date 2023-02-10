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
package com.percussion.services.locking.data;

import com.percussion.services.locking.IPSLockErrors;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PSObjectLock} class.
 */
@Category(IntegrationTest.class)
public class PSObjectLockTest
{
   /**
    * Tests the CRUD functionality for object locks using the locking services. 
    */
   @Test
   public void testCRUD() throws Exception
   {
      IPSGuid propertyId = m_property.getGUID();
      
      IPSObjectLockService service = 
         PSObjectLockServiceLocator.getLockingService();
      
      // create a new lock
      String session = "session";
      String locker = "locker";
      List<PSObjectLock> locks = service.findLocksByUser(session, locker);
      int count = locks.size();
      PSObjectLock lock = service.createLock(propertyId, session, locker, 0, 
         false);
      
      // test find by user info
      locks = service.findLocksByUser(session, locker);
      assertTrue(locks != null && locks.size() == count+1);
      locks = service.findLocksByUser("foo", locker);
      assertTrue(locks != null && locks.size() == 0);
      locks = service.findLocksByUser(session, "bar");
      assertTrue(locks != null && locks.size() == 0);

      // try to lock the same object with different session/user without override
      try
      {
         service.createLock(propertyId, "session2", "locker2", 0, false);
         assertFalse("Should have thrown exception", false);
      }
      catch (PSLockException e)
      {
         // expected exception
         assertTrue(e.getErrorCode() == 
            IPSLockErrors.LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE);
      }

      // try to lock the same object with different session/user with override
      try
      {
         service.createLock(propertyId, "session2", "locker2", 0, true);
         assertFalse("Should have thrown exception", false);
      }
      catch (PSLockException e)
      {
         // expected exception
         assertTrue(e.getErrorCode() == 
            IPSLockErrors.LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE);
      }

      // try to lock the same object with different user without override
      try
      {
         service.createLock(propertyId, session, "locker2", 0, false);
         assertFalse("Should have thrown exception", false);
      }
      catch (PSLockException e)
      {
         // expected exception
         assertTrue(e.getErrorCode() == 
            IPSLockErrors.LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE);
      }

      // try to lock the same object with different user with override
      try
      {
         service.createLock(propertyId, session, "locker2", 0, true);
         assertFalse("Should have thrown exception", false);
      }
      catch (PSLockException e)
      {
         // expected exception
         assertTrue(e.getErrorCode() == 
            IPSLockErrors.LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE);
      }

      // try to lock the same object with different session without override
      try
      {
         service.createLock(propertyId, "session2", locker, 0, false);
         assertFalse("Should have thrown exception", false);
      }
      catch (PSLockException e)
      {
         // expected exception
         assertTrue(e.getErrorCode() == 
            IPSLockErrors.LOCK_EXTENSION_INVALID_SESSION);
      }

      // lock the same object with different session with override
      service.createLock(propertyId, "session2", locker, 0, true);
      service.createLock(propertyId, session, locker, 0, true);
      
      // find the created lock
      PSObjectLock lock2 = service.findLockByObjectId(propertyId, session, 
         locker);
      assertEquals(lock, lock2);
      
      List<IPSGuid> propertyIds = new ArrayList<IPSGuid>();
      propertyIds.add(propertyId);
      locks = service.findLocksByObjectIds(propertyIds, session, locker);
      assertEquals(1, locks.size());

      // is not in the list of expired locks
      assertFalse(service.findExpiredLocks().contains(lock));
      
      // extend the lock for an interval of 1s
      service.extendLock(propertyId, session, locker, 0, 1000);

      // make sure the lock expires
      Thread.sleep(3000);

      // is in the list of expired locks
      assertFalse(service.findExpiredLocks().contains(lock));
      
      service.releaseLock(lock);
      
      // should ignore that the lock does not exist anymore
      service.releaseLocks(Collections.singletonList(lock));
   }

   @BeforeClass
   public static void setUp() throws Exception
   {
      IPSSystemService systemService = 
         PSSystemServiceLocator.getSystemService();
      m_property = new PSSharedProperty("name", "value");
      systemService.saveSharedProperty(m_property);
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   @AfterClass
   public void tearDown() throws Exception
   {
      if (m_property != null)
      {
         IPSSystemService systemService = 
            PSSystemServiceLocator.getSystemService();
         systemService.deleteSharedProperty(m_property.getGUID());
      }
   }
   
   // test property
   static PSSharedProperty m_property = null;
}

