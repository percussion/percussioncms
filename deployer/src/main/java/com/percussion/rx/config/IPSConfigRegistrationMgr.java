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

/**
 * Config registration manager is used for registering and unregistering the
 * configurations. Local config file of registered configuration is monitored
 * for changes and on file change config service is called to apply
 * configuration of updated properties.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSConfigRegistrationMgr
{
   /**
    * Registers the configuration, gets the local configuration file from
    * config service and monitors it for file changes. On file change calls the
    * config service to apply the updated configuration.
    * 
    * @param configName name of the configuration to register, must not be
    * <code>null</code> or empty.
    */
   public void register(String configName);
   
   /**
    * Unregisters a previously registered configuration.
    * 
    * @param configName name of the configuration to unregister, must not be
    * <code>null</code> or empty.
    */
   public void unregister(String configName);
}
