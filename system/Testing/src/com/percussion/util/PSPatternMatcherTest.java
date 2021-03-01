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
package com.percussion.util;

import java.security.SecureRandom;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.percussion.utils.tools.PSPatternMatcher;

/**
 * Unit tests for the PSPatternMatcher class
 */
public class PSPatternMatcherTest extends TestCase
{
   public PSPatternMatcherTest(String name)
   {
      super(name);
   }

   /**
    * Assert that a newly constructed object is in the correct state
    */
   public void testConstructor()
   {
      PSPatternMatcher matchAnything = new PSPatternMatcher('?', '*', "*");
      assertTrue('?' == matchAnything.getMatchOne());
      assertTrue('*' == matchAnything.getMatchZeroOrMore());
      assertTrue(matchAnything.isCaseSensitive());
   }

   public void testMatchAnything()
   {
      PSPatternMatcher matchAnything = new PSPatternMatcher('?', '*', "*");
      SecureRandom rand = new SecureRandom();
      String randStr;
      for (int i = 0; i < 100; i++)
      {
         randStr = randomString(rand);
         assertTrue(randStr, matchAnything.doesMatchPattern(randStr));
      }
   }

   public void testMatchSubstring()
   {
      PSPatternMatcher matchAnything = new PSPatternMatcher('?', '*', "*fa*");
      SecureRandom rand = new SecureRandom();
      String randStr;
      for (int i = 0; i < 100; i++)
      {
         randStr = randomString(rand);
         if (matchAnything.doesMatchPattern(randStr))
            assertTrue(randStr, (-1 != randStr.indexOf("fa")));
      }
      assertTrue("fa", matchAnything.doesMatchPattern("fa"));
      assertTrue("fax", matchAnything.doesMatchPattern("fax"));
      assertTrue("sfa", matchAnything.doesMatchPattern("sfa"));
      assertTrue("sfax", matchAnything.doesMatchPattern("sfax"));
      assertTrue("af", !matchAnything.doesMatchPattern("af"));
   }

   public void testMatchSplitString()
   {
      PSPatternMatcher matchAnything = new PSPatternMatcher('?', '*', "a*a");
      assertTrue("a", !matchAnything.doesMatchPattern("a"));
      assertTrue("aa", matchAnything.doesMatchPattern("aa"));
      assertTrue("aaa", matchAnything.doesMatchPattern("aaa"));
      assertTrue("aba", matchAnything.doesMatchPattern("aba"));
      assertTrue("abababa", matchAnything.doesMatchPattern("abababa"));
      assertTrue("abba", matchAnything.doesMatchPattern("abba"));
      assertTrue("abb", !matchAnything.doesMatchPattern("abb"));
      assertTrue("bba", !matchAnything.doesMatchPattern("bba"));
      assertTrue("aaaaaaaa", matchAnything.doesMatchPattern("aaaaaaaa"));
   }

   // utility method to generate a random String of length <= 100
   // consisting of the printable ASCII characters (of course,
   // encoded with the default encoding)
   protected static String randomString(Random rand)
   {
      byte[] bytes = new byte[rand.nextInt(99) + 1];
      rand.nextBytes(bytes);
      
      // coerce all bytes into ASCII range 32 <= i >= 126
      byte b;
      for (int i = 0; i < bytes.length; i++)
      {
         b = bytes[i];
         if (b < 0)
            b = (byte)-b;
         if (b < (byte)32)
            b = (byte)(126 - b);
         else if (b > (byte)126)
            b = (byte)(252 - b);
         bytes[i] = b;
      }

      return new String(bytes);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSPatternMatcherTest("testConstructor"));
      suite.addTest(new PSPatternMatcherTest("testMatchAnything"));
      suite.addTest(new PSPatternMatcherTest("testMatchSubstring"));
      suite.addTest(new PSPatternMatcherTest("testMatchSplitString"));
      return suite;
   }
}
