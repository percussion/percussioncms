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
package com.percussion.pathmanagement.service;

import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pathmanagement.service.impl.PSSitePathItemService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PSPathUtilsTest
{ 
   /**
    * Tests the {@link PSPathUtils#getFinderPath(String)} and {@link PSPathUtils#getFolderPath(String)} methods.
    * @throws Exception
    */
   @Test
   public void testGetPath() throws Exception
   {
      String asset = "testAsset";
      String site = "testSite";
      String assetFolderPath = PSAssetPathItemService.ASSET_ROOT + '/' + asset;
      String siteFolderPath = PSSitePathItemService.SITE_ROOT + '/' + site;
      String assetFinderPath = PSPathUtils.ASSETS_FINDER_ROOT + '/' + asset;
      String siteFinderPath = PSPathUtils.SITES_FINDER_ROOT + '/' + site;
      String folderPath = "//path";
      String finderPath = "/path";

      assertEquals(assetFinderPath, PSPathUtils.getFinderPath(assetFolderPath));
      assertEquals(siteFinderPath, PSPathUtils.getFinderPath(siteFolderPath));
      assertEquals(assetFolderPath, PSPathUtils.getFolderPath(assetFinderPath));
      assertEquals(siteFolderPath, PSPathUtils.getFolderPath(siteFinderPath));
      assertEquals(finderPath, PSPathUtils.getFinderPath(folderPath));
      assertEquals(folderPath, PSPathUtils.getFolderPath(finderPath));
      assertEquals(finderPath, PSPathUtils.getFinderPath("////path"));
      assertEquals(folderPath, PSPathUtils.getFolderPath("////path"));
   }
   
   @Test
   public void testGetBasePath(){
	   String trailing =  "/Sites/mysite/mymain/mysecond/";
	   String doubleleading  =  "//Sites/mysite/mymain/mysecond/";
	   String page =  "//Sites/mysite/mymain/mysecond/mypage";
	   
	   assertEquals("mymain/mysecond",PSPathUtils.getBaseFolderFromPath(trailing));
	   assertEquals("mymain/mysecond",PSPathUtils.getBaseFolderFromPath(doubleleading));
	   assertEquals("mymain/mysecond",PSPathUtils.getBaseFolderFromPath(page));
		   
   }

   @Test
   public void testChopTrailingSlash() {
       String trailing = "/Sites/mysite/";
       String doubleTrail = "/Sites/mysite//";
       String notrail = "/Sites/mysite";

       assertEquals("/Sites/mysite", PSPathUtils.chopTrailingSlash(trailing));
       assertEquals("/Sites/mysite", PSPathUtils.chopTrailingSlash(doubleTrail));
       assertEquals("/Sites/mysite", PSPathUtils.chopTrailingSlash(notrail));
   }

   @Test
   public void testGetFolderName(){
      String trailing = "/Sites/mysite/myfolder/";
      String doubleTrail = "/Sites/mysite/myfolder//";
      String notrail = "/Sites/mysite/myfolder";

      assertEquals("myfolder", PSPathUtils.getFolderName(trailing));
      assertEquals("myfolder", PSPathUtils.getFolderName(doubleTrail));
      assertEquals("myfolder", PSPathUtils.getFolderName(notrail));
   }

   @Test
   public void testStripFolderName(){
      String trailing = "/Sites/mysite/myfolder/";
      String doubleTrail = "/Sites/mysite//myfolder//";
      String notrail = "//Sites/mysite/myfolder";

      assertEquals("/Sites/mysite", PSPathUtils.stripFolderNameFromPath(trailing));
      assertEquals("/Sites/mysite", PSPathUtils.stripFolderNameFromPath(doubleTrail));
      assertEquals("//Sites/mysite", PSPathUtils.stripFolderNameFromPath(notrail));
   }

}
