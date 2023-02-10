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

package com.percussion.rest.pages;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WorkflowInfo")
@Schema(name="WorkflowInfo",description="Represents information on the workflow.")
public class WorkflowInfo
{
    @Schema(name="name", description="Name of the workflow.")
    private String name;
    @Schema(name="state", description="State within the workflow.")
    private String state;
    @Schema(name="checkedOut", description="Flag if the item is checked out.")
    private Boolean checkedOut;
    @Schema(name="checkedOutUser", description="User that has the item checked out.")
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
