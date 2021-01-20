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
package com.percussion.pathmanagement.service;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.pathmanagement.service.IPSPathService.PSPathServiceException;
import com.percussion.pathmanagement.service.impl.PSDispatchingPathService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.data.PSSimpleDisplayFormat;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.ui.service.IPSListViewProcessor;
import com.percussion.ui.service.IPSUiService;
import com.percussion.user.data.*;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Scenario description: 
 * PSDispatchingPathService has some services registered to it.
 * Paths that begin with /a/ should go to path service a.
 * Paths that begin with /b/ should go to path service b.
 * 
 * @author adamgent, Oct 1, 2009
 */
@RunWith(JMock.class)
@Ignore("Ignored for now until this unit test is moved into a cactus test suite")
public class PSDispatchingPathServiceTest
{

    Mockery context = new JUnit4Mockery();

    PSDispatchingPathService ps;

    IPSPathService pathServiceA;
    IPSPathService pathServiceB;
    PSPathItem rootA;
    PSPathItem rootB;
    Map<String, IPSPathService> pathRegistry;

    IPSRecycleService recycleService = new IPSRecycleService() {
        @Override
        public void recycleItem(int dependentId) {

        }

        @Override
        public void recycleFolder(IPSGuid guid) {

        }

        @Override
        public void restoreItem(String guid) {

        }

        @Override
        public void restoreFolder(String guid) {

        }

        @Override
        public List<IPSItemSummary> findChildren(String path) {
            return null;
        }

        @Override
        public IPSItemSummary findItem(String path) {
            return null;
        }

        /***
         * Returns a boolean indicating if the specified guid is in the Recycler.
         * @param guid A valid guid to search for, never null
         * @return true if guid is in the recycler, false if not
         */
        @Override
        public boolean isInRecycler(String guid) {
            return false;
        }

        @Override
        public boolean isNavInRecycler(String guid) {
            return false;
        }

        /***
         * Returns a boolean indicating if the specified guid is in the Recycler.
         * @param guid A valid guid to search for, never null
         * @return true if guid is in the recycler, false if not
         */
        @Override
        public boolean isInRecycler(IPSGuid guid) {
            return false;
        }

    };

