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

import com.percussion.utils.testing.UnitTest;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(UnitTest.class)
public class PSCheckManifestsForDuplicateFilesTest
{
   public PSCheckManifestsForDuplicateFilesTest(){}

   @Before
   public void setUp() throws Exception
   {
      m_file1 = createTmpFile();
      m_file2 = createTmpFile();
      
      m_fileSet1 = new FileSet();
      m_fileSet1.setFile(m_file1);

      m_fileSet2 = new FileSet();
      m_fileSet2.setFile(m_file2);
   }

   /**
    * Creates empty temporary file for testing.
    */
   private File createTmpFile() throws IOException
   {
      final File file = File.createTempFile("checkManifestDups", "tmp");
      file.deleteOnExit();
      return file;
   }

   @After
   public void tearDown() throws Exception
   {
      m_file1.delete();
      m_file2.delete();
   }

   @Test
   public void testMissingFileSet()
   {
      final PSCheckManifestsForDuplicateFiles task =
            new PSCheckManifestsForDuplicateFiles();
      try
      {
          task.execute();
          fail();
      }
      catch (BuildException e)
      {
          assertTrue(e.getMessage().toLowerCase().contains("manifest"));
      }
   }

   @Test
   public void testNoFiles()
   {
      final PSCheckManifestsForDuplicateFiles task = createTask();
      task.addFileset(m_fileSet1);
      task.addFileset(m_fileSet2);
      m_file1.delete();
      m_file2.delete();

      try
      {
          task.execute();
          fail();
      }
      catch (BuildException success) {}
   }

   @Test
   public void test1File() throws IOException
   {
      final PSCheckManifestsForDuplicateFiles task = createTask();
      task.addFileset(m_fileSet1);
      writeToFile(MANIFEST1, m_file1);
      task.addFileset(m_fileSet2);
      m_file2.delete();

      try
      {
          task.execute();
          fail();
      }
      catch (BuildException e)
      {
         assertTrue(e.getMessage().contains(m_file1.toString()));
      }
   }

   @Test
   public void testWrongFormat() throws IOException
   {
      final PSCheckManifestsForDuplicateFiles task = createTask();
      task.addFileset(m_fileSet1);
      writeToFile("ssss", m_file1);
      task.addFileset(m_fileSet2);
      writeToFile(MANIFEST2, m_file2);

      try
      {
          task.execute();
          fail();
      }
      catch (BuildException e)
      {
         assertTrue(e.getMessage().contains(m_file1.toString()));
      }
   }

   /**
    * Creates and initializes the task.
    */
   private PSCheckManifestsForDuplicateFiles createTask()
   {
      final PSCheckManifestsForDuplicateFiles task =
         new PSCheckManifestsForDuplicateFiles();
      task.setProject(new Project());
      return task;
   }

   @Test
   public void testDuplicateCheck_Ok() throws IOException
   {
      final PSCheckManifestsForDuplicateFiles task = createTask();
      task.addFileset(m_fileSet1);
      task.addFileset(m_fileSet2);
      
      writeToFile(MANIFEST1, m_file1);
      writeToFile(MANIFEST2, m_file2);

      task.execute();
   }

   @Test
   public void testDuplicateCheck_Dup() throws IOException
   {
      final PSCheckManifestsForDuplicateFiles task = createTask();
      task.addFileset(m_fileSet1);
      task.addFileset(m_fileSet2);
      
      writeToFile(MANIFEST1, m_file1);
      writeToFile(MANIFEST2_DUP, m_file2);

      try
      {
         task.execute();
         fail();
      }
      catch (BuildException e)
      {
         final String msg = e.getLocalizedMessage();
         assertTrue(msg.contains("spring.jar"));
         assertTrue(msg.contains(m_file1.getName()));
         assertTrue(msg.contains(m_file2.getName()));
      }
      
      // specify duplicated jar as exclude
      task.createExclude().setName("..\\..\\system\\Tools\\Spring\\" + DUP_JAR);
      task.execute();
   }

   @Test
   public void testExclude()
   {
      PSCheckManifestsForDuplicateFiles.Exclude e =
            new PSCheckManifestsForDuplicateFiles.Exclude();
      assertNull(e.getName());
      try
      {
         e.setName(" \n\t");
         fail();
      }
      catch (IllegalArgumentException success)
      {
      }
      e.setName(DUP_JAR);
      assertEquals(DUP_JAR, e.getName());
   }
 
