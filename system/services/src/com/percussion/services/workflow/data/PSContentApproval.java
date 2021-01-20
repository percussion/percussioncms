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
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
   
   
}
