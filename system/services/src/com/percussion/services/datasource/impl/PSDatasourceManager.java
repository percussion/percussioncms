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

   public PSDatasourceManager() {
      //setup default hibernate properties
      Properties props = new Properties();

      //backward compatibility
      props.put("hibernate.allow_update_outside_transaction","true");

      defaultHibernateProperties = props;
   }

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
      try(Connection conn = getDbConnection(dsName))
      {
         return  conn.getMetaData().getURL(); 
      }
   }

   /**
    *
    * @param info the connection info, may be <code>null</code> to use the
    * repository connection.
    *
    * @return a database connection.  The caller is responsible for releasing the connection.
    * @throws NamingException If a JNDI lookup error occurs
    * @throws SQLException If a SQL exception occurs
    */
   public Connection getDbConnection(IPSConnectionInfo info)
      throws NamingException, SQLException
   {
      IPSDatasourceConfig dsConfig = resolveDatasource(info);      
      String dsName = dsConfig.getDataSource();
      Connection conn = getDbConnection(dsName);
      String dbName = dsConfig.getDatabase();
      if(conn.getMetaData().getURL().contains("oracle")){
         conn = new PSOracleConnectionWrapper(conn);
      }
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
         return getDatasource(dsName).getConnection();
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
      return loc.lookupDataSource();
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
    * , never
    * <code>null</code> after that.
    */
   private static IPSDatasourceResolver m_datasourceResolver;

   private synchronized IPSDatasourceResolver getDatasourceResolver()
   {
       if (m_datasourceResolver==null)
       {
          m_datasourceResolver = PSContainerUtilsFactory.getInstance().getDatasourceResolver();
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
    *             has not
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

      if(info != null) {
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
      }
      return props;
   }

   // see IPSDatasourceManager
   public Properties getDefaultHibernateProperties()
   {
      return defaultHibernateProperties;
   }

   public void setDefaultHibernateProperties(Properties properties)
   {
      defaultHibernateProperties = properties;
   }

}

