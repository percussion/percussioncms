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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.webservices.system.PSAgingTransition;
import com.percussion.webservices.system.PSTransition;
import com.percussion.webservices.system.PSWorkflowRole;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.io.IOUtils;

/**
 * Unit test for the {@link PSWorkflowConverter}.
 */
public class PSWorkflowConverterTest extends TestCase
{
   /**
    * Tests the conversion from a server to a client object. 
    * 
    * @throws Exception if the test fails
    */
   public void testConversion() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSWorkflow.class);
      
      // load xml from file system
      List<PSWorkflow> wfs = loadWorkflows();
      for (PSWorkflow wf : wfs)
      {
         Object clientObject = converter.convert(
            com.percussion.webservices.system.PSWorkflow.class, wf);
         
         validateConversion(wf, 
            (com.percussion.webservices.system.PSWorkflow) clientObject);
      }
   }

   /**
    * Loads test workflows from the system
    * 
    * @return the workflows, never <code>null</code> or empty.
    * 
    * @throws Exception if there are any errors.
    */
   private List<PSWorkflow> loadWorkflows() throws Exception
   {
      List<PSWorkflow> wfs = new ArrayList<PSWorkflow>();
      File base = new File("UnitTestResources/com/percussion/webservices/" +
            "transformation/converter");
      for (int i = 1; i < 4; i++)
      {
         File file = new File(base, "testWorkflow" + i + ".xml");
         
         FileInputStream in = new FileInputStream(file);
         
         try
         {
            PSWorkflow wf = new PSWorkflow();
            String xmlStr = IOUtils.toString(in, 
               PSCharSetsConstants.rxJavaEnc()); 
            wf.fromXML(xmlStr);
            wfs.add(wf);
         }
         finally
         {
            IOUtils.closeQuietly(in);
         }
      }
      
      return wfs;
   }

   /**
    * Validates that the workflow conversion was successful.
    * 
    * @param src the source wf, assumed not <code>null</code>.
    * @param tgt the target wf, assumed not <code>null</code>.
    */
   private void validateConversion(PSWorkflow src, 
      com.percussion.webservices.system.PSWorkflow tgt)
   {
      assertEquals(src.getAdministratorRole(), tgt.getAdministratorRole());
      assertEquals(src.getDescription(), tgt.getDescription());
      assertEquals(src.getName(), tgt.getName());
      assertEquals(src.getGUID().longValue(), tgt.getId().longValue());
      assertEquals(src.getInitialStateId(), tgt.getInitialStateId());
      assertEquals(src.getNotificationDefs().size(), 
         tgt.getNotifications().length);
      assertEquals(src.getRoles().size(), tgt.getRoles().length);
      assertEquals(src.getStates().size(), tgt.getStates().length);
      
      Map<Long, PSState> stateMap = PSWorkflowConverter.getStateMap(src);
      Map<Long, PSWorkflowRole> roleMap = PSWorkflowConverter.getRoleMap(tgt);
      
      // check transition values
      com.percussion.webservices.system.PSState[] states = tgt.getStates();
      for (int i = 0; i < states.length; i++)
      {
         PSTransition[] transitions = states[i].getTransitions();
         assertEquals(src.getStates().get(i).getTransitions().size(), 
            transitions.length);
         for (int j = 0; j < transitions.length; j++)
         {
            PSState state; 
            com.percussion.services.workflow.data.PSTransition srcTrans =
               src.getStates().get(i).getTransitions().get(j);
            state = stateMap.get(srcTrans.getStateId());
            assertEquals(state.getName(), transitions[j].getFromState());
            state = stateMap.get(srcTrans.getToState());
            assertEquals(state.getName(), transitions[j].getToState());
            
            PSWorkflowRole[] roles = transitions[j].getRoles();
            assertEquals(srcTrans.getTransitionRoles().size(), 
               transitions[j].getRoles().length);
            for (int k = 0; k < roles.length; k++)
            {
               com.percussion.services.workflow.data.PSTransitionRole srcRole = 
                  srcTrans.getTransitionRoles().get(k);
               PSWorkflowRole srcWfRole = roleMap.get(srcRole.getRoleId());
               assertEquals(srcWfRole, roles[k]);
            }
         }
         
         PSAgingTransition[] agingtransitions = states[i].getAgingTransitions();
         assertEquals(src.getStates().get(i).getAgingTransitions().size(), 
            agingtransitions.length);
         for (int j = 0; j < agingtransitions.length; j++)
         {
            PSState state; 
            com.percussion.services.workflow.data.PSAgingTransition srcTrans = 
               src.getStates().get(i).getAgingTransitions().get(j);
            state = stateMap.get(srcTrans.getStateId());
            assertEquals(state.getName(), agingtransitions[j].getFromState());
            state = stateMap.get(srcTrans.getToState());
            assertEquals(state.getName(), agingtransitions[j].getToState());
         }         
      }
   }
}

