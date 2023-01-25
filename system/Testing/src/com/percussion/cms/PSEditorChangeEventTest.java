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
