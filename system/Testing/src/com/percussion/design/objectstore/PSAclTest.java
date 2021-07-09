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
