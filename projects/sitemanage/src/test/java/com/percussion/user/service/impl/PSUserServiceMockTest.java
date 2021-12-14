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

import com.percussion.role.service.impl.PSRoleService;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.dao.IPSUserLoginDao;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.data.PSRoleList;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;
import com.percussion.user.data.PSUserLogin;
import com.percussion.user.data.PSUserProviderType;
import com.percussion.utils.service.IPSUtilityService;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@Ignore
public class PSUserServiceMockTest
{
    private static final Logger log = LogManager.getLogger(PSUserServiceMockTest.class);
    
    Mockery context; 
    PSUserService cut; 
    IPSBackEndRoleMgr backendRoleMgr;
    IPSRoleMgr roleMgr;
    IPSUserLoginDao dao;
    IPSPasswordFilter filter;
    IPSFolderHelper folderHelper;
    IPSWorkflowService workflowService;
    IPSSecurityWs securityWs;
    IPSContentWs contentWs;
    IPSIdMapper idMapper;
    List<String> validRoles; 
    IPSNotificationService notificationService;
    IPSUtilityService utilityService;
    
    MockCurrentUserName mockUserName = new MockCurrentUserName()
    {
        
        @Override
        public String mockName()
        {
            return "dummy-admin";
        }
    };

    @Before
    public void setUp() throws Exception
    {
        context = new Mockery(); 
        backendRoleMgr = context.mock(IPSBackEndRoleMgr.class);
        roleMgr = context.mock(IPSRoleMgr.class);
        notificationService = context.mock(IPSNotificationService.class);
        dao = context.mock(IPSUserLoginDao.class); 
        filter = context.mock(IPSPasswordFilter.class); 
        folderHelper = context.mock(IPSFolderHelper.class);
        workflowService = context.mock(IPSWorkflowService.class);
        securityWs = context.mock(IPSSecurityWs.class);
        contentWs = context.mock(IPSContentWs.class);
        utilityService = context.mock(IPSUtilityService.class);
        
        cut = new TestUserService(dao,filter,backendRoleMgr, roleMgr, notificationService, workflowService, securityWs, contentWs, idMapper, utilityService); 
        
        validRoles = new ArrayList<String>(); 
        validRoles.add("a");
        validRoles.add("b");
        validRoles.add("c");
        validRoles.addAll(PSRoleService.DEFAULT_ROLES);
        
        context.checking(new Expectations(){{
            allowing(backendRoleMgr).getRhythmyxRoles();
            will(returnValue(validRoles));
        }});
    }
    
    private void expectFindByName(final String name) throws IPSGenericDao.LoadException {
        final PSUserLogin user = new PSUserLogin();
        user.setUserid(name);
        context.checking(new Expectations(){{
            one(dao).findByName(name);
            will(returnValue(asList(user)));
        }});
    }

    @Test
    public void testValid() throws IPSGenericDao.LoadException, PSValidationException {
        PSUser user = new PSUser();
        user.setName("Fred9");
        user.getRoles().add("a");

        expectFindByName("Fred9");
        cut.doValidation(user, false);
        assertTrue(true);
    }
    
    @Test(expected = PSValidationException.class)
    public void testValidBadUser() throws PSValidationException {
        PSUser user = new PSUser();
        user.setName("Fred9!Z");
        user.getRoles().add("a");
        cut.doValidation(user, false);
    }
    
    @Test
    public void testValidDirectoryUserWithBadCharactersForInternalNames() throws PSValidationException {
        PSUser user = new PSUser();
        user.setName("Fred9!Z");
        user.getRoles().add("a");
        user.setProviderType(PSUserProviderType.DIRECTORY);
        cut.doValidation(user, false);
    }
    
    @Test(expected=PSValidationException.class) 
    public void shouldFailCreatingUserWithNoRoles() throws PSValidationException {
        PSUser user = new PSUser();
        user.setName("Fred9!Z");
        assertTrue(user.getRoles().isEmpty());
        cut.doValidation(user, false);
    }

    @Test(expected=PSValidationException.class)
    public void testValidNoUser() throws PSValidationException {
        PSUser user = new PSUser();
        user.setName(null);
        user.getRoles().add("a"); 
        cut.doValidation(user, false);
    }
    

    @Test(expected=PSValidationException.class) 
    public void testValidTooLongUser() throws PSValidationException {
        PSUser user = new PSUser();
        user.setName("Fred123456789012345678901234567890123456789012345678901234567890");
        user.getRoles().add("a"); 
        cut.doValidation(user, false);        
    }
    
    @Test(expected=PSValidationException.class) 
    public void testValidBadRole() throws IPSGenericDao.LoadException, PSValidationException {
        PSUser user = new PSUser();
        user.setName("fred");
        user.getRoles().add("q");
        expectFindByName("fred");
        cut.doValidation(user, false);
    }
    
