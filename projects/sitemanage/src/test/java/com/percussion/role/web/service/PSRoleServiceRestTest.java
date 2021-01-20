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
package com.percussion.role.web.service;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.role.data.PSRole;
import com.percussion.role.service.IPSRoleService;
import com.percussion.role.service.impl.PSRoleService;
import com.percussion.share.data.PSStringWrapper;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;
import com.percussion.user.web.service.PSUserServiceRestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.core.CombinableMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the role service through rest.
 */

public class PSRoleServiceRestTest extends PSRestTestCase<PSRoleServiceRestClient>
{

    protected PSRole role;
    
    @Override
    protected PSRoleServiceRestClient getRestClient(String baseUrl)
    {
        PSRoleServiceRestClient c = new PSRoleServiceRestClient();
        c.setUrl(baseUrl);
        return c;
    }
    
    private PSUserServiceRestClient getUserServiceRestClient() throws Exception
    {
        PSUserServiceRestClient c = new PSUserServiceRestClient();
        c.setUrl(baseUrl);
        setupClient(c);
        return c;
    }
    
    @Before
    public void setupRole() {
        role = createRole("MyTestRole", "MyDescription", "Dashboard");
    }
    
    @After
    public void runCleaner() {
        roleCleaner.clean();
    }

    @Test
    public void testCreate() throws Exception
    {
        roleCleaner.add(role.getName());
        restClient.create(role);
        
        PSStringWrapper strWrapper = new PSStringWrapper();
        strWrapper.setValue(role.getName());
        assertEquals(role, restClient.find(strWrapper));
        
        PSRole roleWithUser = createRole("MyTestRoleWithUser", "Role with user", "Dashboard");
        roleWithUser.getUsers().add("Admin");
        roleCleaner.add(roleWithUser.getName());
        restClient.create(roleWithUser);
        
        strWrapper.setValue(roleWithUser.getName());
        assertEquals(roleWithUser, restClient.find(strWrapper));
    }

    @Test
    public void testDelete() throws Exception
    {
        testCreate();
        
        //Add Contributor user to role
        PSStringWrapper strWrapper = new PSStringWrapper();
        strWrapper.setValue(role.getName());
        PSRole existingRole = restClient.find(strWrapper);
        existingRole.getUsers().add("Contributor");
        restClient.update(existingRole);
        
        restClient.delete(strWrapper);
        //If successful the cleaner does not need to remove the user.
        roleCleaner.remove(role.getName());
        
        try
        {
            restClient.find(strWrapper);
            fail("Role " + role.getName() + " should not exist");
        }
        catch (Exception e)
        {
            // expected
        }
        
        //Make sure Contributor user is no longer in role
        PSUser user = getUserServiceRestClient().find("Contributor");
        assertFalse(user.getRoles().contains(role.getName()));
    }
    
    @Test
    public void testDeleteSystemRolesShouldFail() throws Exception
    {
        PSStringWrapper strWrapper = new PSStringWrapper();
        
        for (String sysRole : PSRoleService.SYSTEM_ROLES)
        {
            try
            {
                strWrapper.setValue(sysRole);
                restClient.delete(strWrapper);
                fail("Should not be able to delete role '" + sysRole + "'");
            }
            catch (Exception e)
            {
                // expected
            }
        }
    }

    @Test
    public void testFind() throws Exception
    {
        testCreate();
        PSStringWrapper strWrapper = new PSStringWrapper();
        strWrapper.setValue(role.getName());
        PSRole actual = restClient.find(strWrapper);
        assertThat(actual.getName(), is(equalTo(role.getName())));
        assertThat(actual.getDescription(), is(equalTo(role.getDescription())));
    }
    
