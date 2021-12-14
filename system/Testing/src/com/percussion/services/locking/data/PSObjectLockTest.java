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

