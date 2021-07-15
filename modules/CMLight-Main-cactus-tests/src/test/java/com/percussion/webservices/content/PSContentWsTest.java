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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
