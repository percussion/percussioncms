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
package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSEncryptor;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

/**
 * Test case for the {@link PSSecurityProviderConverter} class.
 */
@Category(IntegrationTest.class)
public class PSSecurityProviderConverterTest extends PSBaseConverterTest
{
   /**
    * Test the converter with existing conversions.
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void testConversion() throws Exception
   {
      // load legacy config to ensure it is correct
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(m_legacyFile), false);
      PSLegacyServerConfig legacyConfig = new PSLegacyServerConfig(doc);
      
      // check legacy config
      checkLegacy(legacyConfig);
            
      // create the config
      IPSConfigFileLocator locator = 
         initFileLocator(m_legacyFile, m_springFile, m_jndiDSFile, m_loginFile); 
     
      // create the ctx
      PSConfigurationCtx ctx = new PSConfigurationCtx(locator,
              PSLegacyEncrypter.getInstance(PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
              ).OLD_SECURITY_KEY());
            
      PSInstRepositoryInfo repInfo = new PSInstRepositoryInfo(m_resourceDir);
      
      PSSecurityProviderConverter spConverter= 
         new PSSecurityProviderConverter(ctx, repInfo, true);
            
      // perform conversion
      spConverter.convert();
      
      // save config files
      ctx.saveConfigs();
      
      // load converted config
      doc = PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(locator.getServerConfigFile().getAbsolutePath()),
                  false);
      PSServerConfiguration serverConfig = new PSServerConfiguration(doc);
      
      // check converted config
      checkConverted(serverConfig);
   }
  
   /**
    * Checks if the supplied legacy server configuration is correct.
    * 
    * @param legacyConfig The legacy server config, assumed not <code>null</code>.
    */
   private void checkLegacy(PSLegacyServerConfig legacyConfig)
   {
      PSCollection secProviders = legacyConfig.getSecurityProviderInstances();
      
      int numSecProvs = secProviders.size();
      
      assertTrue(numSecProvs == 3);
      
      int i;
      for (i = 0; i < numSecProvs; i++)
      {
         PSLegacySecurityProviderInstance provider = 
            (PSLegacySecurityProviderInstance) secProviders.get(i);
         int type = provider.getType();
         String name = provider.getName();
                  
         if (type == PSSecurityProvider.SP_TYPE_BETABLE)
         {
            Properties provProps = provider.getProperties();
            assertEquals(name, "rxmaster");
            assertNotNull(provProps.getProperty("serverName"));
            assertNotNull(provProps.getProperty("driverName"));
            assertNotNull(provProps.getProperty("loginPw"));
            assertNotNull(provProps.getProperty("schemaName"));
            assertNotNull(provProps.getProperty("loginId"));
            assertNotNull(provProps.getProperty("databaseName"));
            continue;
         }
         
         if (type == PSSecurityProvider.SP_TYPE_DIRCONN)
         {
            Iterator groups = provider.getGroupProviderNames();
            PSProvider dirProv = provider.getDirectoryProvider();
            PSReference dirSetRef = dirProv.getReference();
            String dirSetName = dirSetRef.getName();
            
            assertEquals(name, "myProvider");
            assertTrue(groups.hasNext());
            
            String groupName = (String) groups.next();
            assertEquals(groupName, "group1");
            
            assertEquals(dirSetName, "DirectorySet0");
         }
      }
      
      assertNotNull(legacyConfig.getGroupProviderInstance("group1", 
            PSSecurityProvider.SP_TYPE_DIRCONN));
            
      PSDirectory dir0 = legacyConfig.getDirectory("Directory0");
      PSDirectory dir1 = legacyConfig.getDirectory("Directory1");
      assertNotNull(dir0);
      assertNotNull(dir1);
            
      Iterator iterDir0 = dir0.getGroupProviderNames();
      assertFalse(iterDir0.hasNext());
      
      Iterator iterDir1 = dir1.getGroupProviderNames();
      assertFalse(iterDir1.hasNext());
      
      PSDirectorySet dirSet0 = legacyConfig.getDirectorySet("DirectorySet0");
      assertNotNull(dirSet0);
      assertTrue(dirSet0.size() == 2);
      assertNotNull(dirSet0.getDirectoryRef("Directory0"));
      assertNotNull(dirSet0.getDirectoryRef("Directory1"));
      
      String emailAttr = dirSet0.getRequiredAttributeName(
            PSDirectorySet.EMAIL_ATTRIBUTE_KEY);
      String roleAttr = dirSet0.getRequiredAttributeName(
            PSDirectorySet.ROLE_ATTRIBUTE_KEY);
      assertNotNull(emailAttr);
      assertNotNull(roleAttr);
      assertEquals(emailAttr, "myEmail");
      assertEquals(roleAttr, "myRole");
   }      

