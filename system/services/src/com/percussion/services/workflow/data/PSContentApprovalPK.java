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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key for {@link PSContentApproval}
 */
@Embeddable
public class PSContentApprovalPK implements Serializable
{
   /**
    * The serial id, used for serialization to detect version changes
    */
   private static final long serialVersionUID = 1L;

   /**
    * The content id of the item
    */
   @Column(name = "CONTENTID", nullable = false)
   private int contentId;
   
   /**
    * The role id for the adhoc assignment
    */
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private int workflowId;
   
   /**
    * The user name, never <code>null</code> or empty after ctor
    */
   @Column(name = "USERNAME", nullable = false)
   private String user;
   
   /**
    * Empty ctor
    */
   public PSContentApprovalPK()
   {
      // Empty
   }
   
   /**
    * Ctor
    * @param contentid the content item's id. 
    * @param workflowid the workflow id.
    * @param user the user name, never <code>null</code> or empty.
    */
   public PSContentApprovalPK(int contentid, int workflowid, String user)
   {
      if (user == null || StringUtils.isBlank(user))
      {
         throw new IllegalArgumentException("user may not be null or empty");
      }
      this.user = user;
      this.workflowId = workflowid;
      this.contentId = contentid;
   }

   /**
    * @return the contentId
    */
   public int getContentId()
   {
      return contentId;
   }

   /**
    * @param contentId the contentId to set
    */
   public void setContentId(int contentId)
   {
      this.contentId = contentId;
   }

   /**
    * Get the workflow id
    * @return the id
    */
   public int getWorkflowId()
   {
      return workflowId;
   }

   /**
    * Set the workflow id
    * @param workflowId the workflowid to set
    */
   public void setWorkflowId(int workflowId)
   {
      this.workflowId = workflowId;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param user the user to set
    */
   public void setUser(String user)
   {
      this.user = user;
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

