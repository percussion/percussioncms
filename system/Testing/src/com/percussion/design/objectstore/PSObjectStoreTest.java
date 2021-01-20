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
package com.percussion.design.objectstore;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.testing.IPSUnitTestConfigHelper;
import com.percussion.testing.PSClientTestCase;
import com.percussion.testing.PSConfigHelperTestCase;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.jetty.PSJettyJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the object store. These tests require the server to be running.
 */
@Category(IntegrationTest.class)
public class PSObjectStoreTest extends PSClientTestCase
{

   @BeforeClass
   public static void setUp()
   {
      BasicConfigurator.configure();
   }

   public PSObjectStoreTest(String name)
   {
      super(name);
   }

   /**
    * Tests loading and saving JNDI datasource configurations through the 
    * objectstore.
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testGetAndSaveJndiDatasources() throws Exception
   {
      // create a connection and an objectstore
      PSDesignerConnection conn = makeConnection();
      PSObjectStore os = makeObjectStore(conn);

      // get and release config lock in case previous test left it locked
      os.releaseServerConfigurationLock(os.getServerConfiguration(true, true));

      // test load and save w/out lock
      List<IPSJndiDatasource> datasources = os.getJndiDatasources(false);
      IPSJndiDatasource newDs = new PSJettyJndiDatasource("jdbc/rxdefault3",
         "jtds:sqlserver", "net.sourceforge.jtds.jdbc.Driver",
          "//bender", "sa", "demo");
      datasources.add(newDs);
      boolean didThrow = false;
      try
      {
         os.saveJndiDatasources(datasources);
      }
      catch (PSServerException e)
      {
         if (e.getOriginatingException() instanceof PSNotLockedException)
            didThrow = true;
         else
            throw e;
      }
      assertTrue(didThrow);
      
      // re-load locked and make sure save did not go through
      List<IPSJndiDatasource> datasources2 = os.getJndiDatasources(true);
      assertTrue(!datasources.equals(datasources2));
      
      // save and compare
      os.saveJndiDatasources(datasources);
      List<IPSJndiDatasource> datasources3 = os.getJndiDatasources(true);
      assertEquals(datasources, datasources3);
      
      // test removal
      os.saveJndiDatasources(datasources2);
      datasources3 = os.getJndiDatasources(true);
      assertEquals(datasources2, datasources3);
   }
   
   /**
    * Tests loading and saving a datasource resolver containing datasource 
    * configurations through the objectstore.
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testGetAndSaveCatalogerConfigs() throws Exception
   {
      // create a connection and an objectstore
      PSDesignerConnection conn = makeConnection();
      PSObjectStore os = makeObjectStore(conn);

      // get and release config lock in case previous test left it locked
      os.releaseServerConfigurationLock(os.getServerConfiguration(true, true));

      // test load and save w/out lock
      List<PSCatalogerConfig> configs = os.getCatalogerConfigs(false);
      List<PSCatalogerConfig> oldconfigs = new ArrayList<PSCatalogerConfig>(
         configs);
      PSCatalogerConfig newConfig1 = new PSCatalogerConfig("config1",
         PSCatalogerConfig.ConfigTypes.SUBJECT, "com.test.foo1",
         "a test cataloger1", new HashMap<String, String>());
      configs.add(newConfig1);
      PSCatalogerConfig newConfig2 = new PSCatalogerConfig("config2",
         PSCatalogerConfig.ConfigTypes.SUBJECT, "com.test.foo2",
         "a test cataloger2", new HashMap<String, String>());
      configs.add(newConfig2);
      PSCatalogerConfig newConfig3 = new PSCatalogerConfig("config3",
         PSCatalogerConfig.ConfigTypes.ROLE, "com.test.foo3",
         "a test cataloger3", new HashMap<String, String>());
      configs.add(newConfig3);
      
      boolean didThrow = false;
      try
      {
         os.saveCatalogerConfigs(configs);
      }
      catch (PSServerException e)
      {
         if (e.getOriginatingException() instanceof PSNotLockedException)
            didThrow = true;
         else
            throw e;
      }
      assertTrue(didThrow);
      
      // re-load locked and make sure save did not go through
      List<PSCatalogerConfig> newconfigs = os.getCatalogerConfigs(true);
      assertTrue(!configs.equals(newconfigs));

      // save and compare      
      os.saveCatalogerConfigs(configs);
      newconfigs = os.getCatalogerConfigs(true);
      assertEquals(configs, newconfigs);
      
      // test removal
      os.saveCatalogerConfigs(oldconfigs);
      newconfigs = os.getCatalogerConfigs(true);
      assertEquals(oldconfigs, newconfigs);
   }
   
   
   /**
    * Tests loading and saving a datasource resolver containing datasource 
    * configurations through the objectstore.
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testGetAndSaveDatasourceConfigs() throws Exception
   {
      // create a connection and an objectstore
      PSDesignerConnection conn = makeConnection();
      PSObjectStore os = makeObjectStore(conn);

      // get and release config lock in case previous test left it locked
      os.releaseServerConfigurationLock(os.getServerConfiguration(true, true));

      // test load and save w/out lock
      PSDatasourceResolver curResolver = os.getDatasourceConfigs(false);  
      List<IPSDatasourceConfig> configs =
         curResolver.getDatasourceConfigurations();
      PSDatasourceConfig newConfig = new PSDatasourceConfig("testDS", 
         "jdbc/test", "dbo", "rxTestdb");
      configs.add(newConfig);
      curResolver.setRepositoryDatasource(newConfig.getName());
      boolean didThrow = false;
      try
      {
         os.saveDatsourceConfigs(curResolver);
      }
      catch (PSServerException e)
      {
         if (e.getOriginatingException() instanceof PSNotLockedException)
            didThrow = true;
         else
            throw e;
      }
      assertTrue(didThrow);
      
      // re-load locked and make sure save did not go through
      PSDatasourceResolver newResolver = os.getDatasourceConfigs(true);
      assertTrue(!configs.equals(newResolver.getDatasourceConfigurations()));
      assertTrue(!newResolver.getRepositoryDatasource().equals(
         curResolver.getRepositoryDatasource()));

      // save and compare      
      os.saveDatsourceConfigs(curResolver);
      newResolver = os.getDatasourceConfigs(true);
      assertEquals(configs, newResolver.getDatasourceConfigurations());
      assertEquals(newConfig.getName(), newResolver.getRepositoryDatasource());
      
      // test removal
      configs.remove(configs.size() - 1);
      curResolver.setRepositoryDatasource(configs.get(0).getName());
      os.saveDatsourceConfigs(curResolver);
      newResolver = os.getDatasourceConfigs(true);
      assertEquals(curResolver.getDatasourceConfigurations(), 
         newResolver.getDatasourceConfigurations());
      assertEquals(curResolver.getRepositoryDatasource(), 
         newResolver.getRepositoryDatasource());      
   }

   /**
    * Tests loading and saving a hibernate dialect configuration through the 
    * objectstore.
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testGetAndSaveHibernateConfig() throws Exception
   {
      // create a connection and an objectstore
      PSDesignerConnection conn = makeConnection();
      PSObjectStore os = makeObjectStore(conn);
      
      // get and release config lock in case previous test left it locked
      os.releaseServerConfigurationLock(os.getServerConfiguration(true, true));

      // test load and save w/out lock
      PSHibernateDialectConfig curConfig = os.getHibernateDialectConfig(false);
      curConfig.setDialect("foo", "org.foo.fooDialect");
      Map<String, String> dialects = curConfig.getDialects();
      boolean didThrow = false;
      try
      {
         os.saveHibernateDialectConfig(curConfig);
      }
      catch (PSServerException e)
      {
         if (e.getOriginatingException() instanceof PSNotLockedException)
            didThrow = true;
         else
            throw e;
      }
      assertTrue(didThrow);
      
      // re-load locked and make sure save did not go through
      PSHibernateDialectConfig newConfig = os.getHibernateDialectConfig(true);
      assertTrue(!dialects.equals(newConfig.getDialects()));

      // save and compare      
      os.saveHibernateDialectConfig(curConfig);
      newConfig= os.getHibernateDialectConfig(true);
      assertEquals(dialects, newConfig.getDialects());
      
      // test removal
      dialects.remove("foo");
      curConfig.setDialects(dialects);
      os.saveHibernateDialectConfig(curConfig);
      newConfig = os.getHibernateDialectConfig(true);
      assertEquals(curConfig.getDialects(), newConfig.getDialects());
   }
   
   
   /**
    * Test creating, renaming, moving, and removing application files
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testGetApplicationFiles() throws Exception
   {
      PSObjectStore os = makeObjectStore();
      os.setClientGeneratedSessionId("uniqueOne123456");
      PSApplication app = os.getApplication("sys_cxSupport", false);
      os.getApplicationFiles(app, null);
      
      byte[] fileContent = "test file content".getBytes();
      File file1 = new File("testFile1");
      File file2 = new File("testFile2");
      File file1_2 = new File("doghouse");
      PSApplicationFile newFile1 = new PSApplicationFile(
         new ByteArrayInputStream(fileContent), file1);
      PSApplicationFile newFile2 = new PSApplicationFile(
         new ByteArrayInputStream(fileContent), file2);
      
      os.saveApplicationFile(app, newFile1, true, true);
      os.saveApplicationFile(app, newFile2, true, true);
      os.renameApplicationFile(app, newFile1, file1_2.getName(), true);
      os.removeApplicationFile(app, new PSApplicationFile(file1_2), true);

      // move file from one application to another
      PSApplication app2 = os.getApplication("rxs_Support_ce", false);
      os.moveApplicationFile(app, newFile2, app2, newFile2, true);
      os.removeApplicationFile(app2, newFile2, true);
      
      // test load from app
      os.loadApplicationFile(app, new PSApplicationFile(new File(
         "VariantList.dtd")));
   }

   /**
    * Creates an object store with a connection having the default settings.
    * @author   chad loder
    *
    * @version 1.0 1999/8/11
    *
    * @return   PSObjectStore
    */
   private PSObjectStore makeObjectStore()
      throws PSServerException, PSAuthorizationException,
             PSAuthenticationFailedException, PSIllegalArgumentException
   {
      return makeObjectStore(null);
   }

