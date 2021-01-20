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

import com.percussion.server.PSServer;
import com.percussion.util.PSDoubleList;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.NamingException;

import org.xml.sax.SAXException;

/**
 *  The PSLogManager class is used as an interface for accesing the server's
 *  log. All reading from and writing to the server's log is done through
 *   the PSLogManager object.
 *  <p>
 *  The PSLogManager object receives log requests from the server and
 *  applications (indirectly) through objects extending the PSLogInformation
 *  class. Each application and the server actually use PSLogHandler objects
 *  to do their logging. See the
 *  {@link com.percussion.log.PSLogHandler PSLogHandler} class for more
 *  details.
 *  <p>
 *  One instance of this object is shared across all threads in the server.
 *  A synchronized queue is used to store the received log messages. A
 *  separate logging thread (created when the log manager starts) applies
 *  the log messages to the back-end. If the back-end server is down,
 *  causing the queue to build up, the log messages will be written to disk.
 *  This is done using the PSFileLogWriter class. The queue is disabled at
 *  this point. Once the server is back up, the log messages will be read
 *  back from disk (using the PSFileLogReader) and written to the back-end.
 *  The queue will once again be enabled.
 *  <p>
 *  Reading the log is also required. This is done by passing a log reader
 *   filter to the read() method.
 *
 *  <p>
 *  The PSLogManager class is also used to load the string resources
 *  used by the various log classes based upon the E2 server's locale.
 *  <p>
 *  Log messages are broken down into ranges, assigned to the various
 *  components. The ranges we are using are as follows:
 *  <table border="1">
 *     <tr><th>Range</th>      <th>Component</th></tr>
 *     <tr><td>0001 - 2000</td><td>Server Logging</td></tr>
 *     <tr><td>2001 - 4000</td><td>Application Logging</td></tr>
 *     <tr><td>4001 - 6000</td><td>User Logging</td></tr>
 *     <tr><td>6001 - 8000</td><td>Error Logging</td></tr>
 *  </table>
 *  <p>
 *  All log messages are stored using the format defined in
 *  the java.text.MessageFormat class. The message string  contains curly
 *  braces around parameters, which are 0 based. The MessageFormat.format
 *  method can then be used with an array of arguments to generate the
 *  appropriate string. For instance:
 *  <pre><code>
 *     String msg = PSLogManager.getMessageText(999);
 *
 *     // let's assume the returned message is:
 *     //    "param 1={0}, param 2 date={1,date}, param 2   time={1,time}"
 *
 *     Object[] args = { new Integer(1), new Date() };
 *
 *     String displayMsg = MessageFormat.format(msg, args);
 *
 *     // displayMsg is returned containing:
 *     //    "param1=1, param 2 date=Jan 6, 1999, param 2   time=4:50 PM"
 *  </code></pre>
 *
 *  This model is excellent for internationalization as the position of the
 *  parameters may change based upon the target language.
 *
 *
 * @author   Tas Giakouminakis
 * @version  1.0
 * @since 1.0
 */
public class PSLogManager
{

