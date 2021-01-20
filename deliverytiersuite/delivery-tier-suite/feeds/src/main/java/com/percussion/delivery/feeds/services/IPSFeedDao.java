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
