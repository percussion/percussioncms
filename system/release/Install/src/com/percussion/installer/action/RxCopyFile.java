/******************************************************************************
 *
 * [ RxCopyFile.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.util.IOTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * This action is used to copy files.
 */
public class RxCopyFile extends RxIAAction
{
   @Override
   public void execute()
   {
      try
      {
         setSource(getInstallValue(
               InstallUtil.getVariableName(
                     getClass().getName(),
                     SOURCE_FILE_VAR)));
         setDestination(getInstallValue(
               InstallUtil.getVariableName(
                     getClass().getName(),
                     DEST_FILE_VAR)));
         setDeleteSourceFile(getInstallValue(
               InstallUtil.getVariableName(
                     getClass().getName(),
                     DEL_SRC_FILE_VAR)).equalsIgnoreCase("true"));
         setIgnoreIfSourceNotExists(getInstallValue(
               InstallUtil.getVariableName(
                     getClass().getName(),
                     IGNORE_SRC_FILE_VAR)).equalsIgnoreCase("true"));
         setOverwriteDestinationFile(getInstallValue(
               InstallUtil.getVariableName(
                     getClass().getName(),
                     OVRWRTE_DEST_FILE_VAR)).equalsIgnoreCase("true"));
         
         if (m_destination.compareTo("") == 0)
         {
            //A destination file path was not specified, so go ahead and create
            //the destination file path incrementally from the source file path.
            File tmpSrcFile = new File(m_source);
            String tmpSrcFilePath = tmpSrcFile.getPath();
            String tmpSrcFileName = tmpSrcFile.getName();
            int indexName = tmpSrcFilePath.indexOf(tmpSrcFileName);
            String tmpSrcFileRoot = tmpSrcFilePath.substring(0,indexName);

            //This is a special case when we are backing up the cacerts file.
            if (tmpSrcFileName.compareTo("cacerts") == 0)
               tmpSrcFileRoot = tmpSrcFileRoot.substring(0,
                     tmpSrcFileRoot.indexOf(JRELOCATION));

            //Increment the destination file extension to current backup + 1, or
            //1 if this is the first time we are backing up.
            int i = 1;
            File tmpDestFile = new File(tmpSrcFileRoot + tmpSrcFileName + "." +
                  i);

            while (tmpDestFile.exists()){
               i++;
               tmpDestFile = new File(tmpSrcFileRoot + tmpSrcFileName + "." +
                     i);
            }

            m_destination = tmpDestFile.getPath();
         }
        
         if (m_source.compareTo("") == 0)
         {
            //A source file path was not specified, so go ahead and create
            //the source file path incrementally from the destination file path.
            //This is only currently used to restore the cacerts file.
            File tmpSrcFile = new File(m_destination);
            String tmpSrcName = tmpSrcFile.getName();
            String tmpSrcPath = tmpSrcFile.getPath();
            int indexName = tmpSrcPath.indexOf(JRELOCATION);
            String tmpSrcRoot = tmpSrcPath.substring(0,indexName);
            m_source = tmpSrcRoot + tmpSrcName;

            //Look for the last cacerts backup file in order to copy
            //it back to the JRE\lib\security folder.
            int j = 1;
            File tmpSFile = new File(m_source + "." + j);

            while (tmpSFile.exists()){
               j++;
               tmpSFile = new File(m_source + "." + j);
            }

            if (j > 1)
               j = j - 1;

            m_source = m_source + "." + j;
            m_destination = m_destination + "." + j;
         }
                 
         File srcFile = new File(m_source);
         File destFile = new File(m_destination);

         if ((!srcFile.isFile()) || (!srcFile.exists()))
         {
            if (m_ignoreIfSourceNotExists)
               return;
            else
            // Throw FileNotFoundException exception. It will be caught in the
            // catch bloack and written to the log file.
               throw new FileNotFoundException("Failed to copy file : " +
                   m_source + "File does not exist.");
         }
         
         if (destFile.exists() && !m_overwriteDestinationFile)
         {
            RxLogger.logInfo(
               "Did not overwrite file because the file exists : " +
               m_destination);
            return;
         }
         
         if ( !destFile.getParentFile().exists() )
            destFile.getParentFile().mkdirs();
         
         RxLogger.logInfo("Copying : " + srcFile.getAbsolutePath() + " to " +
               destFile.getAbsolutePath());
         IOTools.copyFileStreams(srcFile, destFile);
         if (m_deleteSourceFile)
            srcFile.delete();
      }
      catch (IOException io)
      {
         RxLogger.logInfo("ERROR : " + io.getMessage());
         RxLogger.logInfo(io);
      }
   }

