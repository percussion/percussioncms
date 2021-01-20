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
