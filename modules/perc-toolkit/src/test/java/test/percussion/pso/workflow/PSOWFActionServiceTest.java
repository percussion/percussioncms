/*******************************************************************************
 * (c) 2005-2011 Percussion Software.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   Log log = LogFactory.getLog(PSOWFActionServiceTest.class);
   
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
