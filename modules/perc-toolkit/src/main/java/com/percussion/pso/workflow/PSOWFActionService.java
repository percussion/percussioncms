/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.pso.utils.RxServerUtils;
import com.percussion.server.PSServer;
import com.percussion.services.workflow.data.PSTransitionBase;
import com.percussion.services.workflow.data.PSWorkflow;

/**
 * Implementation of the PSOWFActionService. This service is designed
 * to be called from a workflow action dispatcher. It will return the
 * list of IPSWorkflowActions defined for a particular workflow transition.
 * <p> 
 * <b>Note:</b> unlike the original PSOWFActionDispatcher which matched on workflow id
 * and transition id, this implementation matches on Workflow Name and 
 * Transition Label.  While the Workflow Name is unique, it is possible to have
 * more than one transition with the same label.  In this case, the workflow 
 * actions retrieved from this service will match ALL transitions with that label.
 * <p>
 * If this is a problem, make sure that you give your transitions unique labels. In 
 * many cases, you can use this functionality to your advantage. For example, You can 
 * define a single set of actions for all of your "return to public" transitions.  
 * <p>
 * The <code>transitionActions</code> property is a <code>Map</code> of 
 * <code>Maps</code> of <code>Lists</code> of <code>Strings</code>. The 
 * outer map is keyed by the Workflow Name, and the inner map is keyed 
 * by the Transition Label.  The Strings are the names of the Workflow 
 * Actions that you wish to run on that Transition. 
 * <p>
 * The bean configuration will look something like this: 
 * <pre>
   &lt;bean id="psoWFActionService" class="com.percussion.pso.workflow.PSOWFActionService"
      init-method="init"&gt;
       &lt;property name="transitionActions"&gt;
          &lt;map&gt;
              &lt;entry key="workflow1"&gt;
                  &lt;map&gt;
                     &lt;entry key="transition1"&gt;
                         &lt;list&gt;
                            &lt;value&gt;action1&lt;/value&gt;
                            &lt;value&gt;action2&lt;/value&gt;
                         &lt;/list&gt;
                     &lt;/entry&gt; 
                  &lt;/map&gt;
              &lt;/entry&gt;
          &lt;/map&gt;          
       &lt;/property&gt;
    &lt;/bean&gt;
 
 *</pre>
 * <p>
 * A sample file can be found at 
 * /WEB-INF/config/user/spring/PSOSpringWorkflowActionDispatcher-beans.xml
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOWFActionService implements IPSOWFActionService
{
   /**
    * Logger for class.
    */
   private static Log log = LogFactory.getLog(PSOWFActionService.class);
   
   /**
    * Workflow Info finder. 
    */
   private IPSOWorkflowInfoFinder wfFinder = null;
   /**
    * Extension Manager for loading any Workflow Actions we find. 
    */
   private IPSExtensionManager extMgr = null; 
   
   
   /**
    * Configuration map. The outmost key is the Workflow Name, the inner key is the TransitionLabel 
    * and the values are the list of Workflow Action Names.  
    */
   private Map<String, Map<String,List<String>>> transitionActions; 
   /**
    * Default Constructor.
    */
   public PSOWFActionService()
   {
      transitionActions = new HashMap<String, Map<String,List<String>>>(); //add empty map.
   }
   
   /**
    * Initialization method. Should be called from Spring after initialization is complete. 
    */
   public void init()
   {
      /*
       * The reason for this complexity is that when the init method
       * is called (by Spring), the Percussion CMS server is not yet running,
       * and the Extension manager is NULL.  We have to launch a new 
       * thread which waits for the server to initialize before calling
       * the internal initServices() routine.  
       */
      Thread t = new Thread()
        {
        @Override
        public void run()
          {
            try
            {
               RxServerUtils.waitForServerReady();
               initServices();
            } catch (InterruptedException ex)
            { //should only happen if the server startup fails.  
              //Not much to do, as we are going down regardless.  
              log.error("Unexpected Interruption " + ex.getMessage(),ex);
            }
          }
        };
        
//  THIS DOES NOT WORK ON LINUX, so will be temporarily disabled. 
//      log.debug("Starting init thread..."); 
//      t.start(); 
   }
   
   /**
    * Internal initialization method.
    */
   private void initServices()
   {
      if(wfFinder == null)
      {
         log.debug("loading new PSOWorkflowInfoFinder");
         wfFinder = new PSOWorkflowInfoFinder(); 
      }
      if(extMgr == null)
      {
         log.debug("loading new Extension Manager");
         extMgr = PSServer.getExtensionManager(null); 
      }
   }

   /**
    * @see com.percussion.pso.workflow.IPSOWFActionService#getActions(int, int)
    */
   public List<IPSWorkflowAction> getActions(int workflowid, int transitionid) throws Exception
   {
      initServices(); 
      List<IPSWorkflowAction> actions = new ArrayList<IPSWorkflowAction>();
      PSWorkflow  workflow = wfFinder.findWorkflow(workflowid);
      Validate.notNull(workflow,"Workflow not found for id " + workflowid); 
     
      String wfName = workflow.getName(); 
      Map<String,List<String>> wActions = transitionActions.get(wfName);
      if(wActions == null || wActions.isEmpty())
      {
         log.warn("No actions configured for workflow "  + wfName); 
         return actions;
      }
      
      PSTransitionBase trans = wfFinder.findWorkflowAnyTransition(workflow, transitionid); 
      Validate.notNull(trans, "Transition not found for id " + transitionid); 
 
      String tLabel = trans.getLabel(); 
      List<String> aNames =  wActions.get(tLabel);
      if(aNames == null || aNames.isEmpty())
      {
         log.warn("No actions configured for workflow " + wfName + " transition " + tLabel);
         return actions; 
      }
      for(String nm : aNames)
      {
         IPSWorkflowAction wfa = getWorkflowAction(nm); 
         actions.add(wfa); 
      }
      
      return actions; 
   }
   
   /**
    * @see com.percussion.pso.workflow.IPSOWFActionService#getWorkflowAction(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public IPSWorkflowAction getWorkflowAction(String workflowActionName) 
      throws PSExtensionException, PSNotFoundException
   {
      initServices();
      IPSWorkflowAction ext = null; 
      Iterator<PSExtensionRef> itr =  extMgr.getExtensionNames("Java",null,IPSWorkflowAction.class.getName(),
            workflowActionName);
      while(itr.hasNext())
      {
         PSExtensionRef ref  = itr.next();
         log.debug("found extension " + ref.getFQN()); 
         ext = (IPSWorkflowAction) extMgr.prepareExtension(ref, null);
         log.debug("prepared extension " + ext.getClass().getCanonicalName()); 
         return ext;
      }  
      log.error("Extension name " + workflowActionName + " was not found "); 
      return ext;
   }
   
   /**
    * Gets the map of defined transitions. 
    * @return the transitionActions
    */
   public Map<String, Map<String, List<String>>> getTransitionActions()
   {
      return transitionActions;
   }

   /**
    * Sets the map of defined transitions.  
    * @param transitionActions the transitionActions to set
    */
   public void setTransitionActions(
         Map<String, Map<String, List<String>>> transitionActions)
   {
      this.transitionActions = transitionActions;
   }

   /**
    * Sets the extension manager in unit test.
    * @param extMgr the extMgr to set. Used for unit tests only.
    */
   protected void setExtMgr(IPSExtensionManager extMgr)
   {
      this.extMgr = extMgr;
   }

   /**
    * Sets the workflow info finder in unit test.
    * @param wfFinder the wfFinder to set. Used for unit tests only.
    */
   protected void setWfFinder(IPSOWorkflowInfoFinder wfFinder)
   {
      this.wfFinder = wfFinder;
   }

   
}
