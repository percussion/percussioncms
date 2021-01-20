/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
                            // whatever
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