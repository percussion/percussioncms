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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * Hibernate data object for approval information related to a content item, a
 * workflow role and a given user.  This represents an attempt to transition an
 * item that requires multiple approvals
 */
@Entity
@Table(name = "CONTENTAPPROVALS")
@IdClass(PSContentApprovalPK.class)
public class PSContentApproval 
{
   /**
    * The content id of the item
    */
   @Id
   @Column(name = "CONTENTID", nullable = false)
   private int contentId;

   /**
    * The role id used in the approval
    */
   @Basic
   @Column(name = "ROLEID", nullable = false)
   private int roleId;

   /**
    * The user name, never <code>null</code> or empty after ctor
    */
   @Id
   @Column(name = "USERNAME", nullable = false)
   private String user;

   /**
    * The workflowid of the transition acted upon
    */
   @Id
   @Column(name = "WORKFLOWAPPID")
   private int workflowId;

   /**
    * The from state id of the transition acted upon
    */
   @Basic
   @Column(name = "STATEID")
   private int stateId;

   
   /**
    * The the id of the transition acted upon
    */
   @Basic
   @Column(name = "TRANSITIONID")
   private int transitionId;
   
   /**
    * Empty ctor, should only be used by Hibernate and other framework 
    * implmementation.
    */
   public PSContentApproval()
   {

   }

   /**
    * Construct with parameters
    * 
    * @param contentid The contentid of the item being approved 
    * @param roleid The id of the role the user is acting in
    * @param userName The name of the use, not <code>null</code> or empty
    * @param workflowid The workflow id of the transition being executed
    * @param stateid The from-state id of the transition
    * @param transitionid The transition id
    */
   public PSContentApproval(int contentid, int roleid, String userName, 
      int workflowid, int stateid, int transitionid)
   {
      if (StringUtils.isBlank(userName))
         throw new IllegalArgumentException("userName may not be null or empty");
      
      contentId = contentid;
      roleId = roleid;
      user = userName;
      workflowId = workflowid;
      stateId = stateid;
      transitionId = transitionid;
   }
   
   /**
    * Get the id of the item for which the transition was executed.
    * 
    * @return The id.
    */
   public int getContentId()
   {
      return contentId;
   }

   /**
    * Get the id of the workflow role the user was acting in.
    * 
    * @return The id.
    */
   public int getRoleId()
   {
      return roleId;
   }

   /**
    * Get the id of the current workflow state of the item.  
    * 
    * @return the id
    */
   public int getStateId()
   {
      return stateId;
   }

   /**
    * Set the current workflow state id of the item.
    * 
    * @param stateid The id to set.
    */
   public void setStateId(int stateid)
   {
      stateId = stateid;
   }

   /**
    * Get the id of the transition that was performed.
    * 
    * @return The id
    */
   public int getTransitionId()
   {
      return transitionId;
   }

   /**
    * Set the transition id that was performed.
    * 
    * @param transitionid The id to set.
    */
   public void setTransitionId(int transitionid)
   {
      transitionId = transitionid;
   }

   /**
    * Get the name user that acted on the transition.
    * 
    * @return The user, <code>null</code> or empty only if the empty ctor was
    * used directly rather than loading via the service.
    */
   public String getUser()
   {
      return user;
   }

   /**
    * Set the name of the user that acted on the transition. 
    * 
    * @param userName The user to set, may not be <code>null</code> or empty.
    */
   public void setUser(String userName)
   {
      if (StringUtils.isBlank(userName))
         throw new IllegalArgumentException(
            "userName may not be null or empty");
      
      user = userName;
   }

   /**
    * Get the workflow id of the transition being executed
    * 
    * @return the workflowId.
    */
   public int getWorkflowId()
   {
      return workflowId;
   }

   /**
    * Set the workflow id of the transition being executed
    * @param workflowId The workflowId to set.
    */
   public void setWorkflowId(int workflowId)
   {
      this.workflowId = workflowId;
   }

   /**
    * Set the content id of the item being transitioned
    * 
    * @param contentId The contentId to set.
    */
   public void setContentId(int contentId)
   {
      this.contentId = contentId;
   }

   /**
    * Set the id of the role in which the user is acting
    * 
    * @param roleId The roleId to set.
    */
   public void setRoleId(int roleId)
   {
      this.roleId = roleId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentApproval)) return false;
      PSContentApproval that = (PSContentApproval) o;
      return getContentId() == that.getContentId() && getRoleId() == that.getRoleId() && getWorkflowId() == that.getWorkflowId() && getStateId() == that.getStateId() && getTransitionId() == that.getTransitionId() && Objects.equals(getUser(), that.getUser());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getContentId(), getRoleId(), getUser(), getWorkflowId(), getStateId(), getTransitionId());
   }
}
