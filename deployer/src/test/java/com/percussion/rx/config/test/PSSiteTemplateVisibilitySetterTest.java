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

import com.percussion.rx.config.impl.PSSiteTemplateVisibilitySetter;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link PSSiteTemplateVisibilitySetter}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSSiteTemplateVisibilitySetterTest extends PSConfigurationTest
{

   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(PKG_NAME,
               CONFIG_DEF, LOCAL_CFG);
         assertTrue(siteAssociateTemplate(CI_SITE, CI_TEMPLATE_NAME));
         assertTrue(siteAssociateTemplate(EI_SITE, CI_TEMPLATE_NAME));

         // Testing validation
         try
         {
            PSConfigFilesFactoryTest.applyConfig(PKG_NAME + "_2", CONFIG_DEF,
                  LOCAL_CFG);
            fail("Should have failed here, due to validation failure");
         }
         catch (Exception e)
         {
         }
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
      
      // \/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF, DEFAULT_CFG,
            LOCAL_CFG);
      
      assertTrue(siteAssociateTemplate(CI_SITE, CI_TEMPLATE_NAME));
      assertTrue(!siteAssociateTemplate(EI_SITE, CI_TEMPLATE_NAME));
   }

   public void testConfigFiles_WithPrevProperties() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2, LOCAL_CFG_2);
      
      assertTrue(siteAssociateTemplate(CI_SITE, CI_TEMPLATE_NAME));
      assertTrue(siteAssociateTemplate(CI_SITE, EI_TEMPLATE_NAME));
      assertTrue(siteAssociateTemplate(EI_SITE, CI_TEMPLATE_NAME));
      assertTrue(siteAssociateTemplate(EI_SITE, EI_TEMPLATE_NAME));
      
      // \/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2, DEFAULT_CFG,
            LOCAL_CFG_2);
      
      assertTrue(siteAssociateTemplate(CI_SITE, CI_TEMPLATE_NAME));
      assertTrue(!siteAssociateTemplate(CI_SITE, EI_TEMPLATE_NAME));
      assertTrue(!siteAssociateTemplate(EI_SITE, CI_TEMPLATE_NAME));
      assertTrue(!siteAssociateTemplate(EI_SITE, EI_TEMPLATE_NAME));
      
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2, DEFAULT_CFG_2);
      
      assertTrue(siteAssociateTemplate(CI_SITE, CI_TEMPLATE_NAME));
      assertTrue(!siteAssociateTemplate(CI_SITE, EI_TEMPLATE_NAME));
      assertTrue(!siteAssociateTemplate(EI_SITE, CI_TEMPLATE_NAME));
      assertTrue(siteAssociateTemplate(EI_SITE, EI_TEMPLATE_NAME));
   }

   public void testConfigFiles_UnProcess() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2, LOCAL_CFG_3);
      
      assertTrue(siteAssociateTemplate(CI_SITE, EI_TEMPLATE_NAME));
      
      // \/\/\/\/\/\/\/\
      // de-apply or cleanup
      PSConfigFilesFactoryTest.deApplyConfig(PKG_NAME, CONFIG_DEF_2, LOCAL_CFG_3);

      assertTrue(!siteAssociateTemplate(CI_SITE, EI_TEMPLATE_NAME));
   }


   /**
    * Determines if the Site/Template association contains the given template
    * name.
    * 
    * @param siteName the name of the Site in question, assumed not 
    * <code>null</code> or empty.
    * @param templateName the name of the template in question, assumed not
    * <code>null</code> or empty.
    * 
    * @return <code>true</code> if the Site/Template association contains the
    * given template.
    */
   private boolean siteAssociateTemplate(String siteName, String templateName) throws PSNotFoundException {
      IPSDesignModel model = getSiteModel();
      IPSSite site = (IPSSite) model.load(siteName);
      for (IPSAssemblyTemplate t : site.getAssociatedTemplates())
      {
         if (t.getName().equals(templateName))
            return true;
      }
      return false;
   }

   private IPSDesignModel getSiteModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
      .getDesignModelFactory();
      return factory.getDesignModel(PSTypeEnum.SITE);
   }
   
   public void testAddPropertyDefs() throws Exception
   {
      IPSDesignModel model = getSiteModel();
      IPSSite site = (IPSSite) model.load(EI_SITE);
      
      PSSiteTemplateVisibilitySetter setter = new PSSiteTemplateVisibilitySetter();
      
      Map<String, Object> props = new HashMap<String, Object>();
      Map<String, Object> defs = new HashMap<String, Object>();
      
      props.put(PSSiteTemplateVisibilitySetter.VISIBILITY, "${perc.prefix.vis}");
      setter.setProperties(props);
      setter.addPropertyDefs(site, defs);
      
      assertTrue("Expect 1 def", defs.size() == 1);
      List<String> names = (List<String>) defs.get("perc.prefix.vis");
      assertTrue("Expect 1 def", names.size() == 41);
      assertTrue("Contains \"rffPgEiGeneric\"", names.contains("rffPgEiGeneric"));
   }
   
   private static final String CI_SITE = "Corporate_Investments";
   private static final String EI_SITE = "Enterprise_Investments";
   
   private static final String CI_TEMPLATE_NAME = "rffPgCiGeneric";
   private static final String EI_TEMPLATE_NAME = "rffPgEiGeneric";
   
   public static final String PKG_NAME = "PSSiteTemplateVisibilitySetterTest";
   
   public static final String CONFIG_DEF = PKG_NAME + "_configDef.xml";

   public static final String CONFIG_DEF_2 = PKG_NAME + "_2_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String LOCAL_CFG_2 = PKG_NAME + "_2_localConfig.xml";

   public static final String LOCAL_CFG_3 = PKG_NAME + "_3_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";

   public static final String DEFAULT_CFG_2 = PKG_NAME + "_2_defaultConfig.xml";
}
