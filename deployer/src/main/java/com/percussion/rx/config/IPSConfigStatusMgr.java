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
package com.percussion.rx.config;

import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.services.error.PSNotFoundException;

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
   public PSConfigStatus loadConfigStatus(long statusID) throws PSNotFoundException;

   /**
    * Loads the modifiable config status object.
    * 
    * @param statusID Loads the config status object of the supplied id.
    * @return The config status object never <code>null</code>.
    */
   public PSConfigStatus loadConfigStatusModifiable(long statusID) throws PSNotFoundException;

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
   public void deleteConfigStatus(long statusID) throws PSNotFoundException;

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
