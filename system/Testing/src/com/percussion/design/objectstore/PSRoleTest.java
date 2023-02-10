/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Basic role testing, including simple to/from Xml
 * checking w/ simple role classes.  Also includes
 * testing of accessor methods
 */
public class PSRoleTest extends TestCase
{
   /**
    * @see TestCase#TestCase(String)
    */
   public PSRoleTest(String name)
   {
      super(name);
   }

   // See super class
   public void setUp()
   {
   }

   /**
    * Test if the two supplied roles are equal.
    *
    * @param role1 role to compare
    *
    * @param role2 role to compare
    *
    * @return whether or not the supplied roles are equal
    */
   public static boolean testRoleEquals(PSRole role1, PSRole role2)
   {
      if ((role1 == null) || (role2 == null))
      {
         if ((role2 != null) || (role1 != null))
            return false;

         return true; // null == null
      }

      // Check if the names are equal
      if (!role1.getName().equals(role2.getName()))
         return false;

      // check subjects of role1 for corresponding subjects in role2
      if (role1.getSubjects().size() != role2.getSubjects().size())
         return false;

      // By using the following loop we test the containsCorrespondingSubject
      // as well.
      Iterator i1 = role1.getSubjects().iterator();
      while (i1.hasNext())
      {
         PSRelativeSubject rs1 = (PSRelativeSubject) i1.next();
         if (!role2.containsCorrespondingSubject(rs1))
            return false;
      }

      // Now ensure all subjects match !
      i1 = role1.getSubjects().iterator();
      while (i1.hasNext())
      {
         PSRelativeSubject rs1 = (PSRelativeSubject) i1.next();
         Iterator i2 = role2.getSubjects().iterator();

         while (i2.hasNext())
         {
            PSRelativeSubject rs2 = (PSRelativeSubject) i2.next();
            if (rs1.isMatch(rs2))
            {
               if (!PSRelativeSubjectTest.testRelativeSubjectEquals(rs1, rs2))
                  return false;
               break;
            }
         }
      }

      if (role1.getAttributes().size() != role2.getAttributes().size())
         return false;

      i1 = role1.getAttributes().iterator();
      while (i1.hasNext())
      {
         PSAttribute att1 = (PSAttribute) i1.next();
         PSAttribute att2 = role2.getAttributes().getAttribute(att1.getName());
         if (!PSAttributeTest.testAttributeEquals(att1, att2))
            return false;
      }

      return true;      // equal!
   }

   /**
    * Assert that the two supplied roles are equal.
    *
    * @param role1 role to compare
    *
    * @param role2 role to compare
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void assertRoleEquals(PSRole role1, PSRole role2) throws Exception
   {
      assertTrue(testRoleEquals(role1, role2));
   }

   /**
    * Test if two newly constructed roles (empty ctor) are equal
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testEmptyEquals() throws Exception
   {
      PSRole role = new PSRole();
      PSRole otherRole = new PSRole();
      assertRoleEquals(role, otherRole);
   }

   /**
    * Test if two newly constructed roles (name ctor) are equal
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testNameTypeConstructor() throws Exception
   {
      PSRole role =
         new PSRole("foo");

      assertEquals(role.getName(), "foo");

      PSRole otherRole =
         new PSRole("foo");

      assertRoleEquals(role, otherRole);

      boolean didThrow = false;
      try
      {
         role = new PSRole(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         role = new PSRole("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      StringBuilder name = new StringBuilder();
      for (int i = 0; i <= PSRole.MAX_ROLE_NAME_LEN; i++)
         name.append('a');

      didThrow = false;
      try
      {
         role = new PSRole(name.toString());
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   /**
    * Test getName() and setName() on a role
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testGetSetName() throws Exception
   {
      PSRole role = new PSRole();
      boolean didThrow = false;
      try
      {
         role.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         role.setName("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      StringBuilder name = new StringBuilder();
      for (int i = 0; i <= PSRole.MAX_ROLE_NAME_LEN; i++)
         name.append('a');

      didThrow = false;
      try
      {
         role.setName(name.toString());
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      role.setName("foobar");
      assertEquals(role.getName(), "foobar");
   }

   /**
    * Test to and from xml methods (roundtrip) of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testXml() throws Exception
   {
      PSRole role = new PSRole();
      PSRole otherRole = new PSRole();
      assertRoleEquals(role, otherRole);

      role.setName("foobar");
      assertTrue(!testRoleEquals(role,otherRole));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // set a name and verify to/from loop
      role.setName("taebo");
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // add a subject and verify to/from loop
      role.getSubjects().add(new PSRelativeSubject("fred",
               PSSubject.SUBJECT_TYPE_USER, null) );
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      ArrayList l = new ArrayList();
      l.add("one");
      l.add("two");
      l.add("three");

      // add an attribute and verify to/from loop
      role.getAttributes().setAttribute("fred",l);
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // add a second attribute and verify to/from loop
      role.getAttributes().setAttribute("joe",l);
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // add a second subject and verify to/from loop
      role.getSubjects().add(new PSRelativeSubject("joe",
         PSSubject.SUBJECT_TYPE_USER, null) );
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);
      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // remove subject and verify to/from loop
      role.getSubjects().removeElementAt(0);
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // remove last remaining subject and verify to/from loop
      role.getSubjects().removeElementAt(0);
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // remove attribute and verify to/from loop
      role.getAttributes().removeElementAt(0);
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);

      // remove last remaining attribute and verify to/from loop
      role.getAttributes().removeElementAt(0);
      assertTrue(!testRoleEquals(role,otherRole));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = role.toXml(doc);
      doc.appendChild(el);

      otherRole.fromXml(el, null, null);
      assertRoleEquals(role, otherRole);
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
      suite.addTest(new PSRoleTest("testNameTypeConstructor"));
      suite.addTest(new PSRoleTest("testGetSetName"));
      suite.addTest(new PSRoleTest("testXml"));
      return suite;
   }
}

