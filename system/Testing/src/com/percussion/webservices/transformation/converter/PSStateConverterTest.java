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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

