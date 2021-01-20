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

package com.percussion.log;



/**
 * IPSLogReaderFilter defines an interface for filtering log messages
 * read by an IPSLogReader implementation. When calling the IPSLogReader's
 * {@link com.percussion.log.IPSLogReader#read read} method, the filter
 * must be passed in. The filter may define the conditions it wants to
 * filter, in which case the IPSLogReader implementation may be able to
 * optimize its search. If this cannot be done, then the filter will be
 * called to check each log message manually.
 * <p>
 * Log entries can be filtered using one of the following mechanisms:
 * <ul>
 *   <li>within a specified time range</li>
 *   <li>by application id</li>
 *   <li>by application id and time range</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSLogReaderFilter {

   /**
    * Get the application id(s) to retrieve log entries for. Return
    * <code>null</code> to get all log entries (server or application).
    * 
    * @return  the application id(s) to retrieve log entries for
    */
   public int[] getApplicationIds();

   /**
    * Get the time to use as the earliest log entry to retrieve. Return
    * <code>null</code> to retrieve entries starting from the earliest
    * recorded log entry.
    * 
    * @return              the earliest log entry time to retrieve
    */
   public java.util.Date getStartTime();

   /**
    * Get the time to use as the latest log entry to retrieve. Return
    * <code>null</code> to retrieve entries including the most recently
    * recorded log entry.
    * 
    * @return              the latest log entry time to retrieve
    */
   public java.util.Date getEndTime();

   /**
    * Get the time to use for the next traversal of log entries. This
    * uses the latest log time read by processMessage. If this log filter
    * was not previously used in a call to the
    * {@link com.percussion.log.IPSLogReader#read IPSLogReader's read}
    * method, <code>null</code> will be returned.
    * 
    * @return              the time to use for the next traversal of
    *                      log entries
    */
   public java.util.Date getNextStartTime();

   /**
    * Get the types of log entries to retrieve. Return
    * <code>null</code> or an empty array to retrieve all types of log
    * entries.
    * 
    * @return              the types of log entries
    */
   public int[] getEntryTypes();

   /**
    * Process the next log message.
    *
    * @param      msg                     the log message which was read,
    *                                     or <code>null</code> to signify
    *                                     that no more log messages exist
    *
    * @param      filterWasApplied        <code>true</code> if the filter
    *                                     conditions were applied prior to
    *                                     this call by the log reader;
    *                                     <code>false</code> if applying
    *                                     filter conditions is not supported
    *                                     by the log reader
    */
   public void processMessage(PSLogEntry msg,
                              boolean filterWasApplied);
}

