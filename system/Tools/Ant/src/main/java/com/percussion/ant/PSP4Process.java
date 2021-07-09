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
package com.percussion.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Date;

import com.perforce.api.Debug;
import com.perforce.api.Env;
import com.perforce.api.EventLog;
import com.perforce.api.P4JNI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Handles the execution of all perforce commands. This class can be used
 * directly, but the preferred use of this API is through the
 * {@link com.perforce.api.SourceControlObject SourceControlObject} subclasses.
 * <p>
 * <b>Example Usage:</b>
 * <pre>
 *  String l;
 *  Env env = new Env();
 *  String[] cmd = { "p4", "branches"};
 *  try {
 *    P4Process p = new P4Process(env);
 *    p.exec(cmd);
 *    while (null != (l = p.readLine())) {
 *      // Parse the output.
 *    }
 *    p.close();
 *  } catch (Exception ex) {
 *    throw new PerforceException(ex.getMessage());
 *  }
 * </pre>
 *
 * @author <a href="mailto:david@markley.cc">David Markley</a>
 * @version $Date: 2002/01/15 $ $Revision: #3 $
 * @see Env
 * @see SourceControlObject
 * @see Thread
 */
public class PSP4Process
{

   private static final Logger log = LogManager.getLogger(PSP4Process.class);

   /**
    * Default no-argument constructor. If the runtime has not been established,
    * this constructor will set it up. No environment is specified, so the base
    * environment will be used if it exists.
    *
    * @see #getBase()
    */
   public PSP4Process()
   {
      this(null);
   }

   /**
    * Constructor that specifies the source control environment.
    *
    * @param e Source control environment to use.
    */
   public PSP4Process(Env e)
   {
      super();

      if (null == m_runTime)
      {
         m_runTime = Runtime.getRuntime();
      }

      if (null == e)
      {
         if (null == m_base)
         {
            m_base = this;
            this.m_environ = new Env();
         }
         else
         {
            this.m_environ = new Env(m_base.getEnv());
         }
      }
      else
      {
         this.m_environ = e;
      }

      if (null != m_environ)
      {
         this.m_threshold = m_environ.getServerTimeout();
      }
   }

   /**
    * Sets the environment to use.
    *
    * @param e Source control environment.
    */
   public void setEnv(Env e)
   {
      this.m_environ = e;

      if (null != m_environ)
      {
         this.m_threshold = m_environ.getServerTimeout();
      }
   }

   /**
    * Returns the environment in use by this process.
    *
    * @return Source control environment.
    */
   public Env getEnv()
   {
      return this.m_environ;
   }

   /**
    * Returns the base process for this class. The base process is set when
    * this class is first instantiated. The base process is used when other
    * <code>P4Process</code> are instantiated to share settings, including the
    * {@link com.perforce.api.Env source control environment}.
    *
    * @see Env
    * @return Source control environment.
    */
   public static PSP4Process getBase()
   {
      if (null != m_base)
      {
         return m_base;
      }
      else
      {
         return new PSP4Process();
      }
   }

   /**
    * Sets the base process to be used when new processes are instantiated.
    *
    * @see #getBase()
    */
   public static void setBase(PSP4Process b)
   {
      if (null != b)
      {
         m_base = b;
      }
   }

   /**
    * Returns the exit code returned when the underlying process exits.
    *
    * @return Typical UNIX style return code.
    */
   public int getExitCode()
   {
      return m_exitCode;
   }

   /**
    * In raw mode, the process will return the prefix added by the "-s" command
    * line option. The default is false.
    */
   public void setRawMode(boolean raw)
   {
      this.m_raw = raw;
   }

   /**
    * Returns the status of raw mode for this process.
    */
   public boolean getRawMode()
   {
      return this.m_raw;
   }

