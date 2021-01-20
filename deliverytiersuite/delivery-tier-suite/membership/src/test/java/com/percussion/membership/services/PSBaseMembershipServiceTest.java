/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.membership.services;

import com.percussion.delivery.multitenant.PSThreadLocalTenantContext;
import com.percussion.membership.data.PSUserSummary;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jayseletz
 *
 */
@Transactional
public abstract class PSBaseMembershipServiceTest extends TestCase
{
    @Autowired
    private IPSMembershipService membershipService;    
    
    private String TEST_TENANT_ID = "007";
    
    @Before
    public void setup() throws Exception{
        super.setUp();
    	//Setup the tenant id for the mongo tests
        PSThreadLocalTenantContext.setTenantId(TEST_TENANT_ID);
    }
    
    @After
    public void teardown() throws Exception{
        super.tearDown();
    	//Clear the tenant id for the next test script
    	PSThreadLocalTenantContext.clearTenantId();
    }
    
    @Test
    @Transactional
    public void testCreateValidAccounts() throws Exception
    {
        createAccount("validUser1@domain.com", "demo", false);
        createAccount("validUser2@domain.com", "demo", false);
        createAccount("validUser3@domain.com", "demo", false);
    }
    
    @Test
    public void testCreateAccountInvalid() throws Exception
    {
        createAccount(null, "demo", true);
        createAccount("", "demo", true);
        createAccount("testUser@domain.com", "", true);
        createAccount("testUser@domain.com", null, true);
        createAccount(null, null, true);
        createAccount(null, "", true);
        createAccount("", null, true);
        createAccount("<script>badsuff</script>", "demo", true);
    }

    @Test
    @Transactional
    public void testCreateAccountDupe() throws Exception
    {
        createAccount("testDupeUser1", "demo", false);
        createAccount("testDupeUser1", "demo", true);
    }

    @Test
    @Transactional
    public void testGetUser() throws Exception
    {
        String userId = "testGetUser1";
        String sessionId = createAccount(userId, "demo", false);
        isValidSession(userId, sessionId);
    }
    
    @Ignore // This is breaking when within the full build and it takes way too much time
    @Test
    public void testGetUserExpiredSession() throws Exception
    {
        // assumes test bean is configured for 1 minute timeout
        String userId = "testExpiredUser1";
        String sessionId = createAccount(userId, "demo", false);
        
        // check valid session
        isValidSession(userId, sessionId);
        
        // wait 30 secs, touch, then wait 35, ensure still valid 
        System.out.println("Sleeping 30 secs");
        Thread.currentThread().sleep(30 * 1000);
        isValidSession(userId, sessionId);
        System.out.println("Sleeping 35 secs");
        Thread.currentThread().sleep(35 * 1000);
        isValidSession(userId, sessionId);
        
        // wait 1 minute
        System.out.println("Sleeping 1 minute");
        Thread.currentThread().sleep(60 * 1000);
        
        // check invalid
        PSUserSummary userSum = membershipService.getUser(sessionId);
        assertTrue(userSum == null);
    }
    

    @Test
    @Transactional
    public void testGetUserInvalidSession() throws Exception
    {
        assertNull(membershipService.getUser("madeupsessionId"));
    }        

    @Test
    @Transactional
    public void testLogin() throws Exception
    {
        String userId = "testLoginUser1";
        createAccount(userId, "demo", false);
        attemptLogin("badUser", "demo", false);
        attemptLogin("", "demo", false);
        attemptLogin(null, "demo", false);
        attemptLogin("userId", "badpwd", false);
        attemptLogin("userId", "", false);
        attemptLogin("userId", null, false);
        attemptLogin(userId, "demo", true);
    }
    
