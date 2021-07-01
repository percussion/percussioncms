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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.legacy.security.deprecated.PSCryptographer;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.PSJndiDatasourceImpl;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class providing datasource resolution functionality.
 */
public abstract class PSBaseDSConverter
{
   /**
    * Construct the converter
    * 
    * @param configCtx Supplies the configurations required for conversion, may
    * not be <code>null</code>.
    * @param repositoryInfo The repository info, used to determine if creating
    * a datasource configuration that points to the repository.  May be 
    * <code>null</code> to assume the created configurations point to the 
    * repository.
    * @param updateConfig <code>true</code> to create required configurations
    * if they are not found in the configurations supplied by the
    * <code>configCtx</code>, <code>false</code> to throw an exception if
    * the required configurations are not found.
    */
   public PSBaseDSConverter(PSConfigurationCtx configCtx, 
      IPSRepositoryInfo repositoryInfo, boolean updateConfig)
   {
      if (configCtx == null)
         throw new IllegalArgumentException("configCtx may not be null");
      
      m_configCtx = configCtx;
      m_repositoryInfo = repositoryInfo;
      m_updateConfig = updateConfig;
   }
   
   /**
    * Determine if the config can be updated during conversion, or if required
    * configurations must already exist.
    * 
    * @return <code>true</code> if the config can be update, <code>false</code>
    * if configs should already be complete.
    */
   protected boolean shouldUpdateConfig()
   {
      return m_updateConfig;
   }
   
   /**
    * Locates the driver config from the server config in the config ctx
    * supplied during construction.  If no match is found, even if
    * {@link #shouldUpdateConfig()} is <code>true</code>, a new config is NOT
    * created.
    * 
    * @param driver The driver name to match, case-sensitive, may not be 
    * <code>null</code> or empty.
    * 
    * @return The matching config, or <code>null</code> if no match is found.
    */
   protected PSJdbcDriverConfig getDriver(String driver)
   {
      if (StringUtils.isBlank(driver))
         throw new IllegalArgumentException("driver may not be null or empty");
      
      PSJdbcDriverConfig driverConfig = null;
      
      PSLegacyServerConfig serverConfig = m_configCtx.getServerConfig();
      List<PSJdbcDriverConfig> configs = serverConfig.getJdbcDriverConfigs();
      for (PSJdbcDriverConfig test : configs)
      {
         if (test.getDriverName().equals(driver))
         {
            driverConfig = test;
            break;
         }
      }
      
      if (driverConfig == null)
      {
         getLogger().error("Failed to locate driver config for driver: " + 
            driver);
      }
      
      return driverConfig;
   }
   
   /**
    * Get a matching datasource configuration name from the supplied
    * information. If a match is not found and {@link #shouldUpdateConfig()}
    * returns <code>true</code>, a new one is created and added to the
    * configuration object supplied during construction.
    * 
    * @param ds The jndi datasource to match, assumed not <code>null</code>.
    * @param database The database name to match, case-insensitive, may be
    * <code>null</code> or empty.
    * @param origin The origin or schema to match, case-insensitive, may be
    * <code>null</code> or empty.
    * 
    * @return The datasource connection name, may be empty to signify the
    * repository, <code>null</code> if a match could not be found or created.
    */
   protected String getDatasourceName(IPSJndiDatasource ds, 
      String database, String origin)
   {
      if (ds == null)
         throw new IllegalArgumentException("ds may not be null");
      
      if (database == null)
         database = "";      
      if (origin == null)
         origin = "";




      PSDatasourceResolver resolver;


      Set<String> configNames = new HashSet<>();
       BaseContainerUtils containerUtils = PSContainerUtilsFactory.getInstance();
      List<IPSDatasourceConfig> configs = containerUtils.getDatasourceResolver().getDatasourceConfigurations();

      String repoDSName = containerUtils.getDatasourceResolver().getRepositoryDatasource();
      for (IPSDatasourceConfig config : configs)
      {
         if (config.getDataSource().equals(ds.getName()) && 
            config.getDatabase().equalsIgnoreCase(database) && 
            config.getOrigin().equalsIgnoreCase(origin))
               return config.getName().equals(repoDSName)?"":config.getName();

            configNames.add(config.getName());
      }

      String configName = null;
      if (m_updateConfig)
      {
         configName = StringUtils.substringAfter(ds.getName(), "jdbc/");
         configName = ensureUniqueName(configName, configNames);

         PSDatasourceConfig newConfig = new PSDatasourceConfig(configName,
            ds.getName(), origin, database); 
         configs.add(newConfig);
         
         // need to determine if it references the repository
         if (matchesRepository(ds, newConfig))
         {
             containerUtils.getDatasourceResolver().setRepositoryDatasource(configName);
            configName = "";
         }

         containerUtils.getDatasourceResolver().addDatasourceConfig(configName,
                  ds.getName(), origin, database);

         PSContainerUtilsFactory.getConfigurationContextInstance().save();

         getLogger().info("Created datatsource configuration with name: " + 
            configName);
      }
      
      if (configName == null)
      {
         getLogger().info(
            "No datasource configuration match found for JNDI datasource \""
               + ds.getName() + "\", database \"" + database
               + "\", and origin \"" + origin + "\"");
      }
      
      return configName;
   }