    @Test
    public void testCreate() throws PSDataServiceException {
        PSUser user = new PSUser();
        user.setName("fred");
        user.setPassword("secret");
        user.setEmail("fred@yahoo.com");
        user.getRoles().add("a"); 
        final List<String> rl = user.getRoles();
        rl.addAll(PSRoleService.DEFAULT_ROLES);
        
        context.checking(new Expectations(){{
           one(filter).encrypt("secret");
           will(returnValue("super-secret"));
           PSUserLogin rvalue = new PSUserLogin();
           one(dao).create(with(any(PSUserLogin.class)));
           will(returnValue(rvalue));
           one(backendRoleMgr).setRhythmyxRoles(with("fred"), with(1), with(hasRoles(asList("a"))));
        
           /*
            * Expect no existing users with that name
            */
           one(dao).findByName("fred");
           will(returnValue(new ArrayList<PSUserLogin>()));
           one(backendRoleMgr).setSubjectEmail(with("fred"), with("fred@yahoo.com"));
        }});
        
                
        PSUser actual = cut.create(user);
        
        assertThat(actual.getPassword(), is(nullValue()));
        
        assertEquals(actual.getEmail(), "fred@yahoo.com" );
        
        context.assertIsSatisfied(); 
    }
    
    @SuppressWarnings("unchecked")
    private static Matcher<Collection<String>> hasRoles(List<String> roles) {
        return  CombinableMatcher.<Collection<String>>both(hasItems(roles.toArray(new String[] {}))) 
        .and(hasItems(PSRoleService.DEFAULT_ROLES.toArray(new String[] {}))) ;
    }

    @Test
    public void testDelete() throws Exception
    {
        final PSUserLogin login = new PSUserLogin();
        login.setUserid("fred"); 
        context.checking(new Expectations(){{
           allowing(dao).find("fred");
           will(returnValue(login));
           one(dao).delete("fred");
           one(backendRoleMgr).setRhythmyxRoles("fred", 1, Collections.<String>emptyList());
           one(roleMgr).findUsers(asList("fred"), "Default", "backend");
           will(returnValue(Collections.<Subject>emptyList()));
        }});
        
        cut.delete("fred");
        context.assertIsSatisfied(); 
    }

    @Test(expected=PSValidationException.class)
    public void testNoDeleteSelf() throws Exception
    {   
        log.info("deleting self user");
        final PSUserLogin login = new PSUserLogin();
        context.checking(new Expectations(){{
            allowing(dao).find("dummy-admin");
            will(returnValue(login));
            one(roleMgr).findUsers(asList("dummy-admin"), "Default", "backend");
            will(returnValue(Collections.<Subject>emptyList()));
        }}); 

        cut.delete("dummy-admin");
    }
    
    @Test
    public void testFind() throws Exception
    {
        final PSUserLogin login = new PSUserLogin();
        final List<String> roles = new ArrayList<String>();
        roles.add("a"); 
        login.setUserid("fred"); 
        context.checking(new Expectations(){{
           allowing(dao).find("fred");
           will(returnValue(login));
           one(backendRoleMgr).getRhythmyxRoles("fred", 1);
           will(returnValue(roles));
           //Why are we call this twice?
           atMost(2).of(roleMgr).findUsers(asList("fred"), "Default", "backend");
           will(returnValue(Collections.<Subject>emptyList()));
        }});
        
        PSUser result = cut.find("fred");
        assertNotNull(result); 
        assertEquals("fred",result.getName()); 
        assertNull(result.getPassword()); 
        assertTrue(result.getRoles().contains("a")); 
        context.assertIsSatisfied(); 
    }
    
    @Test
    public void shouldGetCurrentUser() throws Exception
    {
        final PSUserLogin actual = new PSUserLogin();
        final List<String> roles = new ArrayList<String>();
        roles.add("a"); 
        actual.setUserid("dummy-admin");

        context.checking(new Expectations(){{
            allowing(dao).find("dummy-admin");
            will(returnValue(actual));
            one(backendRoleMgr).getRhythmyxRoles("dummy-admin", 1);
            will(returnValue(roles));
            //Why are we call this multiple times.
            atMost(2).of(roleMgr).findUsers(asList("dummy-admin"), "Default", "backend");
            will(returnValue(Collections.<Subject>emptyList()));
        }});
        
        PSCurrentUser result = cut.getCurrentUser();
        assertNotNull("Result cannot be null", result);
        assertThat(result.getName(), is("dummy-admin"));
    }
    
    @Test
    public void testGetRoles() throws PSDataServiceException {
        PSRoleList rl = cut.getRoles();
        List<String> roles = rl.getRoles(); 
        assertNotNull(roles); 
        assertEquals(3,roles.size());
        
    }

    @Test
    public void testGetUsers() throws Exception
    {       
        final List<PSUserLogin> logins = new ArrayList<PSUserLogin>();
        PSUserLogin l1 = new PSUserLogin();
        l1.setUserid("fred");
        logins.add(l1);
        l1 = new PSUserLogin();
        l1.setUserid("Bob");
        logins.add(l1);
        l1 = new PSUserLogin(); 
        l1.setUserid("alice"); 
        logins.add(l1);
        
        context.checking(new Expectations(){{
            /*
             * Come from the service not the DAO.
             */
           one(roleMgr).findUsers(null, "Default", "backend");
           will(returnValue(createMockSubject("fred","Bob","alice")));
        }});
        
        PSUserList result = cut.getUsers();
        assertNotNull(result); 
        List<String> u = result.getUsers();
        assertNotNull(u);
        assertEquals(3,u.size());
        assertEquals("alice", u.get(0)); 
        assertEquals("Bob", u.get(1)); 
        context.assertIsSatisfied();         
    }
    
