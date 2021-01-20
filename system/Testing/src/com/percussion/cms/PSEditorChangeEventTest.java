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
package com.percussion.cms;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link PSEditorChangeEvent} class.
 */
public class PSEditorChangeEventTest extends TestCase
{
   /**
    * Constructor for PSEditorChangeEventTest.
    * 
    * @param name The name of the test.
    */
   public PSEditorChangeEventTest(String name)
   {
      super(name);
   }

   /**
    * Tests the equals and hashCode methods of this object.
    * 
    * @throws Exception if there are any errors.
    */   
   public void testEquals() throws Exception 
   {
      PSEditorChangeEvent evt1 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 3, 4, 5);
      
      PSEditorChangeEvent evt2 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 3, 4, 5);
      
      assertEquals(evt1, evt2);
      assertEquals(evt1.hashCode(), evt2.hashCode());
      
      evt1.setPriority(1);
      assertTrue(!evt1.equals(evt2));      
      evt2.setPriority(1);
      assertEquals(evt1, evt2);      
      assertEquals(evt1.hashCode(), evt2.hashCode());
      
      Collection binFields = new ArrayList();
      evt1.setBinaryFields(binFields);
      assertEquals(evt1, evt2);      
      
      binFields.add("foo");
      binFields.add("bar");
      evt1.setBinaryFields(binFields);
      assertTrue(!evt1.equals(evt2)); 
      evt2.setBinaryFields(binFields);
      assertEquals(evt1, evt2);      
      assertEquals(evt1.hashCode(), evt2.hashCode());
      
      evt1 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 3, 4, 5);
      evt2 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 2, 2, 3, 4, 5);
      assertTrue(!evt1.equals(evt2));
      evt2 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 1, 3, 4, 5);
      assertTrue(!evt1.equals(evt2));
      evt2 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 1, 4, 5);
      assertTrue(!evt1.equals(evt2));
      evt2 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 3, 1, 5);
      assertTrue(!evt1.equals(evt2));
      evt2 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 3, 4, 1);
      assertTrue(!evt1.equals(evt2));

   }
     
   
   /**
    * Tests the xml serialization of this object.  {@link #testEquals()} should
    * be run before this test as this test assumes the equals method is 
    * working
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      PSEditorChangeEvent evt1 = new PSEditorChangeEvent(
         PSEditorChangeEvent.ACTION_INSERT, 1, 2, 3, 4, 5);
      evt1.setPriority(1);     
      
      assertEquals(evt1, new PSEditorChangeEvent(evt1.toXml(doc)));
      
      Collection binFields = new ArrayList();
      binFields.add("foo");
      binFields.add("bar");
      evt1.setBinaryFields(binFields);
      assertEquals(evt1, new PSEditorChangeEvent(evt1.toXml(doc)));            
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSEditorChangeEventTest("testEquals"));
      suite.addTest(new PSEditorChangeEventTest("testXml"));
      return suite;
   }
}
