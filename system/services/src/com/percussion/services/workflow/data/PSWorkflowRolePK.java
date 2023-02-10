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
 * Primary key for workflow role lookup
 */
@Embeddable
public class PSWorkflowRolePK implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;
   
   @Column(name = "ROLEID", nullable = false)
   private long roleId;

   /**
    * Default Ctor
    */
   public PSWorkflowRolePK()
   {
      
   }
   
   /**
    * Ctor to create new primary key with data
    * @param wf the workflow id
    * @param roleid the role id
    */
   public PSWorkflowRolePK(long wf, long roleid)
   {
      workflowId = wf;
      roleId = roleid;
   }

   /**
    * @return Returns the role Id.
    */
   public long getRoleId()
   {
      return roleId;
   }

   /**
    * @param roleid The roleid to set.
    */
   public void setRoleId(long roleid)
   {
      roleId = roleid;
   }

   /**
    * @return Returns the workflowid.
    */
   public long getWorkflowId()
   {
      return workflowId;
   }

   /**
    * @param workflowid The workflowid to set.
    */
   public void setWorkflowId(long workflowid)
   {
      workflowId = workflowid;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSWorkflowRolePK)) return false;
      PSWorkflowRolePK that = (PSWorkflowRolePK) o;
      return getWorkflowId() == that.getWorkflowId() && getRoleId() == that.getRoleId();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getWorkflowId(), getRoleId());
   }
}

