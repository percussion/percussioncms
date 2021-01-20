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
import com.percussion.deployer.objectstore.PSArchiveInfoTest;
import com.percussion.deployer.objectstore.PSArchiveManifestTest;
import com.percussion.deployer.objectstore.PSDatasourceMap;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.server.PSServer;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the <code>PSArchivHander</code> object.
 */
@Category(IntegrationTest.class)
public class PSArchiveHandlerTest
{
   /**
    * Test all archive handler functionalities
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testAll() throws Exception
   {
      // create a new archive and its handler for WRITING
      PSArchiveInfo info1 = PSArchiveInfoTest.getArchiveInfo(true);
      PSArchive archive = new PSArchive(new File(PSServer.getRxDir().getAbsolutePath(), "ArchiveTest.pda"), info1);
      PSArchiveHandler ah = new PSArchiveHandler(archive);

      // prepare data
      PSDeployableElement dep1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element",
         "myTestElement", true, false, false);
      PSDeployableElement dep2 = new PSDeployableElement(
         PSDependency.TYPE_LOCAL, "2", "TestElem2", "Test Element2",
         "myTestElement2", true, false, false);
      List depFiles1 = getDepFiles(dep1);
      List depFiles2 = getDepFiles(dep2);

      // add the data to the archive
      ah.addFiles(dep1, depFiles1.iterator());
      ah.addFiles(dep2, depFiles2.iterator());

      // add dbmsinfo for one dep
      PSDatasourceMap dsMap = new PSDatasourceMap("RhythmyxData", "");
      List<PSDatasourceMap> infoList = new ArrayList<PSDatasourceMap>();
      infoList.add(dsMap);      
      ah.addDbmsInfoList(dep1, infoList);
      
      ah.close(); // Save the archive

      // retrieve the data file the archive for READING
      archive = new PSArchive( new File("ArchiveTest.pda") );
      ah = new PSArchiveHandler(archive);

      Iterator tgtDepFiles1 = ah.getFiles(dep1);
      Iterator tgtDepFiles2 = ah.getFiles(dep2);
      PSDependency tgtDep1 = getDepFromDepFile(ah, tgtDepFiles1.next());
      PSDependency tgtDep2 = getDepFromDepFile(ah, tgtDepFiles2.next());

      // get the iterator again since it moved from above
      tgtDepFiles1 = ah.getFiles(dep1);
      tgtDepFiles2 = ah.getFiles(dep2);

      // compare the data
      assertTrue(PSArchiveManifestTest.doesFileListTheSame(depFiles1.iterator(),
         tgtDepFiles1));
      assertTrue(PSArchiveManifestTest.doesFileListTheSame(depFiles2.iterator(),
         tgtDepFiles2));

      assertTrue( dep1.equals(tgtDep1) );
      assertTrue( dep2.equals(tgtDep2) );
      
      // check the dbmsinfo was restored
      assertEquals(archive.getArchiveManifest().getDbmsInfoList(tgtDep1), 
         infoList);

      ah.close();
   }

   /**
    * Get a specified dependency object from a Archive, the dependency object
    * is specified by a dependency file.
    *
    * @param ah The archive object, assume not <code>null</code>.
    * @param obj The <code>PSDependencyFile</code> object, which reference to
    * the to be retrieved dependency object.
    *
    * @return The specified dependency object, it will never be
    * <code>null</code>.
    *
    * @throws Exception if any error occures.
    */
   private static PSDependency getDepFromDepFile(PSArchiveHandler ah,
      Object obj) throws Exception
   {
      PSDependencyFile depFile = (PSDependencyFile) obj;
      InputStream in = ah.getFileData(depFile);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      Element depEl = doc.getDocumentElement();
      PSDeployableElement dep1 = new PSDeployableElement(depEl);
      return dep1;
   }

   /**
    * Creating a list with one dependency file item from a given dependency
    * object. The dependency file points to a file which contains the
    * dependency object in XML format.
    *
    * @param dep1 The dependency object. Assume it is not <code>null</code>
    *
    * @return A list with one <code>PSDependencyFile</code> object, it will
    * never <code>null</code> or empty.
    *
    * @throws IOException if any error occures.
    */
   private static List getDepFiles(PSDependency dep1) throws IOException
   {
      List<PSDependencyFile> depFiles = new ArrayList<PSDependencyFile>();
      depFiles.add(getDepFile(dep1));
      return depFiles;
   }

   /**
    * Writing a given dependency object (in XML format) into a temporary file
    * and creating a dependency file from it.
    *
    * @param dep The dependency object to be written to a temporary file.
    * Assuming it is not <code>null</code>.
    *
    * @return The created dependency file object. It will never be
    * <code>null</code>.
    *
    * @throws IOException if any error occures.
    */
   private static PSDependencyFile getDepFile(PSDependency dep)
      throws IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element depEl = dep.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, depEl);

      File exportDir = new File(PSDeploymentHandler.EXPORT_ARCHIVE_DIR);
      exportDir.mkdirs();

      File file = File.createTempFile("dep_", ".xml", exportDir);
      FileOutputStream out = new FileOutputStream(file);
      PSXmlDocumentBuilder.write(doc, out);

      PSDependencyFile depFile = new PSDependencyFile(
         PSDependencyFile.TYPE_EXTENSION_DEF_XML, file);

      return depFile;
   }

}
