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

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * Represents an assigned state role.
 */
@Entity
@Table(name = "STATEROLES")
@IdClass(PSAssignedRolePK.class)
public class PSAssignedRole implements Serializable, IPSCatalogItem
{
   private static final long serialVersionUID = 1L;

   @Id
   @Column(name = "ROLEID", nullable = false)
   private long roleId;
   
   @Id
   @Column(name = "STATEID", nullable = false)
   private long stateId;
   
   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)   
   private long workflowId;
   
   @Basic
   @Column(name="ASSIGNMENTTYPE", nullable = false)
   private int assignmentType = PSAssignmentTypeEnum.NONE.getValue();
   
   @Basic
   @Column(name="ADHOCTYPE", nullable = false)
   private int adhocType = PSAdhocTypeEnum.DISABLED.getValue();
   
   @Basic
   @Column(name="ISNOTIFYON", nullable = false)
   private char doNotify = 'y';
   
   @Basic
   @Column(name="SHOWININBOX", nullable = false)
   private char  showInInbox = 'y';

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW_ROLE, roleId);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (roleId != 0)
         throw new IllegalStateException("cannot change existing guid");

      roleId = newguid.longValue();
   }
   
   /**
    * Get the id of the state to which this role is assigned.
    * 
    * @return The id.
    */
   public long getStateId()
   {
      return stateId;
   }

   /**
    * Set the state id to which this role is assigned.
    * 
    * @param stId The state id.
    */
   public void setStateId(long stId)
   {
      stateId = stId;
   }

   /**
    * Get the id of the workflow in which this role is assigned.
    * 
    * @return The workflow id.
    */
   public long getWorkflowId()
   {
      return workflowId;
   }

   /**
    * Set the id of the workflow in which this role is assigned.
    * 
    * @param wfId The workflow id.
    */
   public void setWorkflowId(long wfId)
   {
      workflowId = wfId;
   }   

   /**
    * Get the assignment type for this assigned role.
    * 
    * @return the role assignment type, never <code>null</code>.
    */
   public PSAssignmentTypeEnum getAssignmentType()
   {
      return PSAssignmentTypeEnum.valueOf(assignmentType);
   }
   
   /**
    * Set the assignment type for this role.
    * 
    * @param type The assignment type, may not be <code>null</code>.
    */
   public void setAssignmentType(PSAssignmentTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("assignmentType may not be null");
      
      assignmentType = type.getValue();
   }
   
   /**
    * The the adhoc assignment type for this role.
    * 
    * @return the adhoc assignment type, never <code>null</code>.
    */
   public PSAdhocTypeEnum getAdhocType()
   {
      return PSAdhocTypeEnum.valueOf(adhocType);
   }
   
   /**
    * Set the adhoc assignment type for this role.
    * 
    * @param type The adhoc type, may not be <code>null</code>.
    */
   public void setAdhocType(PSAdhocTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      adhocType = type.getValue();
   }
   
   /**
    * Should this assigned role being notified?
    * 
    * @return <code>true</code> if it should, <code>false</code> otherwise.
    */
   public boolean isDoNotify()
   {
      return doNotify == 'y' || doNotify == 'Y';
   }

   /**
    * Set if this assigned role should be notified.
    * 
    * @param notify <code>true</code> to be notified, <code>false</code> if not.
    */
   public void setDoNotify(boolean notify)
   {
      doNotify = notify ? 'y' : 'n';
   }
   
   /**
    * Should items in this state being shown to the assigned roles inbox?
    * 
    * @return <code>true</code> if it should, <code>false</code> otherwise.
    */
   public boolean isShowInInbox()
   {
      return showInInbox == 'y' || showInInbox == 'Y';
   }
   
   /**
    * Set if items in this state should be shown to the assigned roles inbox.
    * @param show <code>true</code> to show them, <code>false</code> if not.
    */
   public void setShowInInbox(boolean show)
   {
      showInInbox = show ? 'y' : 'n';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAssignedRole)) return false;
      PSAssignedRole that = (PSAssignedRole) o;
      return roleId == that.roleId && getStateId() == that.getStateId() && getWorkflowId() == that.getWorkflowId() && getAssignmentType() == that.getAssignmentType() && getAdhocType() == that.getAdhocType() && isDoNotify() == that.isDoNotify() && isShowInInbox() == that.isShowInInbox();
   }

   @Override
   public int hashCode() {
      return Objects.hash(roleId, getStateId(), getWorkflowId(), getAssignmentType(), getAdhocType(), isDoNotify(), isShowInInbox());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSAssignedRole{");
      sb.append("roleId=").append(roleId);
      sb.append(", stateId=").append(stateId);
      sb.append(", workflowId=").append(workflowId);
      sb.append(", assignmentType=").append(assignmentType);
      sb.append(", adhocType=").append(adhocType);
      sb.append(", doNotify=").append(doNotify);
      sb.append(", showInInbox=").append(showInInbox);
      sb.append('}');
      return sb.toString();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

