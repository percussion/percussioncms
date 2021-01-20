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
package com.percussion.services.pkginfo;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageActionStatus;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageType;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link IPSPkgInfoService} class.
 */
@Category(IntegrationTest.class)
public class PSPkgInfoServiceTest
{

   // Constants for testing (tc)
   //
   final PSGuid tcPkgInfoGuid = new PSGuid(PSTypeEnum.PACKAGE_INFO, 1);

   final String tcDescriptorName = "A Test Descriptor Name";

   final PSGuid tcDescriptorGuid = new PSGuid(
         PSTypeEnum.DEPLOYER_DESCRIPTOR_ID, 2);

   final String tcPublisherName = "Persussion Software, Inc.";

   final String tcPublisherUrl = "http://www.percussion.com";

   final String tcDescription = "A Test Package";

   final String tcVersion = "Test Version String v0.0";

   final String tcShippedConfigDefinition = "A Test Shipped Configuration Definition";

   final Date tcLastActionDate = adjustDate(new Date());

   final Date tcOrigConfigDate = adjustDate(new Date());

   final String tcInstaller = "A Test Installer";

   final PackageActionStatus tcActionStatus = PackageActionStatus.SUCCESS;

   final PackageAction tcLastAction = PackageAction.UNINSTALL;

   final PackageType tcType = PackageType.DESCRIPTOR;

   final String tcCmVersionMinimum = "The Very Earliest Version String";

   final String tcCmVersionMaximum = "The Latest Supported Version String";

   /**
    * Convenience method to initialize PkgInfo object
    * 
    * @param pkgInfo The PkgInfo object to be initialized
    * @param initIdInfo Init the name and GUID members
    * @param appendStr For debugging, the tring to append to differentiate
    * string values
    */
   void initPkgInfoPojo(PSPkgInfo pkgInfo, Boolean initIdInfo, String appendStr)
   {
      // Use the setters (mutators) to set values for each property
      //

      if (appendStr == null)
         appendStr = "";

      if (initIdInfo)
      {
         pkgInfo.setPackageDescriptorName(tcDescriptorName);
         pkgInfo.setGuid(tcPkgInfoGuid);
         pkgInfo.setPackageDescriptorGuid(tcDescriptorGuid);
      }
      pkgInfo.setPublisherName(tcPublisherName + appendStr);
      pkgInfo.setPublisherUrl(tcPublisherUrl + appendStr);
      pkgInfo.setPackageDescription(tcDescription + appendStr);
      pkgInfo.setPackageVersion(tcVersion + appendStr);
      pkgInfo
            .setShippedConfigDefinition(tcShippedConfigDefinition + appendStr);
      pkgInfo.setLastActionDate(tcLastActionDate);
      pkgInfo.setOriginalConfigDate(tcOrigConfigDate);
      pkgInfo.setLastActionByUser(tcInstaller + appendStr);
      pkgInfo.setLastActionStatus(tcActionStatus);
      pkgInfo.setLastAction(tcLastAction);
      pkgInfo.setType(tcType);
      pkgInfo.setCmVersionMinimum(tcCmVersionMinimum + appendStr);
      pkgInfo.setCmVersionMaximum(tcCmVersionMaximum + appendStr);

   }

   /**
    * Adjust date to eliminate millesec.
    * 
    */
   private Date adjustDate(Date theDate)
   {
      Long dateTime = theDate.getTime();
      dateTime = dateTime - (dateTime % 1000);
      theDate.setTime(dateTime);
      return theDate;
   }

   /**
    * Test that the configuration can be obtained.
    * 
    * @throws Exception If the test fails.
    */
//   public void testLocatorService() throws Exception
//   {
//      IPSPkgInfoService svc = null;
//
//      svc = PSPkgInfoServiceLocator.getPkgInfoService();
//
//      assertNotNull(svc);
//   }

