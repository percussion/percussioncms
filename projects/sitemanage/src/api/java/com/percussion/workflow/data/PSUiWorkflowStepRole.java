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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class contains the structure of the step role information. The object is
 * composed of a role name, a role id and a list of
 * {@link PSUiWorkflowStepRoleTransition}.
 * 
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
@XmlRootElement(name = "WorkflowStepRoles")
// Define the order in which the fields are written
@XmlType (propOrder={"roleId", "roleName", "enableNotification", "roleTransitions"})
@JsonRootName("WorkflowStepRoles")
public class PSUiWorkflowStepRole extends PSAbstractDataObject
{
    private static final long serialVersionUID = -1L;

    private String roleName;

    private Integer roleId;
    
    private Boolean enableNotification = false;

    private List<PSUiWorkflowStepRoleTransition> roleTransitions;

    public PSUiWorkflowStepRole()
    {
        super();
    }

    /**
     * @param roleName
     */
    public PSUiWorkflowStepRole(String roleName, int roleId)
    {
        this.roleName = roleName;
        this.roleId = roleId;
    }
    
    /**
     * @param roleName
     */
    public PSUiWorkflowStepRole(String roleName, int roleId, boolean isNotified)
    {
        this.roleName = roleName;
        this.roleId = roleId;
        this.enableNotification = isNotified;
    }

    /**
     * @return the name of the role
     */
    public String getRoleName()
    {
        return roleName;
    }

    /**
     * @param role the name of the role to set
     */
    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    /**
     * @return the id of the role
     */
    public Integer getRoleId()
    {
        return roleId;
    }

    /**
     * @param id the id of the role to set
     */
    public void setRoleId(Integer roleId)
    {
        this.roleId = roleId;
    }

    /**
     * @return the transitions of the role, may be empty but never <code>null</code>
     */
    public List<PSUiWorkflowStepRoleTransition> getRoleTransitions()
    {
        return roleTransitions;
    }

    /**
     * @param transitions the transitions of the role to set, may be empty but never <code>null</code>
     */
    public void setRoleTransitions(List<PSUiWorkflowStepRoleTransition> roleTransitions)
    {
        if (roleTransitions == null)
        {
            roleTransitions = new ArrayList<PSUiWorkflowStepRoleTransition>();
        }
        this.roleTransitions = roleTransitions;
    }
    
    /**
     * @return the value for enable notifications
     */
    public Boolean isEnableNotification()
    {
        return enableNotification;
    }

    /**
     * @param enableNotification the  for enableNotification that indicates whether it is enabled or not
     */
    public void setEnableNotification(Boolean enableNotification)
    {
        this.enableNotification = enableNotification;
    }
}
