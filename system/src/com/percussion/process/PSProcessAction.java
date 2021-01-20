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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper around the Java <code>Process</code> class that adds output 
 * stream management. When an instance of this class is created, a new 
 * thread is created in which the process is run. A new thread is also
 * created to read the standard and error output of the process and buffer
 * it locally for consumption by the caller as they desire.
 */
public class PSProcessAction implements Runnable
{
   public static void main(String[] args)
      throws Exception
   {
      Runtime r = Runtime.getRuntime();
      String[] params = new String[]
      {
         "SetupConsole",
         "-console"
      };
      Process p = r.exec(params);
      StringBuffer buf = new StringBuffer();
      OutputReader stdout = new OutputReader(p.getInputStream(), buf, "stdout");
      int done = 2;
      OutputStream stdin = p.getOutputStream();
      Writer ow = new OutputStreamWriter(stdin);
      while (done > 0)
      {
         getNextInput(buf);
         ow.write("1\n");
//         ow.write("\n\n\n\n\n");
         
         done--;
      }
      ow.close();
      
   }

   public static void getNextInput(StringBuffer buf)
      throws Exception
   {
      boolean done = false;
      StringBuffer local = new StringBuffer();
      while (!done)
      {
         String frag;
         synchronized (buf)
         {
            frag = buf.toString();
            buf.delete(0, buf.length());
         }
         System.out.print(frag);
         local.append(frag);
         Thread.sleep(1000);
         if (local.toString().toLowerCase().indexOf("press") > 0)
            done = true;
         System.out.print("X");
      }
   }

   
   
   /**
    * Buffer this many characters of output before starting truncation of
    * the output. Truncation is done as a fraction of this buffering count.
    */
   public static final int BUFFERING_COUNT = 10000;

   /**
    * An arbitrary return code that is used if the {@link #waitFor()} method
    * returns before the process has finished. The value is -998877, chosen
    * as unlikely to be a real return code.
    */
   public static final int RC_NOT_FINISHED = -998877;
   
   /**
    * Executes the specified command and arguments in a separate process with
    * the specified environment.
    * <p>
    * Given an array of strings <code>cmdarray</code>, representing the tokens
    * of a command line, and an array of strings <code>envp</code>,
    * representing "environment" variable settings, this method creates a new
    * process in which to execute the specified command.
    * <p>
    * If <code>envp</code> is <code>null</code>, the subprocess inherits the
    * environment settings of the current process.
    * <p>
    * The working directory of the new subprocess is specified by
    * <code>dir</code>. If <code>null</code>, the subprocess inherits the
    * current working directory of the current process.
    *
    * @param cmdarray array containing the command to call and its arguments,
    * may not be <code>null</code> or empty
    *
    * @param envp array of strings, each element of which has environment
    * variable settings in format name=value, may be <code>null</code> or empty
    *
    * @param dir the working directory of the process, or <code>null</code>
    * if the process should inherit the working directory of the current 
    * process
    *
    * @throws PSProcessException if any error occurs starting the process
    */
   public PSProcessAction(String[] cmdArray, String[] envp, File dir)
      throws PSProcessException
   {
      if ((cmdArray == null) || (cmdArray.length < 1))
         throw new IllegalArgumentException("cmd array may not be null or empty");

      m_cmdArray = cmdArray;
      m_envp = envp;
      m_dir = dir;

      m_status = new PSProcessStatus();
      m_thread = new Thread(this);

      m_thread.start();
   }

   /**
    * Creates the process and waits for its termination. 
    * 
    * @throws RuntimeException if exec'ing the process throws an exception.
    */
   public void run()
   {
      int status = PSProcessStatus.PROCESS_FAILED_TO_START;
      try
      {
         Log l = LogFactory.getLog(getClass());
         if (l.isDebugEnabled())
            l.debug("About to execute command: " + toString());
         m_process = Runtime.getRuntime().exec(m_cmdArray, m_envp, m_dir);

         // Set the status
         m_status.setStatus(PSProcessStatus.PROCESS_STARTED);

         if (m_process.getInputStream() != null)
         {
            m_stdOutputReader = new OutputReader(m_process.getInputStream(), 
                  m_stdOutBuf, m_cmdArray[0] + " Std Output Reader");
         }
         if (m_process.getErrorStream() != null)
         {
            m_stdErrorReader = new OutputReader(m_process.getErrorStream(), 
                  m_stdOutBuf, m_cmdArray[0] + " Error Output Reader");
         }

         // This call returns when the process has completed 
         status = PSProcessStatus.PROCESS_FINISHED;
         try
         {
            m_process.waitFor();
         }
         catch (InterruptedException e)
         {
            // This should never happen
            status = PSProcessStatus.PROCESS_INTERRUPTED;
         }
         
         if (status == PSProcessStatus.PROCESS_FINISHED)
         {
            m_exitValue = m_process.exitValue();
         }
      }
      catch (IOException ex)
      {
         throw new RuntimeException(ex.getLocalizedMessage());
      }
      finally
      {
         m_status.setStatus(status);
      }
   }

