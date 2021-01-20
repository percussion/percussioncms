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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The process classes support a number of commands that can be executed 
 * locally or remotely (using the {@link PSProcessDaemon}. This interface 
 * abstracts out the basic methods and hides the implementation details. 
 *
 * @author paulhoward
 */
public interface IPSCommandHandler
{
   /**
    * Creates an {@link IPSProcess} based on the supplied name and executes it
    * within a context. The context includes the variables in the 
    * environment block of the exec.cfg file, and several properties from
    * the server's config (HOST, PORT and CONFIG_DIR), which take precedence
    * over similarly named variables in the exec.cfg.
    * <p>If the RW server is on a remote host, the request will be sent to
    * the process daemon on that host. A custom property named 'remoteport' 
    * will be expected and will be used with the search engine's host to
    * contact the daemon.
    *  
    * @param procName The name of the process as found in rw_processes.xml.
    * Never <code>null</code> or empty.
    * 
    * @param extraParams These params are combined with the default params
    * If there is a conflict, the supplied param is used. May be <code>null
    * </code> or empty.
    * 
    * @param wait How many milliseconds to wait for the process to finish. 
    * After this time has passed, if the process has not finished, the 
    * <code>terminate</code> flag is used to determine whether to forcibly end 
    * it. ;lt;0 is treated as 0. 0 means don't wait.
    * 
    * @param terminate If the <code>wait</code> period is exceeded before the
    * process ends, this flag controls what is done. If <code>true</code>,
    * the process will be forcibly terminated, otherwise it will be left
    * running after this method returns.
    *  
    * @return This can be used to obtain the console output and other status
    * information. The console output has both the stdout and stderr output
    * intermixed as it was received from the process. Never <code>null</code>.
    * 
    * @throws PSProcessException If the process fails to execute or the
    * process return code is not 0. If the return code is not 0, the exception
    * text includes the console output.
    */
   public PSProcessRequestResult executeProcess(String procName, 
      Map extraParams, int wait, boolean terminate)
      throws PSProcessException;
   
   /**
    * Method will not return until the process associated with the supplied
    * handle has finished.
    * 
    * @param handle The value obtained from {@link 
    * PSProcessRequestResult#getActionHandle()} previously obtained from a 
    * call to {@link #executeProcess(String, Map, int, boolean)} with 
    * <code>terminate</code> set to <code>false</code>. Must be > 0.
    *  
    * @param wait The number of milliseconds to wait for it to finish, at 
    * which point the process will be forcefully terminated. Less than 1 means 
    * wait forever.
    * 
    * @return Results based on the ended process. Never <code>null</code>.
    * 
    * @throws PSProcessException If the action associated with the handle
    * can not be found. 
    */
   public PSProcessRequestResult waitOnProcess(int handle, int wait)
      throws PSProcessException;
   
   /**
    * Performs the equivalent of {@link File#exists()} using the path in the
    * supplied <code>File</code>. 
    * 
    * @param path The location to check. Never <code>null</code>.
    * 
    * @throws PSProcessException if a communication problem occurs.
    */   
   public boolean fileSystemObjectExists(File path)
      throws PSProcessException;
   
   /**
    * Removes a file or directory from the file system. If a directory is 
    * supplied, it is removed recursively. If it doesn't exist, the method
    * silently returns. 
    * 
    * @param path Never <code>null</code>.
    * 
    * @throws PSProcessException if a file or directory cannot be removed. If
    * this happens, some files and directories could have been removed. Also
    * thrown if a communication problem occurs.
    */   
   public void removeFileSystemObject(File path)
      throws PSProcessException;

   /**
    * Creates all directories specified in the supplied path, if they don't 
    * exist. 
    * 
    * @param path Never <code>null</code>.
    * 
    * @throws PSProcessException If one of the directories cannot be created
    * or a communication problem occurs.
    */   
   public void makeDirectories(File path)
      throws PSProcessException;
   
   /**
    * Creates (or replaces) a file using the supplied path with the supplied
    * content. The content is written using the default encoding of the 
    * server on which it is written. 
    * 
    * @param path Never <code>null</code>.
    * 
    * @param content May be <code>null</code> or empty.
    * 
    * @throws IOException If any problems writing the file or 
    * communicating w/ the remote server.
    */
   public void saveTextFile(File path, String content)
      throws IOException;
   
   /**
    * Creates (or replaces) a file using the content supplied in 
    * <code>src</code>.
    * 
    * @param path Never <code>null</code>.
    * @param src May be <code>null</code>, in which case an empty file is
    * created. Takes ownership and closes when finished.
    * 
    * @throws IOException If any problems writing the file or 
    * communicating w/ the remote server.
    */
   public void saveBinaryFile(File path, InputStream src)
      throws IOException;
   
   /**
    * Reads a file's content and returns it. Assumes that the content is in 
    * the default encoding of the OS from which the file is read. 
    * 
    * @param path Never <code>null</code>.
    * 
    * @return The content read from the file. May be empty, never <code>null
    * </code>.
    * 
    * @throws IOException If the file doesn't exist, any problems reading the 
    * file or communicating w/ the remote client.
    */
   public String getTextFile(File path)
      throws IOException;
}
