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

import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSAssemblyTestBase;
import com.percussion.webservices.PSContentTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.TemplateType;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.content.PSAutoTranslation;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.webservices.content.PSContentEditorDefinitionType;
import com.percussion.webservices.content.PSContentTemplateDesc;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.content.PSKeyword;
import com.percussion.webservices.content.PSLocale;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.DeleteKeywordsRequest;
import com.percussion.webservices.contentdesign.FindKeywordsRequest;
import com.percussion.webservices.contentdesign.LoadKeywordsRequest;
import com.percussion.webservices.contentdesign.LoadSharedDefinitionRequest;
import com.percussion.webservices.contentdesign.LoadSharedDefinitionResponse;
import com.percussion.webservices.contentdesign.LoadSystemDefinitionRequest;
import com.percussion.webservices.contentdesign.LoadSystemDefinitionResponse;
import com.percussion.webservices.contentdesign.SaveKeywordsRequest;
import com.percussion.webservices.contentdesign.SaveSharedDefinitionRequest;
import com.percussion.webservices.contentdesign.SaveSystemDefinitionRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallError;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test case for all content design web services.
 */
@Category(IntegrationTest.class)
public class ContentDesignTestCase extends PSContentTestBase
{
  @BeforeClass
   public static void setup() throws Exception
   {
      deleteTestKeywords();

      try
      {
         createTestKeywords();
      }
      catch (Exception e)
      {
         // ignore, tests will fail
      }
   }

   @AfterClass
   protected static void tearDown() throws Exception
   {
      deleteTestKeywords();
   }


   @Test
   public void testCreateKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         String[] request = null;

         // try to get create a keyword without rhythmyx session
         try
         {
            request = new String[] { "keyword_test" };
            binding.createKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a keyword with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new String[] { "keyword_test" };
            binding.createKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to create a node with an empty names
         try
         {
            request = new String[0];
            binding.createKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create keyword
         request = new String[] { "keyword_test" };
         PSKeyword[] keywords = binding.createKeywords(request);
         assertTrue(keywords != null && keywords.length == 1);

         // try to create a keyword with an existing name
         try
         {
            request = new String[] { "keyword_0" };
            binding.createKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
      catch (PSContractViolationFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void testFindKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         FindKeywordsRequest request = null;
         PSObjectSummary[] keywords = null;

         // try to find all keywords without rhythmyx session
         try
         {
            request = new FindKeywordsRequest();
            request.setName(null);
            binding.findKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to find all keywords with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new FindKeywordsRequest();
            request.setName(null);
            binding.findKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // find all keywords
         request = new FindKeywordsRequest();
         request.setName(null);
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length > 0);
         // verify that no excludes are returned
         for (PSObjectSummary keyword : keywords)
            assertTrue(!keyword.getLabel().equals("Lookup Types"));
         int count = keywords.length;

         request = new FindKeywordsRequest();
         request.setName(" ");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == count);

         request = new FindKeywordsRequest();
         request.setName("*");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == count);

         // try to find a non-existing node
         request = new FindKeywordsRequest();
         request.setName("somekeyword");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == 0);

         // find unlocked test keywords
         request = new FindKeywordsRequest();
         request.setName("keyword_0");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == 1);
         assertTrue(keywords[0].getLocked() == null);

