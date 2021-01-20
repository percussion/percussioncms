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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Holds the state of a single content item. This is a transient object 
 * created by the workflow service to communicate the workflow and state a 
 * particular item belongs to.
 * 
 * @author dougrand
 */
public class PSContentWorkflowState
{
   /**
    * Id of the given content item, never <code>null</code> after construction
    */
   IPSGuid m_contentId;
   
   /**
    * Id of the given workflow, never <code>null</code> after construction
    */
   IPSGuid m_workflowAppId;
 
   /**
    * Id of the given state, never <code>null</code> after construction
    */
   IPSGuid m_stateId;

   /**
    * Ctor
    * @param contentId the content guid, never <code>null</code>
    * @param workflowId the workflow guid, never <code>null</code> and checked
    * for type
    * @param stateId the state guid, never <code>null</code> and checked for 
    * type
    */
   public PSContentWorkflowState(IPSGuid contentId, IPSGuid workflowId, 
         IPSGuid stateId)
   {
      if (contentId == null)
      {
         throw new IllegalArgumentException("contentId may not be null");
      }
      if (workflowId == null)
      {
         throw new IllegalArgumentException("workflowId may not be null");
      }
      if (stateId == null)
      {
         throw new IllegalArgumentException("stateId may not be null");
      }
      if (workflowId.getType() != PSTypeEnum.WORKFLOW.getOrdinal())
      {
         throw new IllegalArgumentException("workflow guid of wrong type");
      }
      if (stateId.getType() != PSTypeEnum.WORKFLOW_STATE.getOrdinal())
      {
         throw new IllegalArgumentException("state guid of wrong type");
      }
      m_contentId = contentId;
      m_workflowAppId = workflowId;
      m_stateId = stateId;
   }

   /**
    * @return the contentId
    */
   public IPSGuid getContentId()
   {
      return m_contentId;
   }

   /**
    * @return the stateId
    */
   public IPSGuid getStateId()
   {
      return m_stateId;
   }

   /**
    * @return the workflowAppId
    */
   public IPSGuid getWorkflowAppId()
   {
      return m_workflowAppId;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
   
   
}
