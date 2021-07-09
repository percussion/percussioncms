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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class performs an analysis of jar files from two directories.  The first
 * directory is considered to be the source directory and the second directory
 * is considered to be the target directory.  This is useful when seeing what
 * jar files changed in different JBoss distributions, for example.  Each
 * directory under the source directory will be searched for .jar files.  For
 * each .jar file, the target directory will be searched for matches.
 * Implementation versions will be also be displayed for all .jar files if
 * found.
 */
public class PSDirectoryAnalyzer
{

   private static final Logger log = LogManager.getLogger(PSDirectoryAnalyzer.class);

   /**
    * Performs analysis.
    *
    * @param oldDirectory The source directory, may not be <code>null</code>.
    * @param newDirectory The target directory, may not be <code>null</code>.
    */
   public void analyze(File oldDirectory, File newDirectory)
   {
      buildMaps(oldDirectory, newDirectory);     
      findNewJarLocations();
      displayMissingJars();
   }

   /**
    * Populates directory jar maps.
    * 
    * @param oldDir The source directory, assumed not <code>null</code>.
    * @param newDir The target directory, assumed not <code>null</code>.
    */
   private void buildMaps(File oldDir, File newDir)
   {
      buildJarMap(oldDir, m_oldDirJarMap);
      buildJarMap(newDir, m_newDirJarMap);
   }
   
   /**
    * Populates a directory jar map for a given directory.
    * 
    * @param dir The source directory, assumed not <code>null</code>.
    * @param dirJarMap The map to populate, assumed not <code>null</code>.
    */
   private void buildJarMap(File dir, Map<String, 
         Map<String, String>> dirJarMap)
   {
      File[] files = dir.listFiles();
      for (File file : files)
      {
         if (file.isDirectory())
         {
            buildJarMap(file, dirJarMap);
         }
         else
         {
            String fileName = file.getName();
            if (!fileName.endsWith(".jar"))
            {
               continue;
            }
            
            Map<String, String> jars;
            String dirPath = file.getParentFile().getAbsolutePath();
            if (!dirJarMap.containsKey(dirPath))
            {
               jars = new TreeMap<String, String>();
            }
            else
            {
               jars = dirJarMap.get(dirPath);
            }
                        
            jars.put(fileName, getVersion(file));
            dirJarMap.put(dirPath, jars);           
         }
      }
   }
   
   /**
    * Processes the source directory map and outputs the details of the
    * analysis.
    */
   private void findNewJarLocations()
   {
      Iterator<String> dirs = m_oldDirJarMap.keySet().iterator();
      while (dirs.hasNext())
      {
         int oldRootLength = ms_oldRoot.length();
         String strDir;
         String dir = dirs.next();
         if (dir.length() > oldRootLength)
         {
            strDir = dir.substring(oldRootLength);
         }
         else
         {
            strDir = "<Root>";
         }
         
         ms_pWriter.println("\n" + strDir + ":\n");
         Map<String, String> jars = m_oldDirJarMap.get(dir);
         Iterator<String> iter = jars.keySet().iterator();
         while (iter.hasNext())
         {
            String jar = iter.next();
            String version = jars.get(jar);
            if (version == null)
            {
               version = "";
            }
            
            String newLocs = findNewJarLocs(jar);
            if (newLocs.trim().length() == 0)
            {
               m_missingJars.add(strDir + '\\' + jar + version);
               
            }
            
            ms_pWriter.println(jar + version + " -> [" + newLocs + "]");
         }
         ms_pWriter.println("\n----------------------------------");
      }
   }
   
   /**
    * Finds the matching location(s) of the given jar name in the target
    * directory.
    * 
    * @param name The .jar file, assumed not <code>null</code>.
    * 
    * @return List of new locations and versions of the .jar file as a comma-
    * separated String.  Never <code>null</code>, may be empty.
    */
   private String findNewJarLocs(String name)
   {
      String strLocations = "";
            
      Iterator<String> dirs = m_newDirJarMap.keySet().iterator();
      while (dirs.hasNext())
      {
         int newRootLength = ms_newRoot.length();
         String strDir;
         String dir = dirs.next();
         if (dir.length() > newRootLength)
         {
            strDir = dir.substring(newRootLength);
         }
         else
         {
            strDir = "<Root>";
         }
         
         Map<String, String> jars = m_newDirJarMap.get(dir);
         Iterator<String> iter = jars.keySet().iterator();
         while (iter.hasNext())
         {
            String jar = iter.next();
            if (jar.equals(name))
            {
               if (strLocations.trim().length() > 0)
               {
                  strLocations += ", ";
               }
               
               String version = jars.get(jar);
               if (version != null)
               {
                  strDir += version;
               }
               
               strLocations += strDir;
            }
         }
      }
      
      return strLocations;
   }
   
