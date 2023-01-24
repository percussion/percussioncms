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
package com.percussion.workflow.web.service;

import com.percussion.share.data.PSEnumVals;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.workflow.data.PSUiWorkflow;

import java.util.List;

/**
 * @author leonardohildt
 * @author rafaelsalis
 *
 */
public class PSSteppedWorkflowRestServiceClient extends PSDataServiceRestClient<PSUiWorkflow>
{

    /**
     * 
     * @param url
     */
    public PSSteppedWorkflowRestServiceClient(String url)
    {        
        super(PSUiWorkflow.class, url, "/Rhythmyx/services/workflowmanagement/workflows/");
    }

    public PSUiWorkflow getWorkflow(String workflowName)
    {
        return getObjectFromPath(concatPath(getPath(), "", workflowName), PSUiWorkflow.class);
    }
    
    public PSEnumVals getWorkflowList()
    {
        return getObjectFromPath(getPath(), PSEnumVals.class);
    }
    
    public List<PSUiWorkflow> getWorkflowMetadataList()
    {
        return getObjectsFromPath(concatPath(getPath(), "/metadata"));
    }
    
    public PSUiWorkflow createWorkflow(String workflowName, PSUiWorkflow uiWorkflow)
    {
        postObjectToPath(concatPath(getPath(), workflowName), uiWorkflow);
        return getWorkflow(workflowName);
    }
    
    public PSUiWorkflow updateWorkflow(String workflowName, PSUiWorkflow uiWorkflow)
    {
        putObjectToPath(concatPath(getPath(), workflowName), uiWorkflow);
        String newWorkflowName = uiWorkflow.getWorkflowName();
        return getWorkflow(newWorkflowName); 
    }
    
    public void deleteWorkflow(String workflowName)
    {
        try
        {
            delete(workflowName);
        }
        catch (RuntimeException e)
        {
            if (e.getLocalizedMessage().contains("has items assigned to it"))
            {
                // too fast for reassignment of content, need to wait and try again
                for (int i = 0; i < 3; i++)
                {
                    try
                    {
                        delete(workflowName);
                        return;
                    }
                    catch (Exception e1)
                    {
                        try
                        {
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException ie)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            // if we get here, rethrow original
            throw e;
        }
    }
    
    public PSUiWorkflow createStep(String workflowName, String stateName, PSUiWorkflow uiWorkflow)
    {
        postObjectToPath(concatPath(getPath(), workflowName, "steps", stateName), uiWorkflow);
        return getWorkflow(uiWorkflow.getWorkflowName());        
    }
    
    public PSUiWorkflow updateStep(String workflowName, String stateName, PSUiWorkflow uiWorkflow)
    {
        putObjectToPath(concatPath(getPath(), workflowName, "steps", stateName), uiWorkflow);
        return getWorkflow(uiWorkflow.getWorkflowName());        
    }
    
    public PSUiWorkflow deleteState(String workflowName, String stateName)
    {
        delete(concatPath(workflowName, "steps", stateName));
        return getWorkflow(workflowName);
    }
    
    public PSEnumVals getStatesChoices(String workflowName)
    {
        return getObjectFromPath(concatPath(getPath(), workflowName, "states/choices"), PSEnumVals.class);
    }
    
}
