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
package com.percussion.services.datasource.impl;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.utils.container.IPSHibernateDialectConfig;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.PSStaticContainerUtils;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.jdbc.PSMissingDatasourceConfigException;
import com.percussion.utils.jndi.PSJndiObjectLocator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the service interface.
 */
public class PSDatasourceManager implements IPSDatasourceManager
{

   private Properties defaultHibernateProperties = null;
   private IPSHibernateDialectConfig m_dialectCfg = null;
   
   public IPSHibernateDialectConfig getDialectCfg()
   {
      return m_dialectCfg;
   }

   public void setDialectCfg(IPSHibernateDialectConfig dialectCfg)
   {
      m_dialectCfg = dialectCfg;
   }


   protected Properties getProperties(File propertyFile){
      return PSStaticContainerUtils.getProperties(propertyFile);
   }

   
   // see IPSDatasourceManager
   public PSConnectionDetail getConnectionDetail(IPSConnectionInfo info)
      throws NamingException, SQLException
   {
      IPSDatasourceConfig dsConfig = PSContainerUtilsFactory.getInstance().getDatasourceResolver().resolveDatasource(info);

     String dsName = dsConfig.getDataSource();
     String url = getConnectionUrl(dsName);
     String driver = PSJdbcUtils.getDriverFromUrl(url);
     String database = dsConfig.getDatabase();

     return new PSConnectionDetail(dsName, driver, database,
        dsConfig.getOrigin(), url);

   }

   protected String getConnectionUrl(String dsName) throws NamingException, SQLException{
    Connection conn = null;
      try
      {
         conn = getDbConnection(dsName);
         
         return  conn.getMetaData().getURL(); 
         
         }
      finally
      {
         if (conn != null)
         {
            try {conn.close();} catch (Exception e) {}
         }
      }
   }
   // see IPSDatasourceManager
   public Connection getDbConnection(IPSConnectionInfo info)
      throws NamingException, SQLException
   {
      IPSDatasourceConfig dsConfig = resolveDatasource(info);      
      String dsName = dsConfig.getDataSource();
      Connection conn = getDbConnection(dsName);
      String dbName = dsConfig.getDatabase();
      
      if (!StringUtils.isBlank(dbName))
      {
         if (!dbName.equals(conn.getCatalog()))
            conn.setCatalog(dbName);
      }
      
      return conn;
   }


   // see IPSDatasourceManager
   public List<String> getDatasources()
   {
      List<String> dsList = new ArrayList<>();
      for (IPSDatasourceConfig config : getDatasourceResolver().getDatasourceConfigurations())
      {
         dsList.add(config.getName());
      }
      
      return dsList;
   }


    // see IPSDatasourceManager
   public String getRepositoryDatasource()
   {
      return getDatasourceResolver().getRepositoryDatasource();
   }


   /**
    * Grab a connection from the datasource referenced.
    * 
    * @param dsName The name of the datasource, assumed not <code>null</code> or 
    * empty.
    * 
    * @return A connection, never <code>null</code>.
    * 
    * @throws NamingException if the datasource is not found
    * @throws SQLException on other database errors
    */
   private  Connection getDbConnection(String dsName) 
      throws NamingException, SQLException
   {
      DataSource ds = getDatasource(dsName);
      // clears interrupted if it was set.  workaround https://sourceforge.net/p/jboss/bugs/2224/
      boolean isInterrupted = Thread.interrupted();
      Connection con = null;
      try {
         con = ds.getConnection();
      } finally
      {
        if (isInterrupted) 
           Thread.currentThread().interrupt(); 
      }
      return con;
   }   

   /**
    * Lookup the specified datasource.
    * 
    * @param dsName The datasource name, assumed not <code>null</code> or empty.
    * 
    * @return The datasource, never <code>null</code>.
    * @throws NamingException if the datasource cannot be resolved.
    */
   private  DataSource getDatasource(String dsName) throws NamingException
   {
      PSJndiObjectLocator loc = new PSJndiObjectLocator(dsName);
      DataSource ds = loc.lookupDataSource();
      return ds;
   }
   
