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
package com.percussion.share.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderPermission.Principal;
import com.percussion.pathmanagement.data.PSFolderPermission.PrincipalType;

public class PSFolderPermissionUtilsTest
{

    /**
     * Tests {@link PSFolderPermissionUtils#getFolderPermission(PSFolder)}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetPermission() throws Exception
    {
        PSFolder folder = createFolder(PSFolderPermission.Access.ADMIN, "admin");
        
        PSFolderPermission permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertTrue(permission.getAccessLevel() == PSFolderPermission.Access.ADMIN);
        assertTrue(permission.getAdminPrincipals().size() == 1 &&
                permission.getAdminPrincipals().get(0).getName().equals("admin"));
        assertNull(permission.getReadPrincipals());
        assertNull(permission.getWritePrincipals());

        folder = createFolder(PSFolderPermission.Access.WRITE, "writer");
        
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertTrue(permission.getAccessLevel() == PSFolderPermission.Access.WRITE);
        assertTrue(permission.getWritePrincipals().size() == 1 
                && permission.getWritePrincipals().get(0).getName().equals("writer"));
        assertNull(permission.getAdminPrincipals());
        assertNull(permission.getReadPrincipals());

        folder = createFolder(PSFolderPermission.Access.READ, "reader");
        
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertTrue(permission.getAccessLevel() == PSFolderPermission.Access.READ);
        assertTrue(permission.getReadPrincipals().size() == 1 
                && permission.getReadPrincipals().get(0).getName().equals("reader"));
        assertNull(permission.getAdminPrincipals());
        assertNull(permission.getWritePrincipals());
    }
    
    /**
     * Tests {@link PSFolderPermissionUtils#setFolderPermission(PSFolder, com.percussion.pathmanagement.data.PSFolderPermission.Access)}.
     * 
     * @throws Exception
     */
    @Test
    public void testSetPermission() throws Exception
    {
        PSFolder folder = createFolder(PSFolderPermission.Access.ADMIN, "admin");
        
        // set to ADMIN
        PSFolderPermissionUtils.setFolderPermission(folder, PSFolderPermission.Access.ADMIN);
        
        // validate get ADMIN
        PSFolderPermission permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertTrue(permission.getAccessLevel() == PSFolderPermission.Access.ADMIN);
        

        // set to WRITE
        PSFolderPermissionUtils.setFolderPermission(folder, PSFolderPermission.Access.WRITE);
        
        // validate get WRITE
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertTrue(permission.getAccessLevel() == PSFolderPermission.Access.WRITE);

        // set to READ
        PSFolderPermissionUtils.setFolderPermission(folder, PSFolderPermission.Access.READ);
        
        // validate get READ
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertTrue(permission.getAccessLevel() == PSFolderPermission.Access.READ);
    }
    
