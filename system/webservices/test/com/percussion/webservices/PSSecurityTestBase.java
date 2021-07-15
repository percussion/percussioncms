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
package com.percussion.webservices;

import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.rhythmyxdesign.SecurityDesignLocator;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSCommunityRolesRole;
import com.percussion.webservices.securitydesign.DeleteCommunitiesRequest;
import com.percussion.webservices.securitydesign.FindCommunitiesRequest;
import com.percussion.webservices.securitydesign.FindRolesRequest;
import com.percussion.webservices.securitydesign.LoadCommunitiesRequest;
import com.percussion.webservices.securitydesign.SaveCommunitiesRequest;
import com.percussion.webservices.securitydesign.SecurityDesignSOAPStub;

import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.assertNotNull;

/**
 * Implements utilities used by all security test cases.
 */
public class PSSecurityTestBase extends PSTestBase
{
   /**
    * Create a new binding for the security SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new binding.
    */
   protected SecuritySOAPStub getBinding(Integer timeout)
      throws AssertionFailedError
   {
      return getSecuritySOAPStub(timeout);
   }

   /**
    * Create a new binding for the security design SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new assembly
    *    binding.
    */
   protected static SecurityDesignSOAPStub getDesignBinding(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         SecurityDesignLocator locator = new SecurityDesignLocator();
         locator
            .setsecurityDesignSOAPEndpointAddress(getEndpoint("securityDesignSOAP"));

         SecurityDesignSOAPStub binding = (SecurityDesignSOAPStub) locator
            .getsecurityDesignSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(60000);
         else
            binding.setTimeout(timeout);

         return binding;
      }
      catch (ServiceException e)
      {
         if (e.getLinkedCause() != null)
            e.getLinkedCause().printStackTrace();

         throw new AssertionFailedError("JAX-RPC ServiceException caught: " + e);
      }
   }

   @BeforeClass
   public static void setup() throws Exception
   {

      deleteTestCommunities(m_session);

      try
      {
         createTestCommunities(m_session);
      }
      catch (Exception e)
      {
         System.out.println("Could not create test communities. "
            + "All community tests will fail!");
      }
   }

  @AfterClass
   protected static void tearDown() throws Exception
   {
      deleteTestCommunities(m_session);

   }

   /**
    * Create a new community for the supplied name.
    * 
    * @param session the session used to perform the operation, not 
    *    <code>null</code> or empty.
    * @param name the community name, may be <code>null</code> or empty.
    * @return the new created community, never <code>null</code>.
    * @throws Exception for any error creating the new community.
    */
   protected static PSCommunity createCommunity(String name, String session)
      throws Exception
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      SecurityDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      PSCommunity[] communities = binding
         .createCommunities(new String[] { name });

      FindRolesRequest findRolesRequest = new FindRolesRequest();
      findRolesRequest.setName("*");
      PSObjectSummary[] roles = binding.findRoles(findRolesRequest);

      PSCommunity community = communities[0];
      PSCommunityRolesRole[] communityRoles = new PSCommunityRolesRole[roles.length];
      int index = 0;
      for (PSObjectSummary role : roles)
      {
         PSCommunityRolesRole communityRole = new PSCommunityRolesRole(role
            .getId(), role.getName());
         communityRoles[index++] = communityRole;
      }
      community.setRoles(communityRoles);

      SaveCommunitiesRequest saveRequest = new SaveCommunitiesRequest();
      saveRequest.setPSCommunity(communities);
      binding.saveCommunities(saveRequest);

      return communities[0];
   }

   /**
    * Creates all communities used for testing.
    * 
    * @param session the session used to create the test communities,
    *    not <code>null</code> or empty.
    * @throws Exception for any error.
    */
   protected static void createTestCommunities(String session) throws Exception
   {
      m_swiss = createCommunity("Swiss", session);
      m_swissGerman = createCommunity("Swiss German", session);
      m_swissItalian = createCommunity("Swiss Italian", session);
   }

   /**
    * Looks up all test communities by name and deletes all which exist.
    * 
    * @param session the session used to execute the deletes, not
    *    <code>null</code> or empty.
    * @throws Exception for any error deleting the test communities.
    */
   protected static void deleteTestCommunities(String session) throws Exception
   {
      SecurityDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      List<PSObjectSummary> objects = new ArrayList<PSObjectSummary>();

      FindCommunitiesRequest findRequest = new FindCommunitiesRequest();
      findRequest.setName("Swiss");
      PSObjectSummary[] summaries = binding.findCommunities(findRequest);
      if (summaries.length > 0)
         objects.add(summaries[0]);

      findRequest.setName("Swiss German");
      summaries = binding.findCommunities(findRequest);
      if (summaries.length > 0)
         objects.add(summaries[0]);

      findRequest.setName("Swiss Italian");
      summaries = binding.findCommunities(findRequest);
      if (summaries.length > 0)
         objects.add(summaries[0]);

      findRequest.setName("Swiss French");
      summaries = binding.findCommunities(findRequest);
      if (summaries.length > 0)
         objects.add(summaries[0]);

      if (objects.size() > 0)
      {
         long[] ids = new long[objects.size()];
         for (int i = 0; i < objects.size(); i++)
            ids[i] = objects.get(i).getId();

         DeleteCommunitiesRequest deleteRequest = new DeleteCommunitiesRequest();
         deleteRequest.setId(ids);
         binding.deleteCommunities(deleteRequest);
      }
   }

   /**
    * Lock all communities for the supplied ids.
    * 
    * @param ids the ids of the communities to lock, not <code>null</code> or
    *    empty.
    * @param session the session for which to lock the objects, not
    *    <code>null</code> or empty.
    * @return the locked communities, never <code>null</code> or empty.
    * @throws Exception for any error locking the objects.
    */
   protected PSCommunity[] lockCommunities(long[] ids, String session)
      throws Exception
   {
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      SecurityDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      LoadCommunitiesRequest loadRequest = new LoadCommunitiesRequest();
      loadRequest.setId(ids);
      loadRequest.setLock(true);
      PSCommunity[] communities = binding.loadCommunities(loadRequest);

      return communities;
   }

   // test communities
   protected static PSCommunity m_swiss = null;

   protected static PSCommunity m_swissGerman = null;

   protected static PSCommunity m_swissItalian = null;
}
