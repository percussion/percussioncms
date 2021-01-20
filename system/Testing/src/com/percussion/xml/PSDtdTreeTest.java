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
