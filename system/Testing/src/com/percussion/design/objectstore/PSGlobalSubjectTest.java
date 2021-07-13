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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Basic global subject testing, including simple to/from Xml
 * checking w/ simple global subject classes.  Also includes
 * testing of accessor methods
 */
public class PSGlobalSubjectTest extends TestCase
{
   /**
    * @see TestCase#TestCase(String)
    */
   public PSGlobalSubjectTest(String name)
   {
      super(name);
   }

   // see super class
   public void setUp()
   {
   }

   /**
    * Test if the two supplied global subjects are equal.
    *
    * @param sub1 global subject to compare
    *
    * @param sub2 global subject to compare
    */
   public static boolean testGlobalSubjectEquals(
      PSGlobalSubject sub1, PSGlobalSubject sub2)
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

         /* Get the corresponding att based on att name */
         PSAttribute att2 = sub2.getAttributes().getAttribute(att1.getName());
         if (!PSAttributeTest.testAttributeEquals(att1, att2))
            return false;
      }

      return true;      // equal!
   }

   /**
    * Assert that the two supplied global subjects are equal.
    *
    * @param sub1 subject to compare
    *
    * @param sub2 subject to compare
    */
   public void assertGlobalSubjectEquals(
      PSGlobalSubject sub1, PSGlobalSubject sub2) throws Exception
   {
      assertTrue(testGlobalSubjectEquals(sub1, sub2));
   }

   /**
    * Test if two newly constructed global subjects (empty ctor) are equal.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testEmptyEquals() throws Exception
   {
      PSGlobalSubject sub = new PSGlobalSubject();
      PSGlobalSubject otherSub = new PSGlobalSubject();
      assertGlobalSubjectEquals(sub, otherSub);
   }

   /**
    * Test if two newly constructed global subjects
    *  (name, type, and sp ctor) are equal. Test
    *  construction with invalid parameters.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testNameTypeConstructor() throws Exception
   {
      PSGlobalSubject sub =
         new PSGlobalSubject("foo", PSSubject.SUBJECT_TYPE_USER, null);

      assertEquals(sub.getName(), "foo");

      PSGlobalSubject otherSub =
         new PSGlobalSubject("foo", PSSubject.SUBJECT_TYPE_USER, null);

      assertGlobalSubjectEquals(sub, otherSub);

      // Invalid name, null
      boolean didThrow = false;
      try
      {
         sub =
            new PSGlobalSubject(null, PSSubject.SUBJECT_TYPE_USER, null);
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
         sub = new PSGlobalSubject("", PSSubject.SUBJECT_TYPE_USER, null);
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

         sub = new PSGlobalSubject(name.toString(),
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
         sub = new PSGlobalSubject("validName", 0, null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }


   /**
    * Test using accessors to set illegal values.
    * Ensure they throw exceptions as appropriate.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testIllegalAccessorCalls() throws Exception
   {
      PSGlobalSubject sub = new PSGlobalSubject();
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
    * Test to and from xml methods (roundtrup) of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testXml() throws Exception
   {
      PSGlobalSubject sub = new PSGlobalSubject();
      PSGlobalSubject otherSub = new PSGlobalSubject();
      assertGlobalSubjectEquals(sub, otherSub);

      // block 1
      sub.setName("foobar");
      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);

      // set a name and verify to/from loop
      sub.setName("taebo");
      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);

      // Add an empty att and verify to/from loop
      sub.getAttributes().add( new PSAttribute("att1") );
      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);

      // Add a second att and verify to/from loop
      sub.getAttributes().add( new PSAttribute("att2") );
      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);

      // Add a third att w/ values and verify to/from loop
      ArrayList l = new ArrayList();
      l.add("one");
      l.add("two");
      l.add("three");
      PSAttribute att = new PSAttribute("att3");
      att.setValues(l);
      sub.getAttributes().add(att);

      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);

      // Remove att and verify to/from loop
      sub.getAttributes().removeElementAt(0);
      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);

      // Remove last att and verify to/from loop
      sub.getAttributes().removeElementAt(0);
      assertTrue(!testGlobalSubjectEquals(sub,otherSub));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = sub.toXml(doc);
      doc.appendChild(el);

      otherSub.fromXml(el, null, null);
      assertGlobalSubjectEquals(sub, otherSub);
   }

   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return The suite of tests for this class, not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSGlobalSubjectTest("testEmptyEquals"));
      suite.addTest(new PSGlobalSubjectTest("testNameTypeConstructor"));
      suite.addTest(new PSGlobalSubjectTest("testIllegalAccessorCalls"));
      suite.addTest(new PSGlobalSubjectTest("testXml"));
      return suite;
   }
}

