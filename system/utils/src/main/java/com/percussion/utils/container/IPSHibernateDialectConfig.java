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

package com.percussion.utils.container;

import java.util.Map;

public interface IPSHibernateDialectConfig
{
   /**
    * Get a copy of the internal dialect map.  See {@link #setDialects(Map)}.
    * 
    * @return The map, never <code>null</code>.
    */
   public Map<String, String> getDialects();

   /**
    * Get the dialect class name mapped to the supplied JDBC driver name.
    * 
    * @param driverName The name of the JDBC driver, may not be 
    * <code>null</code> or empty.
    * 
    * @return The dialect, or <code>null</code> if no mapping is found.
    */
   public String getDialectClassName(String driverName);
   
   /**
    * Set the dialect class for a driver.  If dialect is already mapped to
    * the supplied driver, it is replaced by the new dialect value.
    * 
    * @param driverName The name of the JDBC driver, may not be 
    * <code>null</code> or empty.
    * @param dialectClassName The dialect class name, may not be 
    * <code>null</code> or empty.
    */
   public void setDialect(String driverName, String dialectClassName);

   /**
    * Set the dialect classes for multiple drivers.  All current mappings are 
    * cleared and replaced with the supplied dialects.
    * 
    * @param dialects Map of dialects where key is the driver name, and value
    * is the dialect class name, may not be <code>null</code>, and keys and 
    * values may not be <code>null</code> or empty.  See 
    * {@link #setDialect(String, String)} for more info.
    */
   public void setDialects(Map<String, String> dialects);
   
  
}
