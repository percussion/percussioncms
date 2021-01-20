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
