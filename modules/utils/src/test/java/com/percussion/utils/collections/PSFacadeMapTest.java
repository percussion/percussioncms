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
package com.percussion.utils.collections;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PSFacadeMapTest extends TestCase
{
   

   Map<String, String> inner2 = null;

   PSFacadeMap<String, String> fmap = null;

   private Map<String, String> inner;

   /*
    * (non-Javadoc)
    * 
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      HashMap<String, String> map = new HashMap<String,String>();
      map.put("a", "1");
      map.put("b", "2");
      map.put("c", "3");
      inner = Collections.unmodifiableMap(map);
   }
   
   @SuppressWarnings("unchecked")
   public void testClone() throws Exception
   {
      fmap = new PSFacadeMap<String, String>(inner);
      PSFacadeMap<String,String> c = (PSFacadeMap<String, String>) fmap.clone();
      
      assertEquals(fmap,c);
      fmap.put("d", "4");
      assertFalse(fmap.equals(c));
      c.put("d","4");
      assertEquals(fmap,c);
      assertEquals(4, fmap.entrySet().size());
      assertEquals(3, inner.entrySet().size());
   }

   public void testVarious() throws Exception
   {
      fmap = new PSFacadeMap<String, String>(inner);
      
      assertEquals(fmap.get("a"), "1");
      assertEquals(fmap.get("b"), "2");
      assertEquals(fmap.get("c"), "3");

      // Check the key set
      assertEquals(fmap.keySet().size(), 3);
      assertEquals(fmap.values().size(), 3);

      assertTrue(fmap.containsKey("a"));
      assertTrue(fmap.containsKey("b"));
      assertTrue(fmap.containsKey("c"));

      assertTrue(fmap.containsValue("1"));
      assertTrue(fmap.containsValue("2"));
      assertTrue(fmap.containsValue("3"));

      String oldval = fmap.remove("c");
      assertEquals(oldval, "3");
      assertNull(fmap.get("c"));

      fmap.put("b", "99");
      assertEquals(fmap.get("b"), "99");
      assertEquals(2, fmap.values().size());
      assertTrue(fmap.containsKey("a"));
      assertTrue(fmap.containsKey("b"));
      assertTrue(!fmap.containsKey("c"));

      assertTrue(fmap.containsValue("1"));
      assertTrue(!fmap.containsValue("2"));
      assertTrue(!fmap.containsValue("3"));
      assertTrue(fmap.containsValue("99"));

      assertTrue(!fmap.isEmpty());
      
      PSFacadeMap<String, String> newmap = new PSFacadeMap<String, String>(
            new HashMap<String, String>());
      assertTrue(newmap.isEmpty());
      
      fmap.put("d", "4");
      assertEquals("4", fmap.get("d"));
      assertTrue(fmap.containsKey("d"));
      assertTrue(fmap.containsValue("4"));
   }
   
  
   
   public void testRemove() throws Exception
   {
      fmap = new PSFacadeMap<String, String>(inner);
      
      fmap.remove("b");
      assertFalse(fmap.containsKey("b"));
      assertNull(fmap.get("b"));
      assertNotNull(fmap.get("a"));
      assertFalse(fmap.containsValue("2"));
   }

}
