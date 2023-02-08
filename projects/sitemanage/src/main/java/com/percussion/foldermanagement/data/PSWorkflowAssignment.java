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
package com.percussion.foldermanagement.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an Object coming from the client. It has two fields, one is the
 * workflow name, and the other is a list of paths. This object is used to
 * assign the given workflow to all of the folders that are listed.
 * 
 * @author Santiago M. Murchio
 * 
 */
@XmlRootElement(name = "workflowAssignment")
@XmlType(propOrder = {
        "workflowName", 
        "assignedFolders",
        "unassignedFolders",
        "appliedFolders"
    })
@XmlAccessorType(XmlAccessType.FIELD)
public class PSWorkflowAssignment
{
    /**
     * The name of the workflow to assign to each path
     */
    private String workflowName;
    
    /**
     * A list of folder ids. These folders will be assigned to the given workflow. May be empty.
     */
    private String[] assignedFolders;
    
    /**
     * A list of folder ids. These folders are left with no workflow assigned. May be empty.
     */
    private String[] unassignedFolders;
    
    /**
     * A list of folder ids for which the assigned workflow should be applied to all content in the folder.  May be empty.
     */
    private String[] appliedFolders;


    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    public String[] getAssignedFolders()
    {
        if (assignedFolders == null) {
            return new String[]{};
        }
        
        return assignedFolders;
    }

    public void setAssignedFolders(String[] assignedFolders)
    {
        this.assignedFolders = assignedFolders;
    }

    public String[] getUnassignedFolders()
    {
        if (unassignedFolders == null) {
            return new String[]{};
        }
        return unassignedFolders;

    }

    public void setUnassignedFolders(String[] unassignedFolders)
    {
        this.unassignedFolders = unassignedFolders;
    }

    public String[] getAppliedFolders()
    {
        if (appliedFolders == null) {
            return new String[]{};
        }
        
        return appliedFolders;
    }

    public void setAppliedFolders(String[] appliedFolders)
    {
        this.appliedFolders = appliedFolders;
    }
}
