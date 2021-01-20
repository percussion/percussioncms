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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains the structure of the transition for the role in an
 * specific step. The object is composed of a permission.
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
@XmlRootElement(name = "WorkflowStepRoleTransition")
public class PSUiWorkflowStepRoleTransition extends PSAbstractDataObject
{
    private static final long serialVersionUID = -1L;

    private String transitionPermission;

    public PSUiWorkflowStepRoleTransition()
    {
        super();
    }
    
    /**
     * @return the permission for the transition
     */
    public String getTransitionPermission()
    {
        return transitionPermission;
    }

    /**
     * @param transitionPermission
     */
    public PSUiWorkflowStepRoleTransition(String transitionPermission)
    {
        super();
        this.transitionPermission = transitionPermission;
    }

    /**
     * @param the permission for the transition to set
     */
    public void setTransitionPermission(String transitionPermission)
    {
        this.transitionPermission = transitionPermission;
    }
    
}
