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
package com.percussion.user.web.service;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import com.percussion.role.data.PSRole;
import com.percussion.role.web.service.PSRoleServiceRestClient;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.share.data.PSStringWrapper;
import com.percussion.share.test.PSObjectRestClient.DataRestClientException;
import com.percussion.share.test.PSObjectRestClient.DataValidationRestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.user.data.PSAccessLevel;
import com.percussion.user.data.PSAccessLevelRequest;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.data.PSExternalUser;
import com.percussion.user.data.PSRoleList;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;
import com.percussion.user.service.IPSUserService.PSDirectoryServiceStatus;
import com.percussion.user.service.IPSUserService.PSDirectoryServiceStatus.ServiceStatus;
import com.percussion.user.service.IPSUserService.PSImportUsers;
import com.percussion.user.data.PSImportedUser;
import com.percussion.user.data.PSImportedUser.ImportStatus;

import java.util.Collections;
import java.util.List;

import org.hamcrest.core.CombinableMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the user service through rest.
 * @author adamgent
 */

public class PSUserServiceRestTest extends PSRestTestCase<PSUserServiceRestClient>
{

    protected PSUser user;
    
    @Override
    protected PSUserServiceRestClient getRestClient(String baseUrl)
    {
        PSUserServiceRestClient c = new PSUserServiceRestClient();
        c.setUrl(baseUrl);
        return c;
    }
    
    @Before
    public void setupUser() {
        user = createUser("MyTestId", "MyPassword", asList("Admin"));
    }
    
    @After
    public void runCleaner() {
        userCleaner.clean();
    }

    @Test
    public void testCreate() throws Exception
    {
        userCleaner.add(user.getName());
        restClient.create(user);
    }

    @Test
    public void testDelete() throws Exception
    {
        testCreate();
        restClient.delete(user.getName());
        //If successful the cleaner does not need to remove the user.
        userCleaner.remove(user.getName());
    }
    
    @Test(expected=DataValidationRestClientException.class)
    public void testDeleteSystemUserShouldFail() throws Exception
    {
        restClient.delete("rxserver");
    }


    @Test
    public void testCurrent() throws Exception
    {
        PSCurrentUser user = restClient.getCurrentUser();
        assertEquals(user.getName(), "admin1");
        assertEquals(user.isAdminUser(), true);
    }
    
    @Test
    public void testFind() throws Exception
    {
        testCreate();
        PSUser actual = restClient.find(user.getName());
        assertThat(actual.getName(), is(equalTo(user.getName())));
        assertThat(actual.getRoles(), is(equalTo(user.getRoles())));

        List<String> roles = user.getRoles();
        assertTrue("Should not include \"System\" role", !roles.contains("System"));
        assertTrue("Should not include \"Default\" role", !roles.contains("Default"));
    }
    
    @Test
    public void testFindExternalOnRealLDAPServer() throws Exception
    {
        assumeDirectoryServer(ServiceStatus.ENABLED);
        /*
         * Connect to our SUN one LDAP server.
         */
        List<PSExternalUser> externalUsers = restClient.findUsersFromDirectoryService("sun%");
        assertThat(externalUsers, is(not(nullValue())));
        assertThat(externalUsers.get(0).getName(), startsWith("sun"));
    }
    
    @Test(expected=DataRestClientException.class)
    public void testFindExternalForDisabledLdap() throws Exception
    {
        assumeDirectoryServer(ServiceStatus.DISABLED);
        restClient.findUsersFromDirectoryService("sun%");
    }
    
