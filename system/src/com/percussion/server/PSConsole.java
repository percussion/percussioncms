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
package com.percussion.server;

import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.error.PSErrorManager;
import com.percussion.log.PSLogError;
import com.percussion.security.PSUserEntry;
import org.apache.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.SQLException;

/**
 *   The PSConsole offers a single point of reference for console output
 *   (and in the future, perhaps input). Most subsystems should be using
 *   the log mechanism, but for those low-level areas that cannot rely
 *   on logging to be working, they should use PSConsole.
 *   The output through PSConsole currently uses m_out, but this
 *   could change in the future. Output looks like this:
 *   <PRE>
 *   LogManager       1/22/99 5:12 PM: Caught SQLException
 *                    Message: [Microsoft][ODBC SQL Server Driver]Unable to load communication module.  Driver has not been correctly installed.
 *                    SQLState: S1000
 *                    ErrorCode: 126
 *   LogManager       1/22/99 5:12 PM: Caught SQLException
 *                    Message: [Microsoft][ODBC SQL Server Driver]Unable to connect to data source
 *                    SQLState: 08001
 *                    ErrorCode: 0
 *   LogQueueThread   1/22/99 5:12 PM: Started log queue thread: Thread[Thread-2,5,main]
 *   LogQueueThread   1/22/99 5:12 PM: Interrupted. Shutting down.
 *</PRE>
 */
public class PSConsole /* extends Thread */
{
   /**
    * Construct the local server console which receives user input and
    * dispatches the appropriate console command handler.
    *
    * @param printCallStacks a flag specifying whether or not to print the
    *    call stack to the console and to the log (<code>true</code>) or only
    *    to the log.
    */
   PSConsole(boolean printCallStacks)
   {
      // setName("server console");
      // setDaemon(true);
      // setPriority(MIN_PRIORITY);
      ms_printCallStacks = printCallStacks;
   }

   /**
    * This method is called by Thread.start to begin execution of this
    * thread.
    */
   public void run()
   {   
      return;
   }

   /**
    *   Prints out the message for the given subsystem using an error code and
    * argument array. If the subsystem name
    *   is longer than 14 characters, it will be truncated to 14.
    *
    *   @param   subsystem   The name of the subsystem.
    *
    * @param   errorCode   The error code describing the message to print
    *
    *   @param   errorArgs   An array of arguments associated with the error
    */
   public static void printMsg(
      String subsystem,
      int errorCode,
      Object[] errorArgs)
   {
      getLogger(subsystem)
         .error(PSErrorManager.createMessage(errorCode, errorArgs));
   }

   /**
    *   Prints out the warning message for the given subsystem using an error
    *   code and argument array. If the subsystem name is longer than 14 
    *   characters, it will be truncated to 14.
    *
    *   @param   subsystem   The name of the subsystem.
    *
    *   @param   errorCode   The error code describing the message to print
    *
    *   @param   errorArgs   An array of arguments associated with the error
    */
   public static void printWarnMsg(
      String subsystem,
      int errorCode,
      Object[] errorArgs)
   {
      getLogger(subsystem)
         .warn(PSErrorManager.createMessage(errorCode, errorArgs));
   }
   
   /**
    * Convenience method that calls {@link #printInfoMsg(String, int, Object[], 
    * Level) printInfoMsg(subsystem, errorCode, errorArgs, null)}
    */
   public static void printInfoMsg(String subsystem, int errorCode,
      Object[] errorArgs)
   {
      if (subsystem == null)
         throw new IllegalArgumentException("subsystem may not be null");
      
      printInfoMsg(subsystem, errorCode, errorArgs, null);
   }
   
   /**
    * Prints out an informational message for the given subsystem using an 
    * error code and argument array.
    * 
    * @param subsystem the subsystem for which to print the message, not 
    *    <code>null</code>, may be empty. If the subsystem name is longer than 
    *    14 characters, it will be truncated to 14.
    * @param errorCode the error code for which to display the message.
    * @param errorArgs all arguments which are required to format the error
    *    message for the supplied error code, may be <code>null</code> or
    *    empty.
    * @param level the logging level, may be <code>null</code>. If not 
    *    provided the default <code>Level.INFO</code> is used.
    */
   public static void printInfoMsg(String subsystem, int errorCode,
      Object[] errorArgs, Level level)
   {
      if (subsystem == null)
         throw new IllegalArgumentException("subsystem may not be null");
      
      if (level == null)
         level = Level.INFO;
      
      getLogger(subsystem).info(
         PSErrorManager.createMessage(errorCode, errorArgs));
   }

