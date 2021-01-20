/*[ QATestResult.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;
import java.text.FieldPosition;
import java.util.Date;

/**
 * Container for one test result.
 */
class QATestResult implements Serializable, Comparable
{
   public QATestResult(String requestType, Object testDetail, Date startTime)
   {
      m_testDetail = testDetail;
      m_startTime = startTime;
      m_endTime = null;
      m_requestType = requestType;
      m_status = '?';
   }

   public int compareTo(Object o)
   {
      return duration().compareTo(((QATestResult)o).duration());
   }

   public Date duration()
   {
      if(m_endTime == null)
         return new Date(System.currentTimeMillis() - m_startTime.getTime());
      else
         return new Date(m_endTime.getTime() - m_startTime.getTime());
   }

   public Date endTime()
   {
      return m_endTime;
   }

   public char getStatus()
   {
      return m_status;
   }

   public void finishStatus(char status, Date endTime, String message)
   {
      m_status = status;
      m_message = message;
      m_endTime = endTime;
   }

   public Date startTime()
   {
      return m_startTime;
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer(256);
      buf.append(m_status);
      buf.append('\t');
      buf.append(m_requestType);
      buf.append('\t');
      if(m_startTime != null)
         QATestResults.getDateFormat().format(m_startTime, buf, new FieldPosition(0));
      else
         buf.append("???");
   
      buf.append('\t');
      if(m_endTime != null)
         QATestResults.getDateFormat().format(m_endTime, buf, new FieldPosition(0));
      else
         buf.append("???");

      if (m_startTime != null && m_endTime != null)
         buf.append("\t" + (m_endTime.getTime() - m_startTime.getTime()) + "ms");

      buf.append('\t');
      buf.append(m_testDetail.toString());
      buf.append('\t');
      if (m_message == null)
         buf.append("no message");
      else
         buf.append(m_message);

      return buf.toString();
   }

   /**
    * Sets perfomance statistics on this test.  If they have been set, then
    * performance stats will be logged with the rest of the test results.
    *
    * @param perfStats The performance statistics object, containing any
    * additional performance related information.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if perfStats is <code>null</code>.
    */
   public void setPerformanceStats(QAPerformanceStats perfStats)
   {
      if (perfStats == null)
         throw new IllegalArgumentException("perfStats may not be null");

      m_performanceStats = perfStats;
   }

   /**
    * Gets perfomance statistics for this test, if they have been set by a call
    * to {@link #setPerformanceStats}.
    *
    * @return The performance statistics object, containing any
    * additional performance related information.  If one has not been set, an
    * "empty" one is returned.  Never <code>null</code>.
    */
   public QAPerformanceStats getPerformanceStats()
   {
      return m_performanceStats;
   }

   /**
    * Get this test's type as set by the ctor.  
    *
    * @return The request type.  May be <code>null</code> or empty.
    */
   public String getRequestType()
   {
      return m_requestType;
   }

   /**
    * Get this test's detail as set by the ctor.
    *
    * @return The detail Object.  May be <code>null</code>.
    */
   public Object getDetail()
   {
      return m_testDetail;
   }

   /**
    * Get the message included with this test when {@link #finishStatus} is
    * called.
    *
    * @return The message.  May be <code>null</code> or empty.
    */
   public String getMessage()
   {
      return m_message;
   }

   public static final char STATUS_FAILED = 70;
   public static final char STATUS_ERROR = 69;
   public static final char STATUS_PASSED = 80;
   public static final char STATUS_UNKNOWN = 63;
   private char m_status;
   private Object m_testDetail;
   private String m_message;
   private String m_requestType;
   private Date m_startTime;
   private Date m_endTime;

   /**
    * Object containing any performance statistics, set by a call to
    * {@link #setPerformanceStats}.  Initialized to an "empty" instance,
    * never <code>null</code>.
    */
   private QAPerformanceStats m_performanceStats = new QAPerformanceStats();
}