   /**
    * Writes provided string to the file.
    */
   private void writeToFile(String string, File file) throws IOException
   {
      try (Writer writer = new FileWriter(file)) {
         IOUtils.write(string, writer);
      }
   }

   /**
    * The library existing in both - {@link #MANIFEST1} and {@link #MANIFEST2_DUP};
    */
   private static final String DUP_JAR = "spring.jar";

   /**
    * Sample manifest 1.
    */
   private static final String MANIFEST1 = "Manifest-Version: 1.0\n" + 
         "Bundle-ManifestVersion: 2\n" + 
         "Bundle-Name: Rhythmyx Designer Core Plug-in\n" + 
         "Bundle-SymbolicName: com.percussion.client\n" + 
         "Bundle-Version: 1.0.0\n" + 
         "Bundle-Vendor: Percussion Software\n" + 
         "Bundle-Localization: plugin\n" + 
         "Export-Package: com.percussion.client,\n" + 
         " org.apache.log4j\n" + 
         "Bundle-ClassPath: ..\\..\\system\\build\\lib\\rxclient.jar,\n" + 
         " ..\\..\\system\\Tools\\Spring\\spring-beans.jar,\n" + 
         " ..\\..\\system\\Tools\\Spring\\spring-context.jar,\n" + 
         " ..\\..\\system\\Tools\\Spring\\" + DUP_JAR + "\n" + 
         " \n" + 
         "Require-Bundle: org.eclipse.core.runtime,\n" + 
         " org.eclipse.core.runtime.compatibility\n";
   
   /**
    * Sample manifest 2 - no dups with 1.
    */
   private static final String MANIFEST2 = "Manifest-Version: 1.0\n" + 
         "Bundle-ManifestVersion: 2\n" + 
         "Bundle-Name: Rhythmyx Designer Core Plug-in\n" + 
         "Bundle-SymbolicName: com.percussion.client\n" + 
         "Bundle-Version: 1.0.0\n" + 
         "Bundle-Vendor: Percussion Software\n" + 
         "Bundle-Localization: plugin\n" + 
         "Export-Package: com.percussion.client,\n" + 
         " org.apache.log4j\n" + 
         "Bundle-ClassPath: ..\\..\\system\\Tools\\Commons\\commons-betwixt-0.7RC2.jar\n" + 
         " ..\\..\\system\\Tools\\Commons\\commons-codec-1.11.jar\n" + 
         " ..\\..\\system\\Tools\\Commons\\commons-lang-2.4.jar\n" + 
         " \n" + 
         "Require-Bundle: org.eclipse.core.runtime,\n" + 
         " org.eclipse.core.runtime.compatibility\n";

   /**
    * Sample manifest 2 - contains jar {@link #DUP_JAR} duplicated with
    * {@link #MANIFEST1}.
    */
   private static final String MANIFEST2_DUP = "Manifest-Version: 1.0\n" + 
         "Bundle-ManifestVersion: 2\n" + 
         "Bundle-Name: Rhythmyx Designer Core Plug-in\n" + 
         "Bundle-SymbolicName: com.percussion.client\n" + 
         "Bundle-Version: 1.0.0\n" + 
         "Bundle-Vendor: Percussion Software\n" + 
         "Bundle-Localization: plugin\n" + 
         "Export-Package: com.percussion.client,\n" + 
         " org.apache.log4j\n" + 
         "Bundle-ClassPath: ..\\..\\system\\Tools\\Spring\\" + DUP_JAR + ",\n" +
         " ..\\..\\system\\Tools\\Commons\\commons-codec-1.11.jar,\n" + 
         " ..\\..\\system\\Tools\\Commons\\commons-lang-2.4.jar\n" + 
         " \n" + 
         "Require-Bundle: org.eclipse.core.runtime,\n" + 
         " org.eclipse.core.runtime.compatibility\n";
   
   /**
    * Fileset containing file 1.
    */
   private FileSet m_fileSet1;

   /**
    * Fileset containing file 2.
    */
   private FileSet m_fileSet2;

   /**
    * File 1 used for testing.
    */
   private File m_file1;
   
   /**
    * File 2 used for testing.
    */
   private File m_file2;
}
