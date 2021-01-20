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
package com.percussion.rx.audit;

import com.percussion.server.PSRequest;
import com.percussion.services.audit.IPSDesignObjectAuditConfig;
import com.percussion.services.audit.IPSDesignObjectAuditService;
import com.percussion.services.audit.PSDesignObjectAuditServiceLocator;
import com.percussion.services.audit.data.PSAuditLogEntry;
import com.percussion.services.audit.data.PSAuditLogEntry.AuditTypes;
import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;

/**
 * This class is intended to be configured with Spring AOP to write 
 * {@link PSAuditLogEntry} object whenever a design object is inserted, updated,
 * or removed from the repository.    
 */
public class PSDesignObjectAuditor
{
   /**
    * Perform the audit of the method call specified in the supplied joinpoint.
    * If auditing is notenabled, simply returns, otherwise performs the audit 
    * as follows:
    * <p>
    * Only audits method signatures that start with "save" or "delete".  
    * <p>
    * Only considers the first parameter of the method signature.  This argument
    * must be an instance of {@link IPSCatalogIdentifier} or a collection of
    * such instances.
    * <p>
    * For each object audited, a {@link PSAuditData} object is inserted in the
    * repository.  
    * 
    * @param joinPoint The joinpoint, never <code>null</code>.
    * 
    * @throws Throwable If there are any errors.
    */
   public void audit(JoinPoint joinPoint) throws Throwable
   {
      if (joinPoint == null)
         throw new IllegalArgumentException("joinPoint may not be null");
      
      Date auditDate = new Date();

      // make sure we have something to audit
      Object[] args = joinPoint.getArgs();
      if (args == null || args.length == 0)
         return;
      
      if (!isAuditingEnabled())
         return;
      
      String userName = (String) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_USER);
      if (StringUtils.isBlank(userName))
      {
         PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST);
         if (req != null)
            userName = req.getUserSession().getRealAuthenticatedUserEntry();
      }
      
      if (StringUtils.isBlank(userName))
         userName = "unknown";         
      
      IPSDesignObjectAuditService svc = 
         PSDesignObjectAuditServiceLocator.getAuditService();
      Collection<PSAuditLogEntry> entries = new ArrayList<PSAuditLogEntry>();
      
      // assume we are auditing the first argument since all service method
      // signatures follow this pattern
      String name = joinPoint.getSignature().getName();
      Collection<PSAuditData> auditData = createAuditData(
         name, args[0]);
      
      for (PSAuditData data : auditData)
      {
         PSAuditLogEntry entry = svc.createAuditLogEntry();
         entry.setAction(data.mi_auditAction);
         entry.setDate(auditDate);
         entry.setObjectGUID(data.mi_objectGuid);
         entry.setUserName(userName);
         entries.add(entry);
      }
      
      svc.saveAuditLogEntries(entries);
   }
   
   /**
    * Worker method of {@link #audit(JoinPoint)}, see that method for
    * a description of the auditing logic. This method determines the resulting
    * action(s) and guid(s) from the supplied argument and method name. This
    * method is not intended to be called directly, and is public to allow for
    * unit testing.
    * 
    * @param methodName The name of the method being audited, used to determine
    * the audited action, may be <code>null</code> in which case an empty
    * collection is returned.
    * @param arg The argument from which to extract one or more guids for the
    * audited action, may be <code>null</code> in which case an empty
    * collection is returned.
    * 
    * @return The resulting list of audit data, never <code>null</code>, may be 
    * empty.
    */
   @SuppressWarnings("unchecked")
   public Collection<PSAuditData> createAuditData(String methodName, 
      Object arg)
   {
      Collection<PSAuditData> dataList = new ArrayList<PSAuditData>();
      
      if (methodName == null)
         return dataList;
      
      AuditTypes type;
      if (methodName.startsWith("delete"))
         type = AuditTypes.DELETE;
      else if (methodName.startsWith("save"))
         type = AuditTypes.SAVE;
      else
      {
         // not concerned with this method call
         return dataList;
      }
      
      Collection argCollection = null;
      
      // convert single arg to collection for simpler processing
      if (arg instanceof IPSCatalogIdentifier || arg instanceof IPSGuid)
      {
         argCollection = new ArrayList<Object>();
         argCollection.add(arg);
      }
      
      if (arg instanceof Collection)
         argCollection = (Collection) arg;
      
      if (argCollection == null)
         return dataList;
      
      for (Object object : argCollection)
      {
         IPSGuid guid;
         
         if (object instanceof IPSGuid)
            guid = (IPSGuid) object;
         else if (object instanceof IPSCatalogIdentifier)
         {
            IPSCatalogIdentifier id = (IPSCatalogIdentifier) object;
            guid = id.getGUID();
         }
         else 
            continue;
         
         PSAuditData data = new PSAuditData();
         data.mi_objectGuid = guid;
         data.mi_auditAction = type;
         
         dataList.add(data);
      }
      
      return dataList;
   }

   /**
    * Check the audit service to determine if auditing is enabled, caching the 
    * result for use by future invocations of this method.
    * 
    * @return <code>true</code> if it is enabled, <code>false</code>
    * otherwise.
    */   
   private boolean isAuditingEnabled()
   {
      if (m_auditEnabled == null)
      {
         IPSDesignObjectAuditService svc = 
            PSDesignObjectAuditServiceLocator.getAuditService();
         IPSDesignObjectAuditConfig config = svc.getConfig();
         m_auditEnabled = Boolean.valueOf(config.isEnabled());
      }
      
      return m_auditEnabled;
   }

   /**
    * Saves the enabled setting from the audit config, <code>null</code> until
    * the config is checked for the first time, immutable after that.
    */
   Boolean m_auditEnabled = null;
   
   /**
    * Simple data structure to hold the guid and audit action, package access
    * for unit testing.
    */
   class PSAuditData
   {
      /**
       * The guid of the object modified by the method being audited, may be
       * <code>null</code>.
       */
      private IPSGuid mi_objectGuid;
      
      /**
       * The action being performed by the method being audited, may be 
       * <code>null</code>.
       */
      private AuditTypes mi_auditAction; 
      
      /**
       * Get the guid
       * 
       * @return The guid, may be <code>null</code>.
       */
      IPSGuid getGuid()
      {
         return mi_objectGuid;
      }
      
      /**
       * Get the action.
       * 
       * @return The action, may be <code>null</code>.
       */
      AuditTypes getAction()
      {
         return mi_auditAction;
      }
   }
}

