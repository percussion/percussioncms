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
        if (assignedFolders == null)
            return new String[] {};
        
        return assignedFolders;
    }

    public void setAssignedFolders(String[] assignedFolders)
    {
        this.assignedFolders = assignedFolders;
    }

    public String[] getUnassignedFolders()
    {
        if (unassignedFolders == null)
            return new String[] {};
        
        return unassignedFolders;
    }

    public void setUnassignedFolders(String[] unassignedFolders)
    {
        this.unassignedFolders = unassignedFolders;
    }

    public String[] getAppliedFolders()
    {
        if (appliedFolders == null)
            return new String[] {};
        
        return appliedFolders;
    }

    public void setAppliedFolders(String[] appliedFolders)
    {
        this.appliedFolders = appliedFolders;
    }
}
