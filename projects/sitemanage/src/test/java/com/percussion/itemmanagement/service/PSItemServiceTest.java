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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.itemmanagement.service;

import static java.util.Arrays.asList;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.itemmanagement.service.IPSItemService.PSUserItemTypeEnum;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.role.service.impl.PSRoleService;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.user.data.PSUser;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSItemServiceTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;
        
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
        
    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
    }
    
    public void testCopyFolder() throws Exception
    {
        String asset1Id = null;
        String asset2Id = null;
        List<IPSGuid> folderIds = new ArrayList<IPSGuid>();

        try
        {
            // create a folder
            List<PSFolder> folders = contentWs.addFolderTree(PSAssetPathItemService.ASSET_ROOT + "/Test");
            String folderPath = folders.get(0).getFolderPath();
            IPSGuid folderId = folders.get(0).getGuid();
            folderIds.add(folderId);
            
            // create an asset in folder
            PSAsset asset1 = createAsset("asset1", folderPath);
            asset1Id = asset1.getId();
            
            // create a sub-folder
            folders = contentWs.addFolderTree(PSAssetPathItemService.ASSET_ROOT + "/Test/TestSub");
            String subFolderPath = folders.get(0).getFolderPath();
                        
            // create an asset in sub-folder
            PSAsset asset2 = createAsset("asset2", subFolderPath);
            asset2Id = asset2.getId();
            
            // copy folder
            Map<String, String> assetMap = itemService.copyFolder(folderPath, PSAssetPathItemService.ASSET_ROOT,
                    "TestCopy");
            
            List<PSFolder> copyFolders = contentWs.loadFolders(new String[]{PSAssetPathItemService.ASSET_ROOT
                    + "/TestCopy"});
            assertTrue(!copyFolders.isEmpty());
            IPSGuid copyFolderId = copyFolders.get(0).getGuid();
            folderIds.add(copyFolderId);            
            assertFolders(folderId, copyFolderId);
            
            assertEquals(2, assetMap.size());
            assertTrue(assetMap.containsKey(asset1Id));
            assertTrue(assetMap.containsKey(asset2Id));
            
            // create another folder
            folders = contentWs.addFolderTree(PSAssetPathItemService.ASSET_ROOT + "/TestCopy2");
            IPSGuid copy2FolderId = folders.get(0).getGuid();
            folderIds.add(copy2FolderId);
            
            // copy folder to existing folder
            assetMap = itemService.copyFolder(folderPath, PSAssetPathItemService.ASSET_ROOT, "TestCopy2");
    
            copyFolders = contentWs.loadFolders(new String[]{PSAssetPathItemService.ASSET_ROOT + "/TestCopy2"});
            assertTrue(!copyFolders.isEmpty());
                          
            assertFolders(folderId, copy2FolderId);

            assertEquals(2, assetMap.size());
            assertTrue(assetMap.containsKey(asset1Id));
            assertTrue(assetMap.containsKey(asset2Id));            
        }
        finally
        {
            // wait before deleting (to allow for search index queue processing)
            Thread.sleep(1000);
            
            if (asset1Id != null)
            {        
                assetService.delete(asset1Id);
            }
            
            if (asset2Id != null)
            {
                assetService.delete(asset2Id);
            }
            
            if (!folderIds.isEmpty())
            {
                contentWs.deleteFolders(folderIds, false);
            }
        }
    }
    
    public void testMyPages() throws Exception
    {
        myPagesAddRemoveTest();
        myPagesItemDeleteTest();
        myPagesDeleteUserTest();
        myPagesGetTest();
    }
    
    /**
     * Convenient method to test my pages add and remove functionality.
     * @throws Exception
     */
    private void myPagesAddRemoveTest() throws Exception
    {
        //Test add and remove from my pages
        PSPage page1 = fixture.createPage("myPagesAddRemoveTest1");
        String id1 = page1.getId();
        assertFalse(itemService.isMyPage(id1));
        itemService.addToMyPages(id1);
        assertTrue(itemService.isMyPage(id1));
        itemService.removeFromMyPages(id1);
        assertFalse(itemService.isMyPage(id1));
        
        //Add the page to multiple users and make sure data is not getting overwritten
        //Adds it to the logged in user
        int count = itemService.getUserItems(id1).size();
        itemService.addToMyPages(id1);
        //Add it to Editor
        itemService.addUserItem("Editor", idMapper.getContentId(id1), PSUserItemTypeEnum.FAVORITE_PAGE);
        //Make sure we get two more than original size.
        assertTrue(itemService.getUserItems(idMapper.getContentId(id1)).size()==count+2);
    }
    
    /**
     * Convenient method to test my pages functionality when an item is deleted.
     * @throws Exception
     */
    private void myPagesItemDeleteTest() throws Exception
    {
        //Test delete page
        PSPage page1 = fixture.createPage("myPagesItemDeleteTest1");
        PSPage page2 = fixture.createPage("myPagesItemDeleteTest2");
        String id1 = page1.getId();
        String id2 = page2.getId();
        itemService.addToMyPages(id1);
        itemService.addToMyPages(id2);
        
        pageService.delete(id1);
        //make sure page2 is not in my pages anymore
        assertFalse(itemService.isMyPage(id1));
        //make sure I still have page3 in my pages
        assertTrue(itemService.isMyPage(id2));
    }
    
    /**
     * Convenient test to test my pages get functionality.
     */
    private void myPagesGetTest() throws Exception
    {
        int origCount = itemService.getUserItems(PSWebserviceUtils.getUserName()).size();        
        PSPage page1 = fixture.createPage("myPagesGetTest1");
        PSPage page2 = fixture.createPage("myPagesGetTest2");
        String id1 = page1.getId();
        String id2 = page2.getId();
        itemService.addToMyPages(id1);
        itemService.addToMyPages(id2);
        int newCount = itemService.getUserItems(PSWebserviceUtils.getUserName()).size();
        //As we added two pages here, make sure when we get the pages the new count is two more than origCount.
        assertTrue( newCount == origCount+2);
        //Add page 1 to a different user
        itemService.addUserItem("Editor", idMapper.getContentId(id1), PSUserItemTypeEnum.FAVORITE_PAGE);
        //As we added the page 1 to a different user the new count still should be 2 more than origCount.
        newCount = itemService.getUserItems(PSWebserviceUtils.getUserName()).size();
        assertTrue( newCount == origCount+2);
        
    }

    /**
     * Convenient test to test my pages when a user is deleted.
     */
    private void myPagesDeleteUserTest() throws Exception
    {
        //Add a MyPagesTest user
        String testuser = "MyPagesTestUser";
        PSUser myPagesUser = new PSUser();
        myPagesUser.setName(testuser);
        myPagesUser.setPassword("demo");
        myPagesUser.setRoles(Collections.singletonList(PSRoleService.ADMINISTRATOR_ROLE));
        userService.create(myPagesUser);
        boolean isUserDeleted = false;
        try
        {
            PSPage page1 = fixture.createPage("myPagesDeleteUserTest1");
            PSPage page2 = fixture.createPage("myPagesDeleteUserTest2");
            String id1 = page1.getId();
            String id2 = page2.getId();
            itemService.addUserItem(testuser, idMapper.getContentId(id1), PSUserItemTypeEnum.FAVORITE_PAGE);
            itemService.addUserItem(testuser, idMapper.getContentId(id2), PSUserItemTypeEnum.FAVORITE_PAGE);
            int count = itemService.getUserItems(testuser).size();
            //Make sure we have above pages stored for the test user
            assertTrue(count > 0);
            //Delete the testuser
            userService.delete(testuser);
            isUserDeleted = true;
            count = itemService.getUserItems(testuser).size();
            //Make sure the count is zero now
            assertTrue(count == 0);
        }
        finally
        {
            if(!isUserDeleted)//I would have used find method but if the user is already deleted the user service's find method also throws exception.
                userService.delete(testuser);
        }

    }

    /**
     * Creates an asset.
     * 
     * @param name assumed not <code>null</code>.
     * @param folder assumed not <code>null</code>.
     * 
     * @return {@link PSAsset} representation of the asset item, never <code>null</code>.
     * 
     * @throws Exception if an error occurs saving the asset.
     */
    private PSAsset createAsset(String name, String folder) throws Exception
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", name);
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        if (folder != null)
        {
            asset.setFolderPaths(asList(folder));
        }
             
        return assetService.save(asset);
    }
    
    private void assertFolders(IPSGuid id1, IPSGuid id2)
    {
        List<PSItemSummary> items1 = contentWs.findFolderChildren(id1, false);
        List<PSItemSummary> items2 = contentWs.findFolderChildren(id2, false);
        assertEquals(items1.size(), items2.size());
    
        for (PSItemSummary item1 : items1)
        {
            boolean match = false;
            String item1Name = item1.getName();
            IPSGuid item1Guid = item1.getGUID();
            
            for (PSItemSummary item2 : items2)
            {
                String item2Name = item2.getName();
                IPSGuid item2Guid = item2.getGUID();
                
                if (item2Name.equals(item1Name))
                {
                    PSFolder folder1 = null;
                    try
                    {
                        folder1 = contentWs.loadFolder(item1Guid);
                    }
                    catch (PSErrorException e)
                    {
                        // item1 is not a folder
                    }
                    
                    if (folder1 != null)
                    {
                        try
                        {
                            contentWs.loadFolder(item2Guid);
                        }
                        catch (PSErrorException e)
                        {
                            fail(item2Name + " should be a folder");
                        }
                        
                        assertFolders(item2Guid, item1Guid);
                    }
                                   
                    match = true;
                    break;
                }
            }
            
            assertTrue("match not found for " + item1Name, match);
        }
        
    }
    
    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }
    
    /**
     * @return the contentWs
     */
    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    /**
     * @param contentWs the contentWs to set
     */
    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }
    
    /**
     * @return the itemService
     */
    public IPSItemService getItemService()
    {
        return itemService;
    }

    /**
     * @param itemService the itemService to set
     */
    public void setItemService(IPSItemService itemService)
    {
        this.itemService = itemService;
    }
    
    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public IPSUserService getUserService()
    {
        return userService;
    }

    public void setUserService(IPSUserService userService)
    {
        this.userService = userService;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    private IPSAssetService assetService;
    private IPSContentWs contentWs;
    private IPSItemService itemService;
    private IPSPageService pageService;
    private IPSUserService userService;
    private IPSIdMapper idMapper;

  
}
