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
package com.percussion.workflow.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

/**
 * This class contains the structure of the object returned by the Rest method
 * located in sitemanage project. The object is composed of a workflow name
 * and a list of {@link PSUiWorkflowStep}.
 * 
 * @author leonardohildt
 * @author rafaelsalis
 *
 */
@XmlRootElement(name = "Workflow")
public class PSUiWorkflow extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;

    private String workflowName = "";
    
    private String workflowDescription = "";
    
    private String stagingRoleNames = "";
    
    private Boolean defaultWorkflow = false;
    
    /* 
     * If it is an update takes the workflow name value before the modification 
     * and is used to identify the workflow to update.
     */
    private String previousWorkflowName = "";
    
    /* 
     * If it is a creation takes the previous step name value in the workflow 
     * and used to know where to insert the new step.
     * If it is an update takes the step name value before the modification 
     * and is used to identify the step to update.
     */
    private String previousStepName = "";
        
    private List<PSUiWorkflowStep> workflowSteps = new ArrayList<>();
    
    public PSUiWorkflow()
    {
        this("", new ArrayList<>());
    }
    
    public PSUiWorkflow(String workflowName, List<PSUiWorkflowStep> workflowSteps)
    {
        this.workflowName = workflowName;
        this.workflowSteps = workflowSteps;
    }
    
    /**
     * @return the workflowName
     */
    public String getWorkflowName()
    {
        return workflowName;
    }

    /**
     * @param workflowName the workflowName to set
     */
    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }
    
    /**
     * @return the workflow description
     */
    public String getWorkflowDescription()
    {
        return workflowDescription;
    }

    /**
     * @param workflowDescription the workflowDescription to set
     */
    public void setWorkflowDescription(String workflowDescription)
    {
        this.workflowDescription = workflowDescription;
    }

    /**
     * @return the workflowSteps, may be empty but never <code>null</code>
     */
    public List<PSUiWorkflowStep> getWorkflowSteps()
    {
        return workflowSteps;
    }

    /**
     * @param workflowSteps the workflowSteps to set
     */
    public void setWorkflowSteps(List<PSUiWorkflowStep> workflowSteps)
    {
        this.workflowSteps = workflowSteps;
    }

    /**
     * @return the name of the previous workflow
     */
    public String getPreviousWorkflowName()
    {
        return previousWorkflowName;
    }

    /**
     * @param previousWorkflowName the name of the previous workflow to set, may be <code>null</code>
     */
    public void setPreviousWorkflowName(String previousWorkflowName)
    {
        this.previousWorkflowName = previousWorkflowName;
    }
    
    /**
     * @return the name of the previous workflow
     */
    public String getPreviousStepName()
    {
        return previousStepName;
    }

    /**
     * @param previousStepName the name of the previous workflow to set, may be <code>null</code>
     */
    public void setPreviousStepName(String previousStepName)
    {
        this.previousStepName = previousStepName;
    }
    
    /**
     * @return the value for default workflow
     */
    public Boolean isDefaultWorkflow()
    {
        return defaultWorkflow;
    }

    /**
     * @param defaultWorkflow the value for defaultWorkflow that indicates whether it is default workflow or not
     */
    public void setDefaultWorkflow(Boolean defaultWorkflow)
    {
        this.defaultWorkflow = defaultWorkflow;
    }

    /**
     * Semicolon separated list of role names.
     * 
     * @return never <code>null</code> may be empty.
     */
    public String getStagingRoleNames() 
    {
        return StringUtils.defaultString(stagingRoleNames);
    }

    /**
     * Semicolon separated list of staging role names.
     * 
     * @param stagingRoleNames
     *            if <code>null</code> will be set to empty string.
     */
    public void setStagingRoleNames(String stagingRoleNames) 
    {
        this.stagingRoleNames = StringUtils.defaultString(stagingRoleNames);
    }

}