    private List<Subject> createMockSubject(String ... users) {
        List<Subject> subs = new ArrayList<Subject>();
        for (String u : users) {
            Subject s = new Subject();
            s.getPrincipals().add(new PSTypedPrincipal(u, PrincipalTypes.SUBJECT));
            subs.add(s);
            s.getPublicCredentials().add(u);
        }
        return subs;
    }

    @Test
    public void testUpdate() throws PSDataServiceException {
        PSUser user = new PSUser();
        final PSUserLogin login = new PSUserLogin();
        user.setName("fred");
        user.setPassword("secret"); 
        user.setEmail("fred@yahoo.com");
        final List<String> roles = Collections.<String>emptyList(); 
        context.checking(new Expectations(){{
            allowing(dao).find("fred");
            will(returnValue(login));
            one(filter).encrypt("secret");
            will(returnValue("super-secret")); 
            one(dao).save(with(any(PSUserLogin.class)));
            one(backendRoleMgr).setRhythmyxRoles(with("fred"), with(1), with(hasRoles(roles)));
            one(backendRoleMgr).setSubjectEmail(with("fred"), with("fred@yahoo.com")); 

        }});
        
        PSUser actual = cut.update(user);
        assertThat(actual.getPassword(), is(nullValue()));
        context.assertIsSatisfied(); 
    }

    @Test
    public void testChangePassword() throws PSDataServiceException {
        PSUser user = new PSUser();
        final PSUserLogin login = new PSUserLogin();
        user.setName("fred");
        user.setPassword("secret");
        user.setEmail("fred@yahoo.com");
        context.checking(new Expectations(){{
            allowing(dao).find("fred");
            will(returnValue(login));
            one(filter).encrypt("secret");
            will(returnValue("super-secret"));
            one(dao).save(with(any(PSUserLogin.class)));
        }});

        PSUser actual = cut.changePassword(user);
        assertThat(actual.getPassword(), is(nullValue()));
        context.assertIsSatisfied();
    }

    @Test(expected=PSValidationException.class)
    public void testUpdateSelfNoAdmin() throws PSDataServiceException {
        PSUser user = new PSUser();
        user.setName("dummy-admin");
        user.setPassword("secret"); 
        final List<String> roles = new ArrayList<String>();
        roles.add("a"); 
        roles.add("Admin"); 
         
        context.checking(new Expectations(){{
            one(backendRoleMgr).getRhythmyxRoles("dummy-admin", 1);
            will(returnValue(roles));
            one(dao).find("dummy-admin");
            will(returnValue(null));
        }});
        
        cut.update(user);

        context.assertIsSatisfied(); 
    }
    
    public void testUpdateRoles()
    {
        
    }

    @Test
    public void testFindRoles()
    {
        final List<String> roles = new ArrayList<String>();
        roles.add("a");
        roles.add("c"); 
        context.checking(new Expectations(){{
            one(backendRoleMgr).getRhythmyxRoles("fred", 1);
            will(returnValue(roles));
        }});
        
        List<String> result = cut.findRoles("fred");
        assertNotNull(result);
        assertTrue(result.contains("c")); 
        
        context.assertIsSatisfied(); 
        
    }

    @Test(expected=PSValidationException.class)
    public void testCheckUser() throws PSSecurityCatalogException, PSDataServiceException {
        context.checking(new Expectations(){{
            one(dao).find("fred");
            will(returnValue(null)); 
            one(roleMgr).findUsers(asList("fred"), "Default", "backend");
            will(returnValue(Collections.<Subject>emptyList()));
        }});

        cut.checkUser("fred");

        context.assertIsSatisfied(); 
    }

    
    private class TestUserService extends PSUserService
    {
 
        
        
        private TestUserService(IPSUserLoginDao userLoginDao, IPSPasswordFilter passwordFilter,
                IPSBackEndRoleMgr backendRoleMgr, IPSRoleMgr roleMgr, IPSNotificationService notificationService,
                IPSWorkflowService workflowService, IPSSecurityWs securityWs, IPSContentWs contentWs, IPSIdMapper idMapper, IPSUtilityService utilityService)
        {
            super(userLoginDao, passwordFilter, backendRoleMgr, roleMgr, notificationService, workflowService, securityWs, contentWs,idMapper,utilityService);
        }

        @Override
        protected String getCurrentUserName()
        {
            return mockUserName.mockName();
        }
        
        public PSCurrentUser getCurrentUser()
        {
            PSCurrentUser user = new PSCurrentUser();
            user.setName("dummy-admin");
            return user;
        }
        
    }
    
    protected static abstract class MockCurrentUserName {
        public abstract String mockName();
    }
}
