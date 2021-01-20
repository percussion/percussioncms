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
