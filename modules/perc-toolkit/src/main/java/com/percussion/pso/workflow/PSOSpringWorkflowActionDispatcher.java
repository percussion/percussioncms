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

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;


/**
 * A workflow action based on the PSOWFActionService bean. 
 * 
 *
 * @author DavidBenua
 * @see IPSOWFActionService
 */
public class PSOSpringWorkflowActionDispatcher extends PSDefaultExtension
    implements IPSWorkflowAction 
{
    private static final Log log = LogFactory.getLog(PSOSpringWorkflowActionDispatcher.class);
    IPSOWFActionService asvc = null; 
    
    /**
     * Default constructor
     */
    public PSOSpringWorkflowActionDispatcher()
    {
        
    }

    /**
     * 
     * @see com.percussion.extension.PSDefaultExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    public void init(IPSExtensionDef extensionDef, File codeRoot)
        throws PSExtensionException
    {
        super.init(extensionDef, codeRoot);
        log.debug("Initializing WFActionDispatcher...");
        
        if(asvc == null)
        {
           asvc = PSOWFActionServiceLocator.getPSOWFActionService(); 
        }
    }

    /**
     * Perform the action. 
     * @see com.percussion.extension.IPSWorkflowAction#performAction(com.percussion.extension.IPSWorkFlowContext, com.percussion.server.IPSRequestContext)
     */
    public void performAction(IPSWorkFlowContext wfContext, IPSRequestContext request)
        throws PSExtensionProcessingException
    {

        int transitionId = wfContext.getTransitionID();
        int workflowId = wfContext.getWorkflowID();
        log.debug("Workflow id: " + workflowId);
        log.debug("Transition Id: " + transitionId);
        try
        {
           List<IPSWorkflowAction> actions = asvc.getActions(workflowId, transitionId);
           for(IPSWorkflowAction act : actions)
           {
              log.debug("performing action " + act.getClass().getCanonicalName()); 
              act.performAction(wfContext, request); 
           }
           log.debug("finished actions"); 
        }
        catch(Exception nfx)
        {
            log.error("unknown error " + nfx.getMessage(),nfx);
            throw new PSExtensionProcessingException("PSOWFActionDispatcher", nfx);
        }
    }

   /**
    * @param asvc the asvc to set. Used for unit test only. 
    */
   protected void setAsvc(IPSOWFActionService asvc)
   {
      this.asvc = asvc;
   }
}
