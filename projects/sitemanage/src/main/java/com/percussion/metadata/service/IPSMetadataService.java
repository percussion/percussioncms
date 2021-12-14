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
package com.percussion.metadata.service;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.IPSGenericDao;

import java.util.Collection;

/**
 * @author erikserating
 *
 */
public interface IPSMetadataService
{
   /**
    * Finds a metadata object from the repository based on the specified key.
    * @param key the unique key used to retrieve the metadata object. Cannot
    * be <code>null</code> or empty.
    * @return may be <code>null</code> if no matching entry was found in the
    * database.
    */
   public PSMetadata find(String key) throws IPSGenericDao.LoadException;
   
   /**
    * Locate all metadata objects by a key prefix. So to retrieve all of the objects with the
    * following keys: 'user.profile.john', 'user.profile.dave', use the prefix 'user.profile.'.
    * @param prefix the key prefix, cannot be <code>null</code> or empty.
    * @return a collection containing all of the located metadata objects, will
    * be empty if none were found. Never <code>null</code>.
    */
   public Collection<PSMetadata> findByPrefix(String prefix) throws IPSGenericDao.LoadException;
   
   /**
    * Saves the passed in metadata object to the repository, replacing any existing
    * entry that uses the same key or creating a new entry if one does not yet exist.
    * @param data the metadata object to be saved, cannot be <code>null</code>.
    */
   public void save(PSMetadata data) throws IPSGenericDao.LoadException, IPSGenericDao.SaveException;
   
   /**
    * Deletes the metadata object specified by the passed in key if it exists.
    * @param key the unique key used to delete the metadata object. Cannot
    * be <code>null</code> or empty. 
    */
   public void delete(String key) throws IPSGenericDao.LoadException, IPSGenericDao.DeleteException;
   
   /**
    * Deletes all metadata objects who's key starts with the specified prefix.
    * @param prefix the key prefix, cannot be <code>null</code> or empty.
    */
   public void deleteByPrefix(String prefix) throws IPSGenericDao.DeleteException, IPSGenericDao.LoadException;
   
}
