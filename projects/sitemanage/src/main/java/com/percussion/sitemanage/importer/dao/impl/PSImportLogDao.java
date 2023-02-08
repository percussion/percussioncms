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
package com.percussion.sitemanage.importer.dao.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import org.apache.commons.lang.Validate;
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
    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);
    
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

    @Autowired
    public final void setGuidManager(IPSGuidManager guidMgr)
    {
        this.guidMgr = guidMgr;
    }

    

}
