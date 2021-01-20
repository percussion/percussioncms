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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSAclEntryTest extends TestCase
{
   public PSAclEntryTest(String name)
   {
      super(name);
   }

   public void setUp()
   {

   }

   public void testEmptyEquals() throws Exception
   {
      PSAclEntry entry = new PSAclEntry();
      PSAclEntry otherEntry = new PSAclEntry();
      assertEquals(entry, otherEntry);
   }

   public void testNameTypeConstructor() throws Exception
   {
      PSAclEntry entry =
         new PSAclEntry("foo", PSAclEntry.ACE_TYPE_USER);

      assertEquals(entry.getName(), "foo");
      assertTrue(entry.isUser());

      PSAclEntry otherEntry =
         new PSAclEntry("foo", PSAclEntry.ACE_TYPE_USER);

      assertEquals(entry, otherEntry);

      boolean didThrow = false;
      try
      {
         entry = new PSAclEntry(null, PSAclEntry.ACE_TYPE_USER);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         entry = new PSAclEntry("", PSAclEntry.ACE_TYPE_USER);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         String name = "0123456789";
         for (int i = 0; i < 100; i++)
         {
            name += "0123456789";
         }
         entry = new PSAclEntry(name, PSAclEntry.ACE_TYPE_USER);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

   }

   public void testGetSetName() throws Exception
   {
      PSAclEntry entry = new PSAclEntry();
      boolean didThrow = false;
      try
      {
         entry.setName(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         entry.setName("");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         String name = "0123456789";
         for (int i = 0; i < 100; i++)
         {
            name += "0123456789";
         }
         entry.setName(name);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      entry.setName("foobar");
      assertEquals(entry.getName(), "foobar");
   }

   public void testXml() throws Exception
   {
      PSAclEntry entry = new PSAclEntry();
      PSAclEntry otherEntry = new PSAclEntry();
      assertEquals(entry, otherEntry);

      // block 1
      entry.setAccessLevel(PSAclEntry.SACE_ACCESS_DATA);
      entry.setName("foobar");
      assertTrue(!entry.equals(otherEntry));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = entry.toXml(doc);
      doc.appendChild(el);

      otherEntry.fromXml(el, null, null);
      assertEquals(entry, otherEntry);

      // block 2
      entry.setAccessLevel(PSAclEntry.AACE_DATA_QUERY);
      entry.setName("taebo");
      assertTrue(!entry.equals(otherEntry));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = entry.toXml(doc);
      doc.appendChild(el);

      otherEntry.fromXml(el, null, null);
      assertEquals(entry, otherEntry);

   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSAclEntryTest("testEmptyEquals"));
      suite.addTest(new PSAclEntryTest("testNameTypeConstructor"));
      suite.addTest(new PSAclEntryTest("testGetSetName"));
      suite.addTest(new PSAclEntryTest("testXml"));
      return suite;
   }
}

