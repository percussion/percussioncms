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
        if (workflowName == null) {
            return StringUtils.EMPTY;
        }
        
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
            return new ArrayList<>();
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
