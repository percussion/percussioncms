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

package com.percussion.server.actions;

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Tests the basic functionality of the <code>PSActionSetResult</code> class.
 */
public class PSActionSetResultTest extends TestCase
{
   /**
    * Constructs an instance of this class to run the test implemented by the
    * named method.
    * 
    * @param methodName name of the method that implements a test
    */
   public PSActionSetResultTest(String name)
   {
      super( name );
   }
   
   
   /**
    * Collects all the tests implemented by this class into a single suite.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSActionSetResultTest( "testIt" ) );
      return suite;
   }


   /**
    * Performs basic tests of the results.  Make sure ctor rejects invalid
    * parameters.  Make sure ctor can properly init state from provided
    * PSActionSet.  Make sure the set methods perform correctly.
    */
   public void testIt() throws Exception
   {
      PSActionSetResult resultSet;

      // make sure ctor won't accept null
      boolean didThrow = false;
      try
      {
         resultSet = new PSActionSetResult( null, "hiya" );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );

      didThrow = false;
      try
      {
         PSActionSet actionSet = PSActionSetTest.newActionSet(
            PSActionSet.XML_NODE_NAME, "test", "a.htm", 1 );
         resultSet = new PSActionSetResult( actionSet, null );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );

      int numActions = 3;
      PSActionSet actionSet = PSActionSetTest.newActionSet( 
         PSActionSet.XML_NODE_NAME, "test", "a.htm", numActions );    
      resultSet = new PSActionSetResult( actionSet, "ceurl.htm" );
      
      // make sure action result set is seeded with skips for each action
      PSActionSetResult.ActionResult result;
      for (int i=0; i < numActions; i++)
      {
         String name = "action" + i;
         result = resultSet.getResult( name );

         assertEquals( PSActionSetResult.SKIPPED_STATUS, result.getStatus() );
         assertEquals( null, result.getResult() );
      }
      
      // make sure unknown action names throw exception
      didThrow = false;
      try
      {
         result = resultSet.getResult( "thisisnotaactionameintheset" );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
      
      // make sure setFailed sets one and only one result
      Exception e = new Exception();
      resultSet.setFailed( "action1", e );
      result = resultSet.getResult( "action2" );
      assertEquals( PSActionSetResult.SKIPPED_STATUS, result.getStatus() );
      assertEquals( null, result.getResult() );
      result = resultSet.getResult( "action1" );
      assertEquals( PSActionSetResult.FAILED_STATUS, result.getStatus() );
      assertEquals( e, result.getResult() );
      result = resultSet.getResult( "action0" );
      assertEquals( PSActionSetResult.SKIPPED_STATUS, result.getStatus() );
      assertEquals( null, result.getResult() );
      
      // make sure setSuccess sets one and only one result
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      resultSet.setSuccess( "action2", doc );
      result = resultSet.getResult( "action2" );
      assertEquals( PSActionSetResult.SUCCESS_STATUS, result.getStatus() );
      assertEquals( doc, result.getResult() );
      result = resultSet.getResult( "action1" );
      assertEquals( PSActionSetResult.FAILED_STATUS, result.getStatus() );
      assertEquals( e, result.getResult() );
      result = resultSet.getResult( "action0" );
      assertEquals( PSActionSetResult.SKIPPED_STATUS, result.getStatus() );
      assertEquals( null, result.getResult() );
   }
   
}
