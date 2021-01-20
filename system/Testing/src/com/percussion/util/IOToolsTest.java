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
package com.percussion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import junit.framework.TestCase;

public class IOToolsTest extends TestCase
{
   public void testDeleteFile()  throws IOException
   {
      // delete an empty dir
      final File testDir = File.createTempFile("tmp", "dir");
      testDir.delete();
      testDir.mkdir();
      assertTrue(testDir.exists());
      IOTools.deleteFile(testDir);
      assertFalse(testDir.exists());
      
      // delete a file
      testDir.mkdir();
      final File file1 = new File(testDir, "file1");
      file1.createNewFile();
      assertTrue(file1.exists());
      IOTools.deleteFile(file1);
      assertFalse(file1.exists());
      
      // delete a dir recursively
      testDir.mkdir();
      file1.createNewFile();
      final File dir1 = new File(testDir, "dir1");
      dir1.mkdir();
      
      final File file2 = new File(dir1, "file2.txt");
      file2.createNewFile();

      assertTrue(testDir.exists());
      assertTrue(dir1.exists());
      assertTrue(file1.exists());
      assertTrue(file2.exists());
      
      IOTools.deleteFile(testDir);

      assertFalse(testDir.exists());
      assertFalse(dir1.exists());
      assertFalse(file1.exists());
      assertFalse(file2.exists());
   }

   public void testCopyToDir() throws IOException
   {
      final String F1_CONTENT = "f1 content";
      final String F2_CONTENT = "Some f2 content";

      final File testDir = File.createTempFile("tmp", "dir");
      testDir.delete();
      testDir.mkdir();
      final File file1 = new File(testDir, "file1");
      initFileWith(file1, F1_CONTENT);
      final File dir1 = new File(testDir, "dir1");
      dir1.mkdir();
      final File file2 = new File(dir1, "file2.txt");
      initFileWith(file2, F2_CONTENT);
      
      final File copyToDir = File.createTempFile("tmp", "dir");
      copyToDir.delete();
      copyToDir.mkdir();

      try
      {
         IOTools.copyToDir(testDir, copyToDir);
         final File testDirCopy = new File(copyToDir, testDir.getName());
         checkEqualFiles(testDir, testDirCopy);
         final File file1Copy = new File(testDirCopy, file1.getName());
         checkEqualFiles(file1, file1Copy);
         final File dir1Copy = new File(testDirCopy, dir1.getName());
         final File file2Copy = new File(dir1Copy, file2.getName());
         checkEqualFiles(file2, file2Copy);
      }
      finally
      {
         while (testDir.exists())
         {
            // The copy action uses FileChannel which may maintain access to the
            // file for a short time after the close method is invoked
            IOTools.deleteFile(testDir);
         }
         IOTools.deleteFile(copyToDir);
      }
   }
   
   public void testcopyStreamToFile() throws IOException
   {
      final String F1_CONTENT = "f1 content";
      final File file1 = File.createTempFile("tmp", "file1");
      initFileWith(file1, F1_CONTENT);
      final File file2 = File.createTempFile("tmp", "file2");
      
      FileInputStream inStream = null;
      
      try
      {
         inStream = new FileInputStream(file1);
         IOTools.copyStreamToFile(inStream, file2);
      }
      finally
      {
         if(inStream != null)
         {
            inStream.close();
         }
      }
   }
   

   public void testCopyToDirs() throws IOException
   {
      final String F2_CONTENT = "Some f2 content";

      final File testDir = File.createTempFile("tmp", "dir");
      testDir.delete();
      testDir.mkdir();
      final File dir1 = new File(testDir, "dir1");
      dir1.mkdir();
      final File file2 = new File(dir1, "file2.txt");
      initFileWith(file2, F2_CONTENT);
      final File dir2 = new File(dir1, "dir2");
      dir2.mkdir();
      
      final File copyToDir1 = File.createTempFile("tmp", "dir");
      copyToDir1.delete();
      copyToDir1.mkdir();
      
      final File copyToDir2 = File.createTempFile("tmp", "dir");
      copyToDir2.delete();
      copyToDir2.mkdir();

      List copyToDirs = new ArrayList();
      copyToDirs.add(copyToDir1);
      copyToDirs.add(copyToDir2);
      
      try
      {
         IOTools.copyToDirs(dir1, copyToDirs);
         checkEqualDirs(dir1, copyToDir1);
         checkEqualDirs(dir1, copyToDir2);
      }
      finally
      {
         while (testDir.exists())
         {
            IOTools.deleteFile(testDir);
         }
         IOTools.deleteFile(copyToDir1);
         IOTools.deleteFile(copyToDir2);
      }
   }   
   
