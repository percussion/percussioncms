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

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.perforce.api.Env;
import com.perforce.api.PerforceException;

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
