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
package com.percussion.metadata.service.impl;

import com.percussion.metadata.dao.impl.PSMetadataDao;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * @author erikserating
 *
 */
@PSSiteManageBean("metadataService")
public class PSMetadataService implements IPSMetadataService
{   

   /**
    * @param dao
    */
   @Autowired
   public PSMetadataService(PSMetadataDao dao)
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
   
   private PSMetadataDao dao;

}
