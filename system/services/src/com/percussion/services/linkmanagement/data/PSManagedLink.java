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
package com.percussion.services.linkmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author JaySeletz
 *
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
region = "PSManagedLink")
@Table(name = "PSX_MANAGEDLINK")
public class PSManagedLink extends PSAbstractDataObject
{
    
    @Id
    @Column(name = "LINKID")    
    private long linkId = -1L;
    
    @Basic
    @Column(name = "PARENTID")
    private int parentId;
    
    @Basic
    @Column(name = "PARENT_REVISION")
    private int parentRevision;

    @Basic
    @Column(name = "CHILDID")
    private int childId;
    
    @Basic
    @Column(name="ANCHOR")
    private String anchor;
    
    
    
    
    
    /**
    * @return the anchor
    */
   public String getAnchor()
   {
      return anchor;
   }

   /**
    * @param anchor the anchor to set
    */
   public void setAnchor(String anchor)
   {
      this.anchor = anchor;
   }

   /**
     * @return the id of the link's parent
     */
    public int getParentId()
    {
        return parentId;
    }

    /**
     * @return the id of the link's child
     */
    public int getChildId()
    {
        return childId;
    }

    /**
     * @return the link id
     */
    public long getLinkId()
    {
        return linkId;
    }

    /**
     * @param linkId the linkId to set
     */
    public void setLinkId(long linkId)
    {
        this.linkId = linkId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(int parentId)
    {
        this.parentId = parentId;
    }

    /**
     * @param childId the childId to set
     */
    public void setChildId(int childId)
    {
        this.childId = childId;
    }

    /**
     * @return the parentRevision
     */
    public int getParentRevision()
    {
        return parentRevision;
    }

    /**
     * @param parentRevision the parentRevision to set
     */
    public void setParentRevision(int parentRevision)
    {
        this.parentRevision = parentRevision;
    }

}
