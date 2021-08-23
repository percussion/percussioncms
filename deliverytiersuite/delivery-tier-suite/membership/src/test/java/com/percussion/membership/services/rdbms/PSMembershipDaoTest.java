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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.membership.services.rdbms;

import com.percussion.membership.data.IPSMembership;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;
import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.rdbms.impl.PSMembership;
import com.percussion.membership.services.IPSMembershipDao;
import com.percussion.membership.services.PSMemberExistsException;
import junit.framework.TestCase;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jayseletz
 *
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSMembershipDaoTest extends TestCase
{
    
    @Autowired
    private IPSMembershipDao membershipDao;

    @Autowired
    private SessionFactory sessionFactory;


    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        Session session = getSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSMembership> deleteQuery = builder.createCriteriaDelete(PSMembership.class);
            Root<PSMembership> root = deleteQuery.from(PSMembership.class);
            session.createQuery(deleteQuery).executeUpdate();
        }finally {
          //  session.close();
        }
    }

    private Session getSession(){

        return sessionFactory.getCurrentSession();

    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    @Test
    public void testMembership() throws Exception
    {
        IPSMembership membership = new PSMembership();
        membership.setUserId("testuser@foo.com");
        membership.setEmailAddress("testuser");
        membership.setLastAccessed(new Date());
        membership.setSessionId("123abc456xyz");
        membership.setPwdResetKey("aasfjl2424u9asfjl");
        membership.setCreatedDate(new Date());
        
        IPSMembership other = new PSMembership(membership);
        assertEquals(other, membership);
    }
    
    @Test
    public void testCreateAndFind() throws Exception
    {
        IPSMembership member = membershipDao.createMember("testUser", "demo", PSMemberStatus.Active);
        member.setEmailAddress("myEmail@domain.com");
        member.setSessionId("ABC123@mysession1d");
        member.setCreatedDate(new Date());
        member.setStatus(IPSMembership.PSMemberStatus.Active);
        membershipDao.saveMember(member);

        IPSMembership found = membershipDao.findMemberBySessionId(member.getSessionId());
        assertNotNull(member);
        assertEquals(member, found);

        found = membershipDao.findMemberByUserId(member.getUserId());
        assertNotNull(member);
        assertEquals(member, found);

        member.setPwdResetKey("passwordresetkey001");
        membershipDao.saveMember(member);

        found = membershipDao.findMemberByPwdResetKey(member.getPwdResetKey());
        assertNotNull(member);
        assertEquals(member, found);

        found = membershipDao.findMemberBySessionId("badId");
        assertNull(found);

        found = membershipDao.findMemberByUserId("badId");
        assertNull(found);

        found = membershipDao.findMemberByPwdResetKey("badId");
        assertNull(found);
    }
    
    @Test
    public void testCreateDupe() throws Exception
    {
        String userId = "testUserDupe";
        IPSMembership member = membershipDao.createMember(userId, "demo", PSMemberStatus.Active);
        member.setEmailAddress("myEmail@domain.com");
        member.setSessionId("ABC123@mysession1d");
        member.setCreatedDate(new Date());
        member.setStatus(IPSMembership.PSMemberStatus.Active);
        membershipDao.saveMember(member);
        
        // create dupe user
        boolean didThrow = false;
        try
        {
            member = membershipDao.createMember(userId, "demo", PSMemberStatus.Active);
        }
        catch (PSMemberExistsException e)
        {
            didThrow = true;
        }

        assertTrue(didThrow);

        // check case-insensitive
        didThrow = false;
        try
        {
            member = membershipDao.createMember(userId.toUpperCase(), "demo", PSMemberStatus.Active);

        }
        catch (PSMemberExistsException e)
        {
            didThrow = true;
        }

        assertTrue(didThrow);
        
        // create non-dupe, change to dupe and try to save
        member = membershipDao.createMember("notDupeUser", "demo", PSMemberStatus.Active);
        member.setUserId(userId);
        
        didThrow = false;
        try
        {
            membershipDao.saveMember(member);
        }
        catch (PSMemberExistsException e)
        {
            didThrow = true;
        }

        assertTrue(didThrow);

    }
    
    @Test
    public void testFindUsers() throws Exception
    {
        List<IPSMembership> members = new ArrayList<IPSMembership>();
        List<IPSMembership> found = membershipDao.findMembers();
        assertEquals(members, found);
        
        members.add(membershipDao.createMember("findUsers1@email.com", "demo", PSMemberStatus.Active));
        membershipDao.saveMember(members.get(members.size() - 1));
        found = membershipDao.findMembers();
        assertEquals(members, found);
        
        members.add(membershipDao.createMember("findUsers2@email.com", "demo", PSMemberStatus.Active));
        membershipDao.saveMember(members.get(members.size() - 1));
        members.add(membershipDao.createMember("findUsers3@email.com", "demo", PSMemberStatus.Active));
        membershipDao.saveMember(members.get(members.size() - 1));
        members.add(membershipDao.createMember("findUsers4@email.com", "demo", PSMemberStatus.Active));
        membershipDao.saveMember(members.get(members.size() - 1));
        found = membershipDao.findMembers();
        assertEquals(members, found);
    }
    
    @Test
    public void testChangeStatusAccount() throws Exception
    {
        String userId = "testChangeStatusAccount@" + Math.random() + ".com";
        IPSMembership member = membershipDao.createMember(userId, "demo", PSMemberStatus.Active);
        member.setEmailAddress(userId);
        member.setSessionId("123321@mysession1d");
        member.setCreatedDate(new Date());
        member.setStatus(IPSMembership.PSMemberStatus.Active);
        membershipDao.saveMember(member);
        
        IPSMembership found = membershipDao.findMemberByUserId(member.getUserId());
        assertNotNull(found);
        assertEquals(member, found);

        // Changes the status
        PSAccountSummary account = new PSAccountSummary();
        account.setEmail(userId);
        account.setAction("Block");
        membershipDao.changeStatusAccount(account);
        
        member.setStatus(IPSMembership.PSMemberStatus.Blocked);
        
        found = membershipDao.findMemberByUserId(member.getUserId());
        assertNotNull(found);
        assertEquals(member, found);
        
        // Cleans the added account
        membershipDao.deleteAccount(userId);
    }
    
    @Test
    public void testDeleteAccount() throws Exception
    {
        String userId = "testDeleteAccount@" + Math.random() + ".com";
        IPSMembership member = membershipDao.createMember(userId, "demo", PSMemberStatus.Active);
        member.setEmailAddress(userId);
        member.setSessionId("123321@mysession1d");
        member.setCreatedDate(new Date());
        member.setStatus(IPSMembership.PSMemberStatus.Active);
        membershipDao.saveMember(member);
        
        IPSMembership found = membershipDao.findMemberByUserId(member.getUserId());
        assertNotNull(found);
        assertEquals(member, found);

        // Deletes the account
        membershipDao.deleteAccount(userId);
        
        found = membershipDao.findMemberByUserId(member.getUserId());
        assertNull(found);
    }
}