   public void testGetFileContent() throws IOException
   {
      final String F1_CONTENT = "f1 content";
      final File file1 = File.createTempFile("tmp", "file");
      initFileWith(file1, F1_CONTENT);
      
      try
      {
         String file1Str = IOTools.getFileContent(file1);
         assertTrue(file1Str.equals(F1_CONTENT));
      }
      finally
      {
         file1.delete();
      }
   }   
   
   public void testCreateTempFile() throws IOException
   {
      final String F1_CONTENT = "f1 content";
      final File testDir = File.createTempFile("tmp", "dir");
      testDir.delete();
      testDir.mkdir();
      final File file1 = new File(testDir, "file1");
      initFileWith(file1, F1_CONTENT);
      File tempFile = null;
      
      try
      {
         tempFile = IOTools.createTempFile(file1);
         checkEqualFiles(file1, tempFile);
      }
      finally
      {
         IOTools.deleteFile(testDir);
         IOTools.deleteFile(tempFile);
      }
   }
   
   public void testCreateBackupFile() throws IOException
   {
      final String F1_CONTENT = "f1 content";
      final File testDir = File.createTempFile("tmp", "dir");
      testDir.delete();
      testDir.mkdir();
      final File file1 = new File(testDir, "file1");
      initFileWith(file1, F1_CONTENT);
      final File file2 = new File(testDir, "file2.txt");
      initFileWith(file2, F1_CONTENT);
      final File file3 = new File(testDir, ".file3");
      initFileWith(file3, F1_CONTENT);
      
      File backupFile = null;
      
      try
      {
         backupFile = IOTools.createBackupFile(file1);
         assertTrue(backupFile.getName().endsWith(".000"));
         checkEqualFiles(file1, backupFile);
         
         File backupFile2 = IOTools.createBackupFile(file1);
         assertTrue(backupFile2.getName().endsWith(".001"));
         checkEqualFiles(file1, backupFile2);
         
         backupFile = IOTools.createBackupFile(file2);
         assertTrue(backupFile.getName().endsWith(".000"));
         checkEqualFiles(file2, backupFile);
         
         backupFile2 = IOTools.createBackupFile(file2);
         assertTrue(backupFile2.getName().endsWith(".001"));
         checkEqualFiles(file2, backupFile2);
         
         backupFile = IOTools.createBackupFile(file3);
         assertTrue(backupFile.getName().endsWith(".000"));
         checkEqualFiles(file3, backupFile);
         
         backupFile2 = IOTools.createBackupFile(file3);
         assertTrue(backupFile2.getName().endsWith(".001"));
         checkEqualFiles(file3, backupFile2);
      }
      finally
      {
         IOTools.deleteFile(testDir);
      }
   }
   
   public void testGetChecksum() throws IOException
   {
      final String F1_CONTENT = "f1 content";
      final File file1 = File.createTempFile("tmp", "file");
      initFileWith(file1, F1_CONTENT);
      
      FileWriter fw = null;
      try
      {
         // test file checksum
         long checksum = IOTools.getChecksum(file1);
         
         CRC32 crc = new CRC32();
         crc.update(F1_CONTENT.getBytes());
         
         assertEquals(checksum, crc.getValue());
         
         fw = new FileWriter(file1, true);
         fw.write("some more content");
         fw.close();
         
         long checksum2 = IOTools.getChecksum(file1);
         assertFalse(checksum2 == checksum);
         
         // test string checksum
         long checksum3 = IOTools.getChecksum(F1_CONTENT);
         assertEquals(checksum3, crc.getValue());
         
         String str = F1_CONTENT + " some more content";
         long checksum4 = IOTools.getChecksum(str);
         assertFalse(checksum4 == checksum3);
      }
      finally
      {
         file1.delete();
         
         if (fw != null)
            fw.close();
      }
   }
   
   /**
    * Ensures the files are equal.
    */
   private void checkEqualFiles(final File file, final File copy)
   {
      assertTrue(file.exists());
      assertTrue(copy.exists());
      assertTrue(!(file.isDirectory() ^ copy.isDirectory()));
      if (file.isFile())
      {
         assertEquals(file.length(), copy.length());
      }
   }
   

   /**
    * Ensures the directories contain the same number of files and the files
    * have the same name.
    */
   private void checkEqualDirs(final File dir1, final File dir2)
   {
      assertTrue(dir1.exists());
      assertTrue(dir2.exists());
      assertTrue(!(dir1.isFile() || dir2.isFile()));
      assertTrue(dir1.listFiles().length == dir2.listFiles().length);
      
      for (int i = 0; i < dir1.listFiles().length; i++)
      {
         assertTrue(dir1.listFiles()[i].getName().equals(
               dir2.listFiles()[i].getName()));
      }
   }
   
   /**
    * Creates file with the specified content.
    */
   private void initFileWith(final File f, final String content) throws IOException
   {
      final Writer w = new FileWriter(f);
      w.write(content);
      w.close();
   }
}
