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
package com.percussion.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.perforce.api.Env;
import com.perforce.api.PerforceException;

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
