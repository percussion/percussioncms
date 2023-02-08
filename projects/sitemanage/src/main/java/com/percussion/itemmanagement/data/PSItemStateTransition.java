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
