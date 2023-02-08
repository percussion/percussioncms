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
import com.perforce.api.Env;
import com.perforce.api.PerforceException;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 *  &lt;taskdef name="p4packageupdate"
 *             class="com.percussion.ant.packagetool.PSP4PackageUpdate"
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
public class PSP4PackageUpdate
{

   /**
    * @param changelist the change list to be used cannot be <code>null</code> and
    * cannot be <code>empty</code>
    */

   public void setChange(String changelist)
   {
      m_changelist = changelist;
   }

   /**
    * @param path of the package. cannot be <code>null</code> and cannot be <code>empty</code>
    */
   public void setPackagePath(String packagePath)
   {
      m_packagePath = packagePath;
   }

   /**
    * @param envFile environment file to use. cannot be <code>null</code> and cannot be <code>empty</code>
    */
   public void setEnv(File envFile)
   {
      m_envFile = envFile;
   }

   /**
    * @param rootDirPath  cannot be <code>null</code> and cannot be <code>empty</code>
    */
   public void setRootDirPath(String dirPath)
   {
      this.m_rootDirPath = dirPath;
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

      if(m_changelist == null || m_changelist.trim().length() == 0 )
      {
         throw new BuildException("Change list cannot be null or empty.");
      }

      if( m_envFile == null)
      {
         throw new BuildException("Environment file can not be null.");
      }
      File pkgSrcDir = null;
      File pkgDestDir = null;
      List<File> newFiles = new ArrayList<File>();
      List<File> removedFiles = new ArrayList<File>();
      List<File> newFilesWithSourcePath = new ArrayList<File>();
      List<File> removedFilesWithSourcePath = new ArrayList<File>();
      try
      {
         // Make a temp directory
         pkgSrcDir = File.createTempFile("tmp", ".tmp");
         pkgSrcDir.delete();
         pkgSrcDir.mkdir();

         // Unzip package to temp directory
         Expand exp = new Expand();
         exp.setDest(pkgSrcDir);
         exp.setSrc(new File(m_packagePath));
         exp.execute();

         // Make another temp directory
         pkgDestDir = File.createTempFile("tmp", ".tmp");
         pkgDestDir.delete();
         pkgDestDir.mkdir();

         // Put package files in source like structure
         PSPackageBuildToolHelper.moveFilesToDestinationFolder(
                 pkgSrcDir, pkgDestDir);

         String destDirectoryPath = PSPackageBuildToolHelper.getDestinationDirectoryPath(m_packagePath, m_rootDirPath);
         // Get the source directory
         File srcPkgDir = new File(destDirectoryPath);

         // Find new files
         // List<File> newFiles =
         newFiles = PSPackageBuildToolHelper.findAddedFiles(srcPkgDir, pkgDestDir);

         if(newFiles.size() > 0)
         {
            newFilesWithSourcePath = PSPackageBuildToolHelper.getFilesWithSourcePath(newFiles,
                    srcPkgDir, pkgDestDir, false);
         }
         //
         // Find removed files
         //List<File> removedFiles =
         removedFiles  = PSPackageBuildToolHelper.findAddedFiles(pkgDestDir, srcPkgDir);

         if(removedFiles.size() > 0)
         {
            removedFilesWithSourcePath = PSPackageBuildToolHelper.getFilesWithSourcePath(removedFiles,
                    srcPkgDir, pkgDestDir, true);
         }

         try
         {
            // Copy all files to source folder
            FileUtils.copyDirectory(pkgDestDir,new File(destDirectoryPath));

            //Now copy the temp properties to the properties file in the source directory
            String tempDirPath = pkgDestDir.getPath();

            PSPackageBuildToolHelper.copyPropertiesFile(tempDirPath, destDirectoryPath);

            Env env = PSPerforceHelper.getEnv(m_envFile.getAbsolutePath());
            // Add new files to changelist
            for (File newFile : newFilesWithSourcePath)
            {
               PSPerforceHelper.add(env, newFile.getAbsolutePath(), m_changelist);
            }

            // Remove deleted files from changelist and source folder
            for (File removedFile : removedFilesWithSourcePath)
            {
               if (PSPerforceHelper.revert(env, removedFile.getAbsolutePath()))
               {
                  PSPerforceHelper.openForDelete(env, removedFile.getAbsolutePath(), m_changelist, false);
               }
            }
         }
         catch (PerforceException e)
         {
            throw new BuildException("Perforce Error: " + e.getMessage());
         }
      }
      catch (IOException e)
      {
         throw new BuildException("Failed to create temp directory for package", e);
      }
      finally
      {
         //Delete temp directories
         if(pkgSrcDir != null)
            pkgSrcDir.delete();

         if(pkgDestDir != null)
            pkgDestDir.delete();
      }
   }

   /**
    * Root directory path of all packages (required)
    */
   private String m_rootDirPath;

   /**
    * The perforce environment properties file (required)
    */
   private File m_envFile;

   /**
    * Path of the package to be updated (required)
    */
   private String m_packagePath;


   /**
    * The changelist number (required)
    */
   private String m_changelist;

}
