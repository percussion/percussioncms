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
