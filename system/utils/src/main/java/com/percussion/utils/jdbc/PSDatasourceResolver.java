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

import com.percussion.utils.container.IPSJdbcJettyDbmsDefConstants;

import com.percussion.utils.spring.IPSBeanConfig;
import com.percussion.utils.spring.PSSpringBeanUtils;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the {@link IPSDatasourceResolver} interface.
 */
public class PSDatasourceResolver implements IPSDatasourceResolver
{
   // see IPSDatasourceResolver
   public IPSDatasourceConfig resolveDatasource(IPSConnectionInfo info)
   {
      IPSDatasourceConfig dsConfig = null;
      
      // get default dsname from property            
      String dsName = getRepositoryDatasource();
      if (info != null && !StringUtils.isBlank(info.getDataSource()))
         dsName = info.getDataSource();

      Iterator<IPSDatasourceConfig> dsConfigs = getDatasourceConfigurations().iterator();
      while (dsConfigs.hasNext() && dsConfig == null)
      {
         IPSDatasourceConfig test = dsConfigs.next();
         if (test.getName().equalsIgnoreCase(dsName))
            dsConfig = test;
      }
      
      return dsConfig;      
   }

   // see IPSDatasourceResolver
   public List<IPSDatasourceConfig> getDatasources()
   {
      return new ArrayList<IPSDatasourceConfig>(m_configs);
   }
   
   /**
    * Sets the configurations to use to resolve datasources.
    * 
    * @param configs The configurations, may not be <code>null</code> or empty.
    */
   public void setDatasourceConfigurations(List<IPSDatasourceConfig> configs)
   {
      if (configs == null || configs.isEmpty())
         throw new IllegalArgumentException("configs may not be null or empty");

      m_configs = configs;
   }
   
   /**
    * Get the configurations to use to resolve datasources.
    * 
    * @return The configs, never <code>null</code>, only empty if 
    * {@link #setDatasourceConfigurations(List)} has never been called. 
    */
   public List<IPSDatasourceConfig> getDatasourceConfigurations()
   {
      return m_configs;
   }
   
   /**
    * Gets the specified database configuration. 
    * @param name the name of the database configuration. It may not be empty.
    * @return the database configuration, it may be <code>null</code> if cannot find one with the specified name.
    */
   @Override
   public IPSDatasourceConfig getDatasourceConfiguration(String name)
   {
       return m_configs.stream()
               .filter(config -> config.getName().equals(name))
               .findFirst()
               .orElse(null);
   }

   @Override
    public List<IPSDatasourceConfig>getDatasourceConfigurationsForConnection(String jndiName)
    {
        return m_configs.stream()
                .filter(config -> config.getDataSource().equals(jndiName))
                .collect(Collectors.toList());
    }

   
   /**
    * Get the repository datasource name, used to resolve requests for the 
    * repository datasource.
    * 
    * @return The name, never <code>null</code>, only empty if 
    * {@link #setRepositoryDatasource(String)} has never been called.
    */
   public String getRepositoryDatasource()
   {
      return  m_repositoryDatasource;
   }
   
   /**
    * Sets the repository datasource name, used to resolve requests for the 
    * repository datasource.
    * 
    * @param datasourceName The name of one of the datasource configurations, 
    * may not be <code>null</code> or empty.  
    */
   public void setRepositoryDatasource(String datasourceName)
   {
      if (StringUtils.isBlank(datasourceName))
         throw new IllegalArgumentException("datasourceName may not be null or empty");
      
      m_repositoryDatasource = datasourceName;
   }
   
   // see IPSBeanConfig   
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = PSSpringBeanUtils.createBeanRootElement(this, doc);

      PSSpringBeanUtils.addBeanProperty(root, REPOSITORY_PROP_NAME, getRepositoryDatasource());
      
      PSSpringBeanUtils.addBeanProperty(root, DS_CONFIGS_PROP_NAME, getDatasourceConfigurations());
      