   /**
    * Executes a p4 command. This uses the class environment information
    * to execute the p4 command specified in the String array. This array
    * contains all the command line arguments that will be specified for
    * execution, including "p4" in the first position.
    *
    * @param cmd  Array of command line arguments ("p4" must be first).
    */
   public synchronized void exec(String[] cmd)
      throws IOException
   {
      String[] pre_cmds = new String[12];
      int i = 0;
      pre_cmds[i++] = cmd[0];
      pre_cmds[i++] = "-s"; //Forces all commands to use stdout for message reporting, no longer read stderr

      if (!getEnv().getPort().trim().equals(""))
      {
         pre_cmds[i++] = "-p";
         pre_cmds[i++] = getEnv().getPort();
      }

      if (!getEnv().getUser().trim().equals(""))
      {
         pre_cmds[i++] = "-u";
         pre_cmds[i++] = getEnv().getUser();
      }

      if (!getEnv().getClient().trim().equals(""))
      {
         pre_cmds[i++] = "-c";
         pre_cmds[i++] = getEnv().getClient();
      }

      if (!getEnv().getPassword().trim().equals(""))
      {
         pre_cmds[i++] = "-P";
         pre_cmds[i++] = getEnv().getPassword();
      }

      if (cmd[1].equals("-x"))
      {
         pre_cmds[i++] = "-x";
         pre_cmds[i++] = cmd[2];
      }

      m_newCmd = new String[(i + cmd.length) - 1];

      for (int j = 0; j < ((i + cmd.length) - 1); j++)
      {
         if (j < i)
         {
            m_newCmd[j] = pre_cmds[j];
         }
         else
         {
            m_newCmd[j] = cmd[(j - i) + 1];
         }
      }

      Debug.verbose("P4Process.exec: ", m_newCmd);

      if (P4JNI.isValid())
      {
         native_exec(m_newCmd);
         m_isUsingNative = true;
      }
      else
      {
         pure_exec(m_newCmd);
         m_isUsingNative = false;
      }
   }

   /**
    * Executes the command utilizing the P4API. This method will be used
    * only if the supporting Java Native Interface library could be loaded.
    */
   private synchronized void native_exec(String[] cmd)
      throws IOException
   {
      m_jniProc = new P4JNI();

      //    P4JNI tmp = new P4JNI();
      m_jniProc.runCommand(m_jniProc, cmd, m_environ);
      m_in = m_jniProc.getReader();
      m_err = m_in;
      m_out = m_jniProc.getWriter();
   }

