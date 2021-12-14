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
package com.percussion.log;

import com.percussion.util.PSDoubleList;

/**
 *    A PSLogQueueThread will periodically poll for new messages added to
 *    its queue and cause them to be written directly via the PSLogManager.
 *    See the constructor for how to tune the flush period and the flush
 *    limit.
 */
class PSLogQueueThread extends java.lang.Thread
{
   /**
    * Constructs a new PSLogQueueThread. Call start() to start it.
    * @param logQueue The log queue to process. The queue will notify
    *   when it has received a message.
    * @param msecBetweenFlushing Flushing how many milliseconds between periodic
    *   queue flushing. Set to 0 to disable periodic queue flushing.
    * @param flushLimit Flush immediately as soon as this many messages
    *   build up in the queue.
    */
   PSLogQueueThread(PSDoubleList logQueue, long msecBetweenFlushing,
      int flushLimit)
   {
      super();
      setDaemon(true);
      if (logQueue == null)
         throw new IllegalArgumentException("logQueue == null");
      if (msecBetweenFlushing < 0)
         throw new IllegalArgumentException("msecBetweenFlushing < 0");
      if (flushLimit < 0)
         flushLimit = 0;
      m_queue = logQueue;
      m_msecBetween = msecBetweenFlushing;
      m_flushLimit = flushLimit;
   }

   /**
    *    Start the queue thread. In a loop, it does the following:
    *    <OL>
    *    <LI>Wait for someone to add a message to the queue or until X
    *    milliseconds have elapsed, whichever comes first
    *    <LI>If it was the case that X millseconds elapsed, then flush
    *    the queue.
    *    <LI>Otherwise, if someone had added a message to the queue, then
    *    if the number of messages in the queue exceeds a critical limit,
    *    then flush the queue and reduce the next wait time by the amount
    *    of time we waited in the first step.
    *    </OL>
    */
   public void run()
   {
      long truncateTimeAt = 0;
      long truncatePeriod = 0;
      long sleepTimeMS = 0;
      long waitFor = m_msecBetween;
      long waitStart = 0;
      int  queueSize = 0;

      conOut("Started log queue thread: " +
         Thread.currentThread().toString());

      synchronized(this)
      {
         notifyAll();
      }

      while (!m_shutdown)
      {
         waitStart = System.currentTimeMillis();
         if (m_truncateDays > 0)
         {
            /* when performing log truncation, we wake up once a day
             * to delete the previous m_truncateDays worth of log.
             * Since we do this once a day, it should really only
             * clear one days worth of log each time it is run.
             */
            truncatePeriod = waitStart - truncateTimeAt;
            if (truncatePeriod >= ms_millisecInOneDay)
            {
               truncateLog(m_truncateDays);
               truncateTimeAt = System.currentTimeMillis();
               truncatePeriod = 0;
            }
         }

         synchronized (m_queue)
         {
            try
            {
               if (m_queue.size() == 0)
               {
                  // if we aren't truncating the log, we can wait forever
                  if (m_truncateDays > 0)
                     m_queue.wait(ms_millisecInOneDay - truncatePeriod);
                  else
                     m_queue.wait();
               }
               else
               {
                  /* we want to sleep the minimum of the amount of time
                   * until the next truncation (which is 1 day if truncation
                   * is disabled) and the log flush interval
                   * (which defaults to 1 minute).
                   */
                  sleepTimeMS = ms_millisecInOneDay - truncatePeriod;
                  m_queue.wait(Math.min(m_msecBetween, sleepTimeMS));
               }
            }
            catch (InterruptedException e)
            {
               conOut("Interrupted. Shutting down.");
               Thread.currentThread().interrupt();
               break;
            }

            queueSize = m_queue.size();
         }

         waitFor -= System.currentTimeMillis() - waitStart;
         if (waitFor <= 0)
         {
            // time expired. flush the queue even if there are fewer than
            // m_flushLimit messages in it
            flushQueue();
            waitFor = m_msecBetween;
         }
         else if (queueSize >= m_flushLimit)
            // if we've reached our limit of waiting messages, then
            // flush the queue
         {
            flushQueue();
         }
      }
   }

   /**
    *   Writes all pending messages in the queue via the PSLogManager's
    * {@link PSLogManager#writeThrough(PSLogInformation) writeThrough} method.
    */
   public void flushQueue()
   {
      Object[] msgs = null;
      synchronized (m_queue)
      {
         msgs = m_queue.toArray();
         m_queue.clear();
      
         if (msgs != null)
         {
            final int numMsgs = msgs.length;
            m_flushRemaining += numMsgs;

            if (numMsgs >= ms_consoleThreshold)
               conOut("Flushing " + numMsgs + " log messages...");

            for (int i = 0; i < numMsgs; i++)
            {
               PSLogManager.writeThrough((PSLogInformation) msgs[i]);
               m_flushRemaining--;
            }

            if (numMsgs >= ms_consoleThreshold)
               conOut("Finished flushing messages");
         }
      } // TODO: only synchronize the queue access when the thread pool is in place.
   }

   /**
    * Percussion standard console output.
    * 
    * @param   msg The message to output.
    * @param   subMessages Any submessages to be output along with
    *          message.
    * 
    */
   public static void conOut(String msg, String[] subMessages)
   {
      com.percussion.server.PSConsole.printMsg(
         "LogQueueThread", msg, subMessages);
   }

   /**
    * Percussion standard console output.
    * @param msg The string to be output.
    */
   public static void conOut(String msg)
   { conOut(msg, null); }

   /**
    * Set the amount of days for the log to be truncated.
    *
    * @param days The number of days back to start clearing
    *             the logfile.
    */
   public void setRunningLogDays(int days)
   {
      if (days < 0)
         days = 0;

      m_truncateDays = days;

      synchronized (m_queue)
      {
         m_queue.notifyAll();
      }
   }
   
   /**
    * Returns the remaining number of messages which need to be flushed by this 
    * thread.
    *
    * @return the remaining number of messages this thread still has to flush.
    */
   public int getFlushRemaining()
   {
      return m_flushRemaining;
   }

   /**
    *   Truncate the log which has been existing for a certain days. This is done
    * via the PSLogManager's @link PSLogManager#truncateLog truncateLog method.
    *
    * @param days The amount of past days.
    */
   private void truncateLog(int days)
   {
      PSLogManager.truncateLog(days);
   }
   
   /**
    * Gracefully notifies this thread that it whould shutdown
    */
   public void shutdown()
   {
      conOut("Shutting down");
      m_shutdown = true;
      
      synchronized (m_queue)
      {
         m_queue.notifyAll();
      } 
   }   
   
   /**
    * Flag to indicate if the queue should shutdown
    */
   private boolean m_shutdown = false;
   
   /** The log queue to process. */
   private com.percussion.util.PSDoubleList m_queue;

   /** How many milliseconds between periodic flushing. */
   private long m_msecBetween = -1;

   /** Max number of queued messages before a flush. */
   private int m_flushLimit = -1;

   /** The number of days the messages has been in the log. */
   private int m_truncateDays = 0;

   /** The number of milliseconds in one single day. */
   private static final long ms_millisecInOneDay = 86400*1000;

   /**
    * The least number of messages to be written at once that will
    * cause a console message to be printed.
    */
   private static final int ms_consoleThreshold = 20;
   
   /** The total messages which need to be flushed */
   private int m_flushRemaining = 0;
}
