/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

