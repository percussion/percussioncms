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

package com.percussion.delivery.polls.service.rdbms;

import com.percussion.delivery.polls.data.IPSPoll;
import com.percussion.delivery.polls.data.IPSPollAnswer;
import com.percussion.delivery.polls.services.IPSPollsDao;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Transactional
public class PSPollsDao extends HibernateDaoSupport implements IPSPollsDao
{

    @Override
    public IPSPoll find(String pollName)
    {
        Session session = getSession();
        IPSPoll poll = null;
        try
        {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSPoll> criteriaQuery = criteriaBuilder.createQuery(PSPoll.class);
            Root<PSPoll> root = criteriaQuery.from(PSPoll.class);
            criteriaQuery.select(root).where(criteriaBuilder.like(root.get("pollName"), pollName));
            List<PSPoll> resultList = session.createQuery(criteriaQuery).getResultList();

            if (!resultList.isEmpty())
                poll = (IPSPoll) resultList.get(0);
            return poll;

        }
        finally
        {
            // session.close();
        }
    }

    @Override
    public void save(IPSPoll poll)
    {
        Session session = getSession();
        try
        {
            session.saveOrUpdate(poll);
        }
        finally
        {
            //  session.close();
        }
    }

    @Override
    public IPSPoll findByQuestion(String pollQuestion)
    {

        Session session = getSession();
        IPSPoll poll = null;
        try
        {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSPoll> criteriaQuery = criteriaBuilder.createQuery(PSPoll.class);
            Root<PSPoll> root = criteriaQuery.from(PSPoll.class);
             criteriaQuery.where(criteriaBuilder.like(root.get("pollQuestion"), pollQuestion));
            List<PSPoll> resultList = session.createQuery(criteriaQuery).getResultList();
            if (!resultList.isEmpty())
                poll = (IPSPoll) resultList.get(0);
            return poll;
        }
        finally
        {
            // session.close();
        }
    }

    @Override
    public IPSPoll createEmptyPoll()
    {
        return new PSPoll();
    }

    @Override
    public IPSPollAnswer createEmptyAnswer()
    {
        return new PSPollAnswer();
    }

    private Session getSession(){

        return getSessionFactory().getCurrentSession();

    }
}
