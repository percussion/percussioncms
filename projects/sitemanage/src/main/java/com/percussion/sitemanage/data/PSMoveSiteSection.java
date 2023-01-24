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
package com.percussion.sitemanage.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

/**
 * The class contains the info for moving a site section to different location.
 *
 * @author yubingchen
 */
@XmlRootElement(name="MoveSiteSection")
@JsonRootName("MoveSiteSection")
public class PSMoveSiteSection extends PSAbstractDataObject
{
    /**
     * Gets the ID of the target (parent) navigation node.
     *  
     * @return the target ID, not blank for a valid request.
     */
    public String getTargetId()
    {
        return targetId;
    }
    
    /**
     * Sets the ID of the target (parent) navigation node.
     * 
     * @param targetId the new target ID, not blank for a valid request.
     */
    public void setTargetId(String targetId)
    {
        this.targetId = targetId;
    }
    
    /**
     * The ID of the to be moved navigation node.
     * 
     * @return the source node ID, not blank for a valid request.
     */
    public String getSourceId()
    {
        return sourceId;
    }
    
    /**
     * Sets the ID of the to be moved navigation node.
     * 
     * @param srcId the source node ID, not blank for a valid request.
     */
    public void setSourceId(String srcId)
    {
        this.sourceId = srcId;
    }
    
    public String getSourceParentId()
    {
        return sourceParentId;
    }
    
    public void setSourceParentId(String parentId)
    {
        sourceParentId = parentId;
    }
    
    /**
     * Gets the target index, which is <code>0</code> based location under 
     * the target navigation node.
     * 
     * @return the target location. It may be <code>-1</code> if append to 
     * the child nodes of the target node.
     */
    public int getTargetIndex()
    {
        return targetIndex;
    }
    
    /**
     * Sets the target index, see {@link #getTargetIndex()} for detail.
     * 
     * @param index the new location under the target node.
     */
    public void setTargetIndex(int index)
    {
        this.targetIndex = index;
    }
    
    /**
     * The ID of the target (parent) navigation node, not blank for a valid request.
     */
    @NotBlank
    @NotNull
    private String targetId;

    /**
     * The ID of the source navigation node, not blank for a valid request.
     */
    @NotBlank
    @NotNull
    private String sourceId;

    /**
     * The parent ID of the source node. 
     */
    private String sourceParentId;
    
    /**
     * The to be moved location under the target navigation node. It is
     * <code>0</code> based. Append the source node if it is <code>-1</code>. 
     */
    @NotBlank
    @NotNull
    private int targetIndex;
}
