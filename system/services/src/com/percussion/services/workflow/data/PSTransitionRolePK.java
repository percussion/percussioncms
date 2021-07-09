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

import java.io.Serializable;

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

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this,obj);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
}

