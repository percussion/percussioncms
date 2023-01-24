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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for the {@link PSRelationshipProperty} class.
 */
public class PSRelationshipPropertyTest extends TestCase
{
   // see base class
   public PSRelationshipPropertyTest(String name)
   {
      super(name);
   }
   
   /**
    * The all public constructor contracts.
    * 
    * @throws Exception for any error.
    */
   public void testConstructors() throws Exception
   {
      PSRelationshipProperty prop = null;
      Exception exception = null;
      try
      {
         prop = new PSRelationshipProperty("name");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);
         
      prop = null;
      exception = null;
      try
      {
         prop = new PSRelationshipProperty(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      prop = null;
      exception = null;
      try
      {
         prop = new PSRelationshipProperty(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element testXml = new PSRelationshipProperty("name").toXml(doc);

      prop = null;
      exception = null;
      try
      {
         prop = new PSRelationshipProperty(testXml, null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);

      prop = null;
      exception = null;
      try
      {
         prop = new PSRelationshipProperty(null, null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
   }
   
   /**
    * Test public API contracts.
    * 
    * @throws Exception for all errors.
    */
   public void testPublicAPI() throws Exception
   {
      PSRelationshipProperty prop = new PSRelationshipProperty("name");
      
      Exception exception = null;
      try
      {
         prop.setValueText(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      exception = null;
      try
      {
         prop.setValueText(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      exception = null;
      try
      {
         prop.validate(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof NullPointerException);
   }
   
   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSRelationshipPropertyTest("testConstructors"));
      suite.addTest(new PSRelationshipPropertyTest("testPublicAPI"));
      
      return suite;
   }
}
