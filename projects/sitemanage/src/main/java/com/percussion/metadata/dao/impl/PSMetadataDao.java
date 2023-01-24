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
package com.percussion.metadata.dao.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * @author erikserating
 *
 */
@PSSiteManageBean("metadataDao")
@Transactional
public class PSMetadataDao implements com.percussion.metadata.dao.IPSMetadataDao {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);

    
   @Override
   public PSMetadata create(PSMetadata data)throws IPSGenericDao.SaveException
   {
      Session session = getSession();
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
        	  log.error("Error releasing session in create: {}", PSExceptionUtils.getMessageForLog(e));
          }
      }
      return data;      
   }
   
   @Override
   public void delete(String key) throws IPSGenericDao.DeleteException, IPSGenericDao.LoadException {
      PSMetadata data = find(key);
      if (data == null)
      {
         log.warn("delete(String key) Attempted to delete non-existant metadata entry.");
         return;
      }
      delete(data);      
   }
   
   @Override
   public void delete(PSMetadata data) throws IPSGenericDao.DeleteException
   {      
      Session session = getSession();
      
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
        	 log.error("Error releasing session in delete: {} " ,PSExceptionUtils.getMessageForLog(e));
         }
         
      }      
   }
   
   @Override
   public PSMetadata save(PSMetadata data) throws IPSGenericDao.SaveException
   {
      String emsg; 
      Session session = getSession();
      try 
      {   
          String key = data.getKey();
          PSMetadata existing =  session.get(PSMetadata.class, key);
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
    		  log.error("Error releasing session in save: {}",PSExceptionUtils.getMessageForLog(e));
    	  } 
      }
      return data;
   }
   
   @Override
   public PSMetadata find(String key) throws IPSGenericDao.LoadException {
      Session session = getSession();
      
      try
      {
        return  session.get(PSMetadata.class, key);

      }
      catch (HibernateException e)
      {
         String msg = "find(String key) database error " + e.getMessage(); 
         log.error(msg);
         throw new IPSGenericDao.LoadException(msg, e);  
      }

   }
   
   @Override
   @SuppressWarnings("unchecked")
   @Transactional
   public Collection<PSMetadata> findByPrefix(String prefix) throws IPSGenericDao.LoadException {
      String emsg;
      Session session = getSession();
      Collection<PSMetadata> results;
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
