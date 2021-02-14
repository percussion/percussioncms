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
import com.percussion.utils.tools.PSPatternMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * This is a product action task which extracts a jar file into
 * a given location. The jar file to extract should already be installed
 * on the destination system.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="extractJarFiles"
 *              class="com.percussion.ant.install.PSExtractJarFiles"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to extract a jar file.
 *
 *  <code>
 *  &lt;extractJarFiles destinationDir="C:/Rhythmyx"
 *                      filesToExtract="" jarFile="C:/jboss.zip"
 *                      neverReplaceFilter="file1,file2,file3"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSExtractJarFiles extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      try
      {
         File fDest = new File(destinationDir);
         if (!(fDest.exists() && fDest.isDirectory()))
            fDest.mkdirs();

         PSLogger.logInfo("Extracting jar file : " + jarFile + " to :" +
               destinationDir);

         List fileList = new ArrayList();
         try(JarFile jar = new JarFile(jarFile)) {
            if ((filesToExtract == null) || (filesToExtract.length == 0)) {
               for (Enumeration entries = jar.entries(); entries.hasMoreElements(); ) {
                  // Get the next entry.
                  JarEntry entry = (JarEntry) entries.nextElement();
                  String entryName = entry.getName();
                  if (!((entryName.endsWith(File.separator)) ||
                          (entryName.endsWith("/"))))
                     fileList.add(entryName);
               }
            } else {
               for (int i = 0; i < filesToExtract.length; i++)
                  fileList.add(filesToExtract[i]);
            }
         }

         byte[] buffer = new byte[1024];
         int bytesRead;

         for (int k=0; k < fileList.size(); k++)
         {
            String jarFileLoc = destinationDir + File.separator + fileList.get(k);
            File makeLocation = new File(jarFileLoc).getParentFile();
            makeLocation.mkdirs();
           try( JarFile jf = new JarFile(jarFile)) {
              File fJarFile = new File(jarFileLoc);

              if (fJarFile.exists() && fJarFile.isFile()) {
                 //see if we can replace this file?
                 String neverReplaceFilter[] = getNeverReplaceFilter();
                 boolean donotreplace = false;

                 for (int j = 0; j < neverReplaceFilter.length; j++) {
                    String pattern = neverReplaceFilter[j];

                    //if it matches the pattern - we can not replace it
                    PSPatternMatcher matcher =
                            PSPatternMatcher.FileWildcardMatcher(pattern);

                    String strFileName = fJarFile.getName();

                    if (matcher.doesMatchPattern(pattern, strFileName)) {
                       donotreplace = true;
                       break;
                    }
                 }

                 if (donotreplace && !PSAction.refreshFiles())
                    continue; //skip this file and go to the next jar entry

                 // delete the file if it exists, else the write operation may
                 // fail if the file is readonly.
                 fJarFile.delete();
              }

              if(!fJarFile.createNewFile())
                 throw new IOException("Unable to create file.");

              try(InputStream is = jf.getInputStream(jf.getEntry((String) fileList.get(k)))) {
                 try(FileOutputStream fos = new FileOutputStream(fJarFile)){
                    while ((bytesRead = is.read(buffer)) != -1) {
                       fos.write(buffer, 0, bytesRead);
                    }
                 }
              }
           }
            catch (Exception e)
            {
               PSLogger.logError("PSExtractJarFiles " + e.getMessage());
               PSLogger.logError(e);
            }
         }
      }
      catch (Exception e)
      {
         PSLogger.logError(e.getMessage());
         PSLogger.logError(e);
      }
   }



   /***************************************************************************
    * Bean properties
    ***************************************************************************/

   /**
    * Returns the absolute path of the jar file to extract.
    * @return the absolute path of the jar file to extract,
    * never <code>null</code> or empty
    */
   public String getJarFile()
   {
      return jarFile;
   }

   /**
    * Sets the absolute path of the jar file to extract.
    * @param jarFile the absolute path of the jar file to extract,
    * never <code>null</code> or empty
    * @throws IllegalArgumentException if jarFile is <code>null</code>
    * or empty
    */
   public void setJarFile(String jarFile)
   {
      if ((jarFile == null) || (jarFile.trim().length() < 1))
         throw new IllegalArgumentException(
         "jarFile may not be null or empty");
      this.jarFile = jarFile;
   }

   /**
    * Returns the absolute path of the directory in which to extract files.
    * @return the absolute path of the directory in which to extract files,
    * never <code>null</code> or empty
    */
   public String getDestinationDir()
   {
      return destinationDir;
   }

   /**
    * Sets the absolute path of the directory in which to extract files.
    * @param destinationDir the absolute path of the directory in which to
    * extract files, never <code>null</code> or empty
    */
   public void setDestinationDir(String destinationDir)
   {
      this.destinationDir = destinationDir;
   }

   /**
    * Returns the list of files to extract from the jar file.
    * @return the list of files to extract from the jar file, may be
    * <code>null</code> or empty.
    */
   public String[] getFilesToExtract()
   {
      return filesToExtract;
   }

   /**
    * Sets the list of files to extract from the jar file.
    * @param filesToExtract the list of comma separated files to extract from
    * the jar file, may be empty in which case all the files are extracted from
    * the jar file.
    */
   public void setFilesToExtract(String filesToExtract)
   {
      this.filesToExtract = convertToArray(filesToExtract);
   }

   /**
    * @return Returns the m_neverReplaceFilter.
    */
   public String[] getNeverReplaceFilter()
   {
      return m_neverReplaceFilter;
   }
   /**
    * @param replaceFilter The m_neverReplaceFilter to set.
    */
   public void setNeverReplaceFilter(String replaceFilter)
   {
      m_neverReplaceFilter = convertToArray(replaceFilter);
   }

   /**************************************************************************
    * properties
    **************************************************************************/

   /**
    * Path of the jar file to extract, never <code>null</code> or empty after
    * {@link #setJarFile(String)} is called.  The jar file should already be
    * installed on the destination system.
    */
   private String jarFile = "";

   /**
    * The directory in which to extract files, may be <code>null</code> or
    * empty, in which case the jar file is extracted in the directory where
    * it exists.
    */
   private String destinationDir = "";

   /**
    * List of files to extract, may be <code>null</code> or empty array, in
    * which case all the files are extracted from the jar file.
    */
   public String[] filesToExtract;

   /**
    * Never replace filter patterns.  May be <code>empty</code>,
    * never <code>null</code>.  Note, {@link PSAction#refreshFiles()} will
    * override this filter.
    */
   private String[] m_neverReplaceFilter = new String[0];
}





