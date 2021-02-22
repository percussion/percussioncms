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
package com.percussion.sitemanage.importer.dao.impl;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.MAX_IDS;

/**
 * @author JaySeletz
 */
@Repository("importLogDao")
@Transactional
public class PSImportLogDao implements IPSImportLogDao
{
    private static final Log log = LogFactory.getLog(PSImportLogDao.class);
    
    /**
     * Constant for the key used to generate local content id's.
     */
    private static final String LOG_ENTRY_KEY = "PSX_IMPORTLOGENTRY";
    
    

    private IPSGuidManager guidMgr;

    @Override
    @Transactional
    public void save(PSImportLogEntry logEntry) throws IPSGenericDao.SaveException {
        Validate.notNull(logEntry);
        if (logEntry.getLogEntryId() == -1)
        {
            logEntry.setLogEntryId(guidMgr.createId(LOG_ENTRY_KEY));
        }
        
        Session session = getSession();
        try
        {
            session.saveOrUpdate(logEntry);
        }
        catch (HibernateException e)
        {
            String msg = "database error " + e.getMessage();
            log.error(msg);
            throw new IPSGenericDao.SaveException(msg, e);
        }
        finally
        {
            session.flush();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PSImportLogEntry> findAll(String objectId, String type)
    {
        Validate.notNull(type);
        
        Session session = getSession();
        
        Query query = session.createQuery("from PSImportLogEntry where objectId = :objectId and objectType = :objectType");
        query.setParameter("objectId", objectId);
        query.setParameter("objectType", type);

        return query.list(); 


        
    }

    @Override
    public void delete(PSImportLogEntry logEntry) throws IPSGenericDao.SaveException {
        Validate.notNull(logEntry);
        
        Session session = getSession();
        try
        {
            session.delete(logEntry);
        }
        catch (HibernateException e)
        {
            String msg = "database error " + e.getMessage();
            log.error(msg);
            throw new IPSGenericDao.SaveException(msg, e);
        }
        finally
        {
            session.flush();
        }        
    }


    @Override
    public PSImportLogEntry findLogEntryById(long pageLogId)
    {
        Session session = getSession();
       
        return (PSImportLogEntry) session.get(PSImportLogEntry.class, pageLogId);
   
    }
    
    @Override
    public List<Long> findLogIdsForObjects(List<String> objectIds, String type)
    {
        Validate.notNull(objectIds);
        Validate.notNull(type);
        
        List<Long> results = new ArrayList<>();
        
        if (objectIds.isEmpty())
            return results;
        
        
        
        if (objectIds.size() < MAX_IDS)
        {
            results.addAll(findLogIdsByObjectIds(objectIds, type));
        }
        else
        {
            // we need to paginate the query to avoid oracle problems
            for (int i = 0; i < objectIds.size(); i += MAX_IDS)
            {
                int end = (i + MAX_IDS > objectIds.size()) ? objectIds.size() : i + MAX_IDS;
                // make the query
                results.addAll(findLogIdsByObjectIds(objectIds.subList(i, end), type));
            }
        }
        
        Collections.sort(results);
        
        return results;
    }
    
    private List<Long> findLogIdsByObjectIds(List<String> objectIds, String type)
    {
        Session session = getSession();
     
        Query query = session.createQuery("select e.logEntryId from PSImportLogEntry e where e.objectId in (:objectIds) and e.objectType = :objectType");
        query.setParameterList("objectIds", objectIds);
        query.setParameter("objectType", type);

        return query.list(); 

       
      
    }

    private Session getSession()
    {
        return sessionFactory.getCurrentSession();
    }

    @Autowired
    public final void setGuidManager(IPSGuidManager guidMgr)
    {
        this.guidMgr = guidMgr;
    }

    private  SessionFactory sessionFactory;

    public final SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
    
    @Autowired
    public final void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
    

}
