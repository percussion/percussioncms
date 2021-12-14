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

package com.percussion.delivery.metadata.rdbms.impl;

import com.percussion.delivery.metadata.IPSBlogPostVisit;
import com.percussion.delivery.metadata.IPSBlogPostVisitDao;
import com.percussion.delivery.metadata.utils.PSHashCalculator;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Repository
@Scope("singleton")
public class PSBlogPostVisitDao  implements IPSBlogPostVisitDao {

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(PSBlogPostVisitDao.class);

    private static PSHashCalculator hashCalculator = new PSHashCalculator();


    public void delete(Collection<String> pagepaths) {

        /**
         * this code is close but not working.
         Validate.notNull(pagepaths, "pagepaths cannot be null.");

         Collection<String> pagepathHashes = getPagepathHashes(pagepaths);

         Session session = getSession(false);

         // Delete entries
         String hqlMetadataEntryDelete = "delete from PSDbBlogPostVisit visit "
         + "where visit.id in (:pagepathHashes)";

         session.createQuery(hqlMetadataEntryDelete).setParameterList("pagepathHashes", pagepathHashes).executeUpdate();
         **/

        throw new UnsupportedOperationException();

    }

    public boolean delete(String pagepath) {
        /**
         Validate.notEmpty(pagepath, "pagepath cannot be null or empty.");

         List<PSDbBlogPostVisit> visits = findBlogPostVisit(pagepath);
         if (visits != null)
         {
         for(PSDbBlogPostVisit visit : visits) {
         getHibernateTemplate().delete(visit);
         }
         return true;
         }
         return false;
         **/
        throw new UnsupportedOperationException();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void save(Collection<IPSBlogPostVisit> visits) {
        Validate.notNull(visits, "visits cannot be null");
        if (visits.size() == 0)
            return;

        Session session = getSession();

        // Save entries
        int i = 0;
        Collection<PSDbBlogPostVisit> dbVisits = convertToDbVisits(visits);
        for (PSDbBlogPostVisit visit : dbVisits)
        {

            session.saveOrUpdate(visit);

            // To avoid OutOfMemory exceptions in case of a lot of entries
            // to be added, flush and clear session after 50 newly added
            // entries (this value is the same value set for JDBC batch size
            // in beans.xml file).
            if (++i % 50 == 0)
            {
                session.flush();
                session.clear();
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        }

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void save(IPSBlogPostVisit visit) {
        Collection<IPSBlogPostVisit> visits = Collections.singletonList(visit);
        save(visits);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<String> getTopVisitedPages(String sectionPath, int days, int limit, String sortOrder) {
        Session session = getSession();
        Calendar cal = Calendar.getInstance();
        /*
         *  if -1, means we are looking for events from all time.
         *  we just keep current time and filter below current time.
         */
        if (days != -1)
            cal.add(Calendar.DAY_OF_YEAR, -days);
        Date fromDate = cal.getTime();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
        Root<PSDbBlogPostVisit> root = criteriaQuery.from(PSDbBlogPostVisit.class);

        Predicate dateCriteria = null;
        if (days != -1) {
            dateCriteria = criteriaBuilder.greaterThanOrEqualTo(root.get("hitDate"), fromDate);
        }else {
            dateCriteria = criteriaBuilder.lessThanOrEqualTo(root.get("hitDate"), fromDate);
        }

        sectionPath = sectionPath + "%";


        criteriaQuery.select(root.get("pagepath")).where(criteriaBuilder.and(criteriaBuilder.like(root.get("pagepath"), sectionPath),dateCriteria));
        criteriaQuery.groupBy(root.get("pagepath"));
        if(sortOrder == null || sortOrder.equalsIgnoreCase("desc")){
            criteriaQuery.orderBy(criteriaBuilder.desc(criteriaBuilder.sum(root.get("hitCount"))));
        }else{
            criteriaQuery.orderBy(criteriaBuilder.asc(criteriaBuilder.sum(root.get("hitCount"))));
        }

        return session.createQuery(criteriaQuery).setMaxResults(limit).getResultList();

    }

    @Transactional(isolation=Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<PSDbBlogPostVisit> findBlogPostVisit(String pagepath) {
        Validate.notEmpty(pagepath, "pagepath cannot be null nor empty");

        Session session = getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PSDbBlogPostVisit> criteriaQuery = criteriaBuilder.createQuery(PSDbBlogPostVisit.class);
        Root<PSDbBlogPostVisit> root = criteriaQuery.from(PSDbBlogPostVisit.class);
        criteriaQuery.select(root).where(criteriaBuilder.like(root.get("pagepath"), pagepath));

        return session.createQuery(criteriaQuery).getResultList();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED,readOnly = true)
    public PSDbBlogPostVisit findBlogPostVisitByDate(String pagepath, Date date) {
        Validate.notEmpty(pagepath, "pagepath cannot be null nor empty");

        Session session = getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PSDbBlogPostVisit> criteriaQuery = criteriaBuilder.createQuery(PSDbBlogPostVisit.class);
        Root<PSDbBlogPostVisit> root = criteriaQuery.from(PSDbBlogPostVisit.class);
        criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.like(root.get("pagepath"), pagepath),criteriaBuilder.equal(root.get("hitDate"), date)));

        List<PSDbBlogPostVisit> results = session.createQuery(criteriaQuery).getResultList();

        if (results == null || results.isEmpty())
            return null;
        return results.get(0);
    }

    private Collection<String> getPagepathHashes(Collection<String> pagepaths)
    {
        List<String> pagepathHashes = new ArrayList<>();

        for (String pp : pagepaths)
        {
            pagepathHashes.add(hashCalculator.calculateHash(pp));
        }

        return pagepathHashes;
    }

    private Collection<PSDbBlogPostVisit> convertToDbVisits(Collection<IPSBlogPostVisit> visits) {
        Validate.notNull(visits, "list of visits cannot be null");

        Collection<PSDbBlogPostVisit> result = new ArrayList<>();
        for (IPSBlogPostVisit visit : visits) {
            PSDbBlogPostVisit dbVisit = findBlogPostVisitByDate(visit.getPagepath(), visit.getHitDate());
            if (dbVisit != null) {
                dbVisit.setHitCount(dbVisit.getHitCount().add(visit.getHitCount()));
            } else {
                dbVisit = new PSDbBlogPostVisit(visit.getPagepath(), visit.getHitDate(), visit.getHitCount());
            }
            result.add(dbVisit);
        }
        return result;
    }

    @Override
    @Transactional
    public void updatePostsAfterSiteRename(String prevSiteName,
                                           String newSiteName) throws Exception {
        Session session = getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PSDbBlogPostVisit> criteriaQuery = criteriaBuilder.createQuery(PSDbBlogPostVisit.class);
        Root<PSDbBlogPostVisit> root = criteriaQuery.from(PSDbBlogPostVisit.class);
        criteriaQuery.select(root).where(criteriaBuilder.like(root.get("pagepath"), "%/" +prevSiteName + "/%"));

        List<PSDbBlogPostVisit> results = session.createQuery(criteriaQuery).getResultList();

        for (PSDbBlogPostVisit visit : results) {
            visit.setPagepath(visit.getPagepath().replaceAll(prevSiteName, newSiteName));
            session.saveOrUpdate(visit);
        }
    }

    private Session getSession(){

        return sessionFactory.getCurrentSession();

    }
}
