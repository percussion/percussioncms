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
