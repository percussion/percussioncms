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
