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
package com.percussion.workflow.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class contains the structure of the step information. The object is
 * composed of a step name and a list of {@link PSUiWorkflowStepRole}.
 * 
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
@XmlRootElement(name = "WorkflowSteps")
// Define the order in which the fields are written
@XmlType (propOrder={"stepName", "permissionNames", "stepRoles"})
public class PSUiWorkflowStep extends PSAbstractDataObject
{
    private static final long serialVersionUID = -1L;
    
    private String stepName;

    private List<PSUiWorkflowStepRole> stepRoles = new ArrayList<>();
    
    private List<String> permissionNames = new ArrayList<>();

    public PSUiWorkflowStep()
    {
        super();
    }

    /**
     * @return the name of the step
     */
    public String getStepName()
    {
        return stepName;
    }

    /**
     * @param stepName the name of the step
     */
    public void setStepName(String stepName)
    {
        this.stepName = stepName;
    }
    
    /**
     * @return the stepRoles, may be empty but never <code>null</code>
     */
    public List<PSUiWorkflowStepRole> getStepRoles()
    {
        return stepRoles;
    }

    /**
     * @param stepRoles the list of stepRoles to set
     */
    public void setStepRoles(List<PSUiWorkflowStepRole> stepRoles)
    {
        this.stepRoles = stepRoles;
    }
    
    /**
     * @return the permissionNames, may be empty but never <code>null</code>
     */
    public List<String> getPermissionNames()
    {
        return permissionNames;
    }
    
    /**
     * @param permissionNames the list of permission names to set
     */
    public void setPermissionNames(List<String> permissionNames)
    {
        this.permissionNames = permissionNames;
    }
}
