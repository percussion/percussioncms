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
package com.percussion.utils.string;

import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;


@Category(UnitTest.class)
public class PSPatternMatchTest
{
   @Test
   public void testMatchAll()
   {
      PSPatternMatch pattern = new PSPatternMatch("*");
      
      assertTrue(pattern.match("abc"));
      assertTrue(pattern.match("abcyu"));
      assertTrue(pattern.match("ab"));

   }

   @Test
   public void testBegin()
   {
      PSPatternMatch pattern = new PSPatternMatch("abc*");
      
      assertTrue(pattern.match("abc"));
      assertTrue(pattern.match("abcyu"));
      assertTrue(!pattern.match("ab"));

      pattern = new PSPatternMatch("abc");
      assertTrue(pattern.match("abc"));
      assertTrue(!pattern.match("abcyu"));
      assertTrue(!pattern.match("ab"));
   }

   @Test
   public void testBegin_Percent()
   {
      PSPatternMatch pattern = new PSPatternMatch("abc%", "%");
      
      assertTrue(pattern.match("abc"));
      assertTrue(pattern.match("abcyu"));
      assertTrue(!pattern.match("ab"));

      pattern = new PSPatternMatch("abc");
      assertTrue(pattern.match("abc"));
      assertTrue(!pattern.match("abcyu"));
      assertTrue(!pattern.match("ab"));
   }

   @Test
   public void testEnd()
   {
      PSPatternMatch pattern = new PSPatternMatch("*abc");
      
      assertTrue(pattern.match("abc"));
      assertTrue(pattern.match("yu-abc"));
      assertTrue(!pattern.match("ab"));
   }

   @Test
   public void testEnd_Percent()
   {
      PSPatternMatch pattern = new PSPatternMatch("%abc", "%");
      
      assertTrue(pattern.match("abc"));
      assertTrue(pattern.match("yu-abc"));
      assertTrue(!pattern.match("ab"));
   }

   @Test
   public void testMixed()
   {
      PSPatternMatch pattern = new PSPatternMatch("*abc*xyz*");
      
      assertTrue(!pattern.match("abc"));
      assertTrue(!pattern.match("yu-abc"));
      assertTrue(!pattern.match("ab"));

      assertTrue(pattern.match("yxabc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz"));
      assertTrue(pattern.match("abcxyz"));

      pattern = new PSPatternMatch("*abc**xyz*");

      assertTrue(!pattern.match("abc"));
      assertTrue(!pattern.match("yu-abc"));
      assertTrue(!pattern.match("ab"));

      assertTrue(pattern.match("yxabc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz"));
      assertTrue(pattern.match("abcxyz"));
   }

   @Test
   public void testMixed_Percent()
   {
      PSPatternMatch pattern = new PSPatternMatch("%abc%xyz%", "%");
      
      assertTrue(!pattern.match("abc"));
      assertTrue(!pattern.match("yu-abc"));
      assertTrue(!pattern.match("ab"));

      assertTrue(pattern.match("yxabc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz"));
      assertTrue(pattern.match("abcxyz"));

      pattern = new PSPatternMatch("%abc%%xyz%", "%");

      assertTrue(!pattern.match("abc"));
      assertTrue(!pattern.match("yu-abc"));
      assertTrue(!pattern.match("ab"));

      assertTrue(pattern.match("yxabc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz--sdf"));
      assertTrue(pattern.match("abc--xyz"));
      assertTrue(pattern.match("abcxyz"));
   }

   @Test
   public void testMatchedStrings()
   {
      String[] names = new String[] {"yxabc--xyz--sdf", "abcxyz", "yu-abc"};
      
      Collection<String> strList = PSPatternMatch.matchedStrings("*abc*xyz*",
            Arrays.asList(names));
      assertTrue(strList.size() == 2);
   }
}
