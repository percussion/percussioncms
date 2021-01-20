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

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.log.PSLogHandler;
import com.percussion.log.PSLogServerWarning;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.util.PSLineBreaker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
                            
/**
 * Extends PSLogHandler by adding functionality to create and send trace messages. 
 * Tracks all trace options for a particular application, and whether or not 
 * tracing is enabled for that app.  Other objects make calls to the 
 * PSDebugLogManager to create trace messages, and this class is responsible 
 * delegating that to one of the classes derived from IPSTraceMessage.  This class also maintains a reference to the ouput stream that is used to when sending a trace message.  Currently that is to a file in the application directory, but 
 * ultimately that may be to a remote debugging console or other Output Streams.
 */
public class PSDebugLogHandler extends PSLogHandler implements IPSTraceStateListener
{

   /**
    * Stores the log and trace options for this handler's application.  Checks to see 
    * if trace is enabled and sets m_traceEnabled accordingly.  Adds itself as a 
    * listener to the PSTraceInfo object.
    * @param logger The PSLogger from the application.  Contains all logging options
    * for that application.  If <CODE>null</CODE>, all types of logging will be disabled.
    * @param traceInfo The PSTraceInfo from the application.  Contains all
    * tracing options for that application.  May not be <code>null</code>.
    * @param appName the name of the app this is logging for.
    * @throws IllegalArgumentException if traceInfo is <code>null</code>.
    * @roseuid 39F4699A007D
    */
   public PSDebugLogHandler(PSLogger logger, PSTraceInfo traceInfo, PSApplication app)
   {
      super(logger);
      if (traceInfo == null)
         throw new IllegalArgumentException("traceInfo may not be null");

      if (app == null)
         throw new IllegalArgumentException("app may not be null");

      m_traceInfo = traceInfo;
      m_traceEnabled = traceInfo.isTraceEnabled();
      m_app = app;
      m_appName = app.getName();
   }
   
   /**
    * Constructs the appropriate message for the type specified using the object 
    * passed in and send this to the appropriate output stream.  
    *
    * @param type the flag specifying the type of info to trace
    * @param object An object which will contain the info required for the specified
    * type of trace.
    * @throws java.lang.IllegalArgumentException if the specified trace flag is not
    * supported
    * @roseuid 39F49AE60242
    */
   public void printTrace(int type, Object object)
   {
      // return right away if trace is not enabled
      if (!m_traceEnabled)
         return;

      // return right away if tracing is not enabled for the specified type
      if (!m_traceInfo.isTraceEnabled(type))
         return;

      try
      {
         // get the message object
         IPSTraceMessage message = getTraceMessage(type);
         // call print on the message object
         message.printTrace(m_traceInfo, object, m_traceWriter);
      }
      catch (IOException e)
      {
         //log message to console and shut down tracing
         logIOServerWarning(e.toString());
         shutdownTrace();
         
      }
      catch (Exception e)
      {
         PSConsole.printMsg("Debug", e);
      }
   }
   
   /**
    * Used to determine if tracing is on
    *
    * <code>null</code>.
    * @return boolean indicating <code>true</code> if tracing is enabled
    * for the application.  If tracing is not enabled for the application,
    * <code>false</code> is returned.
    * @roseuid 39F49BC50203
    */
   public boolean isTraceEnabled()
   {
      return m_traceEnabled;
   }

   /**
    * Used to determine if tracing is on, and if so, is it on
    * for a particular type of message.
    *
    * @param typeFlag Flag indicating the type of trace.  May not be
    * <code>null</code>.
    * @return boolean indicating <code>true</code> if tracing is enabled
    * for the application as well as for the specified type.  If tracing is not
    * enabled for the application, or if tracing is not enabled for that option,
    * <code>false</code> is returned.
    * @roseuid 39F49BC50203
    */
   public boolean isTraceEnabled(int typeFlag) 
   {
      return (m_traceEnabled && m_traceInfo.isTraceEnabled(typeFlag));
   }

