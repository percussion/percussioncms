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
package com.percussion.utils.jdbc;

import com.percussion.utils.spring.IPSBeanConfig;

/**
 * Interface to describe the configuration used to obtain and use a database
 * connection.
 */
public interface IPSDatasourceConfig extends IPSBeanConfig
{
   /**
    * Get the name used to reference this datasource configuration.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName();

   /**
    * Get the name of the JNDI datasource specified by this configuration. 
    * 
    * @return The datasource name, never <code>null</code> or empty.
    */
   public String getDataSource();

   /**
    * Get the name of the origin or schema specified by this configuration.
    * 
    * @return The origin or schema name, may be empty, never <code>null</code>.
    */
   public String getOrigin();
   
   /**
    * Get the name of the origin or schema specified by this configuration.
    * 
    * @return The database name, may be empty, never <code>null</code>.
    */
   public String getDatabase();

    public void copyFrom(IPSDatasourceConfig config);
}
