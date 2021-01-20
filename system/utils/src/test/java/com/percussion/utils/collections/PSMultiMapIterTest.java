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
package com.percussion.utils.collections;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;

public class PSMultiMapIterTest extends TestCase
{
   public PSMultiMapIterTest(String name) {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSMultiMapIterTest.class);
   }

   public void test1() throws Exception
   {
      MultiMap map = new MultiHashMap();

      map.put("a", 1);
      map.put("b", 2);
      map.put("b", 3);
      map.put("b", 4);
      map.put("c", 5);
      map.put("d", 6);

      PSMultiMapIterator<Integer> simple = new PSMultiMapIterator<Integer>(map,
            null);
      Set<Integer> results = new HashSet<Integer>();
      int count = 0;
      while (simple.hasNext())
      {
         results.add(simple.next());
         count++;
      }

      assertEquals(6, count);
      assertTrue(results.contains(1));
      assertTrue(results.contains(2));
      assertTrue(results.contains(3));
      assertTrue(results.contains(4));
      assertTrue(results.contains(5));
      assertTrue(results.contains(6));

   }

   public void test2() throws Exception
   {
      MultiMap map = new MultiHashMap();

      map.put("a", 1);
      map.put("b", 2);
      map.put("b", 3);
      map.put("b", 4);
      map.put("c", 5);
      map.put("d", 6);

      PSMultiMapIterator<Integer> simple = new PSMultiMapIterator<Integer>(map,
            new Predicate()
            {

               public boolean evaluate(Object o)
               {
                  String key = (String) o;
                  return key.equals("b");
               }
            });

      Set<Integer> results = new HashSet<Integer>();
      int count = 0;
      while (simple.hasNext())
      {
         results.add(simple.next());
         count++;
      }

      assertEquals(3, count);
      assertTrue(results.contains(2));
      assertTrue(results.contains(3));
      assertTrue(results.contains(4));

   }
}
