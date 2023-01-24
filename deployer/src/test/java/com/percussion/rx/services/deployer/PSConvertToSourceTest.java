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
