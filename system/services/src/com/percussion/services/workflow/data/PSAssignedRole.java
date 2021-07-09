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

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;

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
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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

