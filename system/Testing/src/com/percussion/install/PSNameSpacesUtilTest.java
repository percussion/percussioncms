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
package com.percussion.install;

import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.percussion.install.PSNameSpacesUtil.removeWhitespacesFromName;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Category(UnitTest.class)
public class PSNameSpacesUtilTest
{

   @Test
   public void testremoveWhitespacesFromName()
   {
      assertEquals("ab", removeWhitespacesFromName("ab", asSet("1", "2")));
      assertEquals("_a___b__",
            removeWhitespacesFromName(" a\t \nb \n", asSet("1", "2")));
      assertEquals("___", removeWhitespacesFromName("\t\n ", asSet("1", "2")));
      assertEquals("", removeWhitespacesFromName("", asSet("1", "2")));

      assertEquals("a_b", removeWhitespacesFromName("a b", asSet("1", "2")));
      assertEquals("a_b2", removeWhitespacesFromName("a b",
            asSet("a_b", "a_b1")));
      assertEquals("a_b3", removeWhitespacesFromName("a b",
            asSet("a_b", "a_b1", "a_b2")));
   }
   
   /**
    * Convenience method to create unchangeable set of strings.
    */
   private Set<String> asSet(String... strs)
   {
      final Set<String> set = new HashSet<String>();
      Collections.addAll(set, strs);
      return Collections.unmodifiableSet(set); 
   }
}
