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
package com.percussion.services.pkginfo.utils;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.util.PSProperties;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Test id-name helper
 */
@Category(IntegrationTest.class)
public class PSPkgHelperTest extends ServletTestCase
{
   /**
    * Test various methods to perform id-name translation.
    * 
    * @throws Exception if the test fails.
    */
   public void testValidatePackage() throws Exception
      
   {
      IPSPkgInfoService pkgInfoSvc = 
         PSPkgInfoServiceLocator.getPkgInfoService();
      // cleanup before the test
      pkgInfoSvc.deletePkgInfo("Test_Package");
      
      // start the test
      PSPkgInfo pkgInfo = pkgInfoSvc.createPkgInfo("Test_Package");
      FileOutputStream out = null;
      String cfgFileName = "rxconfig/Installer/rxrepository.properties";

      try
      {
         IPSGuid pkgGuid = pkgInfo.getGuid();
         pkgInfoSvc.savePkgInfo(pkgInfo);

         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid tempGuid = gmgr.makeGuid(501, PSTypeEnum.TEMPLATE);

         String extnName = "Java/global/percussion/cx/sys_addNewItemToFolder";
         IPSGuid extnGuid = PSIdNameHelper.getGuid(
               extnName,
               PSTypeEnum.EXTENSION);

         IPSGuid cfgFileGuid = PSIdNameHelper.getGuid(
               cfgFileName,
               PSTypeEnum.CONFIGURATION);

         String handlerName = "actionHandler";
         IPSGuid handlerGuid = PSIdNameHelper.getGuid(
               handlerName,
               PSTypeEnum.LOADABLE_HANDLER);

         PSPkgElement tempElem = pkgInfoSvc.createPkgElement(pkgGuid);
         tempElem.setObjectGuid(tempGuid);
         pkgInfoSvc.savePkgElement(tempElem);

         PSPkgElement extnElem = pkgInfoSvc.createPkgElement(pkgGuid);
         extnElem.setObjectGuid(extnGuid);
         pkgInfoSvc.savePkgElement(extnElem);

         PSPkgElement cfgFileElem = pkgInfoSvc.createPkgElement(pkgGuid);
         cfgFileElem.setObjectGuid(cfgFileGuid);
         pkgInfoSvc.savePkgElement(cfgFileElem);

         // this element won't be tracked
         PSPkgElement handlerElem = pkgInfoSvc.createPkgElement(pkgGuid);
         handlerElem.setObjectGuid(handlerGuid);
         pkgInfoSvc.savePkgElement(handlerElem);

         PSPkgHelper.updatePkgElementVersions(
               pkgInfo.getPackageDescriptorName());

         // validation should return no results
         Set<String> modObjs = PSPkgHelper.validatePackage(pkgGuid);
         // one validation error on community visibility warning/error
         assertTrue(modObjs.size() == 1); 

         // save template
         IPSTemplateService templateSvc = 
            PSAssemblyServiceLocator.getAssemblyService();

         IPSAssemblyTemplate temp = templateSvc.loadTemplate(tempGuid, false);
         templateSvc.saveTemplate(temp);

         // save extension
         IPSExtensionManager extnMgr = PSServer.getExtensionManager(null);
         PSExtensionRef extnRef = new PSExtensionRef(extnName);
         IPSExtensionDef extnDef = extnMgr.getExtensionDef(extnRef);
         extnMgr.updateExtension(extnDef, extnDef.getSuppliedResources());
         
         // save config file
         PSProperties props = new PSProperties(PSServer.getRxFile(
               cfgFileName));
         props.setProperty("test_prop", "test_value");
         out = new FileOutputStream(PSServer.getRxFile(cfgFileName));
         props.store(out, null);
         out.close();

         // validate and check results
         modObjs = PSPkgHelper.validatePackage(pkgGuid);
         // one additional validation on community visibility warning/error
         assertEquals(4, modObjs.size());
         String tempStr = temp.getName() + '(' 
               + PSTypeEnum.TEMPLATE.getDisplayName() + ')';
         String extnStr = extnName + '('
               + PSTypeEnum.EXTENSION.getDisplayName() + ')';
         String cfgFileStr = cfgFileName + '('
               + PSTypeEnum.CONFIGURATION.getDisplayName() + ')';
         assertTrue(modObjs.contains(tempStr));
         assertTrue(modObjs.contains(extnStr));
         assertTrue(modObjs.contains(cfgFileStr));
         
         // modified objects should have been flagged as modified
         tempElem = pkgInfoSvc.loadPkgElement(tempElem.getGuid());
         assertEquals(PSPkgHelper.OBJECT_MODIFIED_VERSION,
               tempElem.getVersion());
         extnElem = pkgInfoSvc.loadPkgElement(extnElem.getGuid());
         assertEquals(PSPkgHelper.OBJECT_MODIFIED_VERSION,
               extnElem.getVersion());
         cfgFileElem = pkgInfoSvc.loadPkgElement(cfgFileElem.getGuid());
         assertEquals(PSPkgHelper.OBJECT_MODIFIED_VERSION,
               cfgFileElem.getVersion());
         
         // test validate package name
         assertEquals(modObjs, PSPkgHelper.validatePackage(
               pkgInfo.getPackageDescriptorName()));
         
         // test update pkg element versions with collection of guids
         Set<IPSGuid> objGuids = new HashSet<IPSGuid>();
         objGuids.add(tempGuid);
                           
         PSPkgHelper.updatePkgElementVersions(objGuids);
         
         // validation should match previous
         assertEquals(modObjs, PSPkgHelper.validatePackage(pkgGuid));
                
         PSPkgHelper.updatePkgElementVersions(
               pkgInfo.getPackageDescriptorName());

         // validation should return no results, except warning on community
         // visibility 
         modObjs = PSPkgHelper.validatePackage(pkgGuid);
         assertTrue(modObjs.size() == 1);
      }
      finally
      {
         pkgInfoSvc.deletePkgInfo(pkgInfo.getPackageDescriptorName());

         PSProperties props = new PSProperties(PSServer.getRxFile(
               cfgFileName));
         props.remove("test_prop");
         out = new FileOutputStream(PSServer.getRxFile(cfgFileName));
         props.store(out, null);
         out.close();
      }
   }
}
