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
package com.percussion.security;

import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link PSRoleManager}. Requires the mock role and subject
 * cataloger registrations found in the
 * UnitTestResources\com\percussion\security\cataloger-beans.xml to be added to
 * the cataloger-beans.xmlused by the running server, and also requires that the
 * PSRoleManagerTest.class file be manually deployed to the
 * WEB-INF/classes/com/percussion/security directory and the
 * IPSCustomJunitTest.class file be manually deployed to the
 * WEB-INF/classes/com/percussion/testing directory (requires server restart).
 */
@SuppressWarnings(value={"unchecked"})
@Category(IntegrationTest.class)
public class PSRoleManagerTest extends ServletTestCase
{
   /**
    * Class under test for List getRoles()
    */
   public void testGetRoles()
   {
      List roles = PSRoleManager.getInstance().getRoles();
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));
      assertTrue(roles.contains("Editor"));
   }

   /**
    * Class under test for List getRoles(String, int)
    */
   public void testGetRolesStringint()
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      List roles;
      roles = mgr.getRoles("admin1", PSSubject.SUBJECT_TYPE_USER);
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));
      assertEquals(roles, mgr.getRoles("admin1", 0));

      roles = mgr.getRoles("subCatUser1_1", PSSubject.SUBJECT_TYPE_USER);
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Author"));
      assertEquals(roles, mgr.getRoles("subCatUser1_1", 0));

      roles = mgr.getRoles("subCatGroup2", PSSubject.SUBJECT_TYPE_USER);
      assertTrue(roles.isEmpty());
      roles = mgr.getRoles("subCatGroup2", PSSubject.SUBJECT_TYPE_GROUP);
      assertTrue(roles.contains("QA"));
      assertEquals(roles, mgr.getRoles("subCatGroup2", 0));
      
      assertEquals(mgr.getRoles(null, 0), mgr.getRoles());
      
      roles = mgr.getRoles(null, PSSubject.SUBJECT_TYPE_GROUP);
      assertTrue(roles.contains("QA"));
      assertFalse(roles.contains("Author"));
      assertFalse(roles.contains("Editor"));

      assertEquals(mgr.getRoles(null, 0), mgr.getRoles());
      roles = mgr.getRoles(null, PSSubject.SUBJECT_TYPE_USER);
      assertTrue(roles.contains("Admin"));
      assertTrue(roles.contains("Author"));
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testIsMemberOfRole()
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      assertTrue(mgr.isMemberOfRole("admin1", "Admin"));
      assertTrue(mgr.isMemberOfRole("editor1", "Editor"));
      assertFalse(mgr.isMemberOfRole("editor1", "Admin"));
      assertTrue(mgr.isMemberOfRole("subCatUser1_1", "Author"));
      assertFalse(mgr.isMemberOfRole("subCatUser1_1", "Editor"));
      assertTrue(mgr.isMemberOfRole("subCatGroup1", "QA")); 
      assertFalse(mgr.isMemberOfRole("subCatGroup1", "Editor"));
   }

   /**
    * Class under test for List memberRoleList(PSUserSession, String)
    * 
    * @throws Exception if there are any errors or the test fails.
    */
   public void testMemberRoleList() throws Exception
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      // login to create a session
      PSSecurityFilter.authenticate(request, 
         response, "admin1", "demo");
      
      PSRequest psreq = (PSRequest) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST);
      PSUserSession userSession = psreq.getUserSession();
      
      List roles;
      roles = mgr.memberRoleList(userSession, "author1");
      assertTrue(roles.contains("Author"));
      assertTrue(!roles.contains("Admin"));
      assertEquals(roles, mgr.memberRoleList(userSession, 
         new PSGlobalSubject("author1", PSSubject.SUBJECT_TYPE_USER, null)));
      
      roles = mgr.memberRoleList(userSession, "subCatUser1_1");
      assertTrue(roles.contains("Author"));
      assertTrue(!roles.contains("Admin"));
      assertEquals(roles, mgr.memberRoleList(userSession, 
         new PSGlobalSubject("subCatUser1_1", PSSubject.SUBJECT_TYPE_USER, 
            null)));
      
      roles = mgr.memberRoleList(userSession, new PSGlobalSubject(
         "subCatGroup1", PSSubject.SUBJECT_TYPE_GROUP, null));
      assertTrue(roles.contains("QA"));
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testExpandGroups()
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      Set<PSSubject> groups = new HashSet<PSSubject>();
      PSSubject foo = new PSGlobalSubject("foo", PSSubject.SUBJECT_TYPE_GROUP, 
         null);
      PSSubject editor = new PSGlobalSubject("editor",
         PSSubject.SUBJECT_TYPE_USER, null);
      PSSubject group1 = new PSGlobalSubject("subCatGroup1", 
         PSSubject.SUBJECT_TYPE_GROUP, null);
      PSSubject group2 = new PSGlobalSubject("subCatGroup2", 
         PSSubject.SUBJECT_TYPE_GROUP, null);
      
      groups.add(foo);
      groups.add(editor);
      groups.add(group1);
      groups.add(group2);
      Set<PSSubject> expanded = mgr.expandGroups(groups);

      assertTrue(expanded.contains(foo));
      assertTrue(expanded.contains(editor));
      assertTrue(!expanded.contains(group1));
      assertTrue(!expanded.contains(group2));

      Set<String> users = new HashSet<String>();
      
      for (PSSubject subject : expanded)
      {
         if (subject.getType() == PSSubject.SUBJECT_TYPE_USER)
            users.add(subject.getName());
      }
      assertTrue(users.contains("subCatUser1_1"));
      assertTrue(users.contains("subCatUser1_1"));
      assertTrue(users.contains("subCatUser2_1"));
      assertTrue(users.contains("subCatUser1_2"));
   }

   /**
    * Class under test for List roleMembers(String, int)
    */
   public void testRoleMembersStringint()
   {

   }

   /**
    * Class under test for List roleMembers(String, int, String)
    */
   public void testRoleMembersStringintString()
   {
      Set<String> userNames = new HashSet<String>();
      Set<String> groupNames = new HashSet<String>();
      
      getRoleMembers("Editor", 0, null, userNames, groupNames);
      assertTrue(userNames.contains("editor1"));
      assertTrue(userNames.contains("editor2"));
      assertTrue(userNames.contains("subCatUser2_1"));
      assertTrue(userNames.contains("subCatUser2_2"));

      getRoleMembers("QA", PSSubject.SUBJECT_TYPE_USER | 
         PSSubject.SUBJECT_TYPE_GROUP, null, userNames, groupNames);
      assertTrue(userNames.contains("qa1"));
      assertTrue(!userNames.contains("editor1"));
      assertTrue(!userNames.contains("subCatGroup1"));
      assertTrue(groupNames.contains("subCatGroup1"));
      assertTrue(!groupNames.contains("qa1"));
      
      Set<String> savedUsers = new HashSet<String>(userNames);
      Set<String> savedGroups = new HashSet<String>(groupNames);
      getRoleMembers("QA", 0, null, userNames, groupNames);
      assertEquals(userNames, savedUsers);
      assertEquals(groupNames, savedGroups);
      
      getRoleMembers("QA", PSSubject.SUBJECT_TYPE_USER, null, userNames, 
         groupNames);
      assertTrue(userNames.contains("qa1"));
      assertTrue(!userNames.contains("editor1"));
      assertTrue(!userNames.contains("subCatGroup1"));
      assertTrue(groupNames.isEmpty());
      
      getRoleMembers("QA", PSSubject.SUBJECT_TYPE_GROUP, null, userNames, 
         groupNames);
      assertTrue(userNames.isEmpty());
      assertTrue(groupNames.contains("subCatGroup1"));
      assertTrue(groupNames.contains("subCatGroup2"));
      
      getRoleMembers("QA", 0, "qa%", userNames, groupNames);
      assertTrue(userNames.contains("qa1"));
      assertTrue(userNames.contains("qa2"));
      assertTrue(groupNames.isEmpty());
      
      getRoleMembers("QA", 0, "sub%", userNames, groupNames);
      assertTrue(groupNames.contains("subCatGroup1"));
      assertTrue(groupNames.contains("subCatGroup2"));
      assertTrue(userNames.isEmpty());
   }

   /**
    * Calls {@link PSRoleManager#roleMembers(String, int, String) 
    * PSRoleManager.roleMembers(name, typeFlags, filter)} and filters the 
    * resulting subject names into the two supplied lists.
    *  
    * @param name The name, may be <code>null</code> or empty.
    * @param typeFlags The type flags, see method doc for details.
    * @param filter The subject name filter, may be <code>null</code> or empty.
    * @param userNames Set to which user names are added, current contents are
    * replaced, assumed not <code>null</code>.
    * @param groupNames Set to which group names are added, current contents are
    * replaced, assumed not <code>null</code>.
    */
   private void getRoleMembers(String name, int typeFlags, String filter,
      Set<String> userNames, Set<String> groupNames)
   {
      filterSubjectNames(
         PSRoleManager.getInstance().roleMembers(name, typeFlags, filter),
         userNames, groupNames);
   }

   /**
    * Filter the supplied list of subjects into two sets of names.
    * 
    * @param members list of subjects, assumed not <code>null</code>.
    * @param userNames Set to which user names are added, current contents are
    * replaced, assumed not <code>null</code>.
    * @param groupNames Set to which group names are added, current contents are
    * replaced, assumed not <code>null</code>.
    */
   private void filterSubjectNames(Collection<PSSubject> members, 
      Set<String> userNames, Set<String> groupNames)
   {
      userNames.clear();
      groupNames.clear();
      for (PSSubject sub : members)
      {
         if (sub.getType() == PSSubject.SUBJECT_TYPE_USER)
            userNames.add(sub.getName());
         else
            groupNames.add(sub.getName());
      }
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testGetRoleAttributes()
   {
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testGetSubjectAttributes()
   {
   }

   /**
    * Class under test for List getSubjectGlobalAttributes(String, int, String, 
    * String)
    */
   public void testGetSubjectGlobalAttributesStringintStringString()
   {
   }

   /**
    * Class under test for List getSubjectGlobalAttributes(String, int, String, 
    * String, boolean)
    */
   public void testGetSubjectGlobalAttributesStringintStringStringboolean()
   {
   }

   /**
    * Class under test for List getSubjectGlobalAttributes(String, int, String, 
    * String, boolean, String)
    */
   public void testGetSubjectGlobalAttributesStringintStringStringbooleanString()
   {
   }

   /**
    * Class under test for Set getSubjects(String, String)
    */
   public void testGetSubjectsStringString()
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      Set<String> userNames = new HashSet<String>();
      Set<String> groupNames = new HashSet<String>();
      Collection<PSSubject> subjects;
      
      subjects = mgr.getSubjects("Editor", null);
      filterSubjectNames(subjects, userNames, groupNames);
      assertTrue(groupNames.isEmpty());
      assertTrue(userNames.contains("editor1"));
      assertTrue(userNames.contains("subCatUser2_1"));
      
      subjects = mgr.getSubjects("Editor", "ed%");
      filterSubjectNames(subjects, userNames, groupNames);
      assertTrue(groupNames.isEmpty());
      assertTrue(userNames.contains("editor1"));
      assertTrue(!userNames.contains("subCatUser2_1"));
      
      subjects = mgr.getSubjects(null, "ed%");
      filterSubjectNames(subjects, userNames, groupNames);
      assertTrue(groupNames.isEmpty());
      assertTrue(userNames.size() == 2);
      assertTrue(userNames.contains("editor1"));
      assertTrue(userNames.contains("editor2"));
      
      subjects = mgr.getSubjects(null, "subCatUser2%");
      filterSubjectNames(subjects, userNames, groupNames);
      assertTrue(groupNames.isEmpty());
      assertTrue(userNames.size() == 2);
      assertTrue(userNames.contains("subCatUser2_1"));
      assertTrue(userNames.contains("subCatUser2_2"));
      
      subjects = mgr.getSubjects(null, "subCatGroup%");
      filterSubjectNames(subjects, userNames, groupNames);
      assertTrue(userNames.isEmpty());
      assertTrue(groupNames.size() == 2);
      assertTrue(groupNames.contains("subCatGroup1"));
      assertTrue(groupNames.contains("subCatGroup2"));      
      
      subjects = mgr.getSubjects("", "");
      filterSubjectNames(subjects, userNames, groupNames);
      assertTrue(!groupNames.isEmpty());
      assertTrue(userNames.contains("editor1"));
      assertTrue(userNames.contains("editor2"));
      assertTrue(userNames.contains("subCatUser2_1"));
      assertTrue(userNames.contains("subCatUser2_2"));
      assertTrue(groupNames.contains("subCatGroup1"));
      assertTrue(groupNames.contains("subCatGroup2"));
   }

   /**
    * Class under test for Set getSubjects(String, String, int, String, String, boolean)
    */
   public void testGetSubjectsStringStringintStringStringboolean()
   {
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testGetSubjectRoleAttributes()
   {
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testGetRoleEmailAddresses()
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      Set noMailSet = new HashSet();
      Set<String> mailSet = new HashSet<String>();
      
      mailSet = mgr.getRoleEmailAddresses("Editor", null, null, noMailSet);
      assertTrue(mailSet.contains("subCatUser2_1@test.percussion.com"));
      assertTrue(mailSet.contains("subCatUser2_2@test.percussion.com"));
      assertEquals(mailSet, mgr.getRoleEmailAddresses("Editor", null, null, 
         null));
      Set<String> users = new HashSet<String>();
      Set<String> groups = new HashSet<String>();
      filterSubjectNames(noMailSet, users, groups);
      assertTrue(users.contains("editor1"));
      assertTrue(users.contains("editor2"));
      
      mailSet = mgr.getRoleEmailAddresses("Editor", "mail", "Default", null);
      assertTrue(mailSet.isEmpty());
      
      mailSet = mgr.getRoleEmailAddresses("foo", null, null, null);
      assertTrue(mailSet.isEmpty());
   }

   /**
    * Tests functionality provided by the {@link PSRoleManager} method specified
    * by this method's name.
    */
   public void testGetSubjectEmailAddresses()
   {
      PSRoleManager mgr = PSRoleManager.getInstance();
      
      Set mailSet = mgr.getSubjectEmailAddresses("subCatUser2_1", null, null);
      assertTrue(mailSet.size() == 1);
      assertTrue(mailSet.contains("subCatUser2_1@test.percussion.com"));
      assertEquals(mailSet, mgr.getSubjectEmailAddresses("subCatUser2_1", "foo", 
         null));
      mailSet = mgr.getSubjectEmailAddresses("subCatUser2_1", null, 
         "Default");
      assertTrue(mailSet.isEmpty());
   }
}

