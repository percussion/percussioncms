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
package com.percussion.metadata.service.impl;

import com.percussion.metadata.dao.IPSMetadataDao;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * @author erikserating
 *
 */
@PSSiteManageBean("metadataService")
@Transactional
public class PSMetadataService implements IPSMetadataService
{   

   /**
    * @param dao
    */
   @Autowired
   public PSMetadataService(IPSMetadataDao dao)
   {
      this.dao = dao;
   }

   /* (non-Javadoc)
    * @see com.percussion.metadata.service.IPSMetadataService#delete(java.lang.String)
    */
   public void delete(String key) throws IPSGenericDao.LoadException, IPSGenericDao.DeleteException {
      dao.delete(key);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.metadata.service.IPSMetadataService#deleteByPrefix(java.lang.String)
    */
   public void deleteByPrefix(String prefix) throws IPSGenericDao.DeleteException, IPSGenericDao.LoadException {
      Collection<PSMetadata> results = findByPrefix(prefix);
      if(!results.isEmpty())
      {
         for(PSMetadata result : results)
         {
            dao.delete(result);
         }
      }      
   }

   /* (non-Javadoc)
    * @see com.percussion.metadata.service.IPSMetadataService#find(java.lang.String)
    */
   public PSMetadata find(String key) throws IPSGenericDao.LoadException {
      return dao.find(key);
   }

   /* (non-Javadoc)
    * @see com.percussion.metadata.service.IPSMetadataService#findByPrefix(java.lang.String)
    */
   public Collection<PSMetadata> findByPrefix(String prefix) throws IPSGenericDao.LoadException {
     return dao.findByPrefix(prefix);
   }

   /* (non-Javadoc)
    * @see com.percussion.metadata.service.IPSMetadataService#save(com.percussion.metadata.data.PSMetadata)
    */
   public void save(PSMetadata data) throws IPSGenericDao.LoadException, IPSGenericDao.SaveException {
      PSMetadata existing = find(data.getKey());
      if(existing != null)
      {
         dao.save(data);
      }
      else
      {
         dao.create(data);
      }

   }
   
   private IPSMetadataDao dao;

}
