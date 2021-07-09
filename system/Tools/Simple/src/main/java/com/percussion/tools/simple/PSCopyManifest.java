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

package com.percussion.tools.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to copy a workbench plugin's MANIFEST.MF file to the
 * appropriate location.  In the process, it will perform the following
 * clean-up on the file to accommodate plugin deployment:
 *
 * 1) converts classpath entry references from local file system to lib directory
 * 2) removes classpath entry ending on name "classes"
 * 3) adds classpath entry for appropriate jar, client or workbench
 *
 * It will also copy the jar files listed in the manifest to the plugin's lib
 * directory under system\eclipse\plugins\<plugin>.
 */
public class PSCopyManifest
{

   private static final Logger log = LogManager.getLogger(PSCopyManifest.class);

   /**
    * Copy MANIFEST.MF after performing necessary clean-up.
    *
    * @param srcFile The source manifest file, may not be <code>null</code>.
    * @param tgtDirectory The target manifest file directory, may not be <code>null</code>.
    * @param additionalEntries additional classpath entries to add
    * to the manifest. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs
    * @throws FileNotFoundException if any file cannot be located.
    */
   public void copyManifest(File srcFile, File tgtDirectory,
         List<File> additionalEntries)
    throws IOException, IllegalArgumentException, FileNotFoundException
   {
      validateCopyManifestParameters(srcFile, tgtDirectory);

      // read in manifest, clean-up, write to new location
      // copy classpath entries
      final File srcProjectDir = srcFile.getParentFile().getParentFile();
      final BufferedReader in = new BufferedReader(new FileReader(srcFile));
      final Writer out =
            new FileWriter(new File(tgtDirectory, srcFile.getName()));
      final File pluginDir = new File(tgtDirectory.getParent());

      try
      {
         String output = null;
         // whether inside of the manifest file classpath section
         boolean inClasspath = false;

         while ((output = in.readLine()) != null)
         {
            if (output.startsWith(BUNDLE_CLASS_PATH_MARK))
            {
               // leave only part which contains classpath entry
               output = output.substring(BUNDLE_CLASS_PATH_MARK.length());
               out.write(BUNDLE_CLASS_PATH_MARK);
               for (final File file : additionalEntries)
               {
                  writeEntry(out, file, pluginDir, true);
               }
               inClasspath = true;
            }

            if (!inClasspath)
            {
               out.write(output + "\n");
               continue;
            }
            else
            {
               // strip ending comma if necessary
               output = output.trim();
               final boolean endsWithComma = output.endsWith(",");
               final String fromEntryStr = endsWithComma
                     ? output.substring(0, output.length() - 1)
                     : output;

               final File fromEntry = new File(srcProjectDir, fromEntryStr);
               if (fromEntry.getName().equals("classes"))
               {
                  continue;
               }
               writeEntry(out, fromEntry, pluginDir, endsWithComma);
               inClasspath = endsWithComma;
            }
         }
      }
      finally
      {
         IOUtils.closeQuietly(in);
         IOUtils.closeQuietly(out);
      }
   }

   /**
    * Throws {@link IllegalArgumentException} if any of the parameters
    * does not confirm to {@link #copyManifest(File, File, List)} contract.
    * See that method for the parameter description. 
    * @see #copyManifest(File, File, List)
    */
   private void validateCopyManifestParameters(File srcFile, File tgtDirectory)
   {
      if (srcFile == null || !srcFile.exists())
      {
         throw new IllegalArgumentException("source manifest is invalid");
      }

      if (tgtDirectory == null || !tgtDirectory.exists())
      {
         throw new IllegalArgumentException("target manifest directory is invalid");
      }
   }

   /**
    * Writes entry of manifest file, copies the file described by the entry.
    * @param out where to write it from. Never <code>null</code>
    * @param fromEntry file of the original manifest entry.
    * Never <code>null</code>.
    * @param pluginDir directory where plugin is constructed.
    * Never <code>null</code>.
    * @param endsWithComma whether written entry should end with comma.
    * @throws IOException if any problem happended during readin/writing the file.
    */
   private void writeEntry(Writer out, File fromEntry, File pluginDir,
         boolean endsWithComma) throws IOException
   {
      if (!fromEntry.exists())
      {
         throw new IllegalArgumentException(
               "Specified classpath entry \"" + fromEntry + "\" is not found");
      }
      // libraries are copied to the lib directory
      // directories are copied to the root plugin directory
      final File toEntry = fromEntry.isDirectory()
            ? new File(fromEntry.getName())
            : new File("lib", fromEntry.getName()); 
      final String toEntryStr = toEntry.getPath();
      final File toEntryFile = new File(pluginDir, toEntryStr);
      if (fromEntry.isDirectory())
      {
         FileUtils.copyDirectory(fromEntry, toEntryFile);
      }
      else
      {
         FileUtils.copyFile(fromEntry, toEntryFile);
      }

      // use classic path separator, instead of Windows one
      out.write(" " + toEntryStr.replace('\\', '/'));
      if (endsWithComma)
      {
         out.write(',');
      }
      out.write("\n");
   }

   /**
    * This class may be used from the command line.
    *
    * Arguments expected are:
    *
    * <ol>
    * <li>sourceFile: The source manifest.mf file.
    * Must point to an existing file.
    * </li>
    *
    * <li>targetDir: The target directory.  Must point to an existing directory.
    * </li>
    * 
    * <li>0 or more additional classpath entries to add.
    * </li>
    * </ol>
    *
    * Any errors are written to System.out
    */
   /**
    * @param args
    */
   public static void main(String[] args)
   {
      try
      {
         // get the args
         if (args.length < 2)
         {
            System.out.println("Incorrect number of arguments.");
            printUsage();
            return;
         }

         final File srcFile = new File(args[0]);
         final File tgtDir = new File(args[1]);
         final List<File> additionalEntries = new ArrayList<File>();
         for (int i = 2; i < args.length; i++)
         {
            additionalEntries.add(new File(args[i]));
         }

         new PSCopyManifest().copyManifest(srcFile, tgtDir, additionalEntries);

      }
      catch(Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
         System.exit(1);
      }
   }

   /**
    * Prints cmd line usage to the screen.
    */
   private static void printUsage()
   {
      System.out.println("Usage:");
      System.out.print("java com.percussion.tools.simple.PSCopyManifest ");
      System.out.println(
         "<source file> <target dir> <additional classpath entry>*");
   }

   /**
    * Marks beginning of the section for bundle classpath
    */
   private static final String BUNDLE_CLASS_PATH_MARK = "Bundle-ClassPath:";
}