   /**
    * Executes the command through a system 'exec'. This method will be used
    * only if the supporting Java Native Interface library could not be loaded.
    */
   private synchronized void pure_exec(String[] cmd)
      throws IOException
   {
      if (null != this.m_environ.getExecutable())
      {
         cmd[0] = this.m_environ.getExecutable();
      }

      m_process = m_runTime.exec(
            cmd,
            this.m_environ.getEnvp());

      final InputStream is = m_process.getInputStream();
      Debug.verbose("P4Process.exec().is: " + is);

      InputStreamReader isr = new InputStreamReader(is);
      Debug.verbose("P4Process.exec().isr: " + isr);
      m_in = new BufferedReader(isr);

      final InputStream es = m_process.getErrorStream();
      Debug.verbose("P4Process.exec().es: " + es);

      InputStreamReader esr = new InputStreamReader(es);
      Debug.verbose("P4Process.exec().esr: " + esr);
      m_err = new BufferedReader(esr);

      OutputStream os = m_process.getOutputStream();
      Debug.verbose("P4Process.exec().os: " + os);

      OutputStreamWriter osw = new OutputStreamWriter(os);
      Debug.verbose("P4Process.exec().osw: " + osw);
      m_out = new BufferedWriter(osw);
      Thread stdWriter = new Thread()
      {
         public void run()
         {
            try
            {
               // Read all the bytes from the stream to stop blocking
               while(is.read() != -1);
            }
            catch (Throwable t)
            {
               // can't do anything from this thread, so just write out
               // the exception
               log.error(t.getMessage());
               log.debug(t.getMessage(), t);
            }
         }
      };
      stdWriter.start();
      Thread errWriter = new Thread()
      {
         public void run()
         {
            try
            {
               // Read all the bytes from the stream to stop blocking
               while(es.read() != -1);               
            }
            catch (Throwable t)
            {
               // can't do anything from this thread, so just write out
               // the exception
               log.error(t.getMessage());
               log.debug(t.getMessage(), t);
            }
         }
      };
      errWriter.start(); 
           
      try
      {
         m_process.waitFor();
      }
      catch (InterruptedException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Sets the event log. Any events that should be logged will be logged
    * through the EventLog specified here.
    *
    * @param log  Log for all events.
    */
   public synchronized void setEventLog(EventLog log)
   {
      this.m_log = log;
   }

   /**
    * Writes <code>line</code> to the standard input of the process.
    *
    * @param line Line to be written.
    */
   public synchronized void print(String line)
      throws IOException
   {
      m_out.write(line);
   }

   /**
    * Writes <code>line</code> to the standard input of the process. A
    * newline is appended to the output.
    *
    * @param line Line to be written.
    */
   public synchronized void println(String line)
      throws IOException
   {
      m_out.write(line);
      m_out.newLine();
   }

   /**
    * Flushes the output stream to the process.
    */
   public synchronized void flush()
      throws IOException
   {
      m_out.flush();
   }

   /**
    * Flushes and closes the output stream to the process.
    */
   public synchronized void outClose()
      throws IOException
   {
      m_out.flush();
      m_out.close();
   }

   /**
    * Returns the next line from the process, or null if the command has
    * completed its execution.
    */
   public synchronized String readLine()
   {
      if (m_isUsingNative && (null != m_jniProc) && m_jniProc.isPiped())
      {
         return native_readLine();
      }
      else
      {
         return pure_readLine();
      }
   }

   /**
    * Reads the next line from the process. This method will be used
    * only if the supporting Java Native Interface library could be loaded.
    */
   private synchronized String native_readLine()
   {
      try
      {
         return m_in.readLine();
      }
      catch (IOException ex)
      {
         return null;
      }
   }

   /**
    * Reads the next line from the process. This method will be used
    * only if the supporting Java Native Interface library could not be loaded.
    */
   private synchronized String pure_readLine()
   {
      String line;
      long current;
      long timeout = ((new Date()).getTime()) + m_threshold;

      if ((null == m_process) || (null == m_in) || (null == m_err))
      {
         return null;
      }

      //Debug.verbose("P4Process.readLine()");
      try
      {
         for (;;)
         {
            if ((null == m_process) || (null == m_in) || (null == m_err))
            {
               Debug.error("P4Process.readLine(): Something went null");

               return null;
            }

            current = (new Date()).getTime();

            if (current >= timeout)
            {
               Debug.error("P4Process.readLine(): Timeout");

               // If this was generating a new object from stdin, return an
               // empty string. Otherwise, return null.
               for (int i = 0; i < m_newCmd.length; i++)
               {
                  if (m_newCmd[i].equals("-i"))
                  {
                     return "";
                  }
               }

               return null;
            }

            //Debug.verbose("P4Process.readLine().in: "+in);
            try
            {
               /** If there's something coming in from stdin, return it.
                *We assume that the p4 command was called with -s which sends all messages to standard out pre-pended with a string that
                *indicates what kind of messsage it is
                *error
                *warning
                *text
                *info
                *exit
                */

               // Some errors still come in on Standard error
               while (m_err.ready())
               {
                  line = m_err.readLine();

                  if (null != line)
                  {
                     addP4Error(line + "\n");
                  }
               }

               if (m_in.ready())
               {
                  line = m_in.readLine();
                  Debug.verbose("From P4:" + line);

                  if (line.startsWith("error"))
                  {
                     if (
                        !line.trim().equals("")
                           && (-1 == line.indexOf("up-to-date"))
                           && (-1 == line.indexOf("no file(s) to resolve")))
                     {
                        addP4Error(line);
                     }
                  }
                  else if (line.startsWith("warning")) {}
                  else if (line.startsWith("text")) {}
                  else if (line.startsWith("info")) {}
                  else if (line.startsWith("exit"))
                  {
                     int exit_code =
                        new Integer(
                           line.substring(
                              line.indexOf(" ") + 1,
                              line.length())).intValue();

                     if (0 == exit_code)
                     {
                        Debug.verbose("P4 Exec Complete.");
                     }
                     else
                     {
                        Debug.error("P4 exited with an Error!");
                     }

                     return null;
                  }

                  if (!m_raw)
                  {
                     line = line.substring(line.indexOf(":") + 1).trim();
                  }

                  Debug.verbose("P4Process.readLine(): " + line);

                  return line;
               }
            }
            catch (NullPointerException ne) {}

            // If there's nothing on stdin or stderr, check to see if the
            // process has exited. If it has, return null.
            try
            {
               m_exitCode = m_process.exitValue();

               return null;
            }
            catch (IllegalThreadStateException ie)
            {
               Debug.verbose("P4Process: Thread is not done yet.");
            }

            // Sleep for a second, so this thread can't become a CPU hog.
            try
            {
               Debug.verbose("P4Process: Sleeping...");
               Thread.sleep(100); // Sleep for 1/10th of a second.
            }
            catch (InterruptedException ie) {}
         }
      }
      catch (IOException ex)
      {
         return null;
      }
   }

   /**
    * Waits for the process to exit and closes out the process. This method
    * should be called after the {@link #exec(java.lang.String[]) exec}
    * method in order to close things down properly.
    *
    * @param out  The stream to which any errors should be sent.
    * @return The exit value of the underlying process.
    */
   public synchronized int close(PrintStream out)
      throws IOException
   {
      if (m_isUsingNative && (null != m_jniProc) && m_jniProc.isPiped())
      {
         native_close(out);
      }
      else
      {
         pure_close(out);
      }

      /* if (0 != exit_code) {
        throw new IOException("P4Process ERROR: p4 sync exited with error ("+
               exit_code+")");
        }*/
      if (null != m_P4Error)
      {
         throw new IOException(m_P4Error);
      }

      return m_exitCode;
   }

   /**
    * Closes down connections to the underlying process. This method will be
    * used only if the supporting Java Native Interface library could be loaded.
    */
   private synchronized void native_close(PrintStream out)
   {
      try
      {
         m_in.close();
         out.flush();
         out.close();
      }
      catch (IOException ioe) {}
   }

   /**
    * Closes down connections to the underlying process. This method will be
    * used only if the supporting Java Native Interface library could not be
    * loaded.
    */
   private synchronized void pure_close(PrintStream out)
   {
      /*
       * Try to close this process for at least 30 seconds.
       */
      for (int i = 0; i < 30; i++)
      {
         try
         {
            m_in.close();
            m_err.close();
            out.flush();
            out.close();
         }
         catch (IOException ioe) {}

         try
         {
            m_exitCode = m_process.waitFor();
            m_process.destroy();

            break;
         }
         catch (InterruptedException ie) {}

         try
         {
            Thread.sleep(1000);
         }
         catch (InterruptedException ie) {}
      }
   }

   /**
    * Waits for the underlying process to exit and closes it down. This method
    * should be called after the {@link #exec(java.lang.String[]) exec}
    * method in order to close things out properly. Errors are sent to
    * System.err.
    *
    * @see System
    * @return The exit value of the underlying process.
    */
   public int close()
      throws IOException
   {
      return close(System.err);
   }

   /**
    * Sets the P4USER in the class information.
    *
    * @see Env#setUser(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setUser(String) setUser(String)}
    * @param user  P4USER value.
    */
   public void setUser(String user)
   {
      this.m_environ.setUser(user);
   }

   /**
    * Returns the P4USER
    *
    * @see Env#getUser()
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getUser() getUser()}
    */
   public String getUser()
   {
      return this.m_environ.getUser();
   }

   /**
    * Sets the P4CLIENT in the class information.
    *
    * @see Env#getClient(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getClient(String) getClient(String)}
    * @param user  P4CLIENT value.
    */
   public void setClient(String client)
   {
      this.m_environ.setClient(client);
   }

   /**
    * Returns the P4CLIENT
    *
    * @see Env#getClient()
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getClient() getClient()}
    */
   public String getClient()
   {
      return this.m_environ.getClient();
   }

   /**
    * Sets the P4PORT in the class information.
    *
    * @see Env#setPort(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setPort(String) setPort(String)}
    * @param user  P4PORT value.
    */
   public void setPort(String port)
   {
      this.m_environ.setPort(port);
   }

   /**
    * Returns the P4PORT.
    *
    * @see Env#getPort()
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getPort() getPort()}
    */
   public String getPort()
   {
      return this.m_environ.getPort();
   }

   /**
    * Sets the P4PASSWD in the class information.
    *
    * @see Env#setPassword(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setPassword(String) setPassword(String)}
    * @param user  P4PASSWD value.
    */
   public void setPassword(String password)
   {
      this.m_environ.setPassword(password);
   }

   /**
    * Returns the P4PASSWORD.
    *
    * @see Env#getPassword()
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getPassword() getPassword()}
    */
   public String getPassword()
   {
      return this.m_environ.getPassword();
   }

   /**
    * Sets the PATH in the class information.
    *
    * @see Env#setPath(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setPath(String) setPath(String)}
    * @param user  PATH value.
    */
   public void setPath(String path)
   {
      this.m_environ.setPath(path);
   }

   /**
    * Returns the P4PATH.
    *
    * @see Env#getPath()
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getPath() getPath()}
    */
   public String getPath()
   {
      return this.m_environ.getPath();
   }

   /**
    * Sets the SystemDrive in the class information. This is only
    * meaningful under Windows.
    *
    * @see Env#setSystemDrive(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setSystemDrive(String) setSystemDrive(String)}
    * @param user  SystemDrive value.
    */
   public void setSystemDrive(String drive)
   {
      this.m_environ.setSystemDrive(drive);
   }

   /**
    * Sets the SystemRoot in the class information. This is only
    * meaningful under Windows.
    *
    * @see Env#setSystemRoot(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setSystemRoot(String) setSystemRoot(String)}
    * @param user  SystemRoot value.
    */
   public void setSystemRoot(String root)
   {
      this.m_environ.setSystemRoot(root);
   }

   /**
    * Sets up the path to reach the p4 executable. The full path passed in must
    * contain the executable or at least end in the system's file separator
    * character. This gotten from the file.separator property. For example:
    * <pre>
    * p4.executable=/usr/bin/p4   # This will work
    * p4.executable=/usr/bin/     # This will work
    * <font color=Red>p4.executable=/usr/bin      # This won't work</font>
    * </pre>
    *
    * @see Env#setExecutable(String)
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#setExecutable(String) setExecutable(String)}
    * @param exe  Full path to the p4 executable.
    */
   public void setExecutable(String exe)
   {
      this.m_environ.setExecutable(exe);
   }

   /**
    * Returns the path to the executable.
    * @see Env#getExecutable()
    * @deprecated  Replaced by {@link #getEnv() getEnv()}.{@link Env#getExecutable() getExecutable()}
    *
    */
   public String getExecutable()
   {
      return this.m_environ.getExecutable();
   }

   /** Set the server timeout threshold. */
   public void setServerTimeout(long threshold)
   {
      this.m_threshold = threshold;
   }

   /** Return the server timeout threshold. */
   public long getServerTimeout()
   {
      return m_threshold;
   }

   public String toString()
   {
      return this.m_environ.toString();
   }

   private void addP4Error(String message)
   {
      if (null == m_P4Error)
      {
         m_P4Error = message;
      }
      else
      {
         m_P4Error += message;
      }
   }
   
   private static PSP4Process m_base = null;
   private P4JNI m_jniProc = null;
   private boolean m_isUsingNative = false;
   private Env m_environ = null;
   private Runtime m_runTime = Runtime.getRuntime();
   private Process m_process;
   private BufferedReader m_in;
   private BufferedReader m_err;
   private BufferedWriter m_out;
   private int m_exitCode = 0;
   private EventLog m_log;
   private String m_P4Error = null;
   private String[] m_newCmd;
   private long m_threshold = 10000; // The default is 10 seconds;
   private boolean m_raw = false;
}