      return root;
   }

   // see IPSBeanConfig
   public void fromXml(Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      try
      {
         PSSpringBeanUtils.validateBeanRootElement(getBeanName(), getClassName(), source);
      }
      catch (PSInvalidXmlException e)
      {
         // Try to validate using 6.0 version of the datasource resolver bean
         // name in the case of a 6.0 -> 6.1 upgrade
         PSSpringBeanUtils.validateBeanRootElement(DATASOURCE_RESOLVER_NAME_60, getClassName(), source);
      }
      
      Element propEl = PSSpringBeanUtils.getNextPropertyElement(source, null, REPOSITORY_PROP_NAME);
      m_repositoryDatasource = PSSpringBeanUtils.getBeanPropertyValue(propEl, false);
      propEl = PSSpringBeanUtils.getNextPropertyElement(source, propEl, DS_CONFIGS_PROP_NAME);
      List<IPSBeanConfig> configs = PSSpringBeanUtils.getBeanPropertyValueList(propEl);
      m_configs = new ArrayList<IPSDatasourceConfig>();
      for (IPSBeanConfig config : configs)
      {
         m_configs.add((PSDatasourceConfig) config);
      }
   }

   public Properties getProperties()
   {
      Properties props = new Properties();
      int index = 1;
      for (IPSDatasourceConfig config : m_configs)
      {
         String indexedPrefix = IPSJdbcJettyDbmsDefConstants.JETTY_CONN_PREFIX + "." + index + ".";
         props.put(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_DS_SUFFIX, config.getDataSource());
         props.put(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_NAME_SUFFIX, config.getName());
         props.put(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_SCHEMA_SUFFIX, config.getOrigin());
         props.put(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_DB_SUFFIX, config.getDatabase());

         if (config.getName().equals(m_repositoryDatasource))
         {
            props.put(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_DEFAULT_SUFFIX, "Y");
         }

         index++;
      }
      return props;
   }

   public void setProperties(Properties props)
   {
      List<IPSDatasourceConfig> new_configs = new ArrayList<>();

      String pattern = IPSJdbcJettyDbmsDefConstants.JETTY_CONN_PREFIX + "\\.(\\d+)\\."
            + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_NAME_SUFFIX;
      Pattern r = Pattern.compile(pattern);

      for (Entry<Object, Object> propEntry : props.entrySet())
      {
         Matcher m = r.matcher((String) propEntry.getKey());
         if (m.find())
         {
            int index = Integer.parseInt(m.group(1));
            String indexedPrefix = IPSJdbcJettyDbmsDefConstants.JETTY_CONN_PREFIX + "." + index + ".";

    
            
            String name = (String)propEntry.getValue();
            String dsName = (String) props.get(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_DS_SUFFIX);
            String origin = (String) props.get(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_SCHEMA_SUFFIX);
            String database = (String) props.get(indexedPrefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_DB_SUFFIX);
            String isDefault = (String) props.get(indexedPrefix
                  + IPSJdbcJettyDbmsDefConstants.JETTY_CONN_DEFAULT_SUFFIX);
            PSDatasourceConfig config = new PSDatasourceConfig(name, dsName, origin, database);
            
            if (StringUtils.equalsIgnoreCase(isDefault, "Y"))
            {
               m_repositoryDatasource = name;
            }
            new_configs.add(config);

         }
      }
      m_configs = new_configs;
   }

   // see IPSBeanConfig
   public String getBeanName()
   {
      return DATASOURCE_RESOLVER_NAME;
   }

   // see IPSBeanConfig
   public String getClassName()
   {
      return getClass().getName();
   }
   
   /**
    * The default datasource name, never <code>null</code>, empty until set by
    * {@link #setRepositoryDatasource(String)}, never <code>null</code> or empty
    * after that.
    */
   private String m_repositoryDatasource = null;

   /**
    * List of datasource configurations, never <code>null</code>, empty until 
    * first call to {@link #setDatasourceConfigurations(List)}, never 
    * <code>null</code> or empty after that.
    */
   private List<IPSDatasourceConfig> m_configs = null;
   
   // private XML constants
   private static final String REPOSITORY_PROP_NAME = "repositoryDatasource";

   private static final String DS_CONFIGS_PROP_NAME = "datasourceConfigurations";

   /**
    * The name of the bean that defines the datasource resolver in 6.0.  Used
    * by the Installer for 6.0 -> 6.X upgrades.
    */
   public static final String DATASOURCE_RESOLVER_NAME_60 = "datasourceResolver";

   /**
    * The name of the bean that defines the datasource resolver.
    */
   public static final String DATASOURCE_RESOLVER_NAME = "sys_datasourceResolver";

    @Override
    public String toString() {
        return "PSDatasourceResolver{" +
                "m_repositoryDatasource='" + m_repositoryDatasource + '\'' +
                ", m_configs=" + m_configs +
                '}';
    }
}
