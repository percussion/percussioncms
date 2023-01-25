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

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Resolves an <code>IPSConnectionInfo</code> object to an 
 * {@link IPSDatasourceConfig} object.
 */
public interface IPSDatasourceResolver extends IPSBeanConfig
{
   /**
    * Given a connection info object, returns a datasource configuration.  Match
    * is made on the datasource name, case-insensitive.
    * 
    * @param info The info, may be <code>null</code> to indicate the repository
    * configuration.
    * 
    * @return The config, <code>null</code> if no matching config is found.
    */
   IPSDatasourceConfig resolveDatasource(IPSConnectionInfo info);
   
   /**
    * Get the name of the repository datasource configuration.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   String getRepositoryDatasource();
   
   /**
    * Sets the configurations to use to resolve datasources.
    * 
    * @param configs The configurations, may not be <code>null</code> or empty.
    */
   void setDatasourceConfigurations(List<IPSDatasourceConfig> configs);
   
   /**
    * Get the configurations to use to resolve datasources.
    * s
    * @return The configs, never <code>null</code>, only empty if 
    * {@link #setDatasourceConfigurations(List)} has never been called. 
    */
   List<IPSDatasourceConfig> getDatasourceConfigurations();
   
   /**
    * Sets the repository datasource name, used to resolve requests for the 
    * repository datasource.
    * 
    * @param datasourceName The name of one of the datasource configurations, 
    * may not be <code>null</code> or empty.  
    */
   void setRepositoryDatasource(String datasourceName);

   List<IPSDatasourceConfig>getDatasourceConfigurationsForConnection(String jndiName);

   IPSDatasourceConfig getDatasourceConfiguration(String configName);


    default IPSDatasourceConfig getRepositoryDatasourceConfig()
    {
        return getDatasourceConfiguration(getRepositoryDatasource());
    }

    default void addDatasourceConfig(String name,String dsName, String origin, String database)
    {
        getDatasourceConfigurations().stream()
                .filter(ds -> !ds.getName().equals(name))
                .collect(Collectors.toList())
                .add( new PSDatasourceConfig(name, dsName, origin,
                database));
    }

    Properties getProperties();

}

