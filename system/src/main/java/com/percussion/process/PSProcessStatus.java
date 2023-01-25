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
package com.percussion.process;

/**
 * Class for storing and obtaining the current status of the
 * process. A process must be in one of the following states:
 * <pre>
 * <code>PROCESS_NOT_STARTED</code>
 * <code>PROCESS_FAILED_TO_START</code>
 * <code>PROCESS_STARTED</code>
 * <code>PROCESS_INTERRUPTED</code>
 * <code>PROCESS_FINISHED</code>
 * </pre>
 */
public class PSProcessStatus
{
   /**
    * Returns the process status.
    *
    * @return the process status, one of the <code>PROCESS_XXX</code> values
    */
   public synchronized int getStatus()
   {
      return m_status;
   }

   /**
    * Sets the process status.
    *
    * @param status the process status, one of the <code>PROCESS_XXX</code>
    * values
    */
   public synchronized void setStatus(int status)
   {
      if ((status == PROCESS_NOT_STARTED)
            || (status == PROCESS_STARTED)
            || (status == PROCESS_FAILED_TO_START)
            || (status == PROCESS_INTERRUPTED)
            || (status == PROCESS_FINISHED))
      {
         m_status = status;
      }
      else
      {
         throw new IllegalArgumentException("Invalid process status");
      }
   }

   /**
    * Constant indicating the process has not yet started.
    */
   public static final int PROCESS_NOT_STARTED = 0;

   /**
    * Constant indicating that the <code>Process.exec</code> method threw an
    * <code>IOException</code>.
    */
   public static final int PROCESS_FAILED_TO_START = 1;

   /**
    * Constant indicating the process has started but not completed.
    */
   public static final int PROCESS_STARTED = 2;

   /**
    * Constant indicating the process has finished.
    */
   public static final int PROCESS_FINISHED = 3;

   /**
    * Constant indicating the process may continue to run, but the action
    * was interrupted while waiting for it to finish. This should never happen
    * in practice.
    */
   public static final int PROCESS_INTERRUPTED = 4;

   /**
    * Stores the process status, initialized to
    * <code>PROCESS_NOT_STARTED</code>, modified in the <code>setStaus</code>
    * method.
    */
   private int m_status = PROCESS_NOT_STARTED;
}


