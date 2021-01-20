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
package com.percussion.tablefactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple logging class that will print messages to a stream and optionally
 * to a file. The stream defaults to System.out, but can be overridden by the
 * user. In addition, debugging messages can be optionally written (by default
 * they aren't). There is a single instance of this class that can be obtained by
 * calling {@link #getLogger() getLogger}.
 */
public class Logger implements LogSink, IPSLogger
{
   /**
    * Returns the singleton instance of this class. The first time it is called,
    * the instance is created and initialized. The default logger writes its
    * messages to System.out. To log to a file, call {@link #setOutputFile(File)
    * setOutputFile}. To change the output stream call {@link #setOutputStream(
    * PrintStream) setOutputStream}. <p/>
    * The primary class should keep a copy of the logger reference so the GC
    * doesn't unload the class.
    */
   public static Logger getLogger()
   {
      if ( null == ms_logger )
         ms_logger = new Logger();
      return ms_logger;
   }

   /**
    * Sets a flag that dictates whether messages logged via {@link
    * #logDebugMessage(String) logDebugMessage} will be written to the current
    * stream.
    *
    * @param enable If <code>true</code>, debug messages will be written,
    * otherwise they are ignored.
    */
   public void enableDebuggingOutput( boolean enable )
   {
      m_debug = enable;
   }

   /**
    * Flushes all current logging channels and closes all files. All file logging
    * is disabled after this method returns. If any errors occur, a message is
    * written to the console.
    */
   public void shutdown()
   {
      try
      {
         if ( null != m_outFile )
         {
            m_outFile.flush();
            m_outFile.close();
            m_outFile = null;
         }
         m_stdOut.flush();
      }
      catch ( IOException e )
      {
         System.out.println( "[Logger] Failure during logger shutdown: " +
            e.getLocalizedMessage());
      }
   }

   /**
    * This can be used to write stuff to the same stream as the logger when a
    * String is not available (such as printing an exception&apos;s stack trace);
    *
    * @return The stream currently used by the logger. The logger maintains
    * ownership of this stream, so is only allowing the caller to 'borrow' it,
    * so the caller should not close it.
    */
   public PrintStream getOutputStream()
   {
      return m_stdOut;
   }

   /**
    * Enables/disables file logging. If a file name is supplied, that file is
    * opened and logging commences to that file on the next message. If null is
    * supplied, file logging is disabled. All pending output is written before
    * the file is closed. <p/>
    *
    * @param log A valid file object that specifies the logging file. If it
    * doesn't exist, it will be created. If it exists, new entries will be
    * appended. If null, logging to the existing file will be stopped.
    *
    * @throws IOException If any errors occur opening/closing the file.
    */
   public void setOutputFile( File log )
         throws IOException
   {
      if ( m_outFile != null )
      {
         m_outFile.flush();
         m_outFile.close();
         m_outFile = null;
      }
      if ( null != log )
         m_outFile = new FileWriter( log.getAbsolutePath(), true );
   }

   /**
    * Sets the stream to which all messages will be logged. By default, this
    * is System.out.
    *
    * @param stdOut The stream to accept the logging output. May not be null.
    *
    * @throws IllegalArgumentException if stdOut is null.
    */
   public void setOutputStream( PrintStream stdOut )
   {
      if ( null == stdOut )
         throw new IllegalArgumentException( "PrintStream cannot be null." );
      m_stdOut = stdOut;
   }

   /**
    * Logs a message with no arguments to the stream if the debugging flag
    * is enabled after prepending [Debug] onto it.<p/>
    * See {@link #logMessage(String, Object[], boolean) logMessage } for a complete
    * description.
    */
   public void logDebugMessage( String msg )
   {
      if ( null == msg )
         throw new IllegalArgumentException( "Message cannot be null" );
      if ( m_debug )
         logMessage( "[Debug] " + msg, null, true );
   }

   /**
    * Logs a message with no arguments to the stream and file. <p/>
    * See {@link #logMessage(String, Object[], boolean) logMessage } for a complete
    * description.
    */
   public void logMessage( String msg )
   {
      if ( null == msg )
         throw new IllegalArgumentException( "Message cannot be null" );
      logMessage( msg, null, false );
   }

   /**
    * Logs a message with arguments to the stream and file. <p/>
    * See {@link #logMessage(String, Object[], boolean) logMessage } for a complete
    * description.
    */
   public void logMessage( String msg, Object [] args )
   {
      if ( null == msg )
         throw new IllegalArgumentException( "Message cannot be null" );
      logMessage( msg, args, false );
   }

   /**
    * Logs a message with no arguments to the current stream only. By default
    * messages will be printed on the console. <p/>
    * See {@link #logMessage(String, Object[], boolean) logMessage } for a complete
    * description.
    */
   public void logStreamMessage( String msg )
   {
      if ( null == msg )
         throw new IllegalArgumentException( "Message cannot be null" );
      logMessage( msg, null, true );
   }

   /**
    * Logs a message with parameters to the current stream and optionally a file.
    * Uses {@link MessageFormat#format(String, Object[]) format} to create the
    * final message. The msg string should contains placeholders that are
    * replaced when the args are processed. The msg is prepended w/ the current
    * date and time in the format [Mon dd, YYYY HH:MM] (hours range 0-23).
    *
    * @param msg The basic text of the message. May have replacement values. For
    * each replacement arg, there should be 1 or more entries in the args array,
    * as defined in the <code>MessageFormat</code> class. May not be null.
    *
    * @param args Objects that will be formatted and placed in the supplied
    * msg. If null, the msg is logged without formatting.
    *
    * @param streamOnly If <code>true</code>, will only write to the stream,
    * not the file.
    *
    * @throws IllegalArgumentException If msg is null.
    */
   public void logMessage( String msg, Object [] args, boolean streamOnly )
   {
      if ( null == msg )
         throw new IllegalArgumentException( "Message cannot be null" );

      String finalMsg;

      if ( null == args )
         finalMsg = msg;
      else
         finalMsg = MessageFormat.format( msg, args );
        
      //prepend with date&time, so that we could figure out when it was logged..  
      finalMsg = (ms_dateFormat.format(new Date()) + " " + finalMsg);         

      m_stdOut.println( finalMsg );
      try
      {
         if ( !streamOnly && null != m_outFile )
         {
            m_outFile.write( finalMsg );
            m_outFile.write( NEWLINE );
         }
      }
      catch ( IOException e )
      {
         System.out.println( "[Logger] Logging to file failed: " +
            e.getLocalizedMessage());
      }
   }

   /**
    * Made private to implement the singleton pattern.
    */
   private Logger()
   {}

   /*****************************************************************/
   /*************** LogSink Interface *******************************/
   /**
    * Logs the message.
    * @author   chadloder
    *
    * @version 1.2 1999/08/20
    *
    *
    * @param   message
    *
    */
   public void log(String message)
   {
      logMessage( message );
   }

   /**
    * Logs the exception, including a stack trace.
    *
    * @author   chadloder
    *
    * @version 1.2 1999/08/20
    *
    * @param   t
    *
    */
   public void log(Throwable t)
   {
      logMessage( t.getLocalizedMessage());
   }

   /**
    * Logs the exception, including a stack trace, and a message.
    * If the message is null, it will not be logged.
    *
    * @author   chadloder
    *
    * @version 1.2 1999/08/20
    *
    * @param   message
    * @param   t
    *
    */
   public void log(String message, Throwable t)
   {
      logMessage( message );
      logMessage( t.getLocalizedMessage());
   }



   /**
    * The logging file, may be null. If not null, all messages get written to
    * the file.
    */
   private FileWriter m_outFile = null;

   /**
    * The logging stream, never null. All messages get written to this stream.
    */
   private PrintStream m_stdOut = System.out;

   /**
    * Should debugging statements (those logged w/ logDebugMessage) be written?
    * They will be if this flag is <code>true</code>.
    */
   private boolean m_debug = false;

   /**
    * The singleton instance of this object. Is null until the first time the
    * <code>getLogger</code> method is called. Then it is never null after that
    * unless the class gets unloaded. To prevent this, the main class should
    * get the logger and keep a reference to it so the GC doesn't unload the
    * class.
    */
   private static Logger ms_logger;

   /**
    * The characters to use to print a newline, as a String. Must not be null.
    */
   private static final String NEWLINE = "\r\n";
   
   /**
    * Time stamp format for table factory.
    * i.e.: MM/dd/yy HH:mm:ss -> 09/29/04 18:31:28
    */
   private static SimpleDateFormat ms_dateFormat =
      new SimpleDateFormat("MM/dd/yy HH:mm:ss"); //ie: 09/29/04 18:31:28
} 
