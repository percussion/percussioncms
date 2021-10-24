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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.util.IOTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Product Action task that copies a file. It supports the following properties:
 *
 * ignoreIfSourceNotExists - If any error should be logged if the source file
 * does not exist.
 * overwriteDestinationFile - If the destination file should be overwritten if
 * it already exists.
 * deleteSourceFile - If the source file should be deleted after the copy
 * action has been performed.
 * source -  Absolute path of the source file.
 * destination - Absolute path to which the source file should be copied.
 *
 * If source file does not exist - if <code>ignoreIfSourceNotExists</code>
 * property is <code>true</code> then simply returns, else prints error message
 * in the log file.
 * If the destination file already exists - if
 * <code>overwriteDestinationFile</code> property is <code>false</code> then
 * prints a message in the log file, else overwrites the destination file.
 * If the <code>deleteSourceFile</code> is <code>true</code> then deletes the
 * source file after it was successfully copied.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="copyFileAction"
 *              class="com.percussion.ant.install.PSCopyFileAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to copy a file.
 *
 *  <code>
 *  &lt;copyFileAction ignoreIfSourceNotExists="true"
 *                     overwriteDestinationFile="true" deleteSourceFile="true"
 *                     source="C:/Rhythmyx/file"
 *                     destination="C:/Rhythmyx/fileCopy"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSCopyFileAction extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      String resolvedSource = "";
      String resolvedDestination = "";
      try
      {
         if (destination.compareTo("") == 0)
         {
            //A destination file path was not specified, so go ahead and create
            //the destination file path incrementally from the source file path.
            resolvedSource = source;
            File tmpSrcFile = new File(resolvedSource);
            String tmpSrcFilePath = tmpSrcFile.getPath();
            String tmpSrcFileName = tmpSrcFile.getName();
            int indexName = tmpSrcFilePath.indexOf(tmpSrcFileName);
            String tmpSrcFileRoot = tmpSrcFilePath.substring(0,indexName);

            //This is a special case when we are backing up the cacerts file.
            if (tmpSrcFileName.compareTo("cacerts") == 0)
               tmpSrcFileRoot = tmpSrcFileRoot.substring(0,tmpSrcFileRoot.indexOf(
                  JRELOCATION));

            //Increment the destination file extension to current backup + 1, or
            //1 if this is the first time we are backing up.
            int i = 1;
            File tmpDestFile = new File(tmpSrcFileRoot + tmpSrcFileName + "." + i);

            while (tmpDestFile.exists()){
               i++;
               tmpDestFile = new File(tmpSrcFileRoot + tmpSrcFileName + "." + i);
            }

            resolvedDestination = tmpDestFile.getPath();
         }
         else
            resolvedDestination = destination;

         if (source.compareTo("") == 0)
         {
            //A source file path was not specified, so go ahead and create
            //the source file path incrementally from the destination file path.
            //This is only currently used to restore the cacerts file.
            File tmpSrcFile = new File(resolvedDestination);
            String tmpSrcName = tmpSrcFile.getName();
            String tmpSrcPath = tmpSrcFile.getPath();
            int indexName = tmpSrcPath.indexOf(JRELOCATION);
            String tmpSrcRoot = tmpSrcPath.substring(0,indexName);
            resolvedSource = tmpSrcRoot + tmpSrcName;

            //Look for the last cacerts backup file in order to copy
            //it back to the JRE\lib\security folder.
            int j = 1;
            File tmpSFile = new File(resolvedSource + "." + j);

            while (tmpSFile.exists()){
               j++;
               tmpSFile = new File(resolvedSource + "." + j);
            }

            if (j > 1)
               j = j - 1;

            resolvedSource = resolvedSource + "." + j;
            resolvedDestination = resolvedDestination + "." + j;
         }
         else
            resolvedSource = source;

         File srcFile = new File(resolvedSource);
         File destFile = new File(resolvedDestination);

         if ((!srcFile.isFile()) || (!srcFile.exists()))
         {
            if (ignoreIfSourceNotExists)
               return;
            else
            // Throw FileNotFoundException exception. It will be caught in the
            // catch bloack and written to the log file.
               throw new FileNotFoundException("Failed to copy file : " +
                   resolvedSource + "File does not exist.");
         }

         if (destFile.exists() && !overwriteDestinationFile)
         {
            PSLogger.logInfo(
               "Did not overwrite file because the file exists : " +
               resolvedDestination);
            return;
         }

         PSLogger.logInfo(
            "Copying : " + resolvedSource + " to " +
            resolvedDestination);
         if ( !destFile.exists() || destFile.canWrite())
            IOTools.copyFileStreams(srcFile, destFile);
         if (deleteSourceFile)
            srcFile.delete();
      }
      catch (IOException io)
      {
         PSLogger.logInfo("ERROR : " + io.getMessage() + "\n" +
            resolvedSource + "\n" + resolvedDestination);
         PSLogger.logInfo(io);
      }
   }

  /**************************************************************************
  * Bean property Accessors and Mutators
  **************************************************************************/

  /**
   * Sets the absolute path of the source file that should be copied.
   *
   * @param source the absolute path of the source file that should be copied,
   * should not be <code>null</code> or empty.
   */
   public void setSource(String source)
   {
      this.source = source;
   }

   /**
    * Returns the absolute path of the source file that should be copied.
    *
    * @return the absolute path of the source file that should be copied,
    * never <code>null</code>, may be empty.
    */
   public String getSource()
   {
      return source;
   }

  /**
   * Sets the absolute path of the destination file to which the source file is
   * copied.
   *
   * @param destination the absolute path of the destination file to which the
   * source file is copied, should not be <code>null</code> or empty
   */
   public void setDestination(String destination)
   {
      this.destination = destination;
   }

   /**
    * Returns the absolute path of the destination file to which the source file
    * is copied.
    *
    * @return the absolute path of the destination file to which the source file
    * is copied, never <code>null</code> or empty.
    */
   public String getDestination()
   {
      return destination;
   }

   /**
    * Sets the property for determining if the source file should be deleted
    * after copying.
    *
    * @param deleteSourceFile <code>true</code> if the source file will be
    * deleted after copying, otherwise <code>false</code>.
    */
   public void setDeleteSourceFile(boolean deleteSourceFile)
   {
      this.deleteSourceFile = deleteSourceFile;
   }

   /**
    * Returns the property for determining if the source file should be deleted
    * after copying.
    *
    * @return <code>true</code> if the source file will be deleted
    * after copying, otherwise <code>false</code>.
    */
   public boolean getDeleteSourceFile()
   {
      return deleteSourceFile;
   }

   /**
    * Sets the property for determining if the destination file should be
    * overwritten if it exists.
    *
    * @param overwriteDestinationFile <code>true</code> the destination file
    * will be overwritten if it exists, otherwise <code>false</code>.
    */
   public void setOverwriteDestinationFile(boolean overwriteDestinationFile)
   {
      this.overwriteDestinationFile = overwriteDestinationFile;
   }

   /**
    * Returns the property for determining if the destination file should be
    * overwritten if it exists.
    *
    * @return <code>true</code> if the destination file will be overwritten
    * if it exists, otherwise <code>false</code>.
    */
   public boolean getOverwriteDestinationFile()
   {
      return overwriteDestinationFile;
   }

   /**
    * Sets the property for printing error messages if the source file
    * does not exist.
    *
    * @param ignoreIfSourceNotExists <code>true</code> no error message
    * should be written to the log file if the source file does not exist,
    * otherwise <code>false</code>.
    */
   public void setIgnoreIfSourceNotExists(boolean ignoreIfSourceNotExists)
   {
      this.ignoreIfSourceNotExists = ignoreIfSourceNotExists;
   }

   /**
    * Returns the property for printing error messages if the source file
    * does not exist.
    *
    * @return <code>true</code>if no error message should be written to the log
    * file if the source file does not exist, otherwise <code>false</code>.
    */
   public boolean getIgnoreIfSourceNotExists()
   {
      return ignoreIfSourceNotExists;
   }


  /**************************************************************************
  * Bean properties
  **************************************************************************/

  /**
   * Absolute path of the source file that should be copied,
   * never <code>null</code>. This string is resolved at runtime using
   * Installshield's StringResolver methods.
   */
   private String source = "";

  /**
   * Absolute path of the destination file to which the source file is copied,
   * never <code>null</code>. This string is resolved at runtime using
   * Installshield's StringResolver methods.
   */
   private String destination = "";

   /**
    * If <code>true</code> the source file will be deleted after copying,
    * otherwise not. Default value is not to delete the source file.
    */
   private boolean deleteSourceFile = false;

   /**
    * If <code>true</code> and the destination file exists, will be overwritten
    * with the source.  If <code>false</code> and the destination file exists,
    * an exception message is printed in the log file. Default value is to
    * overwrite the destination file.
    */
   private boolean overwriteDestinationFile = true;

   /**
    * If <code>true</code> then simply returns if the source file does not
    * exist, otherwise prints error message in the log file. Default value
    * is to log error message if the source file does not exist.
    */
   private boolean ignoreIfSourceNotExists = false;

   /**************************************************************************
    * Static Variables
    *************************************************************************/

   /**
    * Default AppServer JRE location
    */
    static final private String JRELOCATION = "JRE";
}


