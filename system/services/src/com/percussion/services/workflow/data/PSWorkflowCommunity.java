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
package com.percussion.services.workflow.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a workflow - community relationship.
 */
@Entity
@Table(name = "RXWORKFLOWCOMMUNITY")
public class PSWorkflowCommunity implements Serializable
{
   /**
    * The workflow id.
    */
   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private int workflowId;
   
   /**
    * The community id.
    */
   @Id
   @Column(name = "COMMUNITYID", nullable = false)
   private int communityId;
   
   /**
    * Get the workflow guid.
    * 
    * @return the workflow guid, never <code>null</code>.
    */
   public IPSGuid getWorkflowGuid()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW, workflowId);
   }
   
   /**
    * Set a new workflow guid.
    * 
    * @param id the new workflow guid, not <code>null</code>.
    */
   public void setWorkflowGuid(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      workflowId = id.getUUID();
   }
   
   /**
    * Get the community guid.
    * 
    * @return the community guid, never <code>null</code>.
    */
   public IPSGuid getCommunityGuid()
   {
      return new PSGuid(PSTypeEnum.COMMUNITY_DEF, communityId);
   }
   
   /**
    * Set a new community guid.
    * 
    * @param id the new community guid, not <code>null</code>.
    */
   public void setCommunityGuid(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      communityId = id.getUUID();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSWorkflowCommunity)) return false;
      PSWorkflowCommunity that = (PSWorkflowCommunity) o;
      return workflowId == that.workflowId &&
              communityId == that.communityId;
   }

   @Override
   public int hashCode() {
      return Objects.hash(workflowId, communityId);
   }
}