    @Test
    public void testImportUsers() throws Exception
    {
        PSImportUsers u = new PSImportUsers();
        PSExternalUser a = new PSExternalUser();
        a.setName("a");
        PSExternalUser b = new PSExternalUser();
        b.setName("b");
        u.setExternalUsers(asList(a,b));
        
        userCleaner.add("a","b");
        
        List<PSImportedUser> importedUsers = restClient.importDirectoryUsers(u);
        assertThat(importedUsers, is(not(nullValue())));
        assertThat(importedUsers.get(0).getName(), is(equalTo("a")));
        assertThat(importedUsers.get(0).getStatus(), is(equalTo(ImportStatus.SUCCESS)));
        
        assertThat(importedUsers.get(1).getName(), is(equalTo("b")));
        assertThat(importedUsers.get(1).getStatus(), is(equalTo(ImportStatus.SUCCESS)));
        assertThat(importedUsers.size(), is(2));
        
        /*
         * Imported users should be in the contributor role.
         */
        PSUser user = restClient.find("a");
        assertThat(user.getRoles(), hasItem("Contributor"));
        
        /*
         * Import again should show duplicate for "a"
         */
        u.setExternalUsers(asList(a));
        importedUsers = restClient.importDirectoryUsers(u);
        assertThat(importedUsers.size(), is(1));
        assertThat(importedUsers.get(0).getStatus(), is(equalTo(ImportStatus.DUPLICATE)));
    }
    
    
    public void assumeDirectoryServer(ServiceStatus status) {
        PSDirectoryServiceStatus actual = restClient.checkDirectoryService();
        assumeThat(actual.getStatus(), is(status));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCheckDirectoryService() throws Exception
    {
        PSDirectoryServiceStatus status = restClient.checkDirectoryService();
        assertThat(status, not(nullValue()));
        assertThat(status.getStatus(), anyOf(is(ServiceStatus.DISABLED), is(ServiceStatus.ENABLED)));
    }

    @Test
    public void testGetRoles() throws Exception
    {
        PSRoleList roleList = restClient.getRoles();
        assertThat(roleList.getRoles(), hasItem("Admin"));

        assertTrue("Should not include \"System\" role", !roleList.getRoles().contains("System"));
        assertTrue("Should not include \"Default\" role", !roleList.getRoles().contains("Default"));
    }

    @Test
    public void testGetUsers() throws Exception
    {
        testCreate();
        PSUserList userList = restClient.getUsers();
        assertThat(userList.getUsers(), hasItem(user.getName()));
        assertThat(userList.getUsers(), not(hasItem("rxserver")));
    }
    
    @Test
    public void testGetUserNames() throws Exception
    {
        testCreate();
        PSUserList userList = restClient.getUserNames(user.getName());
        assertThat(userList.getUsers(), hasItem(user.getName()));
        assertThat(userList.getUsers(), not(hasItem("rxserver")));
        
        for (int i = 0; i < user.getName().length(); i++)
        {
            String name = user.getName().substring(0, i) + "%";
            userList = restClient.getUserNames(name);
            assertThat(userList.getUsers(), hasItem(user.getName()));
            assertThat(userList.getUsers(), not(hasItem("rxserver")));
        }
        
        PSUser aUser = createUser("a" + user.getName(), "demo", asList("Admin"));
        userCleaner.add(aUser.getName());
        restClient.create(aUser);
        PSUser userz = createUser(user.getName() + "z", "demo", asList("Admin"));
        userCleaner.add(userz.getName());
        restClient.create(userz);
        
        userList = restClient.getUserNames(user.getName() + "%");
        assertThat(userList.getUsers(), hasItem(user.getName()));
        assertThat(userList.getUsers(), hasItem(userz.getName()));
        assertThat(userList.getUsers(), not(hasItem(aUser.getName())));
        assertThat(userList.getUsers(), not(hasItem("rxserver")));
        
        userList = restClient.getUserNames("%" + user.getName());
        assertThat(userList.getUsers(), hasItem(user.getName()));
        assertThat(userList.getUsers(), not(hasItem(userz.getName())));
        assertThat(userList.getUsers(), hasItem(aUser.getName()));
        assertThat(userList.getUsers(), not(hasItem("rxserver")));
        
        userList = restClient.getUserNames("%" + user.getName() + "%");
        assertThat(userList.getUsers(), hasItem(user.getName()));
        assertThat(userList.getUsers(), hasItem(userz.getName()));
        assertThat(userList.getUsers(), hasItem(aUser.getName()));
        assertThat(userList.getUsers(), not(hasItem("rxserver")));
    }
    
    @Test
    public void testGetUsersByRole() throws Exception
    {
        PSUserList userList = restClient.getUsersByRole("Admin");
        int numAdminUsers = userList.getUsers().size();
        for (String name : userList.getUsers())
        {
            PSUser u = restClient.find(name);
            assertTrue(u.getRoles().contains("Admin"));
        }
        
        testCreate();
        
        userList = restClient.getUsersByRole("Admin");
        assertEquals(numAdminUsers + 1, userList.getUsers().size());
        assertThat(userList.getUsers(), hasItem(user.getName()));
          
        // test role with no users
        assertTrue(restClient.getUsersByRole("foo").getUsers().isEmpty());
    }

    @Test
    public void testUpdate() throws Exception
    {
        testCreate();
        user.setRoles(asList("Contributor"));
        PSUser actual = restClient.update(user);
        assertThat(actual.getRoles(), CombinableMatcher.<Iterable<String>>both(hasItem("Contributor")).and(not(hasItem("Admin"))));
    }

    @Test
    public void testChangePassword() throws Exception {
        String emailAddress = "fbar@gmail.com";
        String newPassword = "foobar";
        String origPassword = user.getPassword();
        user.setEmail(emailAddress);

        // create user
        testCreate();

        // login as user
        restClient.login(user.getName(), user.getPassword());

        // update password
        user.setPassword(newPassword);
        PSUser result = restClient.changePassword(user);

        assertNotNull(result);

        // make sure email address is ok
        PSUser current = restClient.getCurrentUser();
        assertNotNull(current.getEmail());
        assertEquals(emailAddress, current.getEmail());

        restClient.login("Contributor", "demo");

        try {
        	result = restClient.changePassword(user);
        	fail("Expected error trying to change password");
        } catch (Exception e) {
        	assertEquals(DataValidationRestClientException.class.getName(), e.getClass().getName());
        } finally {
        	//testDelete();
        	restClient.login("admin1", "demo");
        }
    }
    
    
    @Test
    public void testGetCurrentUser() throws Exception
    {
        PSUser actual = restClient.getCurrentUser();
        assertThat(actual, is(notNullValue()));
    }

    @Test    
    public void testCannotRemoveOwnAdminRole() throws Exception
    {
        PSCurrentUser actual = restClient.getCurrentUser();
        assertThat(actual.getRoles(), hasItem("Admin"));
        
        PSUser uesr = createUser(actual);
        uesr.getRoles().remove("Admin");
        assertThat(uesr.getRoles(), not(hasItem("Admin")));
        
        try
        {
            restClient.update(uesr);
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().indexOf("cannot.remove.own.admin.role") > 0);
        }
    }

