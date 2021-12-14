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
import java.text.NumberFormat;

/**
 * <P>A simple stopwatch class that simplifies timing things during testing. The
 * class starts in a stopped state, and needs to have the various methods called
 * to change the state. It is illegal to stop the stopwatch if it hasn't been
 * started.
 * 
 * <P>A stopwatch can be used multiple times as long as the state transitions are
 * correct.
 * 
 * <P>Note that on Windows, this class makes use of the hrtlib.dll. See the
 * article at http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html#resources
 * for a good discussion.
 */
public class PSStopwatch
{
   /*
    * Enumerated state values
    */
   private static final int RUN_STATE = 0;
   private static final int STOP_STATE = 1;
   private static final int PAUSED_STATE = 2;
   
   private static final String STATES[] = {
      "running", "stopped", "paused"
   };
   


   /**
    * Create a new stopwatch object. Note that the object must be started
    * in order to record time.
    */
   public PSStopwatch()
   {           
      m_state = STOP_STATE;
      m_initialTime = 0;
      m_stopTime = 0;
      m_pausedTime = 0;
      m_pauseDelta = 0;
   }

   /**
    * Initialize the time in the stopwatch and change the state to 
    * running. The stopwatch must be in the stopped state before this
    * method is called. The initial state of the stopwatch is stopped.
    */
   public void start()
   {
      if (!isStopped())
      {
         throw new IllegalStateException(
            "Stopwatch must be stopped before started");
      }
      m_state = RUN_STATE;
      m_initialTime = getCurrentTime();
      m_stopTime = 0;
      m_pausedTime = 0;
      m_pauseDelta = 0;
   }

   /**
    * Stop the stopwatch and record the end time. The stopwatch must be
    * running or paused.
    */
   public void stop()
   {
      if (!isRunning() && !isPaused())
      {
         throw new IllegalStateException(
            "Stopwatch must be running before stopped");
      }
      m_state = STOP_STATE;
      m_stopTime = getCurrentTime();
   }
   
   /**
    * Pause the stopwatch. This causes any time between the pause and a 
    * call to {@link #cont()} to be disregarded in the reported time.
    * A pause with no corresponding continue will act like a call to
    * {@link #stop()}, but the reported state will be paused. It is an 
    * error if the stopwatch is not running and not paused when this call 
    * is made. If the stopwatch is already paused then this call has no 
    * effect.
    */
   public void pause()
   {
      if (isPaused()) return;
      
      if (!isRunning())
      {
         throw new IllegalStateException(
                     "Stopwatch must be running before pausing");
      }
      m_state = PAUSED_STATE;
      m_pausedTime = getCurrentTime();
   }
   
   /**
    * Put the stopwatch back into a running state. This call has no effect
    * if the stopwatch is already running. If it is paused, the time since
    * the call to pause is calculated and summed with any previous paused
    * times.
    */
   public void cont()
   {
      if (isRunning()) return;
      
      if (!isPaused())
      {
         throw new IllegalStateException(
                  "Stopwatch must be paused before continuing");
      }
      m_state = RUN_STATE;
      m_pauseDelta += getCurrentTime() - m_pausedTime;
      m_pausedTime = 0;
   }

   /**
    * Elapsed either returns the current elapsed time or the total time. If
    * the stopwatch is stopped then elapsed uses the recorded time, otherwise
    * it returns the elapsed time since <code>start</code> was called.
    * @return elapsed time in millis
    */
   public double elapsed()
   {
      if (isRunning())
      {
         return (double)(getCurrentTime() - m_initialTime - m_pauseDelta) / 1E6;
      }
      else if (isPaused())
      {
         return (double)(m_pausedTime - m_initialTime - m_pauseDelta) / 1E6;
      }
      else
      {
         return (double)(m_stopTime - m_initialTime - m_pauseDelta) / 1E6;
      }
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      String state = STATES[m_state];

      return "<Stopwatch " + state + " elapsed " + 
         ms_numberFormat.format(elapsed()) + " ms>";
   }

   /**
    * @return <code>true</code> if the stopwatch has not been 
    * started, or has been stopped.
    */
   boolean isStopped()
   {
      return m_state == STOP_STATE;
   }

   /**
    * @return <code>true</code> if the stopwatch has been started.
    */
   private boolean isRunning()
   {
      return m_state == RUN_STATE;
   }

   /**
    * @return <code>true</code> if the stopwatch is currently paused.
    */
   private boolean isPaused()
   {
      return m_state == PAUSED_STATE;
   }
   
   /**
    * Get the current time, either using a high-resolution timer or the 
    * default system timer.
    * @return the current time as fractional nanoseconds
    */
   private long getCurrentTime()
   {
      return System.nanoTime();
   }
   
   /**
    * A format to use when outputting the elapsed time.
    */
   private static NumberFormat ms_numberFormat = 
      new DecimalFormat("###,##0.##");
   
   /**
    * State is initialized in the constructor, and modified through
    * method calls. It should only ever have the state 
    * <code>STOP_STATE</code> or <code>RUN_STATE</code>.
    */
   private int m_state;
   /**
    * Contains the first time recorded in nanoseconds. This is set
    * in the {@link PSStopwatch#start()} method.
    */
   private long m_initialTime;
   /**
    * Contains the final time recorded in nanoseconds. This is set
    * in the {@link #stop()} method.
    */
   private long m_stopTime;
   /**
    * Contains the time (in nanoseconds) when the stopwatch was paused. 
    * This is set in the {@link #pause()} method.
    */
   private long m_pausedTime;
   /**
    * Contains the total time (in nanoseconds) spend in a paused state. 
    * This is set in the {@link #continue()} method.
    */
   private long m_pauseDelta;
}