   /**
    * Used to obtain the tracing options for this handler's application
    * @return an object containing all trace options for the application.  This
    * is a reference to the actual object stored in the PSApplication object.
    * Changes to this object are reflected in the PSApplication object.
    * @roseuid 39F49DCF036B
    */
   public PSTraceInfo getTraceInfo()
   {
      return m_traceInfo;
   }

   /**
    * Set's the writer for this handler to use when outputting trace messages.
    *
    * @param writer The trace writer to use.  May not be <code>null</code>.
    */
   public void setTraceWriter(PSTraceWriter writer)
   {
      if (writer == null)
         throw new IllegalArgumentException("writer may not be null");

      m_traceWriter = writer;
   }

   /**
    * Retrieve the writer used by this handler for writing trace messages.
    *
    * @return The writer used by this, may be <code>null</code> if a writer
    * has not yet been set.
    */
   public PSTraceWriter getTraceWriter()
   {
      return m_traceWriter;
   }

   /**
    * Force any system buffers associated with the trace output stream to be
    * synchronized with the underlying device (if there is one).  For example
    * if the OutputStream is an instance of a FileOutputStream, this will cause
    * all in-memory modified copies of buffers associated with it's
    * FileDesecriptor to be written to the hard disk, and it's timestamp
    * updated.  If any in-memory buffering is being done by the application
    * (for example, by a BufferedOutputStream object), those buffers must be
    * flushed into the FileDescriptor (for example, by invoking
    * OutputStream.flush) before that data will be affected by this method.
    */
   public void syncOutputStream()
   {
      try
      {
         if (m_traceEnabled)
         {
            m_traceWriter.syncOutputStream();
         }
      }
      catch (IOException e)
      {
         //log message to console and shut down tracing
         logIOServerWarning(e.toString());
         shutdownTrace();
      }
   }

   /**
    * Retrieves the appropriate trace message object matching the supplied trace
    * flag.
    * Has each possible type cached.  If the requested type is not already in the
    * list, it will create one lazily using the PSTraceMessageFactory and then cache
    * it
    * for future use.
    * @param typeFlag A flag indicating the type of info that will be traced
    * @return the type of trace info object as specified by the supplied flag
    * @roseuid 39F5A2CF0280
    */
   private IPSTraceMessage getTraceMessage(int typeFlag)
   {
      /*
       * check for requested message type in the map.  if not there,
       * create and add it for next time this message is requested
       */
      Integer key = new Integer(typeFlag);
      IPSTraceMessage message = (IPSTraceMessage)m_traceMessages.get(key);
      if (message == null)
      {
         message = PSTraceMessageFactory.getTraceMessage(typeFlag);
         m_traceMessages.put(key, message);
      }

      return message;
   }

   /**
    * Notifies this object that tracing has been disabled.  In this case need to
    * change internal flag to disabled and log messages
    *
    *
    * @param traceInfo the PSTraceInfo object that has been disabled
    * @roseuid 3A0084E8031C
    */
   public void traceStopped(PSTraceInfo traceInfo)
   {
      try
      {
         if (m_traceWriter != null)
         {
            PSTraceFlag flag = m_traceInfo.getTraceOptionsFlag();

            // get message
            ResourceBundle bundle = getBundle();

            String logMessage = bundle.getString("traceStopLogMsg");
            String traceMessage = bundle.getString("traceStopTraceMsg");

            Object[] msgParams = {m_appName};
            Object[] dumpParams = {MessageFormat.format(logMessage, msgParams)};

            // get the trace output stream and write a message
            write(
               new PSLogServerWarning(
                  IPSServerErrors.RAW_DUMP,
                  dumpParams,
                  true,
                  "Debug"));

            // change the first param
            msgParams[0] = getFormattedDate();
            // get the trace output stream and write a start header
            writeTraceHeader(
               MessageFormat.format(traceMessage, msgParams),
               m_traceInfo.getColumnWidth());

            // close the output stream
            m_traceWriter.close();
         }
      }
      catch (IOException e)
      {
         // log serverwarning
         logIOServerWarning(e.toString());
      }     
      finally
      {
         m_traceEnabled = false;
      }
   }

