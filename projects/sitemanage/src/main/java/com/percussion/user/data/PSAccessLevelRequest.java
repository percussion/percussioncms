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
package com.percussion.user.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotEmpty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates a request for the access level, see {@link PSAccessLevel}, of the current user for a given content type
 * or workflow.
 */
@XmlRootElement(name = "AccessLevelRequest")
@JsonRootName("AccessLevelRequest")
public class PSAccessLevelRequest
{
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String type;

    private int workflowId;
    
    private String itemId;
    
    private String parentFolderPath;
    
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
    
    public int getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(int workflowId)
    {
        this.workflowId = workflowId;
    }
    
    public String getItemId()
    {
        return itemId;
    }

    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }

    public String getParentFolderPath()
    {
        return parentFolderPath;
    }

    public void setParentFolderPath(String parentFolderPath)
    {
        this.parentFolderPath = parentFolderPath;
    }
}
