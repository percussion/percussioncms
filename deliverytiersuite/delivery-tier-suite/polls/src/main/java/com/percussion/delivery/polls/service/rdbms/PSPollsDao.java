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
