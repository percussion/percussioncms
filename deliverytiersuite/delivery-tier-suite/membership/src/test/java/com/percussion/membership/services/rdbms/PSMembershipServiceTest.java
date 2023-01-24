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
package com.percussion.membership.services.rdbms;

import com.percussion.membership.data.rdbms.impl.PSMembership;
import com.percussion.membership.services.PSBaseMembershipServiceTest;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

/**
 * @author erikserating
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSMembershipServiceTest extends PSBaseMembershipServiceTest
{
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
            deleteQuery.from(PSMembership.class);
            session.createQuery(deleteQuery).executeUpdate();
        }finally {
           // session.close();
        }

    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    private Session getSession(){

        return sessionFactory.getCurrentSession();

    }
}
