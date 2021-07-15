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
package com.percussion.cms.objectstore;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

/**
 * 
 */
public class PSComponentSummaryTest extends TestCase
{
   /**
    * Test all public constuctors.
    * 
    * @throws Exception for any error.
    */
   public void testConstructors() throws Exception
   {
      int cid = 300;
      int rid = 1;
      int tiprid = 1;
      int editrid = 1;
      int objecttype = PSComponentSummary.TYPE_ITEM;
      String name = "item_1";
      int contenttypeid = 301;
      int permissions = 3;
      
      // test valid constuctor parameters
      Exception exception = null;
      try
      {
         new PSComponentSummary(cid, rid, tiprid, editrid, objecttype, name, 
            contenttypeid, permissions);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);

      // test invalid contentid
      exception = null;
      try
      {
         new PSComponentSummary(-1, rid, tiprid, editrid, objecttype, name, 
            contenttypeid, permissions);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test invalid name
      exception = null;
      try
      {
         new PSComponentSummary(cid, rid, tiprid, editrid, objecttype, null, 
            contenttypeid, permissions);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test empty name
      exception = null;
      try
      {
         new PSComponentSummary(cid, rid, tiprid, editrid, objecttype, "", 
            contenttypeid, permissions);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid folder
      exception = null;
      try
      {
         new PSComponentSummary(cid, rid, tiprid, 
            editrid, PSComponentSummary.TYPE_FOLDER, name, contenttypeid, 
            permissions);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);

      // test invalid folder permissions
      exception = null;
      try
      {
         new PSComponentSummary(cid, rid, tiprid, 
            editrid, PSComponentSummary.TYPE_FOLDER, name, contenttypeid, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSComponentSummary item_1 = new PSComponentSummary(cid, rid, tiprid, 
         editrid, objecttype, name, contenttypeid, permissions);
      Element item_1_xml = item_1.toXml(doc);
      
      // test xml constructor for valid element
      exception = null;
      try
      {
         new PSComponentSummary(item_1_xml);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);
      
      // test xml constructor for invalid element
      exception = null;
      try
      {
         new PSComponentSummary(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // test xml constructor for unknown node type element
      exception = null;
      try
      {
         new PSComponentSummary((Element) item_1_xml.getFirstChild());
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);
   }
   
   /**
    * Test all public methods contracts.
    * 
    * @throws Exception for any error.
    */
   public void testPublicAPI() throws Exception
   {
      int cid = 300;
      int rid = 1;
      int tiprid = 1;
      int editrid = 1;
      int objecttype = PSComponentSummary.TYPE_ITEM;
      String name = "item_1";
      int contenttypeid = 301;
      int permissions = 3;
      PSComponentSummary item_1 = new PSComponentSummary(cid, rid, tiprid, 
         editrid, objecttype, name, contenttypeid, permissions);
      
      // test clone method
      PSComponentSummary item_1_clone_1 = (PSComponentSummary) item_1.clone();
      assertTrue(item_1.equals(item_1_clone_1));
      
      // test toXml method
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSComponentSummary item_1_clone_2 = 
         new PSComponentSummary(item_1.toXml(doc));
      assertEqualsWithHash(item_1, item_1_clone_2);
      
      // test equals method
      item_1_clone_2.setName("foo");
      assertTrue(!item_1.equals(item_1_clone_2));
      
      // test setName method with invalid name
      try
      {
         item_1_clone_2.setName(null);
         fail();
      }
      catch (IllegalArgumentException success) {}
      
      // test setName method with empty name
      try
      {
         item_1_clone_2.setName(" ");
         fail();
      }
      catch (IllegalArgumentException success) {}
      
      // test setName method with valid name
      item_1_clone_2.setName("item_1");
   }
   
   /**
    * Tests <code>PSComponentSummary.getEditLocator()</code>.
    *
    */
   public void testEditLocator()
   {
      int cid = 300;
      int contenttypeid = 301;
      int permissions = 3;
      int currentRevision;
      int tipRevision;
      int editRevision;
      PSComponentSummary summary;
      PSLocator loc;

      // make sure edit revision is preferred to current revision
      currentRevision = 1;
      tipRevision = 2;
      editRevision = 2;
      summary = new PSComponentSummary(cid, currentRevision, tipRevision,
            editRevision, PSComponentSummary.TYPE_ITEM, "edit_1",
            contenttypeid, permissions);
      loc = summary.getEditLocator();
      assertNotNull(loc);
      assertEquals(cid, loc.getId());
      assertEquals(editRevision, loc.getRevision());

      // make sure current revision (not tip) is used when edit revision not set
      currentRevision = 2;
      tipRevision = 3;
      editRevision = -1;
      summary = new PSComponentSummary(cid, currentRevision, tipRevision,
            editRevision, PSComponentSummary.TYPE_ITEM, "edit_1",
            contenttypeid, permissions);
      loc = summary.getHeadLocator();
      assertNotNull(loc);
      assertEquals(cid, loc.getId());
      assertEquals(currentRevision, loc.getRevision());
   }
   
   /**
    * Tests <code>PSComponentSummary.getEditLocator()</code>.
    *
    */
   public void testTimeZone()
   {
      int cid = 300;
      int contenttypeid = 301;
      int permissions = 3;
      int currentRevision;
      int tipRevision;
      int editRevision;
      PSComponentSummary summary;

      currentRevision = 1;
      tipRevision = 1;
      editRevision = 1;
      summary = new PSComponentSummary(cid, currentRevision, tipRevision,
            editRevision, PSComponentSummary.TYPE_ITEM, "edit_1",
            contenttypeid, permissions);
      
      summary.setContentPostDateTz("EST");
      
      String timezone = summary.getContentPostDateTz();
      
      assertNotNull(timezone);

   }
}
