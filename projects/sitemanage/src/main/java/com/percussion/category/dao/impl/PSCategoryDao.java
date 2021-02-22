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

package com.percussion.category.dao.impl;

import com.percussion.category.dao.IPSCategoryDao;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author chriswright
 */
@Transactional
@Repository("categoryDao")
public class PSCategoryDao implements IPSCategoryDao {

    /**
     * The hibernate session factory injected by spring
     */
    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IPSContentRepository contentRepository;

    @Autowired
    private IPSIdMapper idMapper;

    private static final Logger log = Logger.getLogger(PSCategoryDao.class
            .getName());

    private PSCategoryDao() {
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Set<String> ids, List<IPSGuid> pageIds) {
        log.info("Ids to delete are: " + ids);
        Session session = getSession();
        String query = null;
        Query q = null;
        try {
            for (String id : ids) {
                query = "DELETE FROM PSCategoryEntity WHERE pageCategoriesTree LIKE :id";
                q = session.createQuery(query);
                q.setParameter("id", "%" + id + "%");
                int result = q.executeUpdate();
                log.info("The result is: " + result);
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
        log.info("IDs to grab are: " + ids);
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
        log.info("The page IDs returned from the category IDs were: " + pageIds);
        return pageIds;
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
