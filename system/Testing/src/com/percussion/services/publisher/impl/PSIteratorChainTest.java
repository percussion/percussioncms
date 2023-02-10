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

package com.percussion.services.publisher.impl;

import static java.util.Arrays.asList;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class PSIteratorChainTest extends TestCase
{

   private List<List<String>> values = new ArrayList<List<String>>();
   @Before
   public void setUp() throws Exception
   {
      values.add(asList("a","b"));
      values.add(Collections.<String>emptyList());
      values.add(asList("c"));
   }

   @Test
   public void testNext()
   {
      List<String> expected = asList("a","b","c");
      final Iterator<List<String>> vit = values.iterator();
      Iterator<String> it = new PSIteratorChain<String>() {
         @Override
         protected Iterator<String> nextIterator()
         {
            if (vit.hasNext())
               return vit.next().iterator();
            return null;
         }
      };
      assertEquals(expected,Lists.newArrayList(it));
   }

}