   /**
    * Get the matching jndi datasource from the JBoss configuration files.  If
    * a match is not found and {@link #shouldUpdateConfig()} returns 
    * <code>true</code>, a new one is created and added to the configuration
    * object supplied during construction.
    * 
    * @param driver The driver to match on, may not be <code>null</code>.
    * @param server The server name to match on, case-insensitive, may not be
    * <code>null</code> or empty.
    * 
    * @return The datasource, or <code>null</code> if no match is found and
    * one was not created.
    */
   protected IPSJndiDatasource getJndiDatasource(PSJdbcDriverConfig driver, 
      String server)
   {
      if (driver == null)
         throw new IllegalArgumentException("driver may not be null");
      if (StringUtils.isBlank(server))
         throw new IllegalArgumentException("server may not be null or empty");
      
      IPSJndiDatasource datasource = null;
      Set<String> dsNames = new HashSet<>(); // build set of names

      List<IPSJndiDatasource> dsList = m_configCtx.getJndiDatasources();
      for (IPSJndiDatasource testDs : dsList)
      {
         if (testDs.getDriverName().equals(driver.getDriverName()) && 
            testDs.getServer().equalsIgnoreCase(server))
         {
            datasource = testDs;
            break;
         }
         else
         {
            dsNames.add(testDs.getName());
         }
      }
      
      if (datasource == null && m_updateConfig)
      {
         // try to create one if we can locate a backend credential
         PSLegacyBackEndCredential cred = null;
         for (PSLegacyBackEndCredential testCred : 
            m_configCtx.getServerConfig().getBackEndCredentials())
         {
            if (testCred.getDriver().equals(driver.getDriverName()) && 
               testCred.getServer().equalsIgnoreCase(server))
            {
               cred = testCred;
               break;
            }
         }
         
         if (cred != null)
         {
            // found one, try to create unique datsource name from the alias
            String jndiName = "jdbc/" + StringUtils.deleteWhitespace(
               cred.getAlias());
            jndiName = ensureUniqueName(jndiName, dsNames);
            String uid = cred.getUserId();
            String pwd = PSCryptographer.decrypt(m_configCtx.getSecretKey(), 
               uid, cred.getPassword());
            datasource = new PSJndiDatasourceImpl(jndiName, driver.getDriverName(),
               driver.getClassName(), server, 
               uid, pwd);
            dsList.add(datasource);
            getLogger().info("Created JNDI datatsource with name: " + jndiName);
         }
         else
         {
            getLogger().info(
               "Could not create a JNDI datasource found for driver \""
                  + driver + "\" and server \"" + server
                  + "\": no backend credential match found");
         }
      }
      
      if (datasource == null)
      {
         getLogger().info("No JNDI datasource found for driver \"" + driver + 
            "\" and server \"" + server + "\"");
      }
      
      return datasource;
   }   
   
