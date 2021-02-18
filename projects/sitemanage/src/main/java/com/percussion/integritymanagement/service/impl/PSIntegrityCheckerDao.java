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

package com.percussion.integritymanagement.service.impl;

import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.integritymanagement.data.PSIntegrityStatus.Status;
import com.percussion.integritymanagement.data.PSIntegrityTask;
import com.percussion.integritymanagement.data.PSIntegrityTaskProperty;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.share.dao.IPSGenericDao.SaveException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository("integrityCheckerDao")
public class PSIntegrityCheckerDao
{

    private SessionFactory sessionFactory;

    public PSIntegrityCheckerDao() {
        super();
    }


    public PSIntegrityCheckerDao(SessionFactory sessionFactory)
    {
        this.setSessionFactory(sessionFactory);
    }

    @Transactional
    public PSIntegrityStatus find(String token)
    {
        PSIntegrityStatus result = null;
        Session session = getSession();

        Criteria crit = session.createCriteria(PSIntegrityStatus.class);
        if (token != null)
            crit.add(Restrictions.eq("token", token));
        crit.addOrder(Order.desc("startTime"));
        if (crit.list().size() > 0)
        {
            result = (PSIntegrityStatus) crit.list().get(0);
        }

        return result;
    }

    @Transactional
    public List<PSIntegrityStatus> find(Status status)
    {
        Session session = getSession();
        List<PSIntegrityStatus> results = new ArrayList<>();

        Criteria crit = session.createCriteria(PSIntegrityStatus.class);
        if (status != null)
            crit.add(Restrictions.eq("status", status));
        crit.addOrder(Order.desc("startTime"));
        results = crit.list();


        return results;
    }

    @Transactional
    public void delete(PSIntegrityStatus intStatus)
    {
        Session session = getSession();
        try
        {
            session.delete(intStatus);
        }
        finally
        {
            session.flush();
        }
    }

    @Transactional
    public void save(PSIntegrityStatus status) throws SaveException
    {
        Session session = getSession();
        try
        {
            setValidPersistedIds(status);
            session.saveOrUpdate(status);
        }
        finally
        {
            session.flush();
        }
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Sets the persisted IDs for the properties of the supplied publish server
     * if needed.
     * 
     * @param pubServer the publish server in question, assumed not
     *            <code>null</code>.
     */
    private void setValidPersistedIds(PSIntegrityStatus status)
    {
        for (PSIntegrityTask t : status.getTasks())
        {
            if (t.getTaskId() == -1L)
            {
                long nextId = PSGuidHelper.generateNext(PSTypeEnum.INTEGRITY_TASK).longValue();
                t.setTaskId(nextId);
            }
            for (PSIntegrityTaskProperty prop : t.getTaskProperties())
            {
                long nextPropId = PSGuidHelper.generateNext(PSTypeEnum.INTEGRITY_TASK_PROPERTY).longValue();
                prop.setTaskPropertyId(nextPropId);
            }
        }
    }


    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
