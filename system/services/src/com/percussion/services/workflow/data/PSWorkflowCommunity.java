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