   /**
    *   Initialize the  log manager to  use the specified  log mechanism
    *   and locale. It is an error to call init() twice without calling
    *   close() in between (i.e., you cannot re init() the log manager
    *   until you have explicitly closed it first).
    * <p>
    * If a DBMS is used for logging, and a database error occurs when trying to
    * create the log writer and reader, this method will retry the creation 
    * several times, in case Rhythmyx has started before the DBMS.
    *   <p>
    *   At  this time, storing the log  in a local file is the only
    *   supported logging    mechanism. The  following information is required
    *   to  describe   where    the log will be stored:
    *   <p>
    *  In case of fileTo = "DBMS":
    *  <table border="1">
    *   <tr><th>Key</th><th>Value</th></tr>
    *   <tr><td>logTo</td>
    *       <td>DBMS</td>
    *   </tr>
    *   <tr><td>driverType</td>
    *       <td>the    type of driver  to  connect with (must be ODBC)</td>
    *   </tr>
    *   <tr><td>serverName</td>
    *       <td>the    back-end    server name (must    be  an  ODBC DSN)</td>
    *   </tr>
    *   <tr><td>databaseName</td>
    *       <td>the    back-end    database    containing the  E2  object store</td>
    *   </tr>
    *   <tr><td>loginId</td>
    *       <td>the    login    ID  to  use when  connecting</td>
    *   </tr>
    *   <tr><td>loginPw</td>
    *       <td>the    login    password    to  use when   connecting</td>
    *   </tr>
    *   <tr><td>logUrl</td>
    *      <td><B>OPTIONAL</B>the URL of the backup log file to be used while the
    *      back end is down (currently, only URLs of the type
    *      file:///c:/foo/bar/baz/logfile.ext are supported
    *   </td>
    *   </tr>
    *   </table>
    *   <p>
    *  In case of fileTo = "FILE":
    *  <table border="1">
    *   <tr><th>Key</th><th>Value</th></tr>
    *   <tr><td>logTo</td>
    *       <td>FILE</td>
    *   </tr>
    *   <tr><td>logUrl</td>
    *         <td>the URL of the log file (currently, only URLs of the type
    *         file:///c:/foo/bar/baz/logfile.ext are supported
    *      </td>
    *   </tr>
    *   </table>
    *
    * @param props The properties defining where the log is stored, not
    * <code>null</code>
    *
    * @param loc The locale to use, used to determine the string bundle 
    * resource
    *
    * @throws IllegalArgumentException If props does not fully define the
    * location of the log
    *
    * @throws IllegalStateException If the log manager has already been 
    * initialized and not closed
    * 
    * @throws IOException If there is an error opening the log file 
    *
    * @throws ClassNotFoundException  If the JDBC driver class cannot be found
    *
    * @throws SAXException If the log file is corrupt
    *
    * @throws SQLException If some SQL problem other than a network
    * problem has occurred
    * @throws NamingException If there is an error resolving the default 
    * connection datasource.
    */
   synchronized public static void init(Properties props, Locale loc)
      throws IllegalArgumentException, IllegalStateException, IOException,
      ClassNotFoundException, SQLException, SAXException, NamingException
   {
      conOut("Starting initialization");
      
      // precondition: cannot call init() on an open log manager
      if (isOpen())
         throw new IllegalStateException();
      
      // precondition: props must not be null
      if (props == null)
         throw new IllegalArgumentException("props must not be null");
      
      // get the logTo setting (FILE or DBMS) -- must be specified
      String logTo = props.getProperty("logTo");
      if (logTo == null)
         throw new IllegalArgumentException("logTo not specified");
      
      // Get and parse the file URL (assume it's a file:/// URL)
      // this is either the backup logfile or the primary, depending
      // on the logTo parameter
      String file = props.getProperty("logUrl");
      
      // initialize with something lame to suppress "may not have been
      // initialized" message
      URL fileURL = new URL("file:///_");
      m_logFile = null;
      
      // set up for file-based logging whether or not it is our primary
      // logging mechanism. Primary vs. secondary is done later
      if (file != null)
      {
         try
         {
            fileURL = new URL(file);
            if (!fileURL.getProtocol().equalsIgnoreCase("file"))
               throw new IllegalArgumentException("logfile must be local");
            String strFile = fileURL.getFile(); 
            if (strFile.startsWith("/"))
               m_logFile = new File(fileURL.getFile()); // absolute path
            else
               m_logFile = new File(PSServer.getRxDir(), fileURL.getFile());
            m_logFile.createNewFile();
         }
         catch (MalformedURLException e)
         {
            throw new IllegalArgumentException("Bad logfile URL");
         }
         catch (IOException io)
         {
            conOut("Could not create logfile: " + fileURL.getFile());
            throw io;
         }
      }
      
      // set up the queue
      m_queue = new PSDoubleList();
      
      // are we using back end or local logging?
      if (logTo.equalsIgnoreCase("DBMS"))
      {
         try
         {
            conOut("Using CMS Repository datasource: " + 
               PSConnectionHelper.getConnectionDetail(null).getDetailString());
            m_logWriter = new PSBackEndLogWriter();
            m_logReader = new PSBackEndLogReader();
         }
         catch (SQLWarning w)
         {
            conOut("SQL warning", new String[] { w.toString() } );
         }
         catch (SQLException e)
         {
            printSqlException("LogManager", "Caught SQLException", e);
            
            /* It is possible that Rhythmyx is starting before the backend,
               so wait and retry a few times before throwing the error */
            conOut( "Making five attempts to retry connection, " + 
               "sleeping 10 seconds between attempts." );
            for (int i=0; (i<5 && e != null); i++)
            {
               try
               {
                  Thread.sleep( 10000 );  // 10 seconds
                  
                  // writer might have succeeded, but reader failed
                  if (m_logWriter == null)
                     m_logWriter = new PSBackEndLogWriter();
                  m_logReader = new PSBackEndLogReader();
                  e = null; // when we succeed, clear the error to exit loop
               }
               catch (SQLException ee)
               {
                  e = ee; // remember the error and loop again
               }
               catch (InterruptedException interrupt)
               {
                  break;
               }
            }
            if (e != null)
               throw e;
         }
         
         if (m_logFile != null)
         {
            conOut("Opening secondary log file.");
            try
            {
               PSFileLogReaderWriterPair pair =
                  new PSFileLogReaderWriterPair(m_logFile);
               m_secondaryLogReader = pair;
               m_secondaryLogWriter = pair;

               java.util.Date now = new java.util.Date();

/* This was removed to fix bug Rx-00-10-0077. I left the code because we may
   want to re-instate it in the future with slight modifications.

               // if there are any messages hanging out in secondary log file, then
               // immediately transfer them to the back end (failing that, add them
               // to the queue so we can do it later)

               PSServerLogReaderFilter filter
                  = new PSServerLogReaderFilter(null, now)
               {
                  public int[] getApplicationIds()
                  {
                     return null; // all messages
                  }

                  public void processMessage(PSLogEntry msg,
                     boolean filterWasApplied)
                  {
                     if (firstMsg)
                        conOut("Found existing messages in secondary log file. Transferring to back end.");

                     firstMsg = false;

                     if (filterWasApplied && msg != null)
                     {
                        if (isOpen)
                        {
                           try
                           {
                              m_logWriter.write(msg);
                           }
                           catch (Throwable t)
                           {
                              isOpen = false;
                           }
                        }

                        if (!isOpen)
                        {
                           m_queue.add(msg);
                        }
                     }
                  }

                  private boolean firstMsg = true;
                  private boolean isOpen = true;
               };

               m_secondaryLogReader.read(filter);
*/
               m_secondaryLogWriter.truncateLog(now);
            }
            catch (SAXException sax)
            {
               conOut("Caught SAXException. Secondary logfile is corrupted. " +
                  "Failsafe will not be available.",
                  new String[] { sax.toString() } );
            }
         }
         else
            conOut(
            "No secondary logging mechanism specified. " +
            "Failsafe will not be available.");
      }
      else if (logTo.equalsIgnoreCase("FILE"))
      {
         if (m_logFile == null)
            throw new IllegalArgumentException();

         try
         {
            PSFileLogReaderWriterPair pair = 
               new PSFileLogReaderWriterPair(m_logFile);
            m_logWriter = pair;
            m_logReader = pair;
         }
         catch (SAXException sax)
         {
            conOut("Caught SAXException. Primary logfile is corrupted. " +
            "Logging will not be available.", new String[] { sax.toString() } );
            throw sax;
         }
      }
      else
         throw new IllegalArgumentException("logTo must be FILE or DBMS");

      try
      {
         m_queueChecker =
            new PSLogQueueThread(m_queue, 60000, 15);
         //m_queueChecker.setPriority(Thread.NORM_PRIORITY + 1);

         // give the queue checker thread some time to get started
         synchronized (m_queueChecker)
         {
            conOut("Starting log queue thread...");
            /* The following line used to be outside (above) this block. This
               resulted in a race condition which, depending on the timeslice size,
               could cause a hang. The hang could occur under the following
               condition: start was called, the logQ thread executes thru its
               notifyAll method before this thread even gets into this
               block. Then when it gets to the wait below, it would block forever.
               By moving it in here, the start method (which is synchronized)
               will block immediately, until this thread goes into the wait
               state, at which point it will initialize and notify this
               thread that it has completed. */
            m_queueChecker.start();
            m_queueChecker.wait();
         }
      }
      catch (InterruptedException e)
      {
         close();
         return;
      }
      // load the message string resource bundle for the specified locale
      m_messageStrings = ResourceBundle.getBundle(
         PSLogStringBundle.class.getName(), loc);
      
      m_isOpen = true;
   }

