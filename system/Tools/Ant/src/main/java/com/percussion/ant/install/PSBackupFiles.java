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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;

/**
 * PSBackupFiles creates backup of existing files and directory.
 * @see <code>getBackupFileName()</code> method to see how the name of the
 * backup files/directory is generated.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="backupFiles"
 *              class="com.percussion.ant.install.PSBackupFiles"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to backup one or more files.
 *
 *  <code>
 *  &lt;backupFiles ="C:/Rhythmyx/file1,C:/Rhythmyx/file2"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSBackupFiles extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      if (backupFiles.length == 0)
         return;

      String strRootDir = getRootDir();

      if (StringUtils.isBlank(strRootDir))
      {
         throw new BuildException("strRootDir may not be null or empty");
      }

      if (!(strRootDir.endsWith(File.separator)))
         strRootDir += File.separator;

      for (int i = 0; i < backupFiles.length; i++)
      {
         String srcFilePath = strRootDir + backupFiles[i];
         String destFilePath = strRootDir + getBackupFileName(backupFiles[i]);

         File srcFile = new File(srcFilePath);
         srcFilePath = srcFile.getAbsolutePath();

         File destFile = new File(destFilePath);
         destFilePath = destFile.getAbsolutePath();

         try
         {
            if (!srcFile.exists())
               continue;
            PSLogger.logInfo(
                  "Backing up file : " + srcFilePath +
                  " to file : " + destFilePath);
            srcFile.renameTo(destFile);
         }
         catch (Exception e)
         {
            PSLogger.logInfo(
                  "Exception while moving file : " + srcFilePath +
                  " to file : " + destFilePath);
            PSLogger.logInfo(e.getMessage());
            PSLogger.logInfo(e);
         }
      }
   }

   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/

   /**
    * Returns the files and directories which will be backed up.
    *
    * @return the files and directories which will be backed up,
    * never <code>null</code>, may be an empty array
    */
   public String [] getBackupFiles()
   {
      return backupFiles;
   }

   /**
    * Sets the files and directories which will be backed up.
    *
    * @param backupFiles the files and directories which will be
    * backed up, may be <code>null</code> or empty array
    */
   public void setBackupFiles(String backupFiles)
   {
      this.backupFiles = convertToArray(backupFiles);
   }

   /*************************************************************************
    * Public functions
    *************************************************************************/

   /**
    * Creates name of backup file/directory by appending current month, date,
    * hour and min to the specified file/directory name. The name of the
    * backup file is of the form "fileName_MMDD_HHMM"
    *
    * @param fileName name or path of the file/directory whose backup file name
    * is required, may not be <code>null</code> or empty
    *
    * @return the name of the backup file/direcory, never <code>null</code> or
    * empty
    */
   public static String getBackupFileName(String fileName)
   {
      if ((fileName == null) || (fileName.trim().length() < 1))
         throw new IllegalArgumentException(
         "fileName may not be null or empty");

      Calendar calendar = new GregorianCalendar();
      Date date = new Date();
      calendar.setTime(date);

      String suffix = "_";
      suffix += pad(calendar.get(Calendar.MONTH)+1);
      suffix += pad(calendar.get(Calendar.DAY_OF_MONTH));
      suffix += "_";
      suffix += pad(calendar.get(Calendar.HOUR_OF_DAY));
      suffix += pad(calendar.get(Calendar.MINUTE));

      return (fileName + suffix);
   }

   /*************************************************************************
    * Private functions
    *************************************************************************/

   /**
    * Returns the string value of the specified integer. If integer value is
    * less than 10, then prepends a "0" to the integer value.
    *
    * @param value the integer whose string value is required, assumed
    * non-negative
    *
    * @return the string value of the specified integer, never
    * <code>null</code> or empty, left padded with a single "0" if specifed
    * integer is single digit.
    */
   private static String pad(int value)
   {
      String ret = "" + value;
      if (ret.length() < 2)
         ret = "0" + value;
      return ret;
   }

   /*************************************************************************
    * Properties
    *************************************************************************/

   /**
    * stores the files and directories under the Rx install directory which
    * need to be backed up, never <code>null</code> may be an empty array
    */
   private String [] backupFiles = new String[0];

}

