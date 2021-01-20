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

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Unit test class for the <code>PSArchiveManifestTest</code> class.
 */
public class PSArchiveManifestTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSArchiveManifestTest(String name)
   {
      super(name);
   }

   /**
    * Test all operations
    *
    * @throws Exception If there are any errors.
    */
   @SuppressWarnings("unchecked")
   public void testAll() throws Exception
   {
      // Creating PSArchiveManifest objects
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element",
         "myTestElement", true, false, false);

      PSDeployableElement de2 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "2", "TestElem2", "Test Element2",
         "myTestElement2", true, false, true);

      PSDeployableElement de3 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "3", "TestElem3", "Test Element3",
         "myTestElement3", true, false, true);

      File f1 = new File("file1");
      File loc1 = new File("archeFile1");
      File f2 = new File("file2");
      File loc2 = new File("archeFile2");
      File f3 = new File("file3");
      File loc3 = new File("archeFile3");
      File f4 = new File("file4");
      File loc4 = new File("archeFile4");

      PSDependencyFile depFile1 = new PSDependencyFile(
         PSDependencyFile.TYPE_APPLICATION_FILE, f1);
      depFile1.setArchiveLocation(loc1);
      PSDependencyFile depFile2 = new PSDependencyFile(
         PSDependencyFile.TYPE_APPLICATION_XML, f2);
      depFile2.setArchiveLocation(loc2);
      PSDependencyFile depFile3 = new PSDependencyFile(
         PSDependencyFile.TYPE_DBMS_SCHEMA, f3);
      depFile3.setArchiveLocation(loc3);
      PSDependencyFile depFile4 = new PSDependencyFile(
         PSDependencyFile.TYPE_DBMS_SCHEMA, f4);
      depFile4.setArchiveLocation(loc4);

      List<PSDependencyFile> fileList1 = new ArrayList<PSDependencyFile>();
      fileList1.add(depFile1);
      fileList1.add(depFile2);
      
      List<PSDependencyFile> fileList2 = new ArrayList<PSDependencyFile>();
      fileList2.add(depFile3);
      fileList2.add(depFile4);

      List<PSDependencyFile> fileListAll = new ArrayList<PSDependencyFile>();
      fileListAll.addAll(fileList1);
      fileListAll.addAll(fileList2);
           
      PSApplicationIDTypes idtype1 = new PSApplicationIDTypes(de1);
      PSApplicationIDTypes idtype3 = new PSApplicationIDTypes(de3);

      PSArchiveManifest archman = new PSArchiveManifest();
      archman.addFiles(de1, fileList1.iterator());
      archman.addFiles(de1, fileList2.iterator());  // do nothing
      archman.addIdTypes(de1, idtype1);  // de1 has both IDTypes and file list

      archman.addFiles(de2, fileList2.iterator()); // de2 has no IDTypes
      archman.addIdTypes(de3, idtype3); // de3 has no file list

      // Testing getXXXX
      PSApplicationIDTypes idtypes = archman.getIdTypes(de1);
      assertTrue(idtypes.equals(idtype1));

      Iterator fileList_1 = archman.getFiles(de1);
      assertTrue( doesFileListTheSame(fileList_1, fileList1.iterator()) );

      Iterator fileList_2 = archman.getFiles(de2);
      assertTrue( doesFileListTheSame(fileList_2, fileList2.iterator()) );

      assertTrue( doesFileListTheSame(archman.getFiles(de3),
         (new ArrayList()).iterator()) );

      List<PSDependencyFile> allFiles = new ArrayList<PSDependencyFile>();
      Iterator fileList_All = archman.getFiles();
      while (fileList_All.hasNext())
      {
         allFiles.add((PSDependencyFile) fileList_All.next());
      }
      
      assertEquals(allFiles.size(), fileListAll.size());
      for (PSDependencyFile file : allFiles)
      {
         assertTrue(fileListAll.contains(file));
      }
     
      // Testing XML to and from
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = archman.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, srcEl);
      String text = PSXmlDocumentBuilder.toString(doc);
      //System.out.println("Document:\n" + text);

      PSArchiveManifest tgt = new PSArchiveManifest(srcEl);

      assertTrue(archman.equals(tgt));
   }

   /**
    * Compare 2 list of <code>PSDependencyFile</code> objects
    *
    * @param list1 The first list, assume not <code>null</code>, may be empty
    * @param list2 The second list, assume not <code>null</code>, may be empty
    *
    * @return <code>true</code> if they are equals; <code>false</code> otherwise
    */
   @SuppressWarnings("unchecked")
   public static boolean doesFileListTheSame(Iterator list1, Iterator list2)
   {
      while (list1.hasNext() && list2.hasNext())
      {
         PSDependencyFile df1 = (PSDependencyFile) list1.next();
         PSDependencyFile df2 = (PSDependencyFile) list2.next();

         if (! df1.equals(df2) )
            return false;
      }
      return ( (!list1.hasNext()) && (!list2.hasNext()) );
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSArchiveManifestTest("testAll"));
      return suite;
   }
}
