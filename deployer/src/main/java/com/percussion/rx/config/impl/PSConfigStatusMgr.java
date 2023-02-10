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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigStatusMgr;
import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.PSBaseBean;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the crud operations of config status object. 
 * @author bjoginipally
 *
 */
@Transactional
@PSBaseBean("sys_configStatusMgr")
public class PSConfigStatusMgr  implements IPSConfigStatusMgr
{
   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
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
      
       getSession().saveOrUpdate(obj);
   }
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#loadConfigStatus(long)
    */
   public PSConfigStatus loadConfigStatus(long statusID) throws PSNotFoundException {
      // As per the convention added this method, there is not much need at this
      // moment to cache the status objects.

      return loadConfigStatusModifiable(statusID);

   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigStatusMgr#loadConfigStatusModifiable(long)
    */
   public PSConfigStatus loadConfigStatusModifiable(long statusID) throws PSNotFoundException {
      PSConfigStatus cfgStatus = null;
      Session session = getSession();

      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSConfigStatus> criteria = builder.createQuery(PSConfigStatus.class);
      Root<PSConfigStatus> critRoot = criteria.from(PSConfigStatus.class);
      criteria.where(builder.equal(critRoot.get("statusid"), statusID));
      cfgStatus = entityManager.createQuery(criteria).getSingleResult();

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
      Session session = getSession();

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
   public List<PSConfigStatus> findLatestConfigStatus(String nameFilter)
   {
      if (StringUtils.isBlank(nameFilter))
         throw new IllegalArgumentException("nameFilter may not be null or empty string");
     
      List<PSConfigStatus> resultList = new ArrayList<>();
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
   public void deleteConfigStatus(long statusID) throws PSNotFoundException {
      PSConfigStatus cfgStatus = loadConfigStatusModifiable(statusID);
      getSession().delete(cfgStatus);
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

      Session sess = getSession();

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
      List<PSConfigStatus> cfgList;
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSConfigStatus.class);
         criteria.add(Restrictions.eq("configName", configName));
         criteria.add(Restrictions.eq("status", PSConfigStatus.ConfigStatus.SUCCESS));
         criteria.addOrder(Order.desc("dateApplied"));
         criteria.setMaxResults(1);
         cfgList = criteria.list();

      return cfgList.size()==0 ? null : cfgList.get(0);
   }
}

