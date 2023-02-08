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

package com.percussion.server;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

public class PSRequestTest extends TestCase
{
   /**
    * Constructs an instance of this class to run the test implemented by the
    * named method.
    * 
    * @param methodName name of the method that implements a test
    */
   public PSRequestTest(String name)
   {
      super( name );
   }


   /**
    * Constructs <code>PSRequest</code> objects for test classes outside the 
    * server package (since the <code>PSRequest</code> constructors have 
    * package access).
    * 
    * @return new <code>PSRequest</code> using supplied parameters
    */
   public static PSRequest makeRequest(String reqFileURL, String reqHookURL,
                                       Map params, Map<String, String> cgiVars, Map cookies,
                                       Document inData, OutputStream out)
   {
      MockHttpServletRequest req = new MockHttpServletRequest("GET", reqFileURL);
      if (cgiVars != null)
      {
         for (String cgiName : cgiVars.keySet())
         {
            req.addParameter(cgiName, (String) cgiVars.get(cgiName));
         }   
      }
      MockHttpServletResponse res = new MockHttpServletResponse();

      // TODO - Need to handle other data. Probably need to write more
      // complete Mock objects, or look for a better version of these
      
      return new PSRequest( req, res, null, null);
   }


   /**
    * Tests the cloneRequest method to make sure the request parameter map is
    * cloned.
    */
   public void testClone() throws Exception
   {
      // build parameter map
      HashMap params = new HashMap();
      params.put( "alpha", "beta" );
      params.put( "foo", "bar" );

      PSRequest request = getEmptyRequest();
      request.setParameters( params );
      assertEquals( "bar", request.getParameter( "foo" ) );
      assertEquals( "beta", request.getParameter( "alpha" ) );

      PSRequest clone = request.cloneRequest();
      
      // make sure we start from equivalence
      assertEquals( clone.getParameters(), request.getParameters() );
      // PSRequest does not override equals so can't assertEquals(clone, request);
      
      // test modify
      clone.setParameter( "foo", "foo" );
      assertEquals( "foo", clone.getParameter( "foo" ) );
      assertEquals( "bar", request.getParameter( "foo" ) );
      assertTrue( !clone.getParameters().equals( request.getParameters() ) );
      
      // test add
      clone.setParameter( "bar", "bar" );
      assertEquals( "bar", clone.getParameter( "bar" ) );
      assertNull( request.getParameter( "bar" ) );
      assertTrue( !clone.getParameters().equals( request.getParameters() ) );

   }


   /**
    * @return
    */
   private PSRequest getEmptyRequest()
   {

      MockHttpServletRequest req = new MockHttpServletRequest();
      MockHttpServletResponse res = new MockHttpServletResponse();

      return new PSRequest(req, res, null, null);
   }


   /**
    * Tests the putAllParameters method to make sure it add parameters and 
    * replaces existing values.
    */
   public void testPutAllParameters() throws Exception
   {
      // build parameter map
      HashMap params = new HashMap();
      params.put( "alpha", "beta" );
      params.put( "foo", "bar" );

      PSRequest request = getEmptyRequest();
      request.setParameters( params );
      assertEquals( "bar", request.getParameter( "foo" ) );
      assertEquals( "beta", request.getParameter( "alpha" ) );

      HashMap newParams = new HashMap();
      newParams.put( "charlie", "delta" );
      newParams.put( "foo", "elephant" );
      request.putAllParameters( newParams );
      assertEquals( "elephant", request.getParameter( "foo" ) );
      assertEquals( "beta", request.getParameter( "alpha" ) );
      assertEquals( "delta", request.getParameter( "charlie" ) );
   }


   /**
    * Collects all the tests implemented by this class into a single suite.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSRequestTest( "testClone" ) );
      suite.addTest( new PSRequestTest( "testPutAllParameters" ) );
      return suite;
   }

}
