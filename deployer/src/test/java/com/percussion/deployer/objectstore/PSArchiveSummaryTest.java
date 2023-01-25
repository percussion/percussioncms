/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.deployer.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit test class for the <code>PSArchiveSummary</code> class.
 */
public class PSArchiveSummaryTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSArchiveSummaryTest(String name)
   {
      super(name);
   }

   /**
    * Test all features of PSArchiveSummary class
    *
    * @throws Exception If there are any errors.
    */
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

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSArchiveSummaryTest("testAll"));
      return suite;
   }

}