         request = new FindKeywordsRequest();
         request.setName("KEYWORD_0");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == 1);

         request = new FindKeywordsRequest();
         request.setName("keyword_*");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == 3);

         // lock test nodes
         long[] lockIds = new long[keywords.length];
         for (int i = 0; i < keywords.length; i++)
            lockIds[i] = keywords[i].getId();
         lockKeywords(lockIds, session);

         // find locked test keywords
         request = new FindKeywordsRequest();
         request.setName("keyword_0");
         keywords = binding.findKeywords(request);
         assertTrue(keywords != null && keywords.length == 1);
         assertTrue(keywords[0].getLocked() != null);

         // release locked objects
         PSTestUtils.releaseLocks(session, lockIds);
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
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   @Test
   public void testLoadKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = getTestKeywordIds();

         LoadKeywordsRequest request = null;
         PSKeyword[] keywords = null;

         // try to load keywords without rhythmyx session
         try
         {
            request = new LoadKeywordsRequest();
            request.setId(ids);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load keywords with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadKeywordsRequest();
            request.setId(ids);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to load keywords with null ids
         try
         {
            request = new LoadKeywordsRequest();
            request.setId(null);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load keywords with empty ids
         try
         {
            request = new LoadKeywordsRequest();
            request.setId(new long[0]);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load keywords with only invalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i] + 1000;

            request = new LoadKeywordsRequest();
            request.setId(invalidIds);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            // expected exception
            PSErrorResultsFaultServiceCall[] calls = e.getServiceCall();
            for (int i=0; i<calls.length; i++)
            {
               PSErrorResultsFaultServiceCall call = calls[i];
               boolean expectError = true;

               if (expectError)
               {
                  assertTrue(call.getResult() == null);
                  assertTrue(call.getError() != null);
               }
               else
               {
                  assertTrue(call.getResult() != null);
                  assertTrue(call.getError() == null);
               }
            }
         }

         // try to load keywords with first id invalid
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[0] = invalidIds[0] + 100;

            request = new LoadKeywordsRequest();
            request.setId(invalidIds);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            // expected exception
            verifyErrorResultsFault(e, 0, PSKeyword.class.getName());
         }

         // try to load keywords with middle id invalid
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[1] = invalidIds[1] + 100;

            request = new LoadKeywordsRequest();
            request.setId(invalidIds);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            // expected exception
            verifyErrorResultsFault(e, 1, PSKeyword.class.getName());
         }

         // try to load keywords with last id invalid
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 100;

            request = new LoadKeywordsRequest();
            request.setId(invalidIds);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1, PSKeyword.class.getName());
         }

         // load keywords read-only
         request = new LoadKeywordsRequest();
         request.setId(ids);
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 3);

         // load keywords read-writable
         request = new LoadKeywordsRequest();
         request.setId(ids);
         request.setLock(true);
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 3);

         // reload locked keywords read-writable with locking session
         request = new LoadKeywordsRequest();
         request.setId(ids);
         request.setLock(true);
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 3);

         // try to load locked keywords read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadKeywordsRequest();
            request.setId(ids);
            request.setLock(true);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSKeyword.class.getName());
         }

         // release locked objects
         PSTestUtils.releaseLocks(session, ids);
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
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   @Test
   public void testSaveKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         PSKeyword[] keywords = getTestKeywords();

         SaveKeywordsRequest request = null;

         // try to save keywords without rhythmyx session
         try
         {
            request = new SaveKeywordsRequest();
            request.setPSKeyword(keywords);
            binding.saveKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save keywords with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new SaveKeywordsRequest();
            request.setPSKeyword(keywords);
            binding.saveKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save keywords with null nodes
         try
         {
            request = new SaveKeywordsRequest();
            request.setPSKeyword(null);
            binding.saveKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save keywords with empty nodes
         try
         {
            request = new SaveKeywordsRequest();
            request.setPSKeyword(new PSKeyword[0]);
            binding.saveKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save keywords in read-only mode
         try
         {
            request = new SaveKeywordsRequest();
            request.setPSKeyword(keywords);
            binding.saveKeywords(request);
            assertFalse("Should have thrown exception", true);
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

         // lock keywords
         long[] lockIds = getTestKeywordIds();
         lockKeywords(lockIds, session);

         // save locked keywords, do not release
         request = new SaveKeywordsRequest();
         request.setPSKeyword(keywords);
         request.setRelease(false);
         binding.saveKeywords(request);

         // save locked keywords and release
         request = new SaveKeywordsRequest();
         request.setPSKeyword(keywords);
         request.setRelease(true);
         binding.saveKeywords(request);

         // try to save keywords in read-only mode
         try
         {
            request = new SaveKeywordsRequest();
            request.setPSKeyword(keywords);
            binding.saveKeywords(request);
            assertFalse("Should have thrown exception", true);
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
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   @Test
   public void testDeleteKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = getTestKeywordIds();

         DeleteKeywordsRequest request = null;

         // try to delete keywords without rhythmyx session
         try
         {
            request = new DeleteKeywordsRequest();
            request.setId(ids);
            binding.deleteKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete keywords with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new DeleteKeywordsRequest();
            request.setId(ids);
            binding.deleteKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to delete keywords with null ids
         try
         {
            request = new DeleteKeywordsRequest();
            request.setId(null);
            binding.deleteKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete keywords with empty ids
         try
         {
            request = new DeleteKeywordsRequest();
            request.setId(new long[0]);
            binding.deleteKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // lock objects for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         lockKeywords(ids, session2);

         // try to delete objects locked by somebody else
         try
         {
            request = new DeleteKeywordsRequest();
            request.setId(ids);
            binding.deleteKeywords(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            assertTrue(calls != null && calls.length == 3);
            for (PSErrorsFaultServiceCall call : calls)
            {
               PSErrorsFaultServiceCallError error = call.getError();
               assertTrue(error != null);
            }
         }

         // release locked objects
         PSTestUtils.releaseLocks(session2, ids);

         // delete keywords
         request = new DeleteKeywordsRequest();
         request.setId(ids);
         binding.deleteKeywords(request);
         assertTrue(getTestKeywordIds().length == 0);

         // delete non-existing keywords
         request = new DeleteKeywordsRequest();
         request.setId(ids);
         binding.deleteKeywords(request);
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
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   @Test
   public void testLocalesCRUD() throws Exception
   {
      cleanupTestLocales();
      
      List<PSLocale> locales = doCreateLocalesTest();
      doSaveLocalesTest(locales, true);
      doFindLocalesTest(locales);

      long[] ids = localesListToIdArray(locales);
      locales = doLoadLocalesTest(ids);

      // modify and save
      for (PSLocale locale : locales)
      {
         locale.setDescription(locale.getDescription() + " - test");
      }

      doSaveLocalesTest(locales, true);

      doDeleteLocalesTest(ids);
   }

   @Test
   public void testAutoTranslationsCRUD() throws Exception
   {
      List<PSAutoTranslation> newTrans = createTestTranslations();
      List<PSAutoTranslation> ats = new ArrayList<PSAutoTranslation>();
      ats.addAll(newTrans);

      // test no session
      try
      {
         saveAutoTranslations(ats, null, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         saveAutoTranslations(ats, "nosuchsession", false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test no session
      try
      {
         loadAutoTranslations(null, true, null, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         loadAutoTranslations("nosuchsession", true, null, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test save unlocked
      try
      {
         saveAutoTranslations(ats, m_session, true);
         assertTrue("Should have thrown", false);
      }
      catch (PSLockFault e)
      {
         // expected exception
      }

      // load current locked in case we have some
      List<PSAutoTranslation> curTrans = loadAutoTranslations(m_session, true,
         null, null);

      ats.addAll(curTrans);

      boolean success = false;
      try
      {
         // save
         saveAutoTranslations(ats, m_session, false);
         assertEquals(ats, loadAutoTranslations(m_session, true, null, null));

         // test modify
         for (PSAutoTranslation at : newTrans)
         {
            at.setCommunityId(at.getCommunityId() + 100);
            at.setWorkflowId(at.getWorkflowId() + 100);
         }
         saveAutoTranslations(ats, m_session, true);
         assertEquals(ats, loadAutoTranslations(m_session, true, null, null));

         // now try as another user
         try
         {
            loadAutoTranslations(null, true, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSLockFault e)
         {
            // expected
         }

         // test remove
         saveAutoTranslations(curTrans, m_session, false);
         assertEquals(curTrans, loadAutoTranslations(m_session, false, null,
            null));

         success = true;
      }
      finally
      {
         try
         {
            // load locked
            loadAutoTranslations(m_session, false, null, null);

            // save just what we had at start
            saveAutoTranslations(curTrans, m_session, true);
         }
         catch (Exception e)
         {
            if (!success)
            {
               // don't mask first error
               System.out.println("Failed to clean up test translations: "
                  + e.getLocalizedMessage());
            }
            else
            {
               throw (Exception) e.fillInStackTrace();
            }
         }
      }

   }

   /**
    * Tests deleting locales
    * 
    * @param ids The ids of the locales to delete, assumed not <code>null</code> 
    * or empty and to reference valid locales, which may or may not be 
    * locked.
    * 
    * @throws Exception if the test fails.
    */
   private void doDeleteLocalesTest(long[] ids) throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            deleteLocales(ids, null, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            deleteLocales(ids, "nosuchsession", true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            deleteLocales(null, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            deleteLocales(new long[] {}, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // lock objects for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         loadLocales(ids, session2, true);

         // try to delete objects
         try
         {
            deleteLocales(ids, m_session, true);
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

         // release, delete and search for results
         PSTestUtils.releaseLocks(session2, ids);
         deleteLocales(ids, m_session, true);

         Set<Long> idSet = new HashSet<Long>();
         for (long id : ids)
            idSet.add(id);

         PSObjectSummary[] sums = findLocales(null, null, m_session);
         for (PSObjectSummary sum : sums)
         {
            assertFalse(idSet.contains(sum.getId()));
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
   }

   /**
    * Tests loading locales.
    * 
    * @param ids The ids of the locales to load, assumed not 
    * <code>null</code> or empty and to have been saved.
    * 
    * @return the loaded locales, never <code>null</code> or empty.
    * 
    * @throws Exception if the test fails.
    */
   private List<PSLocale> doLoadLocalesTest(long[] ids) throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            loadLocales(ids, null, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            loadLocales(ids, "nosuchsession", false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            loadLocales(null, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            loadLocales(new long[] {}, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // load locked
         List<PSLocale> resultsLocked = loadLocales(ids, m_session, true);
         assertTrue(resultsLocked.size() == ids.length);
         long[] lockedIds = localesListToIdArray(resultsLocked);
         for (int i = 0; i < ids.length; i++)
         {
            assertEquals(ids[i], lockedIds[i]);
         }

         // now try as another user
         try
         {
            loadLocales(ids, null, true, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSLocale.class.getName());
         }

         // load unlocked
         PSTestUtils.releaseLocks(m_session, ids);
         List<PSLocale> resultsUnlocked = loadLocales(ids, m_session, false);

         assertEquals(resultsLocked, resultsUnlocked);

         // now load as another user unlocked
         loadLocales(ids, null, false, "admin2", "demo");

         // now leave locked
         return loadLocales(ids, m_session, true);
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
    * Tests creating locales.
    * 
    * @return The list of created test locales, never <code>null</code> or 
    * empty.
    * 
    * @throws Exception If the test fails.
    */
   private List<PSLocale> doCreateLocalesTest() throws Exception
   {
      // test no session
      try
      {
         createLocale("fr-ca", "French Canadian", null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         createLocale("fr-ca", "French Canadian", "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // now use authenticated session

      //test invalid name
      try
      {
         createLocale("", "French Canadian", m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      //test invalid lable
      try
      {
         createLocale("fr-ca", "", m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test dupe name
      try
      {
         createLocale("en-us", "US English", m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // Test valid operation
      try
      {
         return createTestLocales();
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1.dumpToString());
      }
      catch (PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2.dumpToString());
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3.dumpToString());
      }
   }

   /**
    * Tests saving locales.
    * 
    * @param locales The locales to save, assumed not <code>null</code> or empty 
    * and to have been locked.
    * @param release <code>true</code> leave the locks released when the test is
    * completed, <code>false</code> to leave the locks in place.
    *  
    * @throws Exception if the test fails.
    */
   private void doSaveLocalesTest(List<PSLocale> locales, boolean release)
      throws Exception
   {
      // test no session
      try
      {
         saveLocales(locales, null, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         saveLocales(locales, "nosuchsession", false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test bad input
      try
      {
         saveLocales(null, m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      try
      {
         saveLocales(new ArrayList<PSLocale>(), m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test save and load, compare results
      try
      {
         saveLocales(locales, m_session, true);

         // try to resave w/out lock
         try
         {
            saveLocales(locales, m_session, true);
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

         long[] ids = localesListToIdArray(locales);
         List<PSLocale> locales2 = loadLocales(ids, m_session, !release);
         assertEquals(locales, locales2);
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
    * Test the <code>findLocales</code> webservice.
    * 
    * @param locales The list of locales to find, assumed not 
    * <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private void doFindLocalesTest(List<PSLocale> locales) throws Exception
   {
      // test no session
      try
      {
         findLocales(null, null, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         findLocales(null, null, "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      try
      {
         // test nulls, empty  
         validateLocaleSummaries(findLocales(null, null, m_session), locales);
         validateLocaleSummaries(findLocales(null, "", m_session), locales);
         validateLocaleSummaries(findLocales("", null, m_session), locales);
         validateLocaleSummaries(findLocales("", "", m_session), locales);

         // test wildcards
         validateLocaleSummaries(findLocales(null, "Test*", m_session), locales);

         // test literal names
         for (PSLocale locale : locales)
         {
            PSObjectSummary[] sums;
            sums = findLocales(locale.getCode(), locale.getLabel(), m_session);
            assertTrue(sums.length == 1);
            assertEquals(sums[0].getName(), locale.getCode());

            sums = findLocales(null, locale.getLabel(), m_session);
            assertTrue(sums.length == 1);
            assertEquals(sums[0].getName(), locale.getCode());

            sums = findLocales(locale.getCode(), null, m_session);
            assertTrue(sums.length == 1);
            assertEquals(sums[0].getName(), locale.getCode());
         }

      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
   }

   /**
    * Validates that the supplied array of locale summaries contains the 
    * expected locales to be found.
    * 
    * @param sums The locale summaries to validate, assumed not 
    * <code>null</code>.
    * @param localesToFind The expected locales, assumed not <code>null</code>.
    */
   private void validateLocaleSummaries(PSObjectSummary[] sums,
      List<PSLocale> localesToFind)
   {
      Map<String, PSObjectSummary> map = new HashMap<String, PSObjectSummary>();
      for (PSObjectSummary sum : sums)
      {
         map.put(sum.getName(), sum);
      }

      for (PSLocale locale : localesToFind)
      {
         PSObjectSummary test = map.get(locale.getCode());
         assertNotNull(test);
         assertEquals(locale.getLabel(), test.getLabel());
         assertEquals(locale.getDescription(), test.getDescription());
      }
   }

   /**
    * Tests all CRUD services for content types design services.
    * 
    * @throws Exception If there are any errors.
    */
   @Test
   public void testContentTypesCRUD() throws Exception
   {
      boolean saved = false;
      List<PSContentType> ctypes = null;
      try
      {
         ctypes = doCreateContentTypesTest();
         doSaveContentTypesTest(ctypes, true);
         saved = true;
         doFindContentTypesTest(ctypes);

         long[] ids = ctypesListToIdArray(ctypes);
         doAssociatedTemplatesCRUDTest(ids);
         ctypes = doLoadContentTypesTest(ids);

         for (PSContentType ctype : ctypes)
         {
            ctype.setDescription(ctype.getDescription() + " - test");
         }

         doSaveContentTypesTest(ctypes, true);

         doDeleteContentTypesTest(ids);
         saved = false;
      }
      finally
      {
         if (saved)
         {
            try
            {
               loadContentTypes(ctypesListToIdArray(ctypes), m_session, true);
               deleteContentTypes(ctypesListToIdArray(ctypes), m_session);
            }
            catch (Exception e)
            {
               // we only do this if there's another error, so just log this 
               // error
               System.out.println("Failed to clean up content types: "
                  + e.getLocalizedMessage());
            }

         }
      }
   }

   /**
    * Test the <code>findContentTypes</code> webservice.
    * 
    * @param typesToFind The list of content types to find, assumed not 
    * <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private void doFindContentTypesTest(List<PSContentType> typesToFind)
      throws Exception
   {
      // test no session
      try
      {
         findContentTypes("test*", null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         findContentTypes("test*", "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      try
      {
         // test null name
         validateFindContentTypes(typesToFind, null);

         // test empty name
         Set<String> names = new HashSet<String>();
         names.add("");
         validateFindContentTypes(typesToFind, null);

         // test wildcards
         names.clear();
         for (PSContentType type : typesToFind)
         {
            String name = type.getName();
            names.add(name.substring(0, name.length() - 1) + "*");
         }
         validateFindContentTypes(typesToFind, names);

         // test literal names
         names.clear();
         for (PSContentType type : typesToFind)
         {
            names.add(type.getName());
         }
         validateFindContentTypes(typesToFind, names);

      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
   }

   /**
    * Creates a map of content type id to object summary from the supplied 
    * array.
    * 
    * @param sums The array of summaries, assumed not <code>null</code>.
    * 
    * @return The map, where the key is the type id, and the value is the 
    * summary, never <code>null</code>.
    */
   private Map<Long, PSObjectSummary> getSummaryMap(PSObjectSummary[] sums)
   {
      Map<Long, PSObjectSummary> sumMap = new HashMap<Long, PSObjectSummary>();

      for (PSObjectSummary sum : sums)
      {
         sumMap.put(sum.getId(), sum);
      }
      return sumMap;
   }

   /**
    * Test the find content types service using the supplied names and 
    * validating against the list of types to find.
    * 
    * @param typesToFind The list of types that are expected to be included in 
    * the results, assumed not <code>null</code>.
    * @param names The names to search for, may be <code>null</code> or empty to
    * find all possibly content types.
    * 
    * @throws Exception if there are any errors.
    */
   private void validateFindContentTypes(List<PSContentType> typesToFind,
      Set<String> names) throws Exception
   {
      Map<Long, PSObjectSummary> sumMap = new HashMap<Long, PSObjectSummary>();
      if (names == null)
      {
         sumMap.putAll(getSummaryMap(findContentTypes(null, m_session)));
      }
      else
      {
         for (String name : names)
         {
            sumMap.putAll(getSummaryMap(findContentTypes(name, m_session)));
         }
      }

      for (PSContentType type : typesToFind)
      {
         PSObjectSummary sum = sumMap.get(type.getId());
         assertNotNull(sum);
         assertEquals(sum.getName(), type.getName());
         assertEquals(sum.getLabel(), type.getLabel());
         assertEquals(sum.getDescription(), type.getDescription());
         assertEquals(sum.getType(), PSTypeEnum.NODEDEF.name());
      }
   }

   /**
    * Tests creating content types.
    * 
    * @return The list of created content types, never <code>null</code> or 
    * empty.
    * 
    * @throws Exception If the test fails.
    */
   private List<PSContentType> doCreateContentTypesTest() throws Exception
   {
      // test no session
      try
      {
         createContentType("test", null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         createContentType("test", "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // now use authenticated session

      //test invalid name
      try
      {
         createContentType("", m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test dupe name
      try
      {
         createContentType("Folder", m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // Test valid operation
      try
      {
         return createTestContentTypes(m_session);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1.dumpToString());
      }
      catch (PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2.dumpToString());
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3.dumpToString());
      }
   }

   /**
    * Tests loading content types.
    * 
    * @param ids The ids of the content types to load, assumed not 
    * <code>null</code> or empty and to have been saved.
    * 
    * @return the loaded types, never <code>null</code> or empty, locked.
    * 
    * @throws Exception if the test fails.
    */
   private List<PSContentType> doLoadContentTypesTest(long[] ids)
      throws Exception
   {
      // Test operation
      try
      {
         // test no session
         try
         {
            loadContentTypes(ids, null, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            loadContentTypes(ids, "nosuchsession", false);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            loadContentTypes(null, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            loadContentTypes(new long[] {}, m_session, false);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // load locked
         List<PSContentType> resultsLocked = loadContentTypes(ids, m_session,
            true);
         assertTrue(resultsLocked.size() == ids.length);
         for (int i = 0; i < ids.length; i++)
         {
            assertEquals(ids[i], resultsLocked.get(i).getId().longValue());
         }

         // now try as another user
         try
         {
            loadContentTypes(ids, null, true, false, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSContentType.class.getName());
         }

         // load unlocked
         PSTestUtils.releaseLocks(m_session, ids);
         List<PSContentType> resultsUnlocked = loadContentTypes(ids, m_session,
            false);

         assertEquals(resultsLocked, resultsUnlocked);

         // now load as another user unlocked
         loadContentTypes(ids, null, false, "admin2", "demo");

         // now leave locked
         return loadContentTypes(ids, m_session, true);
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
    * Tests saving content types.
    * 
    * @param ctypes The types to save, assumed not <code>null</code> or empty 
    * and to have been locked.
    * @param release <code>true</code> leave the locks released when the test is
    * completed, <code>false</code> to leave the locks in place.
    *  
    * @throws Exception if the test fails.
    */
   private void doSaveContentTypesTest(List<PSContentType> ctypes,
      boolean release) throws Exception
   {
      // test no session
      try
      {
         saveContentTypes(ctypes, null, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         saveContentTypes(ctypes, "nosuchsession", false);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test bad input
      try
      {
         saveContentTypes(null, m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      try
      {
         saveContentTypes(new ArrayList<PSContentType>(), m_session, false);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test save and load, compare results
      try
      {
         saveContentTypes(ctypes, m_session, true);

         // try to resave w/out lock
         try
         {
            saveContentTypes(ctypes, m_session, true);
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

         long[] ids = ctypesListToIdArray(ctypes);
         List<PSContentType> ctypes2 = loadContentTypes(ids, m_session,
            !release);
         assertEquals(ctypes, ctypes2);
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
    * Creates templates and tests associating them with content types, leaves
    * the content types unlocked.
    * 
    * @param ids The content types to associate to, assumed not 
    * <code>null</code> or empty.
    * 
    * @throws Exception If the test fails.
    */
   private void doAssociatedTemplatesCRUDTest(long[] ids) throws Exception
   {
      List<PSAssemblyTemplate> templates = null;
      long[] templateIds = new long[3];
      boolean success = false;
      long contentTypeId = ids[0];
      long[] allIds = null;
      try
      {
         // load all to lock
         List<PSContentTemplateDesc> descList = loadTemplateAssociations(
            contentTypeId, m_session, true);

         // save tests
         // test no session
         try
         {
            saveTemplateAssociations(contentTypeId, templateIds, null, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
         }

         // test invalid session
         try
         {
            saveTemplateAssociations(contentTypeId, templateIds, "foo", true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
         }

         // test invalid content typeid
         try
         {
            saveTemplateAssociations(-1, templateIds, m_session, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
         }

         // test invalid template Ids
         try
         {
            long[] badTemplateIds = new long[] { -1 };
            saveTemplateAssociations(contentTypeId, badTemplateIds, m_session,
               true);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
         }

         // create test templates
         templates = createTemplates(templateIds.length);
         for (int i = 0; i < templateIds.length; i++)
         {
            templateIds[i] = templates.get(i).getId();
         }

         // save them
         for (long id : ids)
         {
            // load to lock
            loadTemplateAssociations(id, m_session, true);
            saveTemplateAssociations(id, templateIds, m_session, true);
         }

         // try to resave w/out lock
         try
         {
            saveTemplateAssociations(contentTypeId, templateIds, m_session,
               true);
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

         // test save w/ adds and removes
         descList = loadTemplateAssociations(contentTypeId, m_session, true);
         validateTemplateAssociations(contentTypeId, templateIds, descList);
         descList = loadTemplateAssociations(-1, m_session, false);
         validateTemplateAssociations(ids, templateIds, descList);

         long[] subset = new long[templateIds.length - 1];
         System.arraycopy(templateIds, 0, subset, 0, subset.length);
         saveTemplateAssociations(contentTypeId, subset, m_session, false);
         descList = loadTemplateAssociations(contentTypeId, m_session, true);
         validateTemplateAssociations(contentTypeId, subset, descList);

         System.arraycopy(templateIds, 1, subset, 0, subset.length);
         saveTemplateAssociations(contentTypeId, subset, m_session, false);
         descList = loadTemplateAssociations(contentTypeId, m_session, true);
         validateTemplateAssociations(contentTypeId, subset, descList);

         // do other load testing
         // test no session
         try
         {
            loadTemplateAssociations(contentTypeId, null, true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
         }

         // test invalid session
         try
         {
            loadTemplateAssociations(contentTypeId, "nosuchsession", true);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
         }

         // load locked
         descList = loadTemplateAssociations(contentTypeId, m_session, true);

         // now try as another user
         try
         {
            loadTemplateAssociations(contentTypeId, m_session, true, "admin2",
               "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, 
               PSContentTemplateDesc.class.getName());
         }

         // load unlocked
         PSTestUtils.releaseLocks(m_session, new long[] { contentTypeId });
         List<PSContentTemplateDesc> resultsUnlocked = loadTemplateAssociations(
            contentTypeId, m_session, false);

         assertEquals(descList, resultsUnlocked);

         // load all locked
         PSObjectSummary[] summaries = findContentTypes("*", m_session);
         allIds = new long[summaries.length];
         for (int i=0; i<allIds.length; i++)
            allIds[i] = summaries[i].getId();
         descList = loadTemplateAssociations(-1, m_session, true);

         // now try all as another user
         try
         {
            loadTemplateAssociations(-1, m_session, true, "admin2", "demo");
            assertTrue("Should have thrown", false);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, 
               PSContentTemplateDesc.class.getName());
         }

         // load all unlocked
         PSTestUtils.releaseLocks(m_session, allIds);
         allIds = null;
         resultsUnlocked = loadTemplateAssociations(-1, m_session, false);

         // now load all as another user unlocked
         loadTemplateAssociations(-1, m_session, false, "admin2", "demo");

         // set flag for cleanup error handling
         success = true;
      }
      finally
      {
         try
         {
            if (allIds != null)
               PSTestUtils.releaseLocks(m_session, allIds);

            for (long id : ids)
            {
               // load locked
               loadTemplateAssociations(id, m_session, true);

               // remove the associations
               saveTemplateAssociations(id, null, m_session, false);
            }

            if (templates != null)
               deleteTemplates(templates);
         }
         catch (Exception e)
         {
            if (success)
               throw (Exception) e.fillInStackTrace();
            else
            {
               // result of an error, don't mask the real exception
               System.out.println("Failed to cleanup template-associations: "
                  + e.getLocalizedMessage());
            }
         }
      }
   }

   /**
    * Creates <code>count</code> templates and returns them. These templates
    * have been persisted.
    * 
    * @param count How many to make.
    * 
    * @return The created templates. Never <code>null</code>.
    * 
    * @throws Exception If any problems.
    */
   private List<PSAssemblyTemplate> createTemplates(int count)
      throws Exception
   {
      PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
      List<PSAssemblyTemplate> templates = new ArrayList<PSAssemblyTemplate>();
      for (int i = 0; i < count; i++)
      {
         PSAssemblyTemplate template = assemblyTest.createTemplate(
            "assocTest" + i, "assembler",  OutputFormatType.page, 
            TemplateType.shared, m_session); 
         templates.add(template);
      }
      return templates;
   }
   
   /**
    * Uses another class to permanently remove all templates in the supplied
    * set.
    * 
    * @param templates Assumed not <code>null</code>.
    * 
    * @throws Exception If any problems.
    */
   private void deleteTemplates(Collection<PSAssemblyTemplate> templates)
      throws Exception
   {
      PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
      for (PSAssemblyTemplate template : templates)
      {
         assemblyTest.deleteTemplate(template, m_session);
      }            
   }
   
   
   /**
    * Validates that the loaded templates associates match the expected list.
    *  
    * @param contentTypeId The content type id the templates are associated
    * with
    * @param templateIds The expected associated template ids, assumed not
    * <code>null</code> or empty.
    * @param descList The list of loaded associations, assumed not 
    * <code>null</code>.
    */
   private void validateTemplateAssociations(long contentTypeId,
      long[] templateIds, List<PSContentTemplateDesc> descList)
   {
      assertEquals(templateIds.length, descList.size());
      Set<Long> templateSet = new HashSet<Long>();

      for (PSContentTemplateDesc desc : descList)
      {
         assertEquals(contentTypeId, desc.getContentTypeId());
         templateSet.add(desc.getTemplateId());
      }

      for (long id : templateIds)
         assertTrue(templateSet.contains(id));
   }

   /**
    * Validates that the loaded templates associates match the expected list.
    *  
    * @param ids The content type ids the templates are associated
    * with
    * @param templateIds The expected associated template ids, assumed not
    * <code>null</code> or empty.
    * @param descList The list of loaded associations, assumed not 
    * <code>null</code>.
    */
   private void validateTemplateAssociations(long[] ids, long[] templateIds,
      List<PSContentTemplateDesc> descList)
   {
      assertTrue(descList.size() >= (ids.length * templateIds.length));
      Map<Long, List<Long>> resultMap = new HashMap<Long, List<Long>>();
      for (PSContentTemplateDesc desc : descList)
      {
         List<Long> templates = resultMap.get(desc.getContentTypeId());
         if (templates == null)
         {
            templates = new ArrayList<Long>();
            resultMap.put(desc.getContentTypeId(), templates);
         }
         templates.add(desc.getTemplateId());
      }

      for (long id : ids)
      {
         List<Long> templates = resultMap.get(id);
         assertNotNull(templates);
         assertEquals(templateIds.length, templates.size());
         for (long templateId : templateIds)
         {
            assertTrue(templates.contains(templateId));
         }
      }
   }

   /**
    * Tests deleting content types.
    * 
    * @param ids The ids of the types to delete, assumed not <code>null</code> 
    * or empty and to reference valid content types, which may or may not be 
    * locked.
    * 
    * @throws Exception if the test fails.
    */
   private void doDeleteContentTypesTest(long[] ids) throws Exception
   {
      // Test operation
      List<PSAssemblyTemplate> templates = null;
      try
      {
         // test no session
         try
         {
            deleteContentTypes(ids, null);
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         try
         {
            deleteContentTypes(ids, "nosuchsession");
            assertTrue("Should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad input
         try
         {
            deleteContentTypes(null, m_session);
            assertTrue("Should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            deleteContentTypes(new long[] {}, m_session);
            fail("Should have thrown");
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // lock objects for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         loadContentTypes(ids, session2, true);

         // try to delete objects
         try
         {
            deleteContentTypes(ids, m_session);
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

         // release, delete and search for results
         PSTestUtils.releaseLocks(session2, ids);
         
         //add 2 template associations to make sure they are handled properly
         templates = createTemplates(2);
         List<PSContentTemplateDesc> associations = 
            loadTemplateAssociations(ids[0], m_session, true);
         if (associations.size() == 0)
         {
            saveTemplateAssociations(ids[0], new long[] { templates.get(0)
                  .getId(), templates.get(1).getId() }, m_session, true);
         }
         
         deleteContentTypes(ids, m_session);

         Set<Long> idSet = new HashSet<Long>();
         for (long id : ids)
            idSet.add(id);

         PSObjectSummary[] sums = findContentTypes(null, m_session);
         for (PSObjectSummary sum : sums)
         {
            assertFalse(idSet.contains(sum.getId()));
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
      finally
      {
         if (templates != null)
            deleteTemplates(templates);
      }
   }

   @Test
   public void testLoadSystemDefinition() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         LoadSystemDefinitionRequest request = null;
         LoadSystemDefinitionResponse response = null;

         // try to load without rhythmyx session
         try
         {
            request = new LoadSystemDefinitionRequest();
            binding.loadSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new LoadSystemDefinitionRequest();
            binding.loadSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load system definition read-only
         request = new LoadSystemDefinitionRequest();
         response = binding.loadSystemDefinition(request);
         assertTrue(response != null);
         PSContentEditorDefinition def = response
            .getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.system));

         // load system definition read-writable
         request = new LoadSystemDefinitionRequest();
         request.setLock(true);
         response = binding.loadSystemDefinition(request);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.system));

         // reload locked system definition read-writable with locking session
         request = new LoadSystemDefinitionRequest();
         request.setLock(true);
         response = binding.loadSystemDefinition(request);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.system));

         // try to load locked system definition read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadSystemDefinitionRequest();
            request.setLock(true);
            binding.loadSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSLockFault e)
         {
            // expected exception
         }

         // release locked definition
         PSTestUtils.releaseLocks(session, new long[] { new PSDesignGuid(
            PSTypeEnum.CONFIGURATION, PSContentEditorSystemDef.SYSTEM_DEF_ID)
            .getValue() });
      }
      catch (PSLockFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "LockFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void testSaveSystemDefinition() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         SaveSystemDefinitionRequest request = null;

         PSContentEditorDefinition def = new PSContentEditorDefinition();
         def.setType(PSContentEditorDefinitionType.system);
         def.setDefinition("");

         // try to save without rhythmyx session
         try
         {
            request = new SaveSystemDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new SaveSystemDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save with null def
         try
         {
            request = new SaveSystemDefinitionRequest();
            request.setPSContentEditorDefinition(null);
            binding.saveSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // load system definition read-only
         LoadSystemDefinitionRequest loadRequest = new LoadSystemDefinitionRequest();
         LoadSystemDefinitionResponse response = binding
            .loadSystemDefinition(loadRequest);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.system));

         // try to save with read-only def
         try
         {
            request = new SaveSystemDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSLockFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // load system definition read-writable
         loadRequest = new LoadSystemDefinitionRequest();
         loadRequest.setLock(true);
         response = binding.loadSystemDefinition(loadRequest);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.system));

         // save def
         request = new SaveSystemDefinitionRequest();
         request.setRelease(false);
         request.setPSContentEditorDefinition(def);
         binding.saveSystemDefinition(request);

         // save def and release
         request = new SaveSystemDefinitionRequest();
         request.setRelease(true);
         request.setPSContentEditorDefinition(def);
         binding.saveSystemDefinition(request);

         // make sure its been released
         try
         {
            request = new SaveSystemDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSystemDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSLockFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSLockFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "LockFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void testLoadSharedDefinition() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         LoadSharedDefinitionRequest request = null;
         LoadSharedDefinitionResponse response = null;

         // try to load without rhythmyx session
         try
         {
            request = new LoadSharedDefinitionRequest();
            binding.loadSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new LoadSharedDefinitionRequest();
            binding.loadSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load shared definition read-only
         request = new LoadSharedDefinitionRequest();
         response = binding.loadSharedDefinition(request);
         assertTrue(response != null);
         PSContentEditorDefinition def = response
            .getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.shared));

         // load shared definition read-writable
         request = new LoadSharedDefinitionRequest();
         request.setLock(true);
         response = binding.loadSharedDefinition(request);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.shared));

         // reload shared system definition read-writable with locking session
         request = new LoadSharedDefinitionRequest();
         request.setLock(true);
         response = binding.loadSharedDefinition(request);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.shared));

         // try to load locked shared definition read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadSharedDefinitionRequest();
            request.setLock(true);
            binding.loadSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSLockFault e)
         {
            // expected exception
         }

         // release locked definition
         PSTestUtils.releaseLocks(session, new long[] { new PSDesignGuid(
            PSTypeEnum.CONFIGURATION, PSContentEditorSharedDef.SHARED_DEF_ID)
            .getValue() });
      }
      catch (PSLockFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "LockFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void testSaveSharedDefinition() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         SaveSharedDefinitionRequest request = null;

         PSContentEditorDefinition def = new PSContentEditorDefinition();
         def.setType(PSContentEditorDefinitionType.shared);
         def.setDefinition("");

         // try to save without rhythmyx session
         try
         {
            request = new SaveSharedDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new SaveSharedDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save with null def
         try
         {
            request = new SaveSharedDefinitionRequest();
            request.setPSContentEditorDefinition(null);
            binding.saveSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // load shared definition read-only
         LoadSharedDefinitionRequest loadRequest = new LoadSharedDefinitionRequest();
         LoadSharedDefinitionResponse response = binding
            .loadSharedDefinition(loadRequest);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.shared));

         // try to save with read-only def
         try
         {
            request = new SaveSharedDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSLockFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // load shared definition read-writable
         loadRequest = new LoadSharedDefinitionRequest();
         loadRequest.setLock(true);
         response = binding.loadSharedDefinition(loadRequest);
         assertTrue(response != null);
         def = response.getPSContentEditorDefinition();
         assertTrue(def != null
            && def.getType().equals(PSContentEditorDefinitionType.shared));

         // save def
         request = new SaveSharedDefinitionRequest();
         request.setRelease(false);
         request.setPSContentEditorDefinition(def);
         binding.saveSharedDefinition(request);

         // save def and release
         request = new SaveSharedDefinitionRequest();
         request.setRelease(true);
         request.setPSContentEditorDefinition(def);
         binding.saveSharedDefinition(request);

         // make sure its been relased
         try
         {
            request = new SaveSharedDefinitionRequest();
            request.setPSContentEditorDefinition(def);
            binding.saveSharedDefinition(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSLockFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSLockFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "LockFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }
}