    /**
     * Tests {@link PSFolderPermissionUtils#setFolderPermission(PSFolder, PSFolderPermission)}.
     * 
     * @throws Exception
     */
    @Test
    public void testSetFolderPermission() throws Exception
    {
        PSFolder folder = createFolder(PSFolderPermission.Access.ADMIN, "admin");
        
        PSFolderPermission perm = new PSFolderPermission();
        perm.setAccessLevel(PSFolderPermission.Access.ADMIN);
        Principal p = new Principal();
        p.setName("admin2");
        p.setType(PrincipalType.USER);
        List<Principal> principals = new ArrayList<Principal>();
        principals.add(p);
        perm.setAdminPrincipals(principals);
        
        // set to ADMIN with an additional ADMIN user
        PSFolderPermissionUtils.setFolderPermission(folder, perm);
        
        // validate get ADMIN with an additional ADMIN user
        PSFolderPermission permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertEquals(perm, permission);

        perm = new PSFolderPermission();
        perm.setAccessLevel(PSFolderPermission.Access.WRITE);
        p.setName("writer");
        principals.clear();
        principals.add(p);
        perm.setWritePrincipals(principals);
        
        // set to WRITE with a WRITE user
        PSFolderPermissionUtils.setFolderPermission(folder, perm);
        
        // validate get WRITE with a WRITE user
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertEquals(perm, permission);

        perm = new PSFolderPermission();
        perm.setAccessLevel(PSFolderPermission.Access.READ);
        p.setName("reader");
        principals.clear();
        principals.add(p);
        perm.setReadPrincipals(principals);
        
        // set to READ with a READ user
        PSFolderPermissionUtils.setFolderPermission(folder, perm);
        
        // validate get READ with a READ user
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertEquals(perm, permission);
        
        // set to default permission
        perm = new PSFolderPermission();
        PSFolderPermissionUtils.setFolderPermission(folder, perm);
        
        // validate get default permission
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertEquals(perm, permission);
        
        perm = new PSFolderPermission();
        perm.setAccessLevel(PSFolderPermission.Access.READ);
        List<Principal> adminPrincipals = new ArrayList<Principal>();
        List<Principal> writePrincipals = new ArrayList<Principal>();
        List<Principal> readPrincipals = new ArrayList<Principal>();
        Principal adminUser = new Principal();
        adminUser.setName("user1");
        adminUser.setType(PrincipalType.USER);
        adminPrincipals.add(adminUser);
        perm.setAdminPrincipals(adminPrincipals);
        Principal writeUser = new Principal();
        writeUser.setName("user1");
        writeUser.setType(PrincipalType.USER);
        writePrincipals.add(writeUser);
        perm.setWritePrincipals(writePrincipals);
        Principal readUser = new Principal();
        readUser.setName("user1");
        readUser.setType(PrincipalType.USER);
        readPrincipals.add(readUser);
        perm.setReadPrincipals(readPrincipals);
        
        // set to READ with one user with READ, WRITE, and ADMIN permission
        PSFolderPermissionUtils.setFolderPermission(folder, perm);
        
        // validate one ADMIN user remains
        permission = PSFolderPermissionUtils.getFolderPermission(folder);
        assertFalse(permission.equals(perm));
        assertTrue(permission.getAdminPrincipals().size() == 1 &&
                permission.getAdminPrincipals().get(0).equals(adminUser));
        assertNull(permission.getWritePrincipals());
        assertNull(permission.getReadPrincipals());        
    }
    
    /**
     * Tests {@link PSFolderPermissionUtils#getUserAcl(PSFolder, String, Collection<String)}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetUserAcl() throws Exception
    {
        PSFolder folder = createFolder(PSFolderPermission.Access.ADMIN, PSFolderPermission.Access.WRITE, "user1");
        PSFolderPermission.Access acl = PSFolderPermissionUtils.getUserAcl(folder, "user1", Collections.singletonList("")).getFirst();
        
        assertTrue("user1 should have WRITE permission", acl == PSFolderPermission.Access.WRITE);
        
        acl = PSFolderPermissionUtils.getUserAcl(folder, "user2", Collections.singletonList("Admin")).getFirst();
        assertTrue("user2 should have ADMIN permission", acl == PSFolderPermission.Access.ADMIN);
    }
    
    /**
     * Create a folder with the specified permission and user with the same permission.
     * 
     * @param permission the permission of the created folder, assumed not <code>null</code>.
     * @param user the name of the user, assumed not <code>null</code>.
     * 
     * @return the created folder, never <code>null</code>.
     */
    private PSFolder createFolder(PSFolderPermission.Access accessLevel, String user)
    {
        return createFolder(accessLevel, accessLevel, user);
    }
    
    /**
     * Create a folder with the specified permission and a user with the specified permission.
     * 
     * @param virtualAcl permission of the folder.
     * @param userAcl permission of the user.
     * @param userName the name of the user, assumed not <code>null</code>.
     * 
     * @return the created folder, never <code>null</code>.
     */
    private PSFolder createFolder(PSFolderPermission.Access virtualAccess, PSFolderPermission.Access userAccess, String userName)
    {
        PSFolder folder = new PSFolder("folder1", -1, 0, "Test folder");
        PSObjectAcl acl = new PSObjectAcl();
        
        PSFolderPermission permission = new PSFolderPermission();
        permission.setAccessLevel(virtualAccess);
        PSFolderPermission.Principal principle = new PSFolderPermission.Principal();
        principle.setType(PSFolderPermission.PrincipalType.USER);
        principle.setName(userName);
        if (userAccess == PSFolderPermission.Access.ADMIN)
            permission.setAdminPrincipals(Collections.singletonList(principle));
        else if (userAccess == PSFolderPermission.Access.WRITE)
            permission.setWritePrincipals(Collections.singletonList(principle));
        else
            permission.setReadPrincipals(Collections.singletonList(principle));
        
        PSFolderPermissionUtils.setFolderPermission(folder, permission);
        
        return folder;
    }
    
}