   /**
    * Notifies this object that tracing has been enabled.  In this case need to
    * change internal flag to enabled and log messages
    *
    * @param traceInfo the PSTraceInfo object that has been enabled
    * @roseuid 3A0084F2004E
    */
   public void traceStarted(PSTraceInfo traceInfo)
   {
      try
      {
         // setup output stream for tracing
         setTraceWriter(PSDebugManager.getDebugManager().getTraceWriter(m_app));

         String logMessage;
         String traceMessage;

         // get message
         ResourceBundle bundle = getBundle();

         // if trace already enabled, it's a restart
         if (m_traceEnabled)
         {
            traceMessage = bundle.getString("traceReStartTraceMsg");
            logMessage = bundle.getString("traceReStartLogMsg");
         }
         else
         {
            traceMessage = bundle.getString("traceStartTraceMsg");
            logMessage = bundle.getString("traceStartLogMsg");
         }


         // get the msg params
         PSTraceFlag flag = m_traceInfo.getTraceOptionsFlag();
         Object[] msgParams = {m_appName,
                        flag.toString(0),
                        flag.toString(1),
                        flag.toString(2),
                        flag.toString(3)
                        };

         Object[] dumpParams = {MessageFormat.format(logMessage, msgParams)};

         // write message to log and console
         write(
            new PSLogServerWarning(
               com.percussion.server.IPSServerErrors.RAW_DUMP,
               dumpParams,
               true,
               "Debug"));

         // change the first param
         msgParams[0] = getFormattedDate();

         writeTraceHeader(
            MessageFormat.format(traceMessage, msgParams),
            m_traceInfo.getColumnWidth());

         m_traceEnabled = true;

      }
      catch (IOException e)
      {
         // log serverwarning and shutdown
         logIOServerWarning(e.toString());
         shutdownTrace();
      }
   }

   /**
    * Notifies this object that tracing has been re-enabled.  In this case there is
    * no need to change internal flag to enabled - just log message
    *
    * @param traceInfo the PSTraceInfo object that has been enabled
    * @roseuid 3A0084F2004E
    */
   public void traceRestarted(PSTraceInfo traceInfo)
   {
      traceStarted(traceInfo);
   }

   /**
    * Disables tracing, freeing any associated resources.
    */
   public void shutdownTrace()
   {

      // if tracing is enabled, disable to trigger logging
      if (m_traceInfo.isTraceEnabled())
         m_traceInfo.setTraceEnabled(false);

      // clear references in PSTraceInfo object
      m_traceInfo.removeTraceStateListener(this);

      // try to close writer
      try
      {
         if (m_traceWriter != null) m_traceWriter.close();
      }
      catch (IOException e){}
   }

   /**
    * Notifies this object that tracing has been enabled.  In this case need to
    * change internal flag to enabled
    *
    * @param traceInfo the PSTraceInfo object that has been enabled
    * @roseuid 3A0084F2004E
    */
   private ResourceBundle getBundle()
   {
      if (m_resourceBundle == null)
         m_resourceBundle =
         ResourceBundle.getBundle("com.percussion.server.PSStringResources");

      return m_resourceBundle;
   }

   /**
    * formats a header using the supplied message and width
    *
    * @param msg the message
    * @param width the width of the message in chars
    * @throws java.io.IOException if there is a problem writing to the buffer
    */
   private void writeTraceHeader(String msg, int width)
      throws IOException
   {
      // get the trace output stream
      BufferedWriter buf = new BufferedWriter(m_traceWriter);

      // add a blank line
      buf.newLine();

      // add a row of chars
      addChars(buf, HEADER_CHAR, width);
      buf.newLine();;


      /* now add the middle row(s) with the text inside
       * may need to chunk up message and add mulitple rows if too long
       * need to account for a "#" and a space on either end (4 chars) as in:
       * 
       * #############
       * # <message> #
       * #############
       *
       * the longest "word" in our message is 10 chars, so give a max of 11
       */
      PSLineBreaker breaker = new PSLineBreaker(msg, width - 4, 11);

      // now add the middle row(s) with the text inside
      int msgLen = breaker.maxLength();
      int remainder = width - (msgLen + 2);

      int leftSideLen = remainder / 2;
      int rightSideLen = (width - (msgLen + 2) - leftSideLen);

      while (breaker.hasNext())
      {
         // get the string
         String mid = breaker.next();

         // add left side
         addChars(buf, HEADER_CHAR, leftSideLen);
         buf.write(' ');

         // add the message chunk
         buf.write(mid);

         // pad out spaces to match longest line
         if (mid.length() < msgLen)
            addChars(buf, ' ', msgLen - mid.length());

         // add right side
         buf.write(' ');
         addChars(buf, HEADER_CHAR, rightSideLen);
         buf.newLine();
      }

      // add row of footer chars
      addChars(buf, HEADER_CHAR, width);
      buf.newLine();;

      // add a blank line
      buf.newLine();;

      // flush it
      buf.flush();
   }

