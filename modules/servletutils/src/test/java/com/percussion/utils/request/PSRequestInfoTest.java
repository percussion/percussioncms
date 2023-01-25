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
package com.percussion.utils.request;

import java.util.HashMap;
import java.util.Map;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
import org.junit.Test;

/**
 * Test request info
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSRequestInfoTest extends TestCase
{

   /**
    * @param arg0
    */
   public PSRequestInfoTest(String arg0) {
      super(arg0);
      // TODO Auto-generated constructor stub
   }
   
   @Test
   public static TestSuite suite()
   {
      return new TestSuite(PSRequestInfoTest.class);
   }
   
   @Test
   public void test10Basic()
   {
      try
      {
         PSRequestInfo.setRequestInfo("FOO", "BAR");
         assertTrue("No exception where expected", false);
      }
      catch(Exception e)
      {
         // OK
      }
      
      PSRequestInfo.initRequestInfo((Map<String,Object>) null);
      String value = (String) PSRequestInfo.getRequestInfo("FOO");
      assertNull(value);
      
      PSRequestInfo.setRequestInfo("FOO", "YES");
      value = (String) PSRequestInfo.getRequestInfo("FOO");
      assertEquals("YES", value);
      
      PSRequestInfo.resetRequestInfo();     
   }
   
   @Test
   public void test20Initial()
   {
      Map<String,Object> initial = new HashMap<String,Object>();
      initial.put("FOO", "BAR");
      
      PSRequestInfo.initRequestInfo(initial);
      String v = (String) PSRequestInfo.getRequestInfo("FOO");
      assertEquals("BAR", v);      
   }

}
