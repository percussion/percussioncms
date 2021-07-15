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
package com.percussion.utils.jsr170;

import org.junit.Test;

import static org.junit.Assert.*;

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
