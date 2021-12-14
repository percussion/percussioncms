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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.webservices.PSSecurityTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.TemplateType;
import com.percussion.webservices.assemblydesign.AssemblyDesignSOAPStub;
import com.percussion.webservices.assemblydesign.DeleteAssemblyTemplatesRequest;
import com.percussion.webservices.assemblydesign.FindAssemblyTemplatesRequest;
import com.percussion.webservices.assemblydesign.SaveAssemblyTemplatesRequest;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallError;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSCommunityRolesRole;
import com.percussion.webservices.security.data.PSCommunityVisibility;
import com.percussion.webservices.securitydesign.DeleteCommunitiesRequest;
import com.percussion.webservices.securitydesign.FindCommunitiesRequest;
import com.percussion.webservices.securitydesign.FindRolesRequest;
import com.percussion.webservices.securitydesign.GetVisibilityByCommunityRequest;
import com.percussion.webservices.securitydesign.IsValidRhythmyxUserRequest;
import com.percussion.webservices.securitydesign.IsValidRhythmyxUserResponse;
import com.percussion.webservices.securitydesign.LoadCommunitiesRequest;
import com.percussion.webservices.securitydesign.SaveCommunitiesRequest;
import com.percussion.webservices.securitydesign.SecurityDesignSOAPStub;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SecurityDesignTestCase extends PSSecurityTestBase
{
   @Test
   public void test1securityDesignSOAPIsValidRhythmyxUser() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         IsValidRhythmyxUserRequest request = null;

         // try to use service without rhythmyx session
         try
         {
            request = new IsValidRhythmyxUserRequest("admin1");
            binding.isValidRhythmyxUser(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to use service with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new IsValidRhythmyxUserRequest("admin1");
            binding.isValidRhythmyxUser(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to use service with null username
         try
         {
            request = new IsValidRhythmyxUserRequest(null);
            binding.isValidRhythmyxUser(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to use service with empty username
         try
         {
            request = new IsValidRhythmyxUserRequest(null);
            binding.isValidRhythmyxUser(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // is admin1 a valid rhythmyx user?
         request = new IsValidRhythmyxUserRequest("admin1");
         IsValidRhythmyxUserResponse response = binding
            .isValidRhythmyxUser(request);
         assertTrue(response != null && response.isIsValid());

         // is QA2 a valid rhythmyx user?
         request = new IsValidRhythmyxUserRequest("QA2");
         response = binding.isValidRhythmyxUser(request);
         assertTrue(response != null && response.isIsValid());

         // is someuser a valid rhythmyx user?
         request = new IsValidRhythmyxUserRequest("someuser");
         response = binding.isValidRhythmyxUser(request);
         assertTrue(response != null && !response.isIsValid());
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void test2securityDesignSOAPCreateCommunity() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         // try to get create a community without rhythmyx session
         try
         {
            binding.createCommunities(new String[] { "Swiss French" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a community with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.createCommunities(new String[] { "Swiss French" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to create a community with a null name
         try
         {
            binding.createCommunities(new String[] { null });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a community with an empty name
         try
         {
            binding.createCommunities(new String[] { " " });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create community "Swiss French"
         PSCommunity testcreate = binding
            .createCommunities(new String[] { "Swiss French" })[0];

         assertTrue(testcreate != null);
         PSCommunity[] saveCommunities = new PSCommunity[1];
         saveCommunities[0] = testcreate;
         SaveCommunitiesRequest saveRequest = new SaveCommunitiesRequest();
         saveRequest.setPSCommunity(saveCommunities);
         binding.saveCommunities(saveRequest);

         // try to create a second community "Swiss French"
         try
         {
            binding.createCommunities(new String[] { "SWISS FRENCH" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void test3securityDesignSOAPFindCommunities() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         FindCommunitiesRequest request = null;
         PSObjectSummary[] communities = null;

         // try to find all communities without rhythmyx session
         try
         {
            request = new FindCommunitiesRequest();
            request.setName(null);
            binding.findCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to find all communities with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new FindCommunitiesRequest();
            request.setName(null);
            binding.findCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // find all communities
         request = new FindCommunitiesRequest();
         request.setName(null);
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length > 0);

         int count = communities.length;

         request = new FindCommunitiesRequest();
         request.setName(" ");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == count);

         request = new FindCommunitiesRequest();
         request.setName("*");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == count);

         // try to find a non-existing community
         request = new FindCommunitiesRequest();
         request.setName("somecommunity");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == 0);

         // find test communities
         request = new FindCommunitiesRequest();
         request.setName("Swiss*");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == 3);
         assertTrue(communities[0].getLocked() == null);

         request = new FindCommunitiesRequest();
         request.setName("SWISS*");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == 3);

         request = new FindCommunitiesRequest();
         request.setName("*German");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == 1);

         // lock test communities
         long[] lockIds = new long[3];
         lockIds[0] = m_swiss.getId();
         lockIds[1] = m_swissGerman.getId();
         lockIds[2] = m_swissItalian.getId();
         lockCommunities(lockIds, session);

         // find test communities
         request = new FindCommunitiesRequest();
         request.setName("Swiss*");
         communities = binding.findCommunities(request);
         assertTrue(communities != null && communities.length == 3);
         assertTrue(communities[0].getLocked() != null);

         // release locked objects
         PSTestUtils.releaseLocks(session, lockIds);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void test4securityDesignSOAPLoadCommunities() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = new long[3];
         ids[0] = m_swiss.getId();
         ids[1] = m_swissGerman.getId();
         ids[2] = m_swissItalian.getId();

         LoadCommunitiesRequest request = null;
         PSCommunity[] communities = null;

         // try to load communities without rhythmyx session
         try
         {
            request = new LoadCommunitiesRequest();
            request.setId(ids);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load communities with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new LoadCommunitiesRequest();
            request.setId(ids);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to load communities with null ids
         try
         {
            request = new LoadCommunitiesRequest();
            request.setId(null);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load communities with empty ids
         try
         {
            request = new LoadCommunitiesRequest();
            request.setId(new long[0]);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load communities with invalid ids
         try
         {
            long[] invalidIds = new long[4];
            invalidIds[0] = m_swiss.getId();
            invalidIds[1] = m_swissGerman.getId();
            invalidIds[2] = m_swissItalian.getId();
            invalidIds[3] = m_swissItalian.getId() + 10;

            request = new LoadCommunitiesRequest();
            request.setId(invalidIds);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, 3, PSCommunity.class.getName());
         }

         // load communities read-only
         request = new LoadCommunitiesRequest();
         request.setId(ids);
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 3);

         // load communities read-writable
         request = new LoadCommunitiesRequest();
         request.setId(ids);
         request.setLock(true);
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 3);

         // load locked communities read-writable with locking session
         request = new LoadCommunitiesRequest();
         request.setId(ids);
         request.setLock(true);
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 3);

         // try to load locked communities read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadCommunitiesRequest();
            request.setId(ids);
            request.setLock(true);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSCommunity.class.getName());
         }

         // release locked objects
         PSTestUtils.releaseLocks(session, ids);
      }
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
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
   public void test5securityDesignSOAPSaveCommunities() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         PSCommunity[] communities = new PSCommunity[3];
         communities[0] = m_swiss;
         communities[1] = m_swissGerman;
         communities[2] = m_swissItalian;

         SaveCommunitiesRequest request = null;

         // try to save communities without rhythmyx session
         try
         {
            request = new SaveCommunitiesRequest();
            request.setPSCommunity(communities);
            binding.saveCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save communities with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new SaveCommunitiesRequest();
            request.setPSCommunity(communities);
            binding.saveCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save communities with null communities
         try
         {
            request = new SaveCommunitiesRequest();
            request.setPSCommunity(null);
            binding.saveCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save communities with empty communities
         try
         {
            request = new SaveCommunitiesRequest();
            request.setPSCommunity(new PSCommunity[0]);
            binding.saveCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save communities in read-only mode
         try
         {
            request = new SaveCommunitiesRequest();
            request.setPSCommunity(communities);
            binding.saveCommunities(request);
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

         // lock communities
         long[] lockIds = new long[3];
         lockIds[0] = m_swiss.getId();
         lockIds[1] = m_swissGerman.getId();
         lockIds[2] = m_swissItalian.getId();
         lockCommunities(lockIds, session);

         // save locked communities, do not release
         request = new SaveCommunitiesRequest();
         request.setPSCommunity(communities);
         request.setRelease(false);
         binding.saveCommunities(request);

         // load, modify and resave communities
         LoadCommunitiesRequest loadRequest = new LoadCommunitiesRequest();
         loadRequest.setId(lockIds);
         loadRequest.setLock(true);
         communities = binding.loadCommunities(loadRequest);
         for (PSCommunity community : communities)
            community.setDescription("New description");
         request = new SaveCommunitiesRequest();
         request.setPSCommunity(communities);
         request.setRelease(false);
         binding.saveCommunities(request);

         // associate all roles and verify results
         FindRolesRequest findRolesRequest = new FindRolesRequest();
         findRolesRequest.setName("*");
         PSObjectSummary[] roleSummaries = binding.findRoles(findRolesRequest);
         List<PSCommunityRolesRole> roles = new ArrayList<PSCommunityRolesRole>();
         for (PSObjectSummary roleSummary : roleSummaries)
         {
            PSCommunityRolesRole role = new PSCommunityRolesRole(roleSummary
               .getId(), roleSummary.getName());
            roles.add(role);
         }
         loadRequest = new LoadCommunitiesRequest();
         loadRequest.setId(lockIds);
         loadRequest.setLock(true);
         communities = binding.loadCommunities(loadRequest);
         for (PSCommunity community : communities)
            community.setRoles(roles.toArray(new PSCommunityRolesRole[roles
               .size()]));
         request = new SaveCommunitiesRequest();
         request.setPSCommunity(communities);
         request.setRelease(false);
         binding.saveCommunities(request);
         loadRequest = new LoadCommunitiesRequest();
         loadRequest.setId(lockIds);
         loadRequest.setLock(true);
         PSCommunity[] communities2 = binding.loadCommunities(loadRequest);
         for (int i = 0; i < communities.length; i++)
            assertTrue(communities[i].getRoles().length == communities2[i]
               .getRoles().length);

         // associate only every 2nd role and verify results
         roles = new ArrayList<PSCommunityRolesRole>();
         int index = 0;
         for (PSObjectSummary roleSummary : roleSummaries)
         {
            if ((index % 2) > 0)
            {
               PSCommunityRolesRole role = new PSCommunityRolesRole(roleSummary
                  .getId(), roleSummary.getName());
               roles.add(role);
            }

            index++;
         }
         loadRequest = new LoadCommunitiesRequest();
         loadRequest.setId(lockIds);
         loadRequest.setLock(true);
         communities = binding.loadCommunities(loadRequest);
         for (PSCommunity community : communities)
            community.setRoles(roles.toArray(new PSCommunityRolesRole[roles
               .size()]));
         request = new SaveCommunitiesRequest();
         request.setPSCommunity(communities);
         request.setRelease(false);
         binding.saveCommunities(request);
         loadRequest = new LoadCommunitiesRequest();
         loadRequest.setId(lockIds);
         loadRequest.setLock(true);
         communities2 = binding.loadCommunities(loadRequest);
         for (int i = 0; i < communities.length; i++)
            assertTrue(communities[i].getRoles().length == communities2[i]
               .getRoles().length);

         // save locked communities and release
         request = new SaveCommunitiesRequest();
         request.setPSCommunity(communities);
         request.setRelease(true);
         binding.saveCommunities(request);

         // try to save communities in read-only mode
         try
         {
            request = new SaveCommunitiesRequest();
            request.setPSCommunity(communities);
            binding.saveCommunities(request);
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
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
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
   public void test6securityDesignSOAPDeleteCommunities() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = new long[3];
         ids[0] = m_swiss.getId();
         ids[1] = m_swissGerman.getId();
         ids[2] = m_swissItalian.getId();

         DeleteCommunitiesRequest request = null;

         // try to delete communities without rhythmyx session
         try
         {
            request = new DeleteCommunitiesRequest();
            request.setId(ids);
            binding.deleteCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete communities with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new DeleteCommunitiesRequest();
            request.setId(ids);
            binding.deleteCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to delete communities with null ids
         try
         {
            request = new DeleteCommunitiesRequest();
            request.setId(null);
            binding.deleteCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete communities with empty ids
         try
         {
            request = new DeleteCommunitiesRequest();
            request.setId(new long[0]);
            binding.deleteCommunities(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // lock objects for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         lockCommunities(ids, session2);

         // try to delete objects locked by somebody else
         try
         {
            request = new DeleteCommunitiesRequest();
            request.setId(ids);
            binding.deleteCommunities(request);
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

         // delete locked communities
         request = new DeleteCommunitiesRequest();
         request.setId(ids);
         binding.deleteCommunities(request);

         // delete non-existing communities
         request = new DeleteCommunitiesRequest();
         request.setId(ids);
         binding.deleteCommunities(request);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
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
   public void test7securityDesignSOAPFindRoles() throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         FindRolesRequest request = null;
         PSObjectSummary[] roles = null;

         // try to find all roles without rhythmyx session
         try
         {
            request = new FindRolesRequest();
            request.setName(null);
            binding.findRoles(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to find all roles with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new FindRolesRequest();
            request.setName(null);
            binding.findRoles(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // find all roles
         request = new FindRolesRequest();
         request.setName(null);
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length > 0);

         int count = roles.length;

         request = new FindRolesRequest();
         request.setName(" ");
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length == count);

         request = new FindRolesRequest();
         request.setName("*");
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length == count);

         // try to find a non-existing role
         request = new FindRolesRequest();
         request.setName("somerole");
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length == 0);

         // load fast forward admin roles
         request = new FindRolesRequest();
         request.setName("*admi*");
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length == 5);

         request = new FindRolesRequest();
         request.setName("*ADMIN*");
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length == 5);

         request = new FindRolesRequest();
         request.setName("admin");
         roles = binding.findRoles(request);
         assertTrue(roles != null && roles.length == 1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   @Test
   public void test8securityDesignSOAPGetVisibilityByCommunity()
      throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      String testTemplateName = "getVisibilityByCommunity";

      // Test operation
      String session = m_session;
      try
      {
         GetVisibilityByCommunityRequest request = null;

         // find all fast forward communities
         PSTestUtils.setSessionHeader(binding, session);
         FindCommunitiesRequest findCommunitiesRequest = new FindCommunitiesRequest();
         findCommunitiesRequest.setName("*");
         PSObjectSummary[] communities = binding
            .findCommunities(findCommunitiesRequest);
         assertTrue(communities != null && communities.length > 0);

         long[] communityIds = new long[communities.length];
         int index = 0;
         for (PSObjectSummary community : communities)
            communityIds[index++] = community.getId();

         binding.clearHeaders();

         // try to use service without rhythmyx session
         try
         {
            request = new GetVisibilityByCommunityRequest();
            request.setId(communityIds);
            binding.getVisibilityByCommunity(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to use service with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new GetVisibilityByCommunityRequest();
            request.setId(communityIds);
            binding.getVisibilityByCommunity(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to use service with null community ids
         try
         {
            request = new GetVisibilityByCommunityRequest();
            request.setId(null);
            binding.getVisibilityByCommunity(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to use service with empty community ids
         try
         {
            request = new GetVisibilityByCommunityRequest();
            request.setId(new long[0]);
            binding.getVisibilityByCommunity(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // get visibility for all supported object types
         request = new GetVisibilityByCommunityRequest();
         request.setId(communityIds);
         PSCommunityVisibility[] visibilities = binding
            .getVisibilityByCommunity(request);
         assertTrue(visibilities != null
            && visibilities.length == communityIds.length);
         index = 0;
         for (PSCommunityVisibility visibility : visibilities)
         {
            long communityId = visibility.getId();
            PSObjectSummary[] visibleObjects = visibility.getVisibleObjects();
            assertTrue(communityId == communityIds[index++]
               && visibleObjects != null);
            assertTrue(visibleObjects.length == 0 || visibleObjects.length >= 13);
         }

         // get the visible workflows for all communities
         request = new GetVisibilityByCommunityRequest();
         request.setId(communityIds);
         request.setType(new Integer(PSTypeEnum.WORKFLOW.getOrdinal()));
         visibilities = binding.getVisibilityByCommunity(request);
         assertTrue(visibilities != null
            && visibilities.length == communityIds.length);
         index = 0;
         for (PSCommunityVisibility visibility : visibilities)
         {
            long communityId = visibility.getId();
            PSObjectSummary[] visibleObjects = visibility.getVisibleObjects();
            assertTrue(communityId == communityIds[index++]
               && visibleObjects != null);
            assertTrue(visibleObjects.length == 0 || visibleObjects.length ==2);
         }

         // try to use service with unsupported object type
         try
         {
            request = new GetVisibilityByCommunityRequest();
            request.setId(communityIds);
            request.setType(new Integer(PSTypeEnum.CONFIGURATION.getOrdinal()));
            binding.getVisibilityByCommunity(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // find default community
         PSObjectSummary defaultCommunity = null;
         for (PSObjectSummary community : communities)
         {
            if (community.getName().equals("Default"))
            {
               defaultCommunity = community;
               break;
            }
         }

         // create test template
         deleteAssemblyTemplate(testTemplateName, session);
         PSAssemblyTemplate testTemplate = setupAssemblyTemplate(
            testTemplateName, session);

         // create community runtime permission for Default community
         setupCommunityAcls(testTemplate.getId(), defaultCommunity.getName(),
            session);

         // get the visible templates for the Default community
         request = new GetVisibilityByCommunityRequest();
         request.setId(new long[] { defaultCommunity.getId() });
         request.setType(new Integer(PSTypeEnum.TEMPLATE.getOrdinal()));
         visibilities = binding.getVisibilityByCommunity(request);
         assertTrue(visibilities != null && visibilities.length == 1);
         PSObjectSummary[] visibleObjects = visibilities[0].getVisibleObjects();
         assertTrue(visibleObjects != null && visibleObjects.length == 1);
         assertTrue(visibleObjects[0].getName().equals(testTemplateName));

         // get all visible objects for the Default community
         request = new GetVisibilityByCommunityRequest();
         request.setId(new long[] { defaultCommunity.getId() });
         visibilities = binding.getVisibilityByCommunity(request);
         assertTrue(visibilities != null && visibilities.length > 0);
         visibleObjects = visibilities[0].getVisibleObjects();
         assertTrue(visibleObjects != null && visibleObjects.length > 0);
         boolean found = false;
         for (PSObjectSummary visibleObject : visibleObjects)
         {
            if (visibleObject.getName().equals(testTemplateName))
            {
               found = true;
               break;
            }
         }
         assertTrue(found);
      }
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
      finally
      {
         deleteAssemblyTemplate(testTemplateName, session);
      }
   }

   /**
    * Create an assembly template for testing.
    * 
    * @param name the template name, assumed not <code>null</code> or empty.
    * @param session the session used, assumed not <code>null</code> or empty.
    * @return the new template created, saved and released, never 
    *    <code>null</code>.
    * @throws Exception for any error.
    */
   private PSAssemblyTemplate setupAssemblyTemplate(String name, String session)
      throws Exception
   {
      AssemblyDesignLocator locator = new AssemblyDesignLocator();
      locator
         .setassemblyDesignSOAPEndpointAddress(getEndpoint("assemblyDesignSOAP"));

      AssemblyDesignSOAPStub binding = (AssemblyDesignSOAPStub) locator
         .getassemblyDesignSOAP();
      PSTestUtils.setSessionHeader(binding, session);

      PSAssemblyTemplate[] templates = binding
         .createAssemblyTemplates(new String[] { name });
      templates[0].setLabel("label");
      templates[0].setAssemblyUrl("assemblyUrl");
      templates[0].setAssembler("assembler");
      templates[0].setDescription("description");
      templates[0].setOutputFormat(OutputFormatType.page);
      templates[0].setTemplateType(TemplateType.shared);

      binding.saveAssemblyTemplates(new SaveAssemblyTemplatesRequest(templates,
         true));

      return templates[0];
   }

   /**
    * Delete the supplied template.
    * 
    * @param name the template name to delete, assumed not <code>null</code> or
    *    empty.
    * @param session the session to use, assumed not <code>null</code> or
    *    empty.
    * @throws Exception for any error.
    */
   private void deleteAssemblyTemplate(String name, String session)
      throws Exception
   {
      AssemblyDesignLocator locator = new AssemblyDesignLocator();
      locator
         .setassemblyDesignSOAPEndpointAddress(getEndpoint("assemblyDesignSOAP"));

      AssemblyDesignSOAPStub binding = (AssemblyDesignSOAPStub) locator
         .getassemblyDesignSOAP();
      PSTestUtils.setSessionHeader(binding, session);

      FindAssemblyTemplatesRequest findRequest = new FindAssemblyTemplatesRequest();
      findRequest.setName(name);
      PSObjectSummary[] templates = binding.findAssemblyTemplates(findRequest);

      if (templates.length > 0)
         binding.deleteAssemblyTemplates(new DeleteAssemblyTemplatesRequest(
            new long[] { templates[0].getId() }, true));
   }
}
