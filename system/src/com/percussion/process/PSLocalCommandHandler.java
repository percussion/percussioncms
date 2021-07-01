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

import com.percussion.util.IOTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This class is the local implementation of the interface being implemented.
 * It executes all commands on the current machine.
 * <p>The real work is done by a series of methods named <code>doXXX</code>.
 * This was done so that these methods could be shared between this guy and
 * the {@link PSProcessDaemon}.
 * <p>If the feature that allows a process to keep running after the 
 * <code>execute</code> method returns is used, then a long-lived instance of
 * a using class must keep this class in memory. This is because this class
 * stores the <code>PSProcessAction</code> object for later access via a 
 * simple handle and if the class is not kept, it may be garbage collected and
 * the associated handle will become useless.
 *
 * @author paulhoward
 */
public class PSLocalCommandHandler implements IPSCommandHandler
{
   /**
    * All paths supplied to methods in this class will be modified based on
    * the supplied path. 
    * 
    * @param env See {@link #setEnvironment(Map)} for details. This method
    * calls <code>setEnvironment(env)</code>.
    * 
    * @param config The process definition file. See {@link PSProcessManager}
    * for details on the format.
    * 
    * @throws IOException If the config file can't be read for any reason.
    * @throws SAXException If the content of the config file is not well-formed
    * xml.
    * @throws PSProcessException If the content of the config file doesn't 
    * conform to dtd as specified in {@link PSProcessManager} description.
    */
   public PSLocalCommandHandler(Map env, File config)
      throws IOException, SAXException, PSProcessException
   {
      setEnvironment(env);
      InputStream pcStream = new FileInputStream(config);
      m_processManager = new PSProcessManager(pcStream);
   }

   /**
    * Sets the variable context for commands accessed through {@link 
    * #executeProcess(String, Map, int, boolean)}. 
    * 
    * @param env These variables are the starting point for the 
    * {@link #executeProcess(String, Map, int, boolean) executeProcess} method.
    * When executing a process, a map is created using these variables, then
    * the <code>extraParams</code> are added. The final result is used for the
    * process execution context.
    */
   public void setEnvironment(Map env)
   {
      m_environment = null == env ? new HashMap() : env;
   }

   //see base class method for details
   public PSProcessRequestResult executeProcess(
      String procName,
      Map extraParams,
      int wait,
      boolean terminate)
      throws PSProcessException
   {
      Map vars = new HashMap();
      vars.putAll(m_environment);
      // supplied vars have highest precedence
      if (null != extraParams)
         vars.putAll(extraParams);
      Logger sink = LogManager.getLogger(getClass());
      return doExecuteProcess(m_processManager, procName, vars, wait, 
            terminate, sink);
   }

   //see base class method for details
   public PSProcessRequestResult waitOnProcess(int handle, int wait) 
      throws PSProcessException 
   {
      return doWaitOnProcess(handle, wait);
   }

