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
package com.percussion.metadata.dao.impl;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSSiteManageBean;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author erikserating
 *
 */
@PSSiteManageBean("metadataDao")
@Transactional
public class PSMetadataDao
{

    private static final Logger log = LogManager.getLogger(PSMetadataDao.class);

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


   public PSMetadata create(PSMetadata data)throws IPSGenericDao.SaveException
   {
      Session session = sessionFactory.getCurrentSession();
      try 
      {   
          session.save(data);
      }
      catch(HibernateException e)
      {   
          String msg = "create(PSMetadata data) database error " + e.getMessage(); 
          log.error(msg);
          throw new IPSGenericDao.SaveException(msg, e); 
      }
      finally
      {
          try 
          {
        	  session.flush();
          }
          catch (Exception e) 
          {
        	  log.error("Error releasing session in create: {}", e.getMessage());
          }
      }
      return data;      
   }
   
   public void delete(String key) throws IPSGenericDao.DeleteException, IPSGenericDao.LoadException {
      PSMetadata data = find(key);
      if (data == null)
      {
         log.warn("delete(String key) Attempted to delete non-existant metadata entry.");
         return;
      }
      delete(data);      
   }
   
   public void delete(PSMetadata data) throws IPSGenericDao.DeleteException
   {      
      Session session = sessionFactory.getCurrentSession();
      
      try
      {         
         session.delete(data);
      }
      catch (HibernateException e)
      {
         String msg = "delete(PSMetadata data) database error " + e.getMessage(); 
         log.error(msg);
         throw new IPSGenericDao.DeleteException(msg, e);  
      }
      finally
      {
         try 
         {
        	 session.flush();

         }
         catch (Exception e) 
         {
        	 log.error("Error releasing session in delete: {} " , e.getMessage());
         }
         
      }      
   }
   
   public PSMetadata save(PSMetadata data) throws IPSGenericDao.SaveException
   {
      String emsg; 
      Session session = sessionFactory.getCurrentSession();
      try 
      {   
          String key = data.getKey();
          PSMetadata existing = (PSMetadata) session.get(PSMetadata.class, key);
          if(existing == null)
          {
              emsg = "Attempt to modify non-existant record " + key; 
              log.error(emsg);
              throw new IPSGenericDao.SaveException(emsg);
          }
          existing.setData(data.getData());
          session.update(existing); 
           
      }
      catch(HibernateException he)
      {   
          emsg = "save(PSMetadata data) database error " + he.getMessage(); 
          log.error(emsg);
          throw new IPSGenericDao.SaveException(emsg, he); 
      }
      finally
      {
    	  try 
    	  {
    		  session.flush();

    	  }
    	  catch(Exception e) 
    	  {
    		  log.error("Error releasing session in save: {}", e.getMessage());
    	  } 
      }
      return data;
   }
   
   public PSMetadata find(String key) throws IPSGenericDao.LoadException {
      Session session = sessionFactory.getCurrentSession();
      
      try
      {
         PSMetadata data = (PSMetadata)session.get(PSMetadata.class, key);
         return data;         
      }
      catch (HibernateException e)
      {
         String msg = "find(String key) database error " + e.getMessage(); 
         log.error(msg);
         throw new IPSGenericDao.LoadException(msg, e);  
      }

   }
   
   @SuppressWarnings("unchecked")
   @Transactional
   public Collection<PSMetadata> findByPrefix(String prefix) throws IPSGenericDao.LoadException {
      String emsg;
      Session session = sessionFactory.getCurrentSession();
      Collection<PSMetadata> results = new ArrayList<>();
      try
      {
          results = session.createCriteria(PSMetadata.class)
             .add(Restrictions.ilike("key", prefix, MatchMode.START)).list();
          return results;
      }
      catch (HibernateException he)
      {
          emsg = "findByPrefix(String prefix) database error " + he.getMessage(); 
          log.error(emsg);
          throw new IPSGenericDao.LoadException(emsg, he); 
      }

   }   
 
}
