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
package test.percussion.pso.workflow;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.pso.workflow.IPSOWFActionService;
import com.percussion.pso.workflow.PSOSpringWorkflowActionDispatcher;
import com.percussion.server.IPSRequestContext;

public class PSOSpringWorkflowActionDispatcherTest
{
   private static final Logger log = LogManager.getLogger(PSOSpringWorkflowActionDispatcherTest.class);
   
   Mockery context;
   TestablePSOSpringWorkflowActionDispatcher cut; 
   IPSOWFActionService asvc; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new TestablePSOSpringWorkflowActionDispatcher();
      asvc = context.mock(IPSOWFActionService.class);
      cut.setAsvc(asvc);       
   }
   
   @Test
   public final void testPerformAction()
   {
      final IPSRequestContext request = context.mock(IPSRequestContext.class);
      final IPSWorkFlowContext wfContext = context.mock(IPSWorkFlowContext.class);
      final IPSWorkflowAction action = context.mock(IPSWorkflowAction.class);
      final List<IPSWorkflowAction> acts = new ArrayList<IPSWorkflowAction>();
      acts.add(action); 
      try
      {
         context.checking(new Expectations(){{
            one(wfContext).getWorkflowID();
            will(returnValue(1));
            one(wfContext).getTransitionID();
            will(returnValue(2));
            one(asvc).getActions(1, 2);
            will(returnValue(acts));
            one(action).performAction(wfContext, request); 
         }});
         
         cut.performAction(wfContext, request);
         
         context.assertIsSatisfied();
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      } 
   }
   
   private class TestablePSOSpringWorkflowActionDispatcher extends PSOSpringWorkflowActionDispatcher
   {

      @Override
      public void setAsvc(IPSOWFActionService asvc)
      {
         super.setAsvc(asvc);
      }
      
   }
}
