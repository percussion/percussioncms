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
