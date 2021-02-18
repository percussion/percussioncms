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
