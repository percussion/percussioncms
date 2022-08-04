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

package com.percussion.services.audit.impl;

import com.percussion.services.audit.IPSDesignObjectAuditConfig;
import com.percussion.services.audit.IPSDesignObjectAuditService;
import com.percussion.services.audit.data.PSAuditLogEntry;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filestorage.data.PSBinary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

      CriteriaBuilder builder = getSession().getCriteriaBuilder();
      CriteriaQuery<PSAuditLogEntry> criteria = builder.createQuery(PSAuditLogEntry.class);
      Root<PSAuditLogEntry> critRoot = criteria.from(PSAuditLogEntry.class);
      criteria.where(builder.equal(critRoot.get("auditDate"),beforeDate));
      List<PSAuditLogEntry> entries = entityManager.createQuery(criteria).getResultList();

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
