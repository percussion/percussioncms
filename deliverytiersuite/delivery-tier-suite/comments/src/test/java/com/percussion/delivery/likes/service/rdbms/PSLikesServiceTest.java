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

package com.percussion.delivery.likes.service.rdbms;

import com.percussion.delivery.likes.data.IPSLikes;
import com.percussion.delivery.likes.services.IPSLikesService;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
@ContextConfiguration(locations =
{"classpath:test-beans.xml"})
public class PSLikesServiceTest extends TestCase
{

    private final String COMMENT1_PAGEPATH = "/01_site1/folder/page1.html";

    private final String COMMENT2_PAGEPATH = "/02_site5/folder/page11.html";

    private final String SITE = "the site";

    @Autowired
    private IPSLikesService likesService;

    @Autowired
    private SessionFactory sessionFactory;



    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        Session session = getSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSLikes> deleteQuery = builder.createCriteriaDelete(PSLikes.class);
            Root<PSLikes> root = deleteQuery.from(PSLikes.class);
            session.createQuery(deleteQuery).executeUpdate();
        }finally {
          //  session.close();
        }
    }

    @After
    public void tearDown()
    {
       // session.close();
    }

    private Session getSession(){

        return sessionFactory.getCurrentSession();

    }
    @Test
    public void testLike() throws Exception
    {
        int total = likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(total, 1);
    }

    @Test
    public void testUnlike() throws Exception
    {
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        int total = likesService.unlike(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(total, 0);

        likesService.like(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        total = likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(total, 0);
    }

    @Test
    public void testGetTotalLike() throws Exception
    {
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        likesService.like(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        int total = likesService.getTotalLikes(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(total, 5);

        total = likesService.getTotalLikes(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(total, 1);
    }

    @Test
    public void testLikesAndUnlikes() throws Exception
    {
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.unlike(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.like(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.unlike(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        // total= 3

        likesService.like(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        likesService.unlike(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());
        // total= 0

        int total = likesService.getTotalLikes(SITE, COMMENT1_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(3, total);

        total = likesService.getTotalLikes(SITE, COMMENT2_PAGEPATH, IPSLikes.TYPE.COMMENT.toString());

        assertEquals(total, 0);
    }
}
