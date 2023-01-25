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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Primary key for the PSTransitionRole 
 */
@Embeddable
public class PSTransitionRolePK implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Column(name = "TRANSITIONROLEID", nullable = false)
   private long roleId;
   
   @Column(name = "TRANSITIONID", nullable = false)
   private long transitionId;
   
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;
   
   /**
    * Default ctor
    */
   public PSTransitionRolePK()
   {
      
   }

   /**
    * Ctor to create new primary key with data
    * 
    * @param wfid the workflow id
    * @param transid the transition id
    * @param roleid the role id
    */
   public PSTransitionRolePK(long wfid, long transid, long roleid)
   {
      workflowId = wfid;
      transitionId = transid;
      roleId = roleid;
   }

   /**
    * Get the id of the workflow role.
    * 
    * @return the id.
    */
   public long getRoleId()
   {
      return roleId;
   }

   /**
    * Set the id of the workflow role.
    * 
    * @param roleid the id.
    */
   public void setRoleId(long roleid)
   {
      roleId = roleid;
   }
   
   /**
    * Get the id of the transition for which this notification is specified.
    * 
    * @return the id.
    */
   public long getTransitionId()
   {
      return transitionId;
   }

   /**
    * Set the id of the transition for which this notification is specified.
    * 
    * @param transId the id.
    */
   public void setTransitionId(long transId)
   {
      this.transitionId = transId;
   }   
   
   /**
    * Get the workflow id of this state
    * 
    * @param id The id.
    */
   public void setWorkflowId(long id)
   {
      workflowId = id;
   }

   /**
    * Get the workflow id
    * 
    * @return The id.
    */
   public long getWorkflowId()
   {
      return workflowId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTransitionRolePK)) return false;
      PSTransitionRolePK that = (PSTransitionRolePK) o;
      return getRoleId() == that.getRoleId() && getTransitionId() == that.getTransitionId() && getWorkflowId() == that.getWorkflowId();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getRoleId(), getTransitionId(), getWorkflowId());
   }
}

