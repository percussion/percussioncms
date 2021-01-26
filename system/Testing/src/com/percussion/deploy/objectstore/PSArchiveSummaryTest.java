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

package com.percussion.deploy.objectstore;

import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSArchiveSummary</code> class.
 */
@Category(UnitTest.class)
public class PSArchiveSummaryTest
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
    public PSArchiveSummaryTest()
   {
   }

   /**
    * Test all features of PSArchiveSummary class
    *
    * @throws Exception If there are any errors.
    */
   @Test
   public void testAll() throws Exception
   {
      PSArchiveSummary src = getArchiveSummaryNoManifest();
      PSArchiveSummary src2 = getArchiveSummaryWithManifest();

      // object -> XML -> object
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl_1 = src.toXml(doc);
      Element srcEl_2 = src2.toXml(doc);

      PSArchiveSummary tgt = new PSArchiveSummary(srcEl_1);
      PSArchiveSummary tgt2 = new PSArchiveSummary(srcEl_2);

      // source should be the same as the target object.
      assertTrue( src.equals(tgt) );
      assertTrue( src2.equals(tgt2) );
   }

   /**
    * creating an archive summary object without archive manifest.
    *
    * @return A newly created <code>PSArchiveSummary</code> object, which does
    * not contain an archive manfest object.
    */
   public static PSArchiveSummary getArchiveSummaryNoManifest()
   {
      PSArchiveInfo info = PSArchiveInfoTest.getArchiveInfo(false);
      Date idate = new Date();
      PSArchivePackage pkg1 = new PSArchivePackage("pkg1", "pkgType1",
         PSArchivePackage.STATUS_IN_PROGRESS, -1);
      PSArchivePackage pkg2 = new PSArchivePackage("pkg2", "pkgType2",
         PSArchivePackage.STATUS_IN_PROGRESS, -1);
      PSArchivePackage pkg3 = new PSArchivePackage("pkg3", "pkgType2",
         PSArchivePackage.STATUS_IN_PROGRESS, -1);
      List pkgList = new ArrayList();
      pkgList.add(pkg1);
      pkgList.add(pkg2);
      pkgList.add(pkg3);

      PSArchiveSummary as = new PSArchiveSummary(info, idate,
         pkgList.iterator());

      return as;
   }

   /**
    * creating an archive summary object with an archive manifest in it.
    *
    * @return A newly created <code>PSArchiveSummary</code> object, which does
    * contain an archive manfest object.
    */
   public static PSArchiveSummary getArchiveSummaryWithManifest()
   {
      PSArchiveManifest archman = new PSArchiveManifest();
      PSArchiveSummary as = getArchiveSummaryNoManifest();
      as.setArchiveManifest(archman);

      return as;
   }
}
