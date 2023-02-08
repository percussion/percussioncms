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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

public class PSAclTest extends TestCase
{
   public PSAclTest(String name)
   {
      super(name);
   }

   public void setUp()
   {

   }

   public void testDefaultConstructor() throws Exception
   {
      PSAcl acl = new PSAcl();
      PSCollection entries = acl.getEntries();
      assertTrue("Entries not null", entries != null);
      assertTrue("Entries empty", 0 == entries.size());
      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      PSAcl otherAcl = new PSAcl();
      assertEquals("Two empty acl's are equal", acl, otherAcl);
      assertEquals(otherAcl, acl);
   }

   public void testGetSetMultiMemberAccess() throws Exception
   {
      PSAcl acl = new PSAcl();
      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      acl.setAccessForMultiMembershipMaximum();
      assertTrue(acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      acl.setAccessForMultiMembershipMergedMaximum();
      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      acl.setAccessForMultiMembershipMinimum();
      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      acl.setAccessForMultiMembershipMergedMinimum();
      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(acl.isAccessForMultiMembershipMergedMinimum());
   }

   public void testXml() throws Exception
   {
      PSAcl acl = new PSAcl();
      PSAcl otherAcl = new PSAcl();
      assertEquals(acl, otherAcl);

      IPSDocument doc = new DocumentContainer();
      doc.toXml().appendChild(acl.toXml(doc.toXml()));
      otherAcl.fromXml(doc.toXml().getDocumentElement(), doc, null);
      assertEquals(acl, otherAcl);

      // block 1
      acl.setAccessForMultiMembershipMaximum();
      assertTrue(!acl.equals(otherAcl));
      doc = new DocumentContainer();
      doc.toXml().appendChild(acl.toXml(doc.toXml()));
      otherAcl.fromXml(doc.toXml().getDocumentElement(), doc, null);
      assertTrue(acl.equals(otherAcl));

      assertTrue(acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      assertTrue(otherAcl.isAccessForMultiMembershipMaximum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMinimum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMergedMinimum());

      // block 2
      acl.setAccessForMultiMembershipMergedMaximum();
      assertTrue(!acl.equals(otherAcl));
      doc = new DocumentContainer();
      doc.toXml().appendChild(acl.toXml(doc.toXml()));
      otherAcl.fromXml(doc.toXml().getDocumentElement(), doc, null);
      assertTrue(acl.equals(otherAcl));

      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      assertTrue(!otherAcl.isAccessForMultiMembershipMaximum());
      assertTrue(otherAcl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMinimum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMergedMinimum());

      // block 3
      acl.setAccessForMultiMembershipMinimum();
      assertTrue(!acl.equals(otherAcl));
      doc = new DocumentContainer();
      doc.toXml().appendChild(acl.toXml(doc.toXml()));
      otherAcl.fromXml(doc.toXml().getDocumentElement(), doc, null);
      assertTrue(acl.equals(otherAcl));

      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(acl.isAccessForMultiMembershipMinimum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMinimum());

      assertTrue(!otherAcl.isAccessForMultiMembershipMaximum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(otherAcl.isAccessForMultiMembershipMinimum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMergedMinimum());

      // block 4
      acl.setAccessForMultiMembershipMergedMinimum();
      assertTrue(!acl.equals(otherAcl));
      doc = new DocumentContainer();
      doc.toXml().appendChild(acl.toXml(doc.toXml()));
      otherAcl.fromXml(doc.toXml().getDocumentElement(), doc, null);
      assertTrue(acl.equals(otherAcl));

      assertTrue(!acl.isAccessForMultiMembershipMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!acl.isAccessForMultiMembershipMinimum());
      assertTrue(acl.isAccessForMultiMembershipMergedMinimum());

      assertTrue(!otherAcl.isAccessForMultiMembershipMaximum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMergedMaximum());
      assertTrue(!otherAcl.isAccessForMultiMembershipMinimum());
      assertTrue(otherAcl.isAccessForMultiMembershipMergedMinimum());
   }

   public void testSetEntriesNull() throws Exception
   {
      boolean didThrow = false;
      PSAcl acl = new PSAcl();
      try
      {
         acl.setEntries(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   public void testSetEntriesWrongType() throws Exception
   {
      boolean didThrow = false;
      PSAcl acl = new PSAcl();
      try
      {
         acl.setEntries(new PSCollection(this.getClass().getName()));
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   public void testSetEntriesWithDuplicates() throws Exception
   {
      boolean didThrow = false;
      PSAcl acl = new PSAcl();
      PSCollection entries = new PSCollection(com.percussion.design.objectstore.PSAclEntry.class);
      entries.add(new PSAclEntry());
      entries.add(new PSAclEntry());
      try
      {
         acl.setEntries(entries);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("Should throw when we have duplicate ACL entries", didThrow);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSAclTest("testDefaultConstructor"));
      suite.addTest(new PSAclTest("testGetSetMultiMemberAccess"));
      suite.addTest(new PSAclTest("testXml"));
      suite.addTest(new PSAclTest("testSetEntriesNull"));
      suite.addTest(new PSAclTest("testSetEntriesWrongType"));
      suite.addTest(new PSAclTest("testSetEntriesWithDuplicates"));
      return suite;
   }

   class DocumentContainer implements IPSDocument
   {
      public DocumentContainer()
      {
         m_doc = PSXmlDocumentBuilder.createXmlDocument();
      }

      /**
       * This method is called to create an XML document with the appropriate
       * format for the given object.
       *
       * @return    the newly created XML document
       */
      public Document toXml()
      {
         return m_doc;
      }

      /**
       * This method is called to populate an object from an XML
       * document.
       *
       * @exception PSUnknownDocTypeException   if the XML document does not
       *                                        represent a type supported
       *                                        by the class.
       */
      public void fromXml(Document sourceDoc)
         throws PSUnknownDocTypeException, PSUnknownNodeTypeException
      {
         m_doc = sourceDoc;
      }

      private Document m_doc;
   }
}
