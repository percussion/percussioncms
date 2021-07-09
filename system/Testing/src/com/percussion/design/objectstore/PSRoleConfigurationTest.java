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

package com.percussion.design.objectstore;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Basic role confiuration testing, including simple to/from Xml
 * checking w/ basic roleconfigurations.
 */
public class PSRoleConfigurationTest extends TestCase
{
   /**
    * @see TestCase#TestCase(String)
    */
   public PSRoleConfigurationTest(String name)
   {
      super(name);
   }

   // See super class for description
   public void setUp()
   {

   }

   /**
    * Test if the two supplied roles configurations are equal.
    *
    * @param rc1 role to compare
    *
    * @param rc2 role to compare
    *
    * @return <code>true</code> if the supplied role configurations are equal,
    * otherwise <code>false</code>.
    */
   public static boolean testRoleConfigurationEquals(PSRoleConfiguration rc1,
      PSRoleConfiguration rc2)
   {
      if ((rc1 == null) || (rc2 == null))
      {
         if ((rc2 != null) || (rc1 != null))
            return false;

         return true; // null == null
      }

      // make sure the two role configs have the same number of global subjects
      int rc1Size = 0;
      Iterator i1 = rc1.getSubjects();
      while (i1.hasNext())
      {
         rc1Size++;
      }
      int rc2Size = 0;
      i1 = rc2.getSubjects();
      while (i1.hasNext())
      {
         rc2Size++;
      }
      if (rc1Size != rc2Size) return false;

      i1 = rc1.getSubjects();
      while (i1.hasNext())
      {
         PSGlobalSubject gs1 = (PSGlobalSubject) i1.next();

         PSGlobalSubject gs2 = rc2.getGlobalSubject(gs1.makeRelativeSubject(),
             false);

         if (!PSGlobalSubjectTest.testGlobalSubjectEquals(gs1, gs2))
            return false;
      }

      if (rc1.getRoles().size() != rc2.getRoles().size())
         return false;

      i1 = rc1.getRoles().iterator();
      while (i1.hasNext())
      {
         PSRole role1 = (PSRole) i1.next();
         Iterator i2 = rc2.getRoles().iterator();
         boolean found = false;
         while (i2.hasNext())
         {
            PSRole role2 = (PSRole) i2.next();
            if (role1.getName().equals(role2.getName()))
            {
               found = true;
               if (!PSRoleTest.testRoleEquals(role1, role2))
                  return false;
               break;
            }
         }
         if (!found)
         {
            return false;
         }
      }

      return true;      // equal!
   }

   /**
    * Assert that the two supplied role configurations are equal.
    *
    * @param rc1 role config to compare
    *
    * @param rc2 role config to compare
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void assertRoleConfigurationEquals(PSRoleConfiguration rc1,
      PSRoleConfiguration rc2) throws Exception
   {
      assertTrue(testRoleConfigurationEquals(rc1, rc2));
   }

   /**
    * Test if two newly constructed roles (empty ctor) are equal.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testEmptyEquals() throws Exception
   {
      PSRoleConfiguration rc1 = new PSRoleConfiguration();
      PSRoleConfiguration rc2 = new PSRoleConfiguration();
      assertRoleConfigurationEquals(rc1, rc2);
   }

   /**
    * Test to and from xml methods (roundtrip) of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testXml() throws Exception
   {
      PSRoleConfiguration rc1 = new PSRoleConfiguration();
      PSRoleConfiguration rc2 = new PSRoleConfiguration();

      assertRoleConfigurationEquals(rc1, rc2);

      // make some roles
      PSRole role1 = new PSRole("Role1");
      PSRole role2 = new PSRole("Role2");

      // add atts to role 1
      ArrayList values = new ArrayList();
      values.add("a1");
      values.add("a2");
      values.add("a3");
      role1.getAttributes().setAttribute("role1att1", values);
      values.add("a4");
      role1.getAttributes().setAttribute("role1att2", values);

      rc1.getRoles().add(role1);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      Document doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // add atts to role 2
      values.clear();
      values.add("b1");
      values.add("b2");
      values.add("b3");
      role2.getAttributes().setAttribute("role2att1", values);
      values.add("b4");
      role2.getAttributes().setAttribute("role2att2", values);

      // add the roles to rc1
      rc1.getRoles().add(role2);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // add a subject to the first role
      PSRelativeSubject sub = new PSRelativeSubject("sub1",
         PSSubject.SUBJECT_TYPE_USER, null);
      role1.getSubjects().add(sub);

      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // add a second subject to the first role & test again
      PSRelativeSubject sub2 = new PSRelativeSubject("sub2",
         PSSubject.SUBJECT_TYPE_USER, null);
      role1.getSubjects().add(sub2);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // Add some global attributes to the role configuration for the user
      PSGlobalSubject gs = rc1.getGlobalSubject(sub, true);
      gs.getAttributes().setAttribute("sub1globalatt1", values);
      gs.getAttributes().setAttribute("sub1globalatt2", values);
      gs.getAttributes().setAttribute("sub1globalatt3", values);
      gs.getAttributes().setAttribute("sub1globalatt4", values);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // remove a global attribute and test again...
      gs.getAttributes().removeElementAt(2);
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // remove all global attributes and test again...
      for (int i = gs.getAttributes().size() - 1; i >=0; i--)
         gs.getAttributes().removeElementAt(i);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // remove all role1's subjects and test again...
      for (int i = role1.getSubjects().size() - 1; i >=0; i--)
         role1.getSubjects().removeElementAt(i);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);

      // Remove role1 and test again...
      rc1.getRoles().remove(role1);

      // assert that they are not equal
      assertTrue(!testRoleConfigurationEquals(rc1, rc2));

      // to and from xml & assert
      doc = rc1.toXml();
      rc2 = new PSRoleConfiguration(doc);
      assertRoleConfigurationEquals(rc1, rc2);
    }

   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return The suite containing all tests for this class.
    * Not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSRoleTest("testEmptyEquals"));
      suite.addTest(new PSRoleTest("testXml"));
      return suite;
   }
}

