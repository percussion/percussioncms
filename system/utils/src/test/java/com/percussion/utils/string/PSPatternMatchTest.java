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
