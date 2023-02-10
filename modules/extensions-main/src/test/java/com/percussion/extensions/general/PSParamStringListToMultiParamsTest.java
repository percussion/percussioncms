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
package com.percussion.extensions.general;

import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;

import junit.framework.TestCase;

/**
 * Unit test for the <code>PSParamStringListToMultiParams</code> exit.
 */
public class PSParamStringListToMultiParamsTest extends TestCase
{
   /**
    * Tets all exit contracts and functionality.
    */
   public void testExit() throws Exception
   {
      PSParamStringListToMultiParams test = 
         new PSParamStringListToMultiParams();
      
      // setup test request
      String sourceValue = "value_1,value_2,value_3";
      String targetValue = "someValue";
      PSRequestContext request = new PSRequestContext(
         new TestRequest());
      request.setParameter("sourceName", sourceValue);
      request.setParameter("targetName", targetValue);
      
      // setup valid parameters
      String[] parameters = new String[4];
      parameters[0] = "sourceName";
      parameters[1] = ",";
      parameters[2] = "targetName";
      parameters[3] = null;

      // try null source
      parameters[0] = null;
      try
      {
         test.preProcessRequest(parameters, request);
         assertTrue("Expected exception", false);
      }
      catch (PSParameterMismatchException e)
      {
         // expected
      }

      // try empty source
      parameters[0] = " ";
      try
      {
         test.preProcessRequest(parameters, request);
         assertTrue("Expected exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
      
      parameters[0] = "sourceName";

      // try null delimiter
      parameters[1] = null;
      try
      {
         test.preProcessRequest(parameters, request);
         assertTrue("Expected exception", false);
      }
      catch (PSParameterMismatchException e)
      {
         // expected
      }
      
      parameters[1] = ",";

      // try null target
      parameters[2] = null;
      try
      {
         test.preProcessRequest(parameters, request);
         assertTrue("Expected exception", false);
      }
      catch (PSParameterMismatchException e)
      {
         // expected
      }

      // try empty target
      parameters[2] = " ";
      try
      {
         test.preProcessRequest(parameters, request);
         assertTrue("Expected exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
      
      parameters[2] = "targetName";

      // test appending source parameters
      test.preProcessRequest(parameters, request);
      assertTrue(request.getParameterList("targetName") != null);
      Object[] values = request.getParameterList("targetName");
      String[] expectedValues = 
      {
         "someValue",
         "value_1",
         "value_2",
         "value_3"
      };
      assertTrue(values.length == expectedValues.length);
      for (int i=0; i<values.length; i++)
         assertTrue(values[i].toString().equals(expectedValues[i]));

      // test appending source parameters
      parameters[3] = "yes";
      test.preProcessRequest(parameters, request);
      assertTrue(request.getParameterList("targetName") != null);
      values = request.getParameterList("targetName");
      String[] expectedValues2 = 
      {
         "value_1",
         "value_2",
         "value_3"
      };
      assertTrue(values.length == expectedValues2.length);
      for (int i=0; i<values.length; i++)
         assertTrue(values[i].toString().equals(expectedValues2[i]));
   }
   
   /**
    * Make empty constructor public for testing.
    */
   private class TestRequest extends PSRequest
   {
      public TestRequest()
      {
      }
   }
}

