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


