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

package com.percussion.webservices.rhythmyxdesign;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.util.PSBase64Decoder;
import com.percussion.util.PSBase64Encoder;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSAssemblyTestBase;
import com.percussion.webservices.PSSystemTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.system.PSAccessLevelImpl;
import com.percussion.webservices.system.PSAclEntryImpl;
import com.percussion.webservices.system.PSAclImpl;
import com.percussion.webservices.system.PSDependency;
import com.percussion.webservices.system.PSFilterRule;
import com.percussion.webservices.system.PSItemFilter;
import com.percussion.webservices.system.PSMimeContentAdapter;
import com.percussion.webservices.system.PSRelationshipConfig;
import com.percussion.webservices.system.PSSharedProperty;
import com.percussion.webservices.system.RelationshipCategory;
import com.percussion.webservices.systemdesign.CreateGuidsRequest;
import com.percussion.webservices.systemdesign.CreateLocksRequest;
import com.percussion.webservices.systemdesign.CreateRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.DeleteRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.FindRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.FindWorkflowsRequest;
import com.percussion.webservices.systemdesign.LoadAclsRequest;
import com.percussion.webservices.systemdesign.LoadConfigurationRequest;
import com.percussion.webservices.systemdesign.LoadConfigurationResponse;
import com.percussion.webservices.systemdesign.LoadRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.SaveAclsRequest;
import com.percussion.webservices.systemdesign.SaveConfigurationRequest;
import com.percussion.webservices.systemdesign.SaveRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Test system design web service functionality.
 */
@Category(IntegrationTest.class)
public class SystemDesignTestCase extends PSSystemTestBase
{
   @Test
   public void testSystemDesignSharedPropertiesCRUD() throws Exception
   {
      String[] names = { "name_0", "name_1", "name_2" };

      List<PSSharedProperty> properties = new ArrayList<PSSharedProperty>();
      for (int i = 0; i < names.length; i++)
         properties.add(new PSSharedProperty(names[i], "value_" + i));

      doSaveSharedPropertiesTest(properties, true);

      properties = doLoadSharedPropertiesTest(names);

      // modify and save
      for (PSSharedProperty property : properties)
         property.setValue(property.getName() + " - test");

      doSaveSharedPropertiesTest(properties, true);

      doDeleteSharedPropertiesTest(properties);
   }

