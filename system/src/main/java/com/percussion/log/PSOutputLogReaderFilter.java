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
 *   The PSOutputLogReaderFilter class writes messages to an output stream
 *   in either XML or raw text format.
 *
 */

public class PSOutputLogReaderFilter implements IPSLogReaderFilter
{
   // to provide support for additional output formats, add a constant
   // below in order (its value must be one plus the previous constant)
   // and don't forget to increment OUTPUT_INVALID
   public static final int OUTPUT_RAW = 0;
   public static final int OUTPUT_XML = 1;
   
   // this should be one greater than the greatest valid format
   private static final int OUTPUT_INVALID = OUTPUT_XML + 1;

   /**
    * Constructs an output log reader filter to write all log
    * messages to the output stream.
    *
    * @param   out   the output stream
    *
    * @param   outputFormat   OUTPUT_XML for XML format output, OUTPUT_RAW
    * for straight text format output
    *
    * @exception   IllegalArgumentException   if out is null or if outputFormat
    * is not a supported output format type
    */
   public PSOutputLogReaderFilter(java.io.OutputStream out, int outputFormat)
      throws IllegalArgumentException
   {
      this(out, outputFormat, null, null, null);
   }

   /**
    * Constructs an output log reader filter to write all log
    * messages created between startTime and endTime, inclusive,
    * to the output stream.
    *
    * @param   out   the output stream
    *
    * @param   outputFormat   OUTPUT_XML for XML format output, OUTPUT_RAW
    * for straight text format output
    *
    * @param   startTime   the earliest date of log messages to read.
    * Specify null if you want to read any messages created up to
    * endTime
    *
    * @param   endTime   the latest date of log messages to read.
    * Specify null if you want to read all messages created at or after
    * startTime up through the most recent message
    *
    * @param   applicationIds[]   array of the application IDs whose
    * messages will be retrieved. Any messages with an application ID not
    * found in this array will not be retrieved. Specify null to retrieve
    * messages with any application ID.
    *
    * @exception   IllegalArgumentException   if out is null or if outputFormat
    * is not a supported output format type
    */
   public PSOutputLogReaderFilter(
      java.io.OutputStream out,
      int outputFormat,
      java.util.Date startTime,
      java.util.Date endTime,
      int[] applicationIds   )
         throws IllegalArgumentException
   {
      super();
      
      if (out == null)
         throw new IllegalArgumentException("output stream cannot be null");
      
      if (outputFormat < OUTPUT_RAW || outputFormat >= OUTPUT_INVALID)
         throw new IllegalArgumentException("unsupported output format: " + outputFormat);
      
      m_out = new java.io.PrintStream(out);
      m_startTime = startTime;
      m_endTime = endTime;
      m_outputFormat = outputFormat;
      m_appIds = applicationIds;
   }

   /**
    * Get the application id(s) to retrieve log entries for. Return
    * <code>null</code> to get all log entries (server or application).
    * 
    * @return  the application id(s) to retrieve log entries for
    */
   public int[] getApplicationIds()
   {
      return m_appIds;
   }

   /**
    * Get the time to use as the earliest log entry to retrieve. Return
    * <code>null</code> to retrieve entries starting from the earliest
    * recorded log entry.
    * 
    * @return              the earliest log entry time to retrieve
    */
   public java.util.Date getStartTime()
   {
      return m_startTime;
   }

   /**
    * Get the time to use as the latest log entry to retrieve. Return
    * <code>null</code> to retrieve entries including the most recently
    * recorded log entry.
    * 
    * @return              the latest log entry time to retrieve
    */
   public java.util.Date getEndTime()
   {
      return m_endTime;
   }

   /**
    * Get the types of log entries to retrieve. Return
    * <code>null</code> or an empty array to retrieve all types of log
    * entries.
    * 
    * @return              the types of log entries
    */
   public int[] getEntryTypes()
   {
      return null;
   }

   /**
    * Dumps the message out to the output stream
    */
   public void processMessage(PSLogEntry msg,
      boolean filterWasApplied)
   {
      if (msg == null)
      {
         m_out.println("There are no more messages");
         return;
      }
      switch (m_outputFormat)
      {
      case OUTPUT_RAW :
         m_out.println(msg.toString());
         break;
      case OUTPUT_XML :
         m_out.println(msg.toXMLString());
         break;
      default:
         // do nothing
      }
      m_latestTimeRead = msg.getMessageTime();
   }

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
   public java.util.Date getNextStartTime()
   {
      // advance by a millisecond, then return it
      if (m_latestTimeRead != null)
         m_latestTimeRead.setTime(m_latestTimeRead.getTime() + 1);
      return m_latestTimeRead;
   }

   private java.util.Date m_startTime;
   private java.util.Date m_endTime;
   private java.util.Date m_latestTimeRead = null;

   private int m_outputFormat;
   private java.io.PrintStream m_out;
   private int[] m_appIds;
}
