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
package com.percussion.rx.config.test;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.rx.config.IPSConfigService.ConfigTypes;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.config.impl.PSConfigService;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@Category(IntegrationTest.class)
public class PSConfigServiceTest extends PSConfigurationTest
{
   PSConfigService m_srv;
   
   public void testProcessLocalSpecChanges() throws PSNotFoundException {
      m_cfgFactory.applyConfig(false);

      // Load the model and check whether the label has been changed or not.
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid callOutTemplateGuid = gmgr.makeGuid(501, PSTypeEnum.TEMPLATE);
      IPSDesignModelFactory dmFactory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = dmFactory.getDesignModel(PSTypeEnum.TEMPLATE);
      IPSAssemblyTemplate template = (IPSAssemblyTemplate) model
            .load(callOutTemplateGuid);
      assertEquals("S - Callout1", template.getLabel());

      // Load the model and check whether the label has been changed or not.
      IPSGuid autoIndexTypeGuid = gmgr.makeGuid(301, PSTypeEnum.NODEDEF);
      model = dmFactory.getDesignModel(PSTypeEnum.NODEDEF);
      PSItemDefinition itemDef = (PSItemDefinition) model
            .load(autoIndexTypeGuid);
      assertEquals("Auto Index 1", itemDef.getLabel());

   }
   
   public void testUninstallConfigs() throws Exception
   {
      String uninstConfig = "PSConfigServiceUninstallTest";
      new PSConfigFilesFactoryTest(uninstConfig, CONFIG_DEF_SAMPLE,
            DEFAULT_CONFIG_SAMPLE, DEFAULT_CONFIG_SAMPLE);
      PSConfigService cfgS = (PSConfigService) PSConfigServiceLocator
            .getConfigService();
      File cdFile = cfgS.getConfigFile(ConfigTypes.CONFIG_DEF,
            uninstConfig);
      assertTrue(cdFile.exists());
      File visFile = cfgS.getConfigFile(ConfigTypes.VISIBILITY,
            uninstConfig);
      assertTrue(visFile.exists());
      
      cfgS.uninstallConfiguration(uninstConfig);
      cdFile = cfgS.getConfigFile(ConfigTypes.CONFIG_DEF,
            uninstConfig);
      assertFalse(cdFile.exists());
      File dcFile = cfgS.getConfigFile(ConfigTypes.DEFAULT_CONFIG,
            uninstConfig);
      assertFalse(dcFile.exists());
      File lcFile = cfgS.getConfigFile(ConfigTypes.LOCAL_CONFIG,
            uninstConfig);
      assertFalse(lcFile.exists());
      assertFalse(visFile.exists());
   }
   
   public void testCommunityVisibility() throws Exception
   {

      Collection<String> communities = new ArrayList<String>();
      // reset the community visibility
      m_srv.saveCommunityVisibility(communities, PKG_NAME, true);
      
      communities = m_srv.loadCommunityVisibility(PKG_NAME);
      assertTrue("Expecte empty community list", communities.isEmpty());

      // add a couple of communities
      communities.add("EI_Community");
      communities.add("CI_Community");
      m_srv.saveCommunityVisibility(communities, PKG_NAME, true);

      communities = m_srv.loadCommunityVisibility(PKG_NAME);
      assertTrue("Expecte 2 communities", communities.size() == 2);
      assertTrue(communities.contains("EI_Community"));
      assertTrue(communities.contains("CI_Community"));

      // test merge
      communities.clear();
      communities.add("BI_Community\u5929\u5929");
      m_srv.saveCommunityVisibility(communities, PKG_NAME, false);
      communities = m_srv.loadCommunityVisibility(PKG_NAME);
      assertTrue("Expecte 3 communities", communities.size() == 3);
      assertTrue(communities.contains("BI_Community\u5929\u5929"));
   }
   
   @Override
   protected void setUp() throws Exception
   { 
      super.setUp();
      
      m_cfgFactory = new PSConfigFilesFactoryTest(PKG_NAME,
            CONFIG_DEF_SAMPLE, DEFAULT_CONFIG_SAMPLE, DEFAULT_CONFIG_SAMPLE);
      
      m_srv = (PSConfigService) PSConfigServiceLocator.getConfigService();      
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      m_cfgFactory.release();
   }
   
   private static final String PKG_NAME = "PSConfigServiceTest";
   
   private PSConfigFilesFactoryTest m_cfgFactory;
   
   private static final String CONFIG_DEF_SAMPLE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
      "<beans xmlns=\"http://www.springframework.org/schema/beans\" \r\n" +
      "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n" + 
      "       xmlns:aop=\"http://www.springframework.org/schema/aop\" \r\n" + 
      "       xmlns:tx=\"http://www.springframework.org/schema/tx\" \r\n" + 
      "       xsi:schemaLocation=\" \r\n" +
      "   http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\r\n" +
      "   http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.0.xsd\r\n"+
      "   http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\r\n"+
      "   <bean id=\"SnipTemplate\" class=\"com.percussion.rx.config.impl.PSObjectConfigHandler\">\r\n"+
      "      <property name=\"name\" value=\"rffSnCallout\"/>\r\n"+
      "      <property name=\"type\" value=\"TEMPLATE\"/>\r\n"+
      "      <property name=\"propertySetters\">\r\n"+
      "        <bean class=\"com.percussion.rx.config.impl.PSSimplePropertySetter\">\r\n"+
      "           <property name=\"properties\">\r\n"+
      "              <map>\r\n"+
      "                 <entry key=\"label\" value=\"${com.percussion.PSConfigServiceTest.templateLabel}\"/>\r\n"+
      "              </map>\r\n"+
      "           </property>\r\n"+
      "        </bean>\r\n"+
      "      </property>\r\n"+
      "   </bean>\r\n"+
      "   <bean id=\"ContentType\" class=\"com.percussion.rx.config.impl.PSObjectConfigHandler\">\r\n"+
      "      <property name=\"name\" value=\"rffAutoIndex\"/>\r\n"+
      "      <property name=\"type\" value=\"NODEDEF\"/>\r\n"+
      "      <property name=\"propertySetters\">\r\n"+
      "        <bean class=\"com.percussion.rx.config.impl.PSSimplePropertySetter\">\r\n"+
      "           <property name=\"properties\">\r\n"+
      "              <map>\r\n"+
      "                 <entry key=\"label\" value=\"${com.percussion.PSConfigServiceTest.contenttypeLabel}\"/>\r\n"+
      "              </map>\r\n"+
      "           </property>\r\n"+
      "        </bean>\r\n"+
      "      </property>\r\n"+
      "   </bean>\r\n"+
      "</beans>\r\n";
   
   private static final String DEFAULT_CONFIG_SAMPLE = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
      "<SolutionConfigurations publisherPrefix=\"com.percussion\" publisherName=\"Percussion\" type=\"config\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"localConfig.xsd\">\r\n" +
      "   <SolutionConfig name=\"PSConfigServiceTest\">\r\n" +
      "      <property name=\"templateLabel\" value=\"S - Callout1\"/>\r\n" +
      "      <property name=\"contenttypeLabel\" value=\"Auto Index 1\"/>\r\n" +
      "   </SolutionConfig>\r\n" +
      "</SolutionConfigurations>\r\n";
}
