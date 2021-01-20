/******************************************************************************
 *
 * [ RxExecuteAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installanywhere.RxIAUtils;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * This action executes the specified executable during install.  The executable
 * may be bundled in the installer archive and is extracted to the target
 * machine and launched at install time, or it may already exist on the target
 * system.  The following properties are supported:
 *
 * m_bundleExecutable - Determines if the executable be bundled in the installer
 * archive.
 *
 * m_executable - the path of the executable.  This is resolved during build
 * time.
 *
 * m_arguments - arguments to be passed to the executable during install.
 *
 * m_workingDir - Working directory for the executable, resolved at runtime.
 * 
 * m_waitForCompletion - Determines whether to wait for the completion of
 * the launched process.
 *
 * m_exitCode - Exit code of the launched process. This exit code is valid only
 * if {@link #m_waitForCompletion} is <code>true</code>.
 *
 * m_stdoutDestination - path of the file to write to the std out of the
 * launched process, resolved at runtime.
 *
 * m_stderrDestination - path of the file to write to the std error of the
 * launched process, resolved at runtime.
 *
 * m_displayMsg - Message to be displayed when the process is being executed,
 * resolved at runtime.
 */

public class RxExecuteAction extends RxIAAction
implements Runnable
{
   /**
    * See this method of <code>com.percussion.installanywhere.RxIAAction</code>
    * for detailed information.
    * @see com.percussion.installanywhere.RxIAAction
    *
    * This method is called by the installer framework at runtime to
    * execute the custom action.  If {@link #m_bundleExecutable} is
    * <code>true</code> then it extracts the executable to a
    * temporary location, and executes it as a new process.  The extracted
    * executable is deleted when the installer exits.  If
    * {@link #m_bundleExecutable} is <code>false</code>, the executable
    * should exist on the target system.  The installer passes the
    * arguments specified by the {@link #m_arguments} property to the
    * executable.  It waits for the process to complete if the
    * {@link #m_waitForCompletion} property is <code>true</code>.
    */
   @Override
   public void execute()
   {
      setArguments(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), ARGUMENTS_VAR)));
      setWorkingDir(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), WORKING_DIR_VAR)));
      setStdoutDestination(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), STD_OUT_DEST_VAR)));
      setStderrDestination(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), STD_ERR_DEST_VAR)));
      setExecutable(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), EXECUTABLE_VAR)));
      setBundleExecutable(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), BUNDLE_EXECUTABLE_VAR)).
            equalsIgnoreCase("true"));
      setExitCode(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), EXIT_CODE_VAR)));
      setWaitForCompletion(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), WAIT_FOR_COMPLETION_VAR)).
            equalsIgnoreCase("true"));
      setDisplayMsg(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), DISPLAY_MSG_VAR)));
      
      if (!m_bundleExecutable)
         m_executable = resolveString(m_executable);
      else
         m_executable = RxIAUtils.fixupSourcePath(m_executable);
      
      if (!((m_arguments == null) || (m_arguments.length == 0)))
      {
         for (int i = 0; i < m_arguments.length; i++)
            m_arguments[i] = resolveString(m_arguments[i]);
      }
      
      m_workingDir = resolveString(m_workingDir);
      m_stdoutDestination = resolveString(m_stdoutDestination);
      m_stderrDestination = resolveString(m_stderrDestination);
            
      setProgressDescription(m_displayMsg);
      
      m_status = NOT_STARTED;
      if (!m_waitForCompletion)
      {
         Thread executeThread = new Thread(this);
         executeThread.setPriority(Thread.NORM_PRIORITY);
         executeThread.start();
         while (!m_processStatus.getProcessStarted())
         {
            try
            {
               Thread.sleep(300);
            }
            catch (InterruptedException ex)
            {
            }
         }
      }
      else
      {
         run();
      }
   }
   
   /***************************************************************************
    * Bean properties
    ***************************************************************************/
   
   /**
    * Returns <code>true</code> if the executable be bundled in the installer
    * archive, <code>false</code> otherwise.
    *
    * @return <code>true</code> if the executable be bundled in the installer
    * archive, <code>false</code> otherwise.
    */
   public boolean isBundleExecutable()
   {
      return m_bundleExecutable;
   }
   
   /**
    * Sets if the executable be bundled in the installer archive.
    *
    * @param bundleExecutable <code>true</code> if the executable be bundled in
    * the installer archive, <code>false</code> otherwise.
    */
   public void setBundleExecutable(boolean bundleExecutable)
   {
      m_bundleExecutable = bundleExecutable;
   }
   
   /**
    * Returns the executable that will be executed during install.
    *
    * @return the executable that will be executed during install, never
    * <code>null</code> or empty
    */
   public String getExecutable()
   {
      return m_executable;
   }
   
   /**
    * Sets the executable that will be executed during install.
    *
    * @param executable executable that will be executed during install, never
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if executable is <code>null</code>
    * or empty
    */
   public void setExecutable(String executable)
   {
      if ((executable == null) || (executable.trim().length() < 1))
         throw new IllegalArgumentException(
         "executable may not be null or empty");
      m_executable = executable.trim();
   }
   
   /**
    * Returns the command line arguments to the executable that will be executed
    * during install.
    *
    * @return the command line arguments to the executable that will be executed
    * during install, never <code>null</code>, may be an empty array.
    */
   public String[] getArguments()
   {
      return m_arguments;
   }
   
   /**
    * Sets the command line arguments to the executable that will be executed
    * during install.
    *
    * @param arguments the command line arguments to the executable that will
    * be executed during install, may be <code>null</code> or empty. If
    * <code>null</code> then set to empty array.
    */
   public void setArguments(String arguments)
   {
      m_arguments = RxIAUtils.toArray(arguments);
   }
   
   /**
    * Returns the working directory for the executable that will be executed
    * during install.
    *
    * @return the working directory for the executable that will be executed
    * during install, never <code>null</code>, may be empty.
    */
   public String getWorkingDir()
   {
      return m_workingDir;
   }
   
   /**
    * Sets the working directory for the executable that will be executed
    * during install.
    *
    * @param workingDir the working directory for the executable that will
    * be executed during install, may be <code>null</code> or empty. If
    * <code>null</code> then set to empty.
    */
   public void setWorkingDir(String workingDir)
   {
      if (workingDir == null)
         workingDir = "";
      m_workingDir = workingDir.trim();
   }
   
   /**
    * Returns whether to wait for the completion of the launched process.
    *
    * @return <code>true</code> if the installer should wait for the
    * completion of the launched process.
    */
   public boolean isWaitForCompletion()
   {
      return m_waitForCompletion;
   }
   
   /**
    * Sets whether to wait for the completion of the launched process.
    *
    * @param waitForCompletion <code>true</code> if the installer should wait
    * for the completion of the launched process, <code>false</code> otherwise.
    */
   public void setWaitForCompletion(boolean waitForCompletion)
   {
      m_waitForCompletion = waitForCompletion;
   }
   
   /**
    * Returns the exit code of the launched process.
    *
    * @return the exit code of the launched process.
    */
   public int getExitCode()
   {
      return ms_exitCode;
   }
   
   /**
    * Sets the exit code of the launched process.
    *
    * @param exitCode the exit code of the launched process.
    */
   public void setExitCode(String exitCode)
   {
      ms_exitCode = Integer.parseInt(exitCode);
   }
   
   /**
    * Returns the path of the file to which the standard output of the launched
    * process is written at install time.
    *
    * @return path of the file to which the standard output is
    * written at install time, may be <code>null</code> or empty.
    */
   public String getStdoutDestination()
   {
      return m_stdoutDestination;
   }
   
   /**
    * Sets the path of the destination file to which the standard output is
    * written at install time.
    *
    * @param stdoutDestination path of the file to which standard output is
    * written at runtime, may be <code>null</code> or empty.
    * If <code>null</code> or empty then std out is written to System.out
    */
   public void setStdoutDestination(String stdoutDestination)
   {
      m_stdoutDestination = stdoutDestination;
   }
   
   /**
    * Returns the path of the file to which the error output of the launched
    * process is written at install time.
    *
    * @return path of the file to which standard error output is written
    * at install time, may be <code>null</code> or empty.
    */
   public String getStderrDestination()
   {
      return m_stderrDestination;
   }
   
   /**
    * Sets the path of the destination file to which the error output is
    * written at install time.
    *
    * @param stderrDestination path of the file to which error output is
    * written at runtime, may be <code>null</code> or empty.
    * If <code>null</code> or empty then std error is written to System.out
    */
   public void setStderrDestination(String stderrDestination)
   {
      m_stderrDestination = stderrDestination;
   }
   
   /**
    * Returns the message to be displayed when the action is being executed
    * during install.
    *
    * @return the message to be displayed when the action is being executed
    * during install, never <code>null</code>, may be empty
    */
   public String getDisplayMsg()
   {
      return m_displayMsg;
   }
   
   /**
    * Sets the message to be displayed when the action is being executed
    * during install.
    *
    * @param displayMsg the message to be displayed when the action is being
    * executed during install, may be <code>null</code> or empty. If
    * <code>null</code> then set to empty.
    */
   public void setDisplayMsg(String displayMsg)
   {
      if (displayMsg == null)
         displayMsg = "";
      m_displayMsg = displayMsg.trim();
   }
   
   
   /**************************************************************************
    * private function
    **************************************************************************/
   
   /**
    * Starts the thread.
    * Executes the process and traps the process output.
    */
   public void run()
   {
      try
      {
         String[] commandArry = new String[m_arguments.length + 1];
         if (m_bundleExecutable)
            commandArry[0] = getProcessRuntimeLocation();
         else
            commandArry[0] = m_executable;
         
         RxLogger.logInfo("Executing process : " + commandArry[0]);
         
         for (int i=1; i < commandArry.length; i++)
         {
            commandArry[i] = m_arguments[i-1];
            RxLogger.logInfo("Param[" + i + "] = " + commandArry[i]);
         }
         
         Process process = null;
         if (m_workingDir.trim().length() < 1)
            process = Runtime.getRuntime().exec(commandArry);
         else
            process = Runtime.getRuntime().exec(commandArry, null,
                  new File(m_workingDir));
         
         m_status = STARTED;
         try
         {
            Thread.sleep(500);
         }
         catch(InterruptedException ie)
         {
         }
         if (m_processStatus != null)
            m_processStatus.setProcessStarted();
         if (m_waitForCompletion)
         {
            ProcessOutputThread pot =  new ProcessOutputThread(
                  process.getInputStream(), CAPTURE_OUTPUT);
            ProcessOutputThread pet = new ProcessOutputThread(
                  process.getErrorStream(), CAPTURE_ERROR);
            pot.start();
            pet.start();
            process.waitFor();
            pot.join();
            pet.join();
            ms_exitCode = process.exitValue();
            m_status = FINISHED;
         }
      }
      catch(Exception e)
      {
         RxLogger.logInfo("ERROR : " + e.getMessage());
         RxLogger.logInfo(e);
      }
   }
   
   /**
    * Returns the current state of the launched process.
    *
    * @return the status of the Process launched.
    * Possible values are -
    * <code>STARTED</code>
    * <code>NOT_STARTED</code>
    * <code>FINISHED</code>
    */
   public int fetchStatus()
   {
      return m_status;
   }
   
   /**
    * The process which is executed will terminate with an exit code.
    * 
    * @return the exit code returned by the executed process.
    */
   public static int fetchExitCode()
   {
      return ms_exitCode;
   }
   
   /**************************************************************************
    * private functions
    **************************************************************************/
   
   /**
    * Helper function which extracts the executable from the installer jar file.
    * It also sets the execute permissions on that file.
    *
    * @return the path of the executable. This is the path where the
    * executable bundled with the installer is extracted.
    * @throws IOException if it fails to extract the executable if it is
    * bundled with the installer.
    */
   private String getProcessRuntimeLocation() throws IOException
   {
      String localizedFile = RxIAFileUtils.normalizeFileName(m_executable);
      String executable = getResourceFile(localizedFile).getAbsolutePath();
      
      // set the file executable
      RxIAFileUtils.setFileExecutable(getFileService(), executable);
      
      return executable;
   }
   
   /**
    * This method handles the output data from the launched process.
    * It writes the output from the launched process to the destination file
    * that is specified in the stdoutDestination property.
    * <EM>Override this method to handle the output of the launched process
    * in your own way.<EM>
    *
    * @param ipStream Contains the output data of the launched process.
    */
   protected void processOutputData(InputStream ipStream)
   {
      writeData(ipStream, m_stdoutDestination);
   }
   
   /**
    * This method handles the std error out of the launched process.
    * It writes the std error from the launched process to the destination file
    * that is specified in stderrDestination property.
    * <EM>Override this method if you want to handle the std error of the launched
    * process in your own way.</EM>
    *
    * @param ipStream Contains the std error data of the launched process.
    */
   protected void processErrorData(InputStream ipStream)
   {
      writeData(ipStream, m_stderrDestination);
   }
   
   /**
    * Helper method to write the data from the InputStream to a file.
    *
    * @param ipStream Stream containing the data to be written. This function
    * closes this stream before returning.
    * @param destination Name of the file to which to write the data. May be
    * <code>null</code> or empty.
    */
   private void writeData(InputStream ipStream, String destination)
   {
      OutputStreamWriter osWriter = null;
      InputStreamReader reader = null;
      CharArrayWriter cWriter = null;
      
      try
      {
         if (!((destination == null) || (destination.trim().length() < 1)))
         {
            try
            {
               osWriter = createOutputStreamWriter(destination);
            }
            catch (Exception e)
            {
               osWriter = null;
               RxLogger.logInfo("ERROR : " + e.getMessage());
               RxLogger.logInfo(e);
            }
         }
         reader = new InputStreamReader(ipStream);
         char[] data = new char[1024];
         int count=0;
         cWriter = new CharArrayWriter();
         while ((count = reader.read(data, 0, data.length)) != -1)
            cWriter.write(data,0,count);
         
         if (osWriter != null)
            cWriter.writeTo(osWriter);
         else
            System.out.print(cWriter.toCharArray());
      }
      catch(Exception e)
      {
         RxLogger.logInfo("ERROR : " + e.getMessage());
         RxLogger.logInfo(e);
      }
      finally
      {
         if (osWriter != null)
         {
            try
            {
               osWriter.close();
            }
            catch (Exception e)
            {
            }
            osWriter = null;
         }
         if (cWriter != null)
         {
            try
            {
               cWriter.close();
            }
            catch (Exception e)
            {
            }
            cWriter = null;
         }
         if (reader != null)
         {
            try
            {
               reader.close();
            }
            catch (Exception e)
            {
            }
            reader = null;
         }
         if (ipStream != null)
         {
            try
            {
               ipStream.close();
            }
            catch (Exception e)
            {
            }
            ipStream = null;
         }
      }
   }
   
   /**
    * Creates the OutputStreamWriter from the file specified. If the destination
    * is null or empty, it creates the OutputStream with the System.out stream.
    *
    * @param destination the path of the file to which the
    * @return the stream writer for writing to the <code>destination</code>
    * file, may be <code>null</code> if any error occurs creating the stream
    * writer.
    */
   private OutputStreamWriter createOutputStreamWriter(String destination)
   {
      OutputStreamWriter osWriter = null;
      if (destination != null && destination.length() > 0)
      {
         destination = RxIAFileUtils.normalizeFileName(destination, '/');
         int index = destination.lastIndexOf("/");
         if (index != -1)
         {
            File file = new File(destination.substring(0, index+1));
            if (!file.isDirectory())
               file.mkdir();
            try
            {
               osWriter = new OutputStreamWriter(
                     new FileOutputStream(new File(destination)));
            }
            catch(Exception e)
            {
               osWriter = null;
               RxLogger.logInfo("ERROR : " + e.getMessage());
               RxLogger.logInfo(e);
            }
         }
      }
      return osWriter;
   }
   
   /**************************************************************************
    * Constants.
    **************************************************************************/
   
   /**
    * Executable not started.
    */
   private static final int NOT_STARTED = 0;
   
   /**
    * Executable started execution.
    */
   private static final int STARTED = 1;
   
   /**
    * Executable finished execution.
    */
   private static final int FINISHED = 2;
   
   /**
    * Constant that indicates the process io thread to capture the std output.
    * or std err.
    */
   private final int CAPTURE_OUTPUT = 0;
   
   /**
    * Constant that indicates the process io thread to capture the std err.
    */
   private final int CAPTURE_ERROR = 1;
   
   /**
    * The variable name for the executable parameter passed in via the IDE.
    */
   private static final String EXECUTABLE_VAR = "executable";
   
   /**
    * The variable name for the arguments parameter passed in via the IDE.
    */
   private static final String ARGUMENTS_VAR = "arguments";
   
   /**
    * The variable name for the standard error destination parameter passed in
    * via the IDE.
    */
   private static final String STD_ERR_DEST_VAR = "stderrDestination";
   
   /**
    * The variable name for the standard output destination parameter passed in
    * via the IDE.
    */
   private static final String STD_OUT_DEST_VAR = "stdoutDestination";
   
   /**
    * The variable name for the working directory parameter passed in via the
    * IDE.
    */
   private static final String WORKING_DIR_VAR = "workingDir";
   
   /**
    * The variable name for the bundle executable parameter passed in via the
    * IDE.
    */
   private static final String BUNDLE_EXECUTABLE_VAR = "bundleExecutable";
   
   /**
    * The variable name for the display message parameter passed in via the IDE.
    */
   private static final String WAIT_FOR_COMPLETION_VAR = "waitForCompletion";
   
   /**
    * The variable name for the exit code parameter passed in via the IDE.
    */
   private static final String EXIT_CODE_VAR = "exitCode";
   
   /**
    * The variable name for the display message parameter passed in via the IDE.
    */
   private static final String DISPLAY_MSG_VAR = "displayMsg";
   
   /**************************************************************************
    * properties
    **************************************************************************/
   
   /**
    * If <code>true</code>, bundles the executable with the installer.
    * Default value is not to bundle the executable.
    */
   private boolean m_bundleExecutable = false;
   
   /**
    * Executable to run, may not be <code>null</code> or empty. This is a
    * sample value. Actual value will be set through ide. This string
    * is resolved using string resolver methods before use during install.
    */
   private String m_executable = "";
   
   /**
    * Command line arguments passed to the executable, may be <code>null</code>
    * or empty. If <code>null</code> then set to empty array. This is a sample
    * value. Actual value will be set through Installshield. This string is
    * resolved using string resolver methods before use during install.
    */
   private String[] m_arguments = new String[0];
   
   /**
    * Working directory for the executable, may be <code>null</code>
    * or empty. This is a sample value. Actual value will be set through
    * ide. This string is resolved before use during install.
    */
   private String m_workingDir = "";
   
   /**
    * Determines if the installer should wait for the completion of
    * the launched process. Default value is to wait till the launched process
    * has exited.
    */
   private boolean m_waitForCompletion = true;
   
   /**
    * Exit code of the launched process. Default exit code is 0.
    */
   private static int ms_exitCode = 0;
   
   /**
    * Path of the file to write to the std out of the launched process.
    * If <code>null</code> or empty then std out is written to System.out
    */
   private String m_stdoutDestination = null;
   
   /**
    * Path of the file to write to std error of the launched process.
    * If <code>null</code> or empty then std error is written to System.err
    */
   private String m_stderrDestination = null;
   
   /**
    * Message to be displayed when the action is being executed, may be
    * <code>null</code> or empty. If <code>null</code> then set to empty.
    * This string is resolved using string resolver methods before use during
    * install.
    */
   private String m_displayMsg = "Running...";
   
   /**************************************************************************
    * private member variables
    **************************************************************************/
   
   /**
    * Used to ensure that this wizard action does not proceed until
    * the process has been fully started. This prevents a problem where
    * exec wizard actions at the end of a wizard tree do not fully execute
    * their process prior to the wizard exiting.
    */
   private ProcessStatus m_processStatus = new ProcessStatus();
   
   /**
    * Status of the executable.
    * Possible values:
    * <code>STARTED</code>
    * <code>NOT_STARTED</code>
    * <code>FINISHED</code>
    */
   private int m_status = NOT_STARTED;
   
   /**************************************************************************
    * Inner classes.
    **************************************************************************/
   
   /**
    * The thread that captures the std output and std error of the
    * executed process.
    */
   class ProcessOutputThread extends Thread
   {
      private int type = -1;
      private InputStream stream = null;
      
      /**
       * The Constructor that creates the Thread class.
       *
       * @param stream stream from which to read the input data.
       * @param type specifies the capturing of the output or error.
       * Possible values: {@link RxExecuteAction#CAPTURE_OUTPUT} and
       * {@link RxExecuteAction#CAPTURE_ERROR}.
       */
      public ProcessOutputThread(InputStream stream , int type)
      {
         this.stream = stream;
         this.type = type;
      }
      
      @Override
      public void run()
      {
         if (stream != null && (type == CAPTURE_OUTPUT))
            processOutputData(stream);
         else if (stream != null && (type == CAPTURE_ERROR))
            processErrorData(stream);
      }
   }
   
   /**
    * Class for storing the status of the process.
    */
   class ProcessStatus
   {
      /**
       * Stores the status of the process.
       */
      boolean processStarted = false;
      
      /**
       * Sets that the process has started running.
       */
      public synchronized void setProcessStarted()
      {
         this.processStarted = true;
      }
      
      /**
       * Returns if the process has started running.
       * @return <code>true</code> if the process has started running,
       * <code>false</code> otherwise.
       */
      public synchronized boolean getProcessStarted()
      {
         return processStarted;
      }
   }
   
}
