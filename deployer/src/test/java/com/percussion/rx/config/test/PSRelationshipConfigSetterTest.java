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

import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRule;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Category(IntegrationTest.class)
public class PSRelationshipConfigSetterTest extends PSConfigurationTest
{
   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(PKG_NAME,
               IMPL_CFG, LOCAL_CFG);
         validateLocalConfiguration();

         // Testing validation
         try
         {
            PSConfigFilesFactoryTest.applyConfig(PKG_NAME + "_2", IMPL_CFG,
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

      PSConfigFilesFactoryTest.deApplyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
      validateDefaultConfiguration();
   }

   @SuppressWarnings("unchecked")
   private void validateDefaultConfiguration() throws PSNotFoundException {
      PSRelationshipConfig cfg = getRelationshipConfig();
      List<PSCloneOverrideField> cloneFields = cfg.getCloneOverrideFieldList();
      assertTrue(cloneFields.size() == 3);
      PSProcessCheck scloneCheck = cfg.getProcessCheck("rs_cloneshallow");
      Iterator conditions = scloneCheck.getConditions();
      List<PSRule> condRules = new ArrayList<PSRule>();
      while (conditions.hasNext())
      {
         condRules.add((PSRule) conditions.next());
      }
      assertTrue(condRules.size() == 1);
      PSProcessCheck dcloneCheck = cfg.getProcessCheck("rs_clonedeep");
      conditions = dcloneCheck.getConditions();
      condRules = new ArrayList<PSRule>();
      while (conditions.hasNext())
      {
         condRules.add((PSRule) conditions.next());
      }
      assertTrue(condRules.size() == 1);
      //Validate effects
      Iterator<PSConditionalEffect> effects = cfg.getEffects();
      PSConditionalEffect effect = null;
      while(effects.hasNext())
      {
         PSConditionalEffect ef = effects.next();
         if (ef.getEffect().getName()
               .equalsIgnoreCase(
                     "sys_PublishMandatory"))
         {
            effect = ef;
            break;
         }
      }
      assertNull(effect);
   }

   @SuppressWarnings("unchecked")
   private void validateLocalConfiguration() throws PSNotFoundException {
      PSRelationshipConfig config = getRelationshipConfig();
      List<PSCloneOverrideField> cloneFields = config
            .getCloneOverrideFieldList();
      assertTrue(cloneFields.size() == 1);
      PSProcessCheck scloneCheck = config.getProcessCheck("rs_cloneshallow");
      Iterator conditions = scloneCheck.getConditions();
      List<PSRule> condRules = new ArrayList<PSRule>();
      while (conditions.hasNext())
      {
         condRules.add((PSRule) conditions.next());
      }
      assertTrue(condRules.size() == 3);
      PSProcessCheck dcloneCheck = config.getProcessCheck("rs_clonedeep");
      conditions = dcloneCheck.getConditions();
      condRules = new ArrayList<PSRule>();
      while (conditions.hasNext())
      {
         // make sure there is one condition with 1 = 2 rule.
         condRules.add((PSRule) conditions.next());
      }
      assertTrue(condRules.size() == 2);
      //Validate effects
      Iterator<PSConditionalEffect> effects = config.getEffects();
      PSConditionalEffect effect = null;
      while(effects.hasNext())
      {
         PSConditionalEffect ef = effects.next();
         if (ef.getEffect().getName()
               .equalsIgnoreCase(
                     "sys_PublishMandatory"))
         {
            effect = ef;
            break;
         }
      }
      assertNotNull(effect);
   }

   private PSRelationshipConfig getRelationshipConfig() throws PSNotFoundException {
      IPSDesignModelFactory dm = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = dm
            .getDesignModel(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
      return (PSRelationshipConfig) model.load("NewCopy");

   }

   public static final String PKG_NAME = "PSRelationshipConfigSetterTest";

   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";
}
