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
