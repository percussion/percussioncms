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
package com.percussion.services.schedule.impl;

import com.percussion.services.utils.general.PSServiceConfigurationBean;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.NamingException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Bean to create and configure a Quartz scheduler.
 * Always returns the same scheduler object.
 *
 * @see Scheduler
 * @author Andriy Palamarchuk
 */
public class PSSchedulerBean implements FactoryBean, InitializingBean
{
   // see base
   public void afterPropertiesSet() throws SchedulerException
   {
      validateBeanProperties();
      finishConfiguration();
      setConnectionProviderDatasourceManager();

      final StdSchedulerFactory factory = new StdSchedulerFactory();
      m_quartzProperties.putAll(getConfigurationBean().getQuartzProperties());
      factory.initialize(m_quartzProperties);      
      m_scheduler = factory.getScheduler();

      if (ms_log.isDebugEnabled())
         ms_log.debug("Quartz properties: " + m_quartzProperties);
   }
   
   /**
    * Shutsdown the quartz scheduler. This will be called from spring
    * via "destroy-method" XML attribute. See beans.xml.
    * 
    * @throws SchedulerException
    * @author adamgent
    * @author peterfrontiero
    */
   public void destroy() throws SchedulerException {
      if (m_scheduler != null)
          m_scheduler.shutdown();
   }

   /**
    * Initializes {@link PSRhythmyxConnectionProvider} with the current
    * datasource manager.
    */
   void setConnectionProviderDatasourceManager()
   {
      PSRhythmyxConnectionProvider.setDatasourceManager(m_datasourceManager);
   }

   /**
    * Finishes generating Quartz configuration.
    */
   private void finishConfiguration()
   {
      configureDriverDelegate();
      qualifyTableName();
   }

   /**
    * Sets the "tablePrefix" Quartz job store property, so that Quartz generates
    * SQL with fully qualified table names.
    */
   private void qualifyTableName()
   {
      final String s = m_quartzProperties.getProperty(TABLE_PREFIX_PROPERTY);
      if (StringUtils.isBlank(s))
      {
         throw new IllegalArgumentException("Quartz property "
               + TABLE_PREFIX_PROPERTY + " must have a non-empty value");
      }
      final String prefix = StringUtils.isBlank(s) ? "" : s;
      final PSConnectionDetail d = getConnectionDetail();
      final String qualifiedPrefix = PSSqlHelper.qualifyTableName(
            prefix, d.getDatabase(), d.getOrigin(), d.getDriver());
      m_quartzProperties.setProperty(TABLE_PREFIX_PROPERTY, qualifiedPrefix);
   }

   /**
    * Configures driver delegate property.
    * Quartz has a number of database driver delegates, which generate
    * database-specific SQL.
    */
   private void configureDriverDelegate()
   {
      final String driver = getConnectionDetail().getDriver();
      if (StringUtils.isBlank(driver)
            || !ms_delegates.containsKey(driver))
      {
         throw new IllegalArgumentException("Unrecognized database: \""
               + driver + "\"");
      }
      m_quartzProperties.put("org.quartz.jobStore.driverDelegateClass",
            ms_delegates.get(driver));
   }

   /**
    * Database connection detail for the repository data source.
    * @return the database connection detail. Not <code>null</code>.
    */
   private PSConnectionDetail getConnectionDetail()
   {
      try
      {
         return m_datasourceManager.getConnectionDetail(null);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      catch (SQLException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Throws IllegalArgumentException if incomplete data was specified
    * for the bean.
    */
   private void validateBeanProperties()
   {
      if (m_quartzProperties == null || m_quartzProperties.isEmpty())
      {
         throw new IllegalArgumentException(
               "Quartz properties were not specified.");
      }
      if (m_datasourceManager == null)
      {
         throw new IllegalArgumentException("Data source was not specified");
      }
   }

   /**
    * @see #ms_delegates
    * @return newly created Quartz delegates map.
    * Never <code>null</code> or empty.
    */
   private static Map<String, String> createDelegates()
   {
      final Map<String, String> delegates = new HashMap<>();
      final String msSqlDelegate = "org.quartz.impl.jdbcjobstore.MSSQLDelegate";
      final String oracleDelegate = "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";
      final String db2Delegate = "org.quartz.impl.jdbcjobstore.DB2v8Delegate";
      final String derbyDelegate = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
      final String mysqlDelegate = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";

      //@TODO: Fix me - this should not be hardcoded and should come from PSJDBCUtils
      delegates.put("jtds:sqlserver", msSqlDelegate);
      delegates.put("inetdae7", msSqlDelegate);
      delegates.put("sqlserver", msSqlDelegate);
      delegates.put("oracle:thin", oracleDelegate);
      delegates.put("db2", db2Delegate);
      delegates.put("derby", derbyDelegate);
      delegates.put("mysql", mysqlDelegate);

      return Collections.unmodifiableMap(delegates);
   }

   /**
    * The Quartz properties to be passed to the scheduler factory.
    * Must be specified in the Spring bean configuration.
    * @param quartzProperties the properties to set.
    * Can't be <code>null</code> or empty
    * (validated during {@link #afterPropertiesSet()}).
    */
   public void setQuartzProperties(Properties quartzProperties)
   {
      m_quartzProperties = quartzProperties;
   }

   public Properties getQuartzProperties()
   {
      return m_quartzProperties;
   }

   // see base
   public Object getObject()
   {
      return m_scheduler;
   }

   // see base
   public Class<?> getObjectType()
   {
      return Scheduler.class;
   }

   /**
    * Returns <code>true</code>. Returns the same scheduler factory object.
    * {@inheritDoc}
    */
   public boolean isSingleton()
   {
      return true;
   }

   /**
    * The datasource manager to use for providing database connections
    * during Quartz initialization.
    * Must be specified in the Spring bean configuration.
    * @param datasourceManager the datasource manager to set.
    * Not <code>null</code>.
    */
   public void setDatasourceManager(IPSDatasourceManager datasourceManager)
   {
      m_datasourceManager = datasourceManager;
   }
   
   /**
    * @return the configurationBean
    */
   public PSServiceConfigurationBean getConfigurationBean()
   {
      return m_configurationBean;
   }

   /**
    * @param configurationBean the configurationBean to set
    */
   public void setConfigurationBean(
         PSServiceConfigurationBean configurationBean)
   {
      m_configurationBean = configurationBean;
   }
   
   /**
    * Service configuration bean. It is fired by Spring bean configuration.
    */
   private PSServiceConfigurationBean m_configurationBean;
   
   /**
    * A mappings between a JDBC driver name and a Quartz database delegate. 
    */
   private final static Map<String, String> ms_delegates = createDelegates();

   /**
    * Quartz table prefix property name. 
    */
   private static final String TABLE_PREFIX_PROPERTY =
         "org.quartz.jobStore.tablePrefix";

   /**
    * Not null after the bean is initialized.
    */
   private Scheduler m_scheduler;

   /**
    * @see #setQuartzProperties(Properties)
    */
   private Properties m_quartzProperties;

   /**
    * @see #setDatasourceManager(IPSDatasourceManager)
    */
   private IPSDatasourceManager m_datasourceManager;
   
   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSSchedulerBean.class);
}
