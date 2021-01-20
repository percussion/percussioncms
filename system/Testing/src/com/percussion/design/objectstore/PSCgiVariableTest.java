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
