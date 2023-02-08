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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.workflow.data.PSAgingTransition;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;

/**
 * Test the {@link PSStateConverter}
 */
public class PSStateConverterTest extends TestCase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails
    */
   public void testConverter() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSState.class);
      
      PSState src = new PSState();
      List<PSAgingTransition> agingTrans = new ArrayList<PSAgingTransition>();
      agingTrans.add(new PSAgingTransition());
      agingTrans.add(new PSAgingTransition());
      src.setAgingTransitions(agingTrans);
      List<PSAssignedRole> roleList = new ArrayList<PSAssignedRole>();
      roleList.add(new PSAssignedRole());
      roleList.add(new PSAssignedRole());
      src.setAssignedRoles(roleList);
      src.setDescription("desc");
      src.setStateId(123);
      src.setName("state");
      src.setPublishable(true);
      src.setSortOrder(3);
      List<PSTransition> transitionList = new ArrayList<PSTransition>();
      transitionList.add(new PSTransition());
      transitionList.add(new PSTransition());
      src.setTransitions(transitionList);
      src.setWorkflowId(456);
      
      com.percussion.webservices.system.PSState tgt = 
         (com.percussion.webservices.system.PSState) converter.convert(
            com.percussion.webservices.system.PSState.class, src);
      
      assertEquals(src.getAgingTransitions().size(), 
         tgt.getAgingTransitions().length);
      assertEquals(src.getAssignedRoles().size(), 
         tgt.getAssignedRoles().length);
      assertEquals(src.getDescription(), tgt.getDescription());
      assertEquals(src.getName(), tgt.getName());
      Integer var1 = src.getSortOrder();
      Integer var2 = tgt.getSortOrder();
      assertEquals(var1,var2);
      assertEquals(src.getTransitions().size(), tgt.getTransitions().length);
   }
}

