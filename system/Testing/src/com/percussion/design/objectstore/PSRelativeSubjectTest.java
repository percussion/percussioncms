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

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * Basic relative subject testing, including simple to/from Xml
 * checking w/ simple relative subject classes.  Also includes
 * testing of accessor methods
 */
public class PSRelativeSubjectTest extends TestCase
{
   /**
    * Constructor to call base class constructor.
    *
    * @see TestCase#TestCase(String) for more information.
    */
   public PSRelativeSubjectTest(String name)
   {
      super(name);
   }

   // See base class
   public void setUp()
   {

   }

   /**
    * Test if the two supplied relative subjects are equal.
    *
    * @param role1 role to compare
    *
    * @param role2 role to compare
    *
    * @return <code>true</code> if the two subjects are equal,
    * <code>false</code> otherwise
    */
   public static boolean testRelativeSubjectEquals(
      PSRelativeSubject sub1, PSRelativeSubject sub2)
   {
      if ((sub1 == null) || (sub2 == null))
      {
         if ((sub2 != null) || (sub1 != null))
            return false;

         return true; // null == null
      }

      if (!sub1.isMatch(sub2))
         return false;

      if (sub1.getAttributes().size() != sub2.getAttributes().size())
         return false;

      Iterator i1 = sub1.getAttributes().iterator();
      while (i1.hasNext())
      {
         PSAttribute att1 = (PSAttribute) i1.next();
         PSAttribute att2 = sub2.getAttributes().getAttribute(att1.getName());
         if (!PSAttributeTest.testAttributeEquals(att1, att2))
            return false;
      }

      return true;      // equal!
   }

   /**
    * Assert that the two supplied relative subjects are equal.
    *
    * @param sub1 subject to compare
    *
    * @param sub2 subject to compare
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void assertRelativeSubjectEquals(
      PSRelativeSubject sub1, PSRelativeSubject sub2) throws Exception
   {
      assertTrue(testRelativeSubjectEquals(sub1, sub2));
   }

   /**
    * Test if two newly constructed relative subjects (empty ctor) are equal
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testEmptyEquals() throws Exception
   {
      PSRelativeSubject sub = new PSRelativeSubject();
      PSRelativeSubject otherSub = new PSRelativeSubject();
      assertRelativeSubjectEquals(sub, otherSub);
   }

   /**
    * Test if two newly constructed relative subjects
    *  (name, type, and sp ctor) are equal, also test
    *  construction with invalid parameters
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testNameTypeConstructor() throws Exception
   {
      PSRelativeSubject sub =
         new PSRelativeSubject("foo", PSSubject.SUBJECT_TYPE_USER, null);

      assertEquals(sub.getName(), "foo");

      PSRelativeSubject otherSub =
         new PSRelativeSubject("foo", PSSubject.SUBJECT_TYPE_USER, null);

      assertRelativeSubjectEquals(sub, otherSub);

      // Invalid name, null
      boolean didThrow = false;
      try
      {
         sub =
            new PSRelativeSubject(null, PSSubject.SUBJECT_TYPE_USER, null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // Invalid name, empty
      didThrow = false;
      try
      {
         sub = new PSRelativeSubject("", PSSubject.SUBJECT_TYPE_USER, null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // Invalid name, too long
      didThrow = false;
      try
      {
         StringBuilder name = new StringBuilder();
         for (int i = 0; i <= PSSubject.SUBJECT_MAX_NAME_LEN; i++)
            name.append('a');

         sub = new PSRelativeSubject(name.toString(),
            PSSubject.SUBJECT_TYPE_USER, null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // Invalid subject type, 0
      didThrow = false;
      try
      {
         sub = new PSRelativeSubject("validName", 0, null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   /**
    * Test using accessors to set illegal values
    *    (setName/setSecurityProviderType) - ensure they throw
    * exceptions appropriately.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testIllegalAccessorCalls() throws Exception
   {
      PSRelativeSubject sub = new PSRelativeSubject();
      boolean didThrow = false;
      try
      {
         sub.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         sub.setName("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // Invalid name, too long
      StringBuilder name = new StringBuilder();
      for (int i = 0; i <= PSSubject.SUBJECT_MAX_NAME_LEN; i++)
         name.append('a');
      didThrow = false;
      try
      {
         sub.setName(name.toString());
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      sub.setName("foobar");
      assertEquals(sub.getName(), "foobar");
   }

   /**
    * Test to and from xml methods of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testXml() throws Exception
   {
      PSRelativeSubject sub = new PSRelativeSubject();
      PSRelativeSubject otherSub = new PSRelativeSubject();
      assertRelativeSubjectEquals(sub, otherSub);

      // block 1
      sub.setName("foobar");
      assertTrue(!testRelativeSubjectEquals(sub,otherSub));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertRelativeSubjectEquals(sub, otherSub);

      // set a name and verify to/from loop
      sub.setName("taebo");
      assertTrue(!testRelativeSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertRelativeSubjectEquals(sub, otherSub);

      // Add an empty att and verify to/from loop
      sub.getAttributes().add( new PSAttribute("att1") );
      assertTrue(!testRelativeSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertRelativeSubjectEquals(sub, otherSub);

      // Add a second att and verify to/from loop
      sub.getAttributes().add( new PSAttribute("att2") );
      assertTrue(!testRelativeSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertRelativeSubjectEquals(sub, otherSub);

      // Remove att and verify to/from loop
      sub.getAttributes().removeElementAt(0);
      assertTrue(!testRelativeSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertRelativeSubjectEquals(sub, otherSub);

      // Remove last att and verify to/from loop
      sub.getAttributes().removeElementAt(0);
      assertTrue(!testRelativeSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertRelativeSubjectEquals(sub, otherSub);
   }

   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return The suite of test methods for this class.  Not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSRelativeSubjectTest("testEmptyEquals"));
      suite.addTest(new PSRelativeSubjectTest("testNameTypeConstructor"));
      suite.addTest(new PSRelativeSubjectTest("testIllegalAccessorCalls"));
      suite.addTest(new PSRelativeSubjectTest("testXml"));
      return suite;
   }
}

