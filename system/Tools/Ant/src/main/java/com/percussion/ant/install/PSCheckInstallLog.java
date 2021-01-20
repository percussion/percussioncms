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

import com.percussion.util.IOTools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;


/**
 * An install action bean to check for errors in the installation log file.  If
 * any errors are found, a build exception will be thrown.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="checkInstallLog"
 *              class="com.percussion.ant.install.PSCheckInstallLog"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 * 
 * Now use the task to check the install log.
 * 
 *  <code>
 *  &lt;checkInstallLog/&gt;
 *  </code>
 * 
 * </pre>
 * 
 * @author peterfrontiero
 *
 */
public class PSCheckInstallLog extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      String rootDir = getRootDir();
      File installLog = new File(rootDir, INSTALL_LOG_FILE);
      File antLog = new File(rootDir, ANT_LOG_FILE);
      
      try
      {
         String fullcontents = IOTools.getFileContent(installLog);
         int index = fullcontents.lastIndexOf(START_ENTRY);
         if(index == -1){
           // The log may have been rotated. TODO: Look into how to detect log rotation so this is more valid.
            index = 0;
         }
         //Pull from the start entry or the start of the file.
         String contents = fullcontents.substring(index);
         String antcontents = null;
         
         if (antLog.exists())
         {
            antcontents = IOTools.getFileContent(antLog);
         }
         
         Iterator<String> iter = m_errorKeywords.iterator();
         while (iter.hasNext())
         {
            String check = iter.next();
            if (contents.indexOf(check) != -1)
            {
               throw new BuildException("An error was encountered during " +
                     "installation.  Please see " + installLog.getAbsolutePath() +
                    " for details.");
            }           
         }
         
         iter = m_errorAntKeywords.iterator();
         while (iter.hasNext())
         {
            String check = iter.next();
            if (antcontents != null && antcontents.indexOf(check) != -1)
            {
               throw new BuildException("An error was encountered during " +
                     "installation.  Please see " + antLog.getAbsolutePath() +
                     " for details.");
            }
         }
         
         iter = m_errorPackageInstall.iterator();
         while (iter.hasNext())
         {
            String check = iter.next();
            int matches = StringUtils.countMatches(antcontents, check);
            
            if (matches > 0)
            {
               throw new BuildException("An error was encountered. " +
                     "A total of " + matches + " packages received errors during " +
                     "installation. Please see " + antLog.getAbsolutePath() +
                     " for details.");
            }
         }
         
      }
      catch (IOException ioe)
      {
         throw new BuildException(ioe.getMessage());
      }
   }
   
   /**
    * Defines the set of keywords to search for as indication of errors.
    */
   private static Set<String> m_errorKeywords = new HashSet<String>();
   
   /**
    * Defines the set of keywords to search for as indication of errors.
    */
   private static Set<String> m_errorAntKeywords = new HashSet<String>();
   
   /**
    * Defines the set of keywords to search for as indication of 
    * Package install errors.
    */
   private static Set<String> m_errorPackageInstall = new HashSet<String>();
   
   /**
    * The relative location of the installation log file.
    */
   private static final String INSTALL_LOG_FILE =
      "rxconfig/Installer/install.log";
   
   /**
    * The relative location of the ant log file.
    */
   private static final String ANT_LOG_FILE =
      "rxconfig/Installer/ant.log";
   
   /**
    * Start log entry string
    */
   private static final String START_ENTRY =
      "Started Rx Log"; 
   
   static 
   {
      m_errorKeywords.add("ERROR");
      m_errorKeywords.add("Error");
      m_errorKeywords.add("Exception");
      m_errorAntKeywords.add("Exception");
      m_errorPackageInstall.add("finished with Errors");
   }
   
}
