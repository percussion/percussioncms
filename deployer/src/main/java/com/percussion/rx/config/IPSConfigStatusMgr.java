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
package com.percussion.rx.config;

import com.percussion.rx.config.data.PSConfigStatus;

import java.util.List;

/**
 * Class to manage the crud and catalog operations for config status object.
 */
public interface IPSConfigStatusMgr
{
   /**
    * Creates the config status object with default values for the given config
    * name.
    * 
    * @param configName Name of the configuration, must not be blank.
    * @return PSConfigStatus The config status object never <code>null</code>.
    */
   public PSConfigStatus createConfigStatus(String configName);

   /**
    * Saves or updates the given config status object.
    * 
    * @param obj must not be <code>null</code>.
    */
   public void saveConfigStatus(PSConfigStatus obj);

   /**
    * Loads config status object of the given status id.
    * 
    * @param statusID Loads the config status object of the supplied id.
    * @return The config status object never <code>null</code>.
    */
   public PSConfigStatus loadConfigStatus(long statusID);

   /**
    * Loads the modifiable config status object.
    * 
    * @param statusID Loads the config status object of the supplied id.
    * @return The config status object never <code>null</code>.
    */
   public PSConfigStatus loadConfigStatusModifiable(long statusID);

   /**
    * Find the objects whose name matches the supplied name, case-insensitive.
    * 
    * @param nameFilter The pattern that identifies those objects you want
    * returned. SQL-like wildcards (%) may be used. Never <code>null</code> or
    * empty.
    * 
    * @return All PkgInfo objects whose names match the supplied filter
    * (case-insensitive.) A <code>List</code> is returned which is sorted
    * first by name (ascending) and then by install date (descending). Never
    * <code>null</code>, may be empty.
    */
   public List<PSConfigStatus> findConfigStatus(String nameFilter);

   /**
    * Find the latest PSConfigStatus objects whose name matches the supplied
    * filter.
    * 
    * @param nameFilter The pattern that identifies those objects you want
    * returned. SQL-like wildcards (%) may be used. Never <code>null</code> or
    * empty.
    * 
    * @return The latest of each set of PSConfigStatus objects whose name
    * matches the supplied filter (case-insensitive.) A <code>List</code> is
    * returned which is sorted first by name (ascending) and then by date
    * applied(descending). Never <code>null</code>, may be empty.
    */
   public List<PSConfigStatus> findLatestConfigStatus(String nameFilter);

   /**
    * Deletes the config status entry with the given status id.
    * 
    * @param statusID The status id whos entry needs to be deleted.
    */
   public void deleteConfigStatus(long statusID);

   /**
    * Deletes all the status entries that matches the given name filter.
    * 
    * @param nameFilter must not be <code>null</code>. SQL-like wildcards (%)
    * may be used. Never <code>null</code> or empty.
    */
   public void deleteConfigStatus(String nameFilter);

   /**
    * Find the last successful PSConfigStatus object whose name matches the
    * supplied name.
    * 
    * @param configName The name of the configuration for which the last
    * successful configuration is needed.
    * 
    * @return The last successful configuration of the supplied configuration
    * name, may be <code>null</code> if no successful configuration found.
    */
   public PSConfigStatus findLastSuccessfulConfigStatus(String configName);

}
