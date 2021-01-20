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
package com.percussion.install;

import static org.junit.Assert.*;

import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.testing.PSAbstractSpringContextTest;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.percussion.utils.annotations.IgnoreInWebAppSpringContext;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.SpringContextTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

/**
 * @author JaySeletz
 *
 */
@Category({IntegrationTest.class, SpringContextTest.class})
@IgnoreInWebAppSpringContext
public class PSUpgradePluginAddDesignerRoleTest extends PSAbstractSpringContextTest
{

   @Test
   public void test() throws IOException, SAXException
   {
      PSUpgradePluginAddDesignerRole plugin = new PSUpgradePluginAddDesignerRole();

      plugin.setLogger(System.out);
      
      File wfFile = PSResourceUtils.getFile(PSUpgradePluginAddDesignerRoleTest.class,"/com/percussion/rxupgrade/AddDesignerRoleWorkflow.xml",null);
      PSWorkflow testWF = new PSWorkflow();
      testWF.fromXML(FileUtils.readFileToString(wfFile));
      
      assertFalse(isDesignerAssignee(testWF));
      plugin.addRoleToWorkflow(testWF);
      assertTrue(isDesignerAssignee(testWF));      
   }
   
   /**
    * Check that the designer role is an assignee of every state, and has a transition role for every transition in the supplied
    * workflow
    * 
    * @param workflow The workflow to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if so, <code>false</code> if not.
    */
   private boolean isDesignerAssignee(PSWorkflow workflow)
   {
      List<PSWorkflowRole> roles = workflow.getRoles();
      IPSGuid roleId = null;
      for (PSWorkflowRole role : roles)
      {
         if (role.getName().equals(PSUpgradePluginAddDesignerRole.DESIGNER_ROLE_NAME))
         {
            roleId = role.getGUID();
            break;
         }
      }
      
      if (roleId == null)
         return false;
      
      boolean hasStateRole = false;
      List<PSState> states = workflow.getStates();
      for (PSState state : states)
      {
         List<PSAssignedRole> stateRoles = state.getAssignedRoles();
         for (PSAssignedRole stateRole : stateRoles)
         {
            if (stateRole.getGUID().equals(roleId))
            {
               if (stateRole.getAssignmentType().equals(PSAssignmentTypeEnum.ASSIGNEE))
               {
                  hasStateRole = true;
                  break;
               }
            }
         }
         
         if (!hasStateRole)
            return false;
         
         List<PSTransition> transitions = state.getTransitions();
         for (PSTransition transition : transitions)
         {
            boolean hasTransRole = false;
            List<PSTransitionRole>transRoles = transition.getTransitionRoles();
            for (PSTransitionRole transRole : transRoles)
            {
               if (transRole.getRoleId() == roleId.getUUID())
               {
                  hasTransRole = true;
                  break;
               }
            }
            
            if (!hasTransRole)
               return false;
         }
      }
      
      return true;
   }

   /**
    * Actually updates the default workflow to add the role.  Was used to update the Default Workflow for re-package, not run as part of the plugin test.
    * 
    * @throws IOException
    * @throws SAXException
    */
   public void updateDefaultWorkflow() throws IOException, SAXException
   {
      IPSWorkflowService wfService = PSWorkflowServiceLocator.getWorkflowService();
      List<PSWorkflow> wfs = wfService.findWorkflowsByName("Default Workflow");
      assertTrue(wfs.size() == 1);
      PSWorkflow defaultWF = wfs.get(0);
      
      PSUpgradePluginAddDesignerRole plugin = new PSUpgradePluginAddDesignerRole();
      plugin.setLogger(System.out);
      
      plugin.addRoleToWorkflow(defaultWF);
      
      wfService.saveWorkflow(defaultWF);

   }
   

   /**
    * Actually updates the local workflow to add the role.  Was used to update the Local Workflow for re-package, not run as part of the plugin test.
    * 
    * @throws IOException
    * @throws SAXException
    */
   public void updateLocalWorkflow() throws IOException, SAXException
   {
      IPSWorkflowService wfService = PSWorkflowServiceLocator.getWorkflowService();
      List<PSWorkflow> wfs = wfService.findWorkflowsByName("LocalContent");
      assertTrue(wfs.size() == 1);
      PSWorkflow localWF = wfs.get(0);
      
      PSUpgradePluginAddDesignerRole plugin = new PSUpgradePluginAddDesignerRole();
      plugin.setLogger(System.out);
      
      plugin.addRoleToWorkflow(localWF);
      
      wfService.saveWorkflow(localWF);

   }   

}