    IPSFolderHelper folderHelper = new IPSFolderHelper() {
        @Override
        public String pathSeparator() {
            return null;
        }

        @Override
        public void addItem(String path, String id) throws Exception {

        }

        @Override
        public String findPathFromLegacyFolderId(Number id) throws Exception {
            return null;
        }

        @Override
        public Number findLegacyFolderIdFromPath(String path) throws Exception {
            return null;
        }

        @Override
        public void removeItem(String path, String itemId, boolean purgeItem) throws Exception {

        }

        @Override
        public List<String> findPaths(String itemId) throws Exception {
            return null;
        }

        @Override
        public List<String> findPaths(String itemId, String relationshipTypeName) throws Exception {
            return null;
        }

        @Override
        public List<IPSItemSummary> findItems(String path) throws Exception {
            return null;
        }

        @Override
        public List<IPSItemSummary> findItems(String path, boolean foldersOnly) throws Exception {
            return null;
        }

        @Override
        public List<String> findItemIdsByPath(String path) throws Exception {
            return null;
        }

        @Override
        public IPSItemSummary findItem(String path) throws Exception {
            return null;
        }

        @Override
        public PSPathItem findItemById(String id) {
            return null;
        }

        @Override
        public PSPathItem findItemById(String id, String relationshipTypeName) {
            return null;
        }

        @Override
        public IPSGuid getParentFolderId(IPSGuid itemId) {
            return null;
        }

        @Override
        public IPSGuid getParentFolderId(IPSGuid itemId, boolean isRequired) {
            return null;
        }

        @Override
        public PSPathItem setFolderAccessLevel(PSPathItem item) {
            return null;
        }

        @Override
        public List<PSPathItem> setFolderAccessLevel(List<PSPathItem> items) {
            return null;
        }

        @Override
        public PathTarget pathTarget(String path) {
            return null;
        }

        @Override
        public PathTarget pathTarget(String path, boolean shouldRecycle) {
            return null;
        }

        @Override
        public String getUniqueFolderName(String parentPath, String baseName) {
            return null;
        }

        @Override
        public String getUniqueNameInFolder(String parentPath, String baseName, String suffix, int startingIndex, boolean skipFirstIndex) {
            return null;
        }

        @Override
        public String concatPath(String start, String... end) {
            return null;
        }

        @Override
        public String parentPath(String path) {
            return null;
        }

        @Override
        public String name(String path) {
            return null;
        }

        @Override
        public void createFolder(String path) throws Exception {

        }

        @Override
        public void createFolder(String path, PSFolderPermission.Access acl) throws Exception {

        }

        @Override
        public void deleteFolder(String path) throws Exception {

        }

        @Override
        public void deleteFolder(String path, boolean recycleFolder) throws Exception {

        }

        @Override
        public boolean validateFolderPermissionForDelete(String folderId) {
            return false;
        }

        @Override
        public boolean hasFolderPermission(String folderId, PSFolderPermission.Access acl) {
            return false;
        }

        @Override
        public void renameFolder(String path, String name) throws Exception {

        }

        @Override
        public List<String> findChildren(String path) throws Exception {
            return null;
        }

        @Override
        public PSItemProperties findItemProperties(String path) throws Exception {
            return null;
        }

        @Override
        public PSItemProperties findItemProperties(String path, String relationshipTypeName) throws Exception {
            return null;
        }

        @Override
        public PSItemProperties findItemPropertiesById(String id) throws Exception {
            return null;
        }

        @Override
        public PSItemProperties findItemPropertiesById(String id, String relationshipTypeName) throws Exception {
            return null;
        }

        @Override
        public PSFolderPermission.Access getFolderAccessLevel(String id) {
            return null;
        }

        @Override
        public PSFolderProperties findFolderProperties(String id) throws PSErrorException {
            return null;
        }

        @Override
        public void saveFolderProperties(PSFolderProperties folder) {

        }

        @Override
        public String getFolderPath(String path) {
            return null;
        }

        @Override
        public void moveItem(String targetFolderPath, String itemPath, boolean isFolder) {

        }

        @Override
        public IPSItemSummary findFolder(String path) throws Exception {
            return null;
        }

        @Override
        public List<IPSSite> getItemSites(String contentId) {
            return null;
        }

        @Override
        public boolean validateFolderReservedName(String name) {
            return false;
        }

        @Override
        public int getValidWorkflowId(PSFolderProperties folder) {
            return 0;
        }

        @Override
        public int getDefaultWorkflowId() {
            return 0;
        }

        @Override
        public PSFolder getRootFolderForAsset(String assetId) {
            return null;
        }

        @Override
        public String getRootLevelFolderAllowedSitesPropertyValue(String assetId) {
            return null;
        }

        @Override
        public boolean isFolderValidForRecycleOrRestore(String targetPath, String originalPath,
                                                        String targetRelType, String origRelType) {
            return false;
        }

        @Override
        public PSPair<String, String> fixupLastModified(IPSGuid id, String userName, Date lastModified, boolean isPublishable) {
            return null;
        }
    };
    
    IPSUiService uiService = new IPSUiService()
    {
        public PSSimpleDisplayFormat getDisplayFormatByName(String name)
        {
            return null;
        }
        
        public PSSimpleDisplayFormat getDisplayFormat(int id)
        {
            return null;
        }
    };
    
    IPSUserService userService = new IPSUserService()
    {
        
        @Override
        public PSUser update(PSUser user) throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            return new PSUser();
        }
        
        @Override
        public PSUser changePassword(PSUser user) throws PSDataServiceException
        {
        	// FIXME Auto-generated method stub
            return new PSUser();
        }
        
        @Override
        public boolean isAdminUser(String userName)
        {
            // FIXME Auto-generated method stub
            return false;
        }
        
        @Override
        public List<PSImportedUser> importDirectoryUsers(PSImportUsers importUsers) throws PSDirectoryServiceException
        {
            // FIXME Auto-generated method stub
            return new ArrayList<PSImportedUser>();
        }
        
