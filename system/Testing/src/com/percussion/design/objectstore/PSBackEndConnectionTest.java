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

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the PSBackEndConnection class.
 */
public class PSBackEndConnectionTest extends TestCase
{
   public PSBackEndConnectionTest(String name)
   {
      super(name);
   }

   public void testConstructor() throws Exception
   {
      // create two valid, identical connections and test for equality
      {
         PSBackEndConnection conn = new PSBackEndConnection(
            "driverName", "className", "serverName");
         PSBackEndConnection otherConn = new PSBackEndConnection(
            "driverName", "className", "serverName");
         assertEquals(conn, otherConn);
      }

      // invalid - null driver name
      {
         boolean didThrow = false;
         try
         {
            PSBackEndConnection conn = new PSBackEndConnection(
               null, "className", "serverName");
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }

      // invalid - empty driver name
      {
         boolean didThrow = false;
         try
         {
            PSBackEndConnection conn = new PSBackEndConnection(
               "", "className", "serverName");
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }

      // invalid - null class name
      {
         boolean didThrow = false;
         try
         {
            PSBackEndConnection conn = new PSBackEndConnection(
               "driverName", null, "serverName");
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }

      // invalid - empty class name
      {
         boolean didThrow = false;
         try
         {
            PSBackEndConnection conn = new PSBackEndConnection(
               "driverName", "", "serverName");
         }
         catch (IllegalArgumentException e)
         {
            didThrow = true;
         }
         assertTrue(didThrow);
      }

      // this is valid
      {
         PSBackEndConnection conn = new PSBackEndConnection(
            "driverName", "className", null);
      }

      // this is valid
      {
         PSBackEndConnection conn = new PSBackEndConnection(
            "driverName", "className", "");
      }
   }

   public void testXml() throws Exception
   {
      PSBackEndConnection conn = new PSBackEndConnection(
         "driverName", "className", "server");

      PSBackEndConnection otherConn = new PSBackEndConnection();

      conn.setConnectionMax(27);
      conn.setConnectionMin(17);
      conn.setIdleTimeout(217);

      assertTrue(!conn.equals(otherConn));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = conn.toXml(doc);

      otherConn.fromXml(el, null, null);

      assertEquals(conn, otherConn);
      assertEquals(27, otherConn.getConnectionMax());
      assertEquals(17, otherConn.getConnectionMin());
      assertEquals(217, otherConn.getIdleTimeout());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSBackEndConnectionTest("testConstructor"));
      suite.addTest(new PSBackEndConnectionTest("testXml"));
      return suite;
   }
}
