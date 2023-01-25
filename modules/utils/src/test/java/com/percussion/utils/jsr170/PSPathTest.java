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
package com.percussion.utils.jsr170;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Test PSPath
 * 
 * @author dougrand
 */
public class PSPathTest
{

   public PSPathTest() {
   }

   /**
    * Basic tests
    * 
    * @throws Exception
    */
   @Test
   public void test1() throws Exception
   {
      PSPath p = new PSPath("/a/b/c");
      assertTrue(!p.isRelative());
      assertEquals("a", p.getName(0));
      assertEquals("b", p.getName(1));
      assertEquals("c", p.getName(2));
      assertEquals("c", p.getLastName());
      assertEquals(-1, p.getIndex(0));
      assertEquals(-1, p.getIndex(1));
      assertEquals(-1, p.getIndex(2));
      assertEquals("/a/b/c", p.toString());
      PSPath x = new PSPath("a/b/c");
      PSPath y = new PSPath("/a/b/c");
      assertEquals(y, p);
      assertNotSame(y, x);
      assertEquals(y.hashCode(), p.hashCode());
      
      p = new PSPath("a");
      assertTrue(p.isRelative());
      assertEquals("a", p.getName(0));
      
      p = new PSPath("a/b[3]");
      p = new PSPath("a[0]");
      assertEquals(0, p.getIndex(0));
      assertEquals("a", p.getName(0));
      p = new PSPath("a[0]/b[2]");
      assertEquals(0, p.getIndex(0));
      assertEquals("a", p.getName(0));
      assertEquals(2, p.getIndex(1));
      assertEquals("b", p.getName(1));
   }
   
   /**
    * Test directory handling methods
    */
   @Test
   public void testDir()
   {
      PSPath source = new PSPath("/a/b");
      PSPath result;
      
      result = source.getAllButLast();
      assertEquals("/a", result.toString());
      
      source = new PSPath("b/c/d/e");
      assertEquals("b/c/d", source.getAllButLast().toString());
      
      source = new PSPath("/a/b/c/d");
      result = source.getRest();
      assertEquals("b/c/d", result.toString());
      result = result.getRest();
      assertEquals("c/d", result.toString());
      result = result.getRest();
      assertEquals("d", result.toString());
   }
   
   /**
    * Test
    */
   @Test
   public void testDoubleSlash()
   {
      PSPath source = new PSPath("//");
      assertFalse(source.isRelative());
      assertEquals(0, source.getCount());
   }
   
   /**
    * Test subpath functions
    */
   @Test
   public void testSubpath()
   {
      PSPath source = new PSPath("/a/b/c/d");
      PSPath result;
      
      result = source.subpath(0);
      assertEquals("/a/b/c/d", result.toString());
      
      result = source.subpath(1);
      assertEquals("b/c/d", result.toString());
      
      result = source.subpath(2);
      assertEquals("c/d", result.toString());
      
      result = source.subpath(3);
      assertEquals("d", result.toString());
      
      result = source.subpath(0,1);
      assertEquals("/a", result.toString());
      
      result = source.subpath(0,2);
      assertEquals("/a/b", result.toString());
      
      result = source.subpath(0,3);
      assertEquals("/a/b/c", result.toString());
      
      result = source.subpath(0,4);
      assertEquals("/a/b/c/d", result.toString());

      source = new PSPath("/a");
      result = source.subpath(0);
      assertEquals("/a", result.toString());
   }
   
}
