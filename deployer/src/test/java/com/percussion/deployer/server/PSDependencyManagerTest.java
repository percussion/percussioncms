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
package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.server.dependencies.PSApplicationDependencyHandler;
import com.percussion.deployer.server.dependencies.PSCEDependencyHandler;
import com.percussion.deployer.server.dependencies.PSCommunityDependencyHandler;
import com.percussion.deployer.server.dependencies.PSContentListDependencyHandler;
import com.percussion.deployer.server.dependencies.PSCustomDependencyHandler;
import com.percussion.deployer.server.dependencies.PSDataDependencyHandler;
import com.percussion.deployer.server.dependencies.PSExitDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSMenuActionDependencyHandler;
import com.percussion.deployer.server.dependencies.PSSchemaDependencyHandler;
import com.percussion.deployer.server.dependencies.PSSharedGroupDependencyHandler;
import com.percussion.deployer.server.dependencies.PSSlotDependencyHandler;
import com.percussion.deployer.server.dependencies.PSTemplateDependencyHandler;
import com.percussion.deployer.server.dependencies.PSUserDependencyHandler;
import com.percussion.deployer.server.dependencies.PSWorkflowDependencyHandler;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for the PSDependencyMap and PSDependencyDef classes
 */
@Category(IntegrationTest.class)
public class PSDependencyManagerTest
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSDependencyManagerTest()
   {
   }

   @Test
   public void testDependencyManager() throws Exception
   {
      PSDependencyManager depMgr = getDepMgr();
      PSDependencyDef def = depMgr.getDependencyDef("Custom");
      assertTrue(def != null);
   }

   private PSDependencyManager getDepMgr() throws Exception
   {
      if (PSDependencyManager.getInstance() != null)
         return PSDependencyManager.getInstance();
      
      PSDependencyManager.setConfigDir(RESOURCE_D0C_DIR);
      
      PSDependencyManager depMgr = new PSDependencyManager();
      depMgr = PSDependencyManager.getInstance();

      return depMgr;
   }
   
   /**
    * Test the package element reordering.
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void testReorderElements() throws Exception
   {
      List<PSImportPackage> elems = getElements(new String[] {MENU_TYPE, SLOT_TYPE});

      PSDependencyManager depMgr = getDepMgr();
      elems = depMgr.reorderDeployedElements(elems);
      
      assertTrue(elems.size() == 3);
      assertTrue(depMgr.getObjectType(elems.get(0)).equals(SLOT_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(1)).equals(SLOT_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(2)).equals(MENU_TYPE));
      

      elems = getElements(new String[] { CLIST_TYPE, MENU_TYPE, SLOT_TYPE,
            CE_TYPE, TEMPLATE_TYPE, COMMUNITY_TYPE, WF_TYPE, APP_TYPE, 
            SCHAREDGRP_TYPE, SCHEMA_TYPE, EXIT_TYPE, DATA_TYPE, USER_TYPE });
      elems = depMgr.reorderDeployedElements(elems);

      assertTrue(elems.size() == 14);
      assertTrue(depMgr.getObjectType(elems.get(0)).equals(SCHEMA_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(1)).equals(DATA_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(2)).equals(USER_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(3)).equals(EXIT_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(4)).equals(SCHAREDGRP_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(5)).equals(APP_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(6)).equals(WF_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(7)).equals(COMMUNITY_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(8)).equals(SLOT_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(9)).equals(TEMPLATE_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(10)).equals(CE_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(11)).equals(SLOT_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(12)).equals(CLIST_TYPE));
      assertTrue(depMgr.getObjectType(elems.get(13)).equals(MENU_TYPE));

   }
   
   /**
    * Creates a list of package elements from the given types.
    * @param types the type of the deployment elements.
    * @return the created list.
    */
   private List<PSImportPackage> getElements(String[] types)
   {
      List<PSImportPackage> elements = new ArrayList<PSImportPackage>();
      for (String type : types)
      {
         elements.add( new PSImportPackage(createElement(type)) );
      }
      
      return elements;
   }
   
   /**
    * Creates the given deployment element.
    * @param type
    * @return
    */
   private PSDeployableElement createElement(String type)
   {
      if (type.equals(SLOT_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "502", SLOT_TYPE, SLOT_TYPE,
               "rffAutoIndex", false, false, false);
      }

      if (type.equals(MENU_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "101", MENU_TYPE, "Action Menu",
               "Edit", false, false, false);
      }
      
      if (type.equals(CE_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "101", CE_TYPE, "Content Type",
               "Folder", false, false, false);         
      }

      if (type.equals(TEMPLATE_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "504", TEMPLATE_TYPE, "Template",
               "rffSnTitleLink", false, false, false);         
      }

      if (type.equals(CLIST_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "310", CLIST_TYPE, "Content List",
               "rffEiFullBinary", false, false, false);         
      }

      if (type.equals(COMMUNITY_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "1002", COMMUNITY_TYPE, "Community",
               "Enterprise_Investments", false, false, false);         
      }

      if (type.equals(WF_TYPE))
      {
         return new PSDeployableElement(
               PSDependency.TYPE_LOCAL, "4", WF_TYPE, "Workflow",
               "Simple_Workflow", false, false, false);         
      }

      if (type.equals(USER_TYPE))
      {
         return new PSDeployableElement(PSDependency.TYPE_LOCAL, USER_TYPE,
               CUSTOM_TYPE, CUSTOM_TYPE, "User Dependency", false,
               false, false);         
      }

      if (type.equals(APP_TYPE))
      {
         return new PSDeployableElement(PSDependency.TYPE_LOCAL, APP_TYPE
               + "-rx_reports", CUSTOM_TYPE, CUSTOM_TYPE, "rx_reports", false,
               false, false);         
      }

      if (type.equals(DATA_TYPE))
      {
         return new PSDeployableElement(PSDependency.TYPE_LOCAL, DATA_TYPE
               + "-RXS_CT_FILE", CUSTOM_TYPE, CUSTOM_TYPE, "RXS_CT_FILE",
               false, false, false);         
      }

      if (type.equals(SCHEMA_TYPE))
      {
         return new PSDeployableElement(PSDependency.TYPE_LOCAL, SCHEMA_TYPE
               + "-RXS_CT_FILE", CUSTOM_TYPE, CUSTOM_TYPE, "RXS_CT_FILE",
               false, false, false);         
      }

      if (type.equals(EXIT_TYPE))
      {
         return new PSDeployableElement(PSDependency.TYPE_LOCAL, EXIT_TYPE
               + "-Java/global/percussion/system/asmhelper", CUSTOM_TYPE,
               CUSTOM_TYPE, "asmhelper", false, false, false);         
      }

      if (type.equals(SCHAREDGRP_TYPE))
      {
         return new PSDeployableElement(PSDependency.TYPE_LOCAL,
               SCHAREDGRP_TYPE + "-sharedimage", CUSTOM_TYPE, CUSTOM_TYPE,
               "sharedimage", false, false, false);         
      }


      return null;
   }
   
   /**
    * The directory of the source tree that contains the package configuration file.
    */
   private static final String RESOURCE_D0C_DIR = "config/Deployer";
   
   private static final String MENU_TYPE = PSMenuActionDependencyHandler.DEPENDENCY_TYPE;
   private static final String SLOT_TYPE = PSSlotDependencyHandler.DEPENDENCY_TYPE;
   private static final String CE_TYPE = PSCEDependencyHandler.DEPENDENCY_TYPE;
   private static final String TEMPLATE_TYPE = PSTemplateDependencyHandler.DEPENDENCY_TYPE;
   private static final String CLIST_TYPE = PSContentListDependencyHandler.DEPENDENCY_TYPE;
   private static final String COMMUNITY_TYPE = PSCommunityDependencyHandler.DEPENDENCY_TYPE;
   private static final String WF_TYPE = PSWorkflowDependencyHandler.DEPENDENCY_TYPE;

   private static final String CUSTOM_TYPE = PSCustomDependencyHandler.DEPENDENCY_TYPE;

   private static final String USER_TYPE = PSUserDependencyHandler.DEPENDENCY_TYPE;
 
   
   private static final String APP_TYPE = PSApplicationDependencyHandler.DEPENDENCY_TYPE;
   private static final String DATA_TYPE = PSDataDependencyHandler.DEPENDENCY_TYPE;
   private static final String EXIT_TYPE = PSExitDefDependencyHandler.DEPENDENCY_TYPE;
   private static final String SCHEMA_TYPE = PSSchemaDependencyHandler.DEPENDENCY_TYPE;
   private static final String SCHAREDGRP_TYPE = PSSharedGroupDependencyHandler.DEPENDENCY_TYPE;

}
