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

/**
 * Unit tests for the PSCgiVariable class.
 */
public class PSCgiVariableTest extends TestCase
{
   public PSCgiVariableTest(String name)
   {
      super(name);
   }

   public void testConstructor() throws Exception
   {

      {
         PSCgiVariable var = new PSCgiVariable("foobar");
         assertEquals("foobar", var.getName());
      }

      {
         PSCgiVariable var = new PSCgiVariable("foobar");
         PSCgiVariable otherVar = new PSCgiVariable("foobar");
         assertEquals(var, otherVar);
      }
   
      {
         boolean didThrow = false;
         try
         {
            PSCgiVariable var = new PSCgiVariable(null);
         }
         catch (IllegalArgumentException ex)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }

      {
         boolean didThrow = false;
         try
         {
            PSCgiVariable var = new PSCgiVariable("");
         }
         catch (IllegalArgumentException ex)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }
   }

   public void testXml() throws Exception
   {
      PSCgiVariable var = new PSCgiVariable("foobar");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = var.toXml(doc);
      PSCgiVariable otherVar = new PSCgiVariable(el, null, null);

      assertEquals(var, otherVar);
   }

   /**
    * Tests that the <code>clone()</code> method produces a new instance of
    * the class.  Assumes that <code>equals()</code> is implemented correctly.
    * 
    * @throws Exception if the test fails.
    */ 
   public void testClone() throws Exception
   {
      PSCgiVariable foo = new PSCgiVariable("foo");
      PSCgiVariable bar = (PSCgiVariable) foo.clone();
      
      assertEquals( foo, bar ); // I hope so!
      
      bar.setName( "bar" );
      assertTrue( !foo.equals( bar ) );
   }
   
   
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSCgiVariableTest("testConstructor"));
      suite.addTest(new PSCgiVariableTest("testXml"));
      suite.addTest(new PSCgiVariableTest("testClone"));
      return suite;
   }
}