   /**
    * Convenience method that calls {@link #printMsg(String, String, String[], 
    * Level) printMsg(subsystem, message, subMessages, null)}
    */
   public static void printMsg(String subsystem, String message,
      String[] subMessages)
   {
      if (subsystem == null)
         throw new IllegalArgumentException("subsystem may not be null");
      
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      
      printMsg(subsystem, message, subMessages, null);
   }
   
   /**
    * Convenience method that calls {@link #printMsg(String, String, String[]) 
    * printMsg(subsystem, message, null)}
    */
   public static void printMsg(String subsystem, String message)
   {
      if (subsystem == null)
         throw new IllegalArgumentException("subsystem may not be null");
      
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      
      printMsg(subsystem, message, null);
   }
   
   /**
    * Prints the supplied message and sub-messages to the console for the 
    * provided subsystem and logging level.
    * 
    * @param subsystem the subsystem for which to print the message, not 
    *    <code>null</code>, may be empty. If the subsystem name is longer than 
    *    14 characters, it will be truncated to 14.
    * @param message the message to be printed, not <code>null</code>, may be 
    *    empty.
    * @param subMessages the sub-messages to be printed, may be 
    *    <code>null</code> or empty.
    * @param level the logging level, may be <code>null</code>. If not 
    *    provided the default <code>Level.INFO</code> is used.
    */
   public static void printMsg(String subsystem, String message, 
      String[] subMessages, Level level)
   {
      if (subsystem == null)
         throw new IllegalArgumentException("subsystem may not be null");
      
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      
      if (level == null)
         level = Level.INFO;
      
      Logger l = getLogger(subsystem);
      if (subMessages == null)
         l.info(message);
      else
      {
         String arr[] = new String[subMessages.length + 1];
         arr[0] = message;
         System.arraycopy(subMessages, 0, arr, 1, subMessages.length);
         l.info( arr);
      }
   }

   /**
    * Prints an error message to the console for the provided subsystem and
    * exception. If the error message retrieved from the exception is
    * <code>null</code> or empty, we add the class name instead. The same
    * message and the exception call-stack is also logged to the log database.
    * If the exception is a <code>SQLException</code>, the messages of the
    * entire tree is logged to the log database, but only the first message
    * is printed to the server console.
    *
    * @param subsystem the subsystem string which caused the exception, not
    *    <code>null</code> or empty.
    * @param t the exception causing this error and for which to log the
         call-stack to the log database, not <code>null</code>.
    * @throws IllegalArgumentException is any parameter is <code>null</code>
    *    or the subsystem string is empty.
    */
   public static void printMsg(String subsystem, Throwable t)
   {
      if (subsystem == null || subsystem.length() == 0)
         throw new IllegalArgumentException("subsystem cannot be null or empty");
      if (t == null)
         throw new IllegalArgumentException("t cannot be null");

      String errorMessage = t.getLocalizedMessage();
      if (errorMessage == null || errorMessage.trim().length() == 0)
         errorMessage = t.getClass().getName();

      Object[] args = { errorMessage };
      String message =
         PSErrorManager.createMessage(
            IPSServerErrors.UNEXPECTED_EXCEPTION_CONSOLE,
            args);
      printMsg(subsystem, t, message);
   }

   /**
    * Prints the provided message for the supplied subsystem to the server
    * console. If the error message retrieved from the exception is
    * <code>null</code> or empty, we add the class name instead. The same
    * message and the exception call-stack is also logged to the log database.
    * If the exception is a <code>SQLException</code>, the messages of the
    * entire tree is logged to the log database, but only the first message
    * is printed to the server console.
    *
    * @param subsystem the subsystem string which caused the exception, not
    *    <code>null</code> or empty.
    * @param t the exception causing this error and for which to log the
         call-stack to the log database, not <code>null</code>.
    * @param message the error message to be printed to the server console,
    *    not <code>null</code> or empty.
    * @throws IllegalArgumentException is any parameter is <code>null</code>
    *    or the subsystem or message string is empty.
    */
   public static void printMsg(String subsystem, Throwable t, String message)
   {
      if (subsystem == null || subsystem.length() == 0)
         throw new IllegalArgumentException("subsystem cannot be null or empty");
      if (message == null || message.length() == 0)
         throw new IllegalArgumentException("message cannot be null or empty");
      if (t == null)
         throw new IllegalArgumentException("t cannot be null");

      if (t instanceof SQLException)
      {
         /*
          * We already have the first error message in the message string.
          * This will get all messages from the entire exception tree and
          * append them to the message string, each one on a new line.
          */
         StringBuffer buf = new StringBuffer(250); // arbitrary size
         SQLException next = ((SQLException) t).getNextException();
         while (next != null)
         {
            buf.append(System.getProperty("line.separator"));
            buf.append(next.getLocalizedMessage());
            next = next.getNextException();
         }

         message += buf.toString();
      }

      PSLogError logError = PSServerLogHandler.logException(message, t);

      Logger l = getLogger(subsystem);
      if (ms_printCallStacks)
         l.error(logError.toString());
      else
         l.error(message);
   }

