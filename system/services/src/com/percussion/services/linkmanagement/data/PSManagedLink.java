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