        @Override
        public PSUserList getUsersByRole(String roleName) throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            return new PSUserList();
        }
        
        @Override
        public PSUserList getUsers() throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            return new PSUserList();
        }
        
        @Override
        public PSRoleList getRoles() throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            return new PSRoleList();
        }
        
        @Override
        public PSCurrentUser getCurrentUser() throws PSNoCurrentUserException
        {
            // FIXME Auto-generated method stub
            return new PSCurrentUser();
        }
        
        @Override
        public List<PSExternalUser> findUsersFromDirectoryService(String query) throws PSDirectoryServiceException
        {
            // FIXME Auto-generated method stub
            return new ArrayList<PSExternalUser>();
        }
        
        @Override
        public PSUser find(String name) throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            return new PSUser();
        }
        
        @Override
        public void delete(String name) throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            
        }
        
        @Override
        public PSUser create(PSUser user) throws PSDataServiceException
        {
            // FIXME Auto-generated method stub
            return new PSUser();
        }
        
        @Override
        public PSDirectoryServiceStatus checkDirectoryService()
        {
            // FIXME Auto-generated method stub
            return new PSDirectoryServiceStatus();
        }

        public PSAccessLevel getAccessLevel(PSAccessLevelRequest request)
        {
            PSAccessLevel accessLevel = new PSAccessLevel();
            accessLevel.setAccessLevel(PSAssignmentTypeEnum.ADMIN.name());
            
            return accessLevel;
        }

        @Override
        public PSUserList getUserNames(String nameFilter) throws PSDataServiceException
        {
            return new PSUserList();
        }

        @Override
        public boolean isDesignUser(String userName)
        {
            return false;
        }
    };
    
    IPSListViewHelper listViewHelper = new IPSListViewHelper()
    {
        public void fillDisplayProperties(PSDisplayPropertiesCriteria criteria)
        {
        }

        @Override
        public void setPostProcessors(List<IPSListViewProcessor> processors)
        {
        }
    };
    
    @Before
    public void setUp() throws Exception
    {
        pathServiceA = context.mock(IPSPathService.class, "pathServiceA");
        pathServiceB = context.mock(IPSPathService.class, "pathServiceB");
        pathRegistry = new HashMap<String, IPSPathService>();
        pathRegistry.put("/a/", pathServiceA);
        pathRegistry.put("/b/", pathServiceB);
        rootA = new PSPathItem();
        rootA.setPath("/");
        rootB = new PSPathItem();
        rootB.setPath("/");
        ps = new PSDispatchingPathService(uiService, userService, listViewHelper, recycleService, folderHelper);
        ps.setRegistry(pathRegistry);
    }

    @Test
    public void shouldFindByDispatchingToA()
    {
        /*
         * Given: See setup. We have a path item that the service will return that is valid.
         */
        final PSPathItem pathItem = new PSPathItem();
        pathItem.setPath("/b/c/");
        /* 
         * Expect: pathServiceA to be called.
         */
         
        context.checking(new Expectations() {{
            one(pathServiceA).find("/b/c/");
            will(returnValue(pathItem));
        }});

        /*
         * When: we call find.
         */

        PSPathItem actual = ps.find("/a/b/c");
        
        /*
         * Then: the return path item should have the full path.
         */

        assertEquals("/a/b/c/", actual.getPath());
    }
    
    @Test(expected=PSPathServiceException.class)
    public void shouldFailIfDispatchedServiceReturnsAPathItemWithOutPathSet()
    {
        /*
         * Given: See setup. We have a path item that the service will return that is INVALID
         * Because the path is null.
         */
        final PSPathItem pathItem = new PSPathItem();
        pathItem.setPath(null);
        
        /* 
         * Expect: pathServiceA to be called.
         */
         
        context.checking(new Expectations() {{
            one(pathServiceA).find("/b/c/");
            will(returnValue(pathItem));
        }});

        /*
         * When: we call find.
         */

        ps.find("/a/b/c");
        
        /*
         * Then: We should throw an exception.
         */
    }
    
    @Test(expected=PSPathServiceException.class)
    public void shouldFailIfDispatchedServiceReturnsAPathItemWithPathNotStartingWithRelative()
    {
        /*
         * Given: See setup. We have a path item that the service will return that is INVALID
         * Because the path should begin with /b/c/.
         */
        final PSPathItem pathItem = new PSPathItem();
        pathItem.setPath("/d");
        
        /* 
         * Expect: pathServiceA to be called.
         */
         
        context.checking(new Expectations() {{
            one(pathServiceA).find("/b/c/");
            will(returnValue(pathItem));
        }});

        /*
         * When: we call find.
         */

        ps.find("/a/b/c");
        
        /*
         * Then: We should throw an exception.
         */
    }
    
    public void shouldNotFailIfDispatchedServiceReturnsAPathItemWithPathStartingWithIncorrectCasing()
    {
        /*
         * Given: See setup. We have a path item that the service will return that has different
         * casing then the path request /b/c/
         */
        final PSPathItem pathItem = new PSPathItem();
        pathItem.setPath("/B/C");
        
        /* 
         * Expect: pathServiceA to be called.
         */
         
        context.checking(new Expectations() {{
            one(pathServiceA).find("/b/c/");
            will(returnValue(pathItem));
        }});

        /*
         * When: we call find.
         */

        ps.find("/a/b/c");
        
        /*
         * Then: We should NOT throw an exception.
         */
    }
    
    @Test
    public void shouldFindChildrenByDispatchingToB()
    {
        /*
         * Given: path service B has the following children under its root.
         */

        final List<PSPathItem> pathItems = new ArrayList<PSPathItem>();
        for(String relativePath : new String[] {"/x/","/y/","/z/"}) {
            PSPathItem item = new PSPathItem();
            item.setPath(relativePath);
            pathItems.add(item);
        }
        
        /* 
         * Expect: pathServiceB to be called.
         */

        context.checking(new Expectations() {{
            one(pathServiceB).findChildren("/");
            will(returnValue(pathItems));
        }});

        /*
         * When: we call find children.
         */

        List<PSPathItem> actual = ps.findChildren("/b/");
        
        /*
         * Then: the return path item should have the full path and not the relative path above.
         */
        assertEquals("/b/x/", actual.get(0).getPath());
        assertEquals("/b/y/", actual.get(1).getPath());
        assertEquals("/b/z/", actual.get(2).getPath());
    }
    
    @Test
    public void shouldFindRootChildrenByCallingFindOnEachPathService()
    {
        /*
         * Given: path service B has the following children under its root.
         */

        final List<PSPathItem> pathItems = new ArrayList<PSPathItem>();
        for(String relativePath : new String[] {"/a/","/b/"}) {
            PSPathItem item = new PSPathItem();
            item.setPath(relativePath);
            pathItems.add(item);
        }
        
        /* 
         * Expect: each path service to be called in alpha order.
         */

        context.checking(new Expectations() {{
            Sequence seq = context.sequence("rootFind");
            one(pathServiceB).find("/");
            will(returnValue(rootB)); inSequence(seq);
            one(pathServiceA).find("/"); inSequence(seq);
            will(returnValue(rootA));
          
            
        }});

        /*
         * When: we call find children.
         */

        List<PSPathItem> actual = ps.findChildren("/");
        
        /*
         * Then: the return paths of all the sub services.
         */
        
        assertEquals("/b/", actual.get(0).getPath());
        assertEquals("/a/", actual.get(1).getPath());
    }
    
    @Test
    public void shouldFindRoot()
    {
        /*
         * Given: nothing see setup.
         */

        
        /* 
         * Expect: nothing as the root path service will do this on its own.
         */

        context.checking(new Expectations() {{
        }});

        /*
         * When: we call find children.
         */

        PSPathItem actual = ps.find("/");
        
        /*
         * Then: the return paths of all the sub services.
         */
        
        assertEquals("/", actual.getPath());
    }
    
    
    @Test(expected=PSPathNotFoundServiceException.class)
    public void shouldFailOnNullPathForFind() throws Exception
    {
        String path = null;
        ps.find(path);
    }
    
    @Test(expected=PSPathNotFoundServiceException.class)
    public void shouldFailOnNullPathForFindChildren() throws Exception
    {
        String path = null;
        ps.find(path);
    }
    
    @Test(expected=PSPathNotFoundServiceException.class)
    public void shouldFailIfNoPathFound() throws Exception
    {
        ps.find("/crap/");
    }
    
    @Test(expected=PSPathNotFoundServiceException.class)
    public void shouldFailIfNoPathFoundForBadPath() throws Exception
    {
        ps.find("crap");
    }

}
