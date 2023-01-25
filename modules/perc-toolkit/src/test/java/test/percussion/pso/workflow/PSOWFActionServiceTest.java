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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionRef;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.pso.workflow.PSOWFActionService;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;

public class PSOWFActionServiceTest
{
   private static final Logger log = LogManager.getLogger(PSOWFActionServiceTest.class);
   
   Mockery context;
   
   TestablePSOWFActionService cut; 
   IPSExtensionManager emgr;
   IPSOWorkflowInfoFinder wfFinder; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      cut = new TestablePSOWFActionService();
      emgr = context.mock(IPSExtensionManager.class);
      cut.setExtMgr(emgr);
      wfFinder = context.mock(IPSOWorkflowInfoFinder.class);
      cut.setWfFinder(wfFinder); 
   }
   @Test
   public final void testGetWorkflowAction()
   {
      final PSExtensionRef ref = new PSExtensionRef("java/user/xyz");
      final IPSWorkflowAction action = context.mock(IPSWorkflowAction.class); 
     
      try
      {
         
         context.checking(new Expectations(){{            
            one(emgr).getExtensionNames("Java", null, IPSWorkflowAction.class.getName(), "xyz");
            will(returnIterator(new PSExtensionRef[]{ref}));
            one(emgr).prepareExtension(ref, null);
            will(returnValue(action));
         }});
          
         
         IPSWorkflowAction result = cut.getWorkflowAction("xyz");
         assertNotNull(result);
         assertEquals(action,result); 
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception");
      }
   }
   @Test
   public final void testGetTransitionActions()
   {
      final PSExtensionRef ref = new PSExtensionRef("java/user/xyz");
      final IPSWorkflowAction action = context.mock(IPSWorkflowAction.class); 
      Map<String, Map<String,List<String>>> wconfig = new HashMap<String, Map<String,List<String>>>();
      Map<String,List<String>> tconfig = new HashMap<String, List<String>>();
      List<String> aconfig = new ArrayList<String>();
      aconfig.add("xyz");
      tconfig.put("trans1", aconfig); 
      wconfig.put("wf1", tconfig);
      
      cut.setTransitionActions(wconfig); 
      
      final PSWorkflow wf = context.mock(PSWorkflow.class); 
      final PSTransition trans = context.mock(PSTransition.class); 
      try
      {
         context.checking(new Expectations(){{
            one(wfFinder).findWorkflow(1);
            will(returnValue(wf));
            one(wf).getName();
            will(returnValue("wf1"));
            one(wfFinder).findWorkflowAnyTransition(wf, 1);
            will(returnValue(trans)); 
            one(trans).getLabel();
            will(returnValue("trans1")); 
            one(emgr).getExtensionNames("Java", null, IPSWorkflowAction.class.getName(), "xyz");
            will(returnIterator(new PSExtensionRef[]{ref}));
            one(emgr).prepareExtension(ref, null);
            will(returnValue(action));
         }});
          
         
         List<IPSWorkflowAction> result = cut.getActions(1, 1);
         assertNotNull(result);
         assertEquals(1,result.size());  
         IPSWorkflowAction act2 = result.get(0);
         assertEquals(action, act2); 
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception");
      }
   }
   
   private class TestablePSOWFActionService extends PSOWFActionService
   {

      /**
       * @see com.percussion.pso.workflow.PSOWFActionService#setExtMgr(com.percussion.extension.IPSExtensionManager)
       */
      @Override
      public void setExtMgr(IPSExtensionManager extMgr)
      {
         super.setExtMgr(extMgr);
      }

      /**
       * @see com.percussion.pso.workflow.PSOWFActionService#setWfFinder(com.percussion.pso.workflow.IPSOWorkflowInfoFinder)
       */
      @Override
      public void setWfFinder(IPSOWorkflowInfoFinder wfFinder)
      {
         super.setWfFinder(wfFinder);
      }
      
   }
}
