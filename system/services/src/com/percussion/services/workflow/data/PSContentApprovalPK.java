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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentApprovalPK)) return false;
      PSContentApprovalPK that = (PSContentApprovalPK) o;
      return getContentId() == that.getContentId() && getWorkflowId() == that.getWorkflowId() && Objects.equals(getUser(), that.getUser());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getContentId(), getWorkflowId(), getUser());
   }
}