   /**
    * Resolves the supplied connection information to a datasource. If no
    * configuration is found and {@link #shouldUpdateConfig()} is
    * <code>true</code>, the required configurations will be created.
    * 
    * @param driverName The driver name to match or use, may not be
    * <code>null</code> or empty.
    * @param server The server name to match or use, may not be
    * <code>null</code> or empty.
    * @param database The database to match or use, may be <code>null</code>
    * or empty.
    * @param origin The origin to match or use, may be <code>null</code> or
    * empty.
    * 
    * @return The datasource name, <code>null</code> if it is the respository
    * datasource, never empty.
    * 
    * @throws PSUnknownNodeTypeException If the datasource name cannot be
    * resolved.
    */
   protected String resolveToDatasource(String driverName, String server, 
      String database, String origin) throws PSUnknownNodeTypeException
   {
      if (StringUtils.isBlank(driverName))
         throw new IllegalArgumentException(
            "driverName may not be null or empty");
      
      if (StringUtils.isBlank(server))
         throw new IllegalArgumentException("server may not be null or empty");
      
     // locate matching driver
      PSJdbcDriverConfig driver = getDriver(driverName);
      if (driver == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.NO_JDBC_DRIVER_CONFIG, driverName);
      }
      
      // locate matching datsource
      IPSJndiDatasource ds = getJndiDatasource(driver, server);
      if (ds == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.NO_JNDI_DATASOURCE, 
            new String[] {driver.getDriverName(), server});
      }
      
      // locate matching connection
      String dsName = getDatasourceName(ds, database, origin);
      if (dsName == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.NO_DATASOURCE_CONNECTION, 
            new String[] {ds.getName(), database, origin});
      }
      
      return StringUtils.isBlank(dsName) ? null : dsName;      
   }

   /**
    * Determine if the supplied datasource information matches the repository
    * info supplied during construction.
    * 
    * @param ds The Jndi datasource, assumed not <code>null</code>.
    * @param config The datasouce config, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if they match the repository or if no
    * repository info was supplied during construction, <code>false</code> 
    * otherwise.
    */
   private boolean matchesRepository(IPSJndiDatasource ds, 
      PSDatasourceConfig config)
   {
      boolean isMatch = m_repositoryInfo == null;
      
      if (!isMatch)
      {
         isMatch = ds.getDriverName().equals(m_repositoryInfo.getDriver()) &&
            ds.getServer().equalsIgnoreCase(m_repositoryInfo.getServer()) &&
            config.getOrigin().equalsIgnoreCase(m_repositoryInfo.getOrigin()) &&
            config.getDatabase().equalsIgnoreCase(
               m_repositoryInfo.getDatabase());
      }
      
      return isMatch;
   }   
   
   /**
    * Ensures a unique name from a set of existing names.
    * 
    * @param name The proposed name, assumed not <code>null</code> or empty.
    * @param curNames The current list of names, assumed not <code>null</code>.
    * 
    * @return The unique name, never <code>null</code> or empty.
    */
   private String ensureUniqueName(String name, Set<String> curNames)
   {
      String uniqueName = null;
      int count = 1;
      String suffix = "";
      while (uniqueName == null)
      {
         String test = name + suffix;
         if (!curNames.contains(test))
            uniqueName = test;
         else
         {
            suffix += String.valueOf(count);
            count++;
         }
      }
      
      return uniqueName;
   }
   
   /**
    * Get the logger to use.
    * 
    * @return The logger, never <code>null</code>.
    */
   protected static final Logger getLogger()
   {
      return ms_logger;
   }
   
   /**
    * The config ctx supplied in the ctor, never <code>null</code> after that.
    */
   protected PSConfigurationCtx m_configCtx;
   
   /**
    * The repository info supplied during construction, may be 
    * <code>null</code>.
    */
   private IPSRepositoryInfo m_repositoryInfo;
   
   /**
    * The flag passed during construction to determine if configs can be
    * updated during conversion. 
    */
   private boolean m_updateConfig;
   
   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger ms_logger = LogManager.getLogger(
      PSBaseDSConverter.class);
}
