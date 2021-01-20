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

package com.percussion.webservices.rhythmyx;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSAssemblyTestBase;
import com.percussion.webservices.PSSecurityTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assemblydesign.AssemblyDesignSOAPStub;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.rhythmyxdesign.AssemblyDesignTestCase;
import com.percussion.webservices.security.LoadCommunitiesRequest;
import com.percussion.webservices.security.LoadRolesRequest;
import com.percussion.webservices.security.LoginRequest;
import com.percussion.webservices.security.LoginResponse;
import com.percussion.webservices.security.LogoutRequest;
import com.percussion.webservices.security.RefreshSessionRequest;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSCommunityVisibility;
import com.percussion.webservices.security.data.PSLocale;
import com.percussion.webservices.security.data.PSLogin;
import com.percussion.webservices.security.data.PSRole;
import com.percussion.webservices.securitydesign.GetVisibilityByCommunityRequest;
import com.percussion.webservices.securitydesign.SecurityDesignSOAPStub;
import com.percussion.webservices.system.SwitchCommunityRequest;
import com.percussion.webservices.system.SystemSOAPStub;

import java.rmi.RemoteException;
import java.util.Arrays;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Test case for all public security web services
 */
@Category(IntegrationTest.class)
public class SecurityTestCase extends PSSecurityTestBase
{
   /**
    * Test login
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void test1securitySOAPLogin() throws Exception
   {
      SecuritySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         LoginRequest loginRequest = null;

         // try to login with null user name
         try
         {
            loginRequest = new LoginRequest();
            loginRequest.setUsername(null);
            loginRequest.setPassword("demo");
            binding.login(loginRequest);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected
         }

         // try to login with empty user name
         try
         {
            loginRequest = new LoginRequest();
            loginRequest.setUsername(" ");
            loginRequest.setPassword("demo");
            binding.login(loginRequest);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // try to login with null password
         try
         {
            loginRequest = new LoginRequest();
            loginRequest.setUsername("admin1");
            loginRequest.setPassword(null);
            binding.login(loginRequest);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected
         }

         // try to login with empty password
         try
         {
            loginRequest = new LoginRequest();
            loginRequest.setUsername("admin1");
            loginRequest.setPassword(" ");
            binding.login(loginRequest);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // login with null client id
         loginRequest = new LoginRequest("admin1", "demo", null, null, null);
         LoginResponse value = binding.login(loginRequest);

         PSLogin login = value.getPSLogin();
         assertNotNull("login is null", login);

         String session = login.getSessionId();
         assertFalse(StringUtils.isBlank(session));

         // lock the test communities
         long[] ids = new long[3];
         ids[0] = m_swiss.getId();
         ids[1] = m_swissGerman.getId();
         ids[2] = m_swissItalian.getId();

         lockCommunities(ids, session);

         // release the community locks
         PSTestUtils.releaseLocks(session, ids);

         // login with "workbench" client id
         loginRequest = new LoginRequest("admin1", "demo", "workbench", null,
            null);
         value = binding.login(loginRequest);

         login = value.getPSLogin();
         assertNotNull("login is null", login);

         session = login.getSessionId();
         assertFalse(StringUtils.isBlank(session));

         // lock the test communities
         lockCommunities(ids, session);

         // release the community locks
         PSTestUtils.releaseLocks(session, ids);

         // try to login with a different community and locale
         String newCommunity = null;
         String newLocale = null;
         String defaultCommunity = login.getDefaultCommunity();
         String defaultLocale = login.getDefaultLocaleCode();
         for (PSCommunity community : login.getCommunities())
         {
            if (!community.getName().equals(defaultCommunity))
            {
               newCommunity = community.getName();
               break;
            }
         }

         for (PSLocale locale : login.getLocales())
         {
            if (!locale.getCode().equals(defaultLocale))
            {
               newLocale = locale.getCode();
               break;
            }
         }

         if (!(StringUtils.isBlank(newCommunity) && StringUtils
            .isBlank(newLocale)))
         {
            loginRequest = new LoginRequest("admin1", "demo", "workbench",
               newCommunity, newLocale);
            value = binding.login(loginRequest);
            login = value.getPSLogin();
            assertNotNull("login is null", login);
            if (!StringUtils.isBlank(newCommunity))
               assertEquals(newCommunity, login.getDefaultCommunity());
            if (!StringUtils.isBlank(newLocale))
               assertEquals(newLocale, login.getDefaultLocaleCode());
            
            // test with whitespace
            loginRequest = new LoginRequest(" admin1 ", "demo", "workbench",
               newCommunity, newLocale);
            value = binding.login(loginRequest);
            login = value.getPSLogin();
            assertNotNull("login is null", login);
            if (!StringUtils.isBlank(newCommunity))
               assertEquals(newCommunity, login.getDefaultCommunity());
            if (!StringUtils.isBlank(newLocale))
               assertEquals(newLocale, login.getDefaultLocaleCode());            

            // switch back
            loginRequest = new LoginRequest("admin1", "demo", "workbench",
               defaultCommunity, defaultLocale);
            value = binding.login(loginRequest);
            login = value.getPSLogin();
         }
      }
      catch (PSNotAuthenticatedFault e1)
      {
         throw new AssertionFailedError(
            "NotAuthenticatedFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
   }

   /**
    * Test logout
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void test2securitySOAPLogout() throws Exception
   {
      SecuritySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         // create a new session id
         String sessionId = PSTestUtils.login();

         // try to logout with null session
         try
         {
            binding.logout(new LogoutRequest(null));
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected
         }

         // try to logout with empty session
         try
         {
            binding.logout(new LogoutRequest(" "));
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // refresh valid session to make sure the session id is valid
         binding.refreshSession(new RefreshSessionRequest(sessionId));

         // logout the just created session
         binding.logout(new LogoutRequest(sessionId));

         // refresh invalid session to make sure the session was invalidated
         try
         {
            binding.refreshSession(new RefreshSessionRequest(sessionId));
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
   }

   /**
    * Test refresh session
    * 
    * @throws Exception
    */
   @Test
   public void test3securitySOAPRefreshSession() throws Exception
   {
      SecuritySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         String sessionId = PSTestUtils.login();
         binding.refreshSession(new RefreshSessionRequest(sessionId));
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
   }

