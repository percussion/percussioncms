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

package com.percussion.delivery.polls.service.rdbms;

import com.percussion.delivery.polls.data.IPSPoll;
import com.percussion.delivery.polls.data.IPSPollAnswer;
import com.percussion.delivery.polls.services.IPSPollsService;
import junit.framework.TestCase;
import org.hibernate.FlushMode;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSPollsServiceTest extends TestCase
{
    @Autowired
    private IPSPollsService pollsService;
    @Autowired
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        Session session = getSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSPoll> deleteQuery = builder.createCriteriaDelete(PSPoll.class);
            deleteQuery.from(PSPoll.class);
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
    
    @Test
    public void testSave() throws Exception
    {
        sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
        Map<String,Boolean> answers = new HashMap<String, Boolean>();
        answers.put("Answer1", true);
        answers.put("Answer2", false);
        answers.put("Answer3", false);
        pollsService.savePoll("TestPoll", "TestQuestion", answers);
        try {
            sessionFactory.getCurrentSession().flush();
        }catch (Exception e){

        }
        IPSPoll poll = pollsService.findPollByQuestion("TestQuestion");
        assertNotNull(poll);
        assertEquals("TestPoll", poll.getPollName());
        assertEquals("TestQuestion", poll.getPollQuestion());
        assertEquals(1, poll.getPollAnswers().size());
        
        //add a different answer
        answers.put("Answer1", false);
        answers.put("Answer2", false);
        answers.put("Answer3", true);
        int currSize = poll.getPollAnswers().size();
        pollsService.savePoll("TestPoll", "TestQuestion", answers);
        poll = pollsService.findPollByQuestion("TestQuestion");
        assertEquals(currSize + 1, poll.getPollAnswers().size());
        
        //check the increments
        answers.put("Answer1", true);
        answers.put("Answer2", false);
        answers.put("Answer3", false);
        currSize = poll.getPollAnswers().size();
        pollsService.savePoll("TestPoll", "TestQuestion", answers);
        poll = pollsService.findPollByQuestion("TestQuestion");
        //as we updated existing answer the size should be same 
        assertEquals(currSize, poll.getPollAnswers().size());        
        //Answer1 must be incremented by 1
        Set<IPSPollAnswer> pollAnswers = poll.getPollAnswers();
        for (IPSPollAnswer ipsPollAnswer : pollAnswers)
        {
            if(ipsPollAnswer.getAnswer().equals("Answer1"))
                assertEquals(2, ipsPollAnswer.getCount());
            
            if(ipsPollAnswer.getAnswer().equals("Answer3"))
                assertEquals(1, ipsPollAnswer.getCount());
        }
        
        //Multi answer check
        answers.put("Answer1", false);
        answers.put("Answer2", true);
        answers.put("Answer3", true);
        currSize = poll.getPollAnswers().size();
        pollsService.savePoll("TestPoll", "TestQuestion", answers);
        poll = pollsService.findPollByQuestion("TestQuestion");
        //Answer1 must be incremented by 1
        pollAnswers = poll.getPollAnswers();
        for (IPSPollAnswer ipsPollAnswer : pollAnswers)
        {
            if(ipsPollAnswer.getAnswer().equals("Answer1"))
                assertEquals(2, ipsPollAnswer.getCount());

            if(ipsPollAnswer.getAnswer().equals("Answer2"))
                assertEquals(1, ipsPollAnswer.getCount());
            
            if(ipsPollAnswer.getAnswer().equals("Answer3"))
                assertEquals(2, ipsPollAnswer.getCount());
        }        
        
    }
}
