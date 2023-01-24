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

package com.percussion.services.useritems.impl;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.useritems.IPSUserItemsDao;
import com.percussion.services.useritems.data.PSUserItem;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSBaseBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@PSBaseBean("sys_userItemsDao")
@Transactional
public class PSUserItemsDao implements IPSUserItemsDao
{
   private static final Logger log = LogManager.getLogger(PSUserItemsDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }
    
    /**
    * Constant for the key used to generate summary ids.
    */
   private static final String USER_ITEM_KEY = "PSX_USERITEM";
   
   private IPSGuidManager m_guidMgr;
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.userpages.IPSUserItemsDao#find(java.lang.String, int)
    */
   public PSUserItem find(String userName, int itemId)
   {
      PSUserItem userItem = null;
      if(StringUtils.isBlank(userName))
         return userItem;
      Session session = getSession();

          Query query = session.createQuery("from PSUserItem where itemId = :itemId and userName = :userName");
          query.setParameter("itemId", itemId);
          query.setParameter("userName", userName);

          @SuppressWarnings("unchecked")
         List<PSUserItem> userItems = query.list(); 
          if(!userItems.isEmpty())
             userItem = userItems.get(0);
          return userItem;

   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.userpages.IPSUserItemsDao#save(com.percussion.services.userpages.data.PSUserItem)
    */
   @Transactional
   public void save(PSUserItem userItem) throws IPSGenericDao.SaveException {
      Validate.notNull(userItem);

      if (userItem.getUserItemId() == -1)
      {
         userItem.setUserItemId(m_guidMgr.createId(USER_ITEM_KEY));
      }

      Session session = getSession();
      try
      {
          session.saveOrUpdate(userItem);
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

   /*
    * (non-Javadoc)
    * @see com.percussion.services.userpages.IPSUserItemsDao#find(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<PSUserItem> find(String userName)
   {
      List<PSUserItem> userItems = new ArrayList<>();
      if(StringUtils.isBlank(userName))
         return userItems;
      Session session = getSession();

          Query query = session.createQuery("from PSUserItem where userName = :userName");
          query.setParameter("userName", userName);

          userItems = query.list(); 
          return userItems;

   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.userpages.IPSUserItemsDao#find(int)
    */
   @SuppressWarnings("unchecked")
   public List<PSUserItem> find(int itemId)
   {
      List<PSUserItem> userItems;
      Session session = getSession();

          Query query = session.createQuery("from PSUserItem where itemId = :itemId");
          query.setParameter("itemId", itemId);
          userItems = query.list();
          return userItems;

   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.userpages.IPSUserItemsDao#delete(com.percussion.services.userpages.data.PSUserItem)
    */
   @Transactional
   public void delete(PSUserItem userItem)
   {
      Validate.notNull(userItem);
      Session session = getSession();
      try
      {
          session.delete(userItem);
      }
      catch (HibernateException e)
      {
          String msg = "Failed to delete user item: " + e.getMessage();
          log.error(msg);
      }
      finally
      {
          session.flush();
      }
   }

   public void setGuidManager(IPSGuidManager guidMgr)
   {
       m_guidMgr = guidMgr;
   }

}
