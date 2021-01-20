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

package com.percussion.server;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

public class ThreadLocalPropertiesTest extends TestCase
{
   public void testInjectThreadLocalProperties() throws InterruptedException, ExecutionException
   {
      ExecutorService pool = Executors.newFixedThreadPool(1); 
      
      System.setProperty("Test1", "Test1Val");

      String vmName = System.getProperty("java.vm.name");
      assertNotNull(vmName);
      
      // Inject properties interceptor
      ThreadLocalProperties.setupProperties();
  
      // Check existing values still work.
      assertEquals("Test1Val", System.getProperty("Test1"));
      assertEquals(vmName, System.getProperty("java.vm.name"));
      
      // Set thread Local Value
      System.setProperty( "threadlocal.Test1", "Test1Val2");
      assertEquals("Test1Val2", System.getProperty("Test1"));
      
      // Change underlying setting
      System.setProperty( "Test1", "Test1Val3");
      
      // Check Thread local value restored to default after remove.
      System.getProperties().remove( "threadlocal.Test1");
      assertEquals("Test1Val3", System.getProperty("Test1"));
      
      assertEquals(vmName, System.getProperty("java.vm.name"));
      
      // Reset test property
      System.setProperty( "Test1", "Test1Val3");
      // Can the thread see the existing properties
      Callable<String> callable = new LocalPropertyCallable(null,null,"Test1");
      Future<String> future = pool.submit(callable);
      String result = future.get();
      assertEquals("Test1Val3",result);
      
      // Test setting regular property effects underlying value. 
      Callable<String> callable2 = new LocalPropertyCallable("Test1","TestVal5","Test1");
      Future<String> future2 = pool.submit(callable2);
      String result2 = future2.get();
      assertEquals("TestVal5",result2);
      assertEquals(System.getProperty("Test1"),"TestVal5");
      
      // Inner class sees set value for Test 1, this does not change value in main thread
      Callable<String> callable3 = new LocalPropertyCallable("threadlocal.Test1","TestVal6","Test1");
      Future<String> future3 = pool.submit(callable3);
      String result3 = future3.get();
      assertEquals("TestVal6",result3);
      assertEquals(System.getProperty("Test1"),"TestVal5");
      
      
      
      
   }

   public static class LocalPropertyCallable implements Callable<String>
   {
      private String setProp;
      private String getProp;
      private String setVal;

      public LocalPropertyCallable(String setProp, String setVal, String getProp)
      {
         this.setProp = setProp;
         this.getProp = getProp;
         this.setVal = setVal;
      }

      public String call()
      {
         if (setProp != null)
            System.setProperty( setProp, setVal);
         if (getProp!=null)
            return System.getProperty(getProp);
         return null;
      }
   }
   
}