   /**
    * Test creating Package Info objects. Even though in practice, the service
    * will be used to create these objects this is a base test to be sure they
    * are created correctly without the service being involved.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgInfoPojoObjectsCreation() throws Exception
   {

      PSPkgInfo pkgInfo = new PSPkgInfo();
      assertNotNull(pkgInfo);

      PSPkgElement pkgElement = new PSPkgElement();
      assertNotNull(pkgElement);

      PSPkgDependency pkgDependency = new PSPkgDependency();
      assertNotNull(pkgDependency);

   }

   /**
    * Test the primary Package Info object.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgInfoPojo() throws Exception
   {

      // Use constructor to create and initialize values.
      PSPkgInfo pkgInfo = new PSPkgInfo();
      initPkgInfoPojo(pkgInfo, true, ""); // true = init the GUIDs

      // Test for these values with the getters (accessors)
      //
      assertEquals(pkgInfo.getGuid(), tcPkgInfoGuid);
      assertEquals(pkgInfo.getPackageDescriptorName(), tcDescriptorName);
      assertEquals(pkgInfo.getPackageDescriptorGuid(), tcDescriptorGuid);
      assertEquals(pkgInfo.getPublisherName(), tcPublisherName);
      assertEquals(pkgInfo.getPublisherUrl(), tcPublisherUrl);
      assertEquals(pkgInfo.getPackageDescription(), tcDescription);
      assertEquals(pkgInfo.getPackageVersion(), tcVersion);
      assertEquals(pkgInfo.getShippedConfigDefinition(),
            tcShippedConfigDefinition);
      assertEquals(pkgInfo.getLastActionDate(), tcLastActionDate);
      assertEquals(pkgInfo.getOriginalConfigDate(), tcOrigConfigDate);
      assertEquals(pkgInfo.getLastActionByUser(), tcInstaller);
      assertEquals(pkgInfo.getLastActionStatus(), tcActionStatus);
      assertEquals(pkgInfo.getLastAction(), tcLastAction);
      assertEquals(pkgInfo.getType(), tcType);
      assertEquals(pkgInfo.getCmVersionMinimum(), tcCmVersionMinimum);
      assertEquals(pkgInfo.getCmVersionMaximum(), tcCmVersionMaximum);
   }

   /**
    * Test the Package Element object.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgElementPojo() throws Exception
   {

      // Use constructor to create and initialize values.
      PSPkgElement pkgElement = new PSPkgElement();

      // Constants for testing (tc)
      //
      final PSGuid tcPkgElemGuid1 = new PSGuid(PSTypeEnum.PACKAGE_ELEMENT, 1);
      final PSGuid tcPkgElemGuid2 = new PSGuid(PSTypeEnum.PACKAGE_INFO, 2);
      final PSGuid objectGuid = new PSGuid(PSTypeEnum.SITE, 301);
      final long tcPkgElemVersion = 1;

      // Use the setters (mutators) to set values for each property
      //
      pkgElement.setGuid(tcPkgElemGuid1);
      pkgElement.setPackageGuid(tcPkgElemGuid2);
      pkgElement.setObjectGuid(objectGuid);
      pkgElement.setVersion(tcPkgElemVersion);

      // Test for these values with the getters (accessors)
      //
      assertEquals(pkgElement.getGuid(), tcPkgElemGuid1);
      assertEquals(pkgElement.getPackageGuid(), tcPkgElemGuid2);
      assertEquals(pkgElement.getObjectGuid(), objectGuid);
      assertEquals(pkgElement.getObjectUuid(), objectGuid.getUUID());
      assertEquals(pkgElement.getObjectType(), objectGuid.getType());
      assertEquals(pkgElement.getVersion(), tcPkgElemVersion);
   }

   /**
    * Test the Package Dependency object.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgDependencyPojo() throws Exception
   {

      // Use constructor to create and initialize values.
      PSPkgDependency pkgDep = new PSPkgDependency();
      // Constants for testing (tc)
      //
      final long tcId = 1;
      final PSGuid tcPkgOwnerGuid2 = new PSGuid(PSTypeEnum.PACKAGE_INFO, 2);
      final PSGuid tcPkgDepGuid3 = new PSGuid(PSTypeEnum.PACKAGE_INFO, 3);
      final Boolean tcImpliedDep = new Boolean(true);

      // Use the setters (mutators) to set values for each property
      //
      pkgDep.setId(tcId);
      pkgDep.setOwnerPackageGuid(tcPkgOwnerGuid2);
      pkgDep.setDependentPackageGuid(tcPkgDepGuid3);
      pkgDep.setImpliedDep(tcImpliedDep);

      // Test for these values with the getters (accessors)
      //
      assertEquals(pkgDep.getId(), tcId);
      assertEquals(pkgDep.getOwnerPackageGuid(), tcPkgOwnerGuid2);
      assertEquals(pkgDep.getDependentPackageGuid(), tcPkgDepGuid3);
      assertEquals(pkgDep.isImpliedDep(), tcImpliedDep);

   }
   private void cleanupPkgInfo(String startsWith)
   {
      IPSPkgInfoService srv = PSPkgInfoServiceLocator.getPkgInfoService();
      List<PSPkgInfo> pkgInfoList = srv.findAllPkgInfos();
      String prefix = startsWith.toLowerCase();
      for (PSPkgInfo pkg : pkgInfoList)
      {
         if (pkg.getPackageDescriptorName().toLowerCase().startsWith(prefix))
            srv.deletePkgInfo(pkg.getGuid());
      }
   }

   /**
    * Test the primary Package Info object's Services.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgInfoServices() throws Exception
   {
      // cleanup test specific data
      cleanupPkgInfo(tcDescriptorName);

      // Get the PkgInfo Service
      IPSPkgInfoService pkgInfoService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      // IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

      int oldPkgInfoCount = pkgInfoService.findAllPkgInfos().size();

      // Use service to create and return a Package Info object.
      PSPkgInfo pkgInfo0 = pkgInfoService.createPkgInfo(tcDescriptorName
            + "_0");

      // Initialize the Package Info object
      //
      initPkgInfoPojo(pkgInfo0, false, "_0");
      
      // Save the Package Info object
      pkgInfoService.savePkgInfo(pkgInfo0);

      // Load the persisted object into a different Package Info object
      PSGuid testGuid = (PSGuid) pkgInfo0.getGuid();
      PSPkgInfo pkgInfoTest = pkgInfoService.loadPkgInfo(testGuid);

      // Test for these values with the getters (accessors)
      //
      assertEquals(pkgInfo0.getGuid(), pkgInfoTest.getGuid());
      assertEquals(pkgInfo0.getPackageDescriptorName(), pkgInfoTest
            .getPackageDescriptorName());
      assertEquals(pkgInfo0.getPackageDescriptorGuid(), pkgInfoTest
            .getPackageDescriptorGuid());
      assertEquals(pkgInfo0.getPublisherName(), pkgInfoTest.getPublisherName());
      assertEquals(pkgInfo0.getPublisherUrl(), pkgInfoTest.getPublisherUrl());
      assertEquals(pkgInfo0.getPackageDescription(), pkgInfoTest
            .getPackageDescription());
      assertEquals(pkgInfo0.getPackageVersion(), pkgInfoTest
            .getPackageVersion());
      assertEquals(pkgInfo0.getShippedConfigDefinition(), pkgInfoTest
            .getShippedConfigDefinition());
      assertEquals(pkgInfo0.getLastActionDate(), pkgInfoTest
            .getLastActionDate());
      assertEquals(pkgInfo0.getOriginalConfigDate(), pkgInfoTest
            .getOriginalConfigDate());
      assertEquals(pkgInfo0.getLastActionByUser(), 
            pkgInfoTest.getLastActionByUser());
      assertEquals(pkgInfo0.getLastActionStatus(), pkgInfoTest
            .getLastActionStatus());
      assertEquals(pkgInfo0.getLastAction(), pkgInfoTest.getLastAction());
      assertEquals(pkgInfo0.getType(), pkgInfoTest.getType());
      assertEquals(pkgInfo0.getCmVersionMinimum(), pkgInfoTest
            .getCmVersionMinimum());
      assertEquals(pkgInfo0.getCmVersionMaximum(), pkgInfoTest
            .getCmVersionMaximum());

      // Load the modifiable version persisted object into a different
      // Package Info object and Test for these values with the getter
      PSPkgInfo pkgInfoTest1 = pkgInfoService.loadPkgInfoModifiable(testGuid);
      assertEquals(pkgInfo0.getGuid(), pkgInfoTest1.getGuid());

      // To test the "find list of Packages by Name" method, we must
      // create a few more PkgInfoObjects. This is done in same manner
      // as above, but we assume the objects are created successfully
      // (because it was successful above)
      PSPkgInfo pkgInfo1 = pkgInfoService.createPkgInfo(tcDescriptorName
            + "_1");
      initPkgInfoPojo(pkgInfo1, false, "_1");
      pkgInfoService.savePkgInfo(pkgInfo1);

      PSPkgInfo pkgInfo2 = pkgInfoService.createPkgInfo(tcDescriptorName
            + "_2");
      initPkgInfoPojo(pkgInfo2, false, "_2");
      pkgInfoService.savePkgInfo(pkgInfo2);

      // Now actually call the "find list of Packages by Name" method.
      // Test that the objects returned are as expected (the three you
      // previously inserted.
      PSPkgInfo pkgInfoViaFind = pkgInfoService
            .findPkgInfo(pkgInfo0.getPackageDescriptorName());
      assertEquals(pkgInfo0, pkgInfoViaFind);
      pkgInfoViaFind = pkgInfoService
            .findPkgInfo(pkgInfo1.getPackageDescriptorName());
      assertEquals(pkgInfo1, pkgInfoViaFind);

      //
      List<PSPkgInfo> pkgInfoList;
      pkgInfoList = pkgInfoService.findAllPkgInfos();
      assertEquals(oldPkgInfoCount + 3, pkgInfoList.size());
      assertTrue(pkgInfoList.contains(pkgInfo0));
      assertTrue(pkgInfoList.contains(pkgInfo1));
      assertTrue(pkgInfoList.contains(pkgInfo2));

      // To test the "find list of last Packages by Name" method, we must
      // create a few more PkgInfoObjects. The dates are set to make
      // them happen in sequence.
      Calendar installCal0 = Calendar.getInstance();
      installCal0.set(1956, 7, 9, 6, 0, 0); // Aug 9, 1956 6:00 AM
      Date installDate0 = adjustDate(installCal0.getTime());

      pkgInfo0.setLastActionDate(installDate0);
      pkgInfoService.savePkgInfo(pkgInfo0);

      pkgInfo1.setLastActionDate(installDate0);
      pkgInfoService.savePkgInfo(pkgInfo1);

      pkgInfo2.setLastActionDate(installDate0);
      pkgInfoService.savePkgInfo(pkgInfo2);

      pkgInfoList = pkgInfoService.findAllPkgInfos();

      assertEquals(pkgInfoList.size(), 3 + oldPkgInfoCount);
      assertTrue(pkgInfoList.contains(pkgInfo0));
      assertTrue(pkgInfoList.contains(pkgInfo1));
      assertTrue(pkgInfoList.contains(pkgInfo2));
            
      // Clean up these test objects (remove from persisted storage)
      pkgInfoService.deletePkgInfo(pkgInfo0.getGuid());
      pkgInfoService.deletePkgInfo(pkgInfo1.getPackageDescriptorName());
      pkgInfoService.deletePkgInfo(pkgInfo2.getGuid());

      assertTrue(oldPkgInfoCount == pkgInfoService.findAllPkgInfos().size());
   }

   /**
    * Tests the {@link PSPkgInfo#isCreated()} and
    * {@link PSPkgInfo#isSuccessfullyInstalled()} methods.
    */
   @Test
   public void testConvenienceMethods()
   {
      PSPkgInfo info = new PSPkgInfo();
      info.setType(PackageType.DESCRIPTOR);
      info.setLastAction(PackageAction.INSTALL_CREATE);
      info.setLastActionStatus(PackageActionStatus.SUCCESS);
      assertTrue(info.isCreated());
      assertFalse(info.isSuccessfullyInstalled());

      info.setType(PackageType.PACKAGE);
      assertFalse(info.isCreated());
      assertTrue(info.isSuccessfullyInstalled());
      
      info.setLastAction(PackageAction.UNINSTALL);
      assertFalse(info.isSuccessfullyInstalled());
      
      info.setEditable(true);
      assertTrue(info.isEditable());
      info.setEditable(false);
      assertFalse(info.isEditable());
   }
   