   /**
    * Initialize the log manager to use the specified log mechanism and the
    * default locale for this system.
    * <p>
    * Calls {@link #init(Properties, Locale) init(props, Locale.getDefault())} 
    */
   synchronized public static void init(java.util.Properties props)
      throws IllegalArgumentException, IllegalStateException, IOException,
      ClassNotFoundException, SQLException, SAXException, NamingException
   {
      init(props, Locale.getDefault());
   }

   /**
    * Shut down the log manager. No further logging is permitted through the log
    * manager once this call is made. Use the {@link #init(Properties) init}
    * method to re-initialize the log before attempting any further writes. It
    * is not an error to call close() on a closed PSLogManager.
    */
   synchronized public static void close()
   {
      conOut("Closing the log manager.");
      
      if (null != m_queueChecker)
      {
         m_queueChecker.shutdown();
         
         // wait for the queue checker thread to terminate
         try
         {
            m_queueChecker.join();
         }
         catch (InterruptedException ex)
         {
            conOut(
               "Interrupted while waiting for log queue thread to terminate.");
         }
      }
      
      flushQueue();
      conOut("Queue flushed. Continuing shutdown.");
      
      m_isOpen = false;
      
      m_useSecondaryLogMechanism = false;
      if (m_secondaryLogWriter != null)
         m_secondaryLogWriter.close();
      if (m_secondaryLogReader != null)
         m_secondaryLogReader.close();
      if (m_logReader != null)
         m_logReader.close();
      if (m_logWriter != null)
         m_logWriter.close();
   }

