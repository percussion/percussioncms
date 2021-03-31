/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.workflow;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   Log log = LogFactory.getLog(PSOSpringWorkflowActionDispatcherTest.class);
   
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
