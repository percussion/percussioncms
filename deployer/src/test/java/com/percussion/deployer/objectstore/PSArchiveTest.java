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
 
package com.percussion.deployer.objectstore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the <code>PSArchive</code> object.
 */
public class PSArchiveTest
{
   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();
   private String rxdeploydir;

   @Before
   public void setup() throws IOException {

      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   /**
    * Construct this unit test
    *
    */
   public PSArchiveTest()
   {
      super();
   }
   
   /**
    * Test all archive functionality
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testArchive() throws Exception
   {
      File archiveFile = File.createTempFile("ArchiveTest", ".pda");
      archiveFile.deleteOnExit();

      // create a new archive
      PSArchiveInfo info1 = PSArchiveInfoTest.getArchiveInfo(true);
      String archiveRef = "ref1";
      info1.setArchiveRef(archiveRef);
      PSArchive archive = new PSArchive(archiveFile, info1);
      
      PSArchiveInfo info2 = archive.getArchiveInfo(true);
      assertEquals(info1, info2);
      PSArchiveManifest man = new PSArchiveManifest();
      assertNull(archive.getArchiveManifest());
      archive.storeArchiveManifest(man);
      assertNotNull(archive.getArchiveManifest());

      // be sure we can't read a file while opened for writing.      
      boolean caught;
      caught = false;
      try 
      {
         archive.getFile("testFile.xml");
      }
      catch (IllegalStateException e) 
      {
         caught = true;
      }
      assertTrue(caught);
      
      archive.close();
      
      // be sure we can't write after closing.      
      caught = false;
      try 
      {
         archive.storeArchiveManifest(man);
      }
      catch (IllegalStateException e) 
      {
         caught = true;
      }
      assertTrue(caught);
      
      // should always be able to get the info object.
      caught = false;
      try 
      {
         archive.getArchiveInfo(false);
      }
      catch (IllegalStateException e) 
      {
         caught = true;
      }
      assertTrue(!caught);
      
      // now open for reading
      archive = new PSArchive(archiveFile);
      info2 = archive.getArchiveInfo(true);
      
      // archive open will clear the dbmsinfo since there were no external dbms 
      // listed in the manifest
      PSArchiveDetail detail1 = info1.getArchiveDetail();
      Iterator pkgs = detail1.getPackages();
      while (pkgs.hasNext())
      {
         detail1.setDbmsInfoList((PSDeployableElement) pkgs.next(), 
            new ArrayList());
      }
      
      // archive ref will now be filename
      assertTrue(!info1.equals(info2));
      String newArchiveRef = archiveFile.getName();
      newArchiveRef = newArchiveRef.substring(0, 
         newArchiveRef.lastIndexOf("."));
      
      info1.setArchiveRef(newArchiveRef);
      assertEquals(info1, info2);  
      assertNotNull(archive.getArchiveManifest());
      
      // be sure we can't write a file while opened for writing.      
      caught = false;
      try 
      {
         archive.storeArchiveManifest(man);
      }
      catch (IllegalStateException e) 
      {
         caught = true;
      }
      assertTrue(caught);
      archive.close();
      
      // be sure we can't read after closing.      
      caught = false;
      try 
      {
         archive.getFile("testFile.xml");
      }
      catch (IllegalStateException e) 
      {
         caught = true;
      }
      assertTrue(caught);
      
   }

   
}
