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

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Create connections using the correct mechanisms for objects that obey
 * the {@link com.percussion.utils.jdbc.IPSConnectionInfo} interface.  
 * {@link #createInstance(IPSDatasourceManager)} must be called before this
 * class can be used.
 */
public class PSConnectionHelper
{
   /**
    * Ctor - cannot instantiate
    */
   private PSConnectionHelper() 
   {
      super();
   }

   /**
    * Grab a connection from the datasource referenced
    * 
    * @param info the connection info, may be <code>null</code> to use the
    * repository connection.
    * 
    * @return the connection, never <code>null</code>
    * 
    * @throws IllegalStateException if 
    * {@link #createInstance(IPSDatasourceManager)} has not been called.
    * @throws NamingException if the datasource is not found
    * @throws SQLException on other database errors
    * @throws PSMissingDatasourceConfigException if the info cannot be resolved
    * to a datasource configuration. 
    */
   public static Connection getDbConnection(IPSConnectionInfo info) 
   throws NamingException, SQLException
   {
      return getMgr().getDbConnection(info);
   }
   
   /**
    * Convenience method that calls 
    * {@link #getDbConnection(IPSConnectionInfo) getDbConnection(null)} to get 
    * the repository connection.
    */
   public static Connection getDbConnection() 
      throws NamingException, SQLException
   {
      return getDbConnection(null);
   }   
   
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
    * @throws IllegalStateException if 
    * {@link #createInstance(IPSDatasourceManager)} has not been called.
    * @throws PSMissingDatasourceConfigException if the info cannot be resolved
    * to a datasource configuration.
    */
   public static PSConnectionDetail getConnectionDetail(IPSConnectionInfo info) 
      throws NamingException, SQLException
   {
      return getMgr().getConnectionDetail(info);
   }
   
   /**
    * Convenience method that calls
    * {@link #getConnectionDetail(IPSConnectionInfo) getConnectionDetail(null)}
    * to get the repository connection detail.
    */
   public static PSConnectionDetail getConnectionDetail() 
      throws NamingException, SQLException
   {
      return getConnectionDetail(null);
   }
   
   /**
    * Get the datasource manager if one has been set.
    * 
    * @return The manager, never <code>null</code>.
    * 
    * @throws IllegalStateException if 
    * {@link #createInstance(IPSDatasourceManager)} has not been called.
    */
   private static IPSDatasourceManager getMgr()
   {
      if (ms_instance == null)
         throw new IllegalStateException("instance must be initialized");
      
      IPSDatasourceManager mgr = ms_instance.m_dsMgr;
      
      return mgr;
   }
   
   /**
    * Singleton accessor, used to obtain an instance to initialize the 
    * datasource manager.
    * 
    * @param dsMgr The datasource manager, may not be <code>null</code>.
    * @return The instance, never <code>null</code>.
    */
   public static synchronized PSConnectionHelper createInstance(
      IPSDatasourceManager dsMgr)
   {
      if (ms_instance != null)
         throw new IllegalStateException("Instance already initialized");
      
      if (dsMgr == null)
         throw new IllegalArgumentException("dsMgr may not be null");
      
      ms_instance = new PSConnectionHelper();
      ms_instance.m_dsMgr = dsMgr;
      
      return ms_instance;
   }
   
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
   public static Properties getHibernateProperties(IPSConnectionInfo info) 
      throws NamingException, SQLException
   {
      return getMgr().getHibernateProperties(info);
   }
   
   /**
    * Instance of this class, inialized by first call to 
    * {@link #createInstance(IPSDatasourceManager)}, never <code>null</code> 
    * after that.
    */
   private static PSConnectionHelper ms_instance = null;
  
   
   /**
    * Used to obtain connections and details. May be <code>null</code>. 
    */
   private IPSDatasourceManager m_dsMgr = null;
}
