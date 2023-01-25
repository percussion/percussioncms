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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.List;


/**
 * A workflow action based on the PSWFActionService bean. 
 * 
 *
 * @author DavidBenua
 * @see IPSWFActionService
 */
public class PSSpringWorkflowActionDispatcher extends PSDefaultExtension
    implements IPSWorkflowAction 
{
    private static final Log m_log = LogFactory.getLog(PSSpringWorkflowActionDispatcher.class);
    IPSWFActionService m_asvc = null; 
    
    /**
     * Default constructor
     */
    public PSSpringWorkflowActionDispatcher()
    {
        
    }
    /*
     * @see IPSWorkflowAction#performAction(IPSWorkFlowContext, IPSRequestContext)
     */
    @Override
   public void init(IPSExtensionDef extensionDef, File codeRoot)
        throws PSExtensionException
    {
        super.init(extensionDef, codeRoot);
        m_log.debug("Initializing WFActionDispatcher...");
        
        if(m_asvc == null)
        {
           m_asvc = PSWFActionServiceLocator.getPSWFActionService(); 
        }
    }

    /*
     * @see IPSWorkflowAction#performAction(IPSWorkFlowContext, IPSRequestContext)
     */
    public void performAction(IPSWorkFlowContext wfContext, IPSRequestContext request)
        throws PSExtensionProcessingException
    {

        int transitionId = wfContext.getTransitionID();
        int workflowId = wfContext.getWorkflowID();
        m_log.debug("Workflow id: " + workflowId);
        m_log.debug("Transition Id: " + transitionId);
        try
        {
           List<IPSWorkflowAction> actions = m_asvc.getActions(workflowId, transitionId);
           for(IPSWorkflowAction act : actions)
           {
              m_log.debug("performing action " + act.getClass().getCanonicalName()); 
              act.performAction(wfContext, request); 
           }
           m_log.debug("finished actions"); 
        }
        catch(Exception nfx)
        {
            m_log.error("unknown error " + nfx.getMessage(),nfx);
            throw new PSExtensionProcessingException("PSWFActionDispatcher", nfx);
        }
    }

   /**
    * @param asvc the asvc to set. Used for unit test only. 
    */
   protected void setAsvc(IPSWFActionService asvc)
   {
      this.m_asvc = asvc;
   }
}
