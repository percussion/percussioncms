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
package com.percussion.utils.jdbc;

import com.percussion.utils.spring.IPSBeanConfig;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

