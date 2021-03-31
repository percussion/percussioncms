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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.design.objectstore.PSNotFoundException;
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
    private static final Log log = LogFactory.getLog(PSOWFActionDispatcher.class);
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
        log.debug("Content id: " + contentId);
        log.debug("Workflow id: " + workflowId);
        log.debug("Transition Id: " + transitionId);
        //int contentType = 0;
        try
        {
            //contentType = getContentType(contentId, request);
        }
        catch(Exception e)
        {            
            log.error("WFActionDispatcher::performAction", e);
        }
        try
        {
            //Object actions[] = getWorkflowActions(contentType, transitionId);
        	List<String> actions = getWorkflowActions(workflowId, transitionId);
            log.debug("found " + actions.size() + " actions to execute."); 
            for(String action : actions)
            {
                log.debug("Executing " + action + "... ");
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
            log.error("WFActionDispatcher::performAction",nfx);
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
                log.error("Could not find property " + workflowId + ":" + transitionId + " in " + "rxconfig/Workflow/dispatcher.properties");
            }
        }
        catch(FileNotFoundException fex)
        {
            log.error("Properties file not found: rxconfig/Workflow/dispatcher.properties", fex);
            //fex.printStackTrace();
        }
        catch(IOException ex)
        {
            log.error("Properties file could not be opened: rxconfig/Workflow/dispatcher.properties",ex);
            //ex.printStackTrace();
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
          log.debug("found extension " + ref.getFQN()); 
          ext = extMgr.prepareExtension(ref, null);
          log.debug("prepared extension " + ext.getClass().getCanonicalName()); 
          return ext;
       }  
       log.error("Extension name " + workflowActionName + " was not found "); 
       return ext;
    }

    IPSExtensionDef m_extensionDef;
    File m_codeRoot;
}
