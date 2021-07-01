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
package com.percussion.sitemanage.dao.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSAuthenticationEvent;
import com.percussion.auditlog.PSUserManagementEvent;
import com.percussion.services.security.data.PSSecurityUtils;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.dao.IPSUserLoginDao;
import com.percussion.user.data.PSUserLogin;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.security.PSSecurityUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author DavidBenua
 *
 */
@Transactional
@Repository("userLoginDao")
public class PSUserLoginDao implements IPSUserLoginDao
{
    private static final Logger log = LogManager.getLogger(PSUserLoginDao.class);
    
    private SessionFactory sessionFactory;
    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSUserManagementEvent psUserManagementEvent;
    @Autowired
    public PSUserLoginDao(SessionFactory sessionFactory)
    {
        this.setSessionFactory(sessionFactory);
    }
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
    
    private Session getSession()
    {
        return sessionFactory.getCurrentSession();
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
            results = session.createCriteria(PSUserLogin.class).add(Restrictions.ilike("userid", name)).list();
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
            results = session.createCriteria(PSUserLogin.class)
            .addOrder(Order.asc("userid")).list();
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
   
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
    
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
