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
package com.percussion.ant;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Fails build if finds bundled jar with the same name
 * existing in more than one manifest file.
 * It is possible to exclude some jars from the check by specifying them
 * as excludes.
 * A jar can be excluded if, for example, exporting the library from one
 * plugin to another will cause classloader problems.  
 *
 * @author Andriy Palamarchuk
 */
public class PSCheckManifestsForDuplicateFiles extends Task
{
   /**
    * Adds list of files to check.
    */
   public void addFileset(final FileSet fileset)
   {
      m_fileSets.add(fileset);
   }
   
   /**
    * Support for nested {@link Exclude} element.
    * @return newly created <code>Exclude</exclude> object,
    * never <code>null</code>.
    */
   public Exclude createExclude()
   {
      final Exclude exclude = new Exclude();
      m_excludes.add(exclude);
      return exclude;
   }

   /**
    * Executes the task.
    * @see org.apache.tools.ant.Task#execute()
    */
   @Override
   public void execute() throws BuildException
   {
      final Set<String> excludedLibs = getExcludedLibs();
      
      final Map<String, File> libFiles = new HashMap<String, File>();
      for (final File file : getManifestFiles())
      {
         final Set<String> libs = extractLibs(file);
         libs.removeAll(excludedLibs);
         for (final String lib : libs)
         {
            if (libFiles.containsKey(lib))
            {
               throw new BuildException(
                     "Manifest files \"" + libFiles.get(lib) + "\" and \""
                     + file + "\" both bundle library " + lib);
            }
            libFiles.put(lib, file);
         }
      }
   }

   /**
    * Creates set of filenames to be excluded from the check. 
    * @return The excludes names set stored in {@link #m_excludes}.
    */
   private Set<String> getExcludedLibs()
   {
      final Set<String> excludedLibs = new HashSet<String>();
      for (final Exclude exclude : m_excludes)
      {
         excludedLibs.add(exclude.getName());
      }
      return excludedLibs;
   }

   /**
    * Parses the provided manifest and extracts the specified libraries.
    * @return a set with library names, as specified in the manifest.
    */
   private Set<String> extractLibs(File file)
   {
      boolean libDelimiterFound = false;
      final Set<String> libs = new HashSet<String>();
      for (final String s : readLines(file))
      {
         if (libDelimiterFound)
         {
            if (StringUtils.isBlank(s))
            {
               break;
            }
            parseLibSpec(s, libs);
         }
         else
         {
            if (s.startsWith(LIB_SPEC_DELIM))
            {
               libDelimiterFound = true;
               parseLibSpec(s.substring(LIB_SPEC_DELIM.length()), libs);
            }
         }
      }
      if (!libDelimiterFound)
      {
         throw new BuildException(
               "Can't find library specifications in manifest " + file);
      }
      return libs;
   }

   /**
    * Adds library specification to the name-path map.
    */
   private void parseLibSpec(String s, Set<String> libs)
   {
      String path = s.trim();
      if (path.endsWith(","))
      {
         path = path.substring(0, path.length() - 1);
      }
      final File file = new File(path);
      libs.add(file.getName());
   }

   /**
    * Reads lines from the specified file. Any exceptions are wrapped in
    * {@link BuildException}.
    */
   @SuppressWarnings("unchecked")
   private List<String> readLines(File file)
   {
      try
      {
         final Reader reader = new FileReader(file);
         try
         {
            return IOUtils.readLines(reader);
         }
         finally
         {
            reader.close();
         }
      }
      catch (IOException e)
      {
         throw new BuildException(e);
      }
   }

   /**
    * Returns list of the specified manifest files.
    */
   private List<File> getManifestFiles()
   {
      if (m_fileSets.isEmpty())
      {
         throw new BuildException("No manifest files are specified");
      }

      final List<File> manifestFiles = new ArrayList<File>();
      
      for (final FileSet fileSet : m_fileSets)
      {
         final DirectoryScanner directoryScanner =
               fileSet.getDirectoryScanner(getProject());
         for (final String fileName : directoryScanner.getIncludedFiles())
         {
            final File file = new File(directoryScanner.getBasedir(), fileName);
            if (!manifestFiles.contains(file))
            {
               manifestFiles.add(file);
            }
         }
      }
      if (manifestFiles.isEmpty())
      {
         throw new BuildException("No manifest files were found");
      }
      if (manifestFiles.size() == 1)
      {
         throw new BuildException(
               "No manifest files to compare file \"" +
               manifestFiles.iterator().next() + "\" to. " +
               "At least 2 files should be specified");
      }
      return manifestFiles;
   }
   
   /**
    * Stores file name of a library excluded from the check.
    */
   public static class Exclude
   {
      /**
       * File name of a library to be excluded from a check.
       * @return name of a library to exclude which is set before by call to
       * {@link #getName()}. Never <code>null</code>.
       */
      public String getName()
      {
         return mi_name;
      }

      /**
       * @see #getName()
       * @param name file name of a library to be excluded. Should not be blank. 
       */
      public void setName(String name)
      {
         if (StringUtils.isBlank(name))
         {
            throw new IllegalArgumentException(
                  "Excluded file name should not be null");
         }
         mi_name = name;
      }
      
      private String mi_name;
   }

   /**
    * The line beginning for the library specification section.
    */
   private static final String LIB_SPEC_DELIM = "Bundle-ClassPath:";

   /**
    * Files to process.
    */
   private final List<FileSet> m_fileSets = new ArrayList<FileSet>();
   
   /**
    * Excludes - files do not include in processing.
    */
   private final List<Exclude> m_excludes = new ArrayList<Exclude>();
}
