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
package com.percussion.services.filestorage.impl;

import com.percussion.server.PSServer;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.filestorage.IPSFileDigestService;
import com.percussion.services.filestorage.IPSFileMeta;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.IPSHashedFieldCataloger;
import com.percussion.services.filestorage.PSFileStorageServiceLocator;
import com.percussion.services.filestorage.PSHashedFieldCatalogerLocator;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.data.PSMeta;
import com.percussion.services.filestorage.error.PSFileStorageException;
import com.percussion.util.IOTools;
import com.percussion.util.PSPurgableTempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSDbStorageServiceTest extends ServletTestCase
{
   private PSPurgableTempFile testXml;

   private PSPurgableTempFile testTxt;

   private PSPurgableTempFile noFilenameTxt;

   /**
    * Each unit test stores here the hashes that will be removed by the setUp
    * method.
    */
   private static List<String> hashesToRemove;

   @Override
   public void setUp()
   {
      try
      {
         testXml = createFile("<xml>This is a test xml file</xml>", "test1.xml", "text/xml", "UTF-8");
         testTxt = createFile("This is a test txt file", "test2.txt", "text/plain", "UTF-8");
         noFilenameTxt = createFile("This is a test txt file no filename", null, null, null);
         // Delete all PSHashedFile and PSHashedMeta objects in
         // the database
         IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

         if (hashesToRemove == null)
         {
            hashesToRemove = new ArrayList<String>();
         }
         else
         {
            for (String hash : hashesToRemove)
               fssvc.delete(hash);

            hashesToRemove.clear();
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Also tests using File instead of PurgeableTempFile
    * 
    * @throws Exception
    */
   public void testStore_HugeFile() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String hugeFilePath = PSServer.getRxFile("InstallableApps/RxApp/rxapp.ear");

      String hugeFileHash = fssvc.store(new File(hugeFilePath));
      assertNotNull(hugeFileHash);
      assertTrue(fssvc.fileExists(hugeFileHash));
      hashesToRemove.add(hugeFileHash);
   }

   /**
    * Test error returned if not filename for purgeable temp file
    * 
    * @throws Exception
    */
   @Test
   public void testStore_NoFilename() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      PSMeta xmlMeta = new PSMeta();
      assertTrue(xmlMeta.isEmpty());

      try
      {
         fssvc.store(noFilenameTxt);
         assertTrue(false);
      }
      catch (PSFileStorageException e)
      {
         // this is expected
      }

   }

   /**
    * @throws Exception
    */
   public void testStore() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String xmlHash = fssvc.store(testXml);
      assertNotNull(xmlHash);

      assertTrue(fssvc.fileExists(xmlHash));
      hashesToRemove.add(xmlHash);

      String txtHash = fssvc.store(testTxt);
      assertNotNull(txtHash);
      assertTrue(fssvc.fileExists(txtHash));
      hashesToRemove.add(txtHash);

      assertFalse(txtHash.equals(xmlHash));
   }

   /**
    * @throws Exception
    */
   public void testStore_HashAlreadyExists() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String xmlHash = fssvc.store(testXml);
      assertNotNull(xmlHash);

      assertTrue(fssvc.fileExists(xmlHash));
      hashesToRemove.add(xmlHash);

      // Save the same file again
      String xmlHash2 = fssvc.store(testXml);
      assertNotNull(xmlHash2);

      assertTrue(fssvc.fileExists(xmlHash2));

      assertEquals(xmlHash, xmlHash2);
   }

   /**
    * 
    * @throws Exception
    */
   public void testDelete() throws Exception
   {
      // Arrange
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String xmlHash = fssvc.store(testXml);
      assertNotNull(xmlHash);

      assertTrue(fssvc.fileExists(xmlHash));

      // Act
      fssvc.delete(xmlHash);

      // Assert
      assertFalse(fssvc.fileExists(xmlHash));

      IPSFileMeta meta = fssvc.getMeta(xmlHash);
      assertNotNull(meta);
      assertTrue(meta.isEmpty());
   }

   /**
    * 
    * @throws Exception
    */
   public void testDelete_ObjectDoesNotExist() throws Exception
   {
      // If the object to delete does not exist, then quitting
      // silently is expected.

      // Arrange
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      // Act
      fssvc.delete("NonExistantHash");
   }

   /**
    * @throws Exception
    */
   public void testFileExists() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String xmlHash = fssvc.store(testXml);
      assertTrue(fssvc.fileExists(xmlHash));
      assertFalse(fssvc.fileExists("foo"));
      hashesToRemove.add(xmlHash);
   }

   /**
    * @throws Exception
    */
   public void testGetStream() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String xmlHash = fssvc.store(testXml);
      InputStream xmlIn = fssvc.getStream(xmlHash);
      assertNotNull(xmlIn);
      assertTrue(IOTools.compareStreams(new FileInputStream(testXml), xmlIn));
      hashesToRemove.add(xmlHash);

      String txtHash = fssvc.store(testTxt);
      assertFalse(IOTools.compareStreams(fssvc.getStream(xmlHash), fssvc.getStream(txtHash)));
      hashesToRemove.add(txtHash);

      assertNull(fssvc.getStream("foo"));
   }

   /**
    * @throws Exception
    */
   public void testGetMeta() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      String xmlHash = fssvc.store(testXml);
      IPSFileMeta xmlMeta = fssvc.getMeta(xmlHash);
      assertFalse(xmlMeta.isEmpty());
      assertEquals(xmlMeta.entrySet(), fssvc.getMeta(xmlHash).entrySet());
      hashesToRemove.add(xmlHash);

      String txtHash = fssvc.store(testTxt);
      IPSFileMeta txtMeta = fssvc.getMeta(txtHash);
      assertFalse(txtMeta.isEmpty());
      assertEquals(txtMeta.entrySet(), fssvc.getMeta(txtHash).entrySet());
      assertFalse(txtMeta.equals(xmlMeta));
      hashesToRemove.add(txtHash);
   }

   /**
    * @throws Exception
    */
   public void testGetAlgorithm() throws Exception
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();

      assertNotNull(fssvc.getAlgorithm());
   }

   private PSPurgableTempFile createFile(String content, String sourceFile, String contentType, String encType)
         throws IOException
   {
      PSPurgableTempFile f = new PSPurgableTempFile("tmp", "tmp", null, sourceFile, contentType, encType);

      FileWriter fw = new FileWriter(f);
      fw.write(content);
      fw.close();

      return f;
   }

   @Test
   public void testCountOld()
   {
      // Need to set some to older time and test
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();
      assertEquals(0, fssvc.countOlderThan(1));
   }

   @Test
   public void testDeleteOld()
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();
      fssvc.deleteOlderThan(1);
      assertEquals(0, fssvc.countOlderThan(1));
   }

   @Test
   public void testTouchHashes()
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();
      IPSHashedFieldCataloger service = PSHashedFieldCatalogerLocator.getHashedFileCatalogerService();
      Set<PSHashedColumn> columns = service.validateColumns();
      fssvc.touchAllHashes(columns);
   }

   @Test
   public void testExportAll()
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();
      fssvc.exportAllBinary("exportTest");
   }

   @Test
   public void testExportAllLegacy()
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();
      fssvc.exportAllLegacyBinary("exportTest");
   }

   @Test
   public void testImportAll()
   {
      IPSFileStorageService fssvc = PSFileStorageServiceLocator.getFileStorageService();
      fssvc.importAllBinary("exportTest");
   }

   private IPSFileDigestService getFileDigestService()
   {
      return (IPSFileDigestService) PSBaseServiceLocator.getBean("sys_digestService");
   }
}
