/******************************************************************************
 *
 * [ PSSpringWorkflowActionDispatcher.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
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
