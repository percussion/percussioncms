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

