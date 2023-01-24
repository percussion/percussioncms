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

import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

// Test case

public class PSUrlRequestTest extends TestCase
{
   public PSUrlRequestTest(String name)
   {
      super( name );
   }


   /**
    * Tests that the <code>clone()</code> method creates a separate-but-equal
    * instance, and that the copy is deep.
    * 
    * @throws Exception if the test fails.
    */ 
   public void testClone() throws Exception
   {
      PSCollection parameters = new PSCollection( PSParam.class );
      PSParam param1 = new PSParam( "p1", new PSTextLiteral( "param1" ) );
      PSParam param2 = new PSParam( "p2", new PSTextLiteral( "param2" ) );
      PSParam param3 = new PSParam( "p3", new PSTextLiteral( "param3" ) );
      parameters.add( param1 );
      parameters.add( param2 );
      parameters.add( param3 );

      PSUrlRequest foo = new PSUrlRequest( "foo",
         "http://foo.com/foo.xml?foo=foo", parameters );
      assertEquals( foo, foo );

      PSUrlRequest bar = (PSUrlRequest) foo.clone();
      assertEquals( foo, bar );

      param2.setName( "2p2" );  // mutate param
      
      // expect foo to have been modified, but not bar
      boolean found = false;
      for (Iterator iter = foo.getQueryParameters(); iter.hasNext();)
      {
         PSParam param = (PSParam) iter.next();
         if (param.getName().equals( "2p2" ))
         {
            found = true;
            break;
         }
      }
      assertTrue( "foo has change", found );
      found = false;
      for (Iterator iter = bar.getQueryParameters(); iter.hasNext();)
      {
         PSParam param = (PSParam) iter.next();
         if (param.getName().equals( "2p2" ))
         {
            found = true;
            break;
         }
      }
      assertTrue( "bar does not have change", !found );
   }


   public void testXmlParts() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc, "Test" );

      // create test object
      PSParam param1 = new PSParam( "p1", new PSTextLiteral( "param1" ) );
      PSParam param2 = new PSParam( "p2", new PSTextLiteral( "param2" ) );
      PSParam param3 = new PSParam( "p3", new PSTextLiteral( "param3" ) );
      PSCollection parameters = new PSCollection( param1.getClass() );
      parameters.add( param1 );
      parameters.add( param2 );
      parameters.add( param3 );
      PSUrlRequest testTo = new PSUrlRequest( "new", "http://38.227.11.8/Rhythmyx/1111.htm", parameters );
      Element elem = testTo.toXml( doc );
      root.appendChild( elem );

      // create a new object and populate it from our testTo element
      PSUrlRequest testFrom = new PSUrlRequest( elem, null, null );
      assertTrue( testTo.equals( testFrom ) );
   }


   public void testXmlUdf() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc, "Test" );

      // create test object
      PSExtensionRef exitRef = new PSExtensionRef( "handler", "context", "exit" );
      PSExtensionCall exitCall = new PSExtensionCall( exitRef, null );
      PSUrlRequest testTo = new PSUrlRequest( null, exitCall );
      Element elem = testTo.toXml( doc );
      root.appendChild( elem );

      // create a new object and populate it from our testTo element
      PSUrlRequest testFrom = new PSUrlRequest( elem, null, null );
      assertTrue( testTo.equals( testFrom ) );
   }


   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest( new PSUrlRequestTest( "testXmlParts" ) );
      suite.addTest( new PSUrlRequestTest( "testXmlUdf" ) );
      suite.addTest( new PSUrlRequestTest( "testClone" ) );

      return suite;
   }
}
