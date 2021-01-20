/*[ QATestResults.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.*;
import java.text.*;
import java.util.*;

public class QATestResults implements Serializable
{
   public QATestResults(String scriptName, String clientName, 
      boolean loggingEnabled)
   {
      m_tests = new ArrayList();
      m_testsStarted = new Date();
      m_scriptName = scriptName;
      m_clientName = clientName;
      m_numFailures = 0;
      m_numPasses = 0;
      m_numErrors = 0;
        m_numSkips = 0;
      m_cacheValid = true;
      m_averageCaseTime = 0.0D;
      m_medianCaseTime = 0.0D;
      m_fastestCaseTime = 0.0D;
      m_slowestCaseTime = 0.0D;
      m_log = new Vector();
      m_loggingEnabled = loggingEnabled;
   }

   public double averageCaseTime()
   {
      computeStatistics();
      return m_averageCaseTime;
   }

   private void computeStatistics()
   {
      if (isCacheValid())
         return;

      int numTests = m_tests.size();
      if (numTests > 0)
      {
         long totalCaseTime = 0L;
            int testsToCount = 0;
         for(int i = 0; i < numTests; i++)
            {
               QATestResult tr = (QATestResult)m_tests.get(i);
               if (tr.getStatus() != 'S')
               {
               totalCaseTime += tr.duration().getTime();
                  testsToCount++;
               }
            }

            if (testsToCount > 0)
            m_averageCaseTime = totalCaseTime / (long)testsToCount;

         QATestResult results[] = new QATestResult[numTests];
         results = (QATestResult[])m_tests.toArray(results);
         Arrays.sort(results);
            int firstRealTestIdx = numTests - testsToCount;
         m_medianCaseTime = results[firstRealTestIdx +
            Math.round(numTests / 2)].duration().getTime();
         if((numTests & 0x1) == 0)
            m_medianCaseTime = (m_medianCaseTime + (double)results[
               firstRealTestIdx + (numTests / 2)].duration().getTime()) / 2D;
         m_fastestCaseTime = results[firstRealTestIdx].duration().getTime();
         m_slowestCaseTime = results[numTests - 1].duration().getTime();
         validateCache();
      }
   }

   /**
    * Create a new test for the supplied request type and test detail.
    *
    * @param requestType the request type (GET or POST), assumed not 
    *    <code>null</code>.
    * @param testDetail the test details, assumed not <code>null</code>.
    * @return the number of tests currently created.
    */
   public synchronized int createTest(String requestType, Object testDetail)
   {
      QATestResult test = new QATestResult(requestType, testDetail, new Date());
      m_tests.add(test);
      invalidateCache();

      // if currently executing a page, add it to the page as well
      if (m_pages.size() > 0)
      {
         // check last page in list and see if it is executing
         QATestPageResults page = (QATestPageResults)m_pages.get(
            m_pages.size() - 1);
         if (page.isPageExecuting())
            page.addTestResult(test);
      }

      return m_tests.size() - 1;
   }

   public void passTest(int testID, QAPerformanceStats stats )
   {
      passTest(testID, "", stats);
   }

   public synchronized void passTest(int testID, String message,
      QAPerformanceStats stats )
   {
      QATestResult tr = (QATestResult)m_tests.get(testID);
      tr.finishStatus('P', new Date(), message);
      tr.setPerformanceStats(stats);
      m_numPasses++;
      invalidateCache();
   }

    public synchronized void skipTest(int testID, String message)
    {
      QATestResult tr = (QATestResult)m_tests.get(testID);
      tr.finishStatus('S', new Date(), message);
      m_numSkips++;
      invalidateCache();
    }

   public synchronized void errorTest(int testID, String error)
   {
      QATestResult tr = (QATestResult)m_tests.get(testID);
      tr.finishStatus('E', new Date(), error);
      m_numErrors++;
      invalidateCache();
   }

   public synchronized void failTest(int testID, String reason,
      QAPerformanceStats stats)
   {
      QATestResult tr = (QATestResult)m_tests.get(testID);
      tr.finishStatus('F', new Date(), reason);
      if (stats != null)
         tr.setPerformanceStats(stats);
      m_numFailures++;
      invalidateCache();
   }

   public double fastestCaseTime()
   {
      computeStatistics();
      return m_fastestCaseTime;
   }

   public String getClientName()
   {
      return m_clientName;
   }

   public int getErrorsCount()
   {
      return m_numErrors;
   }

   public int getFailuresCount()
   {
      return m_numFailures;
   }

   public int getSkipsCount()
   {
      return m_numSkips;
   }

   public int getPassesCount()
   {
      return m_numPasses;
   }

   public String getScriptName()
   {
      return m_scriptName;
   }

   public int getTestsCount()
   {
      return m_tests.size();
   }

   private void invalidateCache()
   {
      m_cacheValid = false;
   }

   private boolean isCacheValid()
   {
      return m_cacheValid;
   }

   public boolean isLoggingEnabled()
   {
      return m_loggingEnabled;
   }

   /**
    * Writes a message to the log, if logging is currently enabled.
    *
    * @param message The message to log.  May be <code>null</code> or empty.  If
    * <code>null</code>, then the message is not added to the log.  
    */
   public void log(String message)
   {
      if(m_loggingEnabled && message != null)
         m_log.add(message);
   }

   public double medianCaseTime()
   {
      computeStatistics();
      return m_medianCaseTime;
   }

   public double slowestCaseTime()
   {
      computeStatistics();
      return m_slowestCaseTime;
   }
   
   public List getTests()
   {
      return m_tests;
   }
   
   public List getLogs()
   {
      return m_log;
   }
   
   public Date getStartDate()
   {
      return m_testsStarted;
   }
   
   public static SimpleDateFormat getDateFormat()
   {
      return ms_dateFormat;
   }

   public String toString()
   {
      StringWriter s = new StringWriter();
      try
      {
         write(s);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      s.flush();
      return s.toString();
   }

   private void validateCache()
   {
      m_cacheValid = true;
   }

   /**
    * Writes any statements in the log to the specified writer.  Does not flush
    * or close the writer.
    *
    * @param out The Writer to use, may not be <code>null</code>.  
    *
    * @throws IllegalArgumentException if out is <code>null</code>.
    */
   public void writeLog(Writer out) throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out may not be null");

      for(int i = 0; i < m_log.size(); i++)
      {
         Object logItem = m_log.get(i);
         if (logItem != null)
            out.write(logItem.toString() + "\r\n");
      }
   }

   public void write(IQAWriter out) throws Exception
   {
      out.write(this);
   }

   public void write(Writer out)
      throws IOException
   {
      if ( !(out instanceof PrintWriter ))
         out = new PrintWriter( out );
      out.write("Client: ");
      out.write(m_clientName);
      out.write("\r\nScript: ");
      out.write(m_scriptName);
      out.write("\r\nStarted: ");
      out.write(ms_dateFormat.format(m_testsStarted));
      out.write("\r\nAverage case time: " + averageCaseTime());
      out.write("\r\nMedian case time: " + medianCaseTime());
      out.write("\r\nFastest case time: " + fastestCaseTime());
      out.write("\r\nSlowest case time: " + slowestCaseTime());
      out.write("\r\nTests: " + m_tests.size());
      out.write("\r\nFailures: " + m_numFailures);
      out.write("\r\nErrors: " + m_numErrors);
      out.write("\r\nSkips: " + m_numSkips);
      out.write("\r\nPasses: " + m_numPasses);
      if ( null != m_cpuUsage )
      {
         StringBuffer buf = new StringBuffer();
         buf.append( "\r\nCpu(" );
         buf.append( m_cpuUsage.getAllCpuTicks().length );
         buf.append( " samples): avg=" );
         buf.append( m_cpuUsage.getAvgCpu());
         buf.append( ", min=" );
         buf.append( m_cpuUsage.getMinCpu());
         buf.append( ", max=" );
         buf.append( m_cpuUsage.getMaxCpu());

         buf.append( " (" );
         Object[][] o = m_cpuUsage.getAllCpuTicks();
         for ( int i = 0; i < o.length; i++ )
            buf.append( o[i][1] + "," );
         buf.append( ")" );

         out.write( buf.toString());
      }

      out.write("\r\n[[BEGIN LOG]]");
      for(int i = 0; i < m_tests.size(); i++)
      {
         QATestResult t = (QATestResult) m_tests.get(i);
         out.write("\r\n");
         out.write(t.toString());
      }

      if (m_loggingEnabled)
      {
         out.write("\r\n\r\nLog:\r\n");
         writeLog(out);
      }
      
      out.flush();
   }

   /**
    * Gets cpu usage statistics for this test, if they have been set by a call
    * to {@link #setCpuUsage}.
    *
    * @return The performance statistics object, containing cpu usage details.
    *    If one has not been set, <code>null</code> is returned.
    */
   public QAPerformanceStats getCpuUsage()
   {
      return m_cpuUsage;
   }

   /**
    * Sets cpu usage statistics for this test.  If they have been set, then
    * they will be logged with the rest of the test results.
    *
    * @param perfStats The performance statistics object. Only the cpu usage
    *    information will be used. If <code>null</code>, the current stats
    *    are removed.
    */
   public void setCpuUsage(QAPerformanceStats perfStats)
   {
      m_cpuUsage = perfStats;
   }

   /**
    * Logs the beginning of a PageExecBlock.  Any subsequent calls to
    * {@link #createTest} will associate the test as part of this page block,
    * until a call to {@link #endPage} is made.
    *
    * @param pageName The name of the page, may not be <code>null</code> or
    * empty.
    * @param threadPoolSize The size of the thread pool used to process
    *
    * @return pageId The pageId to use when calling {@link #endPage}.
    *
    * @throws IllegalArgumentException if pageName is <code>null</code> or
    * empty.
    */
   public int startPage(String pageName, int threadPoolSize)
   {
      if (pageName == null || pageName.trim().length() == 0)
         throw new IllegalArgumentException(
            "pageName may not be null or empty");

      QATestPageResults page = new QATestPageResults(pageName, threadPoolSize);
      m_pages.add(page);

      // return index of page we just added
      return m_pages.size() - 1;
   }

   /**
    * Logs the closing of a PageExecBlock.  Any subsequent calls to {@link
    * #createTest} will no long associate the test as part of this page block.
    *
    * @param pageId The id of the page, must refer to a pageid returned from a
    * call to {@link #startPage}.
    *
    * @throws IllegalArgumentException if startPage is <code>null</code> or
    * empty, or if a call to <code>startPage</code> has not already been made
    * for this page.
    */
   public void endPage(int pageId)
   {
      QATestPageResults page = (QATestPageResults)m_pages.get(pageId);
      if (page == null)
         throw new IllegalArgumentException("invalid pageId");

      page.endPage();
   }

   /**
    * Returns the list of test page results.
    *
    * @return The List, never <code>null</code>, may be empty.
    */
   public List getPages()
   {
      return m_pages;
   }

   /**
    * List of pages, never <code>null</code>, may be emtpy.  Pages are added
    * by calls to {@link #startPage}.
    */
   private List m_pages = new ArrayList();

   /**
    * If the test is tracking client cpu usage, this member will contain a
    * valid stats object. Only the cpu portion of the object is used.
    * If it is valid, the min, max and avg values are written in the log.
    */
   private QAPerformanceStats m_cpuUsage;

   private List m_tests;
   private Date m_testsStarted;
   private static SimpleDateFormat ms_dateFormat =
      new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
   private String m_scriptName;
   private String m_clientName;
   private int m_numFailures;
   private int m_numErrors;
   private int m_numPasses;
    private int m_numSkips;
   private boolean m_cacheValid;
   private double m_averageCaseTime;
   private double m_medianCaseTime;
   private double m_fastestCaseTime;
   private double m_slowestCaseTime;
   private List m_log;
   private boolean m_loggingEnabled;
}