   /**
    * Test load communities
    * 
    * @throws Exception
    */
   @Test
   public void test4securitySOAPLoadCommunities() throws Exception
   {
      SecuritySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         LoadCommunitiesRequest request = null;
         PSCommunity[] communities = null;

         // try to load all communities without rhythmyx session
         try
         {
            request = new LoadCommunitiesRequest();
            request.setName(null);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load all communities with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadCommunitiesRequest();
            request.setName(null);
            binding.loadCommunities(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load all communities
         request = new LoadCommunitiesRequest();
         request.setName(null);
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length > 0);

         int count = communities.length;

         request = new LoadCommunitiesRequest();
         request.setName(" ");
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == count);

         request = new LoadCommunitiesRequest();
         request.setName("*");
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == count);

         // try to load a non-existing community
         request = new LoadCommunitiesRequest();
         request.setName("somecommunity");
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 0);

         // load test communities
         request = new LoadCommunitiesRequest();
         request.setName("swiss*");
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 3);

         request = new LoadCommunitiesRequest();
         request.setName("SWISS*");
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 3);

         request = new LoadCommunitiesRequest();
         request.setName("*italian");
         communities = binding.loadCommunities(request);
         assertTrue(communities != null && communities.length == 1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   /**
    * Test load roles
    * 
    * @throws Exception
    */
   @Test
   public void test5securitySOAPLoadRoles() throws Exception
   {
      SecuritySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         LoadRolesRequest request = null;
         PSRole[] roles = null;

         // try to load all roles without rhythmyx session
         try
         {
            request = new LoadRolesRequest();
            request.setName(null);
            binding.loadRoles(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load all roles with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadRolesRequest();
            request.setName(null);
            binding.loadRoles(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load all roles
         request = new LoadRolesRequest();
         request.setName(null);
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length > 0);

         int count = roles.length;

         request = new LoadRolesRequest();
         request.setName(" ");
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length == count);

         request = new LoadRolesRequest();
         request.setName("*");
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length == count);

         // try to load a non-existing role
         request = new LoadRolesRequest();
         request.setName("somerole");
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length == 0);

         // load fast forward admin roles
         request = new LoadRolesRequest();
         request.setName("*admi*");
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length == 5);

         request = new LoadRolesRequest();
         request.setName("*ADMIN*");
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length == 5);

         request = new LoadRolesRequest();
         request.setName("admin");
         roles = binding.loadRoles(request);
         assertTrue(roles != null && roles.length == 1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   /**
    * Test the FilterByRuntimeVisibility web service, assumes FF is installed.
    * 
    * @throws Exception if the test fails.
    */
   @Ignore
   @Test //TODO: Fix this
   public void FIXME_test6SecuritySOAPFilterByRuntimeVisibility() throws Exception
   {
      SecuritySOAPStub binding = getBinding(null);

      // ensure in EI community
      SystemSOAPStub sysBinding = new SystemTestCase().getBinding(600000);
      PSTestUtils.setSessionHeader(sysBinding, m_session);
      sysBinding.switchCommunity(new SwitchCommunityRequest(
         "Enterprise_Investments"));

      long[] ids;
      long[] expected;

      // test no session
      ids = new long[] { 100 };
      try
      {
         binding.filterByRuntimeVisibility(ids);
         assertFalse("should have thrown", true);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      PSTestUtils.setSessionHeader(binding, "nosuchsession");
      try
      {
         binding.filterByRuntimeVisibility(ids);
         assertFalse("should have thrown", true);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid input
      PSTestUtils.setSessionHeader(binding, m_session);
      ids = new long[0];
      try
      {
         binding.filterByRuntimeVisibility(ids);
         assertFalse("should have thrown", true);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test invalid (non-design) ids
      ids = new long[1];
      ids[0] = new PSLegacyGuid(335, 1).longValue();
      PSTestUtils.setSessionHeader(binding, m_session);
      ids = new long[0];
      try
      {
         binding.filterByRuntimeVisibility(ids);
         assertFalse("should have thrown", true);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test filter runtime
      ids = new long[2];
      ids[0] = new PSDesignGuid(PSTypeEnum.SITE, 301).getValue();
      ids[1] = new PSDesignGuid(PSTypeEnum.SITE, 303).getValue();
      expected = new long[] { ids[0] };

      long[] results = binding.filterByRuntimeVisibility(ids).getIds();
      assertTrue(results.length >= ids.length);

      // test "passthru"
      ids = new long[2];
      ids[0] = new PSDesignGuid(PSTypeEnum.CONTENT_LIST, 310).getValue();
      ids[1] = new PSDesignGuid(PSTypeEnum.SITE, 301).getValue();

      results = binding.filterByRuntimeVisibility(ids).getIds();
      assertTrue(Arrays.equals(ids, results));

      ids = new long[2];
      ids[0] = new PSDesignGuid(PSTypeEnum.CONTENT_LIST, 310).getValue();
      ids[1] = new PSDesignGuid(PSTypeEnum.SITE, 303).getValue();
      expected = new long[] { ids[0] };

      results = binding.filterByRuntimeVisibility(ids).getIds();
      assertTrue(results.length >= ids.length);

      // test no results
      ids = new long[1];
      ids[0] = new PSDesignGuid(PSTypeEnum.SITE, 303).getValue();
      expected = new long[0];

      results = binding.filterByRuntimeVisibility(ids).getIds();
      assertEquals(results.length, 0);
   }
   
   /**
    * Test the FilterByRuntimeVisibility web service, assumes FF is installed.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testCommunityVisibility() throws Exception
   {
      SecurityDesignSOAPStub secBinding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(secBinding, m_session);
      
      // ensure in EI community
      AssemblyDesignSOAPStub assemBinding = new PSAssemblyTestBase().getDesignBinding(null);
      PSTestUtils.setSessionHeader(assemBinding, m_session);
      
      
      GetVisibilityByCommunityRequest req = new GetVisibilityByCommunityRequest();
      
      long[] ids = new long[1];
      ids[0] = new PSDesignGuid(PSTypeEnum.COMMUNITY_DEF, 1002).getValue();
      req.setId(ids);
      req.setType(4);
      
      PSCommunityVisibility[] vises = secBinding.getVisibilityByCommunity(req);
      
      // create an Template without Acl entry
      PSAssemblyTemplate tstTemplate = AssemblyDesignTestCase.createTemplate(
            assemBinding, "tstTemplate1", "tstTemplate1");
      AssemblyDesignTestCase.saveTemplate(assemBinding, tstTemplate, true);
      
      PSCommunityVisibility[] vises_2 = secBinding.getVisibilityByCommunity(req);
      
      // the visible # of objects should be the same as before, since
      // the created Template should not be visible yet.
      assertTrue(vises.length >= vises_2.length);
      
      new PSAssemblyTestBase().deleteTemplate(tstTemplate, m_session);
   }
}
