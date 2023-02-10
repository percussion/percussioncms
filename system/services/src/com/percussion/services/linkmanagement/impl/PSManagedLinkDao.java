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
package com.percussion.services.linkmanagement.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.linkmanagement.IPSManagedLinkDao;
import com.percussion.services.linkmanagement.data.PSManagedLink;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.MAX_IDS;
import static org.apache.commons.lang.StringUtils.join;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Handles CRUD for link data used by a managed link.
 * 
 * @author JaySeletz
 *
 */
@PSSiteManageBean("sys_managedLinkDao")
@Transactional
public class PSManagedLinkDao implements IPSManagedLinkDao
{
    private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);
    
    /**
     * Constant for the key used to generate link id's.
     */
    private static final String MANAGED_LINK_KEY = "PSX_MANAGEDLINK";

    private IPSGuidManager m_guidMgr;

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    @Autowired
    public void setGuidManager(IPSGuidManager guidMgr)
    {
        m_guidMgr = guidMgr;
    }
    
    @Transactional
    public PSManagedLink createLink(int parentId, int parentRev, int childId, String anchor)
    {
        PSManagedLink link = new PSManagedLink();
        link.setParentId(parentId);
        link.setParentRevision(parentRev);
        link.setChildId(childId);
        link.setAnchor(anchor);
        
        return link;
    }

    @Transactional
    public void saveLink(PSManagedLink link) throws IPSGenericDao.SaveException {
        Validate.notNull(link);
        if (link.getLinkId() == -1)
        {
            link.setLinkId(m_guidMgr.createId(MANAGED_LINK_KEY));
        }
        
        Session session = getSession();
        try
        {
            session.saveOrUpdate(link);
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

    public PSManagedLink findLinkByLinkId(long linkId)
    {
        Session session = getSession();

            return session.get(PSManagedLink.class,linkId);

    }

    @Transactional
    public void deleteLink(PSManagedLink link)
    {
        Validate.notNull(link);
        Session session = getSession();
        try
        {
            session.delete(link);
        }
        catch (HibernateException e)
        {
            String msg = "Failed to delete managed link: " + e.getMessage();
            log.error(msg);
        }
        finally
        {
            session.flush();

        }
    }
    
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteLinksInNewTransaction(Collection<PSManagedLink> links)
    {
        Validate.notNull(links);
        Session session = getSession();
        try
        {
            for (PSManagedLink link : links)
            {
                log.debug("Deleting managed link with id: " + link.getLinkId());
                session.delete(link);
            }
        }
        catch (HibernateException e)
        {
            String msg = "Failed to delete managed link in new transaction: " + e.getMessage();
            log.error(msg);
        }
        finally
        {
            session.flush();
        }
    }
    
    @Transactional(readOnly=false,propagation=Propagation.REQUIRES_NEW)
    public void cleanupOrphanedLinks()
    {
       Session session = getSession();
       try
       {
          Query q = session.createQuery("DELETE FROM PSManagedLink ml WHERE ml.childId NOT IN (select m_contentId from PSComponentSummary)");
          q.executeUpdate();
          q = session.createQuery("DELETE FROM PSManagedLink ml WHERE ml.parentId NOT IN (select m_contentId from PSComponentSummary)");
          q.executeUpdate();
       }
       catch (HibernateException e)
       {
           String msg = "Failed to cleanup managed links: " + e.getMessage();
           log.error(msg);
       }
       finally
       {
           session.flush();

       }
    }

    @SuppressWarnings("unchecked")
    public List<PSManagedLink> findLinksByParentId(int parentId)
    {
        Session session = getSession();

            Query query = session.createQuery("from PSManagedLink where parentid = :parentId");
            query.setParameter("parentId", parentId);
            

            return query.list();

    }
    
    public List<PSManagedLink> findLinksByParentIds(List<Integer> parentIds)
    {
       if(isEmpty(parentIds))
       {
           return new ArrayList<>();
       }
       
       if (parentIds.size() < MAX_IDS)
       {
           return findLinksByListOfParentIds(parentIds);
       }
       else
       {
           // use pagination to avoid issues with some DB engines
           List<PSManagedLink> results =  new ArrayList<>();
           for (int i = 0; i < parentIds.size(); i += MAX_IDS)
           {
               int end = Math.min(i + MAX_IDS, parentIds.size());
               // make the query
               results.addAll(findLinksByListOfParentIds(parentIds.subList(i, end)));
           }
           return results;
       }
    }
    
   @SuppressWarnings("unchecked")
   private List<PSManagedLink> findLinksByListOfParentIds(List<Integer> parentIds)
   {
      Session session = getSession();

         Query query = session
               .createQuery("from PSManagedLink where parentid in (" + join(parentIds, ",") + ") ");
         List<PSManagedLink> results = query.list();
         return results;


   }

   @Override
   public List<PSManagedLink> findLinksByChildId(int childId)
   {
      Session session = getSession();

     Query query = session
           .createQuery("from PSManagedLink where childid = :childId ");
     query.setParameter("childId", childId);
     return  query.list();
   }
    
}
