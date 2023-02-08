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

import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;

/**
 * Interface to allow classes to listen for changes to package configurations.
 */
public interface IPSConfigChangeListener
{
   /**
    * Called to notify listeners when a configuration has been applied to a
    * package.
    * 
    * @param ids a set of IDs of the configured Design Objects, never
    * <code>null</code> or empty.
    * @param status The status of the package configuration, never
    * <code>null</code>.
    */
   public void configChanged(Collection<IPSGuid> ids, ConfigStatus status) throws PSNotFoundException;
   
   /**
    * Called to notify listeners before a configuration is applied to a package.
    * 
    * @param name The package name, never <code>null</code> or empty.
    */
   public void preConfiguration(String name) throws PSNotFoundException;
}
