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
package com.percussion.sitemanage.dao.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSUserManagementEvent;
import com.percussion.cms.IPSConstants;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.dao.IPSUserLoginDao;
import com.percussion.user.data.PSUserLogin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author DavidBenua
 *
 */
@Transactional
@Repository("userLoginDao")
public class PSUserLoginDao implements IPSUserLoginDao
{
    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    private static final Logger log = LogManager.getLogger(IPSConstants.SECURITY_LOG);
    

    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSUserManagementEvent psUserManagementEvent;

    /* (non-Javadoc)
     * @see com.percussion.share.dao.IPSGenericDao#delete(java.io.Serializable)
     */
    @Override
    public void delete(String name) throws IPSGenericDao.DeleteException
    {
        String emsg; 
        Session session = getSession(); 
        try 
        {   
            PSUserLogin login = (PSUserLogin) session.get(PSUserLogin.class, name);
            log.debug("deleting userlogin for " + login);
            if(login == null)
            {
                emsg = "Attempt to delete non-existant user " + name; 
                log.warn(emsg);
                return; 
            }
            session.delete(login);

        }
        catch(HibernateException he)
        {
            psUserManagementEvent=new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                    PSUserManagementEvent.UserEventActions.delete,
                    PSActionOutcome.FAILURE);
            psAuditLogService.logUserManagementEvent(psUserManagementEvent);
            emsg = "database error " + he.getMessage(); 
            log.error(emsg);
            throw new IPSGenericDao.DeleteException(emsg, he); 
        }
        finally
        {
            session.flush(); 
        }


    }

    /* (non-Javadoc)
     * @see com.percussion.share.dao.IPSGenericDao#find(java.io.Serializable)
     */
    @Override
    public PSUserLogin find(String id) throws IPSGenericDao.LoadException
    {
        String emsg; 
        Session session = getSession();
        PSUserLogin result = null; 
        try
        {
            result = (PSUserLogin) session.get(PSUserLogin.class, id); 
            if(result == null)
            {
                emsg = "no such user " + id; 
                log.debug(emsg);
            }
        }
        catch(HibernateException he)
        {   
            emsg = "database error " + he.getMessage(); 
            log.error(emsg);
            throw new IPSGenericDao.LoadException(emsg, he); 
        }
       
        return result;
    }
    

    /* (non-Javadoc)
     * @see com.percussion.share.dao.IPSUserLoginDao#findByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<PSUserLogin> findByName(String name) throws IPSGenericDao.LoadException
    {
        String emsg;
        Session session = getSession(); 
        List<PSUserLogin> results = new ArrayList<>();
        try
        {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<PSUserLogin> criteria = builder.createQuery(PSUserLogin.class);
            Root<PSUserLogin> critRoot = criteria.from(PSUserLogin.class);
            criteria.where(builder.equal(builder.lower(critRoot.get("userid")), name.toLowerCase()));
            results = entityManager.createQuery(criteria).getResultList();
        }
        catch (HibernateException he)
        {
            emsg = "database error " + he.getMessage(); 
            log.error(emsg);
            throw new IPSGenericDao.LoadException(emsg, he); 
        }
       

        return results;
    }

    /* (non-Javadoc)
     * @see com.percussion.share.dao.IPSGenericDao#findAll()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PSUserLogin> findAll() throws com.percussion.share.dao.IPSGenericDao.LoadException
    {
        String emsg;
        Session session = getSession(); 
        List<PSUserLogin> results = new ArrayList<>();
        try
        {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<PSUserLogin> criteria = builder.createQuery(PSUserLogin.class);
            Root<PSUserLogin> critRoot = criteria.from(PSUserLogin.class);
            criteria.orderBy(builder.asc(critRoot.get("userid")));
            results = entityManager.createQuery(criteria).getResultList();
        }
        catch (HibernateException he)
        {
            emsg = "database error " + he.getMessage(); 
            log.error(emsg);
            throw new IPSGenericDao.LoadException(emsg, he); 
        }
        
        return results;
    }

    /* (non-Javadoc)
     * @see com.percussion.share.dao.IPSGenericDao#save(java.lang.Object)
     */
    @Override
    public PSUserLogin save(PSUserLogin login) throws com.percussion.share.dao.IPSGenericDao.SaveException
    {
        String emsg; 
        Session session = getSession(); 
        try 
        {   
            String uid = login.getUserid();
            PSUserLogin l2 = (PSUserLogin) session.get(PSUserLogin.class, uid);
            if(l2 == null)
            {
                emsg = "Attempt to modify non-existant user " + uid; 
                log.error(emsg);
                throw new IPSGenericDao.SaveException(emsg); 
            }
            l2.setPassword(login.getPassword()); 
            session.update(l2);

             
        }
        catch(HibernateException he)
        {
            psUserManagementEvent=new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                    PSUserManagementEvent.UserEventActions.update,
                    PSActionOutcome.FAILURE);
            psAuditLogService.logUserManagementEvent(psUserManagementEvent);
            emsg = "database error " + he.getMessage(); 
            log.error(emsg);
            throw new IPSGenericDao.SaveException(emsg, he); 
        }
        finally
        {
            session.flush();
        }
        return login;
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.dao.IPSUserLoginDao#create(com.percussion.sitemanage.data.PSUserLogin)
     */
    @Override
    public PSUserLogin create(PSUserLogin login) throws com.percussion.share.dao.IPSGenericDao.SaveException
    {
        String emsg; 
        Session session = getSession(); 
        try 
        {   
            session.save(login);
            psUserManagementEvent=new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                    PSUserManagementEvent.UserEventActions.create,
                    PSActionOutcome.SUCCESS);
            psAuditLogService.logUserManagementEvent(psUserManagementEvent);
            
        }
        catch(HibernateException he)
        {   
            emsg = "database error " + he.getMessage(); 
            log.error(emsg);
            psUserManagementEvent=new PSUserManagementEvent(PSSecurityFilter.getCurrentRequest().getServletRequest(),
                    PSUserManagementEvent.UserEventActions.create,
                    PSActionOutcome.FAILURE);
            psAuditLogService.logUserManagementEvent(psUserManagementEvent);
            throw new IPSGenericDao.SaveException(emsg, he); 
        }
        finally
        {
            session.flush();
        }
        return login;
    }

}