   //see base class method for details
   public boolean fileSystemObjectExists(File path) throws PSProcessException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      return path.exists();
   }

   //see base class method for details
   public void removeFileSystemObject(File path) throws PSProcessException
   {
      //let the called method perform the contract check on the param
      doRemoveFileSystemObject(path);
   }

   //see base class method for details
   public void makeDirectories(File path) throws PSProcessException
   {
      //let the called method perform the contract check on the param
      doMakeDirectories(path);
   }

   //see base class method for details
   public void saveTextFile(File path, String content)
      throws IOException
   {
      //let the called method perform the contract check on the param
      doSaveTextFile(path, content);
   }

   //see base class method for details
   public void saveBinaryFile(File path, InputStream content)
      throws IOException
   {
      doSaveBinaryFile(path, content);
   }

   //see base class method for details
   public String getTextFile(File path) throws IOException
   {
      //let the called method perform the contract check on the param
      return doGetTextFile(path);
   }

   /**
    * Does the real work for {@link #executeProcess(String, Map, int, boolean)}.
    * See that method for details and param descriptions not shown below.
    *  
    * @param mgr Used to obtain the action to execute. Never <code>null</code>.
    * 
    * @param params All the environment parameters for the action's execution.
    * Never <code>null</code>, may be empty.
    * 
    * @param outputSink Used to log <code>DEBUG</code> level information if
    * present. May be <code>null</code> if no logging desired. Spits out the
    * console output of the action every 1/2 second while waiting for the 
    * process to finish. The stdout and stderr outputs are both printed each
    * cycle.
    */
   static PSProcessRequestResult doExecuteProcess(
         PSProcessManager mgr,
         String procName,
         Map params,
         int wait,
         boolean terminate,
         Logger outputSink)
   {
      if ( null == mgr)
      {
         throw new IllegalArgumentException("mgr cannot be null");  
      }
      if ( null == procName || procName.trim().length() == 0)
      {
         throw new IllegalArgumentException("procName cannot be null or empty");  
      }
      if ( null == params)
      {
         throw new IllegalArgumentException("params cannot be null");  
      }
      
      int rc = -1;
      int resultStatus = PSProcessRequestResult.STATUS_ERROR;
      String resultText = "";
      int spawnedActionHandle = -1;
      try
      {
         Map vars = new HashMap();
         if (null != params)
            vars.putAll(params);

         IPSProcess proc = mgr.getProcess(procName);
         if (null == proc)
         {
            //throw and it will be handled below
            String[] args = 
            {
               procName   
            };
            throw new PSProcessException("Named process not found in config: " 
                  + procName);
         }
         PSProcessAction action = proc.start(vars);
         while(action.getStatus() == PSProcessStatus.PROCESS_NOT_STARTED)
         {
            try
            {
               if (action.getStatus() 
                     == PSProcessStatus.PROCESS_FAILED_TO_START)
               {
                  String[] args =
                  {
                     procName 
                  };
                  throw new PSProcessException("Failed to start: " + procName);
               }
               // Wait a little while before checking again
               Thread.sleep(100);
            }
            catch (InterruptedException e1)
            {
               // Ignore
            } 
         }
         
         if (wait <= 0)
            wait = 1;
         final int LOGGING_INTERVAL_MILLIS = 500; 
         int shortWait = 
               wait < LOGGING_INTERVAL_MILLIS ? wait : LOGGING_INTERVAL_MILLIS;
         int waitLeft = wait;
         StringBuffer consoleBuf = new StringBuffer(1000);
         do
         {
            if (waitLeft > 0)
            {
               waitLeft -= shortWait;
            }
            rc = action.waitFor(shortWait);
            String consoleFrag = null;
            consoleFrag = action.getStdOutText();
            consoleFrag += action.getStdErrorText();
            consoleBuf.append(consoleFrag);
            if (null != outputSink)
               outputSink.debug(consoleFrag);
         }
         while(waitLeft > 0 && (action.getStatus() !=
                                 PSProcessStatus.PROCESS_FINISHED));
         
         consoleBuf.append(action.getStdOutText());
         consoleBuf.append(action.getStdErrorText());
         int status = action.getStatus();
         if (status == PSProcessStatus.PROCESS_FINISHED)
         {
            //process ended
            resultStatus = PSProcessRequestResult.STATUS_FINISHED;
         }
         else if (status == PSProcessStatus.PROCESS_STARTED)
         {
            if (terminate)
            {
               // it didn't finish in the allotted time, shut it down
               action.destroy();
               resultStatus = PSProcessRequestResult.STATUS_TERMINATED;
            }
            else
            {
               //process spawn
               resultStatus = PSProcessRequestResult.STATUS_STARTED;
               spawnedActionHandle = storeAction(action);
            }
         }
         else
         {
            resultStatus = PSProcessRequestResult.STATUS_ERROR;
         }
         
         resultText = consoleBuf.toString().trim();
      }
      catch (Exception e)
      {
         resultText = e.getLocalizedMessage();
         if (null == resultText || resultText.trim().length() == 0)
            resultText = e.getClass().getName();
      }
      
      return new PSProcessRequestResult(procName, rc, resultText,
            resultStatus, spawnedActionHandle);
   }

   /**
    * See {@link #waitOnProcess(int, int)} for details.
    */
   static PSProcessRequestResult doWaitOnProcess(int handle, int wait)
      throws PSProcessException
   {
      PSProcessAction action = removeAction(handle);
      if (null == action)
      {
         throw new PSProcessException("No action found for handle " + handle);
      }
      if (wait < 0)
         wait = 0;
      
      int rc = action.waitFor(wait);
      String resultText = action.getStdOutText() 
            + "\r\nStd Error: "
            + action.getStdErrorText();
      int resultStatus = PSProcessRequestResult.STATUS_TERMINATED;
      
      if (rc != PSProcessAction.RC_NOT_FINISHED)
      {
         resultStatus = PSProcessRequestResult.STATUS_FINISHED;
      }
      else
         action.destroy();
      
      return new PSProcessRequestResult("handle " + handle, rc, resultText,
            resultStatus);      
   }

   /**
    * Allocates a new handle and stores the supplied action in the action map.
    * 
    * @param action Assumed not <code>null</code>.
    * 
    * @return The handle that can be used to retrieve the action using the
    * {@link #getAction(int)} method.
    */
   private static synchronized int storeAction(PSProcessAction action)
   {
      int nextHandle;
      nextHandle = ms_nextHandle++;
      ms_spawnedActions.put(new Integer(nextHandle), action);
      return nextHandle;
   }
   
   /**
    * Returns an action previously stored with 
    * {@link #storeAction(PSProcessAction)}.
    * 
    * @param handle A value returned by {@link #storeAction(PSProcessAction)}.
    * 
    * @return The originally stored action, or <code>null</code> if the 
    * handle is not in the map.
    */
   private static PSProcessAction getAction(int handle)
   {
      return (PSProcessAction) ms_spawnedActions.get(new Integer(handle));
   }

   /**
    * Removes the action associated with the supplied handle from the local 
    * map. 
    * 
    * @param handle Should be a value returned by the 
    * {@link #storeAction(PSProcessAction)} method.
    * 
    * @return The associated action, or <code>null</code> if there is not an
    * entry in the map for the handle.
    */
   private static PSProcessAction removeAction(int handle)
   {
      return (PSProcessAction) ms_spawnedActions.remove(new Integer(handle));
   }
   
   /**
    * This counter is used as a handle to a started action that is not 
    * terminated before the execute method returns.
    * <p>Use the current value and increment. Access must be synchronized.
    */
   private static int ms_nextHandle = 1;
   
   /**
    * This map stores actions for which handles are returned. The key of each
    * entry is an <code>Integer</code> whose value is the next available
    * value of <code>ms_nextHandle</code>. The value is the 
    * <code>PSProcessAction</code> that was spawned. Never <code>null</code>.
    */
   private static Map ms_spawnedActions = new HashMap();
   
   /**
    * Removes the file or directory specified by <code>path</code>. If
    * <code>path</code> is a directory, all files and directories in it 
    * are removed recursively. 
    * 
    * @param path Never <code>null</code>. Used as is, no transformation is 
    * done.
    * 
    * @return <code>null</code> if the specified object and all its 
    * children are successfully removed (or they don't exist), the supplied
    * path otherwise.
    */
   static void doRemoveFileSystemObject(File path) 
      throws PSProcessException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      if (path.exists())
      {
         File result = removeRecursive(path);
         if (null != result)
         {
            throw new PSProcessException(
               "Failed to remove file or directory identified by this path: "
               + result.getAbsolutePath());
         }
      }
   }

   /**
    * Removes the file or directory specified by <code>path</code>. If
    * <code>path</code> is a directory, all files and directories in it 
    * are removed recursively. 
    * 
    * @param path Assumed not <code>null</code>.
    * 
    * @return <code>null</code> if the specified object and all its 
    * children are successfully removed (or they don't exist), the supplied
    * path otherwise.
    */
   static private File removeRecursive(File path)
   {
      if (!path.exists())
         return null;
            
      if (path.isFile())
         path.delete();
      else
      {
         File[] files = path.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            if (null != removeRecursive(files[i]))
               return path;
         }
         path.delete();
      }
      return null;
   }

   /**
    * Creates all directories in path that don't yet exist. 
    * 
    * @param path Never <code>null</code>. Used as is, no transformation is 
    * done.
    * 
    * @throws PSProcessException If any directory can't be created.
    */
   static void doMakeDirectories(File path)
      throws PSProcessException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      boolean result = true;
      if (!path.exists())
         result = path.mkdirs();
      if (!result)
      {
         throw new PSProcessException(
               "Failed to create one or more directories: "
               + path.getAbsolutePath());
      }
   }

   /**
    * Saves file to OS, using the system's default encoding.
    * 
    * @param path Never <code>null</code>. Used as is, no transformation is 
    * done.
    * 
    * @param content What is stored in the file. May be <code>null</code> or 
    * empty.
    * 
    * @throws IOException If any problems writing to file.
    */
   static void doSaveTextFile(File path, String content)
      throws IOException
   {
      if (null == content)
         content = "";
      InputStream src = new ByteArrayInputStream(content.getBytes());
      saveFile(path, src);      
   }
   
   /**
    * Saves file to OS, using bytes read from the supplied stream.
    * 
    * @param path Never <code>null</code>. Used as is, no transformation is 
    * done.
    * 
    * @param content May be <code>null</code>, in which case an empty file
    * is created. Takes ownership of stream and closes when finished.
    * 
    * @throws IOException If any problems writing to file.
    */
   static void doSaveBinaryFile(File path, InputStream content)
      throws IOException
   {
      if (null == content)
         content = new ByteArrayInputStream(new byte[0]);
      saveFile(path, content);      
   }
   
   /**
    * Saves file to OS, using raw bytes.
    * 
    * @param path Assumed not <code>null</code>. Used as is, no transformation 
    * is done.
    * 
    * @param src What is stored in the file. Assumed not <code>null</code>.
    * Closes stream when finished.
    * 
    * @throws IOException If any problems writing to file.
    */
   static private void saveFile(File path, InputStream src)
      throws IOException
   {   
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }

      FileOutputStream fos = null;
      try
      {
         fos = new FileOutputStream(path);
         IOTools.copyStream(src, fos);
      }
      finally
      {
         if (fos != null)
         {
            FileDescriptor fd = fos.getFD();
            if (fd.valid())
            {
               fd.sync();
            }
         }
         close(fos);
         close(src);
      }
   }

   /**
    * Reads file from the OS, assuming the file content is in the system's 
    * default encoding.
    * 
    * @param path Never <code>null</code>. Used as is, no transformation is 
    * done.
    * 
    * @return The content of the file, never <code>null</code>.
    * 
    * @throws IOException If any problems reading from file.
    */
   static String doGetTextFile(File path)
      throws IOException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }

      InputStream fis = null;
      ByteArrayOutputStream bos = null;
      try
      {
         fis = new FileInputStream(path);
         bos = new ByteArrayOutputStream(2000);
         IOTools.copyStream(fis, bos);
         return new String(bos.toByteArray());
      }
      finally
      {
         close(fis);
         close(bos);
      }
   }

   /**
    * Closes the supplied stream, ignoring any exceptions.
    * 
    * @param o May be <code>null</code>. If not an <code>InputStream</code>, 
    * <code>OutputStream</code> or <code>Socket</code>, a 
    * <code>RuntimeException</code> is thrown.
    */
   static void close(Object o)
   {
      if (null == o)
         return;
      try
      {
         if (o instanceof InputStream)
            ((InputStream) o).close();
         else if (o instanceof OutputStream)
            ((OutputStream) o).close();
         else if (o instanceof Socket)
            ((Socket)o).close();
         else
            throw new RuntimeException("****** Unexpected stream *****");
      }
      catch (IOException e)
      {
         // ignore
      }
   }

   /**
    * Set during construction, then never <code>null</code> or modified after 
    * that. See ctor param <code>env</code> description for details.
    */
   private Map m_environment;
   
   /**
    * Set in ctor, then never <code>null</code> or modified after that. It is
    * used to fulfill requests to the {@link #executeProcess(String, Map, int, 
    * boolean) executeProcess} method.
    */
   private PSProcessManager m_processManager;
}
