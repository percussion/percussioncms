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
package com.percussion.delivery.feeds.services;

import com.percussion.delivery.feeds.data.IPSFeedDescriptor;

import java.util.List;

/**
 * The data access layer for feeds, abstracted out so that we can create different implmentations
 * for very different types of repositories, ie, rdbms, bigtable, amazon data store....
 * @author erikserating
 *
 */
public interface IPSFeedDao
{
   /**
    * Saves/updates a list of descriptors.
    * @param descriptors list of all descriptors to be saved, cannot
    * be <code>null</code>, may be empty in which case nothing will be done.
    */
   public void saveDescriptors(List<IPSFeedDescriptor> descriptors);
   
   /**
    * Retrieve all existing feed descriptors.
    * @return List of all existing feed descriptors, never <code>null</code>,
    * may be empty.
    */
   public List<IPSFeedDescriptor> findAll();
   
   /**
    * Find a descriptor by name and site.
    * @param name the feed name, cannot be <code>null</code> or empty.
    * @param site the feed site, cannot be <code>null</code> or empty.
    * @return the retrieved descriptor or </code>null</code> if not found.
    */
   public IPSFeedDescriptor find(String name, String site);
   
   /**
    * Find all descriptors by site.
    * @param site the feed site, cannot be <code>null</code> or empty.
    * @return List of all existing feed descriptors for the site, never <code>null</code>,
    * may be empty.
    */
   public List<IPSFeedDescriptor> findBySite(String site);
   
   /**
    * Deletes all descriptors as specified in the passed in list.
    * @param descriptors cannot be <code>null</code>, may be empty.
    */
   public void deleteDescriptors(List<IPSFeedDescriptor> descriptors);
  
   /**
    * Save the connection info to access the meta data service.
    * @param url
    * @param user
    * @param pass
    * @param encrypted
    */
   public void saveConnectionInfo(String url, String user, String pass, boolean encrypted);
   
   /**
    * 
    * @return the connection info, may be <code>null</code>.
    */
   public IPSConnectionInfo getConnectionInfo();
}
