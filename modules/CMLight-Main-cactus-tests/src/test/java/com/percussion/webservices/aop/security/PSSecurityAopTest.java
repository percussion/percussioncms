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
package com.percussion.webservices.aop.security;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.security.*;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.aop.security.data.PSMockDesignObject;
import com.percussion.webservices.aop.security.strategy.PSSecurityStrategy;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test case for the AOP implementation used to enforce web service security.
 * Uses Mock service interfaces to test that the correct behavior is implemented
 * by the various classes extending {@link PSSecurityStrategy}.  
 */
@Category(IntegrationTest.class)
public class PSSecurityAopTest extends ServletTestCase
{
   /**
    * Get the test acls created during {@link #setUp()}.
    * 
    * @return The list, not <code>null</code> or empty during test execution.
    */
   public static List<IPSAcl> getTestAcls()
   {
      return ms_testAcls;
   }
   
   /**
    * Test enforcement of various public load signatures
    * 
    * @throws Exception if the test fails
    */
   public void testPublicLoad() throws Exception
   {
      IPSSecurityAopTestImplWs mgr = getPublicService();
      
      // test as admin1
      login("admin1", "demo");
      assertNotNull(mgr.loadDesignObject());

      List<PSMockDesignObject> result = mgr.loadDesignObjects("test");
      assertNotNull(result);
      assertTrue(result.size() == 2);
      
      // test as admin2
      login("admin2", "demo");
      mgr.loadDesignObject();
      
      result = mgr.loadDesignObjects("test");
      assertNotNull(result);
      assertTrue(result.size() == 2);
      
      // test as edtior
      login("editor1", "demo");
      assertNotNull(mgr.loadDesignObject());
      
      result = mgr.loadDesignObjects("test");
      assertNotNull(result);
      assertTrue(result.size() == 2);
   }

   
   /**
    * Test enforcement of various public load signatures
    * 
    * @throws Exception if the test fails
    */
   public void testDesignLoad() throws Exception
   {
      // test as admin1
      String user = "admin1";
      String sessionId = login(user, "demo");
      doDesignLoadTest(user, sessionId, 2, 1, false, true);

      user = "admin2";
      sessionId = login(user, "demo");
      doDesignLoadTest(user, sessionId, 1, 1, true, true);

      user = "editor1";
      sessionId = login(user, "demo");
      doDesignLoadTest(user, sessionId, 1, 1, false, false);

      user = "author1";
      sessionId = login(user, "demo");
      doDesignLoadTest(user, sessionId, 0, 0, true, true);
   }
   
   /**
    * Test public find methods
    * 
    * @throws Exception if the test fails.
    */
   public void testPublicFind() throws Exception
   {
      IPSSecurityAopTestImplWs mgr = getPublicService();
      List<PSMockDesignObject> objList;
      
      // test as editor1
      login("editor1", "demo");
      
      try
      {
         objList = mgr.findPublicObjects(null);
         assertFalse("should have thrown", true);
      }
      catch(RuntimeException e)
      {
         // expected
      }
      
      // should be unfiltered
      objList = mgr.findPublicObjects("test");
      assertNotNull(objList);
      assertEquals(objList.size(), 2);
   }

   /**
    * Test design find methods
    * 
    * @throws Exception if the test fails.
    */
   public void testDesignFind() throws Exception
   {
      IPSSecurityAopTestImplDesignWs mgr = getDesignService();
      
      login("admin1", "demo");
      
      try
      {
         mgr.findDesignObjects(null);
         assertFalse("should have thrown", true);
      }
      catch(RuntimeException e)
      {
         // expected
      }
      
      validateFindResults(mgr.findDesignObjects("test"), 2, 
         new PSPermissions[] {PSPermissions.READ});
      
      login("editor1", "demo");
      validateFindResults(mgr.findDesignObjects("test"), 1, 
         new PSPermissions[] {PSPermissions.READ, PSPermissions.UPDATE, 
         PSPermissions.DELETE});

      login("editor1", "demo");
      validateFindResults(mgr.findDesignObjects("test"), 1, 
         new PSPermissions[] {PSPermissions.READ, PSPermissions.UPDATE, 
         PSPermissions.DELETE});
   }
   
