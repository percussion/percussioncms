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

import com.percussion.delivery.metadata.IPSCookieConsent;
import com.percussion.delivery.metadata.IPSCookieConsentDao;
import com.percussion.delivery.metadata.data.PSCookieConsent;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author chriswright
 *
 */
@Repository
@Scope("singleton")
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class PSCookieConsentDao implements IPSCookieConsentDao {

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    private static final Logger log = LogManager.getLogger(PSCookieConsentDao.class);


    @Override
    public void save(Collection<PSDbCookieConsent> consents) {
        Validate.notNull(consents, "Cookie consent object cannot be null");
        
        if (consents.isEmpty())
            return;

        try {
            Session session = getSession();
            
            int i = 0;
            
            for (PSDbCookieConsent consent : consents) {
                session.saveOrUpdate(consent);
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
        catch (Exception e) {
            log.error("Error when saving cookie consent entry. Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public Collection<IPSCookieConsent> getAllCookieConsentStats() {
        
        Collection<IPSCookieConsent> consents = new ArrayList<>();
        
        try {
            Session session = getSession();
            
            Criteria crit = session.createCriteria(PSDbCookieConsent.class);

            @SuppressWarnings("unchecked")
            List<IPSCookieConsent> result = crit.list();
            
            for(IPSCookieConsent res : result) {
                consents.add(res);
            }
        }
        catch (Exception e) {
            log.error("Error retrieving list of cookie consent entries from database. Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);
        }

        return consents;
    }
    
    @Transactional
    @Override
    public Collection<IPSCookieConsent> getAllCookieStatsForSite(String siteName) {
        Collection<IPSCookieConsent> consents = new ArrayList<>();
        try {
            Session session = getSession();
            
            Criteria crit = session.createCriteria(PSDbCookieConsent.class);

            crit.add(Restrictions.eq("siteName", siteName));
            
            @SuppressWarnings("unchecked")
            List<IPSCookieConsent> result = crit.list();
            
            for(IPSCookieConsent res : result) {
                consents.add(res);
            }
        }
        catch (Exception e) {
            log.error("Error retrieving list of cookie consent entries from database. Error: {}", e.getMessage());
        }

        return consents;
    }
    
    @Transactional
    @Override
    public void deleteAll() throws Exception {
        try {
            Session session = getSession();


            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSDbCookieConsent> deleteQuery = builder.createCriteriaDelete(PSDbCookieConsent.class);
            deleteQuery.from(PSDbCookieConsent.class);
            session.createQuery(deleteQuery).executeUpdate();

        }
        catch (Exception e) {
            throw new Exception("Error deleting cookie consent entries from DB.", e);
        }
    }
    
    @Transactional
    @Override
    public void deleteForSite(String siteName) throws Exception {
        try {
            Session session = getSession();

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSDbCookieConsent> deleteQuery = builder.createCriteriaDelete(PSDbCookieConsent.class);
            Root<PSDbCookieConsent> root = deleteQuery.from(PSDbCookieConsent.class);
            deleteQuery.where(builder.like(root.get("siteName"), siteName));
            session.createQuery(deleteQuery).executeUpdate();

        }
        catch (Exception e) {
            throw new Exception("Error deleting cookie consent entries for site: " + siteName, e);
        }
    }
    
    @Transactional
    @Override
    public Map<String, Integer> getTotalsForAllSites() throws Exception {
        try {
            Map<String, Integer> results = new HashMap<>();
            Session session = getSession();

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSDbCookieConsent> criteriaQuery = criteriaBuilder.createQuery(PSDbCookieConsent.class);
            Root<PSDbCookieConsent> root = criteriaQuery.from(PSDbCookieConsent.class);
            criteriaQuery.select(root);

            List<PSDbCookieConsent> cookieConsents = session.createQuery(criteriaQuery).
                    getResultList();

            for (PSDbCookieConsent cookieConsent : cookieConsents) {
                String s = cookieConsent.getSiteName();
                Integer c = results.get(s);
                if(c == null){
                    c = new Integer(1);
                }else{
                    c = c + 1;
                }
                results.put(s, c);
            }
            
            return results;
        }
        catch (Exception e) {
            throw new Exception("Error getting total cookie consents", e);
        }
    }
    
    @Transactional
    @Override
    public Map<String, Integer> getTotalsForSite(String siteName) throws Exception {
        try {
            Map<String, Integer> results = new HashMap<>();
            
            Session session = getSession();
            
            Criteria crit = session.createCriteria(PSDbCookieConsent.class);
            crit.add(Restrictions.eq("siteName", siteName));
            crit.setProjection(Projections.projectionList().add(Projections.property("serviceName")));
            @SuppressWarnings("unchecked")
            List<String> serviceNames = crit.list();
            
            for (String sName : serviceNames) {
                crit = session.createCriteria(PSDbCookieConsent.class);
                crit.setProjection(Projections.rowCount());
                crit.add(Restrictions.eq("serviceName", sName));
                crit.add(Restrictions.eq("siteName", siteName));
                
                @SuppressWarnings("unchecked")
                List<Long> res = crit.list();
                
                results.put(siteName, res.get(0).intValue());
            }
            
            return results;
        }
        catch (Exception e) {

            log.error("Error getting cookie consent entries for site: {} Error: {}", siteName, e.getMessage());
            log.debug(e.getMessage(), e);
            throw new Exception("Error getting cookie consent entries for site: " + siteName, e);
        }
    }

    @Transactional
    @Override
    public void updateOldSiteName(String oldSiteName, String newSiteName) throws Exception {
        Session session = getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaUpdate<PSDbCookieConsent> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(PSDbCookieConsent.class);
        Root<PSDbCookieConsent> root = criteriaUpdate.from(PSDbCookieConsent.class);
        criteriaUpdate.set(root.get("siteName"), newSiteName).where(criteriaBuilder.equal(root.get("siteName"), oldSiteName));
        session.createQuery(criteriaUpdate).executeUpdate();


    }

    private Session getSession(){
        return sessionFactory.getCurrentSession();

    }
}
