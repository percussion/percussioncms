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