   /**
    * Queues the log message for writing. This method is thread-safe.
    *
    * @param msg the log message to be written
    * @exception   IllegalStateException    if the log manager is not
    *   associated with a log mechanism (init was never called, or close has
    *   been called and init never re-called)
    */
   public static void write(PSLogInformation msg)
      throws IllegalStateException
   {
      if (msg == null)
      {
         throw new IllegalArgumentException("Log manager cannot write null message");
      }
      
      if (!isOpen())
      {
         conOut("Discarded message because log manager is closed:",
            new String[] { "Message is \"" + msg.toString() + "\"" }
         );
         return;
      }
      synchronized (m_queue)
      {
         m_queue.addLast(msg);
      }
      notifyQueue();
   }
   
   /**
    * Immediately writes the log message to the log, bypassing the queue.
    * If the message cannot be written immediately, it is discarded. For
    * this reason, only the log queue thread should call writeThrough.
    * Everyone else should just call @link #write write(), which will
    * use the queue.
    *
    * @param  msg The log message to be written.
    * @throws IllegalStateException If the log manager is not
    * associated with a log mechanism (init was never called, or close was
    * called and init never re-called).
    */
   protected static void writeThrough(PSLogInformation msg)
   {
      if (!isOpen())
         return;
      
      IPSLogWriter writer;
      
      if (!m_useSecondaryLogMechanism)
      {
         if (m_logWriter.isOpen())
            writer = m_logWriter;
         else
         {
            conOut("The primary logging mechanism has gone down." +
               " Switching to failsafe mode.");
            writer = m_secondaryLogWriter;
            m_useSecondaryLogMechanism = true;
         }
      }
      else
      {
         writer = m_secondaryLogWriter;
         // if it's time, then attempt a reconnect
         if (System.currentTimeMillis() - m_msecLastReOpenAttempt >=
            m_msecBetweenReconnect)
         {
            // bug #Rx-99-10-0195 : we should record the time of the last
            // open attempt AFTER we do the attempt, so if the open attemp
            // itself takes longer than the retry interval, we don't try
            // to re-open each time we come through this logic
            // m_msecLastReOpenAttempt = System.currentTimeMillis(); 
            conOut("Attempting to re-open primary logging mechanism.");
            
            if (m_logWriter != null && m_logWriter.open())
            {
               conOut("Primary log mechanism re-open successful. Leaving failsafe mode.");
               m_useSecondaryLogMechanism = false;
               writer = m_logWriter;
               m_exitFailsafeTime = new java.util.Date();
               // read all messages from the secondary logfile between
               // m_enterFailSafeTime and m_exitFailSafeTime and put them
               // into the back end
               PSServerLogReaderFilter filter
                  = new PSServerLogReaderFilter(null, m_exitFailsafeTime)
               {
                  public int[] getApplicationIds()
                  {
                     return null;
                  }
                  
                  public void processMessage(PSLogEntry logMsg,
                     boolean filterWasApplied)
                  {
                     if (filterWasApplied && logMsg != null)
                     {
                        write(logMsg);
                     }
                  }
               };
               
               m_secondaryLogReader.read(filter);
               
               m_secondaryLogWriter.truncateLog(m_exitFailsafeTime);
            }
            else
            {
               m_msecLastReOpenAttempt = System.currentTimeMillis();
               conOut("Primary reopen failed. Failsafe mode still active.");
            }
         }
      }
      
      // in writeThrough(), messages will be discarded if they cannot
      // be immediately written to the log writer.
      if (!writer.isOpen())
      {
         conOut("Discarded message because all writers are closed:",
            new String[] { msg.toString() }
         );
         return;
      }
      
      // don't need to synchronize on the log writer because the write()
      // method is synchronized
      writer.write(msg);
   }
   
