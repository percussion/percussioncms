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
package com.percussion.install;

import com.percussion.util.IOTools;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Test case for the {@link InstallUtil} class.
 */
public class InstallUtilTest
{
   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();

   public InstallUtilTest(){}

   @Before
   public void setup() throws IOException {
      ms_root = temporaryFolder.newFolder().getAbsolutePath();
   }
   /**
    * Tests the <code>getSubstituteName</code> method.
    */
   @Test
   public void testGetSubstituteName()
   {
      String variableName = "VARIABLE";
      String substituteName = '$' + variableName + '$';
      assertEquals(InstallUtil.getSubstituteName(variableName), substituteName);
   }

   /**
    * Tests the <code>getVariableName</code> method for multiple cases.
    */
   @Test
   public void testGetVariableName()
   {
      testGetVariableName("com.percussion.install.Test", "var", "Test_var");
      testGetVariableName("Test", "var", "Test_var");
      testGetVariableName("com.percussion.install.Test.", "var",
            "com.percussion.install.Test._var");
   }

   /**
    * Tests the backup/restore operations on rxrepository.properties.
    */
   @Test
   public void testRepositoryPropsFileOps() throws Exception
   {
      File orig = null;
      File backup = null;
      FileWriter fw = null;
      
      try
      {
         // Create repository properties file, including necessary parent dirs
         orig = new File(ms_root, InstallUtil.REPOSITORY_PROPS_FILE);
         orig.getParentFile().mkdirs();
         orig.createNewFile();
         fw = new FileWriter(orig);
         fw.write("Original content");
         fw.close();

         // Load repository properties file
         String origContent = IOTools.getFileContent(orig);

         // Make sure backup does not exist
         backup = new File(ms_root, InstallUtil.ORIG_REPOSITORY_PROPS_FILE);
         assertFalse(backup.exists());

         // Create backup
         InstallUtil.backupRepositoryPropertyFile(ms_root);

         // Compare backup with original
         assertTrue(backup.exists());
         assertTrue(IOTools.getFileContent(backup).equals(origContent));

         // Modify original
         fw = new FileWriter(orig);            
         fw.write("New content");
         fw.close();
         assertFalse(IOTools.getFileContent(orig).equals(origContent));

         // Restore original from backup
         InstallUtil.restoreRepositoryPropertyFile(ms_root);

         // Compare restored file with original
         assertTrue(IOTools.getFileContent(orig).equals(origContent));

         // Backup should have been deleted
         assertFalse(backup.exists());
      }
      finally
      {
         if (fw != null)
         {
            fw.close();
         }
         
         if (orig != null)
         {
            orig.delete();
         }
         
         if (backup != null)
         {
            backup.delete();
         }
      }
   }
   @Test
   public void testSilentLogging() throws Exception
   {
      PSLogger.init(ms_root);
      clearLogContent();
      assertTrue(getLogContent().isEmpty());
      
      assertFalse(InstallUtil.isSilentInstall());
      Connection conn = InstallUtil.createConnection("jtds", "bar", "admin", "demo");
      assertNull(conn);
      assertTrue(getLogContent().isEmpty());
      
      InstallUtil.setIsSilentInstall(true);
      conn = InstallUtil.createConnection("jtds", "bar", "admin", "demo");
      assertNull(conn);
      String logContent = getLogContent();
      //There shouldn't be anything in the log if silent is turned on
      assertTrue(logContent.isEmpty());
      clearLogContent();
      assertTrue(getLogContent().isEmpty());
   }
   
   /**
    * @throws IOException 
    * 
    */
   private void clearLogContent() throws IOException
   {
      File logFile = new File(ms_root, PSLogger.DEFAULT_LOG_FILE);
      FileUtils.writeStringToFile(logFile, "");
   }

   private String getLogContent() throws IOException
   {
      File logFile = new File(ms_root, PSLogger.DEFAULT_LOG_FILE);
      return FileUtils.readFileToString(logFile);
   }

   /**
    * Tests the <code>getVariableName</code> method.
    * 
    * @param fullClassName the full class name.
    * @param varName the variable name.
    * @param variableName the expected install variable name.
    * 
    */
   private void testGetVariableName(String fullClassName, String varName,
         String variableName)
   {
      assertEquals(InstallUtil.getVariableName(fullClassName, varName),
            variableName);
   }
   
   /**
    * Acts as the root directory for tests.
    */
   private  String ms_root;
}


