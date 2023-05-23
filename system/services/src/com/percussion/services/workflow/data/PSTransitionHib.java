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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.IPSTransitionsContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represent a workflow transition for both aging and non-aging transitions.
 * <p>
 * Note, this is a persisted object, used to persist all type of transitions.
 * We have trouble to use 2 persisted classes to do the CRUD operation on 1 
 * table with hibernate. So the workaround is to use one persisted class to
 * do the underlying CRUD operation, but transform the objects in the 
 * public API. 
 */
@Entity
@Table(name = "TRANSITIONS")
@IdClass(PSTransitionPK.class)
public class PSTransitionHib implements IPSTransition, IPSAgingTransition
{
   /**
    * Transition type for normal and aging transitions.
    * 
    * @author YuBingChen
    */
   public enum TransitionType
   {
      /**
       * Represents a normal (non-aging) transition type.
       */
      TRANSITION(0),
      
      /**
       * Represents an aging transition type
       */
      AGING(1);
      
      private TransitionType(int typeValue)
      {
         value = typeValue;
      }
      
      /**
       * Get the integer value of the enum
       * 
       * @return The value.
       */
      public int getValue()
      {
         return value;
      }
      
      /**
       * Get the corresponding enum from a value
       * 
       * @param value The value of the enum to get.
       * 
       * @return The type, or <code>null</code> if no match is found.
       */
      public static TransitionType valueOf(int value)
      {
         for (TransitionType type : values())
         {
            if (type.value == value)
            {
               return type;
            }
         }
         
         throw new IllegalStateException("Unknown value (" + value + ") for the transition type.");
      }
      
      private int value;
   }

   private static final long serialVersionUID = 1L;

   @Id
   @Column(name = "TRANSITIONID", nullable = false)
   private long transitionId;
   
   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;

   @Basic
   @Column(name = "TRANSITIONFROMSTATEID", nullable = false)
   private long stateId;

   @Basic
   @Column(name="TRANSITIONLABEL", nullable = true)
   private String label;
   
   @Basic
   @Column(name="TRANSITIONDESC", nullable = true)
   private String description;
   
   @Basic
   @Column(name="TRANSITIONACTIONTRIGGER", nullable = true)   
   private String trigger;
   
   @Basic
   @Column(name="TRANSITIONTOSTATEID", nullable = true)   
   private long toState;
   
   @Basic
   @Column(name="TRANSITIONACTIONS", nullable = true)   
   private String transitionAction;

   @OneToMany(targetEntity = PSNotification.class, fetch = FetchType.LAZY, cascade =
   {CascadeType.ALL}, orphanRemoval = true)
   @JoinColumns({
      @JoinColumn(name = "WORKFLOWAPPID", referencedColumnName = "WORKFLOWAPPID", insertable = false, updatable = false),
      @JoinColumn(name = "TRANSITIONID", referencedColumnName = "TRANSITIONID", insertable = false, updatable = false)
   })
   private List<PSNotification> notifications = new ArrayList<>();
   
   @Basic
   @Column(name="TRANSITIONTYPE", nullable = false)   
   private int transitionType = TransitionType.TRANSITION.value; 
   
   // Non-aging specific properties 
   @Basic
   @Column(name="TRANSITIONAPPROVALSREQUIRED", nullable = true)   
   private int approvals = 1;
   
   @Basic
   @Column(name="TRANSITIONCOMMENTREQUIRED", nullable = true)   
   private String requiresComment = 
      PSTransition.PSWorkflowCommentEnum.OPTIONAL.getTypeValue();
   
   @Basic
   @Column(name="DEFAULTTRANSITION", nullable = true)   
   private String defaultTransition = "n";
   
   @Basic
   @Column(name="TRANSITIONROLES", nullable = true)   
   private String transitionRoles = 
      IPSTransitionsContext.NO_TRANSITION_ROLE_RESTRICTION;

   @OneToMany(targetEntity = PSTransitionRole.class, fetch = FetchType.LAZY, 
   cascade = CascadeType.MERGE, orphanRemoval = true )
   @JoinColumns({
      @JoinColumn(name = "WORKFLOWAPPID", referencedColumnName = "WORKFLOWAPPID", insertable = false, updatable = false),
      @JoinColumn(name = "TRANSITIONID", referencedColumnName = "TRANSITIONID", insertable = false, updatable = false)
   })
   private List<PSTransitionRole> roles = new ArrayList<>();

