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
package com.percussion.webservices.content;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

import java.util.Collections;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test the content ws.
 */
@Category(IntegrationTest.class)
public class PSContentWsTest extends ServletTestCase
{
   @Override
   public void setUp() throws Exception
   {
      IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
      secWs.login(request, response, "admin1", "demo", null, 
            "Enterprise_Investments_Admin", null);        
   }
   
   /**
    * Tests the load folders methods.
    * 
    * @throws Exception
    */
   public void testLoadFolders() throws Exception
   {
      IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
      List<PSFolder> folders = contentWs.loadFolders(
            new String[]{"//Sites/EnterpriseInvestments"});
      PSFolder f1 = folders.get(0);
      IPSGuid f1Guid = f1.getGuid(); 
      assertNotNull(f1Guid);
      folders = contentWs.loadFolders(Collections.singletonList(f1Guid));
      PSFolder f2 = folders.get(0);
      IPSGuid f2Guid = f2.getGuid();
      assertNotNull(f2Guid);
      assertEquals(f1Guid, f2Guid);
      assertEquals(f1, f2);
   }
   
   /**
    * Tests load folders followed by save folders.
    * 
    * @throws Exception
    */
   public void testLoadThenSaveFolders() throws Exception
   {
      IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
      PSFolder testFolder = contentWs.addFolder("Test", "//Sites/EnterpriseInvestments");
      List<PSFolder> folders = contentWs.loadFolders(Collections.singletonList(testFolder.getGuid()));
      List<IPSGuid> folderIds = contentWs.saveFolders(folders);
      assertTrue(!folderIds.isEmpty());
      
   }
   
   /**
    * Tests findDescendantFolders() method.
    * 
    * @throws Exception
    */
   public void testFindDescendantFolders() throws Exception
   {
      IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();

      IPSGuid id = contentWs.getIdByPath("//Sites/EnterpriseInvestments/AboutEnterpriseInvestments");
      List<PSFolder> folders = contentWs.findDescendantFolders(id);
      assertTrue(folders.size() == 4);

      id = contentWs.getIdByPath("//Sites/EnterpriseInvestments/Files");
      folders = contentWs.findDescendantFolders(id);
      assertTrue(folders.size() == 0);      
   }
   
   public void tearDown()
   {
      try
      {
         IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
         List<PSFolder> folders = contentWs.loadFolders(new String[]{"//Sites/EnterpriseInvestments/Test"});
         if (!folders.isEmpty())
         { 
            contentWs.deleteFolders(Collections.singletonList(folders.get(0).getGuid()), true);
         }
      }
      catch (Exception e)
      {
         // could not delete
      }
   }
   
}