    @Test
    @Transactional
    public void testLogout() throws Exception
    {
        // test non-existent session id
        membershipService.logout("abc");
        
        // create and login
        String userId = "testLogoutnUser1";
        String sessionId = createAccount(userId, "demo", false);
        
        // test valid
        assertNotNull(membershipService.getUser(sessionId));
        
        // logout
        membershipService.logout(sessionId);
        
        // test not valid
        assertNull(membershipService.getUser(sessionId));
    }
    
    @Test
    @Transactional
    public void testGetUsers() throws Exception
    {
        // test none
        List<String> expected = new ArrayList<String>();
        List<PSUserSummary> sums = membershipService.findUsers();
        assertNotNull(sums);
        assertEquals(0, membershipService.findUsers().size());
        
        // test 1
        expected.add("testGetUsers0@email.com");
        createAccount(expected.get(0), "demo", false);
        sums = membershipService.findUsers();
        assertNotNull(sums);
        assertEquals(1, sums.size());
        
        // test more
        int tot = 6;
        for (int i = 1; i < tot; i++)
        {
            String email = "testGetUsers" + i + "@email.com";
            expected.add(email);
            createAccount(email, "demo", false);
        }
        
        sums = membershipService.findUsers();
        assertNotNull(sums);
        assertEquals(tot, sums.size());
        for (int i = 0; i < sums.size(); i++)
        {
            PSUserSummary sum = sums.get(i);
            assertNotNull(sum);
            assertEquals(expected.get(i), sum.getEmail());
            assertNotNull(sum.getCreatedDate());
        }
    }
    
//    public void testResetPassword() throws Exception
//    {
//        // assumes test bean is configured for 1 minute timeout
//        String userId = "testExpiredUser1";
//        String sessionId = createAccount(userId, "demo", false);
//        
//        String resetKey = null;
//        
//        try
//        {
//            resetKey = membershipService.setResetKey(userId);
//        }
//        catch (Exception e)
//        {
//            return;
//        }
//        
//        assertNotNull(sessionId);
//        assertNotNull(resetKey);
//        assertFalse(resetKey.length() == 0);
//
//    }
    
    /**
     * Attempt to login with the given creds
     * 
     * @param userId
     * @param pwd
     * @param isValid
     */
    private void attemptLogin(String userId, String pwd, boolean isValid) throws Exception
    {
        String sessionId = null;
        try
        {
            sessionId = membershipService.login(userId, pwd);
        }
        catch (Exception e)
        {
            assertFalse(isValid);
            return;
        }
        
        assertTrue(isValid);
        assertNotNull(sessionId);
        assertFalse(sessionId.length() == 0);
        PSUserSummary userSum = membershipService.getUser(sessionId);
        assertNotNull(userSum);
        assertTrue(userId.equals(userSum.getEmail()));
        
        return;
    }

    /**
     * Attempt to create an account that may or may not be valid, and test against expected validity.
     * 
     * @param userId
     * @param password
     * @param notValid <code>true</code> if the supplied params are not expected to be valid, <code>false</code> if valid.
     * @return The resulting session id, or <code>null</code> if <code>shouldThrow</code> is <code>true</code> and and exception was thrown.
     * 
     * @throws Exception if the test fails.
     */
    private String createAccount(String userId, String password, boolean notValid) throws Exception
    {
        String sessionId = null;
        try
        {
            sessionId = membershipService.createAccount(userId, password, false, "", "customerSite");            
            
        }
        catch (Exception e)
        {
            assertTrue(notValid);
            return sessionId;
        }
        
        assertFalse(notValid);
        assertNotNull(sessionId);
        assertFalse(sessionId.length() == 0);
        
        return sessionId;
    }        
    
    /**
     * Test if the supplied session id is valid for the supplied user id
     * 
     * @param userId
     * @param sessionId
     * 
     * @throws Exception if it is not valid.
     */
    private void isValidSession(String userId, String sessionId) throws Exception
    {
        PSUserSummary userSum = membershipService.getUser(sessionId);
        assertNotNull(userSum);
        assertEquals(userId, userSum.getEmail());
    }    


}
