/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.ant;

import com.perforce.api.Env;
import com.perforce.api.PerforceException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;

/**
 * Syncs the client to the head revision.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="PSP4Sync"
 *             class="com.percussion.ant.PSP4Sync"
 *             classpath="c:\lib"/&gt;
 *  </code>
 * 
 *  * 
 *  <code>  
 *  &lt;PSP4Sync env="c:/perforce.properties"/&gt;
 * </code>
 * 
 * 
 * Example perforce properties file:
 * 
 * <code>
 * #
 * # Perforce P4 java API Properties
 * #
 *
 * # Full path to the P4 executable
 * #
 * p4.executable=C:\\Program Files\\Perforce\\P4.EXE
 *
 * # P4PORT to connect to.
 * #
 * p4.port=hera:1666
 *
 * # P4USER to run as.
 * #
 * p4.user=erikserating
 *
 * # P4CLIENT to use.
 * #
 * p4.client=erikserating-refactorBuild
 *
 * # P4PASSWD to use for the P4USER, if one has been set.
 * #
 * p4.password=secretpass
 *
 *
 * # SystemRoot and SystemDrive for a Windows machine. These are needed in order
 * # for the package to resolve hostnames. Why? I have no idea. Send questions to
 * # Bill.Gates@microsoft.com
 * #
 * p4.sysroot=C:\\WINDOWS
 * p4.sysdrive=C:
 *
 * </code>
 * 
 * 
 * </pre>
 */
@Deprecated
public class PSP4Sync extends Task
{
  
   /**
    * Sets the perforce environment properties file
    * @param file the perforce environment properties file,
    * cannot be <code>null</code>.
    */
   public void setEnv(File file)
   {
      m_envFile = file; 
   }
   
   /**
    * Sets the specific Perforce depot path to be synched
    * 
    * @param path the depot path. May be <code>null</code> or empty.
    */
   public void setPath(String path)
   {
      m_path = path;
   }
   
   /**
    * Syncs the client to the head revision
    */
   public void execute() throws BuildException
   {
      if(!m_envFile.exists())
         throw new BuildException(
            "Perforce environment properties file does not exist.");   
     
      try
      {
        
         Env env = PSPerforceHelper.getEnv(m_envFile.getAbsolutePath());
        
         if(m_path != null && m_path.trim().length() > 0)
         {
            log("Synching: " + m_path);
         }
         else
         {
            log("Synching client to head revision.");
         }
         
         PSPerforceHelper.syncClient(env, m_path);
             
      }
      catch(PerforceException e)
      {
         throw new BuildException("Perforce Error: " + e.getMessage());
      }
   }
     
   /**
    * The perforce environment properties file (required)
    */
   private File m_envFile;
   
   /**
    * The specific depot path to be synced   
    */
   private String m_path;
   

}
