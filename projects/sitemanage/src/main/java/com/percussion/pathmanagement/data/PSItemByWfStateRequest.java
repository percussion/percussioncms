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
package com.percussion.pathmanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is posted to the rest service as part of a request to find the properties of a set of items by path,
 * workflow, and workflow state.
 * 
 * @author peterfrontiero
 */
@XmlRootElement(name = "ItemByWfStateRequest")
@JsonRootName("ItemByWfStateRequest")
public class PSItemByWfStateRequest
{
    /**
     * @return the path under which all items will be requested, never <code>null</code> or empty.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the parent path of the requested items, may not be <code>null</code> or empty.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return the workflow for which the items will be requested, never <code>null</code> or empty.
     */
    public String getWorkflow()
    {
        return workflow;
    }

    /**
     * @param workflow the workflow of the requested items, may not be <code>null</code> or empty.
     */
    public void setWorkflow(String workflow)
    {
        this.workflow = workflow;
    }

    /**
     * @return the workflow state for which the items will be requested.  May be <code>null</code> or empty to indicate
     * properties for items in all states should be returned.
     */
    public String getState()
    {
        return state;
    }

    /**
     * @param state the workflow state of the requested items.  May be <code>null</code> or empty to indicate
     * properties for items in all states should be returned.
     */
    public void setState(String state)
    {
        this.state = state;
    }

    /**
     * See {@link #getPath()}.
     */
    @NotNull
    @NotBlank
    private String path;
    
    /**
     * See {@link #getWorkflow()}.
     */
    @NotNull
    @NotBlank
    private String workflow;
    
    /**
     * See {@link #getState()}.
     */    
    private String state;
        
}
