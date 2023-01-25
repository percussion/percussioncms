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
package test.percussion.pso.utils;

import static org.junit.Assert.*;
import static org.apache.commons.collections.CollectionUtils.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.percussion.pso.utils.MutableHttpServletRequestWrapper;

public class MutableHttpServletRequestWrapperTest
{
   
   MutableHttpServletRequestWrapper cut;
   
   @Before
   public void setUp() throws Exception
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setParameter("foo", "foo");
      request.setParameter("bar", "bar");
      request.setParameter("baz", new String[]{"baz","baz"});
      request.addHeader("foo", "bar");
      
      
      cut = new MutableHttpServletRequestWrapper(request);
   }
   @Test
   public final void testGetParameterString()
   {
      cut.setParameter("bar", "barbar"); 
      assertEquals("foo", cut.getParameter("foo")); 
      assertEquals("barbar", cut.getParameter("bar")); 
      assertNull(cut.getParameter("fizz")); 
      cut.setParameter("fizz", "fizzfizz");
      assertEquals("fizzfizz", cut.getParameter("fizz"));
      cut.setParameter("bar", new String[]{"bat","ball"});
      assertEquals("bat", cut.getParameter("bar")); 
      String [] b = cut.getParameterValues("bar"); 
      assertNotNull(b);
      assertEquals(2,b.length); 
      assertEquals("ball",b[1]); 
      
   }
   @Test
   @SuppressWarnings("unchecked")
   public final void testGetParameterNames()
   {
      cut.setParameter("biz", "bzzzz");
      Set<String> v = new HashSet<String>();
      v.add("foo");
      v.add("bar");
      v.add("baz");
      v.add("biz"); 
      int i = 0; 
      Enumeration<String> nms = cut.getParameterNames();
      while(nms.hasMoreElements())
      {
         String n = nms.nextElement(); 
         assertTrue(v.contains(n));
         i++; 
      }
      assertEquals(4,i); 
   }
   
   @Test
   public final void testGetHeader() 
   {
      cut.setHeader("baz", "bat");
      String h1 = cut.getHeader("FOO");
      assertNotNull(h1);
      assertEquals("bar",h1);
      
      h1 = cut.getHeader("baz");
      assertNotNull(h1);
      assertEquals("bat",h1);
      
      h1 = cut.getHeader("xyzzy");
      assertNull(h1); 
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public final void testGetHeaders()
   {
      cut.setHeader("baz", new String[]{"bat","ball"});
      String h1 = cut.getHeader("BAZ"); 
      assertNotNull(h1);
      assertEquals("bat",h1); 
      
      Enumeration e = cut.getHeaders("baz");
      List<String> s = new ArrayList<String>();
      while(e.hasMoreElements())
      {
         s.add((String)e.nextElement());
      }
      assertEquals(2,s.size());
      assertTrue(s.contains("bat"));
      assertTrue(s.contains("ball")); 
      
      
      e = cut.getHeaders("foo");
      s = new ArrayList<String>(); 
      while(e.hasMoreElements())
      {
         s.add((String)e.nextElement());
      }
      assertEquals(1,s.size());
      assertTrue(s.contains("bar")); 
      
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public final void testGetHeaderNames()
   {
      cut.setHeader("baz", "bat");
      
      Enumeration e = cut.getHeaderNames();
      List<String> s = new ArrayList<String>();
      addAll(s, e);
      assertEquals(2,s.size());
      /*
       * We have to ignore the case for headers (AMG).
       */
      assertTrue(exists(s, new IgnoreCase("foo")));
      assertTrue(exists(s, new IgnoreCase("baz"))); 
   }
   
   
   private static class IgnoreCase implements Predicate {
       private Object find;

       public IgnoreCase(Object find) {
           super();
           this.find = find;
       }

       public boolean evaluate(Object item) {
           return find.toString().equalsIgnoreCase(item.toString());
       }
   }
}
