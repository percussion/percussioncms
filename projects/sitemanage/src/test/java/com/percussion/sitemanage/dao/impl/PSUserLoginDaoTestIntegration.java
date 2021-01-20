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

/**
 * 
 */
package com.percussion.sitemanage.dao.impl;

import com.percussion.user.data.PSUserLogin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author DavidBenua
 *
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TestSpringContext.xml")
public class PSUserLoginDaoTestIntegration extends AbstractTransactionalJUnit4SpringContextTests
{
    private static Log log = LogFactory.getLog(PSUserLoginDaoTestIntegration.class); 

    @Autowired
    PSUserLoginDao dao;

    @Autowired
    SessionFactory sessionFactory;

    /* Not needed anymore ?
    @BeforeTransaction
    protected void onSetUpBeforeTransaction() throws Exception
    {
        dao = (PSUserLoginDao) applicationContext.getBean("UserLoginDao");
    }
    */


    /**
     * Test method for {@link com.percussion.sitemanage.dao.impl.PSUserLoginDao#delete(java.lang.String)}.
     */
    @Test
    public void testDelete()
    {
        int count = countRows();
        assertEquals("user xyzzy already exists", 0, count); 
        addRow(); 
        
        log.info("Testing delete of user xyzzy");
        
        dao.delete("xyzzy"); 
        
        count = countRows(); 
        assertEquals("user xyzzy not deleted", 0, count); 
    }

    /**
     * Test method for {@link com.percussion.sitemanage.dao.impl.PSUserLoginDao#find(java.lang.String)}.
     */
    @Test
    public void testFind()
    {
        int count = countRows();  
        assertEquals("user xyzzy already exists", 0, count);
        addRow();
        log.info("finding xyzzy"); 
        PSUserLogin login = dao.find("xyzzy"); 
        assertNotNull(login); 
        log.info("login is " + login); 
        assertEquals("xyzzy", login.getUserid());
        assertEquals("demo", login.getPassword()); 
        
    }

    /**
     * Test method for {@link com.percussion.sitemanage.dao.impl.PSUserLoginDao#findAll()}.
     */
    @Test
    public void testFindAll()
    {
        int count = countRows(); 
        assertEquals("user xyzzy already exists", 0, count);
        addRow(); 
        log.info("finding all entries"); 
        
        List<PSUserLogin> users = dao.findAll();
        assertTrue(users.size() > 0); 
        log.info("There are " + users.size() + " user entries"); 
        
        PSUserLogin myLogin = new PSUserLogin();
        myLogin.setUserid("xyzzy");
        myLogin.setPassword("demo"); 
        
        assertTrue(users.contains(myLogin)); 
    }

    /**
     * Test method for {@link com.percussion.sitemanage.dao.impl.PSUserLoginDao#save(com.percussion.user.data.PSUserLogin)}.
     */
    @Test
    public void testSave()
    {
        int count = countRows(); 
        assertEquals("user xyzzy already exists", 0, count);
        addRow(); 
        
        log.info("testing save"); 
        PSUserLogin myLogin = new PSUserLogin();
        myLogin.setUserid("xyzzy");
        myLogin.setPassword("demo2");
        
        dao.save(myLogin); 
        
        count = countRows(); 
        assertEquals(1,count);
        
        String pw2 = jdbcTemplate.queryForObject("select password from userlogin where userid = 'xyzzy'", String.class);
        log.debug("new password is "  + pw2); 
        assertEquals("demo2", pw2); 
        
        
    }

    /**
     * Test method for {@link com.percussion.sitemanage.dao.impl.PSUserLoginDao#create(com.percussion.user.data.PSUserLogin)}.
     */
    @Test
    public void testCreate()
    {
        PSUserLogin myLogin = new PSUserLogin();
        myLogin.setUserid("xyzzy");
        myLogin.setPassword("demo");
        
        log.info("testing create"); 
        dao.create(myLogin); 
        
        int count = countRows(); 
        assertEquals(1,count); 
    }

    /**
     * @return the dao
     */
    public PSUserLoginDao getDao()
    {
        return dao;
    }

    /**
     * @param dao the dao to set
     */
    public void setDao(PSUserLoginDao dao)
    {
        this.dao = dao;
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
    
    protected int countRows()
    {
        String query ="select count(*) from userlogin where userid = 'xyzzy'";
        int count = jdbcTemplate.queryForObject(query,Integer.class);
        return count; 
    }
    
    protected void addRow()
    {
        jdbcTemplate.execute("insert into userlogin (userid,password) values ('xyzzy', 'demo')"); 
    }
}
    
    

    