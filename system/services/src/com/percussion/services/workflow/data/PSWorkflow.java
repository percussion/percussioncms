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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.xml.sax.SAXException;

/**
 * Represents a workflow
 */
@Entity
@Table(name = "WORKFLOWAPPS")
public class PSWorkflow
      implements
         Serializable,
         IPSCatalogSummary,
         IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 3105407723614336921L;

   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long id;

   @Basic
   @Column(name = "WORKFLOWAPPNAME")
   private String name;

   @Basic
   @Column(name = "WORKFLOWAPPDESC")
   private String description;

   @Basic
   @Column(name = "ADMINISTRATOR")
   private String administratorRole;

   @Basic
   @Column(name = "INITIALSTATEID")
   private long initialStateId;
   
   /**
    * The object version.
    */
   private Integer version;

   @OneToMany(targetEntity = PSState.class, fetch = FetchType.LAZY, cascade =
   {CascadeType.ALL}, orphanRemoval = true)
   @JoinColumn(name = "WORKFLOWAPPID", insertable = false, updatable = false)
   private List<PSState> states = new ArrayList<PSState>();

   @OneToMany(targetEntity = PSWorkflowRole.class, fetch = FetchType.EAGER, cascade =
   {CascadeType.ALL}, orphanRemoval = true)
   @JoinColumn(name = "WORKFLOWAPPID", insertable = false, updatable = false)
   @Fetch(FetchMode. SUBSELECT)
   private List<PSWorkflowRole> roles = new ArrayList<PSWorkflowRole>();

   @OneToMany(targetEntity = PSNotificationDef.class, fetch = FetchType.LAZY, cascade =
   {CascadeType.ALL}, orphanRemoval = true)
   @JoinColumn(name = "WORKFLOWAPPID", insertable = false, updatable = false)
   private List<PSNotificationDef> notificationDefs = new ArrayList<PSNotificationDef>();

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW, id);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (id != 0)
         throw new IllegalStateException("cannot change existing guid");

      id = newguid.longValue();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the workflow name.
    * 
    * @param wfname The name, may not be <code>null</code> or empty.
    */
   public void setName(String wfname)
   {
      if (StringUtils.isBlank(wfname))
         throw new IllegalArgumentException("wfname may not be null or empty");

      name = wfname;
   }

   /**
    * Get the object version.
    * 
    * @return the object version, <code>null</code> if not initialized yet.
    */
   public Integer getVersion()
   {
      return version;
   }

   /**
    * Set the object version. The version can only be set once in the life cycle
    * of this object.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("version can only be initialized once");

      if (version != null && version.intValue() < 0)
         throw new IllegalArgumentException("version must be >= 0");

      this.version = version;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Set the description.
    * 
    * @param desc The description, may be <code>null</code> or empty.
    */
   public void setDescription(String desc)
   {
      description = desc;
   }

   /**
    * Get the role of the administrator for this workflow.
    * 
    * @return the administrator role, may be <code>null</code> or empty.
    */
   public String getAdministratorRole()
   {
      return administratorRole;
   }

   /**
    * Set the role of the administrator for this workflow.
    * 
    * @param roleName the administrator role, may be <code>null</code> or
    *           empty.
    */
   public void setAdministratorRole(String roleName)
   {
      administratorRole = roleName;
   }

   /**
    * The id of the initial state into which all items enter this workflow.
    * 
    * @return the initial state id.
    */
   public long getInitialStateId()
   {
      return initialStateId;
   }

   /**
    * Get the intial state object.
    * 
    * @return The state, or <code>null</code> if a valid initial state has not
    *         been specified.
    */
   @IPSXmlSerialization(suppress = true)
   public PSState getInitialState()
   {
      PSState state = null;

      for (PSState test : states)
      {
         if (test.getStateId() == initialStateId)
         {
            state = test;
            break;
         }
      }

      return state;
   }

   /**
    * Set the initial state
    * 
    * @param initStateId the id of the initial state.
    */
   public void setInitialStateId(long initStateId)
   {
      initialStateId = initStateId;
   }

   /**
    * Add a state.
    * 
    * @param state The state to add, may not be <code>null</code>.
    */
   public void addState(PSState state)
   {
      if (state == null)
         throw new IllegalArgumentException("state may not be null");

      states.add(state);
   }

   /**
    * Get all workflow states.
    * 
    * @return a list with all defined workflow states, never <code>null</code>,
    *         may be empty.
    */
   public List<PSState> getStates()
   {
      return states;
   }

   /**
    * Scan all states that are part of this workflow and return the one that
    * matches the supplied id.
    * 
    * @param stateId If <code>null</code>, <code>null</code> is returned.
    * 
    * @return The matching state, or <code>null</code> if no state matches.
    */
   public PSState findState(IPSGuid stateId)
   {
      if (null == stateId || states == null)
         return null;
      for (PSState state : states)
      {
         if (state.getGUID().equals(stateId))
            return state;
      }
      return null;
   }
   
   /**
    * Set the states.
    * 
    * @param stateList The states, may be <code>null</code> or empty.
    */
   public void setStates(List<PSState> stateList)
   {
      if (stateList == null)
         stateList = new ArrayList<PSState>();

      states = stateList;
   }

   /**
    * The the supplied role to the collection.
    * 
    * @param role The role to add, may not be <code>null</code> and the ID and name
    * of the role must not exist in current role list.
    */
   public void addRole(PSWorkflowRole role)
   {
      notNull(role);

      // validate the added role does not exist
      for (PSWorkflowRole r : roles)
      {
         if (r.getGUID().equals(role.getGUID()))
            throw new IllegalArgumentException("Role ID, \"" + role.getGUID() + "\", already exists in workflow \"" + getName() + "\".");

         if (r.getName().equalsIgnoreCase(role.getName()))
            throw new IllegalArgumentException("Role name, \"" + role.getName() + "\", already exists in workflow \"" + getName() + "\".");
      }

      roles.add(role);
   }

   /**
    * Get all workflow roles.
    * 
    * @return a list with all defined workflow roles, never <code>null</code>,
    *         may be empty.
    */
   public List<PSWorkflowRole> getRoles()
   {
      return roles;
   }

   /**
    * Set the roles.
    * 
    * @param wfroles The roles, may be <code>null</code> or empty.
    */
   public void setRoles(List<PSWorkflowRole> wfroles)
   {
      if (wfroles == null)
         wfroles = new ArrayList<PSWorkflowRole>();

      this.roles = wfroles;
   }

   /**
    * Add a notification to the collection.
    * 
    * @param notif The notification to add, may not be <code>null</code>.
    */
   public void addNotificationDef(PSNotificationDef notif)
   {
      if (notif == null)
         throw new IllegalArgumentException("notif may not be null");

      notificationDefs.add(notif);
   }

   /**
    * Get all workflow notification definitions.
    * 
    * @return a list with all defined workflow notificcations, never
    *         <code>null</code>, may be empty.
    */
   public List<PSNotificationDef> getNotificationDefs()
   {
      return notificationDefs;
   }

   /**
    * Set the notifications.
    * 
    * @param notificationList The list of notifications, may be
    *           <code>null</code> or empty.
    */
   public void setNotificationDefs(List<PSNotificationDef> notificationList)
   {
      if (notificationList == null)
         notificationList = new ArrayList<PSNotificationDef>();

      notificationDefs = notificationList;
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

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }


   /**
    * Get the roles from this workflow that match user roles
    * 
    * @param userRoles the roles, never <code>null</code>, may be empty.
    * 
    * @return a list of corresponding guids, never <code>null</code>
    */
   public Set<Integer> getRoleIds(Collection<String> userRoles)
   {
      if (userRoles == null)
         throw new IllegalArgumentException("userRoles may not be null");
      
      Set<Integer> rids = new HashSet<Integer>();

      for (PSWorkflowRole role : roles)
      {
         if (userRoles.contains(role.getName()))
         {
            rids.add(role.getGUID().getUUID());
         }
      }

      return rids;
   }
   
   /**
    * Get the role names for the specified wf role ids.
    * 
    * @param roleids The role ids to get the matching names for, may not be
    * <code>null</code>, may be empty.
    * 
    * @return A list of role names, never <code>null</code>, may be empty if
    * the supplied list is empty or no matches are found.
    */
   public Set<String> getRoleNames(Collection<Integer> roleids)
   {
      if (roleids == null)
         throw new IllegalArgumentException("roleids may not be null");
      
      Set<String> names = new HashSet<String>();
      
      for (PSWorkflowRole role : roles)
      {
         if (roleids.contains(role.getGUID().getUUID()))
         {
            names.add(role.getName());
         }
      }
      
      return names;
   }
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("state", PSState.class);
      PSXmlSerializationHelper.addType("role", PSWorkflowRole.class);
      PSXmlSerializationHelper.addType("notificationdef",
            PSNotificationDef.class);
   }
}
