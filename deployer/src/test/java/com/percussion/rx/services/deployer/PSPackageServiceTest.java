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

import com.percussion.rx.config.IPSConfigStatusMgr;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
import com.percussion.rx.config.impl.PSConfigService;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageActionStatus;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageType;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author erikserating
 *
 */
@Category(IntegrationTest.class)
public class PSPackageServiceTest
{

   public PSPackageServiceTest()
   {
      super();
   }


   public void testService() throws Exception
   {
      
      PSPackageService pkgService = new PSPackageService();
      PSPackages pkgs = pkgService.getAllPackages();
      PSPackage pkg = null;
      
      //Are all entries unique?
      assertTrue(allEntriesUnique(pkgs));
      
      //Created Only
//      pkg = getEntryByName(pkgs, TEST_NAME_PREFIX + "CREATED_ONLY");
//      assertTrue(pkg.getPackageStatus() == PSPackageService.NONE);
//      assertTrue(pkg.getConfigStatus() == PSPackageService.NONE);
      
      //Installed Success & No Config
      pkg = getEntryByName(pkgs, TEST_NAME_PREFIX + "INSTALLED_SUCCESS");
      assertTrue(pkg.getPackageStatus().equals(PSPackageService.SUCCESS));
      assertTrue(pkg.getConfigStatus().equals(PSPackageService.NONE));
      
      //Installed Fail
      pkg = getEntryByName(pkgs, TEST_NAME_PREFIX + "INSTALLED_FAIL");
      assertTrue(pkg.getPackageStatus().equals(PSPackageService.ERROR));
      assertTrue(pkg.getConfigStatus().equals(PSPackageService.NONE));
      
      //Installed Success & Config Success
      pkg = getEntryByName(pkgs, TEST_NAME_PREFIX + "INSTALLED_CONFIG_SUCCESS");
      assertTrue(pkg.getPackageStatus().equals(PSPackageService.SUCCESS));
      assertTrue(pkg.getConfigStatus().equals(PSPackageService.SUCCESS));
      
      //Installed Success & Config Fail
      pkg = getEntryByName(pkgs, TEST_NAME_PREFIX + "INSTALLED_CONFIG_FAIL");
      assertTrue(pkg.getPackageStatus().equals(PSPackageService.SUCCESS));
      assertTrue(pkg.getConfigStatus().equals(PSPackageService.ERROR));
      
   }
   
   /**
    * Determine that we are only getting back unique entries by
    * name.
    * @param pkgs
    * @return
    */
   private boolean allEntriesUnique(PSPackages pkgs)
   {
      List<String> entries = new ArrayList<String>();
      for(PSPackage pkg : pkgs.getPackages())
      {
           if(entries.contains(pkg.getName()))
              return false;
           entries.add(pkg.getName());
      }
      return true;
   }
   
   /**
    * Return a package object entry by name.
    * @param pkgs
    * @param name
    * @return
    */
   private PSPackage getEntryByName(PSPackages pkgs, String name)
   {
      for(PSPackage pkg : pkgs.getPackages())
      {
           if(name.equals(pkg.getName()))
              return pkg;
      }
      return null;
   }
   
