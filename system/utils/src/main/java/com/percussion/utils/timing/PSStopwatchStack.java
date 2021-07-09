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
package com.percussion.utils.timing;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Stopwatch handler that creates stopwatches on a thread-based stack. This
 * allows handlers to push and pop the timers on the stack. As timers are pushed
 * onto the stack, they are paused. As they are popped to the top of the stack
 * they are restarted.
 * 
 * @author dougrand
 */
public class PSStopwatchStack
{
   private static final Logger ms_logger = LogManager.getLogger(PSStopwatchStack.class);

   /**
    * Holds a reference to the stopwatch stack for the current thread.
    * Initialized in the ctor.
    */
   private Stack<PSStopwatch> m_stack = new Stack<PSStopwatch>();

   /**
    * The name of the current timer category, used in doing statistics
    */
   private Stack<String> m_category = new Stack<String>();

   /**
    * A map containing string keys of the measurement categories, and
    * <code>Double</code> values that are the accumulated time for each
    * category.
    */
   private Map<String,Double> m_statistics = new HashMap<String,Double>();
   
   /**
    * A map containing string keys of the measurement categories, and
    * a <code>Integer</code> value that is the number of times the category
    * has been run.
    */
   private Map<String,Integer> m_counters = new HashMap<String,Integer>();

   /**
    * Holds a reference to the current sw stack instance for the given thread.
    * The pattern is basically a Singleton pattern.
    */
   static ThreadLocal<PSStopwatchStack> ms_swRef = new ThreadLocal<PSStopwatchStack>();

   /**
    * Private ctor, users should call the factory instead to get the current
    * thread based instance.
    */
   PSStopwatchStack() {
      super();
      m_stack = new Stack<PSStopwatch>();
   }

   /**
    * Factory method to get the stopwatch stack. Threadsafe through the use of
    * ThreadLocal.
    * 
    * @return the current thread's instance of the stopwatch stack
    */
   public static PSStopwatchStack getStack()
   {
      PSStopwatchStack rval = ms_swRef.get();

      if (rval == null)
      {
         rval = new PSStopwatchStack();
         ms_swRef.set(rval);
      }
      return rval;
   }

   /**
    * Start the stopwatch. If there's a current stopwatch being measured, then
    * pause it.
    * 
    * @param category category used for recording statistics, must not be
    *           <code>null</code> or empty
    * 
    */
   public void start(String category)
   {
      start(category, new PSStopwatch());
   }

   /**
    * Start a stopwatch and add to the stack. If there's a current stopwatch
    * being measured, then pause it.
    * 
    * @param category the category, never <code>null</code> or empty
    * @param watch the stopwatch, never <code>null</code>
    */
   public void start(String category, PSStopwatch watch)
   {
      if (watch == null)
      {
         throw new IllegalArgumentException("watch may not be null");
      }
      if (category == null || category.trim().length() == 0)
      {
         throw new IllegalArgumentException("category may not be null or empty");
      }
      if (m_stack.size() > 0)
      {
         PSStopwatch w = (PSStopwatch) m_stack.peek();
         w.pause();
      }
      m_stack.push(watch);
      m_category.push(category);
      if (watch.isStopped())
         watch.start();
      // ms_logger.debug("Start: " + category);
   }

   /**
    * Peek at the current stopwatch
    * 
    * @return the current top stopwatch or <code>null</code> if the stack is
    *         empty
    */
   public PSStopwatch current()
   {
      if (m_stack.size() > 0)
      {
         return m_stack.peek();
      }
      else
      {
         return null;
      }
   }

   /**
    * Stop the top stopwatch and return it. If there are more stopwatches on the
    * stack then continue the next stopwatch.
    * 
    * @return the top stopwatch, never <code>null</code>. Throws an exception
    *         if the stack is exhausted.
    */
   public PSStopwatch stop()
   {
      if (m_stack.size() < 1)
      {
         throw new IllegalStateException("Stopwatch stack is exhausted");
      }
      PSStopwatch w = m_stack.pop();
      w.stop();
      double total = 0;
      String cat = (String) m_category.pop();
      Double cur = (Double) m_statistics.get(cat);
      if (cur != null)
         total = cur.doubleValue();
      total += w.elapsed();
      m_statistics.put(cat, new Double(total));
      Integer counter = m_counters.get(cat);
      if (counter == null)
      {
         counter = 1;
      }
      else
      {
         counter++;
      }
      m_counters.put(cat,counter);
      // ms_logger.debug("Stop: " + cat);
      if (m_stack.size() > 0)
      {
         PSStopwatch top = m_stack.peek();
         top.cont();
      }

      return w;
   }

   /**
    * Print out known statistics at this point
    * 
    * @return the statistics, formatted as a series of category=value pairs
    */
   public String getStats()
   {
      DecimalFormat dec = new DecimalFormat("###,###,###.##");
      SortedSet<String> sortedKeys = new TreeSet<String>(m_statistics.keySet());
      StringBuffer buf = new StringBuffer(80);
      Iterator<String> kiter = sortedKeys.iterator();
      while (kiter.hasNext())
      {
         String key = kiter.next();
         Double time = (Double) m_statistics.get(key);
         buf.append("  ");
         buf.append(key);
         buf.append("=");
         buf.append(dec.format(time));
         buf.append("/");
         buf.append(m_counters.get(key));
         if (kiter.hasNext())
            buf.append("\n");
      }
      return buf.toString();
   }

   /**
    * Check the stack for completeness and print the statistics. If there are
    * lingering stopwatches on the stack, print warnings and stop them.
    */
   public void finish()
   {
      while (m_stack.size() > 0)
      {
         ms_logger.warn("Stopwatch left open: " + m_category.peek());
         stop(); // Stops the watch, compiles statistics and pops the stacks
      }

      ms_logger.debug("Statistics on thread "
            + Thread.currentThread().getName() + "\n" + getStats());

      // Clear the statistics
      m_statistics = new HashMap<String,Double>();
      m_counters = new HashMap<String,Integer>();
   }
   

   // this method is the around advice
   public Object profile(ProceedingJoinPoint call) throws Throwable
   {
      Object returnValue;
      PSStopwatchStack threadinstance = getStack();
      try
      {
         threadinstance.start(call.getTarget().getClass().getName() + ":"
               + call.toShortString());
         returnValue = call.proceed();
      }
      finally
      {
         threadinstance.stop();
         if (threadinstance.m_stack.size() == 0)
         {
            ms_logger.debug("Timing results: " + getStats());
            threadinstance.finish();
         }
      }
      return returnValue;
   }   
}
