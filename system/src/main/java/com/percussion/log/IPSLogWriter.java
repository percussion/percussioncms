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

package com.percussion.log;


/**
 * IPSLogWriter defines an interface for writing log messages
 * (PSLogInformation sub-objects) to a log implementation. Log writers
 * should only be used by the PSLogManager object.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSLogWriter {
   /**
    *   Close   the   log   writer.   This should   only be   called when   it is   no longer
    *   needed.   Any   subsequent attempts to write through this object will throw
    *   an exception. It is not an error to call close() on a closed log writer.
    *
    *   @see #isOpen
    */
   public void close();

   /**
    *   Use to query whether the log writer is open or not.
    *
    *   @return   Returns true if the log writer is open, false if the log
    *   writer is not open.
    *
    *   @see #close
    */
    public boolean isOpen();

   /**
    * Write the log message.
    *
    * @param      msg                     the log message to be written
    * @exception  IllegalStateException   if close has already been called
    *                                     on this writer
    */
   public void write(PSLogInformation msg)
      throws java.lang.IllegalStateException;

   /**
    * Attemps to reopen the writer if it is closed.
    * @return   true if the log writer was already open or if it was
    * succesfully reopend, false otherwise.
    */
   public boolean open();

   /**
    * Remove all entries in the log created on or before the given date
    * and time.
    *
    * @param   allBefore   all entries with a time up to and including this
    * date will be truncated.
    */
   public void truncateLog(java.util.Date allBefore);
}