    @Test    
    public void testCannotRemoveSelfFromAdminRole() throws Exception
    {
        PSUser actual = getUserServiceRestClient().getCurrentUser();
        assertThat(actual.getRoles(), hasItem(IPSRoleService.ADMINISTRATOR_ROLE));
        
        PSStringWrapper strWrapper = new PSStringWrapper();
        strWrapper.setValue(IPSRoleService.ADMINISTRATOR_ROLE);
        
        PSRole adminRole = restClient.find(strWrapper); 
        adminRole.getUsers().remove(actual.getName());
        assertThat(adminRole.getUsers(), not(hasItem(actual.getName())));
       
        try
        {
            restClient.update(adminRole);
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().indexOf("cannot.remove.user.admin.role") > 0);
        }
    }

    /**
     * Add and remove Admin role for different user, not for current user.
     * 
     * @throws Exception if error occurs.
     */
    @Test    
    public void testAddRemoveUserFromAdminRole() throws Exception
    {
        testCreate();
        role.setUsers(asList("Admin", "Contributor"));
        PSRole actual = restClient.update(role);
        assertThat(actual.getUsers(), CombinableMatcher.<Iterable<String>>both(hasItem("Contributor")).and(hasItem("Admin")));

        actual.setUsers(asList("Contributor"));
        actual = restClient.update(actual);
        
        assertThat(actual.getUsers(), hasItem("Contributor"));
        assertThat(actual.getUsers(), not(hasItem("Admin")));
    }
    
    @Test
    public void testGetAvailableUsers() throws Exception
    {
        testCreate();
        
        PSUserList roleUsers = getUserServiceRestClient().getUsersByRole(role.getName());
        assertTrue(roleUsers.getUsers().isEmpty());
        
        PSUserList users = getUserServiceRestClient().getUsers();
        assertTrue(users.getUsers().contains("Editor"));
        assertTrue(users.getUsers().contains("Contributor"));
        assertEquals(users, restClient.getAvailableUsers(role));
        
        role.setUsers(asList("Editor", "Contributor"));
        restClient.update(role);
        
        PSUserList availableUsers = restClient.getAvailableUsers(role);
        assertEquals(users.getUsers().size() - 2, availableUsers.getUsers().size());
        assertFalse(availableUsers.getUsers().contains("Editor"));
        assertFalse(availableUsers.getUsers().contains("Contributor"));
    }
    
    @Test
    public void testUpdateUsers() throws Exception
    {
        testCreate();
        
        PSUserList roleUsers = getUserServiceRestClient().getUsersByRole(role.getName());
        assertTrue(roleUsers.getUsers().isEmpty());
 
        PSUser editor = getUserServiceRestClient().find("Editor");
        assertFalse(editor.getRoles().contains(role.getName()));
        
        PSUser contributor = getUserServiceRestClient().find("Contributor");
        assertFalse(contributor.getRoles().contains(role.getName()));
        
        role.setUsers(asList("Editor", "Contributor"));
        PSRole roleWithUsers = restClient.update(role);
        
        assertEquals(2, roleWithUsers.getUsers().size());
        assertTrue(roleWithUsers.getUsers().contains("Editor"));
        assertTrue(roleWithUsers.getUsers().contains("Contributor"));
        
        editor = getUserServiceRestClient().find("Editor");
        assertTrue(editor.getRoles().contains(role.getName()));
        
        contributor = getUserServiceRestClient().find("Contributor");
        assertTrue(contributor.getRoles().contains(role.getName()));
        
        role.setUsers(new ArrayList<String>());
        PSRole roleNoUsers = restClient.update(role);
        
        assertTrue(roleNoUsers.getUsers().isEmpty());
        
        editor = getUserServiceRestClient().find("Editor");
        assertFalse(editor.getRoles().contains(role.getName()));
        
        contributor = getUserServiceRestClient().find("Contributor");
        assertFalse(contributor.getRoles().contains(role.getName()));
    }
    
    @Test
    public void testRoleHomepage() throws Exception
    {
    	//Make sure Admin homepage is Dashboard
    	PSStringWrapper temp = new PSStringWrapper();
    	temp.setValue("Admin");
    	PSRole adminRole = restClient.find(temp);
    	assertEquals(adminRole.getHomepage(), IPSRoleService.HOMEPAGE_TYPE_DASHBOARD);
    	//Update the homepage
    	adminRole.setHomepage(IPSRoleService.HOMEPAGE_TYPE_EDITOR);
    	restClient.update(adminRole);
    	adminRole = restClient.find(temp);
    	assertEquals(adminRole.getHomepage(), IPSRoleService.HOMEPAGE_TYPE_EDITOR);  
    	//Reset the homepage for Admin
    	adminRole.setHomepage(IPSRoleService.HOMEPAGE_TYPE_DASHBOARD);
    	restClient.update(adminRole);

    	//Test a new role
    	PSRole role = createRole("HomepageRole","To test home page",IPSRoleService.HOMEPAGE_TYPE_EDITOR);
        roleCleaner.add(role.getName());
        restClient.create(role);
    	temp.setValue(role.getName());
    	PSRole testRole = restClient.find(temp);
    	assertEquals(testRole.getHomepage(), IPSRoleService.HOMEPAGE_TYPE_EDITOR);
        
        //Test a new role with home as homepage
        PSRole role1 = createRole("HomepageRole1","To test home page",IPSRoleService.HOMEPAGE_TYPE_HOME);
        roleCleaner.add(role1.getName());
        restClient.create(role1);
        temp.setValue(role1.getName());
        PSRole testRole1 = restClient.find(temp);
        assertEquals(testRole1.getHomepage(), IPSRoleService.HOMEPAGE_TYPE_HOME);

        //Test invalid homepage case
    	role.setHomepage("InvalidHomePage");
    	restClient.update(role);
    	temp.setValue(role.getName());
    	testRole = restClient.find(temp);
    	assertEquals(testRole.getHomepage(), IPSRoleService.HOMEPAGE_TYPE_DASHBOARD);  
        
    }
    
    @Test
    public void testUpdate()throws Exception
    {
        String intialDescription = "Role with description";
        
        String changedDescription = "Role description is changed";
            
        PSRole roleWithDescription = createRole("MyTestRole", intialDescription);
        
        
        roleCleaner.add(roleWithDescription.getName());
        PSRole created = restClient.create(roleWithDescription);
        
        assertEquals(created.getDescription(), intialDescription);
        
        roleWithDescription.setDescription(changedDescription);
        
        PSRole role = restClient.update(roleWithDescription);
        
        assertEquals(role.getDescription(), changedDescription);
        
        assertTrue(!role.getDescription().equals(intialDescription));
    }
    
    @Test
    public void testValidateForDelete() throws Exception
    {
        PSUser user = null;
        try
        {
            testCreate();

            PSStringWrapper strWrapper = new PSStringWrapper();
            strWrapper.setValue(role.getName());
            
            try
            {
                restClient.validateForDelete(restClient.find(strWrapper));
            }
            catch (Exception e)
            {
                fail("Unexpected warning");
            }

            //Create a new user and add it to the role
            user = new PSUser();
            user.setName("MyTestUser");
            user.setRoles(Collections.singletonList(role.getName()));
            user = getUserServiceRestClient().create(user);

            try
            {
                restClient.validateForDelete(restClient.find(strWrapper));
            }
            catch (Exception e)
            {
                fail("Unexpected warning");
            }
            
            user.getRoles().add("Contributor");
            user = getUserServiceRestClient().update(user);
            
            try
            {
                restClient.validateForDelete(restClient.find(strWrapper));
            }
            catch (Exception e)
            {
                fail("Unexpected warning");              
            }
            
            PSRole contributor = new PSRole();
            contributor.setName("Contributor");
            
            PSStringWrapper contribWrapper = new PSStringWrapper();
            contribWrapper.setValue(contributor.getName());
                        
            try
            {
                restClient.validateForDelete(restClient.find(contribWrapper));
            }
            catch (Exception e)
            {
                // warning expected for workflow, not user
                assertTrue(e.getMessage().contains("Default Workflow"));
                assertFalse(e.getMessage().contains(user.getName()));
            }
            
            user.getRoles().remove(role.getName());
            user = getUserServiceRestClient().update(user);
                                   
            try
            {
                restClient.validateForDelete(restClient.find(contribWrapper));
            }
            catch (Exception e)
            {
                // warning not expected for user, expected for workflow
                assertFalse(e.getMessage().contains(user.getName()));
                assertTrue(e.getMessage().contains("Default Workflow"));
            }
        }
        finally
        {
            if (user != null)
            {
                getUserServiceRestClient().delete(user.getName());
            }
        }
    }
    
    @Test
    public void testValidateDeleteUsersFromRole() throws Exception
    {
        List<String> userNames = new ArrayList<String>();
        try
        {
            PSUser user1 = new PSUser();
            user1.setName("MyTestUser1");
            user1.setRoles(Collections.singletonList("Editor"));
            user1 = getUserServiceRestClient().create(user1);
            userNames.add(user1.getName());
            
            PSUser user2 = new PSUser();
            user2.setName("MyTestUser2");
            user2.setRoles(Collections.singletonList("Editor"));
            user2 = getUserServiceRestClient().create(user2);
            userNames.add(user2.getName());
                        
            PSUser user3 = new PSUser();
            user3.setName("MyTestUser3");
            user3.setRoles(asList("Editor", "Contributor"));
            user3 = getUserServiceRestClient().create(user3);
            userNames.add(user3.getName());
            
            PSUserList userList = new PSUserList();
            userList.setUsers(Collections.singletonList(user3.getName()));
            
            try
            {
                restClient.validateDeleteUsersFromRole(userList);
            }
            catch (Exception e)
            {
                fail("Unexpected warning");
            }
            
            userList.setUsers(userNames);
            
            try
            {
                restClient.validateDeleteUsersFromRole(userList);
            }
            catch (Exception e)
            {
                fail("Unexpected warning");
            }
        }
        finally
        {
            for (String userName : userNames)
            {
                getUserServiceRestClient().delete(userName);
            }
        }
        
        
    }
    
    private PSTestDataCleaner<String> roleCleaner = new PSTestDataCleaner<String>() {

        @Override
        protected void clean(String id) throws Exception
        {
            PSStringWrapper strWrapper = new PSStringWrapper();
            strWrapper.setValue(id);
            restClient.delete(strWrapper);
        }
        
    };
    
    private PSRole createRole(String name, String description)
    {
        return createRole(name, description, null);
    }
    private PSRole createRole(String name, String description, String homepage)
    {
        PSRole newRole = new PSRole();
        newRole.setName(name);
        newRole.setDescription(description);
        newRole.setHomepage(homepage);    
        return newRole;
    }
     
}

