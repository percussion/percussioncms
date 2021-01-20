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
import static com.percussion.services.workflow.data.PSTransformTransitionUtils.convertTransitions;
import static com.percussion.services.workflow.data.PSTransformTransitionUtils.copyAgingTransitions;
import static com.percussion.services.workflow.data.PSTransformTransitionUtils.copyTransitions;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.services.workflow.data.PSTransitionHib.TransitionType;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.xml.sax.SAXException;

/**
 * Represents a workflow state
 */
@Entity
@Table(name = "STATES")
@IdClass(PSStatePK.class)
public class PSState implements Serializable, IPSCatalogSummary, IPSCatalogItem
{
   private static final long serialVersionUID = 1L;

   @Id
   @Column(name = "STATEID", nullable = false)
   private long stateId = 0;

   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;

   @Basic
   @Column(name = "STATENAME")
   private String name;

   @Basic
   @Column(name = "STATEDESC")
   private String description;

   @Basic
   @Column(name = "SORTORDER")
   private Integer sortOrder = new Integer(-1);

   @Basic
   @Column(name = "CONTENTVALID")
   private String contentValidValue = "n";

   @OneToMany(targetEntity = PSTransitionHib.class, cascade =
   {CascadeType.ALL},orphanRemoval = true)
   @JoinColumns(
   {
         @JoinColumn(name = "WORKFLOWAPPID", referencedColumnName = "WORKFLOWAPPID", insertable = false, updatable = false),
         @JoinColumn(name = "TRANSITIONFROMSTATEID", referencedColumnName = "STATEID", insertable = false, updatable = false)})
   private List<PSTransitionHib> transitionHibs = new ArrayList<PSTransitionHib>();

   @OneToMany(targetEntity = PSAssignedRole.class, cascade =
   {CascadeType.ALL}, orphanRemoval = true)

   @JoinColumns(
   {
         @JoinColumn(name = "WORKFLOWAPPID", referencedColumnName = "WORKFLOWAPPID", insertable = false, updatable = false),
         @JoinColumn(name = "STATEID", referencedColumnName = "STATEID", insertable = false, updatable = false)})
   private List<PSAssignedRole> assignedRoles = new ArrayList<PSAssignedRole>();

   /**
    * The list of (non-aging) transitions. It is non-persisted property, used to cache the transformed (from PSTransitionHib) objects.
    */
   @Transient
   private List<PSTransition> transitionsCache;

