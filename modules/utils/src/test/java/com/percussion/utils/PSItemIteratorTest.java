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
package com.percussion.utils;

import com.percussion.utils.jsr170.PSItemIterator;
import junit.framework.TestCase;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Tests to ensure correct behavior for the base item iterator implementation.
 * 
 * @author dougrand
 */
public class PSItemIteratorTest extends TestCase
{
   /**
    * Test class to test the base class of item iterator
    */
   static class TestItemIterator extends PSItemIterator<Object>
   {
      /**
       * Ctor
       * @param things
       * @param filterpattern
       */
      protected TestItemIterator(Map<String, Item> things, String filterpattern) {
         super(things, filterpattern);
      }
   }
   
   /**
    * Test implementation of Item
    */
   static class TestItem implements Item
   {
      /**
       * Name
       */
      String mi_name = null;
      
      /**
       * Ctor
       * @param name
       */
      public TestItem(String name)
      {
         mi_name = name;
      }
      
      public void accept(ItemVisitor arg0) throws RepositoryException
      {
      }

      public Item getAncestor(int arg0) throws ItemNotFoundException, AccessDeniedException, RepositoryException
      {
         return null;
      }

      public int getDepth() throws RepositoryException
      {
         return 0;
      }

      public String getName() throws RepositoryException
      {
         return mi_name;
      }

      public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException
      {
         return null;
      }

      public String getPath() throws RepositoryException
      {
         return null;
      }

      public Session getSession() throws RepositoryException
      {
         return null;
      }

      public boolean isModified()
      {
         return false;
      }

      public boolean isNew()
      {
         return false;
      }

      public boolean isNode()
      {
         return false;
      }

      public boolean isSame(Item arg0) throws RepositoryException
      {
         return false;
      }

      public void refresh(boolean arg0) throws InvalidItemStateException, RepositoryException
      {
      }

      public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException
      {  
      }

      public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException
      {  
      }
   }
   
   /**
    * Test data for multi map testing
    */
   static MultiValuedMap ms_mm = new ArrayListValuedHashMap<>();
   
   static {
      ms_mm.put("aa", "1");
      ms_mm.put("aa", "1.1");
      ms_mm.put("aa", "1.2");
      ms_mm.put("aa", "1.3");
      ms_mm.put("ab", "2");
      ms_mm.put("ab", "2.1");
      ms_mm.put("ab", "2.2");
      ms_mm.put("ab", "2.3");
      ms_mm.put("ac", "3");
      ms_mm.put("ba", "4");
      ms_mm.put("ba", "4.1");
      ms_mm.put("ba", "4.2");
      ms_mm.put("bb", "5");
      ms_mm.put("bc", "6");
      ms_mm.put("bc", "6.1");
      ms_mm.put("bc", "6.2");
      ms_mm.put("bc", "6.3");
      ms_mm.put("bc", "6.4");
      ms_mm.put("ca", "7");
      ms_mm.put("cb", "8");
      ms_mm.put("cc", "9");
   }
   
   /**
    * Set that results from iterating over the filter a*
    */
   static Set<String> ms_result1 = new HashSet<String>();
   
   /**
    * Set that results from iterating over the filter ba
    */
   static Set<String> ms_result2 = new HashSet<String>();
   
   static
   {
      ms_result1.add("1");
      ms_result1.add("1.1");
      ms_result1.add("1.2");
      ms_result1.add("1.3");
      ms_result1.add("2");
      ms_result1.add("2.1");
      ms_result1.add("2.2");
      ms_result1.add("2.3");
      ms_result1.add("3");
      
      ms_result2.add("4");
      ms_result2.add("4.1");
      ms_result2.add("4.2");
   }
   
   
   /**
    * Test multi map iteration behavior for the item iterator.
    */
   @SuppressWarnings("unchecked")
   public void testMultiMap()
   {
      TestItemIterator ti = new TestItemIterator(ms_mm.asMap(), "a*");
      
      assertEquals(0, ti.getPosition());
      assertEquals(9, ti.getSize());
      assertEquals(0, ti.getPosition());
      
      Set<String> allAStar = new HashSet<String>();
      while(ti.hasNext())
      {
         allAStar.add((String) ti.next());
      }
      assertEquals(ms_result1, allAStar);
      
      ti = new TestItemIterator(ms_mm.asMap(), "ba");
      
      assertEquals(0, ti.getPosition());
      assertEquals(3, ti.getSize());
      assertEquals(0, ti.getPosition());
      
      Set<String> allBA = new HashSet<String>();
      while(ti.hasNext())
      {
         allBA.add((String) ti.next());
      }
      assertEquals(ms_result2, allBA);
      
      // Test unfiltered case
      ti = new TestItemIterator(ms_mm.asMap(), null);
      
      int count = 0;
      Iterator<Object> v = ms_mm.values().iterator();
      while(v.hasNext())
      {
         v.next();
         count++;
      }
      assertEquals(0, ti.getPosition());
      assertEquals(count, ti.getSize());
      assertEquals(0, ti.getPosition());
   }
   
   /**
    * Test regular maps
    * @throws RepositoryException 
    */
   public void testMapIteration() throws RepositoryException
   {
      Map<String,Item> test = new HashMap<String,Item>();
      
      test.put("aa", new TestItem("aa"));
      test.put("ab", new TestItem("ab"));
      test.put("ac", new TestItem("ac"));
      test.put("ba", new TestItem("ba"));
      test.put("bb", new TestItem("bb"));
      test.put("bc", new TestItem("bc"));
      
      TestItemIterator ti = new TestItemIterator(test, "a*");
      
      assertEquals(0, ti.getPosition());
      assertEquals(3, ti.getSize());
      assertEquals(0, ti.getPosition());
      
      Set<String> result = new HashSet<String>();
      int i = 0;
      while(ti.hasNext())
      {
         assertEquals(i++, ti.getPosition());
         Item item = (Item) ti.next();
         result.add(item.getName());
      }
      
      assertEquals(3, result.size());
      assertTrue(result.contains("aa"));
      assertTrue(result.contains("ab"));
      assertTrue(result.contains("ac"));
   }
   
}
