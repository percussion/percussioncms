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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigStatusMgr;
import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the crud operations of config status object. 
 * @author bjoginipally
 *
 */
@Transactional
public class PSConfigStatusMgr  implements IPSConfigStatusMgr
{

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#createConfigStatus(java.lang.String)
    */
   public PSConfigStatus createConfigStatus(String configName)
   {
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException("configName must not be blank");
      PSConfigStatus cs = new PSConfigStatus();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      int stid = gmgr.createId("CONFIG_STATUS_ID");
      cs.setStatusId(stid);
      cs.setConfigName(configName);
      return cs;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#saveConfigStatus(com.percussion.rx.config.data.PSConfigStatus)
    */
   public void saveConfigStatus(PSConfigStatus obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
      
       sessionFactory.getCurrentSession().saveOrUpdate(obj);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#loadConfigStatus(long)
    */
   public PSConfigStatus loadConfigStatus(long statusID)
   {
      // As per the convention added this method, there is not much need at this
      // moment to cache the status objects.

      PSConfigStatus cfgStatus = loadConfigStatusModifiable(statusID);

      return cfgStatus;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#loadConfigStatusModifiable(long)
    */
   public PSConfigStatus loadConfigStatusModifiable(long statusID)
   {
      PSConfigStatus cfgStatus = null;
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSConfigStatus.class);
         criteria.add(Restrictions.eq("statusId", statusID));
         cfgStatus = (PSConfigStatus) criteria.uniqueResult();

         if (cfgStatus == null)
         {
            String msg = "Failed to find config status for supplied " +
                  "status id ({0})";
            Object[] args = {statusID};
            throw new PSNotFoundException(MessageFormat.format(msg, args));
         }

      return cfgStatus;
   }
      
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#findConfigStatus(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<PSConfigStatus> findConfigStatus(String nameFilter)
   {
      if (StringUtils.isBlank(nameFilter))
         throw new IllegalArgumentException("nameFilter may not be null or empty string");
     
      List<PSConfigStatus> cfgStatusList = null;
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSConfigStatus.class);
         criteria.add(Restrictions.like("configName", nameFilter).ignoreCase());
         criteria.addOrder(Order.asc("configName"));
         criteria.addOrder(Order.desc("dateApplied"));
         cfgStatusList = criteria.list();

      return cfgStatusList;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#findLatestConfigStatus(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<PSConfigStatus> findLatestConfigStatus(String nameFilter)
   {
      if (StringUtils.isBlank(nameFilter))
         throw new IllegalArgumentException("nameFilter may not be null or empty string");
     
      List<PSConfigStatus> resultList = new ArrayList<PSConfigStatus>();
      List<PSConfigStatus> cfgStatusList = findConfigStatus(nameFilter);
      if(!cfgStatusList.isEmpty())
      {
         PSConfigStatus currPkg = cfgStatusList.get(0);
         resultList.add(currPkg);
         for (PSConfigStatus status : cfgStatusList)
         {
            if (!currPkg.getConfigName().equalsIgnoreCase(
                  status.getConfigName()))
            {
               resultList.add(status);
               currPkg = status;
            }
         }
      }
      return resultList;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#deleteConfigStatus(long)
    */
   public void deleteConfigStatus(long statusID)
   {
      PSConfigStatus cfgStatus = loadConfigStatusModifiable(statusID);
      sessionFactory.getCurrentSession().delete(cfgStatus);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#deleteConfigStatus(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public void deleteConfigStatus(String nameFilter)
   {
      if (StringUtils.isBlank(nameFilter))
         throw new IllegalArgumentException("nameFilter may not be null or empty string");

      Session sess = sessionFactory.getCurrentSession();

      sess.createCriteria(PSConfigStatus.class)
              .add(Restrictions.like("configName", nameFilter).ignoreCase())
              .addOrder(Order.asc("configName"))
              .addOrder(Order.asc("dateApplied"))
              .list().forEach(sess::delete);

   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#findLastSuccessfulConfigStatus(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public PSConfigStatus findLastSuccessfulConfigStatus(String configName)
   {
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException("configName may not be null or empty string");
      List<PSConfigStatus> cfgList = null;
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSConfigStatus.class);
         criteria.add(Restrictions.eq("configName", configName));
         criteria.add(Restrictions.eq("status", PSConfigStatus.ConfigStatus.SUCCESS));
         criteria.addOrder(Order.desc("dateApplied"));
         criteria.setMaxResults(1);
         cfgList = criteria.list();

      return cfgList.size()==0 ? null : cfgList.get(0);
   }
}

