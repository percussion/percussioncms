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

package com.percussion.ant.packagetool;

import com.percussion.ant.PSPerforceHelper;
import com.percussion.error.PSExceptionUtils;
import com.perforce.api.Env;
import com.perforce.api.PerforceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.perforce.P4Add;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

/**
 * Creates a changelist and 
 * marks all files under the provided folder for add and add those files to newly created change list.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>  
 *  &lt;taskdef name="p4packageadd"
 *             class="com.percussion.ant.packagetool.PSP4PackageAdd"
 *             classpath="c:\lib"/&gt;
 *  </code>
 *

 *  <code>  
 *  &lt;PSP4PackageAdd rootpath="system/packages"
 *                      env="c:/perforce.properties"
 *                      description="My new changelist"
 *                      packagepath="C:\directoryname\perc.gadget.activity.ppkg"
 *
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
@Deprecated
public class PSP4PackageAdd extends P4Add
{

   private static final Logger log = LogManager.getLogger(PSP4PackageAdd.class);

   /**
    * @param tempPath the tempPath to set
    */
   public void setTempPath(String tempPath)
   {
      m_tempPath = tempPath;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      m_description = description;
   }

   public void setEnv(File envFile)
   {
      m_envFile = envFile;
   }

   public void setPackagePath(String packagePath)
   {
      this.m_packagePath = packagePath;
   }

   public void setRootPath(String rootPath)
   {
      this.m_rootDirPath = rootPath;
   }

   public void execute() throws BuildException
   {
      if(m_packagePath == null || m_packagePath.trim().length() == 0)
         throw new BuildException("The package path cannot be null or empty.");
      if(!m_envFile.exists())
         throw new BuildException(
                 "Perforce environment properties file does not exist.");
      if(m_rootDirPath == null || m_rootDirPath.trim().length() == 0)
         throw new BuildException("The root directory path cannot be null or empty.");

      if(m_description == null || m_description.trim().length() == 0 )
      {
         m_description = "Adding a new package " + m_packagePath;
      }

      String destinationDirectory= PSPackageBuildToolHelper.getDestinationDirectoryPath(m_packagePath, m_rootDirPath);
      try{
         PSPackageBuildToolHelper.moveFilesToDestinationFolder(new File(m_tempPath), new File(destinationDirectory));
         Env env = PSPerforceHelper.getEnv(m_envFile.getAbsolutePath());
         m_changelist = PSPerforceHelper.newChangeList(env, m_description);
         /*
         //String view = destinationDirectory + File.separator + "*" ;
         PSPerforceHelper.addSourceDirectory(env, view, m_changelist);
         */
         setChangelist(Integer.parseInt(m_changelist));
         FileSet fileset = new FileSet();
         fileset.setDir(new File(destinationDirectory));
         addFileset(fileset);
         setUser(env.getUser());
         setPort(env.getPort());
         setClient(env.getClient());
         super.execute();

      }

      catch(PerforceException e)
      {
         throw new BuildException("Perforce Error: " + e.getMessage());
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

   /**
    * The perforce environment properties file (required)
    */
   private File m_envFile;

   /**
    * Package that needs to be added to the perforce.
    */
   private String m_packagePath;

   /**
    * Root directory path for packages
    */
   private String m_rootDirPath;

   /**
    * Description used if a new changelist is created (optional)
    */
   private String m_description;

   /**
    * Temporary directory path
    */
   private String m_tempPath;

   /**
    * The changelist created for the package files, set in {@link #execute()}.
    */
   protected String m_changelist;
}
