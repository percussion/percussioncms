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
package com.percussion.services.datasource;

import com.percussion.services.utils.hibernate.PSHibernateInterceptor;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.jndi.PSJndiObjectLocator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Extends Spring framework hibernate session factory to dynamically modify the
 * configuration before the session factory is created.
 */
public class PSSessionFactoryBean extends LocalSessionFactoryBean
{


   /**
    * Used to allow content repository to get configured instance. Can't obtain
    * through Spring, which returns a proxy object that doesn't have all the
    * methods!
    */
   private static PSSessionFactoryBean ms_instance = null;

   /**
    * Default ctor
    */
   public PSSessionFactoryBean() {
      synchronized (PSSessionFactoryBean.class)
      {
         ms_instance = this;
      }
   }

   /**
    * Get the static instance, but does not initialize - which is done through
    * the spring framework.
    * 
    * @return the static instance, never <code>null</code> after spring is
    *         initialized.
    */
   public static PSSessionFactoryBean getInstance()
   {
      return ms_instance;
   }
/*
   @Override
   protected Configuration newConfiguration() throws HibernateException
   {
      try
      {
         Configuration config = super.newConfiguration();
         config.getProperties().put("hibernate.physical_naming_strategy","com.percussion.services.datasource.PSSessionFactoryBean.UpcasingNamingStrategy");
         config.setInterceptor(new PSHibernateInterceptor(m_interceptEvents));
         return config;
      }
      catch (Exception e)
      {
         // any exception here is fatal
         throw new RuntimeException(
               "Failed to initialize the hibernate configuration: "
                     + e.getLocalizedMessage());
      }
   }
*/

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
   public Properties getHibernateProperties(IPSConnectionInfo info)
         throws NamingException, SQLException
   {
      if (m_dsMgr == null)
         throw new IllegalStateException("Datasource Manager must be set");

      Properties props = new Properties();
      props.putAll(m_hibernateProperties);

      PSConnectionDetail connDetail = m_dsMgr.getConnectionDetail(info);

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

      // for DB2 & Derby, set the transaction isolation level to read uncommitted
      if (connDetail.getDriver().equalsIgnoreCase(PSJdbcUtils.DB2)
            || connDetail.getDriver()
                  .equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER))
      {
         props.setProperty("hibernate.connection.isolation",
               PSJdbcUtils.TRANSACTION_READ_UNCOMMITTED_VALUE);
      }

      if(connDetail.getDriver()
              .equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER)){

         props.setProperty("hibernate.query.substitutions","true=T,false=F,yes=Y,no=N");
      }

      props.setProperty("hibernate.cache.use_query_cache", "true");

      return props;
   }

   @Override
   public void setHibernateProperties(Properties props)
   {
      m_hibernateProperties.clear();
      m_hibernateProperties.putAll(props);

      super.setHibernateProperties(props);
      super.setImplicitNamingStrategy(new ImplicitNamingStrategyLegacyHbmImpl());
      super.setPhysicalNamingStrategy(new UpperCaseNamingStrategy());

   }

   @Override
   public void afterPropertiesSet() throws IllegalArgumentException,
         HibernateException
   {
      try
      {
         m_hibernateProperties.putAll(getHibernateProperties(null));
         super.setHibernateProperties(m_hibernateProperties);
         // configuration created in super.afterPropertiesSet()
         super.afterPropertiesSet();
         this.getConfiguration().setInterceptor(new PSHibernateInterceptor(m_interceptEvents));
      }
      catch (Exception e)
      {
         // any exception here is fatal
         throw new RuntimeException(
               "Failed to initialize the hibernate configuration: "
                     + e.getLocalizedMessage(), e);
      }
   }

   /**
    * Sets the datasource manager to use override the configuration.
    * 
    * @param dsMgr The datasource manager, may not be <code>null</code>.
    */
   public void setDatasourceManager(IPSDatasourceManager dsMgr)
   {
      if (dsMgr == null)
         throw new IllegalArgumentException("dsMgr may not be null");

      m_dsMgr = dsMgr;
   }

   /**
    * The map of dialects to use when overriding the configuration.
    * 
    * @param dialects The dialect config. May not be <code>null</code> or
    *           empty.
    */
   public void setDialects(PSHibernateDialectConfig dialects)
   {
      if (dialects == null || dialects.getDialects().isEmpty())
         throw new IllegalArgumentException("dialects may not be null or emtpy");

      m_dialectCfg = dialects;
   }

   /**
    * The intercept events are used to add informative event handling for
    * debugging purposes.
    * 
    * @return Returns the interceptEvents.
    */
   public List<String> getInterceptEvents()
   {
      return m_interceptEvents;
   }

   /**
    * @param interceptEvents The interceptEvents to set.
    */
   public void setInterceptEvents(List<String> interceptEvents)
   {
      m_interceptEvents = interceptEvents;
   }

   /**
    * The datasource manager to use to override the new configuration,
    * initalized by the first call to
    * {@link #setDatasourceManager(IPSDatasourceManager)}, never
    * <code>null</code> after that.
    */
   private IPSDatasourceManager m_dsMgr;

   /**
    * Configuration of jdbc driver name to hibernate sql dialect, never
    * <code>null</code>, may be empty. Modified by calls to
    * {@link #setDialects(PSHibernateDialectConfig)}.
    */
   private PSHibernateDialectConfig m_dialectCfg = new PSHibernateDialectConfig();

   /**
    * The list of events will be intercepted and logged to the console for aid
    * in debugging issues
    */
   private List<String> m_interceptEvents = new ArrayList<String>();

   /**
    * Cached hibernate properties saved by overriden
    * {@link #setHibernateProperties(Properties)}, never <code>null</code>,
    * may be empty.
    */
   private Properties m_hibernateProperties = new Properties();
}
