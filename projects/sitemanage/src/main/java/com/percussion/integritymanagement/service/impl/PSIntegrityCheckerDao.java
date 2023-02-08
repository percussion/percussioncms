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

package com.percussion.integritymanagement.service.impl;

import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.integritymanagement.data.PSIntegrityStatus.Status;
import com.percussion.integritymanagement.data.PSIntegrityTask;
import com.percussion.integritymanagement.data.PSIntegrityTaskProperty;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.share.dao.IPSGenericDao.SaveException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Transactional
@Repository("integrityCheckerDao")
public class PSIntegrityCheckerDao implements com.percussion.integritymanagement.service.IPSIntegrityCheckerDao {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }


    public PSIntegrityCheckerDao() {
        super();
    }


    @Override
    @Transactional
    public PSIntegrityStatus find(String token)
    {
        PSIntegrityStatus result = null;
        Session session = getSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<PSIntegrityStatus> criteria = builder.createQuery(PSIntegrityStatus.class);
        Root<PSIntegrityStatus> critRoot = criteria.from(PSIntegrityStatus.class);

        if (token != null)
            criteria.where(builder.equal(critRoot.get("token"), token));
        criteria.orderBy(builder.desc(critRoot.get("startTime")));
        List<PSIntegrityStatus> results = entityManager
                .createQuery(criteria)
                .getResultList();

        if (results.size() > 0)
        {
            result = results.get(0);
        }

        return result;
    }

    @Override
    @Transactional
    public List<PSIntegrityStatus> find(Status status)
    {
        Session session = getSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<PSIntegrityStatus> criteria = builder.createQuery(PSIntegrityStatus.class);
        Root<PSIntegrityStatus> critRoot = criteria.from(PSIntegrityStatus.class);

        if (status != null)
            criteria.where(builder.equal(critRoot.get("status"), status));

        criteria.orderBy(builder.desc(critRoot.get("startTime")));

        return entityManager
                .createQuery(criteria)
                .getResultList();
    }

    @Override
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

    @Override
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


    /**
     * Sets the persisted IDs for the properties of the supplied publish server
     * if needed.
     * 
     * @param status the publish server in question, assumed not
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



}
