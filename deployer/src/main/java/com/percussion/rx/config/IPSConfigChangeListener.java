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

import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
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
   public void configChanged(Collection<IPSGuid> ids, ConfigStatus status);
   
   /**
    * Called to notify listeners before a configuration is applied to a package.
    * 
    * @param name The package name, never <code>null</code> or empty.
    */
   public void preConfiguration(String name);
}
