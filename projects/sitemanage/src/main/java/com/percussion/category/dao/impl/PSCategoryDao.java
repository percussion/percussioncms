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

package com.percussion.category.dao.impl;

import com.percussion.category.dao.IPSCategoryDao;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author chriswright
 */
@Transactional
@Repository("categoryDao")
public class PSCategoryDao implements IPSCategoryDao {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    @Autowired
    private IPSContentRepository contentRepository;

    @Autowired
    private IPSIdMapper idMapper;

    private static final Logger log = LogManager.getLogger(PSCategoryDao.class);

    private PSCategoryDao() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Set<String> ids, List<IPSGuid> pageIds) {
        log.info("Ids to delete are: {}" , ids);
        Session session = getSession();
        String query = null;
        Query q = null;
        try {
            for (String id : ids) {
                query = "DELETE FROM PSCategoryEntity WHERE pageCategoriesTree LIKE :id";
                q = session.createQuery(query);
                q.setParameter("id", "%" + id + "%");
                int result = q.executeUpdate();
                log.info("The result is: {}" , result);
            }
        } catch (HibernateException e) {
            log.error(
                    "There was an error deleting page categories from the database.",
                    e);
        }

        contentRepository.evict(pageIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getPageIdsFromCategoryIds(Set<String> ids) {
        log.info("IDs to grab are: {}" , ids);
        List<IPSGuid> guids = new ArrayList<>();
        List<Integer> pageIds = new ArrayList<>();
        Session session = getSession();
        String query = null;
        Query q = null;
        for (String id : ids) {
            query = "SELECT DISTINCT id FROM PSCategoryEntity WHERE pageCategoriesTree LIKE :id";
            q = session.createQuery(query);
            q.setParameter("id", "%" + id + "%");
            try {
                pageIds = q.list();
            } catch (HibernateException e) {
                log.error("Error executing category query to get page IDs.", e);
            }
        }
        log.info("The page IDs returned from the category IDs were: {}" , pageIds);
        return pageIds;
    }


}
