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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;

/**
 * Creates a changelist if a change number is not passed in and then
 * opens the specified folder for edit in the changelist created or
 * passed in.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="PSP4OpenFileForEdit"
 *             class="com.percussion.ant.PSP4OpenFileForEdit"
 *             classpath="c:\lib"/&gt;
 *  </code>
 * 
 * Now use the task to open a file creating a new changelist, the change
 * number will be put into the ${change.number} property.
 * 
 *  <code>  
 *  &lt;PSP4OpenFileForEdit property="change.number"
 *                         path="com/percussion/util/Version.properties"
 *                         env="c:/perforce.properties"
 *                         description="My new changelist"
 *                         lock="true"/&gt;
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
public class PSP4OpenFileForEdit extends Task
{
   
   /**
    * Sets the path of the file to be opened from the depot.
    * @param path the file path, cannot be <code>null</code> or
    * empty.
    */
   public void setPath(String path)
   {
      m_path = path;
   }
   
   /**
    * The changelist number of a changelist to be used, a new
    * changelist will be created if not specified.
    * @param change the changelist number, may be <code>null</code>.
    */
   public void setChange(String change)
   {
      m_change = change;
   }
   
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
    * Sets the lock flag indicating that the file should be locked
    * upon opening for edit.
    * @param lock
    */
   public void setLock(boolean lock)
   {
      m_lock = lock;
   }
   
   /**
    * Sets the description to be used if a new changelist
    * is created. May be <code>null</code>.
    */
   public void setDescription(String desc)
   {
      m_desc = desc;
   }
   
   /**
    * Sets the property name used to hold the changelist number.
    * @param prop the property name, may be <code>null</code>.
    */
   public void setProperty(String prop)
   {
      m_prop = prop;
   }
   
   /**
    * Creates a changelist if a change number is not passed in and then
    * opens the specified folder for edit in the changelist created or
    * passed in.
    */
   public void execute() throws BuildException
   {
      if(m_path == null || m_path.trim().length() == 0)
         throw new BuildException("The path cannot be null or empty.");
      if(!m_envFile.exists())
         throw new BuildException(
            "Perforce environment properties file does not exist.");   
     
     try
     {
        final Project proj = project;
        Env env = PSPerforceHelper.getEnv(m_envFile.getAbsolutePath());
        // Get new changelist if needed
        if(m_change == null || m_change.trim().length() == 0)
        {
           m_change = PSPerforceHelper.newChangeList(env, m_desc);
           log("Creating new changelist #" + m_change + ".");
        }
        else
        {
           log("Using existing changelist #" + m_change + ".");
        }
        log("Opening file for edit: (" + m_path + ").");
        PSPerforceHelper.openForEdit(env, m_path, m_change, m_lock);
        // Set change number property (this will violate the property
        // immutability rules).
        String propName = (m_prop == null || m_prop.trim().length() == 0) ?
           "p4.change" :
           m_prop;
        proj.setProperty(propName, m_change);      
     }
     catch(PerforceException e)
     {
        throw new BuildException("Perforce Error: " + e.getMessage());
     }
   }
   
   /**
    * The path for the file to be opened, should not be <code>null</code>.
    */
   private String m_path;
   
   /**
    * The changelist number (optional)
    */
   private String m_change;
   
   /**
    * The perforce environment properties file (required)
    */
   private File m_envFile;
   
   /**
    * Flag indicating that the file should be locked upon opening.
    * (optional)
    */
   private boolean m_lock;
   
   /**
    * Description used if a new changelist is created (optional)
    */
   private String m_desc;
   
   /**
    * The name of the property to be set with the changelist number,
    * defaults to "p4.change" if not specified. (optional)
    */
   private String m_prop;
   

}
