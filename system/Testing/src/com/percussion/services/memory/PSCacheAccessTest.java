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
package com.percussion.services.memory;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;
import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the cache service interface
 * 
 * @author dougrand
 *
 */
@Category(IntegrationTest.class)
public class PSCacheAccessTest
{
   /**
    * Testing cache setup
    */
   public static String ms_ehcache = "<ehcache>" 
         + "    <defaultCache"
         + "        maxElementsInMemory=\"10000\""
         + "        eternal=\"false\"" + "        timeToIdleSeconds=\"3000\""
         + "        timeToLiveSeconds=\"3000\"" + "        />\n"
         + "    <cache name=\"object\""
         + "        maxElementsInMemory=\"1000\"" 
         + "        eternal=\"false\""
         + "        overflowToDisk=\"false\""
         + "        timeToIdleSeconds=\"5\""
         + "        timeToLiveSeconds=\"10\"" + "        />"
         + "    <cache name=\"region2\""
         + "        maxElementsInMemory=\"1000\"" 
         + "        eternal=\"false\""
         + "        overflowToDisk=\"false\""
         + "        timeToIdleSeconds=\"500\""
         + "        timeToLiveSeconds=\"10000\"" + "        />"         
         + "</ehcache>";

   /**
    * Define the region to use for the test
    */
   public static String ms_region = "object";
   
   /**
    * A simple test class 
    */
   public static class TestClass implements Serializable
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      /**
       * Random data for testing purposes
       */
      int a; 
      /**
       * Random data for testing purposes
       */
      int b;
      /**
       * Random data for testing purposes
       */
      int c; 
      /**
       * Random data for testing purposes
       */
      int d;
      /**
       * Random data for testing purposes
       */
      long e;
      /**
       * Random data for testing purposes
       */ 
      long f; 
      /**
       * Random data for testing purposes
       */
      long g;
      /**
       * Random data for testing purposes
       */ 
      long i;
      /**
       * Random data for testing purposes
       */
      float j;
      /**
       * Random data for testing purposes
       */
      float k;
      /**
       * Random data for testing purposes
       */
      float l;
      /**
       * Random data for testing purposes
       */
      float m;
      /**
       * Random data for testing purposes
       */
      String o;
      /**
       * Random data for testing purposes
       */
      String p;
      /**
       * Random data for testing purposes
       */
      String q;
      
      /**
       * Ctor
       */
      public TestClass()
      {
         SecureRandom r = new SecureRandom();
         
         a = r.nextInt();
         b = r.nextInt();
         c = r.nextInt();
         d = r.nextInt();
         e = r.nextLong();
         f = r.nextLong();
         g = r.nextLong();
         i = r.nextLong();
         j = r.nextFloat();
         k = r.nextFloat();
         l = r.nextFloat();
         m = r.nextFloat();
         o = makeRandomString(r.nextInt());
         p = makeRandomString(r.nextInt());
         q = makeRandomString(r.nextInt());
      }

      /**
       * Create random string
       * @param seed
       * @return random string, never <code>null</code>
       */
      private String makeRandomString(int seed)
      {
         SecureRandom r = new SecureRandom();
         int len = r.nextInt(500) + 100;
         StringBuilder str = new StringBuilder(len);
         for(int x = 0; x < len; x++)
         {
            str.append((char) r.nextInt(26) + ' ');
         }
         return str.toString();
      }
      
      /* (non-Javadoc)
       * @see java.lang.Object#equals(java.lang.Object)
       */
      public boolean equals(Object other)
      {
         return EqualsBuilder.reflectionEquals(this, other);
      }
      
      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      public int hashCode()
      {
         return (int) (a + b + c + d + e + f + g);
      }
      
   }


   @BeforeClass
   public static void setup() throws Exception
   {
      CacheManager.create(new ByteArrayInputStream(
            ms_ehcache.getBytes()));
   }

   /**
    * Basic testing
    * @throws Exception
    */
   @Test
   public void testCache1() throws Exception
   {
      IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();

      cache.save("a", "b", ms_region);

      Serializable rval;

      rval = cache.get("a", ms_region);

      assertEquals(rval, "b");

      cache.evict("a", ms_region);

      rval = cache.get("a", ms_region);

      assertNull(rval);

      cache.save("aa", "bb", ms_region);

      rval = cache.get("aa", ms_region);

      assertEquals(rval, "bb");

      // Wait 10 seconds for the eviction - can only run outside the server
      // Thread.sleep(10000);
      //
      // rval = cache.get("aa", ms_region);
      //
      //      assertNull(rval);

   }
   
   /**
    * Simple performance test
    * 
    * @throws Exception
    */
   @Test
   public void testCacheTiming() throws Exception
   {
      IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
      PSStopwatch sw = new PSStopwatch();
      Map<String,Object> mapstore = new HashMap<String,Object>();
      TestClass instances[] = new TestClass[100];
      String keys[] = new String[100];
      for(int i = 0; i < instances.length; i++)
      {
         instances[i] = new TestClass();
         keys[i] = "key" + i;
      }
      // Serialize to map and time
      sw.start();
      for(int i = 0; i < instances.length; i++)
      {
         mapstore.put(keys[i], instances[i]);
      }
      sw.stop();
      System.out.println("Putting " + instances.length + " into map took " + sw);
      
      // Serialize from map and time
      sw.start();
      for(int i = 0; i < instances.length; i++)
      {
         mapstore.get(keys[i]);
      }
      sw.stop();     
      System.out.println("Getting " + instances.length + " into map took " + sw);
      
      // Serialize to cache and time
      sw.start();
      for(int i = 0; i < instances.length; i++)
      {
         cache.save(keys[i], instances[i], ms_region);
      }
      sw.stop();
      System.out.println("Putting " + instances.length + " into map took " + sw);
      
      // Serialize from cache and time
      sw.start();
      for(int i = 0; i < instances.length; i++)
      {
         TestClass val = (TestClass) cache.get(keys[i], ms_region);
         assertTrue(val == instances[i]);
      }
      sw.stop();     
      System.out.println("Getting " + instances.length + " into map took " + sw);      
   }
}
