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

package com.percussion.user.service.impl;

import com.percussion.role.service.IPSRoleService;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.data.PSUser;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.service.IPSUtilityService;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;

import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

/**
 * Test PSUserService API within server runtime environment.
 * 
 * @author YuBingChen
 *
 */
@Category(IntegrationTest.class)
public class PSUserServiceTest extends PSServletTestCase
{
    private IPSSecurityWs securityWs;
    private IPSUserService userService;
    private IPSSystemProperties systemProps;
    private IPSUtilityService utilityService;
    private PSMockSystemProps mockProps = new PSMockSystemProps();
    
    public void setSecurityWs(IPSSecurityWs security)
    {
        securityWs = security;
    }
    
    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }
    
    public void setUtilityService(IPSUtilityService utilityService)
    {
        this.utilityService = utilityService;
    }
    
    public IPSUtilityService getUtilityService()
    {
        return this.utilityService;
    }
    
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        systemProps = ((PSUserService)userService).getSystemProps();
        setMockProperties(null);
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
    
    @Override
    public void tearDown() throws Exception
    {
        ((PSUserService)userService).setSystemProps(systemProps);        
    }
    
    public void setUserService(IPSUserService service)
    {
        userService = service;
    }
    
    public IPSUserService getUserService()
    {
        return userService;
    }

    private void setMockProperties(String roles)
    {
        ((PSUserService)userService).setSystemProps(mockProps); 
        if (StringUtils.isNotBlank(roles))
            mockProps.setAccessibilityRoles(roles);
    }
    
    public void testEditorUser() throws Exception
    {
        securityWs.login("Editor", "demo", null, null);
        PSCurrentUser user = userService.getCurrentUser();
        
        assertFalse(user.isAccessibilityUser());
        assertFalse(user.isAdminUser());
        
        setMockProperties("Editor");
        user = userService.getCurrentUser();
        assertTrue(user.isAccessibilityUser());        
        assertFalse(user.isAdminUser());
    }
    
    public void testPercussionAdmin() throws Exception
    {
        PSUser percussionAdmin = null;
        try{
            percussionAdmin = userService.find("PercussionAdmin");
        }
        catch(Exception e){
            //User service throws an exception if there is no user with a supplied name
            //we catch the exception here and do nothing but the assertion will take care of 
            //both cases. 
        }
        
        if(utilityService.isSaaSEnvironment()) {
            assertNotNull(percussionAdmin);
        } else {
            assertNull(percussionAdmin);
        }
    }
    
    public void testAdminUser() throws Exception
    {
        securityWs.login("Admin", "demo", null, null);
        PSCurrentUser user = userService.getCurrentUser();
        
        assertFalse(user.isAccessibilityUser());
        assertTrue(user.isAdminUser());
        
        setMockProperties("Editor,Admin");
        user = userService.getCurrentUser();
        assertTrue(user.isAccessibilityUser());        
        assertTrue(user.isAdminUser());
    }
    
    public void testDesignUser() throws Exception
    {
        String name = "UserServiceTestDesigner";
        String pwd = "demo";
        
        securityWs.login("Admin", "demo", null, null);
        
        // ensure user does not yet exist
        try
        {
            userService.delete(name);
        }
        catch (Exception e)
        {
           // ignore, user does not exist
        }
        
        PSUser designUser = new PSUser();
        designUser.setName(name);
        designUser.setPassword(pwd);
        designUser.setRoles(Collections.singletonList(IPSRoleService.DESIGNER_ROLE));
        
        //FB: DLS_DEAD_LOCAL_STORE NC 1-17-16
        designUser = userService.create(designUser);
        assertTrue(designUser != null);
        try
        {
            assertTrue(userService.isDesignUser(name));
            assertFalse(userService.isDesignUser("Admin"));
            assertFalse(userService.isDesignUser("Editor"));
            
            securityWs.login(name, pwd, null, null);
            PSCurrentUser user = userService.getCurrentUser();
            
            assertFalse(user.isAdminUser());
            assertTrue(user.isDesignerUser());            
        }
        finally
        {
            securityWs.login("Admin", "demo", null, null);
            userService.delete(name);
        }
       
    }
    
    private class PSMockSystemProps extends Properties implements IPSSystemProperties
    {
        private static final long serialVersionUID = 1L;

        public void setAccessibilityRoles(String roles)
        {
            setProperty(ACCESSIBILITY_ROLES, roles);
        }
    }
    
}
