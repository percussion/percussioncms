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
package com.percussion.services.security;

import com.percussion.security.PSGroupEntry;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSUserAttributes;
import com.percussion.security.PSUserEntry;
import com.percussion.services.security.loginmods.data.PSGroup;
import com.percussion.services.security.loginmods.data.PSPrincipal;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for utils
 * @author dougrand
 */
public class PSJaasUtilsTest extends TestCase
{

   /*
    * (non-Javadoc)
    * 
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      ms_testData = new ArrayList<Principal>();
      ms_testRoleGroup = new PSGroup("Roles");

   }

   /**
    * Constructor for PSJaasUtillsTest.
    * 
    * @param arg0
    */
   public PSJaasUtilsTest(String arg0) {
      super(arg0);
   }

   /**
    * Suite method
    * 
    * @return The test suite, never <code>null</code>.
    */
   public static TestSuite suite()
   {
      return new TestSuite(PSJaasUtilsTest.class);
   }

   /**
    * Test data intiialized during {@link #setUp()}, never <code>null</code> 
    * after that.
    */
   static Collection<Principal> ms_testData;

   /**
    * Test group initialized during {@link #setUp()}, never <code>null</code> 
    * after that. 
    */
   static Group ms_testRoleGroup;

   /**
    * Test locating the group containing role names.
    * 
    * @throws Exception if the test failes.
    */
   public void testFindRoleGroup() throws Exception
   {
      ms_testData.add(ms_testRoleGroup);

      Group r = PSJaasUtils.findOrCreateGroup(ms_testData, 
         PSJaasUtils.ROLE_GROUP_NAME);
      assertTrue(r == ms_testRoleGroup);

      ms_testData.remove(ms_testRoleGroup);

      r = PSJaasUtils.findOrCreateGroup(ms_testData, 
         PSJaasUtils.ROLE_GROUP_NAME);
      assertTrue(!(r == ms_testRoleGroup));
   }
   
   /**
    * Test various collection operations.
    * 
    * @throws Exception if the test fails.
    */
   @SuppressWarnings(value={"unchecked"})
   public void testCollectionResults() throws Exception
   {
      ms_testData.add(new PSPrincipal("user"));
      ms_testData.add(new PSPrincipal("grape"));
      ms_testData.add(new PSPrincipal("orange"));

      Iterator<Principal> rval;

      Principal u = PSJaasUtils.findFirstPSPrincipal(ms_testData);
      assertTrue(u == ms_testData.iterator().next());

      rval = 
         new FilterIterator(ms_testData.iterator(), new Predicate()
      {
         public boolean evaluate(Object principal)
         {
            return true;
         }

      });
      
      Iterator<Principal> td = ms_testData.iterator();
      
      assertEquals(rval.next(), td.next());
      assertEquals(rval.next(), td.next());
      assertEquals(rval.next(), td.next());
      assertTrue(rval.hasNext() == false);
      assertTrue(td.hasNext() == false);
   }

   /**
    * Test converting a subject to a user entry
    * 
    * @throws Exception if the test fails.
    */
   @SuppressWarnings(value={"unchecked"})
   public void testSubjectToEntry() throws Exception
   {
      PSGroupEntry group1 = new PSGroupEntry("group1", 0);
      PSGroupEntry group2 = new PSGroupEntry("group2", 0);
      PSGroupEntry[] groups = new PSGroupEntry[] {group1, group2};
      
      PSRoleEntry role1 = new PSRoleEntry("role1", 0);
      PSRoleEntry role2 = new PSRoleEntry("role2", 0);
      PSRoleEntry[] roles = new PSRoleEntry[] {role1, role2};
      
      
      PSUserAttributes attrs = new PSUserAttributes();
      attrs.put("attr1", "attval1");
      attrs.put("attr2", "attval2");
      
      String username = "admin1";
      String pwd = "demo";
      PSUserEntry entry = new PSUserEntry(username, 0, groups, roles, attrs, 
         PSUserEntry.createSignature(username, pwd));
      Subject sub = PSJaasUtils.userEntryToSubject(entry, pwd);
      PSUserEntry entry2 = PSJaasUtils.subjectToUserEntry(sub, username, pwd);
      assertEquals(entry, entry2);
      assertEquals(sub, PSJaasUtils.userEntryToSubject(entry2, pwd));
      
      entry = new PSUserEntry(username, 0, groups, roles, 
         new PSUserAttributes(), PSUserEntry.createSignature(username, pwd));
      assertFalse(sub.equals(PSJaasUtils.userEntryToSubject(entry, pwd)));
      
      sub.getPrincipals().clear();
      assertFalse(entry2.equals(PSJaasUtils.subjectToUserEntry(sub, username, 
         pwd)));
      
      entry = new PSUserEntry(username, 0, groups, new PSRoleEntry[0], attrs, 
         PSUserEntry.createSignature(username, pwd));
      assertFalse(sub.equals(PSJaasUtils.userEntryToSubject(entry, pwd)));
      
      sub.getPrincipals().clear();
      assertFalse(entry2.equals(PSJaasUtils.subjectToUserEntry(sub, username, 
         pwd)));      
   }
}
