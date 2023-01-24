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
package com.percussion.workflow.actions;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionBase;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.List;

/**
 * This is the implementation of {@link IPSWorkflowInfoFinder }
 * 
 * 
 * @author DavidBenua
 * 
 */
public class PSWorkflowInfoFinder implements IPSWorkflowInfoFinder
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory
         .getLog(PSWorkflowInfoFinder.class);

   private IPSSystemWs m_sws = null;

   private IPSGuidManager m_gmgr = null;

   private List<PSWorkflow> m_workflows = null;

   /**
    * Creates a new finder.
    */
   public PSWorkflowInfoFinder()
   {
      super();
   }

   /**
    * Initializes system services.
    * 
    */
   private void initServices()
   {
      if (m_sws == null)
      {
         m_sws = PSSystemWsLocator.getSystemWebservice();
      }
      if (m_gmgr == null)
      {
         m_gmgr = PSGuidManagerLocator.getGuidMgr();
      }
      if (m_workflows == null)
      {
         m_workflows = m_sws.loadWorkflows(null);
      }
   }

   /*
    * @see IPSWorkflowInfoFinder#findWorkflow(int)
    */
   public PSWorkflow findWorkflow(int id)
   {
      initServices();
      for (PSWorkflow wf : m_workflows)
      {
         if (wf.getGUID().longValue() == id)
         {
            return wf;
         }
      }
      return null;
   }

   /*
    * @see
    * IPSWorkflowInfoFinder#findWorkflowState(com.percussion.services.workflow
    * .data.PSWorkflow, int)
    */
   public PSState findWorkflowState(PSWorkflow wf, int state)
   {
      for (PSState st : wf.getStates())
      {
         long st2 = st.getGUID().longValue();
         if (st2 == state)
         {
            return st;
         }
      }
      return null;
   }

   /*
    * 
    * @see IPSWorkflowInfoFinder#findWorkflowTransition(int, int)
    */
   public PSTransition findWorkflowTransition(int workflow, int transid)
   {
      PSWorkflow wf = findWorkflow(workflow);
      if (wf == null)
      {
         String emsg = "Workflow id " + workflow + " not found";
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      return findWorkflowTransition(wf, transid);
   }

   /*
    * 
    * @see IPSWorkflowInfoFinder#findWorkflowAnyTransition(int, int)
    */
   public PSTransitionBase findWorkflowAnyTransition(int workflow, int transid)
   {
      PSWorkflow wf = findWorkflow(workflow);
      if (wf == null)
      {
         String emsg = "Workflow id " + workflow + " not found";
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      return findWorkflowAnyTransition(wf, transid);
   }

   /*
    * 
    * @see IPSWorkflowInfoFinder#findWorkflowTransition(PSWorkflow, int)
    */
   public PSTransition findWorkflowTransition(PSWorkflow wf, int transid)
   {
      return (PSTransition) findWorkflowAnyTransitionInternal(wf, transid,
            false);
   }

   public PSTransitionBase findWorkflowAnyTransition(PSWorkflow wf, int transid)
   {
      return findWorkflowAnyTransitionInternal(wf, transid, true);
   }

   protected PSTransitionBase findWorkflowAnyTransitionInternal(PSWorkflow wf,
         int transid, boolean includeAging)
   {
      PSTransitionBase result = null;
      for (PSState st : wf.getStates())
      {
         result = findTransitionInternal(st.getTransitions(), transid);
         if (result != null)
            return result;
         if (includeAging)
         {
            result = findTransitionInternal(st.getAgingTransitions(), transid);
            if (result != null)
               return result;
         }
      }
      return null;
   }

   private PSTransitionBase findTransitionInternal(
         List< ? extends PSTransitionBase> transList, long transid)
   {
      for (PSTransitionBase trans : transList)
      {
         long tid = trans.getGUID().longValue();
         if (tid == transid)
         {
            return trans;
         }
      }
      return null;
   }

   /*
    * @see IPSWorkflowInfoFinder#findWorkflowState(int, int)
    */
   public PSState findWorkflowState(int workflow, int state)
   {
      PSWorkflow wf = this.findWorkflow(workflow);
      if (wf == null)
      {
         String emsg = "Workflow id " + workflow + " not found";
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      return findWorkflowState(wf, state);
   }

   /*
    * @see IPSWorkflowInfoFinder#findWorkflowState(java.lang.String)
    */
   public PSState findWorkflowState(String contentId) throws PSException
   {
      PSComponentSummary sum = PSItemSummaryFinder.getSummary(contentId);
      int wfapp = sum.getWorkflowAppId();
      int wfst = sum.getContentStateId();
      return findWorkflowState(wfapp, wfst);
   }

   /*
    * @see IPSWorkflowInfoFinder#findWorkflowStateName(java.lang.String)
    */
   public String findWorkflowStateName(String contentId) throws PSException
   {
      PSState state = findWorkflowState(contentId);
      if (state == null)
      {
         String emsg = "Invalid workflow state for item " + contentId;
         log.error(emsg);
         throw new PSException(emsg);
      }
      return state.getName();
   }

   /*
    * @see IPSWorkflowInfoFinder#IsWorkflowValid(String, Collection)
    */
   public boolean IsWorkflowValid(String contentId,
         Collection<String> validFlags) throws PSException
   {
      PSState state = findWorkflowState(contentId);
      String cvalid = state.getContentValidValue();
      if (StringUtils.isBlank(cvalid))
      {
         String emsg = "Invalid content valid flag for state "
               + state.getName();
         log.error(emsg);
         throw new PSException(emsg);
      }
      for (String v : validFlags)
      {
         if (cvalid.equalsIgnoreCase(v))
         {
            return true;
         }
      }
      return false;
   }

   /*
    * @see IPSWorkflowInfoFinder#findDestinationState(String, String)
    */
   public PSState findDestinationState(String contentId, String transitionId)
      throws PSException
   {
      PSState state = findWorkflowState(contentId);
      return findDestinationState(state, transitionId);
   }

   /*
    * @see IPSWorkflowInfoFinder#findDestinationState(PSState, String)
    */
   public PSState findDestinationState(PSState state, String transitionId)
      throws PSException
   {
      String emsg;
      if (state == null)
      {
         emsg = "State must not be null";
         log.error(emsg);
         throw new PSException(emsg);
      }
      if (StringUtils.isBlank(transitionId))
      {
         emsg = "Transition id must not be null";
         log.error(emsg);
         throw new PSException(emsg);
      }
      long tid;
      try
      {
         tid = Long.parseLong(transitionId);
      }
      catch (NumberFormatException ex)
      {
         emsg = "Invalid transition id " + transitionId;
         log.error(emsg);
         throw new PSException(emsg, ex);
      }
      PSTransitionBase trans = findTransitionInternal(state.getTransitions(),
            tid);
      if (trans == null)
      {
         trans = findTransitionInternal(state.getAgingTransitions(), tid);
      }
      if (trans == null)
      {
         log.warn("no transition found for " + state.getName() + " "
               + transitionId);
         return null;
      }
      PSState dest = findWorkflowState((int) state.getWorkflowId(),
            (int) trans.getToState());
      if (dest == null)
      { // stateid is invalid for this workflow.
         emsg = "no such state " + state.getWorkflowId() + " - "
               + trans.getToState();
         log.error(emsg);
         throw new PSException(emsg);
      }
      log.debug("found destination state " + dest.getName());
      return dest;
   }

   /**
    * Sets the system web service. Should be used only in unit tests.
    * 
    * @param sws The sws to set.
    */
   public void setSws(IPSSystemWs sws)
   {
      this.m_sws = sws;
   }

   /**
    * Sets the workflow list. Should be used only in unit tests.
    * 
    * @param workflows The workflows to set.
    */
   public void setWorkflows(List<PSWorkflow> workflows)
   {
      this.m_workflows = workflows;
   }
}