   /**
    * Tests loading shared properties.
    * 
    * @param names the names of the properties to load, may be 
    *    <code>null</code> or empty, asterisk wildcards are supported.
    * @return the loaded properties, never <code>null</code> or empty.
    * @throws Exception if the test fails.
    */
   private List<PSSharedProperty> doLoadSharedPropertiesTest(String[] names)
      throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            loadSharedProperties(names, null, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            loadSharedProperties(names, "nosuchsession", false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // load locked
         List<PSSharedProperty> resultsLocked = loadSharedProperties(names,
            m_session, true);
         assertTrue(resultsLocked != null);

         // now try as another user
         try
         {
            loadSharedProperties(names, null, true, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSSharedProperty.class.getName());
         }

         // load unlocked
         List<PSSharedProperty> resultsUnlocked = loadSharedProperties(names,
            m_session, false);

         assertEquals(resultsLocked, resultsUnlocked);

         // now load as another user unlocked
         loadSharedProperties(names, null, false, "admin2", "demo");

         // now leave locked
         return resultsLocked;
      }
      catch (com.percussion.webservices.faults.PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
   }

   /**
    * Tests saving shared properties.
    * 
    * @param properties the properties to save, assumed not <code>null</code> 
    *    or empty and to have been locked.
    * @param release <code>true</code> leave the locks released when the test is
    *    completed, <code>false</code> to leave the locks in place.
    * @throws Exception if the test fails.
    */
   private void doSaveSharedPropertiesTest(List<PSSharedProperty> properties,
      boolean release) throws Exception
   {
      // test no session
      try
      {
         saveProperties(properties, null, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         saveProperties(properties, "nosuchsession", false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test bad input
      try
      {
         saveProperties(null, m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      try
      {
         saveProperties(new ArrayList<PSSharedProperty>(), m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test save and load, compare results
      try
      {
         saveProperties(properties, m_session, true);

         // try to resave w/out lock
         try
         {
            saveProperties(properties, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }

         String[] names = new String[properties.size()];
         int i = 0;
         for (PSSharedProperty property : properties)
            names[i++] = property.getName();

         List<PSSharedProperty> properties2 = loadSharedProperties(names,
            m_session, !release);
         assertEquals(properties, properties2);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e1.dumpToString());
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2.dumpToString());
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3.dumpToString());
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4.dumpToString());
      }
   }

   /**
    * Tests deleting shared properties.
    * 
    * @param properties the properties to delete, assumed not <code>null</code>.
    * @throws Exception if the test fails.
    */
   private void doDeleteSharedPropertiesTest(List<PSSharedProperty> properties)
      throws Exception
   {
      String[] names = new String[properties.size()];
      int index = 0;
      for (PSSharedProperty property : properties)
         names[index++] = property.getName();

      try
      {
         // test no session
         try
         {
            deleteSharedProperties(properties, null, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            deleteSharedProperties(properties, "nosuchsession", true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // release, delete and search for results
         deleteSharedProperties(properties, m_session, true);
         List<PSSharedProperty> test = loadSharedProperties(names, m_session,
            false);
         assertTrue(test != null && test.size() == 0);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
   }

   @Test
   public void test4systemDesignSOAPExtendLock() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] request = new long[2];
         request[0] = m_eigerSlot.getId();
         request[1] = m_jungfrauSlot.getId();

         // try to lock objects without rhythmyx session
         try
         {
            binding.extendLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to lock objects with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.extendLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to lock objects with null ids
         try
         {
            binding.extendLocks(null);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try lock objects with an empty ids
         try
         {
            binding.extendLocks(new long[0]);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to extend unlocked objects
         try
         {
            binding.extendLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // lock all test slots
         PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
         assemblyTest.lockSlots(request, session);

         // extend the locked slots
         binding.extendLocks(request);

         // try to extend the test slots for a different session
         try
         {
            String session_2 = PSTestUtils.login();
            PSTestUtils.setSessionHeader(binding, session_2);

            binding.extendLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to extend the test slots for a different user
         try
         {
            String session_3 = PSTestUtils.login("admin2", "demo");
            PSTestUtils.setSessionHeader(binding, session_3);

            binding.extendLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // release the test slots
         binding.releaseLocks(request);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   @Test
   public void test5systemDesignSOAPReleaseLock() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] request = new long[2];
         request[0] = m_eigerSlot.getId();
         request[1] = m_jungfrauSlot.getId();

         // lock all test slots
         PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
         assemblyTest.lockSlots(request, session);

         // try to release objects without rhythmyx session
         try
         {
            binding.releaseLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to release objects with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.releaseLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to release objects with null ids
         try
         {
            binding.releaseLocks(null);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try release objects with an empty ids
         try
         {
            binding.releaseLocks(new long[0]);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // release the test slots
         binding.releaseLocks(request);

         // release the test slots again
         binding.releaseLocks(request);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   @Test
   public void testsystemDesignSOAPGetLockedSummaries() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         // try to get locked summaries without rhythmyx session
         try
         {
            binding.getLockedSummaries();
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to get lockd summaries with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.getLockedSummaries();
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         PSObjectSummary[] summaries = binding.getLockedSummaries();
         assertTrue(summaries != null && summaries.length == 0);

         // create a new slot but don't save it, returns a dummy summary
         PSAssemblyTestBase asmTest = new PSAssemblyTestBase();
         asmTest.createSlot("foo", false, session);

         // lock all test slots including the new created slot
         long[] ids = new long[2];
         ids[0] = m_eigerSlot.getId();
         ids[1] = m_jungfrauSlot.getId();
         PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
         assemblyTest.lockSlots(ids, session);

         summaries = binding.getLockedSummaries();
         assertTrue(summaries != null && summaries.length == 3);

         // release the test slots
         binding.releaseLocks(ids);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   @Test
   public void testsystemDesignSOAPCreateLocks() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         // create a new slot but don't save it, returns a dummy summary
         PSAssemblyTestBase asmTest = new PSAssemblyTestBase();
         PSTemplateSlot foo = asmTest.createSlot("foo", false, session);

         long[] ids = new long[3];
         ids[0] = m_eigerSlot.getId();
         ids[1] = m_jungfrauSlot.getId();
         ids[2] = foo.getId();

         CreateLocksRequest request = new CreateLocksRequest();

         // try without rhythmyx session
         try
         {
            binding.createLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.createLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with empty ids
         try
         {
            binding.createLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }
         
         // are the test slots locked?
         PSObjectSummary[] summaries = binding.isLocked(ids);
         assertTrue(summaries != null && summaries.length == 3);
         assertTrue(summaries[0] == null);
         assertTrue(summaries[1] == null);
         assertTrue(summaries[2] != null);

         // lock the eiger slot
         request = new CreateLocksRequest();
         request.setId(new long[] {m_eigerSlot.getId()});
         request.setOverrideLock(false);
         binding.createLocks(request);

         // verify that eiger is locked
         summaries = binding.isLocked(ids);
         assertTrue(summaries != null && summaries.length == 3);
         assertTrue(summaries[0] != null);
         assertTrue(summaries[1] == null);
         assertTrue(summaries[2] != null);
         
         // lock all slots
         request = new CreateLocksRequest();
         request.setId(ids);
         request.setOverrideLock(false);
         binding.createLocks(request);

         // verify that all slots are locked
         summaries = binding.isLocked(ids);
         assertTrue(summaries != null && summaries.length == 3);
         assertTrue(summaries[0] != null);
         assertTrue(summaries[1] != null);
         assertTrue(summaries[2] != null);
         
         // try to create locks for a different session
         String session_2 = PSTestUtils.login();
         PSTestUtils.setSessionHeader(binding, session_2);
         try
         {
            binding.createLocks(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            assertTrue(true);
         }
         
         // override locks
         request.setOverrideLock(true);
         binding.createLocks(request);

         // release all locks
         binding.releaseLocks(ids);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   @Test
   public void testsystemDesignSOAPIsLocked() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         // create a new slot but don't save it, returns a dummy summary
         PSAssemblyTestBase asmTest = new PSAssemblyTestBase();
         PSTemplateSlot foo = asmTest.createSlot("foo", false, session);

         long[] request = new long[3];
         request[0] = m_eigerSlot.getId();
         request[1] = m_jungfrauSlot.getId();
         request[2] = foo.getId();

         // try without rhythmyx session
         try
         {
            binding.isLocked(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.isLocked(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with empty ids
         try
         {
            binding.isLocked(new long[0]);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // are the test slots locked?
         PSObjectSummary[] summaries = binding.isLocked(request);
         assertTrue(summaries != null && summaries.length == 3);
         assertTrue(summaries[0] == null);
         assertTrue(summaries[1] == null);
         assertTrue(summaries[2] != null);

         // lock one test slot
         long[] ids = new long[1];
         ids[0] = m_eigerSlot.getId();
         PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
         assemblyTest.lockSlots(ids, session);

         // are the test slots locked?
         summaries = binding.isLocked(request);
         assertTrue(summaries != null && summaries.length == 3);
         assertTrue(summaries[0] != null);
         assertTrue(summaries[1] == null);
         assertTrue(summaries[2] != null);

         // release the test slot again
         binding.releaseLocks(ids);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   /**
    * Tests the dependency finder service, requires FastForward implemenatation
    * and sample content be installed.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void test6systemDesignSOAPFindDependencies() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // test no session
      try
      {
         binding.findDependencies(null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      PSTestUtils.setSessionHeader(binding, "nosuchsession");
      try
      {
         binding.findDependencies(null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      PSTestUtils.setSessionHeader(binding, m_session);

      // test invalid inputs
      try
      {
         binding.findDependencies(null);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      try
      {
         binding.findDependencies(new long[0]);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // Test valid operation
      try
      {
         long[] ids;
         PSDependency[] results;

         ids = new long[1];
         ids[0] = new PSDesignGuid(PSTypeEnum.WORKFLOW, 4).getValue();
         results = binding.findDependencies(ids);
         assertNotNull(results);
         assertEquals(results.length, ids.length);
         assertNotNull(results[0].getDependents());
         assertTrue(results[0].getDependents().length > 0);

         ids = new long[2];
         ids[0] = new PSDesignGuid(PSTypeEnum.WORKFLOW, 4).getValue();
         ids[1] = new PSDesignGuid(PSTypeEnum.WORKFLOW, 5).getValue();
         results = binding.findDependencies(ids);
         assertNotNull(results);
         assertEquals(results.length, ids.length);
         for (int i = 0; i < results.length; i++)
         {
            assertNotNull(results[i].getDependents());
            assertTrue(results[i].getDependents().length > 0);
         }

         ids = new long[3];
         ids[0] = new PSDesignGuid(PSTypeEnum.WORKFLOW, 4).getValue();
         ids[1] = new PSDesignGuid(PSTypeEnum.WORKFLOW, 5).getValue();
         ids[2] = new PSDesignGuid(PSTypeEnum.WORKFLOW, 5000).getValue();

         results = binding.findDependencies(ids);
         assertNotNull(results);
         assertEquals(results.length, ids.length);
         assertNotNull(results[0].getDependents());
         assertTrue(results[0].getDependents().length > 0);
         assertNotNull(results[1].getDependents());
         assertTrue(results[1].getDependents().length > 0);
         assertNotNull(results[2].getDependents());
         assertTrue(results[2].getDependents().length == 0);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   /**
    * Tests relationship type related services.
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testSystemDesignSOAPRelationshipType() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(new Integer(60000));
      PSTestUtils.setSessionHeader(binding, m_session);

      PSRelationshipConfig config;
      final String USER_RELATIONSHIP = "BenRelationship";

      cleanupRelationshipType(binding, USER_RELATIONSHIP);

      // lookup existing (system) relationship configs = 7
      PSObjectSummary[] summs = findRelationshipTypes(binding, null, null);
      assertTrue(summs.length == 7);

      // update 1st system config
      config = loadRelationshipType(binding, summs[0].getId());
      String newDesc = "Modified by UnitTest for " + config.getName();
      config.setDescription(newDesc);
      saveRelationshipType(binding, config, true);

      config = loadRelationshipType(binding, config.getId(), false);
      assertTrue(config.getDescription().equals(newDesc));

      // fail to load an unknown config id
      try
      {
         long[] invalidIds = new long[summs.length];
         for (int i=0; i<summs.length; i++)
            invalidIds[i] = summs[i].getId();
         invalidIds[summs.length-1] = invalidIds[summs.length-1] + 1000;
         
         LoadRelationshipTypesRequest lreq = new LoadRelationshipTypesRequest(
            invalidIds, false, false);
         binding.loadRelationshipTypes(lreq);
         assertTrue(false);
      }
      catch (PSErrorResultsFault e)
      {
         verifyErrorResultsFault(e, summs.length-1, 
            PSRelationshipConfig.class.getName());
      }

      // create & save a relationship config
      config = createSaveRelationshipType(binding, USER_RELATIONSHIP,
         RelationshipCategory.ActiveAssembly);

      // should have 8 configs now
      summs = findRelationshipTypes(binding, null, null);
      if (summs.length == 8)
         // lookup the samed one
         summs = findRelationshipTypes(binding, USER_RELATIONSHIP, null);

      if (summs.length == 1)
         // cleanup at the end
         cleanupRelationshipType(binding, USER_RELATIONSHIP);
   }

   /**
    * Test loading and saving configurations
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testsystemDesignSOAPLoadSaveConfiguration() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operations
      Properties wfProps = null;
      LoadConfigurationRequest loadReq = new LoadConfigurationRequest();
      SaveConfigurationRequest saveReq = new SaveConfigurationRequest();
      String name = PSConfigurationTypes.WF_CONFIG.name();
      PSMimeContentAdapter config = null;

      boolean saved = false;

      try
      {
         loadReq.setName(name);
         LoadConfigurationResponse resp;

         // test load no session
         try
         {
            binding.loadConfiguration(loadReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test load invalid session
         PSTestUtils.setSessionHeader(binding, "nosuchsession");
         try
         {
            binding.loadConfiguration(loadReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         PSTestUtils.setSessionHeader(binding, m_session);
         // test load invalid args
         loadReq.setName("badname");
         try
         {
            binding.loadConfiguration(loadReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // load locked and get props
         loadReq.setName(name);
         loadReq.setLock(true);
         resp = binding.loadConfiguration(loadReq);
         config = resp.getPSMimeContentAdapter();
         wfProps = extractProperties(config);

         // try load as another user
         String session2 = PSTestUtils.login("admin2", "demo");
         try
         {
            PSTestUtils.setSessionHeader(binding, session2);
            loadReq.setLock(true);
            binding.loadConfiguration(loadReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSLockFault e)
         {
            // expected exception
         }

         // load unlocked
         PSTestUtils.releaseLocks(m_session, new long[] { config.getId() });
         PSTestUtils.setSessionHeader(binding, m_session);
         loadReq.setLock(false);
         binding.loadConfiguration(loadReq);

         // load another user unlocked
         PSTestUtils.setSessionHeader(binding, session2);
         loadReq.setLock(false);
         binding.loadConfiguration(loadReq);

         // leave locked
         PSTestUtils.setSessionHeader(binding, m_session);
         loadReq.setLock(true);
         binding.loadConfiguration(loadReq);

         // test save no session
         binding = getDesignBinding(null);
         saveReq.setRelease(false);

         try
         {
            saveReq.setPSMimeContentAdapter(createContent(config, wfProps));
            binding.saveConfiguration(saveReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test save invalid session
         PSTestUtils.setSessionHeader(binding, "nosuchsession");
         try
         {
            saveReq.setPSMimeContentAdapter(createContent(config, wfProps));
            binding.saveConfiguration(saveReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid content name
         PSTestUtils.setSessionHeader(binding, m_session);
         try
         {
            PSMimeContentAdapter badContent = createContent(config, wfProps);
            badContent.setName("badName");
            saveReq.setPSMimeContentAdapter(badContent);
            binding.saveConfiguration(saveReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // save and release lock
         Properties modProps = new Properties();
         modProps.putAll(wfProps);
         modProps.setProperty("testProp", String.valueOf(System
            .currentTimeMillis()));

         saveReq.setRelease(true);
         saveReq.setPSMimeContentAdapter(createContent(config, modProps));
         binding.saveConfiguration(saveReq);
         saved = true;

         // test save w/out lock
         try
         {
            saveReq.setPSMimeContentAdapter(createContent(config, wfProps));
            binding.saveConfiguration(saveReq);
            assertTrue("Should have thrown", false);
         }
         catch (PSLockFault e)
         {
            // expected exception
         }

         // load and compare
         loadReq.setLock(true);
         resp = binding.loadConfiguration(loadReq);
         config = resp.getPSMimeContentAdapter();
         Properties testProps = extractProperties(config);
         assertEquals(modProps, testProps);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSUnknownConfigurationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "UnknownConfigurationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSLockFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "LockFault Exception caught: " + e4);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e5)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e5);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
      finally
      {
         try
         {
            if (saved)
            {
               // try to restore
               PSTestUtils.setSessionHeader(binding, m_session);
               loadReq.setName(name);
               loadReq.setLock(true);
               config = binding.loadConfiguration(loadReq)
                  .getPSMimeContentAdapter();

               saveReq.setRelease(true);
               saveReq.setPSMimeContentAdapter(createContent(config, wfProps));
               binding.saveConfiguration(saveReq);
            }
         }
         catch (Exception e)
         {
            System.out.println("Failed to restore original wf props");
         }
         finally
         {
            if (config != null)
               PSTestUtils.releaseLocks(m_session,
                  new long[] { config.getId() });
         }
      }
   }

   /**
    * Create a new content adapter from the supplied adapter using the 
    * properties as the content.
    * 
    * @param content The adapter to use as a template, assumed not 
    * <code>null</code>.
    * @param props The properties to use as content.
    * 
    * @return The new content adapter, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSMimeContentAdapter createContent(PSMimeContentAdapter content,
      Properties props) throws Exception
   {
      PSMimeContentAdapter newContent = new PSMimeContentAdapter();
      newContent.setCharacterEncoding(content.getCharacterEncoding());
      newContent.setId(content.getId());
      newContent.setMimeType(content.getMimeType());
      newContent.setName(content.getName());
      newContent.setTransferEncoding(content.getTransferEncoding());

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      props.store(out, null);
      byte[] data = out.toByteArray();
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      out = new ByteArrayOutputStream();
      PSBase64Encoder.encode(in, out);

      newContent.setContent(out.toString(content.getCharacterEncoding()));
      newContent.setContentLength(data.length);

      return newContent;
   }

   /**
    * Validates the supplied content adapter and extracts the content as a set
    * of properties.
    * 
    * @param config The content to validate, assumed not <code>null</code>.
    * 
    * @return the props, never <code>null</code>.
    * 
    * @throws UnsupportedEncodingException if there is an error decoding the
    * content
    * @throws IOException if there is an error extracting the content.
    */
   private Properties extractProperties(PSMimeContentAdapter config)
      throws UnsupportedEncodingException, IOException
   {
      assertNotNull(config);
      assertNotNull(config.getContent());
      assertTrue(config.getContentLength() > 0);
      assertEquals(config.getTransferEncoding(),
         IPSMimeContentTypes.MIME_ENC_BASE64);
      String stringValue = (String) config.getContent();
      ByteArrayInputStream iBuf = new ByteArrayInputStream(stringValue
         .getBytes(config.getCharacterEncoding()));
      ByteArrayOutputStream oBuf = new ByteArrayOutputStream();
      PSBase64Decoder.decode(iBuf, oBuf);
      Properties props = new Properties();
      iBuf = new ByteArrayInputStream(oBuf.toByteArray());
      props.load(iBuf);

      return props;
   }

   @Test
   public void test14systemDesignSOAPFindWorkflows() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // test no session
      FindWorkflowsRequest req = new FindWorkflowsRequest();
      try
      {
         binding.findWorkflows(req);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      PSTestUtils.setSessionHeader(binding, "nosuchsession");
      try
      {
         binding.findWorkflows(req);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      PSTestUtils.setSessionHeader(binding, m_session);

      // Test valid operation
      try
      {
         PSObjectSummary[] sums;
         sums = binding.findWorkflows(req);
         assertNotNull(sums);
         assertTrue(sums.length > 0);

         req.setName("");
         PSObjectSummary[] results = binding.findWorkflows(req);
         assertEquals(sums.length, results.length);
         for (int i = 0; i < results.length; i++)
         {
            assertEquals(sums[i], results[i]);
         }

         for (PSObjectSummary summary : sums)
         {
            req.setName(summary.getName());
            results = binding.findWorkflows(req);
            assertTrue(results.length == 1);
            assertEquals(results[0], summary);
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   @Test
   public void test19systemDesignSOAPCreateGuids() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         CreateGuidsRequest request = null;

         // try to create guids without rhythmyx session
         try
         {
            request = new CreateGuidsRequest();
            request.setType(PSTypeEnum.ACL.ordinal());
            binding.createGuids(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create guids with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new CreateGuidsRequest();
            request.setType(PSTypeEnum.ACL.ordinal());
            binding.createGuids(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to create guids with invalid type
         try
         {
            request = new CreateGuidsRequest();
            request.setType(Short.MAX_VALUE);
            binding.createGuids(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try create guids with invalid count
         try
         {
            request = new CreateGuidsRequest();
            request.setType(PSTypeEnum.ACL.ordinal());
            request.setCount(0);
            binding.createGuids(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create 1 ACL guid
         request = new CreateGuidsRequest();
         request.setType(PSTypeEnum.ACL.ordinal());
         long[] ids = binding.createGuids(request);
         assertTrue(ids.length == 1);
         PSDesignGuid guid = new PSDesignGuid(ids[0]);
         assertTrue(guid.getType() == PSTypeEnum.ACL.ordinal());

         // create 7 Community guids
         request = new CreateGuidsRequest();
         request.setType(PSTypeEnum.COMMUNITY_DEF.ordinal());
         request.setCount(7);
         ids = binding.createGuids(request);
         assertTrue(ids.length == 7);
         for (long id : ids)
         {
            guid = new PSDesignGuid(id);
            assertTrue(guid.getType() == PSTypeEnum.COMMUNITY_DEF.ordinal());
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   /**
    * Tests all ACL web services
    * 
    * @throws Exception if the test fails or there are any errors.
    */
   @Test
   public void testsystemDesignSOAPAclTest() throws Exception
   {
      PSAclImpl[] acls = doCreateAclsTest();
      try
      {
         acls = doSaveAclsTest(acls);
         acls = doLoadAclsTest(acls);
         doDeleteAclsTest(acls);
         acls = null;
      }
      finally
      {
         // delete in finally block
         if (acls != null)
         {
            try
            {
               loadAcls(getObjectIds(acls), m_session, true);
               deleteAcls(getAclIds(acls), m_session, true);
            }
            catch (Exception e)
            {
               System.out.println("Failed to cleanup test acls: "
                  + e.getLocalizedMessage());
            }
         }
      }
   }

   /**
    * Tests creating acls.
    * 
    * @return The created acls, never <code>null</code>.
    * 
    * @throws Exception if the test fails
    */
   private PSAclImpl[] doCreateAclsTest() throws Exception
   {
      // Test operation
      try
      {
         PSDesignGuid templateGuid = new PSDesignGuid(PSTypeEnum.TEMPLATE, 123);
         PSDesignGuid slotGuid = new PSDesignGuid(PSTypeEnum.SLOT, 123);
         long[] ids = new long[] { templateGuid.getValue(), slotGuid.getValue()};

         // test no session
         try
         {
            createAcls(ids, null);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            createAcls(ids, "nosuchsession");
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            createAcls(null, m_session);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            createAcls(new long[0], m_session);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // valid operation
         return createAcls(ids, m_session);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "Remote Exception caught: " + e);
      }
   }

   /**
    * Create acls
    * 
    * @param ids The object ids for which acls are created, assumed not 
    * <code>null</code> or empty.
    * @param session The session to use, may be <code>null</code> or empty.
    * 
    * @return The acls, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSAclImpl[] createAcls(long[] ids, String session) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      PSAclImpl[] acls = binding.createAcls(ids);
      return acls;
   }

   /**
    * Tests saving acls
    * 
    * @param acls The acls to save, assumed not <code>null</code> or empty.
    * 
    * @return The re-loaded saved acls, locked, not <code>null</code>.
    * 
    * @throws Exception if the test fails.
    */
   private PSAclImpl[] doSaveAclsTest(PSAclImpl[] acls) throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            saveAcls(acls, null, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            saveAcls(acls, "nosuchsession", false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            saveAcls(null, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            saveAcls(new PSAclImpl[0], m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // valid operation
         PSAclImpl[] results = saveAcls(acls, m_session, false);
         compareAclArrays(acls, results, false);

         return results;
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
   }

   /**
    * Save the specified acls
    * 
    * @param acls The acls to save, assumed not <code>null</code>.
    * @param session The session to use, may be <code>null</code> or empty.
    * @param release <code>true</code> to release locks, <code>false</code>
    * otherwise.
    * 
    * @return The reloaded acls, locked if release is <code>false</code>.
    * 
    * @throws Exception if the save fails.
    */
   private PSAclImpl[] saveAcls(PSAclImpl[] acls, String session,
      boolean release) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveAclsRequest req = new SaveAclsRequest();
      req.setPSAclImpl(acls);
      req.setRelease(release);

      binding.saveAcls(req);

      long[] ids = getObjectIds(acls);

      PSAclImpl[] result = loadAcls(ids, m_session, !release);

      return result;
   }

   /**
    * Compare two arrays of acls
    * 
    * @param acls The acls to compare, assumed not <code>null</code>.
    * @param result The expected acls, assumed not <code>null</code>.
    * @param compareIds <code>true</code> to compare guids, <code>false</code>
    * to skip that property.
    *   
    * @throws Exception if the comparison fails. 
    */
   private void compareAclArrays(PSAclImpl[] acls, PSAclImpl[] result,
      boolean compareIds) throws Exception
   {
      assertEquals(acls.length, result.length);

      // loaded acls may have a different order
      Map<Long, PSAclImpl> resultMap = new HashMap<Long, PSAclImpl>();
      for (int i = 0; i < result.length; i++)
      {
         PSDesignGuid guid = new PSDesignGuid(PSTypeEnum.valueOf(result[i]
            .getObjectType()), result[i].getObjectId());
         resultMap.put(guid.getValue(), result[i]);
      }

      for (int i = 0; i < acls.length; i++)
      {
         PSDesignGuid guid = new PSDesignGuid(PSTypeEnum.valueOf(acls[i]
            .getObjectType()), acls[i].getObjectId());
         PSAclImpl resultAcl = resultMap.get(guid.getValue());
         assertNotNull(resultAcl);

         assertEquals(acls[i].getObjectId(), resultAcl.getObjectId());
         assertEquals(acls[i].getObjectType(), resultAcl.getObjectType());
         assertEquals(acls[i].getName(), resultAcl.getName());
         assertEquals(acls[i].getDescription(), resultAcl.getDescription());
         if (compareIds)
            assertEquals(acls[i].getId(), resultAcl.getId());

         PSAclEntryImpl[] srcEntries = acls[i].getEntries();
         PSAclEntryImpl[] resultEntries = resultAcl.getEntries();
         assertEquals(srcEntries.length, resultEntries.length);

         Map<String, PSAclEntryImpl> entryMap = new HashMap<String, PSAclEntryImpl>();
         for (int j = 0; j < resultEntries.length; j++)
         {
            assertTrue(!entryMap.containsKey(resultEntries[j].getName()));
            entryMap.put(resultEntries[j].getName(), resultEntries[j]);
         }

         for (int j = 0; j < srcEntries.length; j++)
         {
            PSAclEntryImpl resultEntry = entryMap.get(srcEntries[j].getName());
            assertNotNull(resultEntry);

            assertEquals(srcEntries[j].getType(), resultEntry.getType());
            if (compareIds)
            {
               assertEquals(srcEntries[j].getId(), resultEntry.getId());
               assertEquals(srcEntries[j].getAclId(), resultEntry.getAclId());
            }
            assertEquals(srcEntries[j].getPermissions().length, resultEntry
               .getPermissions().length);

            PSAccessLevelImpl[] srcPerms = srcEntries[j].getPermissions();
            PSAccessLevelImpl[] resultPerms = resultEntry.getPermissions();
            Set<Integer> srcPermSet = new HashSet<Integer>();
            Set<Integer> resultPermSet = new HashSet<Integer>();
            for (int k = 0; k < resultPerms.length; k++)
            {
               if (compareIds)
               {
                  assertEquals(srcPerms[k].getAclEntryId(), resultPerms[k]
                     .getAclEntryId());
                  assertEquals(srcPerms[k].getId(), resultPerms[k].getId());
                  assertEquals(srcPerms[k].getPermission(), resultPerms[k]
                     .getPermission());
               }
               else
               {
                  srcPermSet.add(srcPerms[k].getPermission());
                  resultPermSet.add(resultPerms[k].getPermission());
               }
            }
            assertEquals(srcPermSet, resultPermSet);
         }
      }
   }

   /**
    * Get object id array from acl array.
    * 
    * @param acls The acl array, assumed not <code>null</code>.
    * 
    * @return array of object ids.
    */
   private long[] getObjectIds(PSAclImpl[] acls)
   {
      long[] ids = new long[acls.length];
      for (int i = 0; i < ids.length; i++)
         ids[i] = new PSDesignGuid(PSTypeEnum.valueOf(acls[i].getObjectType()),
            acls[i].getObjectId()).getValue();

      return ids;
   }

   /**
    * Test loading and modifying acls
    * 
    * @param acls The source acls to test loading, assumed not 
    * <code>null</code>.
    * 
    * @return The acls as last loaded by this test, never <code>null</code>, 
    * will be locked.
    * 
    * @throws Exception if there are any errors.
    */
   private PSAclImpl[] doLoadAclsTest(PSAclImpl[] acls) throws Exception
   {
      // Test operation
      try
      {
         // test no session
         long[] ids = getObjectIds(acls);
         try
         {
            loadAcls(ids, null, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            loadAcls(ids, "nosuchsession", false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            loadAcls(null, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            loadAcls(new long[0], m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // load locked
         PSAclImpl[] resultsLocked = loadAcls(ids, m_session, true);
         assertTrue(resultsLocked.length == ids.length);
         compareAclArrays(acls, resultsLocked, true);

         // now try as another user
         try
         {
            loadAcls(ids, null, true, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSAclImpl.class.getName());
         }

         // load unlocked
         PSTestUtils.releaseLocks(m_session, ids);
         PSAclImpl[] resultsUnlocked = loadAcls(ids, m_session, false);

         compareAclArrays(acls, resultsUnlocked, true);

         // now try load as another user unlocked
         try
         {
            loadAcls(ids, null, false, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSAclImpl.class.getName());
         }

         // now leave locked
         resultsLocked = loadAcls(ids, m_session, true);

         // modify and test
         resultsLocked[0].setDescription("test1");
         // test add
         int entryLen = resultsLocked[0].getEntries().length;
         PSAclEntryImpl[] newEntries = new PSAclEntryImpl[entryLen + 1];
         System.arraycopy(resultsLocked[0].getEntries(), 0, newEntries, 0,
            entryLen);
         PSAclEntryImpl newEntry = new PSAclEntryImpl();
         newEntry.setName("Editor");
         newEntry.setType(PrincipalTypes.ROLE.getOrdinal());
         newEntry.setId(-1);
         newEntry.setAclId(resultsLocked[0].getId());
         newEntries[entryLen] = newEntry;
         resultsLocked[0].setEntries(newEntries);
         PSAccessLevelImpl newPerm = new PSAccessLevelImpl();
         newPerm.setAclEntryId(newEntry.getId());
         newPerm.setPermission(PSPermissions.UPDATE.getOrdinal());
         newPerm.setId(-1);
         newEntry.setPermissions(new PSAccessLevelImpl[] { newPerm });

         PSAccessLevelImpl[] perms = new PSAccessLevelImpl[resultsLocked[0]
            .getEntries()[0].getPermissions().length + 1];
         System.arraycopy(resultsLocked[0].getEntries()[0].getPermissions(), 0,
            perms, 0, resultsLocked[0].getEntries()[0].getPermissions().length);
         newPerm = new PSAccessLevelImpl();
         newPerm.setAclEntryId(resultsLocked[0].getEntries()[0].getId());
         newPerm.setPermission(PSPermissions.READ.getOrdinal());
         newPerm.setId(-1);
         perms[perms.length - 1] = newPerm;
         resultsLocked[0].getEntries()[0].setPermissions(perms);
         saveAcls(resultsLocked, m_session, false);
         PSAclImpl[] newResults = loadAcls(ids, m_session, true);
         compareAclArrays(resultsLocked, newResults, false);

         // test remove
         resultsLocked = newResults;
         perms = new PSAccessLevelImpl[resultsLocked[0].getEntries()[0]
            .getPermissions().length - 1];
         // remove a perm that's not owner
         boolean didSkip = false;
         int iNew = 0;
         for (int i = 0; i < perms.length + 1; i++)
         {
            if (!didSkip
               && resultsLocked[0].getEntries()[0].getPermissions()[iNew]
                  .getPermission() != PSPermissions.OWNER.getOrdinal())
            {
               didSkip = true;
               continue;
            }
            perms[iNew] = resultsLocked[0].getEntries()[0].getPermissions()[i];
            iNew++;
         }
         resultsLocked[0].getEntries()[0].setPermissions(perms);

         // remove an entry that's not the owner
         entryLen = resultsLocked[0].getEntries().length - 1;
         newEntries = new PSAclEntryImpl[entryLen];
         didSkip = false;
         iNew = 0;
         for (int i = 0; i < newEntries.length + 1; i++)
         {
            if (!didSkip
               && !resultsLocked[0].getEntries()[iNew].getName().equals(
                  "admin1"))
            {
               didSkip = true;
               continue;
            }
            newEntries[iNew] = resultsLocked[0].getEntries()[i];
            iNew++;
         }
         resultsLocked[0].setEntries(newEntries);

         saveAcls(resultsLocked, m_session, false);
         newResults = loadAcls(ids, m_session, true);
         compareAclArrays(resultsLocked, newResults, false);

         return newResults;
      }
      catch (com.percussion.webservices.faults.PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }

   }

   /**
    * Calls {@link #loadAcls(long[], String, boolean, String, String)
    * loadAcls(ids, session, lock, null, null)}
    */
   private PSAclImpl[] loadAcls(long[] ids, String session, boolean lock)
      throws Exception
   {
      return loadAcls(ids, session, lock, null, null);
   }

   /**
    * Load the specified acls.
    * 
    * @param ids The ids of the object for which acls are to be loaded, assumed 
    * not <code>null</code>.
    * @param session The session to use, may be <code>null</code> or empty, 
    * ignored if <code>user</code> is not <code>null</code>.
    * @param lock <code>true</code> to load them locked, <code>false</code> if
    * not.
    * @param user Optional user id to use, may be <code>null</code> or emtpy. 
    * @param pwd Optional password to use, may be <code>null</code> or empty,
    * ignored if <code>user</code> is <code>null</code>.
    *  
    * @return The loaded acls, never <code>null</code>.
    * 
    * @throws Exception if the load fails.
    */
   private PSAclImpl[] loadAcls(long[] ids, String session, boolean lock,
      String user, String pwd) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
      {
         session = PSTestUtils.login(user, pwd);
      }

      if (session != null)
      {
         PSTestUtils.setSessionHeader(binding, session);
      }

      LoadAclsRequest req = new LoadAclsRequest();
      req.setId(ids);
      req.setLock(lock);
      PSAclImpl[] acls = binding.loadAcls(req);

      return acls;
   }

   /**
    * Test deleting the acls
    * 
    * @param acls The acls to delete, assumed locked.
    * 
    * @throws Exception if the test fails.
    */
   private void doDeleteAclsTest(PSAclImpl[] acls) throws Exception
   {
      long[] ids = getAclIds(acls);

      try
      {
         // test no session
         try
         {
            deleteAcls(ids, null, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            deleteAcls(ids, "nosuchsession", true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            deleteAcls(null, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            deleteAcls(new long[0], m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // delete and search for results
         deleteAcls(ids, m_session, true);
         long[] objectIds = getObjectIds(acls);

         try
         {
            loadAcls(objectIds, m_session, false);
         }
         catch (PSErrorResultsFault e)
         {
            // expected exception
            PSErrorResultsFaultServiceCall[] calls = e.getServiceCall();
            assertEquals(calls.length, objectIds.length);
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorResultsFaultServiceCall call = calls[i];

               assertTrue(call.getResult() == null);
               assertTrue(call.getError() != null);
               assertTrue(call.getError().getPSError() != null);
               assertEquals(IPSWebserviceErrors.OBJECT_NOT_FOUND, call
                  .getError().getPSError().getCode());
            }
         }
         
         // try to delete again, should be noop w/out error
         deleteAcls(ids, m_session, true);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
   }

   /**
    * Get the acl ids from the supplied list.
    * 
    * @param acls The list, assumed not <code>null</code>.
    * 
    * @return the ids.
    */
   private long[] getAclIds(PSAclImpl[] acls)
   {
      long[] ids = new long[acls.length];
      for (int i = 0; i < ids.length; i++)
      {
         ids[i] = acls[i].getId();
      }
      return ids;
   }

   /**
    * Tests all item filter design CRUD services.
    * 
    * @throws Exception for any error.
    */
   @Test
   public void testSystemDesignItemFiltersCRUD() throws Exception
   {
      // create filters
      List<PSItemFilter> filters = doCreateItemFiltersTest();

      // save created filters
      doSaveItemFiltersTest(filters, true);

      // connect parent - child
      long[] ids = itemFiltersListToIdArray(filters);
      loadItemFilters(ids, m_session, true);
      PSItemFilter parent = null;
      PSItemFilter child = null;
      for (PSItemFilter filter : filters)
      {
         if (filter.getName().indexOf("testparent") != -1)
            parent = filter;
         else if (filter.getName().indexOf("testchild") != -1)
            child = filter;
      }
      assertTrue(parent != null && child != null);
      child.setParentFilterId(parent.getId());
      saveItemFilters(filters, m_session, true);

      // find filters
      doFindItemFiltersTest(filters);

      // load filters
      filters = doLoadItemFiltersTest(ids);

      // modify and save
      for (PSItemFilter filter : filters)
         filter.setDescription(filter.getDescription() + " - test");
      doSaveItemFiltersTest(filters, true);

      // delete filters
      doDeleteItemFiltersTest(ids);
   }

   /**
    * Tests creating item filters.
    * 
    * @return the list of created test filters, never <code>null</code> or 
    *    empty.
    * @throws Exception if the test fails.
    */
   private List<PSItemFilter> doCreateItemFiltersTest() throws Exception
   {
      // test no session
      try
      {
         createItemFilter("parent", 0, 0, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         createItemFilter("parent", 0, 0, "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid name
      try
      {
         createItemFilter(null, 0, 0, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test invalid name
      try
      {
         createItemFilter(" ", 0, 0, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test dupe name
      try
      {
         createItemFilter("PREVIEW", 0, 0, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test valid operation
      try
      {
         return createTestItemFilters();
      }
      catch (PSInvalidSessionFault e)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e.dumpToString());
      }
      catch (PSContractViolationFault e)
      {
         throw new AssertionFailedError(
            "ContractViolationFault Exception caught: " + e.dumpToString());
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new AssertionFailedError("NotAuthorizedFault Exception caught: "
            + e.dumpToString());
      }
   }

   /**
    * Tests saving item filters.
    * 
    * @param filters the item filters to save, assumed not <code>null</code> 
    *    or empty and to have been locked.
    * @param release <code>true</code> leave the locks released when the test 
    *    is completed, <code>false</code> to leave the locks in place.
    * @throws Exception if the test fails.
    */
   private void doSaveItemFiltersTest(List<PSItemFilter> filters,
      boolean release) throws Exception
   {
      // test no session
      try
      {
         saveItemFilters(filters, null, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         saveItemFilters(filters, "nosuchsession", false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      try
      {
         saveItemFilters(new ArrayList<PSItemFilter>(), m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test save and load, compare results
      try
      {
         saveItemFilters(filters, m_session, true);

         // try to resave w/out lock
         try
         {
            saveItemFilters(filters, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }

         long[] ids = itemFiltersListToIdArray(filters);
         List<PSItemFilter> filters2 = loadItemFilters(ids, m_session, !release);
         assertTrue(filters.size() == filters2.size());
      }
      catch (PSErrorsFault e)
      {
         throw new AssertionFailedError("ErrorsFault Exception caught: "
            + e.dumpToString());
      }
      catch (PSInvalidSessionFault e)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e.dumpToString());
      }
      catch (PSContractViolationFault e)
      {
         throw new AssertionFailedError(
            "ContractViolationFault Exception caught: " + e.dumpToString());
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new AssertionFailedError("NotAuthorizedFault Exception caught: "
            + e.dumpToString());
      }
   }

   /**
    * Test the <code>findItemFilters</code> webservice.
    * 
    * @param filters the list of item filters to find, assumed not 
    *    <code>null</code>.
    * @throws Exception if there are any errors.
    */
   private void doFindItemFiltersTest(List<PSItemFilter> filters)
      throws Exception
   {
      // test no session
      try
      {
         findItemFilters(null, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         findItemFilters(null, "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      try
      {
         // test nulls, empty  
         validateItemFilterSummaries(findItemFilters(null, m_session), filters);
         validateItemFilterSummaries(findItemFilters("", m_session), filters);

         // test wildcards
         validateItemFilterSummaries(findItemFilters("Test*", m_session),
            filters);

         // test literal names
         for (PSItemFilter filter : filters)
         {
            PSObjectSummary[] summaries = findItemFilters(filter.getName(),
               m_session);
            assertTrue(summaries.length == 1);
            assertEquals(summaries[0].getName(), filter.getName());
         }
      }
      catch (PSInvalidSessionFault e)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   /**
    * Tests loading item filters.
    * 
    * @param ids the ids of the item filters to load, assumed not 
    *    <code>null</code> or empty and to have been saved.
    * @return the loaded item filters, never <code>null</code> or empty.
    * @throws Exception if the test fails.
    */
   private List<PSItemFilter> doLoadItemFiltersTest(long[] ids)
      throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            loadItemFilters(ids, null, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            loadItemFilters(ids, "nosuchsession", false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            loadItemFilters(null, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            loadItemFilters(new long[] {}, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }
         
         // try to load filters with invalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 1000;

            loadItemFilters(invalidIds, m_session, false);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1, PSItemFilter.class.getName());
         }

         // load locked
         List<PSItemFilter> resultsLocked = loadItemFilters(ids, m_session,
            true);
         assertTrue(resultsLocked.size() == ids.length);
         long[] lockedIds = itemFiltersListToIdArray(resultsLocked);
         for (int i = 0; i < ids.length; i++)
            assertEquals(ids[i], lockedIds[i]);

         // now try as another user
         try
         {
            loadItemFilters(ids, null, true, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSItemFilter.class.getName());
         }

         // load unlocked
         PSTestUtils.releaseLocks(m_session, ids);
         List<PSItemFilter> resultsUnlocked = loadItemFilters(ids, m_session,
            false);
         assertEquals(resultsLocked, resultsUnlocked);

         // now load as another user unlocked
         loadItemFilters(ids, null, false, "admin2", "demo");

         // now leave locked
         return loadItemFilters(ids, m_session, true);
      }
      catch (PSErrorResultsFault e)
      {
         throw new AssertionFailedError("ErrorResultsFault Exception caught: "
            + e);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new AssertionFailedError("NotAuthorizedFault Exception caught: "
            + e);
      }
   }

   /**
    * Tests deleting item filters.
    * 
    * @param ids the ids of the item filters to delete, assumed not 
    *    <code>null</code> or empty and to reference valid item filters, 
    *    which may or may not be locked.
    * @throws Exception if the test fails.
    */
   private void doDeleteItemFiltersTest(long[] ids) throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            deleteItemFilters(ids, null, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            deleteItemFilters(ids, "nosuchsession", true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            deleteItemFilters(null, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            deleteItemFilters(new long[] {}, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // lock objects for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         loadItemFilters(ids, session2, true);

         try
         {
            deleteItemFilters(ids, m_session, true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }

         // release, delete rule, save and verify deleted rule
         PSTestUtils.releaseLocks(session2, ids);
         List<PSItemFilter> filters = loadItemFilters(ids, m_session, true);
         assertTrue(filters.size() == 2);
         PSItemFilter filter = filters.get(0);
         PSFilterRule[] rules = filter.getRules();
         assertTrue(rules.length == 1);
         filter.setRules(new PSFilterRule[0]);
         saveItemFilters(filters, m_session, true);
         filters = loadItemFilters(ids, m_session, false);
         assertTrue(filters.size() == 2);
         assertTrue(filters.get(0).getRules().length == 1);

         // delete and search for results
         deleteItemFilters(ids, m_session, true);

         Set<Long> idSet = new HashSet<Long>();
         for (long id : ids)
            idSet.add(id);

         PSObjectSummary[] sums = findItemFilters(null, m_session);
         for (PSObjectSummary sum : sums)
            assertFalse(idSet.contains(sum.getId()));
      }
      catch (PSInvalidSessionFault e)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
      catch (PSErrorsFault e)
      {
         throw new AssertionFailedError("ErrorsFault Exception caught: " + e);
      }
      catch (PSContractViolationFault e)
      {
         throw new AssertionFailedError(
            "ContractViolationFault Exception caught: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new AssertionFailedError("NotAuthorizedFault Exception caught: "
            + e);
      }
   }

   /**
    * Validates that the supplied array of item filter summaries contains the 
    * expected item filters to be found.
    * 
    * @param summaries the item filter summaries to validate, assumed not 
    *    <code>null</code>.
    * @param filters the expected item filters, assumed not <code>null</code>.
    */
   private void validateItemFilterSummaries(PSObjectSummary[] summaries,
      List<PSItemFilter> filters)
   {
      Map<String, PSObjectSummary> map = new HashMap<String, PSObjectSummary>();
      for (PSObjectSummary summary : summaries)
         map.put(summary.getName(), summary);

      for (PSItemFilter filter : filters)
      {
         PSObjectSummary test = map.get(filter.getName());
         assertNotNull(test);
         assertEquals(filter.getDescription(), test.getDescription());
      }
   }

   /**
    * Verifies that a user without design server ACL is not allowed to execute
    * create, load, delete and save services.
    * 
    * @throws Exception for any error.
    */
   @Test
   public void testServerAclAuthorization() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      PSAclEntry aclEntry = null;
      try
      {
         aclEntry = addServerAcl();

         String session = PSTestUtils.login("editor1", "demo");
         PSTestUtils.setSessionHeader(binding, session);
         
         // verify editor1 has no design create access
         try
         {
            CreateRelationshipTypesRequest request = 
               new CreateRelationshipTypesRequest(
                  new String[] { "SomeRelationshipType" }, 
                  new RelationshipCategory[] 
                     { RelationshipCategory.ActiveAssembly });
            binding.createRelationshipTypes(request);
           
            assertTrue("Should have thrown exception", false);
         }
         catch (PSNotAuthorizedFault e)
         {
            assertTrue("Expected", true);
         }
         
         // verify editor1 has find access
         FindRelationshipTypesRequest findRequest = 
            new FindRelationshipTypesRequest("ActiveAssembly", 
               RelationshipCategory.ActiveAssembly);
         PSObjectSummary[] relationshipTypes = binding.findRelationshipTypes(
            findRequest);
         assertTrue(relationshipTypes != null && relationshipTypes.length == 1);
         
         // verify editor1 has load access (can load but not lock)
         LoadRelationshipTypesRequest loadRequest = 
            new LoadRelationshipTypesRequest(
               new long[] { relationshipTypes[0].getId() }, false, false);
         PSRelationshipConfig[] configs = binding.loadRelationshipTypes(
            loadRequest);
         assertTrue(configs != null && configs.length == 1);
         
         // verify editor1 has no design load access
         try
         {
            LoadRelationshipTypesRequest request = 
               new LoadRelationshipTypesRequest(
                  new long[] { relationshipTypes[0].getId() }, true, true);
            binding.loadRelationshipTypes(request);
           
            assertTrue("Should have thrown exception", false);
         }
         catch (PSNotAuthorizedFault e)
         {
            assertTrue("Expected", true);
         }
         
         // verify editor1 has no design save access
         try
         {
            SaveRelationshipTypesRequest request = 
               new SaveRelationshipTypesRequest(configs, true);
            binding.saveRelationshipTypes(request);
           
            assertTrue("Should have thrown exception", false);
         }
         catch (PSNotAuthorizedFault e)
         {
            assertTrue("Expected", true);
         }
         
         // verify editor1 has no design delete access
         try
         {
            DeleteRelationshipTypesRequest request = 
               new DeleteRelationshipTypesRequest(
                  new long[] { relationshipTypes[0].getId() }, true);
            binding.deleteRelationshipTypes(request);
           
            assertTrue("Should have thrown exception", false);
         }
         catch (PSNotAuthorizedFault e)
         {
            assertTrue("Expected", true);
         }
         
         // verify editor1 can create guid's 
         CreateGuidsRequest createRequest = new CreateGuidsRequest(
            PSTypeEnum.RELATIONSHIP.ordinal(), 1);
         long[] guids = binding.createGuids(createRequest);
         assertTrue(guids != null && guids.length == 1);
      }
      finally
      {
         removeServerAcl(aclEntry);
      }
   }
}
