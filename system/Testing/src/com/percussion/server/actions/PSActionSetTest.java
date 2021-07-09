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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.server.actions;

import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.*;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionManager;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestTest;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Tests the functionality of <code>PSActionSet</code>.
 */
public class PSActionSetTest extends TestCase
{
   /**
    * Tests that the getters return valid data
    */
   public void testGetters() throws Exception
   {
      PSActionSet actionSet = newActionSet( PSActionSet.XML_NODE_NAME,
         "orange", "blue/green.htm", 3 );

      // confirm object was created as we expect
      assertEquals( "orange", actionSet.getName() );
      assertEquals( 3, actionSet.getNumberofActions() );
      int i=0;
      for (Iterator iter = actionSet.getActions(); iter.hasNext(); i++)
      {
         PSAction action = (PSAction) iter.next();
         if (i % 2 == 1)
            assertTrue( "my odd actions should ignore errors",
               action.ignoreError() );
         else
            assertTrue( "my even actions should not ignore errors",
               !action.ignoreError() );
      }

      // prepare runtime environment for url extraction
      PSRequest request = PSRequestTest.makeRequest( null, null, null, null,
         null, null, null );
      PSExecutionData data = null;

      // make sure we get an error when init hasn't been called
      boolean didThrow = false;
      try
      {
         data = new PSExecutionData( null, null, request );
         actionSet.getRedirectUrl( data );
      } catch (IllegalStateException e)
      {
         didThrow = true;
      } finally
      {
         if (data != null) data.release();
      }
      assertTrue( "did not throw illegal state when init method not called",
         didThrow );

      // need to call init method on action set before we can get redirect urls
      IPSExtensionManager extMgr = new PSExtensionManager();
      actionSet.init( extMgr );

      // make sure we have reasonable behavior when the replacement value is
      // missing (param is not added)
      data = new PSExecutionData( null, null, request );
      assertTrue( compareUrls("blue/green.htm?p3=param3&p1=param1",
         actionSet.getRedirectUrl( data )));
      data.release();

      // test normal behavior
      request.setParameter("foo", "bar");
      data = new PSExecutionData( null, null, request );
      assertTrue(compareUrls("blue/green.htm?p3=param3&p2=bar&p1=param1", 
            actionSet.getRedirectUrl( data )));
      data.release();

      // make sure when we extract using a different context, we get different
      // values
      request.setParameter("foo", "oof");
      data = new PSExecutionData( null, null, request );
      assertTrue(compareUrls("blue/green.htm?p3=param3&p2=oof&p1=param1",
         actionSet.getRedirectUrl( data )));

      // test that the actions can return their URLs
      i = 0;
      for (Iterator iter = actionSet.getActions(); iter.hasNext();)
      {
         PSAction action = (PSAction) iter.next();
         assertEquals( "action" + i++, action.getName() );
         assertTrue(compareUrls("ceFile/edit.htm?p3=param3&p2=oof&p1=param1",
            action.getUrl( "ceFile/edit.htm", data )));
      }
      data.release();
   }

