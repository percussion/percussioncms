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

package com.percussion.ant.packagetool;

import com.percussion.ant.PSPerforceHelper;
import com.perforce.api.Env;
import com.perforce.api.PerforceException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.perforce.P4Edit;

import java.io.File;

/**
 * Creates a changelist and
 * check out all files under the provided folder and adds to newly created change list.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="p4packageedit"
 *             class="com.percussion.ant.packagetool.PSP4Edit"
 *             classpath="c:\lib"/&gt;
 *  </code>
 *
 * Now use the task to open a file creating a new changelist, the change
 * number will be put into the ${change.number} property.
 *
 *  <code>
 *  &lt;PSP4Edit  path="system/packages"
 *                env="c:/perforce.properties"
 *                description="My new changelist"
 *                packagefodlername="perc.gadget.activity.ppkg"
 *                lock="false"/&gt;
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
 * p4.user=radharanisonnathi
 *
 * # P4CLIENT to use.
 * #
 * p4.client=rsonnathi-cml-projComments
 *
 * # P4PASSWD to use for the P4USER, if one has been set.
 * #
 * p4.password=password
 *
 *
 * </code>
 *
 *
 * </pre>
 */
public class PSP4Edit extends P4Edit
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
   public void setDescription(String description)
   {
      m_description = description;
   }


   /**
    * @param packageName the packageName to set
    */
   public void setPackageName(String packageName)
   {
      m_packageName = packageName;
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

      if(m_packageName == null || m_packageName.trim().length() == 0)
         throw new BuildException("The packagename cannot be null or empty.");

      if(!m_envFile.exists())
         throw new BuildException(
                 "Perforce environment properties file does not exist.");

      String packageFolderName = m_packageName.substring(0, m_packageName.lastIndexOf('.'));
      if(m_description == null || m_description.trim().length() == 0)
      {
         m_description = "Checking out the package folder " + packageFolderName ;
      }

      try
      {
         Env env = PSPerforceHelper.getEnv(m_envFile.getAbsolutePath());
         // Get new changelist
         m_change = PSPerforceHelper.newChangeList(env, m_description);

         //Directory for the package under the \system\packages folder
         String dirPath = m_path + File.separator + packageFolderName;

         String view = dirPath + File.separator + "...";
         //TODO for now this line is commented out. when work on the command linetarget this is will used
         //  PSPerforceHelper.checkOutSourceDirectory(env, view, m_change, m_lock);
         setView(dirPath + File.separator + "...");
         setLock(m_lock);
         setChange(m_change);
         setUser(env.getUser());
         setPort(env.getPort());
         setClient(env.getClient());
         super.execute();

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
    * The changelist number
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
   private String m_description;

   /**
    * Name of the package to be opened for edit
    */
   private String m_packageName;
}    


 