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
package com.percussion.utils.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

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