   /**
    * The list of aging transitions. It is non-persisted property, used to cache the transformed (from PSTransitionHib) objects.
    */
   @Transient
   private List<PSAgingTransition> agingTransitionsCache;

   
   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW_STATE, stateId);
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

      if (stateId != 0)
         throw new IllegalStateException("cannot change existing guid");

      stateId = newguid.longValue();
   }

   /**
    * Set the state id
    * 
    * @param id The id
    */
   public void setStateId(long id)
   {
      stateId = id;
   }

   /**
    * Get the state id.
    * 
    * @return the id.
    */
   public long getStateId()
   {
      return stateId;
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
    * Set the name of the state
    * 
    * @param stateName The name, may not be <code>null</code> or empty.
    */
   public void setName(String stateName)
   {
      if (StringUtils.isBlank(stateName))
         throw new IllegalArgumentException("name may not be null or empty");

      name = stateName;
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
    * Set the description
    * 
    * @param desc the description, may be <code>null</code> or empty.
    */
   public void setDescription(String desc)
   {
      description = desc;
   }

   /**
    * Get the sort order for this state.
    * 
    * @return the sort order.
    */
   public Integer getSortOrder()
   {
      return sortOrder;
   }

   /**
    * Set the sort order of this state.
    * 
    * @param order The order.
    */
   public void setSortOrder(Integer order)
   {
      sortOrder = order;
   }

   /**
    * Is this a public state?
    * 
    * @return <code>true</code> is it is, <code>false</code> otherwise.
    */
   public boolean isPublishable()
   {
      return contentValidValue.trim().equalsIgnoreCase("y");
   }

   /**
    * Set if content in this state is publishable
    * 
    * @param isPublishable <code>true</code> to be publishable,
    *           <code>false</code> if not.
    */
   public void setPublishable(boolean isPublishable)
   {
      contentValidValue = (isPublishable ? "y" : "n");
   }

   /**
    * Get the string representation of the {@link #isPublishable()} setting.
    * 
    * @return the value.
    */
   public String getContentValidValue()
   {
      return contentValidValue;
   }
   
   /**
    * Set the content valid value
    * 
    * @param contentValidValue The content valid value
    */
   public void setContentValidValue(String  contentValid)
   {
      contentValidValue = contentValid;
   }

   /**
    * Get all transitions defined for this state.  You MUST call {@link #setTransitions(List)} if you make any 
    * modifications to the returned transitions that need to be persisted, otherwise the changes are ignored by 
    * hibernate.
    * 
    * @return all transitions, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public List<PSTransition> getTransitions()
   {
      if (transitionsCache != null)
         return transitionsCache;
       
      transitionsCache = (List<PSTransition>) convertTransitions(transitionHibs, TransitionType.TRANSITION);
      return transitionsCache;
   }

   /**
    * Scans all transitions that are part of this state, looking for one that
    * has a name that matches the supplied value. The name of a transition
    * is obtained by calling {@link PSTransitionBase#getTrigger()}.
    * 
    * @param tname If blank, <code>null</code> is returned. Value is case-
    * insensitive.
    * 
    * @return If blank, or a matching transition is not found, 
    * <code>null</code> is returned.
    */
   public PSTransition findTransitionByName(String tname)
   {
      if (StringUtils.isBlank(tname) || transitionHibs == null)
         return null;
      for (PSTransition tran : getTransitions())
      {
         if (tran.getTrigger().equalsIgnoreCase(tname))
            return tran;
      }
      return null;
   }
   
   /**
    * Set the list of transitions.
    * 
    * @param transitions The list, may be <code>null</code> or empty.
    */
   public void setTransitions(List<PSTransition> transitions)
   {
      if (transitions == null)
         transitions = new ArrayList<PSTransition>();

      copyTransitions(transitions, transitionHibs);
      
      // update the cache
      transitionsCache = null;
      getTransitions();
   }

   /**
    * Add a transition to the state's collection.
    * <p>
    * Note, this method is required to support the underlying implementation of 
    * {@link #toXML()} and {@link #fromXML(String)} methods for the list of 
    * {@link PSTransition} objects.
    * 
    * @param transition The transition to add, may not be <code>null</code>.
    */
   public void addTransition(PSTransition transition)
   {
      notNull(transition, "transition may not be null");

      List<PSTransition> transList = getTransitions();
      transList.add(transition);
      setTransitions(transList);
   }

   
   /**
    * Get all aging transitions defined for this state.
    * 
    * @return all aging transitions, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public List<PSAgingTransition> getAgingTransitions()
   {
      if (agingTransitionsCache != null)
         return agingTransitionsCache;
      
      agingTransitionsCache = (List<PSAgingTransition>) convertTransitions(transitionHibs, TransitionType.AGING);
      return agingTransitionsCache;
   }
   
   /**
    * Set the list of aging transitions.
    * 
    * @param transitions The list, may be <code>null</code> or empty.
    */
   public void setAgingTransitions(List<PSAgingTransition> transitions)
   {
      if (transitions == null)
         transitions = new ArrayList<PSAgingTransition>();

      copyAgingTransitions(transitions, transitionHibs);
      // update the cache
      agingTransitionsCache = null;
      getAgingTransitions();
   }

   /**
    * Add an aging transition to the aging transition list.
    * <p>
    * Note, this method is required to support the underlying implementation of 
    * {@link #toXML()} and {@link #fromXML(String)} methods for the list of 
    * {@link PSAgingTransition} objects.
    * 
    * @param transition The aging transition to add, may not be <code>null</code>.
    */
   public void addAgingTransition(PSAgingTransition transition)
   {
      notNull(transition, "transition may not be null");

      List<PSAgingTransition> transList = getAgingTransitions();
      transList.add(transition);
      setAgingTransitions(transList);
   }
   
   /**
    * Add an assigned-role to this state's collection.
    * <p>
    * Note, this method is required to support the underlying implementation of 
    * {@link #toXML()} and {@link #fromXML(String)} methods for the list of 
    * {@link PSAssignedRole} objects.
    * 
    * @param role The assigned role to add, may not be <code>null</code> and
    * the role (of the ID) must not exist in current role list.
    */
   public void addAssignedRole(PSAssignedRole role)
   {
      notNull(role, "role may not be null");
      
      // validate the added role does not exist
      for (PSAssignedRole r : assignedRoles)
      {
         if (r.getGUID().equals(role.getGUID()))
            throw new IllegalArgumentException("Role ID, \"" + role.getGUID() + "\", already exists in state \"" + getName() + "\".");
      }

      assignedRoles.add(role);
   }

   /**
    * Get all assigned roles for this state.
    * 
    * @return all assigend state roles, never <code>null</code>, may be
    *         empty.
    */
   public List<PSAssignedRole> getAssignedRoles()
   {
      return assignedRoles;
   }

   /**
    * Set the list of assigned roles.
    * 
    * @param roleList the list, may be <code>null</code> or empty.
    */
   public void setAssignedRoles(List<PSAssignedRole> roleList)
   {
      if (roleList == null)
         roleList = new ArrayList<PSAssignedRole>();

      assignedRoles = roleList;
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
    * Determines if any of the state roles have adhoc assignment enabled
    * 
    * @return <code>true</code> if adhoc assignment is enabled, 
    * <code>false</code> otherwise.
    */
   public boolean isAdhocEnabled()
   {
      for (PSAssignedRole role : assignedRoles)
      {
         if (role.getAssignmentType().getValue() >= 
            PSAssignmentTypeEnum.ASSIGNEE.getValue() && 
            !role.getAdhocType().equals(PSAdhocTypeEnum.DISABLED))
         {
            return true;
         }
      }
      
      return false;
   }   

   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("assignedrole", PSAssignedRole.class);
      PSXmlSerializationHelper.addType("transition", PSTransition.class);
      PSXmlSerializationHelper.addType("agingTransition", PSAgingTransition.class);
   }
}