   /**
    * appends the character to the buffer the specified number of times
    * @param buf the buffer to append to
    * @param addChar the character to repeat
    * @param len the number of chars to add
    * @throws java.io.IOException if there is a problem writing to the buffer
    */         
   private void addChars(BufferedWriter buf, char addChar, int len)
      throws java.io.IOException
   {
      for (int i = 0; i < len; i++)
         buf.write(addChar);
   }

   /**
    * appends the character to the buffer the specified number of times
    * @return the current time date as a String
    */
   private String getFormattedDate()
   {
      String date = null;
      try
      {
         if (m_format == null)
            m_format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

         date = new String(m_format.format(new Date()));
      }
      catch (Exception e)
      {
         /* this should never happen as we are passing in a new Date object
          * and this will always be valid, and our format does not change.
          */
      }
      return date;
   }

   /**
    * logs warning to console and log if io error
    */
   private void logIOServerWarning(String msg)
   {
      Object[] msgParams = {m_appName, msg};

      Object[] dumpParams = {MessageFormat.format(
         getBundle().getString("traceIOErrorMsg"), msgParams)};

      // write message to log and console
      write(
         new PSLogServerWarning(
            com.percussion.server.IPSServerErrors.RAW_DUMP,
            dumpParams,
            true,
            "Debug"));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDebugLogHandler)) return false;
      PSDebugLogHandler that = (PSDebugLogHandler) o;
      return m_traceEnabled == that.m_traceEnabled &&
              m_traceWriter.equals(that.m_traceWriter) &&
              m_traceMessages.equals(that.m_traceMessages) &&
              m_traceInfo.equals(that.m_traceInfo) &&
              m_appName.equals(that.m_appName) &&
              m_resourceBundle.equals(that.m_resourceBundle) &&
              m_format.equals(that.m_format) &&
              m_app.equals(that.m_app);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_traceWriter, m_traceMessages, m_traceEnabled, m_traceInfo, m_appName, m_resourceBundle, m_format, m_app);
   }

   /**
    * The writer used by this handler to write to the trace output stream, using
    * the appropriate encoding. <code>null</code> until the first call to
    * {@link #setTraceWriter(PSTraceWriter) setTraceWriter}, and is replaced
    * by each subsequent call to that method.
    */
   private PSTraceWriter m_traceWriter = null;

   /**
    * A list of currently instantiated IPSTraceMessage objects.  Stored using
    * typeFlag
    * as a key.
    */
   private HashMap m_traceMessages = new HashMap();

   /**
    * Indicates if tracing is enabled for the application.
    */
   private boolean m_traceEnabled = false;

   /**
    * The tracing options for this handler's application.
    */
   private PSTraceInfo m_traceInfo = null;

   /**
    * The name of the app this is a handler for
    */
   private String m_appName = null;

   /**
    * The resource bundle used for logging messages
    */
   private ResourceBundle m_resourceBundle = null;

   /**
    * The date formatter for trace header messages
    */
   private SimpleDateFormat m_format = null;

   /**
    * The resource bundle used for logging messages
    */
   private static final char HEADER_CHAR = '#';

   /**
    * The app this is a handler for
    */
   private PSApplication m_app = null;

}
