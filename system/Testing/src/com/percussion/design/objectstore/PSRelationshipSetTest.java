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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link PSRelationshipSet} class.
 */
public class PSRelationshipSetTest extends TestCase
{
   // see base class
   public PSRelationshipSetTest(String name)
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
      Exception exception = null;
      PSRelationshipSet rset = null;
      try
      {
         rset = new PSRelationshipSet(null, null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);
   }
   
   /**
    * Test public API contracts.
    * 
    * @throws Exception for all errors.
    */
   public void testPublicAPI() throws Exception
   {
      Exception exception = null;
      PSRelationshipSet rset = new PSRelationshipSet();
      try
      {
         rset.toXml(null);
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
      
      suite.addTest(new PSRelationshipSetTest("testConstructors"));
      suite.addTest(new PSRelationshipSetTest("testPublicAPI"));
      
      return suite;
   }
}
