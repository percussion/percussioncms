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
