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

package com.percussion.rest.pages;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "WorkflowInfo")
@ApiModel(value="WorkflowInfo",description="Represents information on the workflow.")
public class WorkflowInfo
{
    @ApiModelProperty(value="name", notes="Name of the workflow.")
    private String name;
    @ApiModelProperty(value="state", notes="State within the workflow.")
    private String state;
    @ApiModelProperty(value="checkedOut", notes="Flag if the item is checked out.")
    private Boolean checkedOut;
    @ApiModelProperty(value="checkedOutUser", notes="User that has the item checked out.")
    private String checkedOutUser;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public Boolean getCheckedOut()
    {
        return checkedOut;
    }

    public void setCheckedOut(Boolean checkedOut)
    {
        this.checkedOut = checkedOut;
    }

    public String getCheckedOutUser()
    {
        return checkedOutUser;
    }

    public void setCheckedOutUser(String checkedOutUser)
    {
        this.checkedOutUser = checkedOutUser;
    }
}
