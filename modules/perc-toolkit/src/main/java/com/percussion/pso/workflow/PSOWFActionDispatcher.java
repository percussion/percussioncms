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
package com.percussion.pso.workflow;

import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This is the legacy version of the PSOWFActionDispatcher. 
 * <p>
 * For new implementations consider the 
 * {@link com.percussion.pso.workflow.PSOSpringWorkflowActionDispatcher} 
 * instead. 
 * <p>
 * To use this version.
 * <ul>
 * <li>Log into the Content Explorer.</li> 
 * <li>Go to the Workflow tab</li>
 * <li>Select the Workflow that will use the dispatcher (note the workflow ID)
 * <li>Select the State that will contain the transition with the dispatcher
 * <li>Select the Transition to apply the dispatcher to (note the transition ID)
 * <li>Save the Transition</li>
 * </ul>
 * <p>
 *In the file system:
 * <ul>
 * <li>Go to rxconfig/Workflow and open dispatcher.properties
 * <li>Create a new line for each transition that uses the dispatcher: 
 * <pre>
 *  Sample: 5|4:psoSampleWFAction,sys_TouchParentItems
 *  WFID|TRID:wfAction, wfAction
 * </pre>
 * </ul>
 *
 * @author DavidBenua
 *
 */
public class PSOWFActionDispatcher extends PSDefaultExtension
    implements IPSWorkflowAction 
{
    private static final Logger log = LogManager.getLogger(PSOWFActionDispatcher.class);
    public PSOWFActionDispatcher()
    {
        m_extensionDef = null;
        m_codeRoot = null;
    }

    public void init(IPSExtensionDef extensionDef, File codeRoot)
        throws PSExtensionException
    {
        log.debug("Initializing WFActionDispatcher...");
        m_extensionDef = extensionDef;
        m_codeRoot = codeRoot;
    }

    public void performAction(IPSWorkFlowContext wfContext, IPSRequestContext request)
        throws PSExtensionProcessingException
    {
        String sName = "performAction";
        log.debug("WFActionDispatcher::performAction executing...");
        boolean bOK = true;
        int contentId = 0;
        try
        {
            contentId = Integer.parseInt(request.getParameter("sys_contentid"));
        }
        catch(NumberFormatException nfex)
        {
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", nfex);
        }

        int transitionId = wfContext.getTransitionID();
        int workflowId = wfContext.getWorkflowID();
        log.debug("Content id: {}", contentId);
        log.debug("Workflow id: {}", workflowId);
        log.debug("Transition Id: {}", transitionId);
        //int contentType = 0;
        try
        {
            //contentType = getContentType(contentId, request);
        }
        catch(Exception e)
        {            
            log.error("WFActionDispatcher::performAction. Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
        try
        {
            //Object actions[] = getWorkflowActions(contentType, transitionId);
        	List<String> actions = getWorkflowActions(workflowId, transitionId);
            log.debug("found {} actions to execute.", actions.size());
            for(String action : actions)
            {
                log.debug("Executing {}..." , action);
                IPSWorkflowAction wfaction = (IPSWorkflowAction)this.getExtension(action, 
                      IPSWorkflowAction.class.getName());
                if(wfaction != null)
                {
                   wfaction.performAction(wfContext, request);
                }
            }

        }
        catch(Exception nfx)
        {
            log.error("WFActionDispatcher::performAction, Error: {}",nfx.getMessage());
            log.debug(nfx.getMessage(), nfx);
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", nfx);
        }
        log.debug("WFActionDispatcher::performAction done");
    }

    private List<String> getWorkflowActions(int workflowId, int transitionId)
        throws PSExtensionProcessingException
    {
        String PROP_DELIMITER = ",";
        String VALUE_DELIMITER = "|";
        List<String> actions = new ArrayList<String>();
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream("rxconfig/Workflow/dispatcher.properties"));
            String sActions = props.getProperty(workflowId + "|" + transitionId);
            if(actions != null)
            {
                for(StringTokenizer st = new StringTokenizer(sActions, ","); st.hasMoreTokens(); actions.add(st.nextToken()));
            } else
            {
                log.error("Could not find property {} : {} in rxconfig/Workflow/dispatcher.properties", workflowId, transitionId);
            }
        }
        catch(FileNotFoundException fex)
        {
            log.error("Properties file not found: rxconfig/Workflow/dispatcher.properties, Error: {}", fex.getMessage());
            log.debug(fex.getMessage(), fex);
        }
        catch(IOException ex)
        {
            log.error("Properties file could not be opened: rxconfig/Workflow/dispatcher.properties, Error: {}",ex.getMessage());
            log.debug(ex.getMessage(), ex);
        }
        finally
        {
            if(props != null)
                props.clear();
        }
        return actions;
    }

 
    @SuppressWarnings("unchecked")
    private IPSExtension getExtension(String workflowActionName, String interfaceName) 
       throws PSExtensionException, PSNotFoundException
    {
       IPSExtension ext = null; 
       IPSExtensionManager extMgr = PSServer.getExtensionManager(null);         
       Iterator<PSExtensionRef> itr =  extMgr.getExtensionNames("Java",null,interfaceName,
             workflowActionName);
       while(itr.hasNext())
       {
          PSExtensionRef ref  = itr.next();
          log.debug("found extension {}", ref.getFQN());
          ext = extMgr.prepareExtension(ref, null);
          log.debug("prepared extension {}", ext.getClass().getCanonicalName());
          return ext;
       }  
       log.error("Extension name {} was not found", workflowActionName);
       return ext;
    }

    IPSExtensionDef m_extensionDef;
    File m_codeRoot;
}
