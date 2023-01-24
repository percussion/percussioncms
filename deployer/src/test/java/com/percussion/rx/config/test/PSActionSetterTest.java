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

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Category(IntegrationTest.class)
public class PSActionSetterTest extends PSConfigurationTest
{
   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);
      validateLocalConfiguration();
      // \/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
      validateDefaultConfiguration();
   }

   @SuppressWarnings("unchecked")
   private void validateDefaultConfiguration() throws PSNotFoundException {
      IPSDesignModelFactory dm = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = dm.getDesignModel(PSTypeEnum.ACTION);
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSAction action = (PSAction) model.load(gmgr.makeGuid(203,
            PSTypeEnum.ACTION));

      // Check Label
      assertTrue(action.getLabel().equals("Quick Edit"));
      // Check Desc
      assertTrue(action
            .getDescription()
            .equals(
                  "Transitions the current item into a Quick Edit state, check it out and opens it in edit mode."));
      // Check URL
      assertTrue(action.getURL().equals(
            "../sys_action/transitcheckoutedit.xml"));
      // Check URL params
      assertTrue(action.getParameters().size() == 4);
      assertTrue((action.getParameters().getParameter("WFAction"))
            .equals("Quick Edit"));
      // Assignment Types
      PSActionVisibilityContext acxt = action.getVisibilityContexts()
            .getContext(PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE);
      Iterator<String> iter = acxt.iterator();
      List<String> newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 2);
      assertFalse(newCxtVals.contains("3"));
      // Checkout Status
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 1);
      assertFalse(newCxtVals.contains("Checked In"));
      // Content Types
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE);
      assertTrue(acxt==null);
      // Folder Security
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY);
      assertTrue(acxt==null);
      // Locales
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE);
      assertTrue(acxt==null);
      // Object Types
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 1);
      assertTrue(newCxtVals.contains("2"));
      // Publishable
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 3);
      assertTrue(newCxtVals.contains("i"));
      // Roles
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE);
      assertTrue(acxt==null);
      // Workflow
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE);
      assertTrue(acxt==null);
   }

   @SuppressWarnings("unchecked")
   private void validateLocalConfiguration() throws PSNotFoundException {
      IPSDesignModelFactory dm = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = dm.getDesignModel(PSTypeEnum.ACTION);
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSAction action = (PSAction) model.load(gmgr.makeGuid(203,
            PSTypeEnum.ACTION));

      // Check Label
      assertTrue(action.getLabel().equals("Quick Edit LC"));
      // Check Desc
      assertTrue(action.getDescription().equals("LC Desc"));
      // Check URL
      assertTrue(action.getURL().equals(
            "../sys_action/transitcheckouteditLC.xml"));
      // Check URL params
      assertTrue(action.getParameters().size() == 4);
      assertTrue((action.getParameters().getParameter("WFAction"))
            .equals("LCValue"));
      // Check Visibility Contexts
      // Assignment Types
      PSActionVisibilityContext acxt = action.getVisibilityContexts()
            .getContext(PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE);
      Iterator<String> iter = acxt.iterator();
      List<String> newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 3);
      assertTrue(newCxtVals.contains("3"));
      // Checkout Status
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 2);
      assertTrue(newCxtVals.contains("Checked In"));
      // Content Types
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 2);
      assertTrue(newCxtVals.contains("301"));
      // Folder Security
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 1);
      assertTrue(newCxtVals.contains("Read"));
      // Locales
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 1);
      assertTrue(newCxtVals.contains("en-us"));
      // Object Types
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 1);
      assertTrue(newCxtVals.contains("2"));
      // Publishable
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 2);
      assertTrue(newCxtVals.contains("i"));
      // Roles
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 2);
      assertTrue(newCxtVals.contains("Editor"));
      // Workflow
      acxt = action.getVisibilityContexts().getContext(
            PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE);
      iter = acxt.iterator();
      newCxtVals = new ArrayList<String>();
      while (iter.hasNext())
      {
         newCxtVals.add(iter.next());
      }
      assertTrue(newCxtVals.size() == 1);
      assertTrue(newCxtVals.contains("4"));
   }

   public static final String PKG_NAME = "PSActionSetterTest";

   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";

}