   /**
    * Resolve the connection info to a datasource config
    * 
    * @param info The info to resolve, may be <code>null</code>.
    * 
    * @return The config, never <code>null</code>.
    * 
    * @throws PSMissingDatasourceConfigException if the info cannot be resolved.
    */
   private IPSDatasourceConfig resolveDatasource(IPSConnectionInfo info) 
      throws PSMissingDatasourceConfigException
   {
      
      IPSDatasourceConfig dsConfig = PSContainerUtilsFactory.getInstance().getDatasourceResolver().resolveDatasource(
         info);
      if (dsConfig == null)
      {
         String dsName = "";
         if (info != null && info.getDataSource() != null)
            dsName = info.getDataSource();
         throw new PSMissingDatasourceConfigException(dsName);
      }
      
      return dsConfig;
   }   
   
   /*
    * (non-Javadoc)
    * @see com.percussion.utils.jdbc.IPSDatasourceManager#getHibernateSession()
    */
   public Session getHibernateSession()
   {
      SessionFactory fact =
         (SessionFactory) PSBaseServiceLocator.getBean("sys_sessionFactory");
      return fact.openSession();
   }
   
   /**
    * The resolver to use, <code>null</code> until first call to 
    * {@link #setDatasourceResolver(IPSDatasourceResolver)}, never 
    * <code>null</code> after that.
    */
   private static IPSDatasourceResolver m_datasourceResolver;

   private IPSDatasourceResolver getDatasourceResolver()
   {
       if (m_datasourceResolver==null)
       {
           synchronized (this)
           {
               if (m_datasourceResolver==null) {
                   m_datasourceResolver = PSContainerUtilsFactory.getInstance().getDatasourceResolver();
               }
           }
       }
       return m_datasourceResolver;
   }


   /**
    * Get the hibernate properties for the supplied info object. See
    * {@link IPSDatasourceManager#getHibernateProperties(IPSConnectionInfo)} for
    * details.
    * 
    * @param info Specifies the datasource configuration to use, may be
    *           <code>null</code> to use the repository datasource.
    * 
    * @return The properties, never <code>null</code>, will contain the
    *         datasource specific properties derived from the supplied
    *         connection info as well as any other properties specified by the
    *         server's configuration.
    * 
    * @throws IllegalStateException if
    *            {@link #setDatasourceManager(IPSDatasourceManager)} has not
    *            been called (this is normally called when the spring framework
    *            is initialized).
    * @throws NamingException If there is an error looking up the datasource.
    * @throws SQLException If there is an error obtaining the connection details
    *            for the specified datasource.
    */
   public Properties getHibernateProperties(IPSConnectionInfo info) throws NamingException, SQLException
   {
      
      Properties props = new Properties();
      props.putAll(defaultHibernateProperties);

      PSConnectionDetail connDetail = getConnectionDetail(info);

      // need to add the prefix to the datasource name
      String dsName = connDetail.getDatasourceName();
      String jndiPrefix = PSJndiObjectLocator.getPrefix();
      if (!StringUtils.isBlank(jndiPrefix))
         dsName = jndiPrefix + dsName;
      props.setProperty("hibernate.connection.datasource", dsName);

      String catalog = connDetail.getDatabase();
      if (!StringUtils.isBlank(catalog))
         props.setProperty("hibernate.default_catalog", catalog);

      String origin = connDetail.getOrigin();
      if (!StringUtils.isBlank(origin))
         props.setProperty("hibernate.default_schema", origin);

      String dialect = m_dialectCfg.getDialectClassName(connDetail.getDriver());
      if (dialect == null)
         throw new RuntimeException(
               "Cannot determine Hibernate SQL dialect for driver: "
                     + connDetail.getDriver());
      props.setProperty("hibernate.dialect", dialect);

      // for DB2, set the transaction isolation level to read uncommitted
      if (connDetail.getDriver().equalsIgnoreCase(PSJdbcUtils.DB2))
         props.setProperty("hibernate.connection.isolation",
               PSJdbcUtils.TRANSACTION_READ_UNCOMMITTED_VALUE);

      return props;
   }

   // see IPSDatasourceManager
   public Properties getDefaultHibernateProperties(Properties properties) 
   {
      return defaultHibernateProperties;
   }

   public void setDefaultHibernateProperties(Properties properties)
   {
      defaultHibernateProperties = properties;
   }

}

