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

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageActionStatus;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageCategory;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageType;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class PSConvertToSourceTest
{

   @Test
   public void testConvert() throws PSNotFoundException {
      PSPair<Boolean, String> results = new PSPair<Boolean, String>();
      PSConvertToSource converter = new PSConvertToSource();
      
      //create a package
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
      .getPkgInfoService();
      
      PSPkgInfo pkgInfo = pkgService.createPkgInfo("tang");
      pkgInfo.setPublisherName("tang");
      pkgInfo.setPublisherUrl("tang.com");
      pkgInfo.setPackageDescription("tang desc");
      pkgInfo.setPackageVersion("1.0.0");
      pkgInfo.setLastActionDate(new Date());
      pkgInfo.setLastActionByUser("unittest");
      pkgInfo.setLastActionStatus(PackageActionStatus.SUCCESS);
      pkgInfo.setLastAction(PackageAction.INSTALL_CREATE);
      pkgInfo.setType(PackageType.PACKAGE);
      pkgInfo.setEditable(true);
      pkgInfo.setPackageDescriptorName("tang");
      pkgInfo.setCmVersionMinimum("5.1.1");
      pkgInfo.setCmVersionMaximum(".1.1");
      pkgInfo.setCategory(PackageCategory.USER);
      pkgService.savePkgInfo(pkgInfo);
      
      assertTrue(pkgInfo.getType() == PackageType.PACKAGE);
      
      //convert package
      converter.convertDB("tang");
      PSPkgInfo convPkgInfo = pkgService.findPkgInfo("tang");
      
      assertTrue(convPkgInfo.getType() == PackageType.DESCRIPTOR);
      
      
      
      
      //invalid package
      results = converter.convert("not_real_pkg");
      assertFalse(results.getFirst());
      
      //no package
      results = converter.convert(null);
      assertFalse(results.getFirst());
   }

}