   /**
    * Compares 2 urls of the form 
    * <p>
    *    path?p1=v1&p2=v2
    * <p>
    * by comparing the 2 paths (case sensitive) and verifying that for every 
    * param (p1, p2...) in <code>url1</code>, there is the same param in 
    * <code>url2</code> with a matching value (case sensitive).
    * 
    * @param url1 Assumed not <code>null</code>.
    * @param url2 Assumed not <code>null</code>.
    * @return <code>true</code> if the URLs match as noted above, 
    * <code>false</code> otherwise.
    */
   private boolean compareUrls(String url1, String url2)
   {
      int pos1 = url1.indexOf("?");
      int pos2 = url2.indexOf("?");
      if (pos1 != pos2)
         return false;
      if (pos1 < 0 && !url1.equals(url2))
         return false;
      String base1 = url1.substring(0, pos1);
      String query1 = url1.substring(pos1+1);
      String base2 = url2.substring(0, pos2);
      String query2 = url2.substring(pos2+1);
      if (!base1.equals(base2))
         return false;

      Map<String, String> map1 = new HashMap<String, String>();
      StringTokenizer toker = new StringTokenizer(query1, "&");
      while (toker.hasMoreElements())
      {
         String pair = toker.nextToken();
         int pos = pair.indexOf('=');
         if (pos >= 0)
            map1.put(pair.substring(0, pos), pair.substring(pos+1));
      }
      Map<String, String> map2 = new HashMap<String, String>();
      toker = new StringTokenizer(query2, "&");
      while (toker.hasMoreElements())
      {
         String pair = toker.nextToken();
         int pos = pair.indexOf('=');
         if (pos >= 0)
            map2.put(pair.substring(0, pos), pair.substring(pos+1));
      }
      if (!map1.equals(map2))
         return false;
      return true;
   }
   
   /**
    * Tests the constructor with good and bad XML
    */
   public void testCtor() throws Exception
   {
      // tests that wrong element name is error
      expectToFailCtor( "BadRoot", null, null, 0 );

      // tests that missing @name is error
      expectToFailCtor( PSActionSet.XML_NODE_NAME, null, null, 0 );

      // tests that empty @name is error
      expectToFailCtor( PSActionSet.XML_NODE_NAME, "", null, 0 );

      // tests that no children is error
      expectToFailCtor( PSActionSet.XML_NODE_NAME, "test", null, 0 );

      // tests that no action is error
      expectToFailCtor( PSActionSet.XML_NODE_NAME, "test", "test.htm", 0 );

      // tests that no stylesheet is not error
      newActionSet( PSActionSet.XML_NODE_NAME, "test", "a.htm", 1 );

      // tests that multiple actions is not error
      newActionSet( PSActionSet.XML_NODE_NAME, "test", "a.htm", 3 );

   }

   /**
    * Tests that an exception is thrown when a set contains multiple actions
    * with the same name.
    */
   public void testDuplicateActionNames() throws Exception
   {
      Element el = null;
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root =
         PSXmlDocumentBuilder.createRoot( doc, PSActionSet.XML_NODE_NAME );
      root.setAttribute( "name", "testNoDupActionNames" );
      el = newActionElement( "action0", "test0.htm" );
      PSXmlDocumentBuilder.copyTree(doc, root, el, true);
      el = newActionElement( "action1", "test1.htm" );
      PSXmlDocumentBuilder.copyTree(doc, root, el, true);
      el = newActionElement( "action1", "test1.htm" );
      PSXmlDocumentBuilder.copyTree(doc, root, el, true);
      el = newUrl( "bravo.htm" ).toXml( doc );
      PSXmlDocumentBuilder.copyTree(doc, root, el, true);

      boolean didThrow = false;
      try
      {
         new PSActionSet( doc.getDocumentElement() );
      } catch (PSUnknownNodeTypeException e)
      {
         assertEquals( IPSServerErrors.ACTION_DUPLICATE_NAME,
            e.getErrorCode() );
         didThrow = true;
      }
      assertTrue( didThrow );
   }


   /**
    * Tests that actions' exits can be loaded from XML.
    */
   public void testActionExits() throws Exception
   {
      InputStream input =
         new FileInputStream( RESOURCE_PATH + "actionSetWithExits.xml" );
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
         new InputStreamReader( input ), false );
      input.close();

      PSActionSet actionSet = new PSActionSet( doc.getDocumentElement() );
      for (Iterator iter = actionSet.getActions(); iter.hasNext();)
      {
         PSAction action = (PSAction) iter.next();
         if (action.getName().equals( "insert" ))
            assertNotNull( "this action should contain an extension set",
               action.getExtensions() );
         else
            assertNull( "this action should not contain an extension set",
               action.getExtensions() );
      }