   /**
    * Gets the implementation version of the given .jar file.  Loaded from the
    * manifest file.
    * 
    * @param jarFile The .jar file, assumed not <code>null</code>.
    * 
    * @return The version information in a comma-separated String (if multiple
    * versions) enclosed in parentheses.
    */
   private String getVersion(File jarFile)
   {
      String version = null;
      ZipFile zip = null;
      InputStream in = null;
      InputStreamReader isReader = null;
      BufferedReader bReader = null;
      
      try
      {
         zip = new ZipFile(jarFile);
         ZipEntry entry = zip.getEntry(MANIFEST_NAME);
         if (entry != null)
         {
            String implVersions = "";
            in = zip.getInputStream(entry);
            isReader = new InputStreamReader(in);
            bReader = new BufferedReader(isReader);
            String line;
            while ((line = bReader.readLine()) != null)
            {
               if (line.startsWith(IMPL_VERSION))
               {
                  line = line.substring(IMPL_VERSION.length()).trim();
                  if (implVersions.trim().length() > 0)
                  {
                     implVersions += ',';
                  }
                  
                  implVersions += line;
               }
            }
            
            if (implVersions.trim().length() > 0)
            {
               version = '(' + implVersions + ')';
            }
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         if (bReader != null)
         {
            try
            {
               bReader.close();
            }
            catch (IOException e)
            {
               
            }
         }
         
         if (isReader != null)
         {
            try
            {
               isReader.close();
            }
            catch (IOException e)
            {
               
            }
         }
         
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
               
            }
         }
         
         if (zip != null)
         {
            try
            {
               zip.close();
            }
            catch (IOException e)
            {
               
            }
         }
         
      }
      
      return version;      
   }
   
   /**
    * Writes out the set of jars which could not be located under the target
    * directory.
    */
   private void displayMissingJars()
   {
      ms_pWriter.println("\nThe following jars could not be located under " +
            ms_newRoot + ":\n");
      for (String jar : m_missingJars)
      {
         ms_pWriter.println(jar);
      }
   }
   
   /**
    * This class may be used from the command line.
    *
    * Arguments expected are:
    *
    * <ol>
    * <li>sourceDir: The source directory.  Must point to an existing directory.
    * </li>
    *
    * <li>targetDir: The target directory.  Must point to an existing directory.
    * </li>
    * </ol>
    * 
    * Optional argument:
    * 
    * <ol>
    * <li>Output file.  If not specified, output will be written to System.out.
    * </li>
    * </ol>
    *
    * Any errors are written to System.out
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

         final File oldDir = new File(args[0]);
         if (!oldDir.isDirectory())
         {
            throw new IllegalArgumentException(oldDir.getAbsolutePath() +
                  " is not a valid directory");
         }
         ms_oldRoot = oldDir.getAbsolutePath();
                  
         final File newDir = new File(args[1]);
         if (!newDir.isDirectory())
         {
            throw new IllegalArgumentException(newDir.getAbsolutePath() +
                  " is not a valid directory");
         }
         ms_newRoot = newDir.getAbsolutePath();
         
         if (args.length > 2)
         {
            ms_pWriter = new PrintWriter(new File(args[2]));
         }
         else
         {
            ms_pWriter = new PrintWriter(System.out);
         }
        
         ms_pWriter.println("#### Directory Analysis ####");
         ms_pWriter.println("Source Directory: " + ms_oldRoot);
         ms_pWriter.println("Target Directory: " + ms_newRoot);
         
         new PSDirectoryAnalyzer().analyze(oldDir, newDir);
      }
      catch(Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
         System.exit(1);
      }
      finally
      {
         if (ms_pWriter != null)
         {
            ms_pWriter.close();
         }
      }
   }

   /**
    * Prints cmd line usage to the screen.
    */
   private static void printUsage()
   {
      System.out.println("Usage:");
      System.out.print("java com.percussion.tools.simple.PSDirectoryAnalyzer ");
      System.out.println(
         "<source file> <target dir> [output file]");
   }

   /**
    * The source directory to jar map where the key is the sub-directory path
    * and the value is a map of jar file names to versions.  Populated in
    * {@link #buildMaps(File, File)}.
    */
   private Map<String, Map<String, String>> m_oldDirJarMap = 
      new TreeMap<String, Map<String, String>>();
   
   /**
    * The target directory to jar map where the key is the sub-directory path
    * and the value is a map of jar file names to versions.  Populated in
    * {@link #buildMaps(File, File)}.
    */
   private Map<String, Map<String, String>> m_newDirJarMap = 
      new TreeMap<String, Map<String, String>>();
   
   /**
    * Holds the set of source directory jars for which a match could not be
    * found in the target directory.
    */
   private Set<String> m_missingJars = new TreeSet<String>();
 
   /**
    * Used for writing output.  Initialized in {@link #main(String[])}.
    */
   private static PrintWriter ms_pWriter = null;
   
   /**
    * The source directory path.  Initialized in {@link #main(String[])}.
    */
   private static String ms_oldRoot = null;
   
   /**
    * The target directory path.  Initialized in {@link #main(String[])}.
    */
   private static String ms_newRoot = null;
   
   /**
    * Entry name for the manifest file.
    */   
   private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
   
   /**
    * Name of the manifest property used for storing version information.
    */
   private static final String IMPL_VERSION = "Implementation-Version:";
}
