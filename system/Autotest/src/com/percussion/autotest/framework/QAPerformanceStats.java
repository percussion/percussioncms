/*[ QAPerformanceStats.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.autotest.framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class encapsulates all additional performance related statistics
 * for a test result.
 */
public class QAPerformanceStats implements Serializable
{
   /**
    * Sets the current time (in milliseconds) when the request is first
    * initiated. Typically obtained using <code>System.currentTimeMillis()
    * </code>.
    *
    * @param millis The number of milliseconds.  May not be less than zero.
    *
    * @throws IllegalArgumentException if millis is less than zero.
    */
   public void setTimeOfRequest(long millis)
   {
      if (millis < 0)
         throw new IllegalArgumentException("millis cannot be less than zero");

      m_timeOfRequest = millis;
   }

   /**
    * See {@link #setTimeOfRequest}.
    *
    * @return The number of milliseconds.  Will be <code>-1</code> if this
    * value has never been set.
    */
   public long getTimeOfRequest()
   {
      return m_timeOfRequest;
   }

   /**
    * Sets the current time (in milliseconds) when the socket returned from a
    * request becomes available.  Typically obtained using <code>
    * System.currentTimeMillis()</code>.
    *
    * @param millis The number of milliseconds.  May not be less than zero.
    *
    * @throws IllegalArgumentException if millis is less than zero.
    */
   public void setConnectTime(long millis)
   {
      if (millis < 0)
         throw new IllegalArgumentException("millis cannot be less than zero");

      m_connectTime = millis;
   }

   /**
    * See {@link #setConnectTime}.
    *
    * @return The number of milliseconds.  Will be <code>-1</code> if this
    * value has never been set.
    */
   public long getConnectTime()
   {
      return m_connectTime;
   }

   /**
    * Sets the current time (in milliseconds) when the first byte of the
    * response was received.    Typically obtained using <code>
    * System.currentTimeMillis()</code>.
    *
    * @param millis The number of milliseconds.  May not be less than zero.
    *
    * @throws IllegalArgumentException if millis is less than zero.
    */
   public void setTimeOfFirstByte(long millis)
   {
      if (millis < 0)
         throw new IllegalArgumentException("millis cannot be less than zero");

      m_timeOfFirstByte = millis;
   }

   /**
    * See {@link #setTimeOfFirstByte}.
    *
    * @return The number of milliseconds.  Will be <code>-1</code> if this
    * value has never been set.
    */
   public long getTimeOfFirstByte()
   {
      return m_timeOfFirstByte;
   }

   /**
    * Sets the current time (in milliseconds) when the last byte of the
    * response was received.  Typically obtained using <code>
    * System.currentTimeMillis()</code>.
    *
    * @param millis The number of milliseconds.  May not be less than zero.
    *
    * @throws IllegalArgumentException if millis is less than zero.
    */
   public void setTimeToLastByte(long millis)
   {
      if (millis < 0)
         throw new IllegalArgumentException("millis cannot be less than zero");

      m_timeOfLastByte = millis;
   }

   /**
    * See {@link #setTimeToLastByte}.
    *
    * @return The number of milliseconds.  Will be <code>-1</code> if this
    * value has never been set.
    */
   public long getTimeOfLastByte()
   {
      return m_timeOfLastByte;
   }

   /**
    * Sets the number of bytes received in response to a request.
    *
    * @param numBytes The number of bytes.  May not be less than zero.
    *
    * @throws IllegalArgumentException if millis is less than zero.
    */
   public void setBytesReceived(int numBytes)
   {
      if (numBytes < 0)
         throw new IllegalArgumentException("numBytes cannot be less than zero");

      m_bytesReceived = numBytes;
   }

   /**
    * See {@link #setBytesReceived}.
    *
    * @return The number of bytes.  Will be <code>-1</code> if this
    * value has never been set.
    */
   public int getBytesReceived()
   {
      return m_bytesReceived;
   }

   /**
    * Sets the number of bytes sent to make a request.
    *
    * @param numBytes The number of bytes.  May not be less than zero.
    *
    * @throws IllegalArgumentException if numBytes is less than zero.
    */
   public void setBytesSent(int numBytes)
   {
      if (numBytes < 0)
         throw new IllegalArgumentException("numBytes cannot be less than zero");

      m_bytesSent = numBytes;
   }

   /**
    * See {@link #setBytesSent}.
    *
    * @return The number of bytes.  Will be <code>-1</code> if this
    * value has never been set.
    */
   public int getBytesSent()
   {
      return m_bytesSent;
   }

