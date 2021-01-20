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
package com.percussion.foldermanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

/**
 * Object that will be serialized for the workflow tab. Used to list the sites
 * and asset folders assigned to a given workflow.
 * 
 * @author Santiago M. Murchio
 * 
 */
@XmlRootElement(name = "folderItem")
@XmlType(propOrder={
        "name",
        "id",
        "workflowName",
        "allChildrenAssociatedWithWorkflow",
        "children"
    })
@XmlAccessorType(XmlAccessType.FIELD)
public class PSFolderItem extends PSAbstractDataObject
{
    /**
     * The name of the folder or site it represents.
     */
    private String name;

    /**
     * The id of the folder
     */
    private String id;

    /**
     * The name of the workflow this folder or site is associated with.
     */
    private String workflowName;

    /**
     * <code>true</code> if all of the subfolders are assignated to the same
     * workflow as this one. <code>false</code> otherwise.
     */
    private Boolean allChildrenAssociatedWithWorkflow;

    /**
     * The list of this folder's children. May be empty.
     */
    @XmlElement(name = "children")
    private PSFolders children = new PSFolders();

    /**
     * @param folderItem
     */
    public PSFolderItem()
    {
        
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the workflowName
     */
    public String getWorkflowName()
    {
        if (workflowName == null)
            return StringUtils.EMPTY;
        
        return workflowName;
    }

    /**
     * @param workflowName the workflowName to set
     */
    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    /**
     * @return the allChildrenAssociatedWithWorkflow
     */
    public Boolean getAllChildrenAssociatedWithWorkflow()
    {
        return allChildrenAssociatedWithWorkflow;
    }

    /**
     * @param allChildrenAssociatedWithWorkflow the
     *            allChildrenAssociatedWithWorkflow to set
     */
    public void setAllChildrenAssociatedWithWorkflow(Boolean allChildrenAssociatedWithWorkflow)
    {
        this.allChildrenAssociatedWithWorkflow = allChildrenAssociatedWithWorkflow;
    }

    /**
     * @return the children. May be empty, but never <code>null</code>
     */
    public List<PSFolderItem> getChildren()
    {
        if (children == null)
        {
            return new ArrayList<PSFolderItem>();
        }
        
        return children.getChildren();
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<PSFolderItem> children)
    {
        this.children.setChildren(children);
    }

}
