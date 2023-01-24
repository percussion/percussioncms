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
