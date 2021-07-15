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

package com.percussion.ant.packagetool;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Zip;

/**
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="pszippackage"
 *             class="com.percussion.ant.packagetool.PSZipPackage"
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
 *                packagename="perc.gadget.activity.ppkg"
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
public class PSZipPackage extends Zip
{


   public String getDestDirPath()
   {
      return destDirPath;
   }

   public void setDestDirPath(String destDirPath)
   {
      this.destDirPath = destDirPath;
   }


   /**
    * @param tempDestPath the tempDestPath to set
    */
   public void setTempPath1(String tempPath1)
   {
      this.tempPath1 = tempPath1;
   }

   /**
    * @param tempPath2 the tempPath2 to set
    */
   public void setTempPath2(String tempPath2)
   {
      this.tempPath2 = tempPath2;
   }

   /**
    * @param packagename the packagename to set
    */
   public void setPackageName(String packagename)
   {
      packageName = packagename;
   }

   /**
    * @param rootDirPath the rootDirPath to set
    */
   public void setRootDirPath(String rootDirPath)
   {
      this.rootDirPath = rootDirPath;
   }

   @Override
   public void execute() throws BuildException
   {
      if(rootDirPath == null ||  rootDirPath.trim().length() == 0)
         throw new BuildException("The package path can not be null or empty.");

      if(packageName == null || packageName.trim().length() == 0)
         throw new BuildException("The package name can not be null or empty.");

      if( destDirPath == null ||  destDirPath.trim().length() == 0)
         throw new BuildException("The destination directory path can not be null or empty.");

      //Get the directory name from packagename Ex: perc.widgets.image1.ppkg -> perc.widgets.image1
      String directoryName = packageName.substring(0, packageName.lastIndexOf('.'));

      try
      {
         //Get the properties file for this package
         Properties prop = PSPackageBuildToolHelper.getPropertiesFile(rootDirPath + File.separator + directoryName +
                 File.separator + directoryName);
         PSPackageBuildToolHelper.moveFilesToOriginalPaths(directoryName, tempPath1,
                 new File(tempPath1),tempPath2, prop);

         File sourceDirectory = new File(tempPath2);
         File destinationDirectory = new File(destDirPath + File.separator + directoryName+ ".ppkg");
         setBasedir(sourceDirectory);
         setDestFile(destinationDirectory);
         super.execute();
      }
      catch (IOException e)
      {
         throw new BuildException(e);
      }
   }

   /**
    * Root directory path for packages
    */
   private String rootDirPath;

   /**
    * Package name going to be zip up
    */
   private String packageName;

   /**
    * Temporary directory path
    */
   private String tempPath1;

   /**
    * Temporary directory path
    */
   private String tempPath2;

   /**
    * Destination directory path
    */
   private String destDirPath;
}  
