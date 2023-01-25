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
package com.percussion.rx.services.deployer;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.ArrayUtils;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSPackageVisibilityTest extends ServletTestCase
{
   public void testGetCommunities()
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      IPSGuid guid301 = mgr.makeGuid(301, PSTypeEnum.NODEDEF);// Auto Index
      // type
      guids.add(guid301);
      IPSGuid guid314 = mgr.makeGuid(314, PSTypeEnum.NODEDEF);// Navon type
      guids.add(guid314);
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      Map<IPSGuid, String> commMap = pkgVis.getCommunities(guids);
      String comm301 = commMap.get(guid301);
      String comm314 = commMap.get(guid314);
      // Auto Index type has four communities test we get back four communities.
      assertTrue(comm301.split(PSPackageService.NAME_SEPARATOR).length == 4);
      assertTrue(ArrayUtils.contains(comm301
            .split(PSPackageService.NAME_SEPARATOR), "Enterprise_Investments"));
      // Navon type is in all 5 communities
      assertTrue(comm314.split(PSPackageService.NAME_SEPARATOR).length == 5);
      //assertFalse(ArrayUtils.contains(comm314.split(PSPackageService.NAME_SEPARATOR), "Enterprise_Investments"));
   }

   public void testClearCommunity()
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      IPSGuid guid301 = mgr.makeGuid(301, PSTypeEnum.NODEDEF);// Auto Index
      // type
      guids.add(guid301);
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      pkgVis.clearCommunity("Enterprise_Investments", guids);
      Map<IPSGuid, String> commMap = pkgVis.getCommunities(guids);
      String comm301 = commMap.get(guid301);
      // After clearing the community we should have three communities.
      assertTrue(comm301.split(PSPackageService.NAME_SEPARATOR).length == 3);
      assertFalse(ArrayUtils.contains(comm301
            .split(PSPackageService.NAME_SEPARATOR), "Enterprise_Investments"));
   }

   public void testSetCommunity()
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      IPSGuid guid301 = mgr.makeGuid(301, PSTypeEnum.NODEDEF);// Auto Index
      // type
      guids.add(guid301);
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      // Set one community and clear rest of the communities
      pkgVis.setCommunities(guid301, Collections
            .singletonList("Enterprise_Investments"), true);
      Map<IPSGuid, String> commMap = pkgVis.getCommunities(guids);
      String comm301 = commMap.get(guid301);
      // make sure there is only one community
      assertTrue(comm301.split(PSPackageService.NAME_SEPARATOR).length == 1);
      assertTrue(ArrayUtils.contains(comm301
            .split(PSPackageService.NAME_SEPARATOR), "Enterprise_Investments"));
      List<String> commsList = new ArrayList<String>();
      commsList.add("Enterprise_Investments_Admin");
      commsList.add("Corporate_Investments");
      commsList.add("Corporate_Investments_Admin");
      // Put the other communities back without clearing.
      pkgVis.setCommunities(guid301, commsList, false);
      commMap = pkgVis.getCommunities(guids);
      comm301 = commMap.get(guid301);
      // Make sure there are four communities.
      assertTrue(comm301.split(PSPackageService.NAME_SEPARATOR).length == 4);
      assertTrue(ArrayUtils.contains(comm301
            .split(PSPackageService.NAME_SEPARATOR),
            "Enterprise_Investments_Admin"));
   }
}
