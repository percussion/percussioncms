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
 * Primary key for transition notification lookup
 */
@Embeddable
public class PSNotificationPK implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;

   @Column(name = "TRANSITIONID", nullable = false)
   private long transitionId;   
   
   @Column(name = "NOTIFICATIONID", nullable = false)
   private long notificationId;

   @Column(name = "TRANSITIONNOTIFICATIONID", nullable = false)
   private long transNotificationId;
   
   /**
    * Default Ctor
    */
   public PSNotificationPK()
   {
      
   }
   
   /**
    * Ctor to create new primary key with data
    * @param wf the workflow id
    * @param trans the transition id
    * @param notifid the role id
    * @param transNotifId the transition notification Id
    */
   public PSNotificationPK(long wf, long trans, long notifid, long transNotifId)
   {
      workflowId = wf;
      transitionId = trans;
      notificationId = notifid;
      transNotificationId = transNotifId;
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

   public long getTransNotificationId()
   {
      return transNotificationId;
   }
   
   public void setTransNotificationId(long id)
   {
      transNotificationId = id;
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
   

   /**
    * @return Returns the transition id.
    */
   public long getTransitionId()
   {
      return transitionId;
   }

   /**
    * @param transid The transition id to set.
    */
   public void setTransitionId(long transid)
   {
      transitionId = transid;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSNotificationPK)) return false;
      PSNotificationPK that = (PSNotificationPK) o;
      return getWorkflowId() == that.getWorkflowId() && getTransitionId() == that.getTransitionId() && getNotificationId() == that.getNotificationId() && getTransNotificationId() == that.getTransNotificationId();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getWorkflowId(), getTransitionId(), getNotificationId(), getTransNotificationId());
   }
}

