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
package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveManifest;
import com.percussion.util.IOTools;
import com.percussion.util.PSArchiveFiles;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the PSPackageLockManager.
 */
@SuppressWarnings("unchecked")
@Category(IntegrationTest.class)
public class PSPackageLockManagerTest
{
   /**
    * Tests lock/unlock.
    * 
    * @throws Exception If there is an error.
    */
   @Test
   public void testAll() throws Exception
   {
      File tmpFile = null;
      ZipFile zipFile1 = null;
      PSArchive archive1 = null;
      PSArchive archive2 = null;
      
      try
      {
         tmpFile = File.createTempFile("tmp", null);
         tmpFile.deleteOnExit();
         tmpFile.delete();
         tmpFile.mkdir();
         
         File resourceDir = new File(RESOURCE_DIR);
         File[] resourceFiles = resourceDir.listFiles();
         for (File file : resourceFiles)
         {
            if (file.isDirectory())
            {
               continue;
            }
            
            IOTools.copyToDir(file, tmpFile);
         }
         
         PSPackageLockManager pkgLockMgr = new PSPackageLockManager();
         
         // test update directory
         
         // lock packages
         pkgLockMgr.update(tmpFile, true);
         
         File[] files = tmpFile.listFiles();
         for (File file : files)
         {
            assertTrue(isLocked(file));
         }
         
         // unlock packages
         pkgLockMgr.update(tmpFile, false);
         
         for (File file : files)
         {
            assertTrue(!isLocked(file));
         }
         
         // re-lock packages
         pkgLockMgr.update(tmpFile, true);
         
         for (File file : files)
         {
            assertTrue(isLocked(file));
         }
         
         // test update single package
         for (File file : resourceFiles)
         {
            if (file.isDirectory())
            {
               continue;
            }
            
            IOTools.copyToDir(file, tmpFile);
         }
         File f = files[0];
         
         Map<String, Long> entryMap1 = new HashMap<String, Long>();
         zipFile1 = new ZipFile(f);
         Enumeration entries = zipFile1.entries();
         while (entries.hasMoreElements())
         {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            entryMap1.put(entry.getName(), entry.getCrc());
         }
         zipFile1.close();
         
         // load the original manifest
         archive1 = new PSArchive(f);
         PSArchiveManifest manifest1 = archive1.getArchiveManifest();
         archive1.close();
                  
         // lock
         pkgLockMgr.update(f, true);
         assertTrue(isLocked(f));
         
         // unlock
         pkgLockMgr.update(f, false);
         assertTrue(!isLocked(f));
         
         // re-lock
         pkgLockMgr.update(f, true);
         assertTrue(isLocked(f));
         
         // load the manifest after locking
         archive2 = new PSArchive(f);
         PSArchiveManifest manifest2 = archive2.getArchiveManifest();
         archive2.close();
                  
         Map<String, Long> entryMap2 = new HashMap<String, Long>();
         zipFile1 = new ZipFile(f);
         entries = zipFile1.entries();
         while (entries.hasMoreElements())
         {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            entryMap2.put(entry.getName(), entry.getCrc());
         }
         zipFile1.close();
         
         assertEquals(entryMap1.size(), entryMap2.size());
         
         Iterator<String> iter = entryMap2.keySet().iterator();
         while (iter.hasNext())
         {
            String key = iter.next();
            if (key.equals(PSArchive.ARCHIVE_INFO_PATH) ||
                  key.equals(PSArchive.ARCHIVE_MANIFEST_PATH))
            {
               // archive info may be different, archive manifest contents may
               // have different order
               continue;
            }
            
            assertEquals(entryMap1.get(key), entryMap2.get(key));
         }
         
         // make sure archive manifests are equal
         assertEquals(manifest1, manifest2);
      }
      finally
      {
         if (archive1 != null)
         {
            archive1.close();
         }
         
         if (archive2 != null)
         {
            archive2.close();
         }
         
         if (zipFile1 != null)
         {
            zipFile1.close();
         }
         
         if (tmpFile != null)
         {
            IOTools.deleteFile(tmpFile);
         }
      }
   }
   
   /**
    * Determines if the specified package file is locked.
    * 
    * @param packageFile The package file, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the package is locked, <code>false</code> if
    * it is unlocked.
    * 
    * @throws Exception If there is an error.
    */
   private boolean isLocked(File packageFile) throws Exception
   {
      ZipFile zip = null;
      InputStream in = null;
      
      try
      {
         zip = new ZipFile(packageFile);
         in = PSArchiveFiles.getFile(zip, PSArchive.ARCHIVE_INFO_PATH);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in,
               false);
         PSArchiveInfo info = new PSArchiveInfo(doc.getDocumentElement());
         return !info.isEditable();
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
         
         if (zip != null)
         {
            zip.close();
         }
      }
   }
   
   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_DIR =
      "Packages/Percussion";
  
}
