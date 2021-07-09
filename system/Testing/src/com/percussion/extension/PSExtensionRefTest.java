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
package com.percussion.extension;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the PSExtensionRef class
 */
public class PSExtensionRefTest extends TestCase
{
   public PSExtensionRefTest(String testName)
   {
      super(testName);
   }

   public void testConstruct()
      throws Exception
   {
      for (int i = 0; i < numTests(); i++)
      {
         RefTest test = getTest(i);

         PSExtensionRef ref = new PSExtensionRef(test.handler,
            test.context, test.extension);

         assertEquals(test.full, ref.toString());
      }
   }

   public void testConstructFull()
      throws Exception
   {
      for (int i = 0; i < numTests(); i++)
      {
         RefTest test = getTest(i);

         PSExtensionRef ref = new PSExtensionRef(test.full);
         assertEquals(test.handler, ref.getHandlerName());
         
         // don't test context because the testcase's context may not
         // be canonicalized, whereas the constructed ref's context will
         // always be canonicalized
         assertEquals(test.extension, ref.getExtensionName());
      }

      boolean didThrow = false;
      try
      {
         PSExtensionRef ref = new PSExtensionRef("foo//bar");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
   }

   private static void initRefTests()
   {
      m_refTests = new ArrayList();

      m_refTests.add(new RefTest("testHandler", "testContext/", "testExtension",
         "testHandler/testContext/testExtension"));

      m_refTests.add(new RefTest("testHandler", "testContext", "testExtension",
         "testHandler/testContext/testExtension"));

      m_refTests.add(new RefTest("testHandler", "testContext/subContext", "testExtension",
         "testHandler/testContext/subContext/testExtension"));

      m_refTests.add(new RefTest("testHandler", "testContext/subContext/", "testExtension",
         "testHandler/testContext/subContext/testExtension"));

   }

   private static int numTests()
   {
      return m_refTests.size();
   }

   private static RefTest getTest(int i)
   {
      return (RefTest)(m_refTests.get(i));
   }

   private static ArrayList m_refTests;

   static class RefTest
   {
      public RefTest(String h, String c, String e, String f)
      {
         handler = h;
         context = c;
         extension = e;
         full = f;
      }

      public String handler;
      public String context;
      public String extension;
      public String full;
   }

   /** collect all tests into a TestSuite and return it */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSExtensionRefTest("testConstruct"));
      suite.addTest(new PSExtensionRefTest("testConstructFull"));
      return suite;
   }

   static
   {
      initRefTests();
   }
}
