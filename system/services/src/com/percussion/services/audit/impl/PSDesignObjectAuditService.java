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

package com.percussion.services.audit.impl;

import com.percussion.services.audit.IPSDesignObjectAuditConfig;
import com.percussion.services.audit.IPSDesignObjectAuditService;
import com.percussion.services.audit.data.PSAuditLogEntry;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Implementation of the design object audit service.
 */
@Transactional
public class PSDesignObjectAuditService
   implements IPSDesignObjectAuditService
{
   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }

   /**
    * Set the audit configuration on this class, usually called by Spring
    * framework via dependency injection.
    * 
    * @param config The config, may not be <code>null</code>.
    */
   public void setConfig(IPSDesignObjectAuditConfig config)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      
      m_config = config;
   }

   public IPSDesignObjectAuditConfig getConfig()
   {
      return m_config;
   }

   @Transactional
   public PSAuditLogEntry createAuditLogEntry()
   {
      PSAuditLogEntry entry  = new PSAuditLogEntry();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      entry.setGUID(guidMgr.createGuid(PSTypeEnum.INTERNAL));
      
      return entry;
   }

   @Transactional
   public void saveAuditLogEntry(PSAuditLogEntry entry)
   {
      getSession().save(entry);
   }

   @Transactional
   public void deleteAuditLogEntriesByDate(Date beforeDate)
   {
      if (beforeDate == null)
         throw new IllegalArgumentException("beforeDate may not be null");
      
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSAuditLogEntry.class);
         criteria.add(Restrictions.lt("auditDate", beforeDate));
         List<PSAuditLogEntry> entries = criteria.list();
         
         for (PSAuditLogEntry entry : entries)
         {
            session.delete(entry);
         }

   }

   @Transactional
   public void saveAuditLogEntries(Collection<PSAuditLogEntry> entries)
   {
      Session session = getSession();
      

         for (PSAuditLogEntry entry : entries)
         {
            session.save(entry);
         }

   }

   public Collection<PSAuditLogEntry> findAuditLogEntries()
   {
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSAuditLogEntry.class);
         return criteria.list();

   }

   /**
    * Configuration of this service, see
    * {@link #setConfig(IPSDesignObjectAuditConfig)}
    */
   private IPSDesignObjectAuditConfig m_config;
}
