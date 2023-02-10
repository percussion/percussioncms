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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * Represents a workflow role.
 */
@Entity
@Table(name = "ROLES")
@IdClass(PSWorkflowRolePK.class)
public class PSWorkflowRole implements Serializable, IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1L;
   
   @Id
   @Column(name = "ROLEID", nullable = false)
   private long roleId;
   
   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;
   
   @Basic
   @Column(name="ROLENAME", nullable = false)
   private String name;
   
   @Basic
   @Column(name="ROLEDESC", nullable = true)
   private String description;

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
   
   /**
    * Get the workflow role name.
    * 
    * @return the workflow role name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set the role name.
    * 
    * @param roleName The name of the role, may not be <code>null</code> or 
    * empty.
    */
   public void setName(String roleName)
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException("name may not be null or empty");
      
      name = roleName;
   }

   /**
    * Get the workflow role description.
    * 
    * @return a description for this workflow role, may be <code>null</code>
    *    or empty.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Set the description
    * 
    * @param desc The description, may be <code>null</code> or empty.
    */
   public void setDescription(String desc)
   {
      description = desc;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSWorkflowRole)) return false;
      PSWorkflowRole that = (PSWorkflowRole) o;
      return roleId == that.roleId && getWorkflowId() == that.getWorkflowId() && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription());
   }

   @Override
   public int hashCode() {
      return Objects.hash(roleId, getWorkflowId(), getName(), getDescription());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSWorkflowRole{");
      sb.append("roleId=").append(roleId);
      sb.append(", workflowId=").append(workflowId);
      sb.append(", name='").append(name).append('\'');
      sb.append(", description='").append(description).append('\'');
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

