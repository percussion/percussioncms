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
