/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.tools.simple;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

/**
 * This class parses an eclipse external tools ant build file used for the ant
 * installer, reading in the classpath entries.  The required jar files are
 * copied to a specified target directory.
 */
public class PSCopyAntInstallerClasspath
{

   private static final Logger log = LogManager.getLogger(PSCopyAntInstallerClasspath.class);

   /**
    * Copies jars which appear on the classpath to a specified directory.
    *
    * @param srcFile The source config file, may not be <code>null</code> and
    * must exist.
    * @param rootDirectory The root directory of the source tree, may not be
    * <code>null</code> and must exist.
    * @param tgtDirectory The target manifest file directory, may not be
    * <code>null</code> and must exist.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs.
    * @throws FileNotFoundException if any file cannot be located.
    */
   public void copyJars(File srcFile, File rootDirectory, File tgtDirectory)
    throws IOException, IllegalArgumentException, FileNotFoundException
   {
      validateCopyJarsParameters(srcFile, rootDirectory, tgtDirectory);

      // read in config file and copy classpath entries
      final BufferedReader in = new BufferedReader(new FileReader(srcFile));
      final Writer out = null;
      int jarsCopied = 0;
      
      try
      {
         String output = null;
         boolean inClasspath = false;
         
         while ((output = in.readLine()) != null)
         {
            if (output.equals(CLASSPATH_START))
               inClasspath = true;
          
            if (inClasspath && output.equals(CLASSPATH_END))
               break;
                        
            if (inClasspath)
            {
               int archiveIndex = output.indexOf(ARCHIVE_ATTR);
               if (archiveIndex == -1)
                  continue;
                              
               int extIndex = output.indexOf(".jar", archiveIndex);
               if (extIndex == -1)
                  continue;
               
               String archiveStr = output.substring(archiveIndex, extIndex + 4);
               String[] arr = archiveStr.split("/");
               int length = arr.length;
               
               String[] newArr = new String[arr.length - 2];
               for (int i = 2; i < length; i++)
                  newArr[i - 2] = arr[i];
               
               String jarPath = "";
               for (int i = 0; i < newArr.length; i++)
               {
                  if (jarPath.length() > 0)
                     jarPath += '/';
                  
                  jarPath += newArr[i];
               }
               
               File jarFile = new File(rootDirectory, jarPath);
               File libFile = new File(tgtDirectory, jarFile.getName());
               FileUtils.copyFile(jarFile, libFile);
               jarsCopied++;
            }
         }
      }
      finally
      {
         IOUtils.closeQuietly(in);
         IOUtils.closeQuietly(out);
      }
      
      System.out.println("Copied " + jarsCopied + " jar(s) to " + 
            tgtDirectory.getAbsolutePath());
   }

   /**
    * Throws {@link IllegalArgumentException} if any of the parameters
    * does not confirm to {@link #copyJars(File, File, File)} contract.
    * See that method for the parameter description. 
    * @see #copyJars(File, File, File)
    */
   private void validateCopyJarsParameters(File srcFile, File rootDirectory,
         File tgtDirectory)
   {
      if (srcFile == null || !srcFile.exists())
      {
         throw new IllegalArgumentException("source file is invalid");
      }

      if (rootDirectory == null || !rootDirectory.exists())
      {
         throw new IllegalArgumentException("root directory is invalid");
      }
      
      if (tgtDirectory == null || !tgtDirectory.exists())
      {
         throw new IllegalArgumentException("target directory is invalid");
      }
   }

   /**
    * This class may be used from the command line.
    *
    * Arguments expected are:
    *
    * <ol>
    * <li>sourceFile: The source build file.  Must point to an existing file.
    * </li>
    *
    * <li>rootDir: The root directory.  Must point to an existing directory.
    * </li>
    *
    * <li>targetDir: The target directory.  Must point to an existing directory.
    * </li>
    * 
    * </ol>
    *
    * Any errors are written to System.out
    */
   public static void main(String[] args)
   {
      try
      {
         // get the args
         if (args.length < 3)
         {
            System.out.println("Incorrect number of arguments.");
            printUsage();
            return;
         }

         final File srcFile = new File(args[0]);
         final File rootDir = new File(args[1]);
         final File tgtDir = new File(args[2]);
         
         new PSCopyAntInstallerClasspath().copyJars(srcFile, rootDir,
               tgtDir);
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
         "<source file> <root dir> <target dir>");
   }

   /**
    * Marks beginning of the section for classpath
    */
   private static final String CLASSPATH_START = 
      "<listAttribute key=\"org.eclipse.jdt.launching.CLASSPATH\">";
   
   /**
    * Marks end of the section for classpath
    */
   private static final String CLASSPATH_END = "</listAttribute>";
   
   /**
    * Name of the jar file path attribute of the classpath entry
    */
   private static final String ARCHIVE_ATTR = "internalArchive";
}
