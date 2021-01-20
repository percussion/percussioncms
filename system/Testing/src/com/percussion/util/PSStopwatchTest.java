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
package com.percussion.util;

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
      if (delta > 30)
      {
         throw new AssertionFailedError("Times varied by more than 30 millis. " +
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
