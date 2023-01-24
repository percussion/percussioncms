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

package com.percussion.recent.dao.impl;

import com.percussion.recent.dao.IPSRecentDao;
import com.percussion.recent.data.PSRecent;
import com.percussion.recent.data.PSRecent.RecentType;
import com.percussion.share.dao.IPSGenericDao.SaveException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

@Repository("recentDao")
@Transactional
public class PSRecentDao implements IPSRecentDao
{

  @PersistenceContext
private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    PSRecentDao()
    {
        
    }

    public List<PSRecent> find(String user, String siteName, RecentType type)
    {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<PSRecent> criteria = builder.createQuery(PSRecent.class);
        Root<PSRecent> recent = criteria.from(PSRecent.class);
        List<Predicate> predList = new LinkedList<>();

        if (user!=null) {
            predList.add(builder.equal(recent.get("user"), user));
        }
        if(siteName!=null) {
            predList.add(builder.equal(recent.get("siteName"), siteName));
        }
        if(type!=null) {
            predList.add( builder.equal(recent.get("type"), type));
        }
        Predicate[] preds = new Predicate[predList.size()];
        preds = predList.toArray(preds);
        criteria.where(preds);
        criteria.orderBy(builder.asc(recent.get("order")));
        return entityManager
                .createQuery(criteria)
                .getResultList();
    }
    

    public void saveAll(List<PSRecent> recentList)
    {
     
        for (PSRecent recent : recentList)
        {
            getSession().saveOrUpdate(recent);
        }
        
    }


    public void delete(PSRecent recent) 
    {
        getSession().delete(recent);
    }
    

    public void deleteAll(List<PSRecent> recentList)
    {
        for (PSRecent recent : recentList)
        {
            getSession().delete(recent);
        }
    }

    

    public void save(PSRecent recent) throws SaveException
    {
        getSession().saveOrUpdate(recent);
    }

 
}

