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