   /**
    * Creates an object store with the given designer connection. If
    * the connection is null, creates an object store with a connection
    * having the default settings.
    *
    * @author   chad loder
    *
    * @version 1.0 1999/8/11
    *
    * @param   conn
    *
    * @return   PSObjectStore
    */
   private PSObjectStore makeObjectStore(PSDesignerConnection conn)
      throws PSServerException, PSAuthorizationException,
             PSAuthenticationFailedException, PSIllegalArgumentException
   {
      if (conn == null)
         conn = makeConnection();

      return new PSObjectStore(conn);
   }

   /**
    * Creates a connection to the server using the default connection params.
    *
    * @author   chad loder
    *
    * @version 1.0 1999/8/11
    *
    * @return   PSDesignerConnection
    */
   protected PSDesignerConnection makeConnection() throws PSServerException,
      PSAuthorizationException, PSAuthenticationFailedException,
      PSIllegalArgumentException
   {
      Properties properties = null;
      try
      {
         properties = getConnectionProps(
            PSConfigHelperTestCase.CONN_TYPE_RXSERVER);
      }
      catch (IOException e)
      {
         System.out.println("Failed to load the server configuration for " +
            "the following reason: " + e.getLocalizedMessage() + "We will " +
            "continue with the default connection info " +
            "(http, localhost, 9992, admin1, demo).");
      }
      
      // setup defaults
      String protocol = "http";
      String host = "localhost";
      String port = "9992";
      String loginId = "admin1";
      String loginPw = "demo";
      
      if (properties != null)
      {
         String useSsl = properties.getProperty(
            IPSUnitTestConfigHelper.PROP_USESSL, "false");
         if (useSsl.equalsIgnoreCase("true") || 
            useSsl.equalsIgnoreCase("yes") || useSsl.equalsIgnoreCase("y"))
            protocol = "https";
         
         String property = properties.getProperty(
            IPSUnitTestConfigHelper.PROP_HOST_NAME);
         if (!StringUtils.isBlank(property))
            host = property;
         
         property = properties.getProperty(IPSUnitTestConfigHelper.PROP_PORT);
         if (!StringUtils.isBlank(property))
            port = property;
         
         property = properties.getProperty(
            IPSUnitTestConfigHelper.PROP_LOGIN_ID);
         if (!StringUtils.isBlank(property))
            loginId = property;
         
         property = properties.getProperty(
            IPSUnitTestConfigHelper.PROP_LOGIN_PW);
         if (!StringUtils.isBlank(property))
            loginPw = property;
      }

      return makeConnection(protocol, host, port, loginId, loginPw);
   }

   
   protected PSDesignerConnection makeConnection(String protocol,
      String hostName, String port, String userId, String password)
      throws PSServerException, PSAuthorizationException,
      PSAuthenticationFailedException, PSIllegalArgumentException
   {
        Properties info = new Properties();
        info.put("protocol", protocol);
        info.put("hostName", hostName);
        info.put("port", port);
        info.put("loginId", userId);
        info.put("loginPw", password);
        
      // create and connect
      PSDesignerConnection conn = new PSDesignerConnection(info);
      assertTrue("Is connection connected?", conn.isConnected());
      
      return conn;
   }
}