   /**
    * Test the Package Element object's services.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgElementServices() throws Exception
   {
      // Get the PkgInfo Service
      IPSPkgInfoService pkgInfoService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

      // Constants for testing (tc)
      String tcDescriptorName1 = "A Test Descriptor Name 1";

      // Use service to create and return a Package Info object.
      PSPkgInfo pkgInfo = pkgInfoService.createPkgInfo(tcDescriptorName1);

      // Initialize the Package Info object
      initPkgInfoPojo(pkgInfo, false, "");
      pkgInfo.setType(PackageType.PACKAGE);
      pkgInfo.setLastAction(PackageAction.INSTALL_CREATE);

      // Use service to create and return a Package Element object.
      IPSGuid tcParentGuid = pkgInfo.getGuid();
      PSPkgElement pkgElement = pkgInfoService.createPkgElement(tcParentGuid);

      // Use the setters (mutators) to set values for properties
      int objUuid = 10000;
      final PSGuid objectGuid = new PSGuid(PSTypeEnum.SITE, objUuid++);
      final long tcPkgElemVersion = 1;

      pkgElement.setObjectGuid(objectGuid);
      pkgElement.setVersion(tcPkgElemVersion);

      // Save the Package Info and Package Element objects
      pkgInfoService.savePkgInfo(pkgInfo);
      pkgInfoService.savePkgElement(pkgElement);

      // Load the persisted object into a different Package Element object
      PSGuid testGuid = (PSGuid) pkgElement.getGuid();
      PSPkgElement pkgElementTest = pkgInfoService.loadPkgElement(testGuid);

      // Test for these values with the getters (accessors)
      //
      assertEquals(pkgElement.getGuid(), pkgElementTest.getGuid());
      assertEquals(pkgElement.getPackageGuid(), pkgElementTest
            .getPackageGuid());
      assertEquals(pkgElement.getObjectGuid(), pkgElementTest.getObjectGuid());
      assertEquals(pkgElement.getObjectUuid(), pkgElementTest.getObjectUuid());
      assertEquals(pkgElement.getObjectType(), pkgElementTest.getObjectType());
      assertEquals(pkgElement.getVersion(), pkgElementTest.getVersion());

      // Load the modifiable version persisted object into a different
      // Package Info object and Test for these values with the getter
      PSPkgElement pkgElementTest1 = pkgInfoService
            .loadPkgElementModifiable(testGuid);
      assertEquals(pkgElement.getGuid(), pkgElementTest1.getGuid());
      assertEquals(pkgElement.getVersion(), pkgElementTest.getVersion());

      // To test the "find list of PackageElements by List" method, we must
      // create a few more PackageElements objects. This is done in same manner
      // as above, but we assume the objects are created successfully
      // (because it was successful above)
      PSPkgElement pkgElement1 = pkgInfoService.createPkgElement(tcParentGuid);
      pkgElement1.setObjectGuid(new PSGuid(PSTypeEnum.SITE, objUuid++));
      pkgInfoService.savePkgElement(pkgElement1);

      PSPkgElement pkgElement2 = pkgInfoService.createPkgElement(tcParentGuid);
      pkgElement2.setObjectGuid(new PSGuid(PSTypeEnum.SITE, objUuid++));
      pkgInfoService.savePkgElement(pkgElement2);

      // Pull back the Guids and validate
      List<IPSGuid> guidList = pkgInfoService
            .findPkgElementGuids(tcParentGuid);
      assertEquals(3, guidList.size());

      Boolean existsInList = false;
      existsInList = guidList.contains(pkgElement.getGuid());
      assertTrue(existsInList);
      existsInList = guidList.contains(pkgElement1.getGuid());
      assertTrue(existsInList);
      existsInList = guidList.contains(pkgElement2.getGuid());
      assertTrue(existsInList);

      List<PSPkgElement> elementList = pkgInfoService
            .findPkgElements(tcParentGuid);
      assertEquals(3, elementList.size());
      existsInList = false;
      existsInList = elementList.contains(pkgElement);
      assertTrue(existsInList);
      existsInList = elementList.contains(pkgElement1);
      assertTrue(existsInList);
      existsInList = elementList.contains(pkgElement2);
      assertTrue(existsInList);

      // Now call the "load list of Packages Elements by List" method.
      // Test that the objects returned are as expected (the three you
      // previously inserted.
      elementList.clear();
      elementList = pkgInfoService.loadPkgElements(guidList);
      assertEquals(3, elementList.size());

      existsInList = false;
      existsInList = elementList.contains(pkgElement);
      assertTrue(existsInList);
      existsInList = elementList.contains(pkgElement1);
      assertTrue(existsInList);
      existsInList = elementList.contains(pkgElement2);
      assertTrue(existsInList);

      // Test find package elements by object
      PSPkgElement elementByObj = pkgInfoService
            .findPkgElementByObject(objectGuid);

      // Should match previous
      assertEquals(pkgElement, elementByObj);

      // Add new package element with different object type
      final PSGuid objGuid = (PSGuid) guidMgr.createGuid(PSTypeEnum.ACL);
      PSPkgElement pkgElem = pkgInfoService.createPkgElement(tcParentGuid);
      pkgElem.setObjectGuid(objGuid);
      pkgInfoService.savePkgElement(pkgElem);
      elementByObj = pkgInfoService.findPkgElementByObject(objGuid);

      // Should still match previous list
      assertNotNull(elementByObj);

      // Remove new package element
      pkgInfoService.deletePkgElement(pkgElem.getGuid());
      elementByObj = pkgInfoService.findPkgElementByObject(objGuid);
      assertNull(elementByObj);

      // Clean up these test objects (remove from persisted storage)
      pkgInfoService.deletePkgInfo(pkgInfo.getPackageDescriptorName());

      PSPkgElement remainingElement = pkgInfoService.findPkgElement(pkgElement
            .getGuid());
      assertNull(remainingElement);
      PSPkgElement remainingElement1 = pkgInfoService
            .findPkgElement(pkgElement1.getGuid());
      assertNull(remainingElement1);
      PSPkgElement remainingElement2 = pkgInfoService
            .findPkgElement(pkgElement2.getGuid());
      assertNull(remainingElement2);
      pkgInfo = pkgInfoService.findPkgInfo(tcDescriptorName1);
      assertNull(pkgInfo);
   }

   /**
    * Test the primary Package Element Dependency's object services.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgDependencyServices() throws Exception
   {

      // Get the PkgInfo Service
      IPSPkgInfoService pkgInfoService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

      // Constants for testing (tc)
      //
      String tcDescriptorName3 = "A Test Descriptor Name 3";

      // Use service to create and return a Package Info object.
      PSPkgInfo pkgInfo = pkgInfoService.createPkgInfo(tcDescriptorName3);

      // Initialize the Package Info object
      //
      initPkgInfoPojo(pkgInfo, false, " 3");

      // Use service to create and return a Package Element Dependency object.
      IPSGuid tcParentGuid = pkgInfo.getGuid();
      PSPkgDependency pkgDep = pkgInfoService.createPkgDependency();

      // Use the setters (mutators) to set values for properties
      //
      final PSGuid tcDepPkgGuid = (PSGuid) guidMgr
            .createGuid(PSTypeEnum.PACKAGE_INFO);

      pkgDep.setOwnerPackageGuid(tcParentGuid);
      pkgDep.setDependentPackageGuid(tcDepPkgGuid);
      pkgDep.setImpliedDep(new Boolean(true));

      // Save the Package Info and Package Element objects
      pkgInfoService.savePkgInfo(pkgInfo);
      pkgInfoService.savePkgDependency(pkgDep);

      // Load the persisted object into a different Package Dependency
      // object
      List<PSPkgDependency> pkgDepsTest = pkgInfoService.loadPkgDependencies(
            tcParentGuid, true);
      assertEquals(pkgDepsTest.size(), 1);
      PSPkgDependency pkgDepTest = pkgDepsTest.get(0);
      // Test for these values with the getters (accessors)
      //
      assertEquals(pkgDep.getId(), pkgDepTest.getId());
      assertEquals(pkgDep.getOwnerPackageGuid(), pkgDepTest
            .getOwnerPackageGuid());
      assertEquals(pkgDep.getDependentPackageGuid(), pkgDepTest
            .getDependentPackageGuid());
      assertEquals(pkgDep.isImpliedDep(), pkgDepTest.isImpliedDep());

      // Load the modifiable version persisted object into a different
      // Package Info object and Test for these values with the getter
      List<PSPkgDependency> pkgDepsTest1 = pkgInfoService
            .loadPkgDependenciesModifiable(tcParentGuid, true);
      assertEquals(pkgDepsTest1.size(), 1);
      PSPkgDependency pkgDepTest1 = pkgDepsTest1.get(0);
      assertEquals(pkgDep.getId(), pkgDepTest1.getId());

      // To test the "find list of Package Dependencies by List" method,
      // we must create a few more Package Dependency objects. This is
      // done in same manner as above, but we assume the objects can be created
      // successfully (because it was successful above)
      final PSGuid tcDepPkgGuid1 = (PSGuid) guidMgr
            .createGuid(PSTypeEnum.PACKAGE_INFO);
      PSPkgDependency pkgDep1 = pkgInfoService.createPkgDependency();
      pkgDep1.setOwnerPackageGuid(tcParentGuid);
      pkgDep1.setDependentPackageGuid(tcDepPkgGuid1);
      pkgDep1.setImpliedDep(true);
      pkgInfoService.savePkgDependency(pkgDep1);

      // Test dependent package guids
      List<IPSGuid> guidList = pkgInfoService
            .findDependentPkgGuids(tcParentGuid);
      assertEquals(2, guidList.size());

      Boolean existsInList = false;
      existsInList = guidList.contains(tcDepPkgGuid);
      assertTrue(existsInList);
      existsInList = guidList.contains(tcDepPkgGuid1);
      assertTrue(existsInList);

      // Test the owner package guids
      List<IPSGuid> guidList1 = pkgInfoService.findOwnerPkgGuids(tcDepPkgGuid);
      assertEquals(1, guidList1.size());

      existsInList = guidList1.contains(tcParentGuid);
      assertTrue(existsInList);

      List<PSPkgDependency> pkgDeps = pkgInfoService
            .loadPkgDependenciesModifiable(tcParentGuid, true);
      // Make sure we have two deps
      assertEquals(2, pkgDeps.size());

      pkgInfoService.deletePkgDependency(pkgDeps.get(0).getId());
      pkgDeps = pkgInfoService.loadPkgDependenciesModifiable(tcParentGuid,
            true);
      assertEquals(1, pkgDeps.size());

      // Clean up these test objects (remove from persisted storage)
      pkgInfoService.deletePkgInfo(pkgInfo.getPackageDescriptorName());

      List<IPSGuid> guidList2 = pkgInfoService
            .findDependentPkgGuids(tcParentGuid);
      assertEquals(0, guidList2.size());
   }
   
   /**
    * Test the primary Package Configuration Info object's services.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testPkgConfigInfoServices() throws Exception
   {

      // Get the PkgInfo Service
      IPSPkgInfoService pkgInfoService = PSPkgInfoServiceLocator
            .getPkgInfoService();

      // Constants for testing (tc)
      //
      String tcDescriptorName3 = "A Test Descriptor Name 3";

      // Use service to create and return a Package Info object.
      PSPkgInfo pkgInfo = pkgInfoService.createPkgInfo(tcDescriptorName3);

      // Initialize the Package Info object
      //
      initPkgInfoPojo(pkgInfo, false, " 3");

      // Use service to create and return a Package Config Info object.
      pkgInfo.getGuid();

   }

}
