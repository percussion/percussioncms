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
package com.percussion.util;

import java.util.Comparator;

import junit.framework.TestCase;

public class PSIgnoreCaseStringComparatorTest extends TestCase
{
   public void testStringComparator()
   {
      final Comparator<String> cmp = new PSIgnoreCaseStringComparator();
      assertEquals(0, cmp.compare("a", "a"));
      assertEquals(0, cmp.compare("a", "A"));
      assertEquals(0, cmp.compare("A", "a"));
      assertEquals(0, cmp.compare("A", "A"));
      
      assertTrue(cmp.compare("a", "b") < 0);
      assertTrue(cmp.compare("A", "b") < 0);
      assertTrue(cmp.compare("a", "aa") < 0);
      assertTrue(cmp.compare("aa", "a") > 0);
      assertTrue(cmp.compare("a", "") > 0);
   }
}
