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

package com.percussion.server.cache;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSAutotuneCacheTest extends ServletTestCase
{
   
   // see base class
   public PSAutotuneCacheTest(String name)
   {
      super(name);
   }
   
   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSAutotuneCacheTest("testConstructors"));
      suite.addTest(new PSAutotuneCacheTest("testUpdateCache"));
      return suite;
   }
   
   public void testConstructors() throws Exception
   {
      
      boolean didThrow = false;
      try 
      {
         PSAutotuneCache settings = PSAutotuneCacheLocator.getAutotuneCache();
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      
      assertTrue("Null file passed as ehcache",didThrow);
      
      didThrow = false;
      
      try
      {
         PSAutotuneCache settings = PSAutotuneCacheLocator.getAutotuneCache();
      }
      catch (Exception ex) 
      {
         didThrow = true;
      }
      
      assertFalse("Default Constructor OK.", didThrow);
   }
   
   public void testUpdateCache() throws Exception 
   {
      boolean didThrow = false;
      
      try 
      {             
         PSAutotuneCache settings = PSAutotuneCacheLocator.getAutotuneCache();
         settings.updateEhcache();
      }
      catch (Exception ex)
      {
         didThrow = true;
      }
      
      assertFalse("Update ehcache OK", didThrow);
   }
}
