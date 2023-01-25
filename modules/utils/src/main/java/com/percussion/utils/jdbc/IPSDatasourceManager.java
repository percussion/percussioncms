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

import com.percussion.utils.container.IPSHibernateDialectConfig;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;


/**
 * Manager interface for obtaining connections and connection detail objects.
 */
public interface IPSDatasourceManager
{
   /**
    * Get the connection detail for a datasource configuration.
    * 
    * @param info Specifies the datasource configuration to use, may be 
    * <code>null</code> to use the repository connection.
    * 
    * @return The detail, never <code>null</code>.
    * 
    * @throws SQLException If there is an error getting the detail from the 
    * connection.
    * @throws NamingException If there is an error looking up the datasource.
    * @throws PSMissingDatasourceConfigException if the info cannot be resolved
    * to a datasource configuration.
    */
   public PSConnectionDetail getConnectionDetail(IPSConnectionInfo info) 
      throws NamingException, SQLException;
   
   /**
    * Grab a connection from the datasource referenced
    * 
    * @param info the connection info, may be <code>null</code> to use the
    * repository connection.
    * 
    * @return the connection, never <code>null</code>
    * 
    * @throws NamingException if the datasource is not found
    * @throws SQLException on other database errors
    * @throws PSMissingDatasourceConfigException if the info cannot be resolved
    * to a datasource configuration. 
    */
   public Connection getDbConnection(IPSConnectionInfo info) 
      throws NamingException, SQLException;

   /**
    * Get a list of all active datasource names.
    * 
    * @return The list, never <code>null</code> or empty.
    */
   public List<String> getDatasources();

   /**
    * Gets the name of the repository connection.
    * 
    * @return The name of the repository connection datasource, 
    * never <code>null</code> or empty.
    */
   public String getRepositoryDatasource();
   
   /**
    * Get the hibernate properties for the supplied info object, which includes:
    * <ul>
    * <li>hibernate.connection.datasource</li>
    * <li>hibernate.default_catalog</li>
    * <li>hibernate.default_schema</li>
    * <li>hibernate.dialect</li>
    * </ul>
    * as well as any other properties specified in the server's configuration.
    * 
    * @param info Specifies the datasource configuration to use, may be 
    * <code>null</code> to use the repository connection.
    * 
    * @return The properties, never <code>null</code>, will contain the 
    * datasource specific properties derived from the supplied connection info
    * as well as any other properties specified by the server's configuration. 
    * 
    * @throws NamingException If there is an error looking up the datasource.
    * @throws SQLException If there is an error obtaining the connection details 
    * for the specified datasource.
    */
   public Properties getHibernateProperties(IPSConnectionInfo info) throws NamingException, SQLException;
   
   public void setDefaultHibernateProperties(Properties properties);
  
   public IPSHibernateDialectConfig getDialectCfg();

   public void setDialectCfg(IPSHibernateDialectConfig dialectCfg);

   
}

