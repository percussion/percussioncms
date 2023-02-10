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
 * Primary key for workflow notification lookup
 */
@Embeddable
public class PSNotificationDefPK implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;
   
   @Column(name = "NOTIFICATIONID", nullable = false)
   private long notificationId;

   /**
    * Default Ctor
    */
   public PSNotificationDefPK()
   {
      
   }
   
   /**
    * Ctor to create new primary key with data
    * @param wf the workflow id
    * @param notifid the role id
    */
   public PSNotificationDefPK(long wf, long notifid)
   {
      workflowId = wf;
      notificationId = notifid;
   }

   /**
    * @return Returns the notification Id.
    */
   public long getNotificationId()
   {
      return notificationId;
   }

   /**
    * @param notifid The roleid to set.
    */
   public void setNotificationId(long notifid)
   {
      notificationId = notifid;
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
      if (!(o instanceof PSNotificationDefPK)) return false;
      PSNotificationDefPK that = (PSNotificationDefPK) o;
      return getWorkflowId() == that.getWorkflowId() && getNotificationId() == that.getNotificationId();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getWorkflowId(), getNotificationId());
   }
}

