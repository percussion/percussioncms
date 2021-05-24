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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

/**
 * Tests {@link PSManagedNavService}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSManagedNavServiceTest extends ServletTestCase
{

   private static Logger log = LogManager.getLogger(PSManagedNavServiceTest.class);

   @Override
   public void setUp() throws Exception
   {
      IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
      secWs.login(request, response, "Admin", "demo", null,
            "Enterprise_Investments_Admin", null);
      
      navService = PSManagedNavServiceLocator.getContentWebservice();
      contentWs = PSContentWsLocator.getContentWebservice();
      createdFolders = new ArrayList<IPSGuid>();
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
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }
      }
   }
   
   /**
    * Tests find and get operations of the managed nav service.
    * 
    * @throws Exception if error occurs.
    */
   public void testFinds() throws Exception
   {
      String EI_ROOT = "//Sites/EnterpriseInvestments";
      List<IPSGuid> ids = contentWs.findPathIds(EI_ROOT);
      IPSGuid parentFolderId = ids.get(ids.size()-1);
      
      PSComponentSummary navSum = navService.findNavSummary(parentFolderId);
      assertTrue(navSum != null);
      
      IPSGuid navTreeId = new PSLegacyGuid(navSum.getCurrentLocator());
      String title = navService.getNavTitle(navTreeId);
      assertTrue(title != null && title.trim().length() != 0);
      
      List<IPSGuid> rels = navService.findChildNavonIds(navTreeId);
      assertTrue(rels.size() >= 4);
      int[] contentIds = new int[]{329, 324, 330, 320};
      for (int i=0; i<4; i++)
      {
         int contentId = ((PSLegacyGuid)rels.get(i)).getContentId();
         assertTrue(contentIds[i] == contentId);
      }
      
      rels = navService.findDescendantNavonIds(navTreeId);
      assertTrue(rels.size() >= 14);
      
      assertTrue(navService.isManagedNavUsed());
      
      long navtreeTypeId = navService.getNavtreeContentTypeId();
      long navonTypeId = navService.getNavonContentTypeId();
      
      assertTrue(navtreeTypeId > 0);
      assertTrue(navonTypeId > 0);
      
      String navtreeTypeName = navService.getNavtreeContentTypeName();
      String navonTypeName = navService.getNavonContentTypeName();
      
      assertTrue(StringUtils.isNotBlank(navtreeTypeName));
      assertTrue(StringUtils.isNotBlank(navonTypeName));
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
      assertTrue(navSum != null);
      
      return createdFolder;
   }
   
   /**
    * Tests add or move node service.
    * 
    * @throws Exception if error occurs.
    */
   public void testAdds() throws Exception
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
      assertTrue(items.size() == 1);
      assertTrue(items.get(0).getGUID().getUUID() == 335);

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
      List<IPSGuid> ids = contentWs.findPathIds(EI_ROOT);
      IPSGuid parentFolderId = ids.get(ids.size() - 1);

      PSComponentSummary navSum = navService.findNavSummary(parentFolderId);
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
      
      assertTrue(pair.getFirst().size() == calculatedParentNavons.size());
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
      PSPair<List<Integer>, IPSGuid> pair = new PSPair<List<Integer>, IPSGuid>();
      
      List<Integer> parentNavons = new ArrayList<Integer>();
      
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
      Iterator<IPSGuid> ids = createdFolders.iterator();
      while (ids.hasNext())
      {
         IPSGuid id = ids.next();
         if (id.equals(folderId))
         {
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