   /**
    * Returns the status of the process.
    *
    * @return one of <code>PSProcessStatus.PROCESS_XXX</code> values
    */
   public int getStatus()
   {
      return m_status.getStatus();
   }


   /**
    * Returns all text that has been read from the standard output stream of
    * the running process since the last time this method was called. 
    * <p>Note that at most, {@link #BUFFERING_COUNT} bytes are kept locally
    * before the buffer is truncated. If all output from the process is 
    * required, you may need to poll this method frequently.
    *
    * @return Never <code>null</code>, may be empty.
    * @see #getStdErrorText()
    */
   public String getStdOutText()
   {
      return getOutput(m_stdOutputReader, m_stdOutBuf);
   }

   /**
    * Returns all text that has been read from the standard error stream of
    * the running process since the last time this method was called. 
    * <p>Note that at most, {@link #BUFFERING_COUNT} bytes are kept locally
    * before the buffer is truncated. If all output from the process is 
    * required, you may need to poll this method frequently.
    * 
    * @return Never <code>null</code>, may be empty.
    * @see #getStdOutText()
    */
   public String getStdErrorText()
   {
      return getOutput(m_stdErrorReader, m_stdErrBuf);
   }
   
   /**
    * Returns the data in <code>content</code>, synchronizing on the object. 
    * If the process has finished (as indicated by <code>getStatus</code>),
    * the supplied thread will be <code>join</code>ed to make sure all output
    * has been read.
    * <p>Every time this call is made, the current output is returned, and 
    * the buffer is cleared.
    * <p>Note that at most, {@link #BUFFERING_COUNT} bytes are kept locally
    * before the buffer is truncated. If all output from the process is 
    * required, you may need to poll this method frequently.
    * 
    * @param reader The thread used to read content into the supplied buffer.
    * If the process is finished, the thread is joined for 1 second for any 
    * final processing it may need to do.
    * 
    * @param content Buffer that contains the data to return. The buffer is
    * cleared after the content is read. The read/clear is a synchronized 
    * operation.
    * 
    * @return the command output, never <code>null</code>, but may be empty.
    * The action of calling this method empties the stored output.
    * 
    * @throws IllegalStateException if the process has not been started.
    */
   private String getOutput(Thread reader, StringBuffer content)
   {
      if (m_status.getStatus() == PSProcessStatus.PROCESS_NOT_STARTED)
         throw new IllegalStateException("Process not running.");

      String rval;
      
      if (getStatus() == PSProcessStatus.PROCESS_FINISHED)
      {
         try
         {
            reader.join(1000);
         }
         catch (InterruptedException e)
         {
            // All waits can have this happen
         }
      }

      synchronized (content)
      {
         rval = content.toString();
         content.setLength(0);
      }
      return rval;
   }


   /**
    * Convenience method that calls {@link #waitFor(int) waitFor(0)}.
    */
   public int waitFor()
   {
      return waitFor(0);
   }

   /**
    * Returns the exit value for the process. This method waits for the
    * process thread to complete, and then obtains the return value.
    *
    * @param wait Waits at most this many milliseconds for the thread to
    * finish. A value of 0 means wait forever.
    * 
    * @return the exit value of the process. By convention, the value
    * <code>0</code> indicates normal termination. If this method returns 
    * before the process has completed, the value returned is 
    * <code>RC_NOT_FINISHED</code>.
    */
   public int waitFor(int millis)
   {
      try
      {
         // Wait indefinitely for the called process
         m_thread.join(millis);
      }
      catch (InterruptedException e)
      {
         // Ignore interrupts
      }

      return m_exitValue;
   }

   /**
    * Kills the subprocess. The process is forcibly terminated.
    *
    * @throws IllegalStateException if the process is not running.
    */
   public void destroy()
   {
      if (m_status.getStatus() != PSProcessStatus.PROCESS_STARTED)
         throw new IllegalStateException("Process not running.");

      m_process.destroy();
   }

   /**
    * Creates a string representation of the actual command being executed by 
    * this action, in the form 
    * <p>cmd p1 p2 p3 ...
    * <p>where pN is the nth parameter.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String toString()
   {
      StringBuffer buf = new StringBuffer(1000);
      for (int i = 0; i < m_cmdArray.length; i++)
      {
         buf.append(m_cmdArray[i]);
         if (i < m_cmdArray.length-1)
            buf.append(" ");
      }
      return buf.toString();
   }

   /**
    * Stores the command line arguments, initialized in the ctor, never
    * <code>null</code> or modified after that.
    */
   private String[] m_cmdArray = null;

   /**
    * Stores the environment settings, initialized in the ctor then never
    * changed, may be <code>null</code>. 
    */
   private String[] m_envp = null;

