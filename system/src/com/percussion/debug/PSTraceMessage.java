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

package com.percussion.debug;

import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.util.PSLineBreaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Base class for all objects that output trace messages.
 */
public abstract class PSTraceMessage implements IPSTraceMessage
{

   /**
    * Constructor for this class.  Sets the type of trace flag for this object
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FECDCC034B
    */
   public PSTraceMessage(int typeFlag)
   {
      m_typeFlag = typeFlag;
   }

   /**
    * Formats the message body into a String.  The constructor of this body will add
    * newline characters where required, but the column width specified by the trace
    * options may cause lines to be wrapped when sent to the output stream.
    *
    * @param source the source of the information to be used in generating the trace
    * message.   May not be <code>null</code>.
    * @return the message body as a String
    * @roseuid 39F5AB4A03B9
    */
   protected abstract String getMessageBody(Object source);

   /**
    * Retrieve the text of the message header. Always begins with a newline character.
    *
    * @return the text of the message header
    * @roseuid 39F5B21E0251
    */
   protected abstract String getMessageHeader();

   /**
    * Retreives the necessary information from the object it was instantiated with,
    * and then formats the output of its message and sends it to the supplied output
    * stream.  Calls abstract methods getMessageHeader and getMessageBody to
    * construct the output, and these calls are overriden by the specific message
    * object subclass.
    * @param tracer The trace options set by the application.  May not be
    * <code>null</code>.
    * @param out The output stream writer to which the formatted message is
    * sent.   May not be <code>null</code>.
    * @param source the source of the information to be used in generating the trace
    * message.   May not be <code>null</code>.
    * @throws IOException if there is a problem writing to the output stream
    */
   public void printTrace(PSTraceInfo traceInfo, Object source,
      PSTraceWriter target)
   throws IOException
   {
      // buffer the output for better performance
      BufferedWriter buf = new BufferedWriter(target);

      // format header
      StringBuffer header = new StringBuffer();
      header.append(NEW_LINE);

      // add the timestamp
      if (m_formatter == null)
         m_formatter = new SimpleDateFormat(TS_FORMAT);
      header.append(new String(m_formatter.format(new Date())));
      header.append(' ');

      // add message header
      header.append(getMessageHeader());

      // calculate our line break max based on 25% of the column width
      int lineBreakMax = (traceInfo.getColumnWidth() / 4);

      // print it out, formatting for column width
      PSLineBreaker breaker = new PSLineBreaker(new String(header),
                                    traceInfo.getColumnWidth(), lineBreakMax);

      while (breaker.hasNext())
      {
         // add the line with 2 spaces in front followed by a newline
         buf.write(breaker.next());
         buf.newLine();
      }


      // print out body if not timestamp only adjusting for column width
      if (!traceInfo.IsTimeStampOnlyTrace())
      {
         String msg = getMessageBody(source);
         int colWidth = traceInfo.getColumnWidth() - 2;  // 2 spaces at start of line

         // break up into lines and then adjust according to our column width

         BufferedReader reader = new BufferedReader(new StringReader(msg));

         String line = null;
         while ((line = reader.readLine()) != null)
         {
            // use a line breaker to be sure line wraps as necessary
            breaker = new PSLineBreaker(line, colWidth, lineBreakMax);

            while (breaker.hasNext())
            {
               // add the line with 2 spaces in front followed by a newline
               buf.write("  ");
               buf.write(breaker.next());
               buf.newLine();
            }
         }
      }

      // flush the buffer
      buf.flush();
   }

   /**
    * Used to identify the type of information this object is used to trace.
    *
    * @return The type flag of the object implementing this interface.
    * @roseuid 39FECDA702CE
    */
   public int getTypeFlag()
   {
      return m_typeFlag;
   }

   /**
    * Utility method to retreieve a message string and subsitute the
    * arguments using MessageFormat
    *
    * @param args an Object array with the last item a String
    * naming the message format string:
    * Object[] = {[arg1[, ...argN]], MessageFormat}
    *
    * @return the fully constructed message body
    */
   protected static String getMessageFromArgs(Object[] args)
   {
      /* object should be an Object array with the last item a String
       * naming the message format string
       */
      int msgIndex = args.length - 1;
      return MessageFormat.format(ms_bundle.getString((String)args[msgIndex]),
                                    args);
   }

   /**
    * newline separator for this system
    */
   protected static final String NEW_LINE = System.getProperty("line.separator");

   /**
    * resource bundle for use by all derived classes from which to
    * retrieve their message formats
    */
   protected static ResourceBundle ms_bundle = ResourceBundle.getBundle(
            "com.percussion.server.PSStringResources");

   /**
    * Indicates this message's type
    */
   protected int m_typeFlag;

   /**
    * format for the header timestamp
    */
   private static final String TS_FORMAT = "MM/dd hh:mm:ss.SSS";

   /**
    * Formatter for dates
    */
   private SimpleDateFormat m_formatter = null;
}
