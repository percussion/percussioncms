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
