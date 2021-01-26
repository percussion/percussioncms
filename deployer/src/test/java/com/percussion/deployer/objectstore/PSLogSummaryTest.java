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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSLogSummary</code> class.
 */
public class PSLogSummaryTest{

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
    public PSLogSummaryTest()
   {
      super();
   }

   /**
    * Test all features of PSLogSummary class
    *
    * @throws Exception If there are any errors.
    */
   @Test
   public void testAll() throws Exception
   {
      PSDeployableElement dep1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element",
         "myTestElement", true, false, false);
      PSArchiveSummary archSummary1 =
         PSArchiveSummaryTest.getArchiveSummaryNoManifest();
      PSLogSummary src = new PSLogSummary(dep1, archSummary1);

      // object -> XML -> object
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);
      PSLogSummary tgt = new PSLogSummary(srcEl);

      // source should be the same as the target object.
      assertTrue( src.equals(tgt) );
   }



}
