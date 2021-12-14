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