   /**
    * Creates a new instance using the supplied name, sets the required fields,
    * sets the description to the supplied name, and saves the guid in
    * {@link #m_pkgInfoGuids}.
    * 
    * @param name Assumed not <code>null</code> or empty.
    * @return Never <code>null</code>.
    */
   private PSPkgInfo createPkgInfo(String name)
   {
      PSPkgInfo info = m_pkgInfoService.createPkgInfo(name);
      m_pkgInfoGuids.add(info.getGuid());
      info.setPackageDescription(name);
      info.setPackageVersion("1.0.0");
      info.setLastAction(PackageAction.INSTALL_CREATE);
      info.setLastActionByUser("unittest");
      info.setLastActionStatus(PackageActionStatus.SUCCESS);
      info.setLastActionDate(new Date());
      info.setPublisherName("unittest");
      return info;
   }
   static PSPackageServiceTest instance;
   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @BeforeClass
   public static void setUp() throws Exception
   {
      instance = new PSPackageServiceTest();
      instance.removePackageEntries(TEST_NAME_PREFIX);
      PSPkgInfo info = null;
      PSConfigStatus configStatus = null;
      
      // Created Only
      info = instance.createPkgInfo(TEST_NAME_PREFIX + "CREATED_ONLY");
      instance.m_pkgInfoService.savePkgInfo(info);
      
      // Installed Success
      info = instance.createPkgInfo(TEST_NAME_PREFIX + "INSTALLED_SUCCESS");
      info.setType(PackageType.PACKAGE);
      info.setLastActionDate(new Date());
      instance.m_pkgInfoService.savePkgInfo(info);
      
      // Installed Fail
      info =instance.createPkgInfo(TEST_NAME_PREFIX + "INSTALLED_FAIL");
      info.setType(PackageType.PACKAGE);
      info.setLastActionDate(new Date());
      info.setLastActionStatus(PackageActionStatus.FAIL);
      instance.m_pkgInfoService.savePkgInfo(info);
      
      // Installed & Config Success
      info = instance.createPkgInfo(TEST_NAME_PREFIX + "INSTALLED_CONFIG_SUCCESS");
      info.setType(PackageType.PACKAGE);
      info.setLastActionDate(new Date());
      info.setLastActionStatus(PackageActionStatus.SUCCESS);
      instance.m_pkgInfoService.savePkgInfo(info);
      
      configStatus = instance.m_cfgStatusMgr.createConfigStatus(info.getPackageDescriptorName());
      instance.m_pkgConfigInfoIds.add(configStatus.getStatusId());
      configStatus.setStatus(ConfigStatus.SUCCESS);
      configStatus.setDateApplied(new Date());
      instance.m_cfgStatusMgr.saveConfigStatus(configStatus);
      
      // Installed & Config Fail
      info = instance.createPkgInfo(TEST_NAME_PREFIX + "INSTALLED_CONFIG_FAIL");
      info.setType(PackageType.PACKAGE);
      info.setLastActionDate(new Date());
      info.setLastActionStatus(PackageActionStatus.SUCCESS);
      instance.m_pkgInfoService.savePkgInfo(info);
     
      configStatus = instance.m_cfgStatusMgr.createConfigStatus(info.getPackageDescriptorName());
      instance.m_pkgConfigInfoIds.add(configStatus.getStatusId());
      configStatus.setStatus(ConfigStatus.FAILURE);
      configStatus.setDateApplied(new Date());
      instance.m_cfgStatusMgr.saveConfigStatus(configStatus);
   }

   /**
    * Permanently deletes all pkg info entries whose descriptor name starts with
    * the supplied prefix.
    * 
    * @param prefix Assumed not <code>null</code> or empty. Case-insensitive.
    */
   private void removePackageEntries(String prefix)
   {
      List<PSPkgInfo> infos = m_pkgInfoService.findAllPkgInfos();
      String prefixLc = prefix.toLowerCase();
      for (PSPkgInfo p : infos)
      {
         if (p.getPackageDescriptorName().toLowerCase().startsWith(prefixLc))
            m_pkgInfoService.deletePkgInfo(p.getGuid());
      }
   }

   @AfterClass
   public void tearDown() throws Exception
   {
      //Remove config entries first
      for(Long id : m_pkgConfigInfoIds)
      {
         try
         {
            m_cfgStatusMgr.deleteConfigStatus(id);
         }
         catch(Exception e)
         {
            // ignore exception
         }
      }
      //Remove pkg info entries
      for(IPSGuid guid : m_pkgInfoGuids)
      {
         try
         {
            m_pkgInfoService.deletePkgInfo(guid);
         }
         catch(Exception e)
         {
            // ignore exception
         }
      }
   }

   private static final String TEST_NAME_PREFIX = "PSPACKAGESERVICETEST_";
   
   private List<IPSGuid> m_pkgInfoGuids = new ArrayList<IPSGuid>();
   private List<Long> m_pkgConfigInfoIds = new ArrayList<Long>();
   
   private IPSPkgInfoService m_pkgInfoService = 
      PSPkgInfoServiceLocator.getPkgInfoService();
   
   private IPSConfigStatusMgr m_cfgStatusMgr = ((PSConfigService) PSConfigServiceLocator
         .getConfigService()).getConfigStatusManager();
   
}
