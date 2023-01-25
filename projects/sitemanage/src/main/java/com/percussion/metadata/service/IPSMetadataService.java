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