   /**
    * Sets the absolute path of the source file that should be copied.
    *
    * @param source the absolute path of the source file that should be copied,
    * should not be <code>null</code> or empty.
    */
    public void setSource(String source)
    {
       m_source = source;
    }

    /**
     * Returns the absolute path of the source file that should be copied.
     *
     * @return the absolute path of the source file that should be copied,
     * never <code>null</code>, may be empty.
     */
    public String getSource()
    {
       return m_source;
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
       m_destination = destination;
    }

    /**
     * Returns the absolute path of the destination file to which the source
     * file is copied.
     *
     * @return the absolute path of the destination file to which the source
     * file is copied, never <code>null</code> or empty.
     */
    public String getDestination()
    {
       return m_destination;
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
       m_deleteSourceFile = deleteSourceFile;
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
       return m_deleteSourceFile;
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
       m_overwriteDestinationFile = overwriteDestinationFile;
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
       return m_overwriteDestinationFile;
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
       m_ignoreIfSourceNotExists = ignoreIfSourceNotExists;
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
       return m_ignoreIfSourceNotExists;
    }

   /**************************************************************************
   * Bean properties
   **************************************************************************/

   /**
    * Absolute path of the source file that should be copied,
    * never <code>null</code>. This string is resolved at runtime.
    */
    private String m_source = "";

   /**
    * Absolute path of the destination file to which the source file is copied,
    * never <code>null</code>. This string is resolved at runtime.
    */
    private String m_destination = "";

    /**
     * If <code>true</code> the source file will be deleted after copying,
     * otherwise not. Default value is not to delete the source file.
     */
    private boolean m_deleteSourceFile = false;

    /**
     * If <code>true</code> and the destination file exists, will be overwritten
     * with the source.  If <code>false</code> and the destination file exists,
     * an exception message is printed in the log file. Default value is to
     * overwrite the destination file.
     */
    private boolean m_overwriteDestinationFile = true;

    /**
     * If <code>true</code> then simply returns if the source file does not
     * exist, otherwise prints error message in the log file. Default value
     * is to log error message if the source file does not exist.
     */
    private boolean m_ignoreIfSourceNotExists = false;
    
    /**
     * The variable name for the source file parameter passed in via the IDE.
     */
    private static final String SOURCE_FILE_VAR = "source";
    
    /**
     * The variable name for the destination file parameter passed in via the
     * IDE.
     */
    private static final String DEST_FILE_VAR = "destination";
    
    /**
     * The variable name for the delete source file parameter passed in via the
     * IDE.
     */
    private static final String DEL_SRC_FILE_VAR = "deleteSourceFile";
    
    /**
     * The variable name for the overwrite destination file parameter passed in
     * via the IDE.
     */
    private static final String OVRWRTE_DEST_FILE_VAR =
       "overwriteDestinationFile";
    
    /**
     * The variable name for the ignore if source file doesn't exist parameter 
     * passed in via the IDE.
     */
    private static final String IGNORE_SRC_FILE_VAR = "ignoreIfSourceNotExists";
    
    /**
     * Default AppServer JRE location
     */
    static final private String JRELOCATION = "JRE";
}






