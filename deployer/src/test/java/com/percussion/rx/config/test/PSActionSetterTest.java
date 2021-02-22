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
