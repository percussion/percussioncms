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
