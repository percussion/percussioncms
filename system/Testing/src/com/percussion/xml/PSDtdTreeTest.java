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
package com.percussion.xml;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the PSDtdTree class.
 */
public class PSDtdTreeTest extends TestCase
{
   public PSDtdTreeTest(String name)
   {
      super(name);
   }

   /**
    * Test the PSDtdTree.canonicalToArry method
    */
   public void testCanonicalToArray()
   {
      // make sure it throws an IllegalArgumentException on a null String
      boolean didThrow = false;
      try
      {
         PSDtdTree.canonicalToArray(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue("throws on null String?", didThrow);

      CanonicalTest[] canon = new CanonicalTest[20];
      int numTests = 0;
      
      canon[numTests++] = new CanonicalTest("/", new String[0]);

      canon[numTests++] = new CanonicalTest("", new String[0]);

      canon[numTests++] = new CanonicalTest("/ /   /   / / / //////", new String[0]);

      canon[numTests++] = new CanonicalTest("/foo", new String[] { "foo" });

      canon[numTests++] = new CanonicalTest("/foo/bar",
         new String[] { "foo", "bar" });

      canon[numTests++] = new CanonicalTest("/foo/bar/baz",
         new String[] { "foo", "bar", "baz" });

      canon[numTests++] = new CanonicalTest("foo/bar/baz",
         new String[] { "foo", "bar", "baz" });

      canon[numTests++] = new CanonicalTest("/foo//bar/baz",
         new String[] { "foo", "bar", "baz" });

      canon[numTests++] = new CanonicalTest(" /  //foo /  /  //bar// / / / /baz",
         new String[] { "foo", "bar", "baz" });

      canon[numTests++] = new CanonicalTest("/foo/bar/baz",
         new String[] { "foo", "bar", "baz" });

      canon[numTests++] = new CanonicalTest("foo // /  //bar // / /baz  ",
         new String[] { "foo", "bar", "baz" });

      for (int i = 0; i < numTests; i++)
      {
         String[] result = PSDtdTree.canonicalToArray(canon[i].source());
         assertTrue(canon[i].canonEquals(result));
      }
   }

   // represents a test case (source string and expected canonicalization)
   private static class CanonicalTest
   {
      public CanonicalTest(String source, String[] canon)
      {
         m_source = source;
         m_canon = canon;
      }

      public String source()
      {
         return m_source;
      }

      public boolean canonEquals(String[] canon)
      {
         return Arrays.equals(canon, m_canon);
      }

      private String m_source;
      private String[] m_canon;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSDtdTreeTest("testCanonicalToArray"));
      return suite;
   }

}
