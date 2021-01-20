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

package com.percussion.ant.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.helper.ProjectHelperImpl;

/**
 * Class that excepts and validates options for the rx build then builds a temp
 * batch file with the appropriate ant command line args. For patch builds, four
 * simple ant build files will be generated to handle the copying of patch
 * files, backup, install, and uninstall of the patch files. A configuration
 * file will also be generated for the patch describing the details of the
 * patch.
 */
public class PSRxBuildInput
{

   /**
    * Contructs a new Ui
    * 
    * @param batchfile the temporary batch file which will be used to launch the
    *           build, may not be <code>null</code> or empty.
    * @param libPath the library path specified by the Ant -lib command line
    *           parameter, may not be <code>null</code> or empty.
    * @param buildfile the build file which will include instructions for
    *           copying source files to the patch files build directory.
    * @param backupfile the build file which will include instructions for
    *           backing up original Rhythmyx files to be patched.
    * @param installfile the build file which will include instructions for
    *           installing the patch files.
    * @param uninstallfile the build file which will include instructions for
    *           uninstalling the patch files and restoring the originals.
    * @param rootDir the source root directory, may not be <code>null</code> or
    *           empty.
    * @param configfile the full path to the .cfg file should not be <code>null</code>
    */
   public PSRxBuildInput(String batchfile, String libPath, String buildfile, String backupfile, String installfile,
         String uninstallfile, String rootDir, String configfile, String readme, boolean obfuscation, boolean debug, boolean manufacture, boolean sync, boolean verbose
         )  
   {
      if (batchfile == null || batchfile.trim().length() == 0)
         throw new IllegalArgumentException("Batchfile cannot be null or empty.");

      if (libPath == null || libPath.trim().length() == 0)
         throw new IllegalArgumentException("LibPath cannot be null or empty.");

      if (buildfile == null || buildfile.trim().length() == 0)
         throw new IllegalArgumentException("Buildfile cannot be null or empty.");

      if (backupfile == null || backupfile.trim().length() == 0)
         throw new IllegalArgumentException("Backupfile cannot be null or empty.");

      if (installfile == null || installfile.trim().length() == 0)
         throw new IllegalArgumentException("Installfile cannot be null or empty.");

      if (uninstallfile == null || uninstallfile.trim().length() == 0)
         throw new IllegalArgumentException("Uninstallfile cannot be null or empty.");

      if (rootDir == null || rootDir.trim().length() == 0)
         throw new IllegalArgumentException("Rootdir cannot be null or empty.");

      if (configfile == null || configfile.trim().length() == 0)
         throw new IllegalArgumentException("Configfile cannot be null or empty.");

      if (readme == null || readme.trim().length() == 0)
         throw new IllegalArgumentException("Readme cannot be null or empty.");


      m_batchfile = batchfile;
      m_libPath = libPath;
      m_buildfile = buildfile;
      m_backupfile = backupfile;
      m_installfile = installfile;
      m_uninstallfile = uninstallfile;
      m_rootDir = rootDir;
      m_configfile = configfile;
      m_obfu = obfuscation;
      m_debug = debug; 
      m_manufacture = manufacture;
      m_sync = sync;
      m_verbose = verbose;
      m_readme = readme;

      File file = new File(m_batchfile);
      if (file.exists())
         file.delete();
      File file2 = new File(m_buildfile);
      if (file2.exists())
         file2.delete();
      File file3 = new File(m_backupfile);
      if (file3.exists())
         file3.delete();
      File file4 = new File(m_installfile);
      if (file4.exists())
         file4.delete();
      File file5 = new File(m_uninstallfile);
      if (file5.exists())
         file5.delete();
      File file6 = new File(m_configfile);
      if (!(file6.exists()))
      {
         throw new IllegalArgumentException("Config File does not exist.");
      }
      File file7 = new File(m_readme);
      if (!(file7.exists()))
      {
         throw new IllegalArgumentException("Readme File does not exist.");
      }

      loadConfigFile();

      init();

      try
      {
         writeBuildFile();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         System.exit(1);
      }

      executeTarget();
   }