   /**
    * Get user input from the server console. This method will wait until
    * user input is available before returning to the caller.
    *
    * @param   noWait   <code>true</code> to return immediately if no
    *                     data is readily available for reading
    *
    * @return            the console command submitted by the user or
    *                     <code>null</code> if no input is available and
    *                     <code>noWait</code> is <code>true</code>
    */
   public static String getUserInput(boolean noWait)
   {
      try
      {
         if (!noWait || m_inBuf.ready())
            return m_inBuf.readLine();
      }
      catch (IOException e)
      {
         m_out.println("Error getting console input: " + e.toString());
      }

      return null;
   }
   
   public static void setOutput(PrintStream out)
      throws IllegalArgumentException
   {
      if (out == null)
         throw new IllegalArgumentException("PSConsole output must be a valid stream");

      m_out = out;
   }

   // this method's sole purpose is to let the server determine if an
   // admin request was made using the local console window
   static boolean isConsoleUser(PSRequest request)
   {
      if (request == null)
         return false;

      PSUserSession sess = request.getUserSession();
      if (sess == null)
         return false;

      PSUserEntry[] users = sess.getAuthenticatedUserEntries();
      if ((users == null) || (users.length != 1))
         return false;

      return ms_consoleUser.equals(users[0]);
   }
   
   /**
    * Get the logger based on the subsystem name.
    * 
    * @param subsystem The name, assumed not <code>null</code> or empty.
    * 
    * @return The logger, not <code>null</code>.
    */
   private static final Logger getLogger(String subsystem)
   {
      if (ms_rootLogger == null)
      { 
         ensureLog4jConfiguration();
      }
      
      if (!subsystem.startsWith("com.percussion."))
         subsystem = "com.percussion." + subsystem;
      
      return LogManager.getLogger(subsystem);
   }

   /**
    * This is the reader we use for accessing console input.
    */
   private static final BufferedReader m_inBuf =
      new BufferedReader(new InputStreamReader(System.in));

   /** the PrintStream we print messages to */
   private static PrintStream m_out = System.out;

   /**
    * If <code>true</code> this flag indicates to print the call stack in the
    * <code>printMsg</code> not only to the log, but also to the console.
    */
   private static boolean ms_printCallStacks = false;

   // this is a special wrapper on the PSRequest object which has a fake
   // user session giving the user server admin rights
   class PSConsoleCommandRequest extends PSRequest
   {
      PSConsoleCommandRequest()
      {
         super();
         throw new RuntimeException("Commands are not supported within an" +
               " application server");
      }
   }

      private static final PSUserEntry ms_consoleUser =
         new PSUserEntry(
            "server-console",
            PSAclEntry.SACE_ADMINISTER_SERVER,
            null,
   // groups
      null, // attributes
   PSUserEntry.createSignature("server-console", ""));
   
   /**
    * This reference to a root logger is used for stand-alone uses of
    * PSConsole. Note that this is not used by PSConsole itself, it only
    * prevents gc from removing log4j from memory as long as PSConsole.class
    * is in memory. It also serves as a flag to indicate that log4j has
    * been configured.
    * 
    * It's worth noting for educational purposes that there are circumstances
    * where log4j may be configured externally to any percussion code. The
    * method {@link #ensureLog4jConfiguration()} checks first to make sure
    * that there isn't a root logger before configuration, which allows an
    * external entity to not have its configuration overwritten.
    */
   private static final Logger ms_rootLogger = null;

   /**
    * This method makes sure that log4j is configured for use in the console.
    * Normally this is configured in {@link PSServer} but there are a few
    * uses outside of the server for PSConsole.
    */
   private static synchronized void ensureLog4jConfiguration()
   {
   }   
}
