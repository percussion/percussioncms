/******************************************************************************
 *
 * [ PSWFActionService.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workflow.actions;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.services.workflow.data.PSTransitionBase;
import com.percussion.services.workflow.data.PSWorkflow;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the implementation of {@link IPSWFActionService }
 * 
 * @author DavidBenua
 * 
 */
public class PSWFActionService implements IPSWFActionService
{
   /**
    * Logger for class.
    */
   private static Log log = LogFactory.getLog(PSWFActionService.class);

   /**
    * Workflow Info finder.
    */
   private IPSWorkflowInfoFinder wfFinder = null;

   /**
    * Extension Manager for loading any Workflow Actions we find.
    */
   private IPSExtensionManager extMgr = null;

   /**
    * Configuration map. The outmost key is the Workflow Name, the inner key is
    * the TransitionLabel and the values are the list of Workflow Action Names.
    */
   private Map<String, Map<String, List<String>>> transitionActions;

   /**
    * Default Constructor.
    */
   public PSWFActionService()
   {
      transitionActions = new HashMap<String, Map<String, List<String>>>(); // add
                                                                            // empty
                                                                            // map.
   }

   /**
    * Initialization method. Should be called from Spring after initialization
    * is complete.
    */
   public void init()
   {

   }

   /**
    * Internal initialization method.
    */
   private void initServices()
   {
      if (wfFinder == null)
      {
         log.debug("loading new PSWorkflowInfoFinder");
         wfFinder = new PSWorkflowInfoFinder();
      }
      if (extMgr == null)
      {
         log.debug("loading new Extension Manager");
         extMgr = PSServer.getExtensionManager(null);
      }
   }

   /*
    * @see IPSWFActionService#getActions(int, int)
    */
   public List<IPSWorkflowAction> getActions(int workflowid, int transitionid)
      throws Exception
   {
      initServices();
      List<IPSWorkflowAction> actions = new ArrayList<IPSWorkflowAction>();
      PSWorkflow workflow = wfFinder.findWorkflow(workflowid);
      Validate.notNull(workflow, "Workflow not found for id " + workflowid);

      String wfName = workflow.getName();
      Map<String, List<String>> wActions = transitionActions.get(wfName);
      if (wActions == null || wActions.isEmpty())
      {
         log.warn("No actions configured for workflow " + wfName);
         return actions;
      }

      PSTransitionBase trans = wfFinder.findWorkflowAnyTransition(workflow,
            transitionid);
      Validate.notNull(trans, "Transition not found for id " + transitionid);

      String tLabel = trans.getLabel();
      List<String> aNames = wActions.get(tLabel);
      if (aNames == null || aNames.isEmpty())
      {
         log.warn("No actions configured for workflow " + wfName
               + " transition " + tLabel);
         return actions;
      }
      for (String nm : aNames)
      {
         IPSWorkflowAction wfa = getWorkflowAction(nm);
         actions.add(wfa);
      }

      return actions;
   }

   /*
    * @see IPSWFActionService#getWorkflowAction(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public IPSWorkflowAction getWorkflowAction(String workflowActionName)
      throws PSExtensionException, PSNotFoundException
   {
      initServices();
      IPSWorkflowAction ext = null;
      Iterator<PSExtensionRef> itr = extMgr.getExtensionNames("Java", null,
            IPSWorkflowAction.class.getName(), workflowActionName);
      while (itr.hasNext())
      {
         PSExtensionRef ref = itr.next();
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
    * 
    * @return the transitionActions
    */
   public Map<String, Map<String, List<String>>> getTransitionActions()
   {
      return transitionActions;
   }

   /**
    * Sets the map of defined transitions.
    * 
    * @param transitionActions the transitionActions to set
    */
   public void setTransitionActions(
         Map<String, Map<String, List<String>>> transitionActions)
   {
      this.transitionActions = transitionActions;
   }

   /**
    * Sets the extension manager in unit test.
    * 
    * @param extMgr the extMgr to set. Used for unit tests only.
    */
   protected void setExtMgr(IPSExtensionManager extMgr)
   {
      this.extMgr = extMgr;
   }

   /**
    * Sets the workflow info finder in unit test.
    * 
    * @param wfFinder the wfFinder to set. Used for unit tests only.
    */
   protected void setWfFinder(IPSWorkflowInfoFinder wfFinder)
   {
      this.wfFinder = wfFinder;
   }
}
