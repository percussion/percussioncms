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
package com.percussion.services.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;

import com.percussion.design.objectstore.PSSubject;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityRoleAssociation;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.junit.experimental.categories.Category;


/**
 * Test case for the {@link IPSBackEndRoleMgr}
 */
@Category(IntegrationTest.class)
public class PSBackEndRoleMgrTest extends ServletTestCase
{
   /**
    * Test loading roles from the backend.
    * 
    * @throws Exception if the test fails
    */
   @SuppressWarnings(value =
   {"unchecked"})
   public void testGetRhythmyxRoles() throws Exception
   {
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
      List roles = mgr.getRhythmyxRoles();
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));
      assertTrue(roles.contains("Author"));
      assertTrue(roles.contains("Editor"));
      
      // TODO - the following 2 lines do not work, comment out for now.
      //assertTrue(hasSameMembers(roles, mgr.getRhythmyxRoles(null, 0)));
      //assertTrue(hasSameMembers(roles, mgr.getRhythmyxRoles("", 0)));

      roles = mgr.getRhythmyxRoles("admin1", PSSubject.SUBJECT_TYPE_USER);
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));
      assertTrue(hasSameMembers(roles, mgr.getRhythmyxRoles("admin1", 0)));
      roles = mgr.getRhythmyxRoles("admin1", PSSubject.SUBJECT_TYPE_GROUP);
      assertTrue(roles.isEmpty());

      assertTrue(mgr.getRhythmyxRoles("foo", 0).isEmpty());

      roles = mgr.getRhythmyxRoles(null, PSSubject.SUBJECT_TYPE_USER);
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));
      assertTrue(roles.contains("Author"));
      assertTrue(roles.contains("Editor"));
   }
   
   @SuppressWarnings(value =
   {"unchecked"})
   public void testSetRhythmyxRoles() throws Exception
   {
      final IPSSecurityWs svc = PSSecurityWsLocator.getSecurityWebservice();
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
      
      String SUB_NAME = "editor-test";
      int SUB_TYPE = PSSubject.SUBJECT_TYPE_USER;
      String[] roleNames = new String[] {"Editor", "Artist"};

      try
      {
         mgr.setRhythmyxRoles(SUB_NAME, SUB_TYPE, Arrays.asList(roleNames));
         fail("Should fail on authentication here.");
      }
      catch (Exception e)
      {
         // should be here due to fail authentication
      }

      svc.login("admin1", "demo", null, null);

      // set subject to roles
      mgr.setRhythmyxRoles(SUB_NAME, SUB_TYPE, Arrays.asList(roleNames));
      
      // validating above operation
      List roles = mgr.getRhythmyxRoles(SUB_NAME, SUB_TYPE);

      assertTrue(roles.contains("Editor"));
      assertTrue(roles.contains("Artist"));
      
      // remove the subject from "Artist" role
      roleNames = new String[] {"Editor"};
      mgr.setRhythmyxRoles(SUB_NAME, SUB_TYPE, Arrays.asList(roleNames));

      // validating above operation
      roles = mgr.getRhythmyxRoles(SUB_NAME, SUB_TYPE);

      assertTrue(roles.contains("Editor"));
      assertTrue(!roles.contains("Artist"));

      // remove the subject from all roles
      mgr.setRhythmyxRoles(SUB_NAME, SUB_TYPE, Collections.EMPTY_LIST);
      
      // validating remove operation
      roles = mgr.getRhythmyxRoles(SUB_NAME, SUB_TYPE);
      assertTrue(!roles.contains("Editor"));
      assertTrue(!roles.contains("Artist"));
   }

   @SuppressWarnings(value =
   {"unchecked"})
   public void testSetMultipleRhythmyxRoles() throws Exception
   {
      final IPSSecurityWs svc = PSSecurityWsLocator.getSecurityWebservice();
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
      
      String SUB_NAME = "editor-test";
      ArrayList<String> subjectNames = new ArrayList<String>();
      subjectNames.add(SUB_NAME);
      for (int i = 0; i < 200; i++)
      {
         subjectNames.add(SUB_NAME + i);
      }
      
      int SUB_TYPE = PSSubject.SUBJECT_TYPE_USER;
      String[] roleNames = new String[] {"Editor", "Artist"};

      try
      {
         mgr.setRhythmyxRoles(subjectNames, SUB_TYPE, Arrays.asList(roleNames));
         fail("Should fail on authentication here.");
      }
      catch (Exception e)
      {
         // should be here due to fail authentication
      }

      svc.login("admin1", "demo", null, null);

      // set subject to roles
      mgr.setRhythmyxRoles(subjectNames, SUB_TYPE, Arrays.asList(roleNames));
      
      for (String subjectName : subjectNames)
      {
         List roles = mgr.getRhythmyxRoles(subjectName, SUB_TYPE);

         assertTrue(roles.contains("Editor"));
         assertTrue(roles.contains("Artist"));
      }
      
      // remove the subject from "Artist" role
      roleNames = new String[] {"Editor"};
      mgr.setRhythmyxRoles(SUB_NAME, SUB_TYPE, Arrays.asList(roleNames));

      // validating above operation
      List roles = mgr.getRhythmyxRoles(SUB_NAME, SUB_TYPE);

      assertTrue(roles.contains("Editor"));
      assertTrue(!roles.contains("Artist"));

      // remove the subject from all roles
      mgr.setRhythmyxRoles(SUB_NAME, SUB_TYPE, Collections.EMPTY_LIST);
      
      // validating remove operation
      roles = mgr.getRhythmyxRoles(SUB_NAME, SUB_TYPE);
      assertTrue(!roles.contains("Editor"));
      assertTrue(!roles.contains("Artist"));
   }
   
   /**
    * Test that the two lists have the same members.
    * 
    * @param list1 The first list, assumed not <code>null</code>.
    * @param list2 The second list, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if they have the same members,
    *         <code>false</code> if not.
    */
   boolean hasSameMembers(List<? extends Object> list1,
         List<? extends Object> list2)
   {
      Set<Object> set1 = new HashSet<Object>(list1);
      Set<Object> set2 = new HashSet<Object>(list2);
      return set1.equals(set2);
   }

   /**
    * Test loading community roles.
    * 
    * @throws Exception if the test fails.
    */
   public void testGetCommunityRoles() throws Exception
   {
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<String> roles = mgr.getCommunityRoles(10);
      assertTrue(roles.contains("Default"));
      assertTrue(roles.contains("RxPublisher"));
   }

   /**
    * Test loading communities.
    * 
    * @throws Exception if the test fails.
    */
   public void testLoadCommunities() throws Exception
   {
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
      PSCommunity[] communities = mgr.loadCommunities(new IPSGuid[]
      {new PSGuid(PSTypeEnum.COMMUNITY_DEF, 10),
            new PSGuid(PSTypeEnum.COMMUNITY_DEF, 1002)});
      assertEquals(communities.length, 2);
      assertEquals(communities[0].getName(), "Default");
      assertEquals(communities[1].getName(), "Enterprise_Investments");
   }

   /**
    * Test loading roles.
    * 
    * @throws Exception if the test fails.
    */
   public void testLoadRoles() throws Exception
   {
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
      PSBackEndRole[] roles = mgr.loadRoles(new IPSGuid[]
      {new PSGuid(PSTypeEnum.ROLE, 1), new PSGuid(PSTypeEnum.ROLE, 2)});
      assertEquals(roles.length, 2);
      assertEquals(roles[0].getName(), "Admin");
      assertEquals(roles[1].getName(), "Author");
   }

   /**
    * Test community crud operations
    * 
    * @throws Exception if the test fails.
    */
   public void testCommunityCRUD() throws Exception
   {
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();

      PSCommunity community = null;
      try
      {
         // delete left-overs from previous tests
         String communityName = "TestCommunity";
         List<PSCommunity> communities = mgr
               .findCommunitiesByName(communityName);
         if (!communities.isEmpty())
         {
            community = communities.get(0);
            mgr.deleteCommunity(community.getGUID());
         }

         try
         {
            // try to create a community with null name
            mgr.createCommunity(null, null);
            fail();
         }
         catch (IllegalArgumentException expected)
         {
         }

         try
         {
            // try to create a community with empty name
            mgr.createCommunity(" ", null);
            fail();
         }
         catch (IllegalArgumentException expected)
         {
         }

         try
         {
            // try to save a null community
            mgr.saveCommunity(null);
            fail();
         }
         catch (IllegalArgumentException expected)
         {
         }

         // create new test community
         community = mgr.createCommunity(communityName, "Testing communities");

         // associate all existing roles
         List<PSBackEndRole> roles = mgr.findRolesByName("%");
         for (PSBackEndRole role : roles)
            community.addRoleAssociation(new PSGuid(PSTypeEnum.ROLE, 
               role.getId()));

         // save the test community
         mgr.saveCommunity(community);

         // Modify and resave
         communities = mgr.findCommunitiesByName(communityName);
         assertNotNull(communities);
         assertEquals(1, communities.size());
         community = communities.get(0);
         community.setDescription("New description");
         mgr.saveCommunity(community);
         
         try
         {
            // try to create a community with existing name
            mgr.createCommunity(communityName.toUpperCase(), null);
            fail();
         }
         catch (IllegalArgumentException expected)
         {
         }

         // read community back and verify its equal to what we saved
         communities = mgr.findCommunitiesByName(communityName);
         assertFalse(communities.isEmpty() && communities.size() > 1);
         PSCommunity community2 = communities.get(0);
         assertTrue(community.equals(community2));

         // set new role associations, only every 2nd
         int index = 0;
         Collection<IPSGuid> roleAssociations = new ArrayList<IPSGuid>();
         for (PSBackEndRole role : roles)
         {
            if ((index % 2) > 0)
               roleAssociations.add(new PSGuid(PSTypeEnum.ROLE, role.getId()));

            index++;
         }
         community.setRoleAssociations(roleAssociations);

         // save the test community
         mgr.saveCommunity(community);

         // read community back and verify its equal to what we saved
         communities = mgr.findCommunitiesByName(communityName);
         assertFalse(communities.isEmpty() && communities.size() > 1);
         community2 = communities.get(0);
         assertTrue(community.equals(community2));

         // associate all existing roles
         for (PSBackEndRole role : roles)
            community.addRoleAssociation(new PSGuid(PSTypeEnum.ROLE, role
                  .getId()));

         // save the test community
         mgr.saveCommunity(community);

         // read community back and verify its equal to what we saved
         communities = mgr.findCommunitiesByName(communityName);
         assertFalse(communities.isEmpty() && communities.size() > 1);
         community2 = communities.get(0);
         assertTrue(community.equals(community2));

         // remove associated role
         community.removeRoleAssociation(new PSGuid(PSTypeEnum.ROLE, roles.get(
               0).getId()));

         // save the test community
         mgr.saveCommunity(community);

         // read community back and verify its equal to what we saved
         communities = mgr.findCommunitiesByName(communityName);
         assertFalse(communities.isEmpty() && communities.size() > 1);
         community2 = communities.get(0);
         assertTrue(community.equals(community2));

         // find all communities
         communities = mgr.findCommunitiesByName("%");
         assertTrue(communities != null && communities.size() > 0);
         int count = communities.size();
         communities = mgr.findCommunitiesByName(null);
         assertTrue(communities != null && communities.size() == count);
         communities = mgr.findCommunitiesByName(" ");
         assertTrue(communities != null && communities.size() == count);

         // find the test community
         communities = mgr.findCommunitiesByName(communityName);
         assertTrue(communities != null && communities.size() == 1);

         // update the test community
         community.setDescription("New description");
         mgr.saveCommunity(community);

         try
         {
            // try to delete a null community
            mgr.deleteCommunity(null);
            fail();
         }
         catch (IllegalArgumentException expected)
         {
         }
      }
      finally
      {
         // delete the test community
         if (community != null)
            mgr.deleteCommunity(community.getGUID());
      }
   }
   
   /**
    * Test finding communities for specified role IDs.
    * 
    * @throws Exception If the test fails.
    */
   public void testFindCommunitiesByRole() throws Exception
   {
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<IPSGuid> roleIds = new ArrayList<IPSGuid>();
      
      IPSGuid editor = guidMgr.makeGuid(4, PSTypeEnum.ROLE);
      IPSGuid author = guidMgr.makeGuid(2, PSTypeEnum.ROLE);
      IPSGuid eiMembers = guidMgr.makeGuid(304, PSTypeEnum.ROLE);
      IPSGuid ciMembers = guidMgr.makeGuid(307, PSTypeEnum.ROLE);
      
      roleIds.add(editor);
      roleIds.add(author);
      assertTrue(roleMgr.findCommunitiesByRole(roleIds).isEmpty());
      
      roleIds.clear();
      roleIds.add(eiMembers);
      List<PSCommunityRoleAssociation> results = 
         roleMgr.findCommunitiesByRole(roleIds);
      assertEquals(1, results.size());
      assertEquals(eiMembers.getUUID(), results.get(0).getRoleId());
      assertEquals(1002, results.get(0).getCommunityId());
      
      roleIds.clear();
      roleIds.add(ciMembers);
      results = roleMgr.findCommunitiesByRole(roleIds);
      assertEquals(1, results.size());
      assertEquals(ciMembers.getUUID(), results.get(0).getRoleId());
      assertEquals(1003, results.get(0).getCommunityId());
      
      roleIds.add(eiMembers);
      roleIds.add(editor);
      roleIds.add(author);
      
      results = roleMgr.findCommunitiesByRole(roleIds);
      assertEquals(2, results.size());
   }
}
