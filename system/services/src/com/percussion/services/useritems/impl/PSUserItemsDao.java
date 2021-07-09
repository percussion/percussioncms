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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@PSBaseBean("sys_userItemsDao")
@Transactional
public class PSUserItemsDao implements IPSUserItemsDao
{
   private static final Logger log = LogManager.getLogger(PSUserItemsDao.class);

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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
      Session session = sessionFactory.getCurrentSession();

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
   public void save(PSUserItem userItem) throws IPSGenericDao.SaveException {
      Validate.notNull(userItem);

      if (userItem.getUserItemId() == -1)
      {
         userItem.setUserItemId(m_guidMgr.createId(USER_ITEM_KEY));
      }

      Session session = sessionFactory.getCurrentSession();
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
      Session session = sessionFactory.getCurrentSession();

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
      Session session = sessionFactory.getCurrentSession();

          Query query = session.createQuery("from PSUserItem where itemId = :itemId");
          query.setParameter("itemId", itemId);
          query.addQueryHint(QueryHints.CACHEABLE);
          userItems = query.list(); 
          return userItems;

   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.userpages.IPSUserItemsDao#delete(com.percussion.services.userpages.data.PSUserItem)
    */
   public void delete(PSUserItem userItem)
   {
      Validate.notNull(userItem);
      Session session = sessionFactory.getCurrentSession();
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
