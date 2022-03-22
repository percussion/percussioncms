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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

