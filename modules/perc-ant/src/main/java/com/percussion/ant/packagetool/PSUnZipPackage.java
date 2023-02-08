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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;

import java.io.File;

/**
 * Unzip the .ppkg to given source folder to given destiantion folder
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>  
 *  &lt;taskdef name="psunzippackage"
 *             class="com.percussion.ant.packagetool.PSUnZipPackage"
 *             classpath="c:\lib"/&gt;
 *  </code>
 *

 *  <code>  
 *  &lt;PSP4PackageAdd rootdirpath="system/packages"
 *                     zipfilepath="C:\directoryname\perc.gadget.activity.ppkg"
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
public class PSUnZipPackage extends Expand
{
   /**
    * The source package file path(required)
    */
   private String m_zipFilePath;

   /**
    * Root directory where package folder will go. It is required and comes from build.xml
    */
   private String m_rootDirPath;

   /**
    * @param zipFilePath the zipFilePath to set
    */
   public void setZipFilePath(String zipFilePath)
   {
      this.m_zipFilePath = zipFilePath;
   }

   /**
    * @param destinationDir the destinationDir to set
    */
   public void setRootDirPath(String rootDir)
   {
      this.m_rootDirPath = rootDir;
   }

   @Override
   public void execute() throws BuildException
   {
      if(m_zipFilePath == null || m_zipFilePath.trim().length() ==0)
         throw new BuildException("zipfilePath can not be null or empty.");

      if(m_rootDirPath == null || m_rootDirPath.trim().length() == 0)
         throw new BuildException("The rootDirPath cannot be null or empty.");

      String destinationDirectory= PSPackageBuildToolHelper.getDestinationDirectoryPath(m_zipFilePath, m_rootDirPath);
      // Unzips to the following temp folder later that gets deleted
      File tempDirectory= new File(m_rootDirPath + "/temp");
      setSrc(new File(m_zipFilePath));
      setDest(tempDirectory);
      super.execute();

      PSPackageBuildToolHelper.moveFilesToDestinationFolder(tempDirectory, new File(destinationDirectory));
   }

}