   /**
    * Stores the working directory, initialized in the ctor then never
    * changed, may be <code>null</code>.
    */
   private File m_dir = null;

   /**
    * Stores the process status, initialized in the ctor, never
    * <code>null</code>, modified in the <code>run</code> method.
    */
   private PSProcessStatus m_status = null;

   /**
    * Stores the handle to process, initialized in the <code>run</code>
    * method, never <code>null</code> or modified after that.
    */
   private Process m_process = null;

   /**
    * Stores the exit code of the process, initialized to 
    * <code>RC_NOT_FINISHED</code>, modified in the <code>run</code> 
    * method.
    */
   private int m_exitValue = RC_NOT_FINISHED;

   /**
    * Thread that is reading the standard output from the process. Initialized 
    * in the <code>run</code> method, never <code>null</code> or modified after 
    * that. The output read by this thread is stored in {@link #m_stdOutBuf}.
    */
   private Thread m_stdOutputReader = null;

   /**
    * Thread that is reading the standard error from the process. Initialized 
    * in the <code>run</code> method, never <code>null</code> or modified after 
    * that. The output read by this thread is stored in {@link #m_stdErrBuf}.
    */
   private Thread m_stdErrorReader = null;

   /**
    * String buffer to store output from the process from the time the thread
    * reads it until it is read by a call to {@link #getStdOutText()}. Never 
    * <code>null</code>. All accesses must be synchronized. 
    */
   private StringBuffer m_stdOutBuf = new StringBuffer();

   /**
    * String buffer to store output from the process from the time the thread
    * reads it until it is read by a call to {@link #getStdErrText()}. Never 
    * <code>null</code>. All accesses must be synchronized. 
    */
   private StringBuffer m_stdErrBuf = new StringBuffer();
   
   /**
    * Holds the execution thread for this process. Never <code>null</code>
    * after ctor.
    */
   private Thread m_thread;
}
/**
 * Reads data from a stream and writes it to a supplied buffer until the
 * stream is exhausted or exceptions. Originally designed for reading 
 * the std In and std Err streams associated with a <code>Process</code>. 
 */
class OutputReader extends Thread
{
   /**
    * Stores the supplied params for later use. The thread is set to be
    * a daemon thread.
    * 
    * @param source Data is read from this object until end of stream, or
    * an <code>IOException</code> occurs, at which time this thread exits.
    * Never <code>null</code>. The stream is closed when the thread exits.
    *
    * @param buf All data read from <code>source</code> is written to this
    * object. All writes are synchronized on the object itself. Never 
    * <code>null</code>.
    * 
    * @param name Use as the name for this thread. If <code>null</code> or
    * empty, "Input Reader" is used.
    */
   public OutputReader(InputStream source, StringBuffer buf, String name)
   {
      if ( null == source)
      {
         throw new IllegalArgumentException("source cannot be null");  
      }
      if ( null == buf)
      {
         throw new IllegalArgumentException("buf cannot be null");  
      }
      if (null == name || name.trim().length() == 0)
         name = "Input Reader";
      m_source = new BufferedInputStream(source);
      m_buffer = buf;
      setName(name);
      setDaemon(true);
      start();
   }
   
   /**
    * Attempts are made to read chunks of data at a time from the stream 
    * supplied in the ctor until the stream is exhausted. As each chunk is 
    * read, it is appended to the buffer supplied in the ctor. The stream
    * is read in 1000 byte chunks. If the buffer is longer than 
    * <code>BUFFERING_COUNT</code>, it is truncated before adding the new
    * data.
    */
   public void run()
   {
      int count = -1;
      try
      {
         do 
         {
            final int BUFSIZE = 1000;
            byte[] buffer = new byte[BUFSIZE];
            count = m_source.read(buffer);
            if (count > 0)
            {
               int BUFFERING_COUNT = 10000;
               //FB: ML_SYNC_ON_FIELD_TO_GUARD_CHANGING_THAT_FIELD NC 1-17-16
               synchronized(m_lock)
               {
                  if ((m_buffer.length() + count) > BUFFERING_COUNT)
                  {
                     int x = (m_buffer.length()+count)-BUFFERING_COUNT; 
                     m_buffer = new StringBuffer(m_buffer.substring(x,
                           m_buffer.length()));
                  }
                  m_buffer.append(new String(buffer, 0, count));
               }
            }
         }
         while (count > -1);
      }
      catch (IOException e)
      {
         //ignore, the thread is ending anyway
      }
      finally
      {
         try
         {
            m_source.close();
         }
         catch (IOException e) 
         { /* ignore */ }
      }
   }
   

   /**
    * The <code>source/code> parameter supplied in ctor. Never modified 
    * after construction.
    */
   private InputStream m_source;
   
   /**
    * The <code>buf</code> parameter supplied in ctor. Never 
    * <code>null</code> after construction.
    */
   private StringBuffer m_buffer;
   
   private final Object m_lock = new Object();
   
}