   /**
    * Checks if the supplied post conversion server configuration is correct.
    * 
    * @param serverConfig The converted server config, assumed not <code>null</code>.
    */
   private void checkConverted(PSServerConfiguration serverConfig)
   {
      PSCollection secProviders = serverConfig.getSecurityProviderInstances();
      
      int numSecProvs = secProviders.size();
      
      assertEquals(numSecProvs, 3);
      
      int i;
      for (i = 0; i < numSecProvs; i++)
      {
         PSSecurityProviderInstance provider = 
            (PSSecurityProviderInstance) secProviders.get(i);
         int type = provider.getType();
         String name = provider.getName();
                  
         if (type == PSSecurityProvider.SP_TYPE_BETABLE)
         {
            assertEquals(name, "rxmaster");
            
            Properties provProps = provider.getProperties();
            assertNotNull(provProps.getProperty("datasourceName"));
            assertNull(provProps.getProperty("serverName"));
            assertNull(provProps.getProperty("driverName"));
            assertNull(provProps.getProperty("loginPw"));
            assertNull(provProps.getProperty("schemaName"));
            assertNull(provProps.getProperty("loginId"));
            assertNull(provProps.getProperty("databaseName"));
            continue;
         }
         
         if (type == PSSecurityProvider.SP_TYPE_DIRCONN)
         {
            PSProvider dirProv = provider.getDirectoryProvider();
            PSReference dirSetRef = dirProv.getReference();
            String dirSetName = dirSetRef.getName();
            
            assertEquals(name, "myProvider");
            assertEquals(dirSetName, "DirectorySet0");
         }
      }
      
      assertNotNull(serverConfig.getGroupProviderInstance("group1", 
            PSSecurityProvider.SP_TYPE_DIRCONN));
         
      PSDirectory dir0 = serverConfig.getDirectory("Directory0");
      PSDirectory dir1 = serverConfig.getDirectory("Directory1");
      assertNotNull(dir0);
      assertNotNull(dir1);
      
      Iterator iterDir0 = dir0.getGroupProviderNames();
      assertTrue(iterDir0.hasNext());
      
      String dir0Group = (String) iterDir0.next();
      assertEquals(dir0Group, "group1");
      
      Iterator iterDir1 = dir1.getGroupProviderNames();
      assertTrue(iterDir1.hasNext());
      
      String dir1Group = (String) iterDir1.next();
      assertEquals(dir1Group, "group1");
      
      PSCollection dir0Attrs = dir0.getAttributes();
      assertTrue(dir0Attrs.contains("myEmail"));
      assertTrue(dir0Attrs.contains("myRole"));
      
      PSCollection dir1Attrs = dir1.getAttributes();
      assertTrue(dir1Attrs.contains("myEmail"));
      assertTrue(dir1Attrs.contains("myRole"));
      
      PSDirectorySet dirSet0 = serverConfig.getDirectorySet("DirectorySet0");
      assertNotNull(dirSet0);
      assertTrue(dirSet0.size() == 2);
      assertNotNull(dirSet0.getDirectoryRef("Directory0"));
      assertNotNull(dirSet0.getDirectoryRef("Directory1"));
      
      String emailAttr = dirSet0.getRequiredAttributeName(
            PSDirectorySet.EMAIL_ATTRIBUTE_KEY);
      String roleAttr = dirSet0.getRequiredAttributeName(
            PSDirectorySet.ROLE_ATTRIBUTE_KEY);
      assertNotNull(emailAttr);
      assertNotNull(roleAttr);
      assertEquals(emailAttr, "myEmail");
      assertEquals(roleAttr, "myRole");
   }      
   
   /**
    * The unit test resource directory for this test
    */
   private String m_resourceDir =
      "/com/percussion/design/objectstore/legacy/";
   
   /**
    * Name of the jndi datasource file
    */
   private String m_jndiDSFile = m_resourceDir + "rx-ds.xml";
   
   /**
    * Name of the spring configuration file
    */
   private String m_springFile = m_resourceDir + "server-beans.xml";
   
   /**
    * Name of the login config file
    */
   private String m_loginFile = m_resourceDir + "login-config.xml";
   
   /**
    * Name of the legacy config file
    */
   private String m_legacyFile = m_resourceDir + "legacy-config.xml";
   
}

