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

import com.percussion.util.PSFormatVersion;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the <code>PSArchiveInfo</code> object.
 */
@Category(UnitTest.class)
public class PSArchiveInfoTest
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
    * Test the xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
         
      PSArchiveInfo info1 = getArchiveInfo(false);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = info1.toXml(doc);
      PSArchiveInfo info2 = new PSArchiveInfo(el);
      assertEquals(info1, info2);
      
      
      // now do it with a detail too
      info1 = getArchiveInfo(true);
      el = info1.toXml(doc);

      info2 = new PSArchiveInfo(el);
      assertEquals(info1, info2);
   }
   
   /**
    * Construct an archive info object.
    * 
    * @param includeDetail <code>true</code> to include an archive detail
    * object, <code>false</code> otherwise.
    * 
    * @return The archive info object, never <code>null</code>.
    */
   public static PSArchiveInfo getArchiveInfo(boolean includeDetail)
   {
      PSDbmsInfo rep = new PSDbmsInfo("RhythmyxData", "driver", "server",
            "database", "origin", "uid", "pwd", false);
      
      PSArchiveInfo info = new PSArchiveInfo("test", "myServer", 
         new PSFormatVersion("com.percussion.util.test"), rep, "admin1", "USER");
         
      if (includeDetail)
      {
         PSExportDescriptor desc = PSDescriptorTest.getExportDescriptor(true);
         PSArchiveDetail detail = new PSArchiveDetail(desc);
            
         Iterator pkgs = desc.getPackages();
         if (pkgs.hasNext())
         {
            PSDeployableElement de = (PSDeployableElement) pkgs.next();
            final List<PSDatasourceMap> infoList = new ArrayList<PSDatasourceMap>();
            PSDatasourceMap dsMap = new PSDatasourceMap("RhythmyxData", "");
            infoList.add(dsMap);

            detail.setDbmsInfoList(de, infoList);
         }
         info.setArchiveDetail(detail);
      }
      
      return info;
   }
}
