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
package com.percussion.utils.timing;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author dougrand
 *
 * Unit tests for stopwatch class
 */
public class PSStopwatchTest extends TestCase
{   
   /**
    * @param arg0
    */
   public PSStopwatchTest(String arg0)
   {
      super(arg0);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSStopwatchTest.class);
   }
   
   /**
    * Checks to see if the two times are within some milliseconds. 
    * Some variance is allowed to keep the test from failing on hiccups and
    * inaccuracies in the bios timer. The basic issue is that the bios timer
    * can vary by around 20 ms.
    * @param time1
    * @param time2
    * @throws AssertionFailedError
    */
   public void checkReasonable(double time1, double time2)
   throws AssertionFailedError
   {
      double delta = Math.abs(time1 - time2);
      
      System.err.println("Delta: " + delta);
      if (delta > 80)
      {
         throw new AssertionFailedError("Times varied by more than 80 millis. " +
            "real variance was " + delta + " millis");
      }
   }
   
   public synchronized void testSimple() throws Exception
   {
      PSStopwatch w = new PSStopwatch();
      
      w.start();
      wait(400);
      w.stop();
      
      checkReasonable(400, w.elapsed());
   }
   
   public synchronized void testPause() throws Exception
   {
      PSStopwatch w = new PSStopwatch();
      
      w.start();
      wait(400);
      w.pause();
      wait(200);
      w.cont();
      wait(300);
      w.stop();
      
      checkReasonable(700, w.elapsed());
   }
   
   public void testStatechecks() throws Exception
   {
      PSStopwatch w = new PSStopwatch();
      
      try
      {
         w.stop(); // Should throw an exception
         assertTrue("Failed to throw expected exception on stopping a non" +
            " started stopwatch", true);
      }
      catch(Exception e)
      {
         // Ignore
      }
      
      w.start();
      
      try
      {
         w.start(); // Should throw an exception
         assertTrue("Failed to throw expected exception on starting a " +
            " started stopwatch", true);
      }
      catch(Exception e)
      {
         // Ignore
      } 
      
      w.pause();
      w.pause(); // Should not thrown an exception
      w.cont();
      w.cont(); // Should not throw an exception    
      
   }
   
   public synchronized void testOutput() throws InterruptedException
   {
      PSStopwatch w = new PSStopwatch();
      
      w.start();
      wait(1200);
      w.stop();
      
      String s = w.toString();
      System.out.println(s);      
   }
}
