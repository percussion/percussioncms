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

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;

/**
 * Test the stopwatch stack
 * 
 * @author dougrand
 */
public class PSStopwatchStackTest extends TestCase
{
   private static final Logger ms_log = LogManager.getLogger(PSStopwatchStackTest.class);

   /**
    * Single level test to see that things are working correctly
    * @throws Exception
    */
   public void testOneLevel() throws Exception
   {
      PSStopwatchStack stack = PSStopwatchStack.getStack();
      assertNotNull(stack);
      
      // Make sure that the stack is reset, just in case some other user
      // of this thread has used the stack but has not cleaned it up.
      stack.finish();
      
      assertNull(stack.current());
      stack.start("first");
      assertNotNull(stack.current());

      // Push and check that we have a new current timer
      PSStopwatch c = stack.current();
      stack.start("second");
      assertTrue(!(stack.current() == c));
      PSStopwatch f = stack.stop();
      assertTrue(stack.current() == c);
      assertTrue(!(stack.current() == f));
   }

   /**
    * Check that the timings are working
    * @throws Exception
    */
   public synchronized void testTimings() throws Exception
   {
      PSStopwatchStack stack = PSStopwatchStack.getStack();

      stack.start("first");
      wait(1000);
      stack.start("second");
      wait(1000);
      PSStopwatch c = stack.stop();
      PSStopwatch t = stack.stop();

      assertAboutEqual(1000.0, c.elapsed());
      assertAboutEqual(1000.0, t.elapsed());

      String stats = stack.getStats();
      assertNotNull(stats);
   }

   /**
    * Use a predictable sequence to obtain overhead information for running the
    * stopwatch stack
    * 
    * @throws Exception
    */
   public void testOverhead() throws Exception
   {
      int count = 0;
      SecureRandom rand = new SecureRandom();
      PSStopwatch sw = new PSStopwatch();
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      String cats[] =
      {"one", "two", "three", "four", "five"};

      // Run a few times to warm up the random number generator
      for (int i = 0; i < 500; i++)
      {
         sws.start(cats[rand.nextInt(5)]);
         sws.stop();
      }
      sws.finish(); // Clear
      
      // Run a few times to make sure we don't measure setup
      for (int i = 0; i < 500; i++)
      {
         sws.start(cats[rand.nextInt(5)]);
         sws.stop();
      }
      sws.finish(); // Clear

      // Measure random category selection to subtract from calculation
      sw.start();
      String last = null;
      for (int i = 0; i < 100; i++)
      {
         last = cats[rand.nextInt(5)];
      }
      ms_log.info("Last string selected: " + last);
      sw.stop();
      double randdelay = sw.elapsed();
      sw.start();
      for (int i = 0; i < 500; i++)
      {
         sws.start(cats[rand.nextInt(5)]);
         count++;
         // Dive a random number of levels
         int depth = rand.nextInt(10);
         for (int j = 0; j < depth; j++)
         {
            sws.start(cats[rand.nextInt(5)]);
            count++;
         }
         // Up the same number
         for (int j = 0; j < depth; j++)
         {
            sws.stop();
         }
         sws.stop();
      }
      sw.stop();

      // Display the random number delay to see if it is significant enough
      // to worry about the fudge with the extra call for depth
      ms_log.info("Random number delay " + randdelay + "ms");
      // Total count
      ms_log.info("Total number of measured units: " + count);
      // The per element delay is the interesting figure
      ms_log.info("Per record delay " + (sw.elapsed() / count) + "ms");
   }

   /**
    * Fails a test if the time numbers are close enough.
    * @param timing the expected data.
    * @param testdata the data to compare to the expected data.
    */
   private void assertAboutEqual(double timing, double testdata)
   {
      final double DELTA = 300.0;
      // if not within +/- DELTA ms
      if (Math.abs(testdata - timing) >= DELTA)
      {
         fail(testdata + " is not closer to expected " + timing
                + " than " + DELTA + " ms");
      }
   }

}