   /**
    * Reads the log with the specified filter
    * 
    * @param filter A log reader filter whose processMessage method will
    * be called with each log entry that meets the filter's conditions. Must
    * not be null
    * 
    * @throws IllegalStateException if a log reader has not been initialized.
    */
   public static void read(IPSLogReaderFilter filter)
   {
      conOut("Processing read request...");
      flushQueue();
      if (!isOpen())
         throw new IllegalStateException("cannot read when the log manager is closed");
      if (filter == null)
         throw new IllegalArgumentException("cannot read with a null filter");
      if (m_logReader == null)
         throw new IllegalStateException("logic error: log reader is null");
      
      m_logReader.read(filter);
   }
   
   /**
    *   Get the message text associated with the specified message code.
    *
    * @param code   the message code
    *
    * @return   the message text, or null if there is no message
    *   text associated with this code.
    */
   public static String getMessageText(int code)
   {
      if (m_messageStrings != null)
         return m_messageStrings.getString(String.valueOf(code));
      return null;
   }
   
   /**
    *   Returns true if the log manager is associated with a logging mechanism,
    *   false otherwise.
    *
    * @return   true if the log manager is associated with a logging mechanism,
    *   false otherwise
    */
   public static boolean isOpen()
   {
      return m_isOpen;
   }

   /**
    *   Notify the log queue thread that a new message has been queued.
    */
   private static void notifyQueue()
   {
      synchronized (m_queue)
      {
         m_queue.notifyAll();
      }
   }

   /**
    *   Console output functionality
    */
   private static void conOut(String msg, String[] subMessages)
   {
      com.percussion.server.PSConsole.printMsg(
         "LogManager", msg, subMessages);
   }
   
