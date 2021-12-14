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

package com.percussion.xml;

import com.icl.saxon.expr.XPathException;
import com.percussion.security.xml.PSSecureXMLUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests that {@link PSXPathEvaluator} operates correctly.
 */
public class PSXPathEvaluatorTest
{

   public PSXPathEvaluatorTest()
   {
      PSSecureXMLUtils.setupJAXPDefaults();
   }



   /**
    * Builds an evaluator from an input stream. Obtains an iterator over nodes
    * by evaluating an XPath expresssion. Then constructs
    * <code>PSXPathEvaluator</code> object for each node and evaluate XPath
    * expressions against each node.
    */
   @Test
   @Ignore("TODO: Update for XML Catalog resolver")
   public void testNode() throws Exception
   {
      PSXPathEvaluator xp = new PSXPathEvaluator(
         new FileInputStream(RESOURCE_PATH + "simple.xml"));
      String xpath =
         "//Control[@paramName='authornames']//ActionLinkList/ActionLink[DisplayLabel='Edit']/Param";
      Iterator it = xp.enumerate(xpath, false);

      /*
         This should return the following nodes:

         <Param name="sys_command">edit</Param>
         <Param name="sys_childrowid">3</Param>
         <Param name="sys_pageid">2</Param>
         <Param name="sys_view">sys_All</Param>
      */

      List attrValues = new ArrayList();
      attrValues.add("sys_command");
      attrValues.add("sys_childrowid");
      attrValues.add("sys_pageid");
      attrValues.add("sys_view");

      List textValues = new ArrayList();
      textValues.add("edit");
      textValues.add("3");
      textValues.add("2");
      textValues.add("sys_All");

      while (it.hasNext())
      {
         Node node = (Node)it.next();
         PSXPathEvaluator xpNode = new PSXPathEvaluator(node);
         String attrValue = xpNode.evaluate("./@name");
         String textValue = xpNode.evaluate(".");
         assertTrue(attrValues.contains(attrValue));
         assertTrue(textValues.contains(textValue));
      }
   }

   /**
    * Builds an evaluator from an input stream and tests that XPaths are
    * evaluated correctly.  Also makes sure that an exception is thrown when
    * input is invalid.
    */
   @Test
   @Ignore("TODO: Update for XML Catalog resolver")
   public void testStream() throws Exception
   {
      PSXPathEvaluator xp = new PSXPathEvaluator(
         new FileInputStream( RESOURCE_PATH + "simple.xml" ) );
      testXPaths( xp );

      // null stream should throw exception
      boolean didThrow = false;
      try
      {
         xp = new PSXPathEvaluator( (InputStream) null );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( "did not reject null stream", didThrow);
   }


   /**
    * Builds an evaluator from a document and tests that XPaths are
    * evaluated correctly.  Also makes sure that an exception is thrown when
    * input is invalid.
    */
   @Test
   @Ignore("TODO: Update for XML Catalog resolver")
   public void testDOM() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
         new FileInputStream( RESOURCE_PATH + "simple.xml" ), false);
      PSXPathEvaluator xp = new PSXPathEvaluator( doc );
      testXPaths( xp );

      // null document should throw exception
      boolean didThrow = false;
      try
      {
         xp = new PSXPathEvaluator( (Document) null );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( "did not reject null document", didThrow);

      // try with empty document
      xp = new PSXPathEvaluator( PSXmlDocumentBuilder.createXmlDocument() );
      assertEquals( "", xp.evaluate( "/*/@commandName" ) );
   }


   /**
    * Applies several valid and invalid XPath expressions to make sure the
    * appropriate results are returned.
    */
   private void testXPaths(PSXPathEvaluator xp) throws XPathException {
      assertEquals( "edit", xp.evaluate( "/*/@commandName" ) );
      assertEquals( "3", xp.evaluate("//Control[@paramName='authornames']//ActionLinkList/ActionLink[DisplayLabel='Edit']/Param[@name='sys_childrowid']") );
      assertEquals( "", xp.evaluate("/*/horsey") );

      // try some bad ones to see if exceptions get thrown
      boolean didThrow;

      didThrow = false;
      try
      {
         assertEquals( "", xp.evaluate(null) );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( "did not reject null xpath", didThrow);

      didThrow = false;
      try
      {
         assertEquals( "", xp.evaluate("") );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( "did not reject empty xpath", didThrow);

      didThrow = false;
      try
      {
         assertEquals( "", xp.evaluate("/*/horsey[") );
      } catch (XPathException e)
      {
         didThrow = true;
      }
      assertTrue( "did not reject syntax error in xpath", didThrow);
   }

   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_PATH =
      "UnitTestResources/com/percussion/xml/";

}