   /**
    * Test public save methods
    * 
    * @throws Exception if the test fails
    */
   public void testPublicSave() throws Exception
   {
      IPSSecurityAopTestImplWs mgr = getPublicService();
      login("admin1", "demo");
      
      // test saving null value to ensure no exception is thrown.
      try
      {
         mgr.savePublicObjects(null);
      }
      catch (RuntimeException e)
      {
         assertFalse("should not have thrown", true);
      }
      
      // test saving an object w/no guid and ensure no exception is thrown
      try
      {
         mgr.savePublicObjects("foo");
      }
      catch (RuntimeException e)
      {
         assertFalse("should not have thrown", true);
      }
   }
   
   /**
    * Test public delete methods
    * 
    * @throws Exception if the test fails
    */
   public void testPublicDelete() throws Exception
   {
      IPSSecurityAopTestImplWs mgr = getPublicService();
      login("admin1", "demo");
      
      // test saving null value to ensure no exception is thrown.
      try
      {
         mgr.deletePublicObjects(null);
      }
      catch (RuntimeException e)
      {
         assertFalse("should not have thrown", true);
      }
      
      // test saving an object w/no guid and ensure no exception is thrown
      try
      {
         mgr.deletePublicObjects("foo");
      }
      catch (RuntimeException e)
      {
         assertFalse("should not have thrown", true);
      }
   }   
   
   /**
    * Test enforcement of delete signatures.
    * 
    * @throws Exception if the test fails.
    */
   public void testDesignDelete() throws Exception
   {
      IPSSecurityAopTestImplDesignWs mgr = getDesignService();
      String sessionId = login("admin1", "demo");
 
      // ensure no guid throws error
      try
      {
         mgr.deleteDesignObject("foo", sessionId);
         assertFalse("should have thrown", true);
      }
      catch (RuntimeException e)
      {
         
      }
      
      doDesignDeleteTest(mgr, -1, sessionId);
      
      sessionId = login("editor1", "demo");
      doDesignDeleteTest(mgr, 0, sessionId);
      
      sessionId = login("admin2", "demo");
      doDesignDeleteTest(mgr, 1, sessionId);
   }
   
   /**
    * Test annotation handling
    * 
    * @throws Exception if the test fails
    */
   public void testAnnotations() throws Exception
   {
      IPSSecurityAopTestImplWs pubSvc = getPublicService();
      IPSSecurityAopTestImplDesignWs designSvc = getDesignService();
      
      // test an ingnore method - would normally fail
      login("admin2", "demo");
      try
      {
         assertNotNull(pubSvc.loadDesignObjectIgnore());
      }
      catch (RuntimeException e)
      {
         assertFalse("should not have thrown", true);
      }
      
      // test permission, normally not filtered, now will filter on DELETE
      login("admin1", "demo");
      List<IPSCatalogSummary> result = designSvc.findDesignObjectsPerm("test");
      assertNotNull(result);
      assertTrue(result.isEmpty());
      
      // test strategy - normally not filtered, now will filter on DELETE
      List<PSMockDesignObject> objList = pubSvc.findPublicObjectsCustom("test");
      assertNotNull(objList);
      assertTrue(objList.size() == 2);
   }
   
