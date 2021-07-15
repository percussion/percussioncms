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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.workflow.IPSTransitionsContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * Represent a workflow non-aging transition
 */
public class PSTransition extends PSTransitionBase implements IPSTransition
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1L;

   private int approvals = 1;
   
   private String requiresComment = 
      PSWorkflowCommentEnum.OPTIONAL.getTypeValue();
   
   private String defaultTransition = "n";
   
   private String transitionRoles = 
      IPSTransitionsContext.NO_TRANSITION_ROLE_RESTRICTION;
   
   private List<PSTransitionRole> roles = new ArrayList<>();
   

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
   public PSWorkflowCommentEnum getRequiresComment()
   {
      if (StringUtils.isBlank(requiresComment))
         return PSWorkflowCommentEnum.OPTIONAL;
      else
         return PSWorkflowCommentEnum.typeValueOf(requiresComment);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#setRequiresComment(com.percussion.services.workflow.data.PSTransition.PSWorkflowCommentEnum)
    */
   public void setRequiresComment(PSWorkflowCommentEnum requirement)
   {
      if (requirement == null)
         throw new IllegalArgumentException("requirement may not be null");
      
      requiresComment = requirement.getTypeValue();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#isDefaultTransition()
    */
   public boolean isDefaultTransition()
   {
      return defaultTransition == null ? false : defaultTransition.equalsIgnoreCase("Y");
   }
   
   /**
    * Set if this is the default transition for the from state.
    * 
    * @param isDefault <code>true</code> if it is the default, 
    * <code>false</code> if not.
    */
   public void setDefaultTransition(boolean isDefault)
   {
      defaultTransition = (isDefault ? "y" : "n");
   }

   /**
    * Add a transition role to the transition role list.
    * <p>
    * Note, this method is required to support the underlying implementation of 
    * {@link #toXML()} and {@link #fromXML(String)} methods for the list of 
    * {@link PSTransitionRole} objects.
    * 
    * @param transitionRole the to be added transition role, not <code>null</code>.
    */   
   public void addTransitionRole(PSTransitionRole transitionRole)
   {
      notNull(transitionRole, "transitionRole may not be null");
      
      roles.add(transitionRole);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.data.IPSTransition#getTransitionRoles()
    */
   public List<PSTransitionRole> getTransitionRoles()
   {
      return roles;
   }
   
   /**
    *  Set toles roles allowed to use this transition. Is only used if 
    * {@link #isAllowAllRoles()} returns <code>false</code>.
    * 
    * @param roleList The list of roles, may be <code>null</code> or empty.
    */
   public void setTransitionRoles(List<PSTransitionRole> roleList)
   {
      if (roleList == null)
         roleList = new ArrayList<>();
      
      roles = roleList;
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
   
   /**
    * Class to enumerate the workflow comment requirements.
    */
   public enum PSWorkflowCommentEnum
   {
      /**
       * Indicates comment is optional
       */
      OPTIONAL("n"),
      
      /**
       * Indicates comment is required
       */
      REQUIRED("y"),
      
      /**
       * Indicates comment should not be shown
       */
      DO_NOT_SHOW("d");
      
      private PSWorkflowCommentEnum(String value)
      {
         mi_value = value;
      }
      
      /**
       * Get the string representation of this enum value to use in the 
       * repository.
       * 
       * @return The value
       */
      public String getTypeValue()
      {
         return mi_value;
      }
      
      /**
       * Get a type enum from it's type value (see {@link #getTypeValue()}
       * 
       * @param value The type value to use.
       * 
       * @return The enum, never <code>null</code>.
       */
      public static PSWorkflowCommentEnum typeValueOf(String value)
      {
         for (PSWorkflowCommentEnum type : values())
         {
            if (type.getTypeValue().equals(value))
               return type;
         }
         
         throw new IllegalArgumentException("invalid value");
      }
      
      private String mi_value;
   }
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("notification", PSNotification.class);
      PSXmlSerializationHelper.addType("transitionrole", 
         PSTransitionRole.class);
   }
}

