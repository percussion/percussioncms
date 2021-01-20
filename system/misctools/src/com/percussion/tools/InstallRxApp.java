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

package com.percussion.tools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
*
* This is a simple utility class that installs a Rhythmyx application from a
* specified JAR file.
*
*/
public class InstallRxApp
{

   /**
   * Constructor that takes the JAR file that has all the application files. The
   * file name paths MUST be relative to the Rhythmyx root directory.
   *
   * @param JAR file (full path)
   *
   * @throws FileNotFoundException when the JAR file is not found.
   *
   * @throws IOException when the file could not be accessed.
   *
   */
   public InstallRxApp(String sJarFilePath)
      throws FileNotFoundException, IOException
   {
      File file = new File(sJarFilePath);
      m_JF = new JarFile(file);
      file = null;
   }

   /**
   * This method actually installs the applications. In the first part it copies
   * the application file 'ObjectStore/<appName.xml> to target directory. In the
   * later part, It copies all the application files (folder <appName>) to the
   * target directory.
   *
   * @param target Rhythmyx root directory as String. e.g. c:/Rhythmyx.
   *
   * @param application name as String. e.g. WFEditor in
   * (ObjectStore/WFEditor.xml)
   *
   */
   public void install(String sTargetRoot, String sAppName)
   {
      String sAppFilePath = "ObjectStore/" + sAppName + ".xml";

      ZipEntry entry = null;
      InputStream is = null;
      String name = sAppFilePath;

      entry = m_JF.getEntry(sAppFilePath);
      if(null == entry)
      {
         System.out.println("File <" + sAppFilePath +
            "> does not exist in the archive");

         return;
      }

      try
      {
         is = m_JF.getInputStream(entry);
         copyInputStreamToFile(is, sTargetRoot, name);
         is.close();
      }
      catch(IOException e)
      {
         System.out.println("Error: " + e.getMessage());
         return;
      }

      String appFilter = sAppName + "/";

      Enumeration e = m_JF.entries();

      while(e.hasMoreElements())
      {
         entry = (ZipEntry)e.nextElement();
         name = entry.getName();

         //skip all files with name starting with 'com/'!
         if(-1 == name.indexOf(appFilter))
            continue;

         System.out.println("Extracting file: " + name + "...");
         try
         {
            is = m_JF.getInputStream(entry);
            copyInputStreamToFile(is, sTargetRoot, name);
            is.close();
         }
         catch(IOException ioe)
         {
            System.out.println("Error: " + ioe.getMessage());
         }
      }
   }

   /**
   * This method copies the InputStream to the specified directory and file name.
   *
   * @param is - the InputStream to be written to the specified file.
   *
   * @param tgtRoot - the target root diectory
   *
   * @param fileName - the fileName to which the stream is to be written
   *
   * @throws - IOExceptione when the target file cannot be created or written.
   *
   */
   private void copyInputStreamToFile(InputStream is, String tgtRoot,
         String fileName)
      throws IOException
   {
      File file = new File(tgtRoot, fileName);
      File parent = file.getParentFile();
      if(null != parent && !parent.exists())
         parent.mkdirs();

      FileOutputStream fos = new FileOutputStream(file);
      byte[] buffer = new byte[1024];
      int nRead = -1;
	  	while(true)
      {
         nRead = is.read(buffer);
  			if (nRead < 0)
            break; //end of input stream
         fos.write(buffer, 0, nRead);
      }
      fos.flush();
      fos.close();
   }

   /**
   * Instanec of the jar file that has all Rx application files.
   */
   protected JarFile m_JF = null;

   /**
   * The main method that takes three command line parameters. The first one is
   * JAR file full path, second one is the Rhythmyx root directory name and the
   * last one is the name of application to be installed.
   *
   */
   public static void main(String[] args)
   {
      if(args.length < 3)
      {
         System.out.println("Usage:");
         System.out.println("InstallRxApp <JarFilePath> <RhythmyxRootDir> " +
            "<AppName>");
         System.out.println("Press ENTER to continue...");
         try{System.in.read();}catch(Exception ee){};
         System.exit(1);;
      }

      try
      {
         InstallRxApp installRxApp = new InstallRxApp(args[0]);
         installRxApp.install(args[1], args[2]);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
