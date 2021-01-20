/*[ QATestPageResults.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.autotest.framework;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Class to record page level results, and to associate test results with a
 * particular page.
 */
public class QATestPageResults implements Serializable
{
   /**
    * Creates a page results object.
    *
    * @param pageName The name of the page.  May not be <code>null</code> or
    * empty.
    * @param threadPoolSize The number of threads used to execute any requests
    * following the first request in a page.
    *
    * @throws IllegalArgumentException if pageName is <code>null</code> or
    * empty.
    */
   public QATestPageResults(String pageName, int threadPoolSize)
   {
      if (pageName == null || pageName.trim().length() == 0)
         throw new IllegalArgumentException(
            "pageName may not be null or empty");

      m_pageName = pageName;
      m_threadPoolSize = threadPoolSize;
   }

   /**
    * Causes this page to end.  No more tests may be added to this page.
    *
    * @throws IllegalStateException if this method has already been called.
    */
   public void endPage()
   {
      if (!m_isPageExecuting)
         throw new IllegalStateException("page is not currently executing");

      m_isPageExecuting = false;
   }

   /**
    * Gets the start time of the first test on this page.
    *
    * @return The time in milliseconds.  Will be <code>-1</code> if no tests
    * have been added.
    */
   public long getStartTime()
   {
      long time = -1;

      if (m_tests.size() > 0)
      {
         QATestResult test = (QATestResult)m_tests.get(0);
         time = test.getPerformanceStats().getTimeOfRequest();
      }

      return time;
   }

   /**
    * Gets the time elapsed (in milliseconds) from when the first request of
    * this page was initiated, until the the first byte of the response was
    * received.
    *
    * @return The time in milliseconds.  Will be <code>-1</code> if no tests
    * have been added.
    */
   public long getTimeToFirstByte()
   {
      long time = -1;

      if (m_tests.size() > 0)
      {
         QATestResult test = (QATestResult)m_tests.get(0);
         time = test.getPerformanceStats().getTimeOfFirstByte() -
            test.getPerformanceStats().getTimeOfRequest();
      }

      return time;
   }

   /**
    * Gets the total time elapsed (in milliseconds) to process each request.
    * This is the difference between the earliest test request time and the
    * latest test TTLB.
    *
    * @return The time in milliseconds.  Will be <code>-1</code> if no tests
    * have been added or if no tests have had timings recorded.
    */
   public long getTimeToLastByte()
   {
      long time = -1;

      long start = -1;
      long last = -1;

      if (m_tests.size() > 0)
      {
         Iterator tests = m_tests.iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            long tLast = test.getPerformanceStats().getTimeOfLastByte();
            long tStart = test.getPerformanceStats().getTimeOfRequest();
            if (tLast != -1 && tLast > last)
               last = tLast;
            if (tStart != -1 && (tStart < start || start == -1))
               start = tStart;
         }
         if (start != -1 && last != -1)
            time = last - start;
      }