   /**
    * Logs a point-in-time Cpu utilization.  This method may be called several
    * times, and each call will record an instance of Cpu utilization and the
    * current timestamp.  Running Cpu statistics may be retrieved at any time.
    *
    * @param percent The percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.
    *
    * @throws IllegalArgumentException if percent is not between
    * <code>0</code> and <code>100</code> inclusive.
    *
    * @see #getMaxCpu
    * @see #getMinCpu
    * @see #getAvgCpu
    * @see #getAllCpuTicks
    */
   public void addCpuTick(int percent)
   {
      if (percent < 0 || percent > 100)
         throw new IllegalArgumentException(
            "percent must be between 0 and 100 inclusive");

      Object[] tick = {new Date(), new Integer(percent)};
      m_cpuTicks.add(tick);

      if (m_minCpu == -1 || m_minCpu > percent)
         m_minCpu = percent;

      if (m_maxCpu == -1 || m_maxCpu < percent)
         m_maxCpu = percent;

      m_avgCpu = -1;
   }

   /**
    * Returns an array of all recorded CpuTicks.
    *
    * @return A two dimensional array, where each element is an Object array
    * consisting of two entries: the timestamp as a Date and the Cpu utilization
    * as an Integer object, in that order.  The order of the array of ticks is
    * the order in which they were set by {@link #addCpuTick addCpuTick}, that
    * is, sorted asending by timestamp.  The array will never be <code>null
    * </code>, but could be empty.
    */
   public Object[][] getAllCpuTicks()
   {
      Object[][] ticks = new Object[m_cpuTicks.size()][2];
      m_cpuTicks.toArray(ticks);

      return ticks;
   }

   /**
    * Returns the current maximum Cpu utilization that has been recorded by a
    * call to {@link #addCpuTick}.
    *
    * @return The maximum percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.  Will return <code>
    * -1</code> if no utilization has been recorded.
    */
   public int getMaxCpu()
   {
      return m_maxCpu;
   }

   /**
    * Returns the current minimum Cpu utilization that has been recorded by a
    * call to {@link #addCpuTick}.
    *
    * @return The minimum percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.  Will return <code>
    * -1</code> if no utilization has been recorded.
    */
   public int getMinCpu()
   {
      return m_minCpu;
   }

   /**
    * Returns the current average Cpu utilization that has been recorded by one
    * or more calls to {@link #addCpuTick}.
    *
    * @return The average percentage of Cpu utilization expressed as a number
    * between <code>0</code> and <code>100</code> inclusive.  Will return <code>
    * -1</code> if no utilization has been recorded.
    */
   public int getAvgCpu()
   {
      // see if we've calculated it
      if (m_avgCpu != -1)
         return m_avgCpu;

      int count = m_cpuTicks.size();
      if (count > 0)
      {
         int total = 0;
         Iterator ticks = m_cpuTicks.iterator();
         while (ticks.hasNext())
         {
            Object[] tick = (Object[])ticks.next();
            total += ((Integer)tick[1]).intValue();
         }

         // calculate the mean average, rounding to the nearest integer
         m_avgCpu = Math.round(total / (float)count);

      }
      return m_avgCpu;
   }

   /**
    * List of cpu ticks.  Each element is a two dimensional object array
    * consisting of two entries: the timestamp as a Date and the Cpu utilization
    * as an Integer object, in that order.  Never <code>null</code>, may be
    * empty.
    */
   private List m_cpuTicks = new ArrayList();

   /**
    * Cache of average CPU utilization, initialized to -1, modified by a call to
    * {@link #getAvgCpu()}.
    */
   private int m_avgCpu = -1;

   /**
    * Minimum CPU utilization, initialized to -1, modified by a call to
    * {@link #addCpuTick(int)}.
    */
   private int m_minCpu = -1;

   /**
    * Maximum CPU utilization, initialized to -1, modified by a call to
    * {@link #addCpuTick(int)}.
    */
   private int m_maxCpu = -1;

   /**
    * Number of bytes received, initialized to -1, modified by a call to
    * {@link #setBytesReceived(int)}.
    */
   private int m_bytesReceived = -1;

   /**
    * Number of bytes sent, initialized to -1, modified by a call to
    * {@link #setBytesSent(int)}.
    */
   private int m_bytesSent = -1;

   /**
    * The time at which the request was made, expressed in milliseconds since
    * midnight, January 1, 1970 UTC, initialized to -1, modified by a call to
    * {@link #setTimeOfRequest(long)}.
    */
   private long m_timeOfRequest = -1;

   /**
    * The time expressed in milliseconds when the socket resulting from a
    * request is available for use. Initialized to -1, modified by a call to
    * {@link #setConnectTime(long)}.
    */
   private long m_connectTime = -1;

   /**
    * The time expressed in milliseconds when the first byte is received in
    * reponse to a request. Initialized to -1, modified by a call to {@link
    * #setTimeOfFirstByte(long)}.
    */
   private long m_timeOfFirstByte = -1;

   /**
    * The time expressed in milliseconds when the last byte is received in
    * reponse to a request. Initialized to -1, modified by a call to {@link
    * #setTimeToLastByte(long)}.
    */
   private long m_timeOfLastByte = -1;

}
