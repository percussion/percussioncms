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
package com.percussion.generickey.utils.services;

import com.percussion.generickey.services.IPSGenericKeyService;
import com.percussion.generickey.utils.data.rdbms.impl.PSGenericKey;
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

/**
 * @author leonardohildt
 * 
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSGenericKeyServiceTest extends TestCase
{
    @Autowired
    private IPSGenericKeyService genericKeyService;
    
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
    public void tearDown() {
    }

    @Test
    public void testCreateKey() throws Exception
    {
        String generatedKey = genericKeyService.generateKey(DAY_IN_MILLISECONDS);
        assertNotNull(generatedKey);
        assertFalse(generatedKey.length() == 0);
    }
    
    @Test
    public void testValidKey() throws Exception
    {
        String generatedKey = genericKeyService.generateKey(DAY_IN_MILLISECONDS);
        assertNotNull(generatedKey);
        assertFalse(generatedKey.length() == 0);
        
        boolean isValid = genericKeyService.isValidKey(generatedKey);
        assertTrue(isValid);
    }
    
    /**
     * Constant to set the duration time one day into milliseconds
     */
    private static final long DAY_IN_MILLISECONDS = 86400000;
   
}