      return time;
   }

   /**
    * Gets total number of bytes sent for this page.
    *
    * @return The total number of bytes.
    */
   public int getBytesSent()
   {
      if (m_bytesSent == -1)
      {
         m_bytesSent = 0;

         Iterator tests = m_tests.iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            int bytes = test.getPerformanceStats().getBytesSent();
            if (bytes > -1)
               m_bytesSent += bytes;
         }
      }

      return m_bytesSent;
   }

   /**
    * Gets total number of bytes received for this page.
    *
    * @return The total number of bytes.
    */
   public int getBytesReceived()
   {
      if (m_bytesReceived == -1)
      {
         m_bytesReceived = 0;

         Iterator tests = m_tests.iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            int bytes = test.getPerformanceStats().getBytesReceived();
            if (bytes > -1)
               m_bytesReceived += bytes;
         }
      }
      
      return m_bytesReceived;
   }

   /**
    * Returns the current maximum Cpu utilization recorded by any test.
    *
    * @return The maximum percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.  Will return <code>
    * -1</code> if no utilization has been recorded.
    */
   public int getMaxCpu()
   {
      if (m_maxCpu == -1)
      {
         Iterator tests = m_tests.iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            int max = test.getPerformanceStats().getMaxCpu();
            if (max > m_maxCpu)
               m_maxCpu = max;
         }
      }   

      return m_maxCpu;
   }

   /**
    * Returns the current minimum Cpu utilization recorded by any test.
    *
    * @return The minimum percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.  Will return <code>
    * -1</code> if no utilization has been recorded.
    */
   public int getMinCpu()
   {
      if (m_minCpu == -1)
      {
         Iterator tests = m_tests.iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            int min = test.getPerformanceStats().getMinCpu();
            if (min < m_minCpu && min != -1)
               m_minCpu = min;
         }
      }
      
      return m_minCpu;
   }

   /**
    * Returns an average of the average Cpu utilization of all of the tests.
    *
    * @return The average percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.  Will return <code>
    * -1</code> if no test have been added or no tests have had utilization
    * recorded.
    */
   public int getAvgCpu()
   {
      // see if we've calculated it
      if (m_avgCpu != -1)
         return m_avgCpu;

      if (m_tests.size() > 0)
      {
         int count = 0;
         int total = 0;

         Iterator tests = m_tests.iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            int avg = test.getPerformanceStats().getAvgCpu();
            if (avg > -1)
            {
               total += avg;
               count++;
            }
         }

         // calculate the mean average, rounding to the nearest integer
         if (count > 0)
            m_avgCpu = Math.round(total / (float)count);

      }
      return m_avgCpu;
      
   }


   /**
    * Adds a test result to this page.
    *
    * @param result The test result, may not be <code>null</code>.
    *
    * @throws IllegalStateException if {@link #endPage() has been called}.
    * @throws IllegalArgumentException if result if <code>null</code>.
    */
   public void addTestResult(QATestResult result)
   {
      if (result == null)
         throw new IllegalArgumentException("result may not be null");

      if (!m_isPageExecuting)
         throw new IllegalStateException(
            "Cannot add tests if page is not executing");

      m_tests.add(result);
   }

   /**
    * Determines if page is still executing, or if {@link #endPage()} has been
    * called.
    *
    * @return <code>true</code> if page is still executing, <code>false</code>
    * if it is not.
    */
   public boolean isPageExecuting()
   {
      return m_isPageExecuting;
   }

   /**
    * Gets this page's name.
    *
    * @return The page name, never <code>null</code> or empty.
    */
   public String getPageName()
   {
      return m_pageName;
   }

   /**
    * Gets the thread pool size of this page.  This is the number of threads
    * used to execute any requests following the first request in a page.
    *
    * @return The number of threads.
    */
   public int getThreadPoolSize()
   {
      return m_threadPoolSize;
   }

   /**
    * Get the List of test result objects.  The returned list is immutable.
    *
    * @return The List of tests, never <code>null</code>, may be empty.
    */
   public List getTests()
   {
      return Collections.unmodifiableList(m_tests);
   }

   /**
    * The name of this page.  Initialized by the ctor, never <code>null</code>,
    * emtpy, or modified after that.
    */
   private String m_pageName = null;

   /**
    * The number of threads used to execute any requests following the first
    * request in this page.  Initialized in the ctor, then never modified.
    */
   private int m_threadPoolSize;

   /**
    * Determines if the page is currently executing.  Initialized to
    * <code>true</code>, is set to <code>false</code> by a call to
    * {@link #endPage()}.
    */
   private boolean m_isPageExecuting = true;

   /**
    * List of test result objects added by calls to {@link
    * #addTestResult(QATestResult) addTestResult}.  Never <code>null</code>,
    * may be empty.
    */
   private List m_tests = new ArrayList();

   /**
    * Cache of average CPU utilization, initialized to -1, modified by a call to
    * {@link #getAvgCpu()}.
    */
   private int m_avgCpu = -1;

   /**
    * Minimum CPU utilization, initialized to -1, modified by a call to
    * {@link #getMinCpu()}.
    */
   private int m_minCpu = -1;

   /**
    * Maximum CPU utilization, initialized to -1, modified by a call to
    * {@link #getMaxCpu()}.
    */
   private int m_maxCpu = -1;

   /**
    * Number of bytes received, initialized to -1, modified by a call to
    * {@link #getBytesReceived()}.
    */
   private int m_bytesReceived = -1;

   /**
    * Number of bytes sent, initialized to -1, modified by a call to
    * {@link #getBytesSent()}.
    */
   private int m_bytesSent = -1;


}

