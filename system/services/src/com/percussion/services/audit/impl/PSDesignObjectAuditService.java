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
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
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
   
   public PSAuditLogEntry createAuditLogEntry()
   {
      PSAuditLogEntry entry  = new PSAuditLogEntry();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      entry.setGUID(guidMgr.createGuid(PSTypeEnum.INTERNAL));
      
      return entry;
   }

   public void saveAuditLogEntry(PSAuditLogEntry entry)
   {
      sessionFactory.getCurrentSession().save(entry);
   }

   @SuppressWarnings("unchecked")
   public void deleteAuditLogEntriesByDate(Date beforeDate)
   {
      if (beforeDate == null)
         throw new IllegalArgumentException("beforeDate may not be null");
      
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSAuditLogEntry.class);
         criteria.add(Restrictions.lt("auditDate", beforeDate));
         List<PSAuditLogEntry> entries = criteria.list();
         
         for (PSAuditLogEntry entry : entries)
         {
            session.delete(entry);
         }

   }

   public void saveAuditLogEntries(Collection<PSAuditLogEntry> entries)
   {
      Session session = sessionFactory.getCurrentSession();
      

         for (PSAuditLogEntry entry : entries)
         {
            session.save(entry);
         }

   }

   @SuppressWarnings("unchecked")
   public Collection<PSAuditLogEntry> findAuditLogEntries()
   {
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSAuditLogEntry.class);
         return criteria.list();

   }

   /**
    * Configuration of this service, see
    * {@link #setConfig(IPSDesignObjectAuditConfig)}
    */
   private IPSDesignObjectAuditConfig m_config;
}
