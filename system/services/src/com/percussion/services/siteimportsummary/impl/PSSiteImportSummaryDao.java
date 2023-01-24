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

package com.percussion.services.siteimportsummary.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.siteimportsummary.IPSSiteImportSummaryDao;
import com.percussion.services.siteimportsummary.data.PSSiteImportSummary;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSBaseBean;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@PSBaseBean("sys_siteImportSummaryDao")
@Transactional
@Repository
public class PSSiteImportSummaryDao implements IPSSiteImportSummaryDao
{
   private static final Logger log = LogManager.getLogger(IPSConstants.IMPORT_LOG);
   /**
    * Constant for the key used to generate summary ids.
    */
   private static final String SITE_IMPORT_SUMMARY_KEY = "PSX_SITEIMPORTSUMMARY";
   
   private IPSGuidManager m_guidMgr;

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }
    
    @Transactional
   public void save(PSSiteImportSummary summary) throws IPSGenericDao.SaveException {
      Validate.notNull(summary);
      if (summary.getSummaryId() == -1)
      {
          summary.setSummaryId(m_guidMgr.createId(SITE_IMPORT_SUMMARY_KEY));
      }
      
      Session session = getSession();
      try
      {
          session.saveOrUpdate(summary);
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

   public PSSiteImportSummary findBySummaryId(int summaryId)
   {
      PSSiteImportSummary summary = null;
      Session session = getSession();

          Query query = session.createQuery("from PSSiteImportSummary where summaryId = :summaryId");
          query.setParameter("summaryId", summaryId);

          List<PSSiteImportSummary> results = query.list(); 
          if (!results.isEmpty())
          {
             summary = results.get(0);
              if (results.size() > 1)
              {
                  log.error("More than one managed link found for linkid: {}", summaryId);
              }
          }
          return summary;

   }

   public PSSiteImportSummary findBySiteId(int siteId)
   {
      PSSiteImportSummary summary = null;
      Session session = getSession();

          Query query = session.createQuery("from PSSiteImportSummary where siteId = :siteId");
          query.setParameter("siteId", siteId);
          

          List<PSSiteImportSummary> results = query.list(); 
          if (!results.isEmpty())
          {
             summary = results.get(0);
              if (results.size() > 1)
              {
                  log.error("More than one managed link found for siteId: {}" , siteId);
              }
          }
          return summary;

   }

   @Transactional
   public void delete(PSSiteImportSummary summary)
   {
      Validate.notNull(summary);
      Session session = getSession();
      try
      {
          session.delete(summary);
      }
      catch (HibernateException e)
      {
          String msg = "Failed to delete site import summary: " + e.getMessage();
          log.error(msg);
      }
      finally
      {
          session.flush();

      }
   }

   @Autowired
   public void setGuidManager(IPSGuidManager guidMgr)
   {
       m_guidMgr = guidMgr;
   }

}