   /**
    * Tests various delete permutations.
    * 
    * @param mgr The mgr to use, assumed not <code>null</code>.
    * @param delIndex The index into {@link #getTestGuids()} that should 
    * succeed, all others are assumed to fail.
    * @param sessionId the current session, assumed not <code>null</code>.  
    * 
    * @throws Exception if the test fails.
    */
   private void doDesignDeleteTest(IPSSecurityAopTestImplDesignWs mgr, 
      int delIndex, String sessionId) throws Exception
   {
      // test object
      List<IPSGuid> testGuids = getTestGuids();
      PSMockDesignObject templateObj = new PSMockDesignObject();
      templateObj.setGUID(testGuids.get(0));
      PSMockDesignObject slotObj = new PSMockDesignObject();
      slotObj.setGUID(testGuids.get(1));
      PSMockDesignObject otherObj = new PSMockDesignObject();
      otherObj.setGUID(new PSGuid(PSTypeEnum.INTERNAL, 123));
      
      List<PSMockDesignObject> saveList = new ArrayList<PSMockDesignObject>();
      List<IPSGuid> failedGuids = new ArrayList<IPSGuid>();
      List<IPSGuid> successGuids = new ArrayList<IPSGuid>();
      
      boolean shouldThrow;
      shouldThrow = (delIndex != 0);
      if (shouldThrow)
         failedGuids.add(templateObj.getGuid());
      try
      {
         mgr.deleteDesignObjects(templateObj, false, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         assertTrue("should not have thrown", shouldThrow);
         validateUpdateException(failedGuids, successGuids, e);
      }
      
      shouldThrow = (delIndex != 1);
      failedGuids.clear();
      if (shouldThrow)
         failedGuids.add(slotObj.getGuid());
      try
      {
         mgr.deleteDesignObjects(slotObj, false, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         assertTrue("should not have thrown", shouldThrow);
         validateUpdateException(failedGuids, successGuids, e);
      }
         
      // test list of objects 
      saveList.add(templateObj);
      saveList.add(slotObj);
      failedGuids.clear();
      successGuids.clear();
      for (int i = 0; i < testGuids.size(); i++)
      {
         if (delIndex == i)
            successGuids.add(testGuids.get(i));
         else
            failedGuids.add(testGuids.get(i));
      }
      
      try
      {
         mgr.deleteDesignObjects(saveList, false, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         validateUpdateException(failedGuids, successGuids, e);
      }
      
      // test list with extra object and method throws
      failedGuids.addAll(successGuids);
      successGuids.clear();
      saveList.add(otherObj);
      failedGuids.add(otherObj.getGuid());
      try
      {
         mgr.deleteDesignObjects(saveList, true, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         validateUpdateException(failedGuids, successGuids, e);
      }
   }
   
   /**
    * Test enforcement of save signatures.
    * 
    * @throws Exception if the test fails.
    */
   public void testDesignSave() throws Exception
   {
      IPSSecurityAopTestImplDesignWs mgr = getDesignService();
      String sessionId = login("admin1", "demo");
 
      // ensure no guid throws error
      try
      {
         mgr.saveDesignObject("foo", sessionId);
         assertFalse("should have thrown", true);
      }
      catch (RuntimeException e)
      {
         
      }
      
      doDesignSaveTest(mgr, 1, sessionId);
      
      sessionId = login("editor1", "demo");
      doDesignSaveTest(mgr, 0, sessionId);
      
      sessionId = login("admin2", "demo");
      doDesignSaveTest(mgr, 1, sessionId);
   }
   
   /**
    * Tests various save permutations.
    * 
    * @param mgr The mgr to use, assumed not <code>null</code>.
    * @param saveIndex The index into {@link #getTestGuids()} that should 
    * succeed, all others are assumed to fail.
    * @param sessionId the current session, assumed not <code>null</code>.
    * 
    * @throws Exception if the test fails.
    */
   private void doDesignSaveTest(IPSSecurityAopTestImplDesignWs mgr, 
      int saveIndex, String sessionId) throws Exception
   {
      // test object
      List<IPSGuid> testGuids = getTestGuids();
      PSMockDesignObject templateObj = new PSMockDesignObject();
      templateObj.setGUID(testGuids.get(0));
      PSMockDesignObject slotObj = new PSMockDesignObject();
      slotObj.setGUID(testGuids.get(1));
      PSMockDesignObject otherObj = new PSMockDesignObject();
      otherObj.setGUID(new PSGuid(PSTypeEnum.INTERNAL, 123));
      
      List<PSMockDesignObject> saveList = new ArrayList<PSMockDesignObject>();
      List<IPSGuid> failedGuids = new ArrayList<IPSGuid>();
      List<IPSGuid> successGuids = new ArrayList<IPSGuid>();
      
      boolean shouldThrow;
      shouldThrow = (saveIndex != 0);
      if (shouldThrow)
         failedGuids.add(templateObj.getGuid());
      try
      {
         mgr.saveDesignObjects(templateObj, false, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         assertTrue("should not have thrown", shouldThrow);
         validateUpdateException(failedGuids, successGuids, e);
      }
      
      shouldThrow = (saveIndex != 1);
      failedGuids.clear();
      if (shouldThrow)
         failedGuids.add(slotObj.getGuid());
      try
      {
         mgr.saveDesignObjects(slotObj, false, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         assertTrue("should not have thrown", shouldThrow);
         validateUpdateException(failedGuids, successGuids, e);
      }
         
      // test list of objects 
      saveList.add(templateObj);
      saveList.add(slotObj);
      failedGuids.clear();
      successGuids.clear();
      for (int i = 0; i < testGuids.size(); i++)
      {
         if (saveIndex == i)
            successGuids.add(testGuids.get(i));
         else
            failedGuids.add(testGuids.get(i));
      }
      
      try
      {
         mgr.saveDesignObjects(saveList, false, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         validateUpdateException(failedGuids, successGuids, e);
      }
      
      // test list with extra object and method throws
      failedGuids.addAll(successGuids);
      successGuids.clear();
      saveList.add(otherObj);
      failedGuids.add(otherObj.getGuid());
      try
      {
         mgr.saveDesignObjects(saveList, true, sessionId);
         assertFalse("should have thrown", shouldThrow);
      }
      catch (PSErrorsException e)
      {
         validateUpdateException(failedGuids, successGuids, e);
      }
   }   
   
   /**
    * Validate the summary list.
    * 
    * @param sumList The list to validate, may be <code>null</code>.
    * @param count The expected size of the list.
    * @param perms The expected permissions to find in each entry.
    */
   private void validateFindResults(List<IPSCatalogSummary> sumList, int count, 
      PSPermissions[] perms)
   {
      assertNotNull(sumList);
      assertEquals(sumList.size(), count);
      for (IPSCatalogSummary summary : sumList)
      {
         PSObjectSummary sum = (PSObjectSummary) summary;
         assertNotNull(sum.getPermissions());
         Set<PSPermissions> permSet = 
            sum.getPermissions().getPermissions();
         for (PSPermissions permission : perms)
         {
            assertTrue(permSet.contains(permission));
         }
      }
   }

   
   /**
    * Test all load signatures for a user.  
    * 
    * @param user The user name, assumed not <code>null</code> or empty.
    * @param sessionId The session id of the user, assumed not <code>null</code> 
    * or empty.
    * @param readCount The number of expected objects on a read-only load
    * @param lockCount The number of expected objects on a locked load
    * @param shouldThrowRead <code>true</code> if loading a single object
    * read-only should throw an exception, <code>false</code> otherwise.
    * @param shouldThrowLock <code>true</code> if loading a single object
    * locked should throw an exception, <code>false</code> otherwise.
    * 
    * @throws Exception if the test fails.
    */
   private void doDesignLoadTest(String user, String sessionId, int readCount, 
      int lockCount, boolean shouldThrowRead, boolean shouldThrowLock) 
      throws Exception
   {
      IPSSecurityAopTestImplDesignWs mgr = getDesignService();
      List<IPSGuid> guids = getTestGuids();

      try
      {
         // load read only
         List<PSMockDesignObject> objList;
         try
         {
            objList = mgr.loadDesignObjects(guids, false, true, sessionId, 
               user);
            validateLoadResults(readCount, guids, objList);
         }
         catch(PSErrorResultsException e)
         {
            validateLoadException(readCount, guids, e);
         }
         
         // load locked
         try
         {
            objList = mgr.loadDesignObjects(guids, true, true, sessionId, 
               user);
            validateLoadResults(lockCount, guids, objList);
         }
         catch (PSErrorResultsException e)
         {
            validateLoadException(lockCount, guids, e);
         }
   
         String name = null;
         try
         {
            objList = mgr.loadDesignObjects(name, false, true, sessionId, user);
            assertFalse("failed to throw", true);
         }
         catch(PSErrorResultsException e)
         {
            validateLoadException(guids, e);
         }   
         
         name = "foo";
         try
         {
            objList = mgr.loadDesignObjects(name, true, true, sessionId, user);
            validateLoadResults(lockCount, guids, objList);
         }
         catch (PSErrorResultsException e)
         {
            validateLoadException(lockCount, guids, e);
         }
         
         name = null;
         try
         {
            mgr.loadDesignObject(name, false, true, sessionId, user);
            assertFalse("failed to throw", true);
         }
         catch (RuntimeException e)
         {
            // expected
         }
         
         name = "";
         try
         {
            mgr.loadDesignObject(name, true, true, sessionId, user);
            assertFalse("failed to throw", true);
         }
         catch (RuntimeException e)
         {
            // expected
         }
         
         PSMockDesignObject obj;
         try
         {
            obj = mgr.loadDesignObject(false, true, sessionId, user);
            assertFalse("failed to throw", shouldThrowRead);
            assertNotNull(obj);
         }
         catch (RuntimeException e)
         {
            assertTrue("should not throw", shouldThrowRead);
         }
         
         try
         {
            obj = mgr.loadDesignObject(true, true, sessionId, user);
            assertFalse("failed to throw", shouldThrowLock);
            assertNotNull(obj);
         }
         catch (RuntimeException e)
         {
            assertTrue("should not throw", shouldThrowLock);
         }
      }
      finally
      {
         try
         {
            IPSObjectLockService locksvc = 
               PSObjectLockServiceLocator.getLockingService();
            List<PSObjectLock> locks = locksvc.findLocksByObjectIds(guids, 
               sessionId, user);
            if (!locks.isEmpty())
            {
               locksvc.releaseLocks(locks);
            }
         }
         catch (Exception e)
         {
            System.out.println("Failed to cleanup locks: " +
               e.getLocalizedMessage());
         } 
      }
   }

   /**
    * Validates the results returned by a load.
    * 
    * @param count The expected count
    * @param guids The list of guids requested or expected in the load, assumed 
    * not <code>null</code>. 
    * @param objList The resulting load list, may be <code>null</code>.
    */
   private void validateLoadResults(int count, List<IPSGuid> guids, 
      List<PSMockDesignObject> objList)
   {
      if (count < guids.size())
         assertTrue("Should have thrown", false);
      
      assertNotNull(objList);
      assertTrue(objList.size() == count);
   }

   /**
    * Validates the exception thrown by a load.
    * 
    * @param count The expected count of results.
    * @param guids The requested or expected list of loaded guids, assumed not 
    * <code>null</code>.
    * @param e The exception thrown.
    */
   private void validateLoadException(int count, List<IPSGuid> guids, 
      PSErrorResultsException e)
   {
      if (count == guids.size())
         assertTrue("Should not have thrown", false);
      
      for (IPSGuid guid : guids)
      {
         assertTrue(e.getErrors().containsKey(guid) || 
            e.getResults().containsKey(guid));
      }
      
      for (Object object : e.getErrors().values())
      {
         assertTrue(object instanceof PSErrorException);
         PSErrorException error = (PSErrorException) object;
         assertEquals(error.getCode(), 
            IPSWebserviceErrors.ACCESS_CONTROL_ERROR);
      }
   }
   
   /**
    * Validates the exception thrown by a load.
    * 
    * @param guids The requested or expected list of loaded guids, assumed not 
    * <code>null</code>.
    * @param e The exception thrown.
    */
   private void validateLoadException(List<IPSGuid> guids, 
      PSErrorResultsException e)
   {
      for (IPSGuid guid : guids)
      {
         assertTrue(e.getErrors().containsKey(guid) || 
            e.getResults().containsKey(guid));
      }
      
      for (Map.Entry<IPSGuid, Object> entry : e.getErrors().entrySet())
      {
         Object object = entry.getValue();
         assertTrue(object instanceof PSErrorException);
         PSErrorException error = (PSErrorException) object;
         if (guids.contains(entry.getKey()))
         {
            assertEquals(error.getCode(), 
               IPSWebserviceErrors.ACCESS_CONTROL_ERROR);
         }
         else
         {
            assertEquals(error.getCode(), 
               IPSWebserviceErrors.OBJECT_NOT_FOUND);
         }
      }
   }   
   
   /**
    * Validates the exception thrown by a save or delete.
    * @param failedGuids The list of guids to fail, assumed not 
    * <code>null</code>.
    * @param successGuids The list of guids to succeed, assumed not 
    * <code>null</code>.
    * @param e The exception thrown.
    */
   private void validateUpdateException(List<IPSGuid> failedGuids, 
      List<IPSGuid> successGuids, PSErrorsException e)
   {
      for (IPSGuid guid : failedGuids)
      {
         assertTrue(e.getErrors().containsKey(guid));
      }

      for (IPSGuid guid : successGuids)
      {
         assertTrue(e.getResults().contains(guid));
      }

      for (Map.Entry<IPSGuid, Object> entry : e.getErrors().entrySet())
      {
         Object object = entry.getValue();
         assertTrue(object instanceof PSErrorException);
         PSErrorException error = (PSErrorException) object;
         assertTrue(error.getCode() == IPSWebserviceErrors.ACCESS_CONTROL_ERROR 
            || error.getCode() == IPSWebserviceErrors.OBJECT_NOT_FOUND);
      }
   }      
   
   /**
    * Login using the supplied credentials
    * 
    * @param uid The user id, assumed not <code>null</code> or empty.
    * @param pwd The password, assumed not <code>null</code> or empty.
    * 
    * @return The session id, never <code>null</code> or empty.
    * 
    * @throws Exception if the login fails.
    */
   private String login(String uid, String pwd) throws Exception
   {
      // hack to get by re-logging in to same session see PSSecurityfilter)
      session.setAttribute("RX_LOGIN_ATTEMPTS", null);
      PSSecurityFilter.authenticate(request, response, uid, pwd);
      String sessionId = (String) session.getAttribute(
         IPSHtmlParameters.SYS_SESSIONID);
      return sessionId;
   }

   /**
    * Get a list of object guids from the test acls.
    * 
    * @return The list, never <code>null</code>.
    */
   private List<IPSGuid> getTestGuids()
   {
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      
      for (IPSAcl acl : ms_testAcls)
      {
         guids.add(((PSAclImpl)acl).getObjectGuid());
      }
      
      return guids;
   }   
   
   /**
    * Get the design service manager for testing
    * 
    * @return The mgr, never <code>null</code>.
    */
   private IPSSecurityAopTestImplDesignWs getDesignService()
   {
      return (IPSSecurityAopTestImplDesignWs) PSBaseServiceLocator.getBean(
         "sys_securityAopTestWebServiceDesign");
   }
   
   /**
    * Get the public service manager for testing
    * 
    * @return The mgr, never <code>null</code>.
    */
   private IPSSecurityAopTestImplWs getPublicService()
   {
      return (IPSSecurityAopTestImplWs) PSBaseServiceLocator.getBean(
         "sys_securityAopTestWebService");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      ms_testAcls = createTestAcls();
   }

   @Override
   protected void tearDown() throws Exception
   {
      PSAclServiceTest.deleteAcls(ms_testAcls);
      super.tearDown();
   }
   
   /**
    * Creates and saves two test acls.  The first is for a template with the 
    * follwowing entries:
    * <ul>
    * <li>admin1 (user) - OWNER, READ</li>
    * <li>Editor (role) - READ, UPDATE, DELETE</li>
    * <li>Default (community) - RUNTIME_VISIBLE</li>
    * </ul>
    * The second is for a slot:
    * <ul>
    * <li>admin2 (user) - OWNER, DELETE</li>
    * <li>Admin (role) - READ, UPDATE</li>
    * <li>Default (community) - RUNTIME_VISIBLE</li>
    * </ul>
    * 
    * @return The test acls, never <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */
   private List<IPSAcl> createTestAcls() throws Exception
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      List<IPSAcl> aclList = new ArrayList<IPSAcl>();
      PSAclImpl acl; 
      acl = (PSAclImpl) aclService.createAcl(ms_templateGuid,
         new PSTypedPrincipal("admin1", PrincipalTypes.USER));
      aclList.add(acl);
      
      IPSAclEntry aclEntry;
      aclEntry = acl.getEntries().iterator().next();
      aclEntry.addPermission(PSPermissions.READ);
      
      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Editor", 
         PrincipalTypes.ROLE));
      aclEntry.addPermission(PSPermissions.UPDATE);
      aclEntry.addPermission(PSPermissions.READ);
      aclEntry.addPermission(PSPermissions.DELETE);

      acl.addEntry((PSAclEntryImpl) aclEntry);
      
      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Default",
         PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);
      
      acl = (PSAclImpl) aclService.createAcl(ms_slotTemplate,
         new PSTypedPrincipal("admin2", PrincipalTypes.USER));
      aclList.add(acl);
      
      aclEntry = acl.getEntries().iterator().next();
      aclEntry.addPermission(PSPermissions.DELETE);
      
      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Admin",
         PrincipalTypes.ROLE));
      aclEntry.addPermission(PSPermissions.UPDATE);
      aclEntry.addPermission(PSPermissions.READ);
      acl.addEntry((PSAclEntryImpl) aclEntry);
      
      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Default",
         PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);
      
      aclService.saveAcls(aclList);
      
      return aclList;
   }
   
   /**
    * List of test acls, never <code>null</code> or empty during test execution.
    * See {@link PSAclServiceTest#createTestAcls()} for details.
    */
   private static List<IPSAcl> ms_testAcls;
   
   /**
    * object guid for template
    */
   private static IPSGuid ms_templateGuid = new PSGuid(PSTypeEnum.TEMPLATE, 
      123);
   
   /**
    * object guid for slot
    */
   private static IPSGuid ms_slotTemplate = new PSGuid(PSTypeEnum.SLOT, 123);
}