   /**
    * Removes previous build of this patch if one exists. 
    * Creates or re-creates necesary folders
    * Copies over readme from patchresources
    */
   private void init()
   {
      File rootDirFile = new File(m_rootDir);
      File patchBaseDir = new File(rootDirFile.getParentFile(), "patch");
      patchBaseDir.mkdir();

      File patchDir = new File(patchBaseDir, m_patchId);
      if(patchDir.exists())
      {
         try
         {
            FileUtils.deleteDirectory(patchDir);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      patchDir.mkdir();

      File readme = new File(m_readme);
      try
      {
         FileUtils.copyFileToDirectory(readme, patchDir);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Sets the build properties and then executes the target
    */
   private void executeTarget()
   {
      if (!validate())
         return;

      FileWriter fw = null;
      StringBuffer buff = new StringBuffer();
      StringBuffer propertiesBuff = new StringBuffer();
      try
      {

         setEnviromentVar(propertiesBuff, "OPTIONALID", m_patchId);
         setEnviromentVar(propertiesBuff, "BUILDCOUNT", m_countText);
         setEnviromentVar(propertiesBuff, "BUILDDATE", m_dateText);
         setEnviromentVar(propertiesBuff, "BUILDTYPE", ms_type);
         setEnviromentVar(propertiesBuff, "DEBUG", m_debug);
         setEnviromentVar(propertiesBuff, "NO_OBFU", !m_obfu);

         if(m_manufacture)
            setEnviromentVar(propertiesBuff, "MANUFACTURE", true);

         if(!m_sync)
            setEnviromentVar(propertiesBuff, "NO.SYNC", true);

         buff.append("ant");
         buff.append(" -lib " + m_libPath);
         buff.append(" -listener org.apache.tools.ant.listener.Log4jListener");

         if(m_verbose) 
         {
            buff.append(" -v");
            setProperty(buff, "VERBOSE", true);
         }

         if(m_manufacture)
            setProperty(buff, "MANUFACTURE", true);
         if(!m_sync)
            setProperty(buff, "NO.SYNC", true);

         // Set properties accordingly
         setProperty(buff, "BUILDTYPE", ms_type);
         setProperty(buff, "BUILDDATE", m_dateText);
         setProperty(buff, "BUILDCOUNT", m_countText);
         setProperty(buff, "OPTIONALID", m_patchId);
         setProperty(buff, "NO_OBFU", !m_obfu);
         setProperty(buff, "DEBUG", m_debug);

         buff.append(" ");
         buff.append(ms_targetMap.get(ms_action));

         File file = new File(m_batchfile);
         fw = new FileWriter(file);
         fw.write(buff.toString());

         fw.close();

         File propertiesFile = new File(m_rootDir + "\\patch.properties");
         fw = new FileWriter(propertiesFile);
         fw.write(propertiesBuff.toString());

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            if (fw != null)
               fw.close();
         }
         catch (IOException ignore)
         {
         }
      }

   }

   /**
    * Writes the ant build file used to copy the additional non-obfuscated jars
    * to the temporary patch directory in order to build the patch zipfile. Also
    * writes the ant files used to install and remove files as part of the
    * patch. Generates a .cfg file with this information to reload when building
    * cumulative patches.
    * 
    * @throws IOException if an error occurs accessing a zip file.
    */
   private void writeBuildFile() throws IOException
   {
      String installContents = "";
      String buildContents = "";
      String backupContents = "";
      String uninstallContents = "";
      String mkdirContents = "";
      String copyContents = "";
      String cfgContents = "";
      Set mkdirs = new HashSet();

      // Build jar copy tasks
      ArrayList jarNames = new ArrayList();

      cfgContents += BUILD_DATE_BEGIN_TAG + "\n";
      cfgContents += m_dateText + "\n";
      cfgContents += BUILD_DATE_END_TAG + "\n";

      cfgContents += BUILD_COUNT_BEGIN_TAG + "\n";
      cfgContents += m_countText + "\n";
      cfgContents += BUILD_COUNT_END_TAG + "\n";

      cfgContents += PATCH_ID_BEGIN_TAG + "\n";
      cfgContents += m_patchId + "\n";
      cfgContents += PATCH_ID_END_TAG + "\n";

      cfgContents += ADDITIONAL_JARS_BEGIN_TAG + "\n";

      for (int i = 0; i < m_additionalJarsArrList.size(); i++)
      {
         String jarName = (String) m_additionalJarsArrList.get(i);

         jarNames.add(jarName);
         cfgContents += jarName + "\n";
      }
      cfgContents += ADDITIONAL_JARS_END_TAG + "\n";

      for (int i = 0; i < ms_requiredJars.length; i++)
         jarNames.add(ms_requiredJars[i]);

      for (int i = 0; i < jarNames.size(); i++)
      {
         String name = (String) jarNames.get(i);
         if (name.trim().length() == 0)
            continue;

         if (ms_jarMap.get(name) == null)
            throw new IllegalArgumentException("Cannot find " + name + " in jarMap.");

         String[] directories = ms_jarMap.get(name);
         for (int j = 0; j < directories.length; j++)
         {
            String directory = directories[j];

            // Don't add lib directory to old style patch
            if (!directory.equals("lib"))
            {
               mkdirs.add(directory);

               String file = "${libdir}/" + name;
               String tofile = "${outputdir}/patch/" + directory + "/" + name;
               String copyTask = "<copy file=\"" + file + "\" tofile=\"" + tofile + "\"/>";
               copyContents += copyTask + "\n";
            }

            backupContents += createBackupTask(name, directory);
            installContents += createInstallTask(name, directory, true, false);
            uninstallContents += createUninstallIfAvailableTask(name, directory, false);
         }

         buildContents += createBuildTask(LIBDIR + "/" + name, null);
      }

      // Build mkdir tasks
      Iterator iter = mkdirs.iterator();
      while (iter.hasNext())
      {
         String dir = "${outputdir}/patch/" + (String) iter.next();
         String mkdirTask = "<mkdir dir=\"" + dir + "\"/>";
         mkdirContents += mkdirTask + "\n";
      }

      // Build ant file
      String contents = "<project name=\"copypatchjars\" default=\"copy\" >\n" + "<target name=\"copy\">\n"
            + mkdirContents + "\n" + copyContents + "\n" + "</target>\n" + "</project>";

      // Build file copy tasks
      cfgContents += ADDITIONAL_FILES_BEGIN_TAG + "\n";
      for (int i = 0; i < m_addPatchFilesArrList.size(); i++)
      {
         String[] mapping = ((String) m_addPatchFilesArrList.get(i)).split("->");
         String src = FilenameUtils.separatorsToUnix(new File(m_rootDir,mapping[0]).getAbsolutePath());

         String loc;
         if (mapping.length == 2)
            loc = mapping[1];
         else
            loc = "";

         String name = (new File(src)).getName();

         // zip files need to be handled differently for backup, uninstall
         // DTS files for CM1 are handled directly in the deploy.xml
         // That is why we have a special case for it.
         if (name.endsWith(".zip") && (!name.toLowerCase().contains(DTS_ZIP) &&
               !name.toLowerCase().contains(DTS_APPLIB_ZIP)) )
         {
            backupContents += createZipBackupTask(src, loc);
            uninstallContents += createZipUninstallTask(src, loc);
         }
         else
         {
            backupContents += createBackupTask(name, loc);
            uninstallContents += createUninstallTask(name, loc, true);
         }

         installContents += createInstallTask(name, loc, false, true);
         buildContents += createBuildTask(src, loc);

         cfgContents += src + "->" + loc + "\n";
      }
      cfgContents += ADDITIONAL_FILES_END_TAG + "\n";

      // Build remove file tasks
      cfgContents += REMOVE_FILES_BEGIN_TAG + "\n";
      for (int i = 0; i < m_remPatchFilesArrList.size(); i++)
      {
         String fileLoc = (String) m_remPatchFilesArrList.get(i);
         File file = new File(fileLoc);
         String name = file.getName();
         File parent = file.getParentFile();
         String parentDir = "";
         if (parent != null)
            parentDir = FilenameUtils.separatorsToUnix(parent.getPath());

         installContents += createMoveTask(RHYTHMYX_HOME + "/" + fileLoc, PATCH_BACKUP + "/" + parentDir);
         uninstallContents += createUninstallTask(name, parentDir, true);
         cfgContents += fileLoc + "\n";
      }
      cfgContents += REMOVE_FILES_END_TAG + "\n";

      // Build build files ant file
      String buildFileContents = createProject("patchFiles", "copyFiles", buildContents);

      // Build backup files ant file
      backupContents = "<mkdir dir=\"" + PATCH_BACKUP + "\"/>\n" + backupContents;
      String backupFileContents = createProject("backupFiles", "backup", backupContents);

      // Build install files ant file
      String installFileContents = createProject("installFiles", "install", installContents);

      // Build uninstall files ant file
      String uninstallFileContents = createProject("uninstallFiles", "uninstall", uninstallContents);

      // Write the files
      writeBuildFile(m_buildfile, buildFileContents);
      writeBuildFile(m_backupfile, backupFileContents);
      writeBuildFile(m_installfile, installFileContents);
      writeBuildFile(m_uninstallfile, uninstallFileContents);

      File rootDirFile = new File(m_rootDir);
      File patchBaseDir = new File(rootDirFile.getParentFile(), "patch");
      String patchId = m_patchId;
      File patchDir = new File(patchBaseDir, patchId);

      writeBuildFile(patchDir + "/" + patchId + ".cfg", cfgContents);
      writeBuildFile(m_rootDir + File.separator + "copyPatchJars.xml", contents);
   }

   /**
    * Set a enviroment's variable if the value passed in is not <code>
    * null</code> or empty.
    * 
    * @param buff the stringbuffer to use, cannot be <code>null</code>.
    * @param var the name of the property, cannot be <code>null</code> or
    *           empty.
    * @param value the value to assign, can be <code>null</code> or empty.
    * 
    */
   private void setEnviromentVar(StringBuffer buff, String var, String value)
   {
      if (buff == null)
         throw new IllegalArgumentException("Project cannot be null.");
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("Property name cannot be null or empty.");
      if (value == null || value.trim().length() == 0)
         return;
      buff.append(var + " = " + value + '\n');
   }

   /**
    * Set a enviroment's variable if the value passed in is <code>true</code>.
    * 
    * @param buff the stringbuffer to use, cannot be <code>null</code>.
    * @param var the name of the property, cannot be <code>null</code> or
    *           empty.
    * @param value the boolean value, the property will only be set if this is
    *           <code>true</code>
    * 
    */
   private void setEnviromentVar(StringBuffer buff, String var, boolean value)
   {
      if (value)
         setEnviromentVar(buff, var, "true");
   }

   /**
    * Set a project's property if the value passed in is not <code>
    * null</code> or empty.
    * 
    * @param buff the stringbuffer to use, cannot be <code>null</code>.
    * @param prop the name of the property, cannot be <code>null</code> or
    *           empty.
    * @param value the value to assign, can be <code>null</code> or empty.
    * 
    */
   private void setProperty(StringBuffer buff, String prop, String value)
   {
      if (buff == null)
         throw new IllegalArgumentException("Project cannot be null.");
      if (prop == null || prop.trim().length() == 0)
         throw new IllegalArgumentException("Property name cannot be null or empty.");
      if (value == null || value.trim().length() == 0)
         return;
      buff.append(" -D" + prop + "=" + value);
   }

   /**
    * Set a project's property if the value passed in is <code>true</code>.
    * 
    * @param buff the stringbuffer to use, cannot be <code>null</code>.
    * @param prop the name of the property, cannot be <code>null</code> or
    *           empty.
    * @param value the boolean value, the property will only be set if this is
    *           <code>true</code>
    * 
    */
   private void setProperty(StringBuffer buff, String prop, boolean value)
   {
      if (value)
         setProperty(buff, prop, "true");
   }

   /**
    * Validate the user input fields
    * 
    * @return <code>true</code> if fields are valid
    */
   private boolean validate()
   {

      boolean isValid = true;

      m_patchId = modifyPatchId(m_patchId);

      if (m_patchId.trim().length() == 0)
      {
         isValid = false;
         throw new IllegalArgumentException("Patch Id is required for PATCH and TEST.");
      }

      // Is the count field valid
      if (isNumber(m_countText))
      {
         if (m_countText.trim().length() == 1)
            m_countText = "0" + m_countText.trim();
      }
      else
      {
         isValid = false;
         throw new IllegalArgumentException("The build count must be a valid number.");
      }

      // Is the date field valid
      if (!isValidDate(m_dateText))
      {
         isValid = false;
         throw new IllegalArgumentException("Invalid date.\n"
               + "The build date must be in the following format YYYYMM.");
      }

      return isValid;

   }

   /**
    * Modifies the patch id, replacing spaces with underscores and truncating
    * string to a 20 char maximum.
    * 
    * @param s the patch id string, can be <code>null</code>.
    * @return a string, never <code>null</code>.
    */
   private String modifyPatchId(String s)
   {
      if (s == null)
         return "";
      s = s.replace(' ', '_');
      return s.trim().length() > 20 ? s.substring(0, 20) : s;
   }

   /**
    * Modifies the given string by removing beginning and ending file separators
    * if they exist.
    * 
    * @param s the string to modify.
    * 
    * @return the string with beginning and ending file separators removed.
    */
   private String removeFileSeparators(String s)
   {
      String str = s.trim();

      // Remove ending separator
      if (str.endsWith("/"))
         str = str.substring(0, str.lastIndexOf("/"));
      else if (str.endsWith("\\"))
         str = str.substring(0, str.lastIndexOf("\\"));

      // Remove beginning separator
      if (str.startsWith("/"))
         str = str.substring(str.indexOf("/") + 1);
      else if (str.startsWith("\\"))
         str = str.substring(str.indexOf("\\") + 1);

      return str;
   }

   /**
    * Indicates that a string only contains numeric chars
    * 
    * @param s the string to test, cannot be <code>null</code>.
    * @return <code>true</code> if this is a number.
    */
   private boolean isNumber(String s)
   {
      if (s == null)
         throw new IllegalArgumentException("String cannot be null.");
      try
      {
         Integer.parseInt(s);
         return true;
      }
      catch (NumberFormatException e)
      {
         return false;
      }
   }

   /**
    * Simple method to check for the following valid date format YYYYMM.
    * 
    * @param s the string to validate, cannot be <code>null</code>.
    * @return <code>true</code> if valid date format.
    */
   private boolean isValidDate(String s)
   {
      if (s == null)
         throw new IllegalArgumentException("String cannot be null.");
      if (!isNumber(s))
         return false;
      if (s.trim().length() < 6 || s.trim().length() > 6)
         return false;
      int month = Integer.parseInt(s.substring(4, 6));
      if (month < 1 || month > 12)
         return false;

      return true;
   }

   /**
    * Creates a single Ant copy task statement with file and todir attributes.
    * This copy task will run in verbose mode and existing files will always be
    * overwritten. The failonerror argument will control whether this task fails
    * on error or continues with a warning.
    * 
    * @param src the source file location.
    * @param dest the destination directory.
    * @param failonerror if <code>true</code> the task will be configured to
    *           fail if an error occurs, otherwise a warning will be issued on
    *           failure.
    * 
    * @return an Ant copy task for the specified arguments followed by a
    *         newline.
    */
   private String createCopyTask(String src, String dest, boolean failonerror)
   {
      String foe;
      if (failonerror)
         foe = "true";
      else
         foe = "false";

      String s = removeFileSeparators(src);
      String d = removeFileSeparators(dest);

      String copyTask = "\t<copy file=\"" + s + "\" todir=\"" + d + "\" verbose=\"true\" failonerror=\"" + foe
            + "\" overwrite=\"true\"/>";

      return copyTask + "\n";
   }

   /**
    * Surrounds a single Ant copy task created using
    * {@link #createCopyTask(String, String, boolean)} with an if available
    * statement. The copy task will run only if the source file exists in the
    * installation directory in verbose mode and existing files will always be
    * overwritten. The failonerror argument will control whether this task fails
    * on error or continues with a warning.
    * 
    * @param src the source file location.
    * @param dest the destination directory.
    * @param failonerror if <code>true</code> the task will be configured to
    *           fail if an error occurs, otherwise a warning will be issued on
    *           failure.
    * 
    * @return an Ant copy task for the specified arguments followed by a
    *         newline.
    */
   private String createCopyIfAvailableTask(String src, String dest, boolean failonerror)
   {
      File srcFile = new File(src);
      String srcFileName = srcFile.getName();

      String copyTask = "<if>\n" + "\t<available file=\"" + removeFileSeparators(dest) + "/" + srcFileName + "\"/>\n"
            + "\t<then>\n" + "\t" + createCopyTask(src, dest, failonerror) + "\t</then>\n" + "</if>\n";

      return copyTask;
   }

   /**
    * Creates a single Ant move task statement with file and todir attributes.
    * This move task will run in verbose mode and will print out a warning, but
    * will not fail, on error.
    * 
    * @param src the source file location.
    * @param dest the destination directory.
    * 
    * @return an Ant move task for the specified arguments followed by a
    *         newline.
    */
   private String createMoveTask(String src, String dest)
   {
      String destination = removeFileSeparators(dest);

      String moveTask = "\t<move file=\"" + src + "\" todir=\"" + destination
            + "\" verbose=\"true\" failonerror=\"false\"/>";

      return moveTask + "\n";
   }

   /**
    * Creates a single Ant delete task statement with appropriate attributes.
    * This delete task will run in verbose mode and will print out a warning,
    * but will not fail, on error.
    * 
    * @param src the source file/dir location.
    * @param isDir set to <code>true</code> in order to delete a directory,
    *           otherwise a file will be deleted.
    * 
    * @return an Ant delete task for the specified argument followed by a
    *         newline.
    */
   private String createDeleteTask(String src, boolean isDir)
   {
      String type = "file";
      if (isDir)
         type = "dir";

      String deleteTask = "\t<delete " + type + "=\"" + src + "\" verbose=\"true\"" + " quiet=\"true\"/>";

      return deleteTask + "\n";
   }

   /**
    * Creates an Ant task statement which attempts to copy the given Rhythmyx
    * file to the patch backup directory only if the file exists.
    * 
    * @param name the name of the file to be preserved.
    * @param dir the directory relative to the Rhythmyx root which contains the
    *           file.
    * 
    * @return an Ant task which backs up the specified file to the patch
    *         directory.
    */
   private String createBackupTask(String name, String dir)
   {
      String n = removeFileSeparators(name);
      String directory = removeFileSeparators(dir);
      String originalLoc = normalizeDir(directory);

      String backupTask = "<if>\n" + "\t<available file=\"" + originalLoc + "/" + n + "\"/>\n" + "\t<then>\n" + "\t"
            + createCopyTask(originalLoc + "/" + n, PATCH_BACKUP + "/" + directory, true) + "\t</then>\n" + "</if>\n";

      return backupTask;
   }

   /**
    * Creates an Ant task statement which handles the backup of the contents of
    * a zip file. This attempts to copy the original Rhythmyx files and
    * directories which match those contained in the zip file to the patch
    * backup directory only if they exist.
    * 
    * @param src the location of the source file whose contents are to be
    *           preserved.
    * @param dir the directory relative to the Rhythmyx root to which the file
    *           is unzipped.
    * 
    * @return an Ant task which backs up the contents of the specified file to
    *         the patch directory.
    * @throws IOException if an error occurs accessing the zip file.
    */
   private String createZipBackupTask(String src, String dir) throws IOException
   {
      String backupTask = "";

      ZipFile zip = new ZipFile(src);
      Enumeration entries = zip.entries();
      Set<String> dirs = new HashSet<String>();
      while (entries.hasMoreElements())
      {
         ZipEntry entry = (ZipEntry) entries.nextElement();
         String entryName = entry.getName();
         if (entry.isDirectory())
         {
            dirs.add(FilenameUtils.separatorsToUnix((new File(entryName)).getPath()));
         }
         else
         {
            File tmp = new File(entryName);
            String tmpParent = tmp.getParent();
            dirs.remove(tmpParent);

            backupTask += createBackupTask(tmp.getName(), dir + "/" + tmpParent);
         }
      }

      Iterator<String> iter = dirs.iterator();
      while (iter.hasNext())
      {
         backupTask += createBackupDirTask(iter.next());
      }

      return backupTask;
   }

   /**
    * Creates an Ant task statement for creating a directory.
    * 
    * @param dir the directory to create.
    * 
    * @return Ant task which creates a directory.
    */
   private String createMkdirTask(String dir)
   {
      return "<mkdir dir=\"" + removeFileSeparators(dir) + "\"/>";
   }

   /**
    * Creates an Ant task statement which handles backup of a directory. If the
    * directory exists relative to the Rhythmmyx root, then a new directory of
    * the same name will be created under the patch backup directory.
    * 
    * @param dir the directory to be backed up.
    * 
    * @return Ant task statement to back up a directory.
    */
   private String createBackupDirTask(String dir)
   {
      String directory = removeFileSeparators(dir);
      String originalLoc = normalizeDir(directory);

      String backupTask = "<if>\n" + "\t<available file=\"" + originalLoc + "\"/>\n" + "\t<then>\n" + "\t"
            + createMkdirTask(PATCH_BACKUP + "/" + directory) + "\t</then>\n" + "</if>\n";

      return backupTask;
   }

   /**
    * Creates an Ant task statement which handles the uninstall of the contents
    * of a zip file. This attempts to copy the original Rhythmyx files and
    * directories which match those contained in the zip file from the patch
    * backup directory back to their original locations.
    * 
    * @param src the location of the source file whose contents are to be
    *           uninstalled.
    * @param dir the directory relative to the Rhythmyx root to which the file
    *           is unzipped.
    * 
    * @return an Ant task which uninstalls the contents of the specified file.
    * 
    * @throws IOException if an error occurs accessing the zip file.
    */
   private String createZipUninstallTask(String src, String dir) throws IOException
   {
      String uninstallTask = "";

      ZipFile zip = new ZipFile(src);
      Enumeration entries = zip.entries();
      Set<String> dirs = new HashSet<String>();
      while (entries.hasMoreElements())
      {
         ZipEntry entry = (ZipEntry) entries.nextElement();
         String entryName = entry.getName();
         if (entry.isDirectory())
         {
            dirs.add(FilenameUtils.separatorsToUnix((new File(entryName)).getPath()));
         }
         else
         {
            File tmp = new File(entryName);
            String tmpParent = tmp.getParent();
            dirs.remove(tmpParent);

            uninstallTask += createZipEntryUninstallTask(tmp.getName(), dir + "/" + tmpParent);
         }
      }

      Iterator<String> iter = dirs.iterator();
      while (iter.hasNext())
      {
         uninstallTask += createZipDirUninstallTask(iter.next());
      }

      return uninstallTask;
   }

   /**
    * Creates an Ant task statement which copies the given patch file to the
    * Rhythmyx directory. Zip files will be extracted to a Rhythmyx directory.
    * 
    * @param name the name of the file to be installed.
    * @param dir the directory relative to the Rhythmyx root in which the file
    *           should be installed.
    * @param onlyIfAvailable set to <code>true</code> in order to check for
    *           availability of file before copying, <code>false</code> to copy
    *           without checking availability. This flag does not apply to zip
    *           files. The installing of these files is controlled by
    *           {@link #createUnzipTask(String, String)}.
    * @param usePath set to <code>true</code> in order to use the relative path
    *           of the file under the patch files directory.
    * 
    * @return an Ant task which installs the specified file to the Rhythmyx
    *         installation.
    */
   private String createInstallTask(String name, String dir, boolean onlyIfAvailable, boolean usePath)
   {
      String n = removeFileSeparators(name);
      String directory = removeFileSeparators(dir);

      String originalLoc = normalizeDir(directory);
      String installTask;

      String file;
      if (!usePath || dir.length() == 0)
         file = PATCH_FILES + "/" + n;
      else
         file = PATCH_FILES + "/" + dir + "/" + n;

      if (n.endsWith(".zip"))
      {
         installTask = createUnzipTask(file, originalLoc);
      }
      else
      {
         if (onlyIfAvailable)
            installTask = createCopyIfAvailableTask(file, originalLoc, false);
         else
            installTask = createCopyTask(file, originalLoc, false);
      }

      return installTask;
   }

   /**
    * Creates an Ant task statement which uninstalls the given patch file from
    * the Rhythmyx directory. If an original is found in the patch backup
    * directory then it is restored. If a backup of the file is not found and
    * the file exists in the patch files directory then the file is a new file
    * and it is deleted from the Rhythmyx directory.
    * 
    * @param name the name of the file to be uninstalled.
    * @param dir the directory relative to the Rhythmyx root from which the file
    *           should be uninstalled.
    * @param usePath set to <code>true</code> in order to use the relative path
    *           of the file under the patch files directory.
    * 
    * @return an Ant task which uninstalls the specified file from the Rhythmyx
    *         installation.
    */
   private String createUninstallTask(String name, String dir, boolean usePath)
   {
      String n = removeFileSeparators(name);
      String directory = removeFileSeparators(dir);
      String backupLoc = PATCH_BACKUP;
      if (directory.length() > 0)
         backupLoc += "/" + directory + "/" + n;
      else
         backupLoc += "/" + n;

      String file;
      if (!usePath || dir.length() == 0)
         file = PATCH_FILES + "/" + n;
      else
         file = PATCH_FILES + "/" + dir + "/" + n;

      String originalLoc = normalizeDir(directory);
      String uninstallTask = "<if>\n" + "\t\t<available file=\"" + backupLoc + "\"/>\n" + "\t<then>\n" + "\t"
            + createCopyTask(backupLoc, originalLoc, true) + "\t</then>\n" + "\t<elseif>\n" + "\t\t<available file=\""
            + file + "\"/>\n" + "\t\t<then>\n" + "\t\t" + createDeleteTask(originalLoc + "/" + n, false)
            + "\t\t</then>\n" + "\t</elseif>\n" + "</if>\n";

      return uninstallTask;
   }

   /**
    * Creates an Ant task statement which uninstalls the given zip file entry
    * file from the Rhythmyx directory. If an original is found in the patch
    * backup directory then it is restored. If a backup of the file is not found
    * then the file is a new file and it is deleted from the Rhythmyx directory.
    * 
    * @param name the name of the entry to be uninstalled.
    * @param dir the directory relative to the Rhythmyx root from which the file
    *           should be uninstalled.
    * 
    * @return an Ant task which uninstalls the specified file from the Rhythmyx
    *         installation.
    */
   private String createZipEntryUninstallTask(String name, String dir)
   {
      String n = removeFileSeparators(name);
      String directory = removeFileSeparators(dir);
      String backupLoc = PATCH_BACKUP;
      if (directory.length() > 0)
         backupLoc += "/" + directory + "/" + n;
      else
         backupLoc += "/" + n;

      String originalLoc = normalizeDir(directory);
      String uninstallTask = "<if>\n" + "\t\t<available file=\"" + backupLoc + "\"/>\n" + "\t<then>\n" + "\t"
            + createCopyTask(backupLoc, originalLoc, true) + "\t</then>\n" + "\t<elseif>\n" + "\t\t<available file=\""
            + originalLoc + "/" + n + "\"/>\n" + "\t\t<then>\n" + "\t\t"
            + createDeleteTask(originalLoc + "/" + n, false) + "\t\t</then>\n" + "\t</elseif>\n" + "</if>\n";

      return uninstallTask;
   }

   /**
    * Creates an Ant task statement which uninstalls the given zip entry
    * directory from the Rhythmyx directory. If an original is not found in the
    * patch backup directory but the directory exists in the Rhythmyx directory,
    * then the directory is deleted.
    * 
    * @param name the directory to be uninstalled.
    * 
    * @return an Ant task which uninstalls the specified directory from the
    *         Rhythmyx installation.
    */
   private String createZipDirUninstallTask(String name)
   {
      String n = removeFileSeparators(name);
      String backupLoc = PATCH_BACKUP + "/" + n;
      String originalLoc = normalizeDir(n);

      String uninstallTask = "<if>\n" + "\t<not>\n" + "\t\t<available type=\"dir\" file=\"" + backupLoc + "\"/>\n"
            + "\t</not>\n" + "\t<then>\n" + "\t\t<if>\n" + "\t\t\t<available type=\"dir\" file=\"" + originalLoc
            + "\"/>\n" + "\t\t\t<then>\n" + "\t\t\t\t" + createDeleteTask(originalLoc, true) + "\t\t\t</then>\n"
            + "\t\t</if>\n" + "\t</then>\n" + "</if>\n";

      return uninstallTask;
   }

   /**
    * Creates an Ant task statement which uninstalls the given patch file from
    * the Rhythmyx directory only if it exists in the Rhythmyx directory. See
    * {@link #createUninstallTask(String, String)} for the uninstall logic.
    * 
    * @param name the name of the file to be uninstalled.
    * @param dir the directory relative to the Rhythmyx root from which the file
    *           should be uninstalled.
    * @param usePath set to <code>true</code> in order to use the relative path
    *           of the file under the patch files directory.
    * 
    * @return an Ant task which uninstalls the specified file from the Rhythmyx
    *         installation only if it is currently available.
    */
   private String createUninstallIfAvailableTask(String name, String dir, boolean usePath)
   {
      String originalLoc = normalizeDir(removeFileSeparators(dir));

      String uninstallTask = "<if>\n" + "\t\t<available file=\"" + originalLoc + "/" + name + "\"/>\n" + "\t<then>\n"
            + "\t" + createUninstallTask(name, dir, usePath) + "\t</then>\n" + "</if>\n";

      return uninstallTask;
   }

   /**
    * Creates an Ant task statement which unzips a file.
    * 
    * @param file the location of the file to be unzipped.
    * @param dir the directory relative to the Rhythmyx root to which the file
    *           should be unzipped.
    * 
    * @return an Ant task which unzips the specified file to the specified
    *         location.
    */
   private String createUnzipTask(String file, String dir)
   {
      return "<if>\n" + "\t<available file=\"" + dir + "\"/>\n" + "\t<then>\n" +

      "\t\t<unzip src=\"" + file + "\" dest=\"" + dir + "\"/>\n" + "<touch><fileset dir=\"" + dir + "\"/></touch>\n"
      + "\t</then>\n" + "</if>\n";
   }

   /**
    * Creates an Ant task statement which copies the given file to the patch
    * files build directory. Files are copied to the patch files directory in a
    * folder structure based on their locations relative to the Rhythmyx root.
    * 
    * @param src the absolute path of the file to be copied.
    * @param loc the path of the file relative to the Rhythmyx root.
    * 
    * @return an Ant task which copies the specified file to the patch files
    *         build directory.
    */
   private String createBuildTask(String src, String loc)
   {
      String dir = PATCHDIR_FILES;
      if (loc != null && loc.length() > 0)
         dir += File.separator + loc;

      return createCopyTask(src, dir, true);
   }

   /**
    * Creates an Ant target statement.
    * 
    * @param name the name of the target.
    * @param contents the instructions to be included in the target.
    * 
    * @return an Ant target statement with specified name and instructions.
    */
   private String createTarget(String name, String contents)
   {
      return "<target name=\"" + name + "\">\n" + contents + "</target>\n";
   }

   /**
    * Creates an Ant project statement with one target which is the default.
    * 
    * @param name the name of the project.
    * @param targetName the name of the default target.
    * @param targetContents the instructions to be included in the default
    *           target.
    * 
    * @return an Ant project statement with one default target.
    */
   private String createProject(String name, String targetName, String targetContents)
   {
      String project = "<project name=\"" + name + "\" default=\"" + targetName + "\">\n"
            + createTarget(targetName, targetContents) + "</project>";

      return project;
   }

   /**
    * Normalizes the given directory so that it is relative to the Rhythmyx
    * root.
    * 
    * @param dir the directory path, assumed not <code>null</code>, may be
    *           empty.
    * 
    * @return a directory path which has been made relative to the Rhythmyx
    *         root.
    */
   private String normalizeDir(String dir)
   {
      if (dir.startsWith(RHYTHMYX_HOME))
         return dir;

      String originalLoc;
      if (dir.trim().length() == 0)
         originalLoc = RHYTHMYX_HOME;
      else
         originalLoc = RHYTHMYX_HOME + "/" + dir;

      return originalLoc;
   }

   /**
    * Adds an xml header to a given Ant project definition and then writes it to
    * the specified file.
    * 
    * @param file the absolute path of the file to be written, assumed not
    *           <code>null</code> or empty.
    * @param project the Ant project definition
    */
   private void writeBuildFile(String file, String project)
   {
      FileWriter fw = null;

      try
      {
         fw = new FileWriter(file);
         String header = "<?xml version=\"1.0\"?>\n";
         fw.write(header + project);
      }
      catch (IOException ioe)
      {
         System.out.printf("Unable to open file: %s", file);
         ioe.printStackTrace();
      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   /**
    * Loads the patch configuration information, including build date and count,
    * additional jars and files, and files to be removed.
    */
   private void loadConfigFile()
   {
      File cfgFile = new File(m_configfile);
      boolean bd = false;
      boolean bc = false;
      boolean pi = false;
      boolean aj = false;
      boolean af = false;
      boolean rf = false;

      if (cfgFile.exists())
      {

         m_additionalJarsArrList.clear();
         m_addPatchFilesArrList.clear();
         m_remPatchFilesArrList.clear();


         BufferedReader b;
         try
         {
            b = new BufferedReader(new FileReader(cfgFile));

            String line;

            // read away first line header
            b.readLine();
            while ((line = b.readLine()) != null)
            {

               if (line.equals(BUILD_DATE_BEGIN_TAG))
               {
                  bd = true;
                  continue;
               }
               if (line.equals(BUILD_DATE_END_TAG))
               {
                  bd = false;
                  continue;
               }
               if (line.equals(BUILD_COUNT_BEGIN_TAG))
               {
                  bc = true;
                  continue;
               }
               if (line.equals(BUILD_COUNT_END_TAG))
               {
                  bc = false;
                  continue;
               }
               if (line.equals(PATCH_ID_BEGIN_TAG))
               {
                  pi = true;
                  continue;
               }
               if (line.equals(PATCH_ID_END_TAG))
               {
                  pi = false;
                  continue;
               }
               if (line.equals(ADDITIONAL_JARS_BEGIN_TAG))
               {
                  aj = true;
                  continue;
               }
               if (line.equals(ADDITIONAL_JARS_END_TAG))
               {
                  aj = false;
                  continue;
               }
               if (line.equals(ADDITIONAL_FILES_BEGIN_TAG))
               {
                  af = true;
                  continue;
               }
               if (line.equals(ADDITIONAL_FILES_END_TAG))
               {
                  af = false;
                  continue;
               }
               if (line.equals(REMOVE_FILES_BEGIN_TAG))
               {
                  rf = true;
                  continue;
               }
               if (line.equals(REMOVE_FILES_END_TAG))
               {
                  rf = false;
                  continue;
               }

               if (bd)
               {
                  m_dateText = line;
               }
               if (bc)
               {
                  m_countText = line;
               }
               if (pi)
               {
                  m_patchId = line;
               }
               if (aj)
               {
                  m_additionalJarsArrList.add(line);
               }
               if (af)
               {
                  m_addPatchFilesArrList.add(line);
               }
               if (rf)
               {
                  m_remPatchFilesArrList.add(line);
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Creates a new Ant project from the specified xml build file.
    * 
    * @param buildFile the path to the build file to be invoked. May not be
    *           <code>null</code> or empty.
    * @return the project that represents the specified build file.
    */
   public static Project getProject(String buildFile)
   {
      if (null == buildFile || buildFile.length() == 0)
         throw new IllegalArgumentException("Build file cannot be null or empty.");
      ProjectHelper helper = new ProjectHelperImpl();
      Project project = new Project();
      project.init();

      File file = new File(buildFile);

      helper.parse(project, file);

      return project;
   }

   /**
    * The main program entry point
    * 
    * @param args
    */
   public static void main(String[] args)
   {

      boolean obfuscation = (StringUtils.equalsIgnoreCase(System.getProperty("OBFUSCATION"), "true"));

      boolean debug = (StringUtils.equalsIgnoreCase(System.getProperty("DEBUG"), "true"));

      boolean manufacture = (StringUtils.equalsIgnoreCase(System.getProperty("MANUFACTURE"), "true"));

      boolean sync = (StringUtils.equalsIgnoreCase(System.getProperty("SYNC"), "true"));

      boolean verbose = (StringUtils.equalsIgnoreCase(System.getProperty("VERBOSE"), "true"));

      if (args.length > 8)
      {
         new PSRxBuildInput(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], obfuscation, debug, manufacture, sync, verbose);
      }
   }

   private String m_patchId;

   private String m_batchfile;

   private String m_buildfile;

   private String m_backupfile;

   private String m_installfile;

   private String m_uninstallfile;

   private String m_rootDir;

   private String m_dateText; 

   private String m_countText;

   private String m_configfile;

   private boolean m_obfu; 

   private boolean m_sync; 

   private boolean m_verbose; 

   private boolean m_manufacture;

   private boolean m_debug;

   private String m_libPath;

   private String m_readme;

   private ArrayList m_addPatchFilesArrList = new ArrayList();

   private ArrayList m_remPatchFilesArrList = new ArrayList();

   private ArrayList m_additionalJarsArrList = new ArrayList();

   private static final String ms_action = "Build All";

   private static final String ms_jenkinsProperty = "JENKINS_PATCH";

   private static final Map<String, String> ms_targetMap = new HashMap<String, String>();

   static
   {
      ms_targetMap.put(ms_action, "package");
   }

   private static final String ms_type = "PATCH";

   private static final String DTS_ZIP = "delivery-tier-suite.zip";
   private static final String DTS_APPLIB_ZIP = "delivery-tier-app-lib.zip";
   
   
   // Destination directories for our patches.
   private static final String[] ms_directories = new String[]
         {"Administration",
          "AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib",
          "AppServer/server/rx/lib",
          "eclipse/plugins/com.percussion.client_1.0.0/lib",
          "eclipse/plugins/com.percussion.workbench_1.0.0/lib",
          "InlineLinkConverter", 
          "lib", 
          "sys_resources/AppletJars",
          "VariantConverter"};


   private static final String[] ms_deployui = new String[]
         {ms_directories[1]};

   private static final String[] ms_htmlConverter = new String[]
         {ms_directories[1]};

   private static final String[] ms_InlineLinkConverter = new String[]
         {ms_directories[5]};

   private static final String[] ms_percbeans = new String[]
         {ms_directories[0], ms_directories[1]};

   private static final String[] ms_psjniregistry = new String[]
         {ms_directories[1]};

   private static final String[] ms_rhythmyx = new String[]
         {ms_directories[0], ms_directories[1]};

   private static final String[] ms_rxagent = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxCheckboxTree = new String[]
         {ms_directories[7]};

   private static final String[] ms_rxclient = new String[]
         {ms_directories[0], ms_directories[1]};

   private static final String[] ms_rxcontentui = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxbusiness = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxdesignercore = new String[]
         {ms_directories[3]};

   private static final String[] ms_DesignObjectUtils = new String[]
         {ms_directories[1]};

   private static final String[] ms_deployer = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxextensions = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxff = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxi18n = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxinstall = new String[]
         {ms_directories[1], ms_directories[2]};

   private static final String[] ms_rxlogin = new String[]
         {ms_directories[1], ms_directories[2]};

   private static final String[] ms_rxmisctools = new String[]
         {ms_directories[0], ms_directories[1]};

   private static final String[] ms_pspackagerui = new String[]
         {ms_directories[6]};

   private static final String[] ms_rxpublisher = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxserver = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxservices = new String[]
         {ms_directories[0], ms_directories[1]};
   
   private static final String[] ms_rxantinstall = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxservlet = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxsimple = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxtablefactory = new String[]
         {ms_directories[0], ms_directories[1]};

   private static final String[] ms_rxutils = new String[]
         {ms_directories[0], ms_directories[1], ms_directories[2]};

   private static final String[] ms_rxwebservices = new String[]
         {ms_directories[1]};

   private static final String[] ms_rxworkbench = new String[]
         {ms_directories[4]};

   private static final String[] ms_rxworkflow = new String[]
         {ms_directories[1]};

   private static final String[] ms_serveruicomp = new String[]
         {ms_directories[0], ms_directories[1]};

   private static final String[] ms_deploy = new String[]{
      ms_directories[1]};
   
   private static final String[] ms_rest = new String[]{
      ms_directories[1]};
   
   private static final String[] ms_perc_sitemanage_api = new String[]{
      ms_directories[1]};
   
   private static final String[] ms_perc_sitemanage_impl = new String[]{
      ms_directories[1]};
   
   private static final String[] ms_image_widget = new String[]
         {ms_directories[1], ms_directories[2]};
   
   private static final Map<String, String[]> ms_jarMap = new HashMap<String, String[]>();

   private static final String[] ms_patchJars = new String[]
         {"", 
      "deploy.jar",
      "deployui.jar",
      "InlineLinkConverter.jar",
      "psjniregistry.jar",
      "rxagent.jar",
      "rxCheckboxTree.jar", 
      "rxbusiness.jar", 
      "rxdesignercore.jar", 
      "rxextensions.jar",
      "rxff.jar", 
      "rxi18n.jar",
      "rxlogin.jar",
      "rxmisctools.jar",
      "rxpublisher.jar",
      "rxservlet.jar", 
      "rxsimple.jar", 
      "rxtablefactory.jar",
      "rxutils.jar", 
      "rxwebservices.jar",
      "rxworkbench.jar",
      "rxworkflow.jar", 
      "serveruicomp.jar",
      "rxcontentui.jar",
      "rxdeployer.jar",
      "rxDesignObjectUtils.jar",
      "pspackagerui.jar",
      "rest-1.0.jar",
      "perc-sitemanage-api.jar",
      "perc-sitemanage-impl.jar",
      "image-widget-1.0.jar"};

   private static final String[] ms_requiredJars = new String[]
         {"htmlConverter.jar",
          "percbeans.jar",
          "rhythmyx.jar",
          "rxclient.jar",
          "rxinstall.jar",
          "rxserver.jar",
          "rxservices.jar",
          "rxantinstall.jar"};

   private static final String RHYTHMYX_HOME = "${rhythmyx.home}";

   private static final String PATCH_BACKUP = "${patch.backup}";

   private static final String PATCH_FILES = "${patch.files}";

   private static final String PATCHDIR_FILES = "${patchdir.files}";

   private static final String LIBDIR = "${libdir}";

   private static final String ADDITIONAL_JARS_BEGIN_TAG = "<AJ>";

   private static final String ADDITIONAL_JARS_END_TAG = "</AJ>";

   private static final String ADDITIONAL_FILES_BEGIN_TAG = "<AF>";

   private static final String ADDITIONAL_FILES_END_TAG = "</AF>";

   private static final String REMOVE_FILES_BEGIN_TAG = "<RF>";

   private static final String REMOVE_FILES_END_TAG = "</RF>";

   private static final String BUILD_DATE_BEGIN_TAG = "<BD>";

   private static final String BUILD_DATE_END_TAG = "</BD>";

   private static final String BUILD_COUNT_BEGIN_TAG = "<BC>";

   private static final String BUILD_COUNT_END_TAG = "</BC>";

   private static final String PATCH_ID_BEGIN_TAG = "<PN>";

   private static final String PATCH_ID_END_TAG = "</PN>";

   static
   {
      ms_jarMap.put(ms_patchJars[1], ms_deploy);
      ms_jarMap.put(ms_patchJars[2], ms_deployui);
      ms_jarMap.put(ms_patchJars[3], ms_InlineLinkConverter);
      ms_jarMap.put(ms_patchJars[4], ms_psjniregistry);
      ms_jarMap.put(ms_patchJars[5], ms_rxagent);
      ms_jarMap.put(ms_patchJars[6], ms_rxCheckboxTree);
      ms_jarMap.put(ms_patchJars[7], ms_rxbusiness);
      ms_jarMap.put(ms_patchJars[8], ms_rxdesignercore);
      ms_jarMap.put(ms_patchJars[9], ms_rxextensions);
      ms_jarMap.put(ms_patchJars[10], ms_rxff);
      ms_jarMap.put(ms_patchJars[11], ms_rxi18n);
      ms_jarMap.put(ms_patchJars[12], ms_rxlogin);
      ms_jarMap.put(ms_patchJars[13], ms_rxmisctools);
      ms_jarMap.put(ms_patchJars[14], ms_rxpublisher);
      ms_jarMap.put(ms_patchJars[15], ms_rxservlet);
      ms_jarMap.put(ms_patchJars[16], ms_rxsimple);
      ms_jarMap.put(ms_patchJars[17], ms_rxtablefactory);
      ms_jarMap.put(ms_patchJars[18], ms_rxutils);
      ms_jarMap.put(ms_patchJars[19], ms_rxwebservices);
      ms_jarMap.put(ms_patchJars[20], ms_rxworkbench);
      ms_jarMap.put(ms_patchJars[21], ms_rxworkflow);
      ms_jarMap.put(ms_patchJars[22], ms_serveruicomp);
      ms_jarMap.put(ms_patchJars[23], ms_rxcontentui);
      ms_jarMap.put(ms_patchJars[24], ms_deployer);
      ms_jarMap.put(ms_patchJars[25], ms_DesignObjectUtils);
      ms_jarMap.put(ms_patchJars[26], ms_pspackagerui);
      ms_jarMap.put(ms_patchJars[27], ms_rest);
      ms_jarMap.put(ms_patchJars[28], ms_perc_sitemanage_api);
      ms_jarMap.put(ms_patchJars[29], ms_perc_sitemanage_impl);
      ms_jarMap.put(ms_patchJars[30], ms_image_widget);
      ms_jarMap.put(ms_requiredJars[0], ms_htmlConverter);
      ms_jarMap.put(ms_requiredJars[1], ms_percbeans);
      ms_jarMap.put(ms_requiredJars[2], ms_rhythmyx);
      ms_jarMap.put(ms_requiredJars[3], ms_rxclient);
      ms_jarMap.put(ms_requiredJars[4], ms_rxinstall);
      ms_jarMap.put(ms_requiredJars[5], ms_rxserver);
      ms_jarMap.put(ms_requiredJars[6], ms_rxservices);
      ms_jarMap.put(ms_requiredJars[7], ms_rxantinstall);
   }
  
}
