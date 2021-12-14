/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.design.objectstore.legacy;

import com.percussion.security.ToDoVulnerability;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.spring.PSSpringConfigurationTest;
import com.percussion.utils.testing.PSTestResourceUtils;
import com.percussion.utils.tools.PSBaseXmlConfigTest;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.io.File;
import java.util.List;

/**
 * Base class for component converter tests
 */
@ToDoVulnerability
public abstract class PSBaseConverterTest extends PSBaseXmlConfigTest
{

   /**
    * Constant for the test login config file.
    */
   public static final String TEST_LOGIN_CFG_FILE = 
      "/com/percussion/utils/jboss/login-config.xml";
   
   /**
    * Constant for the test ds config file.
    */
   public static final String TEST_DATASOURCE_FILE = 
      "/com/percussion/utils/jboss/rx-ds.xml";
   
   /**
    * Get the list of datsource configs from the supplied context.
    * 
    * @param ctx The context, assumed not <code>null</code>.
    * 
    * @return The configs, never <code>null</code>.
    * 
    * @throws PSInvalidXmlException If a configuration is bad.
    */
   protected List<IPSDatasourceConfig> getDatasourceConfigs(
      PSConfigurationCtx ctx) throws PSInvalidXmlException
   {
      PSDatasourceResolver resolver = getResolver(ctx);
      List<IPSDatasourceConfig> configs = resolver.getDatasourceConfigurations();
      return configs;
   }
   
   /**
    * Get the datasource resolver from the supplied context.
    * 
    * @param ctx The context, may not be <code>null</code>.
    * 
    * @return The resolver, never <code>null</code>.
    * 
    * @throws PSInvalidXmlException If a configuration is bad.
    */
   protected PSDatasourceResolver getResolver(
      PSConfigurationCtx ctx) throws PSInvalidXmlException
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      PSSpringConfiguration springConfig = ctx.getSpringConfig();
      PSDatasourceResolver resolver = 
         (PSDatasourceResolver) springConfig.getBean(
            PSSpringConfiguration.DS_RESOLVER_NAME);
      
      return resolver;
   }   
   
   /**
    * Copies default source config files and creates a locator
    * 
    * @return The locator, never <code>null</code>.
    * 
    * @throws Exception if there are any errors
    */
   protected IPSConfigFileLocator initFileLocator() throws Exception
   {

      return initFileLocator(
              PSTestResourceUtils.getFile(PSBaseConverterTest.class,PSLegacyServerConfigTest.TEST_LEGACY_CONFIG_FILE,null).getAbsolutePath(),
              PSTestResourceUtils.getFile(PSBaseConverterTest.class,PSSpringConfigurationTest.TEST_BEANS_FILE,null).getAbsolutePath(),
         PSTestResourceUtils.getFile(PSBaseConverterTest.class,TEST_DATASOURCE_FILE,null).getAbsolutePath(),
              PSTestResourceUtils.getFile(PSBaseConverterTest.class,TEST_LOGIN_CFG_FILE,null).getAbsolutePath());
   }

   /**
    * Copies source config files and creates a locator
    * 
    * @param serverFile the server config, never <code>null</code>.
    * @param springFile the spring config, never <code>null</code>.
    * @param jndiFile the jndi config, never <code>null</code>.
    * @param loginFile the login config, never <code>null</code>.
    * 
    * @return The locator, never <code>null</code>.
    * 
    * @throws Exception if there are any errors
    */
   protected IPSConfigFileLocator initFileLocator(String serverFile,
         String springFile, String jndiFile, String loginFile) throws Exception
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");
      if (springFile == null)
         throw new IllegalArgumentException("springFile may not be null");
      if (jndiFile == null)
         throw new IllegalArgumentException("jndiFile may not be null");
      if (loginFile == null)
         throw new IllegalArgumentException("loginFile may not be null");
      
      // copy source files
      File srcServerConfig = getTempXmlFile();
      File srcSpringConfig = getTempXmlFile();
      File srcJndiDSConfig = getTempXmlFile();
      File srcLoginConfig = getTempXmlFile();
      
      File srcFile;
      
      // copy the server config
      srcFile = new File(serverFile); 
      copyXmlFile(srcFile, srcServerConfig);
      
      // copy the spring config
      srcFile = new File(springFile);
      copyXmlFile(srcFile, srcSpringConfig);
      
      // copy the jndi config
      srcFile = new File(jndiFile);
      copyXmlFile(srcFile, srcJndiDSConfig);
      srcFile = new File(loginFile);
      copyXmlFile(srcFile, srcLoginConfig);
      
      
      // create the config
      return new PSConfigFileLocator(srcServerConfig, 
         srcSpringConfig, srcJndiDSConfig, srcLoginConfig);         
   }
   
   @Override
   protected String getFilePrefix()
   {
      return "converter-";
   }

   /**
    * Basic implementation of the config file locator interface.
    */
   class PSConfigFileLocator implements IPSConfigFileLocator
   {
      private File mi_serverConfig;
      private File mi_springConfig;
      private File mi_jndiDSConfig;
      private File mi_loginConfig;

      private PSConfigFileLocator(File serverConfig, File springConfig, 
         File jndiConfig, File loginConfig)
      {
         mi_serverConfig = serverConfig;
         mi_springConfig = springConfig;
         mi_jndiDSConfig = jndiConfig;
         mi_loginConfig = loginConfig;
      }
      
      public File getServerConfigFile()
      {
         return mi_serverConfig;
      }

      public File getSpringConfigFile()
      {
         return mi_springConfig;
      }

      public File getJndiDsFile()
      {
         return mi_jndiDSConfig;
      }

      public File getLoginCfgFile()
      {
         return mi_loginConfig;
      }
   }   
}



