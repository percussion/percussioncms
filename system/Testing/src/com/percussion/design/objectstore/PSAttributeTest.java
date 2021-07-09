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

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Basic attribute testing, including simple to/from Xml
 * checking w/ simple attribute classes.  Also includes 
 * testing of accessor methods
 */
public class PSAttributeTest extends TestCase
{
   /**
    * @see TestCase#TestCase(String)
    */
   public PSAttributeTest(String name)
   {
      super(name);
   }

   // See super class for more info
   public void setUp()
   {
   }

   /**
    * Test if the two supplied attributes are equal.
    *
    * @param att1 attribute to compare
    *
    * @param att2 attribute to compare
    */
   public static boolean testAttributeEquals(
      PSAttribute att1, PSAttribute att2)
   {
      if ((att1 == null) || (att2 == null))
      {
         if ((att2 != null) || (att1 != null))
            return false;

         return true; // null == null
      }

      // Check if the names are equal
      if (!att1.getName().equals(att2.getName()))
      {
         return false;
      }

      if (att1.size() != att2.size())
      {
         return false;
      }

      for (int i = 0; i < att1.size(); i++)
      {
         PSAttributeValue val1 = (PSAttributeValue) att1.get(i);
         PSAttributeValue val2 = (PSAttributeValue) att2.get(i);

         if (!val1.getValueText().equals(val2.getValueText()))
         {
            return false;
         }
      }

      return true;      // equal!
   }

   /**
    * Assert that the two supplied atts are equal.
    *
    * @param att1 att to compare
    *
    * @param att2 att to compare
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void assertAttributeEquals(
      PSAttribute att1, PSAttribute att2) throws Exception
   {
      assertTrue(testAttributeEquals(att1, att2));
   }

   /**
    * Test if two newly constructed atts (empty ctor) are equal
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testEmptyEquals() throws Exception
   {
      PSAttribute att = new PSAttribute();
      PSAttribute otherAtt = new PSAttribute();
      assertAttributeEquals(att, otherAtt);
   }

   /**
    * Test if two newly constructed atts (name ctor) are equal
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testNameTypeConstructor() throws Exception
   {
      PSAttribute att =
         new PSAttribute("foo");

      assertEquals(att.getName(), "foo");

      PSAttribute otherAtt =
         new PSAttribute("foo");

      assertAttributeEquals(att, otherAtt);

      boolean didThrow = false;
      try
      {
         att = new PSAttribute(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         att = new PSAttribute("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   /**
    * Test to and from xml methods (round trip) of this os object.
    *
    * @throws Exception If any exceptions occur or assertions fail.
    */
   public void testXml() throws Exception
   {
      PSAttribute att = new PSAttribute("foobar");
      PSAttribute otherAtt = new PSAttribute();

      assertTrue(!testAttributeEquals(att,otherAtt));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = att.toXml(doc);
      doc.appendChild(el);

      otherAtt.fromXml(el, null, null);
      assertAttributeEquals(att, otherAtt);

      // use different name and verify to/from loop
      att = new PSAttribute("taebo");
      assertTrue(!testAttributeEquals(att,otherAtt));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = att.toXml(doc);
      doc.appendChild(el);

      otherAtt.fromXml(el, null, null);
      assertAttributeEquals(att, otherAtt);

      ArrayList val = new ArrayList();
      val.add("one");
      val.add("two");
      val.add("three");

      // add a value and verify to/from loop
      att.setValues(val);
      assertTrue(!testAttributeEquals(att,otherAtt));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = att.toXml(doc);
      doc.appendChild(el);

      otherAtt.fromXml(el, null, null);
      assertAttributeEquals(att, otherAtt);

      // add a different value (one item list) and verify to/from loop
      ArrayList l = new ArrayList();
      l.add("one");
      att.setValues(l);

      assertTrue(!testAttributeEquals(att,otherAtt));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = att.toXml(doc);
      doc.appendChild(el);
      otherAtt.fromXml(el, null, null);
      assertAttributeEquals(att, otherAtt);

      // add a different value (including a null value) and verify to/from loop
      l.add(null);
      l.add("three");
      att.setValues(l);
      assertTrue(!testAttributeEquals(att,otherAtt));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = att.toXml(doc);
      doc.appendChild(el);

      otherAtt.fromXml(el, null, null);
      assertAttributeEquals(att, otherAtt);

      // Now try a null value and verify to/from loop
      att.setValues(null);
      assertTrue(!testAttributeEquals(att,otherAtt));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = att.toXml(doc);
      doc.appendChild(el);

      otherAtt.fromXml(el, null, null);
      assertAttributeEquals(att, otherAtt);
   }

   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return the suite of all tests for this class. Not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSAttributeTest("testEmptyEquals"));
      suite.addTest(new PSAttributeTest("testNameTypeConstructor"));
      suite.addTest(new PSAttributeTest("testXml"));
      return suite;
   }
}

