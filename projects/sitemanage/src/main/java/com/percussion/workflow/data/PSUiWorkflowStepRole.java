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
            roleTransitions = new ArrayList<>();
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
