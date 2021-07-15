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
package com.percussion.generickey.utils.services.rdbms;

import com.percussion.generickey.data.IPSGenericKey;
import com.percussion.generickey.services.IPSGenericKeyDao;
import com.percussion.generickey.utils.data.rdbms.impl.PSGenericKey;
import junit.framework.TestCase;
import org.apache.commons.lang.time.DateUtils;
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
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * @author leonardohildt
 * 
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSGenericKeyDaoTest extends TestCase
{

    @Autowired
    private IPSGenericKeyDao genericKeyDao;

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        Session session = getSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSGenericKey> deleteQuery = builder.createCriteriaDelete(PSGenericKey.class);
            Root<PSGenericKey> root = deleteQuery.from(PSGenericKey.class);
            session.createQuery(deleteQuery).executeUpdate();
        }finally {
            //session.close();
        }

    }


    private Session getSession(){

        return sessionFactory.getCurrentSession();

    }

    @Override
    @After
    public void tearDown()
    {

    }

    
    @Test
    public void testCreateResetKeyAndValidate() throws Exception
    {       
        // get a calendar instance
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        currentDate = DateUtils.addMilliseconds(currentDate, (int) DAY_IN_MILLISECONDS);

        // create the generic key
        IPSGenericKey genericKey = new PSGenericKey();
        genericKey.setExpirationDate(currentDate);
        genericKey.setGenericKey(UUID.randomUUID().toString());
        
        genericKeyDao.saveKey(genericKey);
        
        IPSGenericKey foundGenericKey = genericKeyDao.findByResetKey(genericKey.getGenericKey());
        
        assertNotNull(foundGenericKey);
        assertEquals(genericKey, foundGenericKey);
    }
    
    @Test
    public void testCreateResetKeyAndDelete() throws Exception
    {       
        // get a calendar instance
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        currentDate = DateUtils.addMilliseconds(currentDate, (int) DAY_IN_MILLISECONDS);

        // create the generic key
        IPSGenericKey genericKey = new PSGenericKey();
        genericKey.setExpirationDate(currentDate);
        genericKey.setGenericKey(UUID.randomUUID().toString());
        
        genericKeyDao.saveKey(genericKey);
        
        assertNotNull(genericKey);
        
        genericKeyDao.deleteKey(genericKey);
    }
    
    /**
     * Constant to set the duration time one day into milliseconds
     */
    private static final long DAY_IN_MILLISECONDS = 86400000;
}
