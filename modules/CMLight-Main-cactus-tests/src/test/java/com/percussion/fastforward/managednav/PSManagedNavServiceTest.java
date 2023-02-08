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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests {@link PSManagedNavService}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSManagedNavServiceTest extends ServletTestCase
{

   private static final Logger log = LogManager.getLogger(PSManagedNavServiceTest.class);

   @Override
   public void setUp() throws Exception
   {
      IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
      secWs.login(request, response, "Admin", "demo", null,
            "Enterprise_Investments_Admin", null);
      
      navService = PSManagedNavServiceLocator.getContentWebservice();
      contentWs = PSContentWsLocator.getContentWebservice();
      createdFolders = new ArrayList<>();
   }
   
   @Override
   public void tearDown() throws Exception
   {
      if (!createdFolders.isEmpty())
      {
         try
         {
            // delete the folders one at a time and ignore errors
            Collections.reverse(createdFolders);
            for (IPSGuid id : createdFolders)
            {
               contentWs.deleteFolders(Collections.singletonList(id), true);
            }
         }
         catch (Exception e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         }
      }
   }
   
   /**
    * Tests find and get operations of the managed nav service.
    *
    */
   public void testFinds()
   {
      String EI_ROOT = "//Sites/EnterpriseInvestments";
      List<IPSGuid> ids = contentWs.findPathIds(EI_ROOT);
      IPSGuid parentFolderId = ids.get(ids.size()-1);
      
      PSComponentSummary navSum = navService.findNavSummary(parentFolderId);
      assertNotNull(navSum);
      
      IPSGuid navTreeId = new PSLegacyGuid(navSum.getCurrentLocator());
      String title = navService.getNavTitle(navTreeId);
      assertTrue(title != null && title.trim().length() != 0);
      
      List<IPSGuid> rels = navService.findChildNavonIds(navTreeId);
      assertTrue(rels.size() >= 4);
      int[] contentIds = new int[]{329, 324, 330, 320};
      for (int i=0; i<4; i++)
      {
         int contentId = ((PSLegacyGuid)rels.get(i)).getContentId();
         assertEquals(contentIds[i], contentId);
      }
      
      rels = navService.findDescendantNavonIds(navTreeId);
      assertTrue(rels.size() >= 14);
      
      assertTrue(navService.isManagedNavUsed());
      
      List<Long> navtreeTypeId = navService.getNavTreeContentTypeIds();
      List<Long> navonTypeId = navService.getNavonContentTypeIds();
      
      assertTrue(navtreeTypeId.size() > 0);
      assertTrue(navonTypeId.size() > 0);
      
      List<String> navtreeTypeName = navService.getNavTreeContentTypeNames();
      List<String> navonTypeName = navService.getNavonContentTypeNames();

      for(String s : navtreeTypeName) {
         assertTrue(StringUtils.isNotBlank(s));
      }
      for(String s : navonTypeName) {
         assertTrue(StringUtils.isNotBlank(s));
      }
   }
   
   /**
    * Number used to create unique folder names
    */
   private static int increment = 2000;

   private PSFolder createFolderAndNavon(String prefix, String parentPath)
   {
      String folderName = prefix + (System.currentTimeMillis() + increment)/1000;
      increment *= 2;
      PSFolder createdFolder = contentWs.addFolder(folderName, parentPath);
      IPSGuid folderId = createdFolder.getGuid();
      createdFolders.add(folderId);

      PSComponentSummary navSum = navService.findNavSummary(folderId);
      if (navSum != null)
      {
         throw new RuntimeException(
               "Folder effect is on, this is not expected behavior for this test.");
      }
      List<IPSGuid> ids = contentWs.findPathIds(parentPath);
      IPSGuid parentFolderId = ids.get(ids.size()-1);

      navService.addNavonToFolder(parentFolderId, folderId, folderName, folderName);
      navSum = navService.findNavSummary(createdFolder.getGuid());
      assertNotNull(navSum);
      
      return createdFolder;
   }
   
   /**
    * Tests add or move node service.
    */
   public void testAdds()
   {
      String EI_ROOT = "//Sites/EnterpriseInvestments";
      List<IPSGuid> ids = contentWs.findPathIds(EI_ROOT);
      IPSGuid parentFolderId = ids.get(ids.size()-1);
      
      PSComponentSummary navSum = navService.findNavSummary(parentFolderId);
      IPSGuid navTreeId = new PSLegacyGuid(navSum.getCurrentLocator());
      String title = navService.getNavTitle(navTreeId);
      
      PSFolder createdFolder =  createFolderAndNavon("TestFolder_", EI_ROOT);
      negativeTestAddNavon(createdFolder, EI_ROOT);
      
      navSum = navService.findNavSummary(createdFolder.getGuid());
         
      // link the navon (owner) to the landing page (dependent)
      IPSGuid navonId = new PSLegacyGuid(navSum.getCurrentLocator());
      IPSGuid pageId = new PSLegacyGuid(335, 1);
      PSItemStatus status = contentWs.prepareForEdit(navonId);
      navService.addLandingPageToNavnode(pageId, navonId, "rffSnTitleLink");
      contentWs.releaseFromEdit(status, false);
      boolean isLandingPage = navService.isLandingPage(pageId);
      assertTrue(isLandingPage);
      
      // test get/set nav-title
      String displayTitle = navService.getNavTitle(navonId);
      String newTitle = title + "-New";
      navService.setNavTitle(navonId, newTitle);
      assertEquals(newTitle,  navService.getNavTitle(navonId));
      navService.setNavTitle(navonId, displayTitle);
      
      // get the landing page id back from the navon
      IPSGuid lpId = navService.getLandingPageFromNavnode(navonId);
      assertEquals(pageId.getUUID(), lpId.getUUID());
      
      // try to get landing page from a bogus navon
      IPSGuid bogusNavonId = new PSLegacyGuid(888, 1);
      assertNull(navService.getLandingPageFromNavnode(bogusNavonId));
      
      // validate the link
      List<PSItemSummary> items = contentWs.findDependents(navonId, null, false);
      assertEquals(1, items.size());
      assertEquals(335, items.get(0).getGUID().getUUID());

      validateMoveService(createdFolder, EI_ROOT);
   }

   public void testIsNavTree_withNavTree()
   {
      String EI_ROOT = "//Sites/EnterpriseInvestments";
      List<IPSGuid> ids = contentWs.findPathIds(EI_ROOT);
      IPSGuid parentFolderId = ids.get(ids.size() - 1);

      PSComponentSummary navSum = navService.findNavSummary(parentFolderId);
      IPSGuid navTreeId = new PSLegacyGuid(navSum.getCurrentLocator());

      assertTrue("The guid should have been detected as a navTree object, but was not.",
            navService.isNavTree(navTreeId));
   }

   public void testIsNavTree_withNavon()
   {
      String EI_ROOT = "//Sites/EnterpriseInvestments";

      PSComponentSummary navSum;
      PSFolder createdFolder = createFolderAndNavon("TestFolder_", EI_ROOT);
      negativeTestAddNavon(createdFolder, EI_ROOT);

      navSum = navService.findNavSummary(createdFolder.getGuid());

      // link the navon (owner) to the landing page (dependent)
      IPSGuid navonId = new PSLegacyGuid(navSum.getCurrentLocator());

      assertFalse("The guid should not have been detected as a navTree object.", navService.isNavTree(navonId));
   }

   public void testFindParentNavons() 
   {
      PSPair<List<Integer>, IPSGuid> pair = createNavigationStructure();
      
      List<IPSGuid> calculatedParentNavons = navService.findAncestorNavonIds(pair.getSecond());

      assertEquals(pair.getFirst().size(), calculatedParentNavons.size());
      for(IPSGuid parentNavon : calculatedParentNavons)
      {
         assertTrue(pair.getFirst().contains(((PSLegacyGuid) parentNavon).getContentId()));
      }
   }

   /**
    * Creates the navigation structure described below. The targed is the (*)
    * node. So the method returns the ids that correspond to that section path.
    * 
    * <pre>
    * - EnterpriseInvestments
    *   - section 1
    *   - section 2
    *     - section 2 - 1
    *       - section 2 - 1 - 1 (*)
    *       - section 2 - 1 - 2
    *     - section 2 - 2
    *     - section 2 - 3
    * </pre>
    */
   @SuppressWarnings("unused")
   private PSPair<List<Integer>, IPSGuid> createNavigationStructure()
   {
      PSPair<List<Integer>, IPSGuid> pair = new PSPair<>();
      
      List<Integer> parentNavons = new ArrayList<>();
      
      String EI_ROOT = "//Sites/EnterpriseInvestments";
      List<IPSGuid> ids = contentWs.findPathIds(EI_ROOT);
      IPSGuid parentFolderId = ids.get(ids.size() - 1);
      parentNavons.add(navService.findNavSummary(parentFolderId).getContentId());

      // first level
      PSFolder section1 = createFolderAndNavon("Section1", EI_ROOT);

      PSFolder section2 = createFolderAndNavon("Section2", EI_ROOT);
      parentNavons.add(navService.findNavSummary(section2.getGuid()).getContentId());
         
      // second level
      PSFolder section21 = createFolderAndNavon("Section2-1", section2.getFolderPath());
      parentNavons.add(navService.findNavSummary(section21.getGuid()).getContentId());
         
      PSFolder section22 = createFolderAndNavon("Section2-2", section2.getFolderPath());
      PSFolder section23 = createFolderAndNavon("Section2-3", section2.getFolderPath());
         
      // third level
      PSFolder section211 = createFolderAndNavon("Section2-1", section21.getFolderPath());
      PSFolder section212 = createFolderAndNavon("Section2-1", section21.getFolderPath());

      pair.setFirst(parentNavons);
      pair.setSecond(new PSLegacyGuid(navService.findNavSummary(section211.getGuid()).getContentId()));
 
      return pair;     
   }

   /**
    * Test move navigation nodes.
    * 
    * @param folder1 the folder that is appended to the parent folder below.
    * @param parentPath the parent folder path, assumed not blank.
    */
   private void validateMoveService(PSFolder folder1, String parentPath)
   {
      PSLegacyGuid navTreeId = getNavonIdFromPath(parentPath);
      
      PSFolder folder2 =  createFolderAndNavon("TestFolder_", parentPath);
      PSFolder folder3 =  createFolderAndNavon("TestFolder_", parentPath);
      PSComponentSummary navon_1 = navService.findNavSummary(folder1.getGuid());
      PSComponentSummary navon_2 = navService.findNavSummary(folder2.getGuid());
      
      List<IPSGuid> ids = navService.findChildNavonIds(navTreeId);
      assertTrue(ids.size() >= 3);
      int length = ids.size();
      PSLegacyGuid lastId = (PSLegacyGuid) ids.get(length - 1);
      PSLegacyGuid last2ndId = (PSLegacyGuid) ids.get(length - 2);
      PSLegacyGuid last3ndId = (PSLegacyGuid) ids.get(length - 3);
      assertEquals(navon_1.getContentId(), last3ndId.getContentId());
      assertEquals(navon_2.getContentId(), last2ndId.getContentId());
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // test move child node within the same parent node
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      navService.moveNavon(last3ndId, null, navTreeId, length -1);
      
      // get the updated "navTreeId" because its revision may changed
      navTreeId = getNavonIdFromPath(parentPath);
      ids = navService.findChildNavonIds(navTreeId);
      PSLegacyGuid lastId_2 = (PSLegacyGuid) ids.get(length - 1);
      assertEquals(last3ndId.getContentId(), lastId_2.getContentId());
      
      navService.moveNavon(lastId_2, null, navTreeId, length -3);

      navTreeId = getNavonIdFromPath(parentPath);
      ids = navService.findChildNavonIds(navTreeId);
      PSLegacyGuid lastId_3 = (PSLegacyGuid) ids.get(length - 3);
      assertEquals(last3ndId.getContentId(), lastId_3.getContentId());
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // test move child node between parent nodes
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      String targetPath = parentPath + "/" + folder1.getName();
      PSLegacyGuid targetId = getNavonIdFromPath(targetPath);
      ids = navService.findChildNavonIds(targetId);
      assertEquals(ids.size(), 0);
      
      navService.moveNavon(lastId, null, targetId, 0);
      clearFolder(folder3);
      navService.moveNavon(last2ndId, null, targetId, 0);
      clearFolder(folder2);
      
      // validate target parent
      targetId = getNavonIdFromPath(targetPath);
      ids = navService.findChildNavonIds(targetId);
      assertEquals(ids.size(), 2);
      
      // validate source parent
      ids = navService.findChildNavonIds(navTreeId);
      assertEquals(ids.size(), length - 2);
   }
   
   /**
    * This is called after the specified folder has been moved under another
    * created folder (which will be deleted in tearDown() later.
    * 
    * @param folder the folder in question, assumed not <code>null</code>.
    */
   private void clearFolder(PSFolder folder)
   {
      IPSGuid folderId = folder.getGuid();
      for (IPSGuid id : createdFolders) {
         if (id.equals(folderId)) {
            createdFolders.remove(id);
            break;
         }
      }
   }
   
   /**
    * Gets the navigation ID which is relate to the specified folder
    * 
    * @param folderPath the folder path that contains a navigation node,
    * assumed not blank.
    * 
    * @return the ID of the node.
    */
   private PSLegacyGuid getNavonIdFromPath(String folderPath)
   {
      IPSGuid navonId = navService.findNavigationIdFromFolder(folderPath);
      
      // get header revision of the navon item
      PSLocator loc = PSWebserviceUtils.getItemLocator((PSLegacyGuid)navonId);
      return new PSLegacyGuid(loc);
   }
   
   /**
    * Test add navon to a folder that contains navon or navtree.
    * 
    * @param createdFolder the folder contains navon.
    * @param parentPath the parent path, which contains navtree.
    */
   private void negativeTestAddNavon(PSFolder createdFolder, String parentPath)
   {
      // try to add navTree to folder with navon
      try
      {
         navService.addNavTreeToFolder(createdFolder.getFolderPath(), "__NavTree", "__NavTree");
         fail("NavTree was added to a folder with a Navon");
      }
      catch (PSNavException e)
      {
         // expected
      }
      
      // try to add navTree to folder with navTree
      try
      {
         navService.addNavTreeToFolder(parentPath, "__NavTree", "__NavTree");
         fail("NavTree was added to a folder with a NavTree");
      }
      catch (PSNavException e)
      {
         // expected
      }      
   }

   private IPSManagedNavService navService;
   private IPSContentWs contentWs;

   /**
    * The place-holder for a created folders, reset by {@link #setUp()}
    * and the folder is removed/cleaned up by {@link #tearDown()}.
    */
   private List<IPSGuid> createdFolders = null;
}