    private PSUser createUser(PSCurrentUser currUser)
    {
        PSUser user = new PSUser();
        user.setName(currUser.getName());
        user.setPassword(currUser.getPassword());
        user.setEmail(currUser.getEmail());
        user.setProviderType(currUser.getProviderType());
        user.setRoles(currUser.getRoles());
        return user;
    }
    
    /**
     * Add and remove Admin role for different user, not for current user.
     * 
     * @throws Exception if error occurs.
     */
    @Test    
    public void testAddRemoveAdminRole() throws Exception
    {
        testCreate();
        user.setRoles(asList("Admin", "Contributor"));
        PSUser actual = restClient.update(user);
        assertThat(actual.getRoles(), CombinableMatcher.<Iterable<String>>both(hasItem("Contributor")).and(hasItem("Admin")));

        actual.setRoles(asList("Contributor"));
        actual = restClient.update(actual);
        
        assertThat(actual.getRoles(), hasItem("Contributor"));
        assertThat(actual.getRoles(), not(hasItem("Admin")));
    }
    
    @Test
    public void testGetAccessLevel() throws Exception
    {
        PSUser newUser = null;
        PSRole newRole = null;
                        
        try
        {
            PSAccessLevelRequest req = new PSAccessLevelRequest();
            req.setType("percImageAutoList");

            restClient.login("Admin", "demo");
            req.setParentFolderPath("/Assets");

            PSAccessLevel accessLevel = restClient.getAccessLevel(req);
            assertEquals(PSAssignmentTypeEnum.ADMIN.name(), accessLevel.getAccessLevel());

            restClient.login("Editor", "demo");

            accessLevel = restClient.getAccessLevel(req);
            assertEquals(PSAssignmentTypeEnum.ASSIGNEE.name(), accessLevel.getAccessLevel());

            restClient.login("Contributor", "demo");

            accessLevel = restClient.getAccessLevel(req);
            assertEquals(PSAssignmentTypeEnum.ASSIGNEE.name(), accessLevel.getAccessLevel());

            restClient.login("Admin", "demo");

            // test with new user in new role
            PSRole r = new PSRole();
            r.setName("newRole");
            newRole = getRoleServiceRestClient().create(r);
            
            PSUser u = new PSUser();
            u.setName("newUser");
            u.setPassword("demo");
            u.setRoles(Collections.singletonList("newRole"));
            newUser = getUserServiceRestClient().create(u);
            
            restClient.login("newUser", "demo");
            
            accessLevel = restClient.getAccessLevel(req);
            assertEquals(PSAssignmentTypeEnum.READER.name(), accessLevel.getAccessLevel());
        }
        finally
        {
            if (newRole != null)
            {
                PSStringWrapper strWrapper = new PSStringWrapper();
                strWrapper.setValue(newRole.getName());
                
                getRoleServiceRestClient().delete(strWrapper);
            }
            
            if (newUser != null)
            {
                getUserServiceRestClient().delete(newUser.getName());
            }
        }
    }
    
    private PSTestDataCleaner<String> userCleaner = new PSTestDataCleaner<String>() {

        @Override
        protected void clean(String id) throws Exception
        {
            restClient.delete(id);
        }
        
    };
    
    private PSUser createUser(String name, String password, List<String> roles)
    {
        PSUser newUser = new PSUser();
        newUser.setName(name);
        newUser.setPassword(password);
        newUser.setRoles(roles);
        
        return newUser;
    }
    
    private PSUserServiceRestClient getUserServiceRestClient() throws Exception
    {
        PSUserServiceRestClient c = new PSUserServiceRestClient();
        c.setUrl(baseUrl);
        setupClient(c);
        return c;
    }
    
    private static PSRoleServiceRestClient getRoleServiceRestClient() throws Exception
    {
        PSRoleServiceRestClient client = new PSRoleServiceRestClient();
        client.setUrl(baseUrl);
        setupClient(client);
        return client;
    }
    
}

