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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class contains the workflow state and all possible transitions for an item.
 * The transition trigger names (along with the item ID) can be used to 
 * transition the item into different state upon request. 
 *
 * @author yubingchen
 */
@XmlRootElement(name="ItemStateTransition")
public class PSItemStateTransition extends PSAbstractDataObject
{
    /**
     * Gets the ID of the item.
     * 
     * @return ID of the item, not blank for a valid object.
     */
    public String getItemId()
    {
        return itemId;
    }
    
    /**
     * Sets the ID of the item.
     * 
     * @param id the new ID of the item, not blank for a valid object.
     */
    public void setItemId(String id)
    {
        this.itemId = id;
    }
    
    /**
     * The nominal workflow state of the item.
     * 
     * @return never <code>null</code>.
     */
    public String getStateName()
    {
        return stateName;
    }

    /**
     * @param stateName never <code>null</code>.
     */
    public void setStateName(String stateName)
    {
        this.stateName = stateName;
    }

    /**
     * Gets the ID of the workflow state for the current item.
     *   
     * @return the state ID, not blank for a valid object.
     */
    public String getStateId()
    {
        return stateId;
    }
    
    /**
     * Sets the workflow state ID.
     * 
     * @param id the state ID, not blank for a valid object.
     */
    public void setStateId(String id)
    {
        stateId = id;
    }
    
    /**
     * Gets the ID of the workflow that contains the specified state
     * see {@link #getStateId()}.
     * 
     * @return the workflow ID, not blank for a valid object.
     */
    public String getWorkflowId()
    {
        return workflowId;
    }
    
    /**
     * Sets the ID of the workflow.
     * 
     * @param id the new workflow ID, not blank for a valid object.
     */
    public void setWorkflowId(String id)
    {
        workflowId = id;
    }
    
    /**
     * Gets all transition trigger names for the current workflow state.
     * 
     * @return all possible transition triggers, never <code>null</code>, but
     * may be empty.
     */
    public List<String> getTransitionTriggers()
    {
        return transitionTriggers;
    }
    
    /**
     * Sets the transition triggers for the current workflow state.
     * 
     * @param triggers the new set of trigger names, may be <code>null</code> or
     * empty, <code>null</code> value is the same as empty list.
     */
    public void setTransitionTriggers(List<String> triggers)
    {
        if (triggers != null)
        {
            transitionTriggers = triggers;
        }
        else
        {
            transitionTriggers.clear();
        }
    }
    
    /**
     * The ID of the item.
     */
    private String itemId;
    
    /**
     * The ID of the current state of the item.
     */
    private String stateId;
    
    /**
     * The name of the current state of the item.
     */
    private String stateName;
    
    /**
     * The ID of the workflow of the state.
     */
    private String workflowId;
    
    
    /**
     * A list of all possible transition trigger names for the specified
     * workflow state.
     */
    private List<String> transitionTriggers = new ArrayList<>();
}