   /**
    *   Console output functionality
    */
   private static void conOut(String msg)
   {
      conOut(msg, null);
   }

   /**
    *   Flushes the queue unconditionally, silently ignores errors
    *
    */
   public static void flushQueue()
   {
      if (m_queue == null)
         return;
      
      Object[] msgs = null;
      synchronized (m_queue)
      {
         msgs = m_queue.toArray();
         m_queue.clear();
      }
      
      if (msgs != null)
      {
         for (int i = 0; i < msgs.length; i++)
         {
            writeThrough((PSLogInformation)msgs[i]);
         }
      }
   }
   
   /**
    * Truncate the log which has been there in the past day(s).
    * @param days      the amount of day the log has been there
    */
   public static void truncateLog(int days)
   {
      if (days <= 0)
         return;     // do not truncate
      
      java.util.Date beforeDate =
         new java.util.Date(
         System.currentTimeMillis() -
         ((long)days * ms_minutesInOneDay * MILLIS_IN_MINUTE));
      
      if (m_logWriter != null)
      {
         m_logWriter.truncateLog(beforeDate);
      }
      if (m_secondaryLogWriter != null)
         m_secondaryLogWriter.truncateLog(beforeDate);
   }

   public static void setRunningLogDays(int days)
   {
      m_queueChecker.setRunningLogDays(days);
   }
   
   static void printSqlException(String subsystem, String msg, SQLException e)
   {
      while (e != null)
      {
         com.percussion.server.PSConsole.printMsg(
            subsystem, msg , new String[] {
            "Message: " + e.getMessage(),
               "SQLState: " + e.getSQLState(),
               "ErrorCode: " + e.getErrorCode() }
         );
         e = e.getNextException();
      }
   }
   
   /**
    *   This is the property file backed string resource bundle containing
    *   our message strings
    */
   static private ResourceBundle m_messageStrings  = null;
   
   /**
    *   This is our instance of the log reader, which could be either a file
    *   reader or a back end reader, depending on the arguments to init().
    */
   static private IPSLogReader m_logReader = null;
   
   /**
    *   This is our instance of the log writer, which could be either a file
    *   writer or a back end writer, depending on the arguments to init().
    */
   static private IPSLogWriter m_logWriter = null;
   
   /**
    *   This is the secondary (backup) log reader, which will be used only
    *   when the primary log reader is down.
    */
   static private IPSLogReader m_secondaryLogReader;
   
   /**
    *   This is our instance of the log writer, which could be either a file
    *   writer or a back end writer, depending on the arguments to init().
    */
   static private IPSLogWriter m_secondaryLogWriter;
   
   /** Our logfile location */
   static private File m_logFile;
   
   /**
    *   true if the log manager is associated with a logging mechanism,
    *   false otherwise
    */
   static private boolean        m_isOpen       = false;
   
   /**
    *   Our internal queue of PSLogInformation objects that have not yet
    *   been written to the log.
    */
   static private PSDoubleList   m_queue;
   
   /** The log queue thread. */
   static private PSLogQueueThread m_queueChecker;
   
   /** True if we are currently in failsafe mode. */
   static private boolean m_useSecondaryLogMechanism = false;
   
   /** The time at which we left failsafe mode. */
   static private java.util.Date m_exitFailsafeTime;
   
   /**
    *   Number of milliseconds between our last written message and
    *   our current message that will invoke a reconnect attempt to
    *   the primary log mechanism.
    */
   static private long m_msecBetweenReconnect   = 30000;
   
   /**
    *   The system time in millseconds since 1970 at which last
    *   attempted to re-open the primary log mechanism.
    */
   static private long m_msecLastReOpenAttempt = 0;
   
   /** Total minutes in a single day. */
   private static final int ms_minutesInOneDay = 24 * 60;
   
   /** Milliseconds in one minute */
   private static final long MILLIS_IN_MINUTE = 60000L; // 1000 * 60   
}