   // Aging specific properties 
   @Basic
   @Column(name="AGINGTYPE", nullable = true)
   private Integer agingType = PSAgingTransition.PSAgingTypeEnum.ABSOLUTE.getValue();
   
   @Basic
   @Column(name="AGINGINTERVAL", nullable = true)
   private Long interval = 1L;
   
   @Basic
   @Column(name="SYSTEMFIELD", nullable = true)
   private String systemField;
   
   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, transitionId);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (transitionId != 0)
         throw new IllegalStateException("cannot change existing guid");

      transitionId = newguid.longValue();
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#getWorkflowId()
    */
   public long getWorkflowId()
   {
      return workflowId;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setWorkflowId(long)
    */
   public void setWorkflowId(long id)
   {
      workflowId = id;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#getStateId()
    */
   public long getStateId()
   {
      return stateId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setStateId(long)
    */
   public void setStateId(long state)
   {
      stateId = state;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return getLabel();
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return label;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setLabel(java.lang.String)
    */
   public void setLabel(String lbl)
   {
      label = lbl;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      return description;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setDescription(java.lang.String)
    */
   public void setDescription(String desc)
   {
      description = desc;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#getTrigger()
    */
   public String getTrigger()
   {
      return trigger;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setTrigger(java.lang.String)
    */
   public void setTrigger(String triggerName)
   {
      this.trigger = triggerName;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#getToState()
    */
   public long getToState()
   {
      return toState;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setToState(long)
    */
   public void setToState(long state)
   {
      toState = state;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#getTransitionAction()
    */
   public String getTransitionAction()
   {
      return transitionAction;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setTransitionAction(java.lang.String)
    */
   public void setTransitionAction(String transAction)
   {
      transitionAction = transAction;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#getNotifications()
    */
   public List<PSNotification> getNotifications()
   {
      return notifications;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransitionBase#setNotifications(java.util.List)
    */
   public void setNotifications(List<PSNotification> notificationList)
   {
      if (notificationList == null)
         notificationList = new ArrayList<>();
      
      this.notifications.clear();
      this.notifications.addAll(notificationList);
   }

   public TransitionType getTransitionType()
   {
      return TransitionType.valueOf(transitionType);
   }
   
   public void setTransitionType(TransitionType type)
   {
      transitionType = type.value;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#isAllowAllRoles()
    */
   public boolean isAllowAllRoles()
   {
      return transitionRoles == null || transitionRoles.trim().equals(
         IPSTransitionsContext.NO_TRANSITION_ROLE_RESTRICTION);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#setAllowAllRoles(boolean)
    */
   public void setAllowAllRoles(boolean allowAll)
   {
      if (allowAll)
         transitionRoles = IPSTransitionsContext.NO_TRANSITION_ROLE_RESTRICTION;
      else
         transitionRoles = 
            IPSTransitionsContext.SPECIFIED_ROLE_TRANSITION_RESTRICTION;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#getApprovals()
    */
   public int getApprovals()
   {
      return approvals;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#setApprovals(int)
    */
   public void setApprovals(int number)
   {
      approvals = number;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#getRequiresComment()
    */
   public PSTransition.PSWorkflowCommentEnum getRequiresComment()
   {
      if (StringUtils.isBlank(requiresComment))
         return PSTransition.PSWorkflowCommentEnum.OPTIONAL;
      else
         return PSTransition.PSWorkflowCommentEnum.typeValueOf(requiresComment);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#setRequiresComment(com.percussion.services.workflow.data.PSTransition.PSWorkflowCommentEnum)
    */
   public void setRequiresComment(PSTransition.PSWorkflowCommentEnum requirement)
   {
      if (requirement == null)
         throw new IllegalArgumentException("requirement may not be null");
      
      requiresComment = requirement.getTypeValue();
   }
   
   /**
    * Is this the default transition for the from state?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isDefaultTransition()
   {
      return defaultTransition == null ? false : defaultTransition.equalsIgnoreCase("Y");
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#setDefaultTransition(boolean)
    */
   public void setDefaultTransition(boolean isDefault)
   {
      defaultTransition = (isDefault ? "y" : "n");
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#getTransitionRoles()
    */
   public List<PSTransitionRole> getTransitionRoles()
   {
      return roles;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#setTransitionRoles(java.util.List)
    */
   public void setTransitionRoles(List<PSTransitionRole> roleList)
   {
      if (roleList == null)
         roleList = new ArrayList<>();
      
      roles.clear();
      roles.addAll(roleList);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTransitionHib)) return false;
      PSTransitionHib that = (PSTransitionHib) o;
      return transitionId == that.transitionId && getWorkflowId() == that.getWorkflowId() && getStateId() == that.getStateId() && getToState() == that.getToState() && getTransitionType() == that.getTransitionType() && getApprovals() == that.getApprovals() && Objects.equals(getLabel(), that.getLabel()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getTrigger(), that.getTrigger()) && Objects.equals(getTransitionAction(), that.getTransitionAction()) && Objects.equals(getNotifications(), that.getNotifications()) && Objects.equals(getRequiresComment(), that.getRequiresComment()) && Objects.equals(isDefaultTransition(), that.isDefaultTransition()) && Objects.equals(getTransitionRoles(), that.getTransitionRoles()) && Objects.equals(roles, that.roles) && Objects.equals(agingType, that.agingType) && Objects.equals(getInterval(), that.getInterval()) && Objects.equals(getSystemField(), that.getSystemField());
   }

   @Override
   public int hashCode() {
      return Objects.hash(transitionId, getWorkflowId(), getStateId(), getLabel(), getDescription(), getTrigger(), getToState(), getTransitionAction(), getNotifications(), getTransitionType(), getApprovals(), getRequiresComment(), isDefaultTransition(), getTransitionRoles(), roles, agingType, getInterval(), getSystemField());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSTransitionHib{");
      sb.append("transitionId=").append(transitionId);
      sb.append(", workflowId=").append(workflowId);
      sb.append(", stateId=").append(stateId);
      sb.append(", label='").append(label).append('\'');
      sb.append(", description='").append(description).append('\'');
      sb.append(", trigger='").append(trigger).append('\'');
      sb.append(", toState=").append(toState);
      sb.append(", transitionAction='").append(transitionAction).append('\'');
      sb.append(", notifications=").append(notifications);
      sb.append(", transitionType=").append(transitionType);
      sb.append(", approvals=").append(approvals);
      sb.append(", requiresComment='").append(requiresComment).append('\'');
      sb.append(", defaultTransition='").append(defaultTransition).append('\'');
      sb.append(", transitionRoles='").append(transitionRoles).append('\'');
      sb.append(", roles=").append(roles);
      sb.append(", agingType=").append(agingType);
      sb.append(", interval=").append(interval);
      sb.append(", systemField='").append(systemField).append('\'');
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
   
   public PSAgingTransition.PSAgingTypeEnum getType()
   {
      if (agingType == null)
         agingType = PSAgingTransition.PSAgingTypeEnum.ABSOLUTE.getValue();
      
      return PSAgingTransition.PSAgingTypeEnum.valueOf(agingType);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSAgingTransition#setType(com.percussion.services.workflow.IPSAgingTransition.PSAgingTypeEnum)
    */
   public void setType(PSAgingTransition.PSAgingTypeEnum type)
   {
      if (agingType == null)
         throw new IllegalArgumentException("agingType may not be null");
      
      agingType = type.getValue();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSAgingTransition#getInterval()
    */
   public long getInterval()
   {
      return interval == null ? 1 : interval;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSAgingTransition#setInterval(long)
    */
   public void setInterval(long agingInterval)
   {
      interval = agingInterval;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSAgingTransition#getSystemField()
    */
   public String getSystemField()
   {
      return systemField;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSAgingTransition#setSystemField(java.lang.String)
    */
   public void setSystemField(String name)
   {
      systemField = name;
   }
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("notification", PSNotification.class);
      PSXmlSerializationHelper.addType("transitionrole", 
         PSTransitionRole.class);
   }
}

