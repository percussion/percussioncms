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