      /*
         It is too difficult to try to initialize an extension manager from
         the unit test.  Therefore, testing init() with exits will need to be
         part of the system test.

      IPSExtensionManager extMgr = new PSExtensionManager();
      Properties initProps = new Properties();
      initProps.setProperty(IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         "Extensions.xml" );
      extMgr.init( new File( RESOURCE_PATH ), initProps );
      actionSet.init( extMgr );
      */

   }

   /**
    * Construct a <code>PSActionSet</code> using an XML document built from
    * the supplied parameters, and assert that the constructor throws a
    * <code>PSUnknownNodeTypeException</code>.
    */
   private void expectToFailCtor(String rootName, String name, String url,
                                 int numActions) throws Exception
   {
      boolean didThrow = false;
      try
      {
         newActionSet( rootName, name, url, numActions );
      } catch (PSUnknownNodeTypeException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
   }


   /**
    * Construct a <code>PSActionSet</code> using an XML document built from
    * the supplied parameters.  Can be used to create illegal XML which will
    * cause a PSUnknownNodeTypeException.  Package access to this method so
    * that other test classes can utilize it.
    *
    * @param rootName determines the name of the root element
    * @param name determines the value of the name attribute; use <code>null
    * </code> to omit the attribute
    * @param url determines the href of the redirect PSURLRequest; use <code>
    * null</code> to omit the element
    * @param numActions determines the number of action elements
    *
    * @return a new <code>PSActionSet</code>
    */
   static PSActionSet newActionSet(String rootName, String name, String url,
                                   int numActions)
      throws PSUnknownNodeTypeException, PSException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc, rootName );
      if (name != null)
         root.setAttribute( "name", name );

      for (int i = 0; i < numActions; i++)
      {
         Element action = newActionElement( "action"+i, "test"+i+".htm" );

         // seed the actions with differing ignoreError attribute values
         // (first one gets no attr, then odds get yes and evens no)
         if ( i > 0 && i % 2 == 1)
            action.setAttribute( PSAction.IGNORE_ERROR_XATTR, "yes" );
         else if ( i > 0 && i % 2 == 0)
         action.setAttribute( PSAction.IGNORE_ERROR_XATTR, "no" );

         PSXmlDocumentBuilder.copyTree(doc, root, action, true);
      }

      if (url != null)
         root.appendChild( newUrl( url ).toXml( doc ) );

      PSActionSet actionSet = new PSActionSet( doc.getDocumentElement() );
      return actionSet;
   }

   /**
    * @return a new <code>PSUrlRequest</code> with three parameters:  two
    * text literals and one single html parameter ("foo")
    */
   private static PSUrlRequest newUrl(String url)
   {
      PSParam param1 = new PSParam( "p1", new PSTextLiteral( "param1" ) );
      PSParam param2 = new PSParam( "p2", new PSSingleHtmlParameter( "foo" ) );
      PSParam param3 = new PSParam( "p3", new PSTextLiteral( "param3" ) );
      PSCollection parameters = new PSCollection( PSParam.class );
      parameters.add( param1 );
      parameters.add( param2 );
      parameters.add( param3 );
      return new PSUrlRequest( "test", url, parameters );
   }

   /**
    * @return an XML element that can be used to construct a PSAction of
    * the provided name and url.
    */
   private static Element newActionElement(String name, String url)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc,
         PSAction.XML_NODE_NAME );
      root.setAttribute( "name", name );

      PSParam param;
      param = new PSParam( "p1", new PSTextLiteral( "param1" ) );
      root.appendChild( param.toXml( doc ) );
      param = new PSParam( "p2", new PSSingleHtmlParameter( "foo" ) );
      root.appendChild( param.toXml( doc ) );
      param = new PSParam( "p3", new PSTextLiteral( "param3" ) );
      root.appendChild( param.toXml( doc ) );

      return doc.getDocumentElement();
   }

   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_PATH =
      "UnitTestResources/com/percussion/server/actions/";

}
