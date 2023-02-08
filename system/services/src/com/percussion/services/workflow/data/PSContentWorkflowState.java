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
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Objects;

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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentWorkflowState)) return false;
      PSContentWorkflowState that = (PSContentWorkflowState) o;
      return Objects.equals(m_contentId, that.m_contentId) && Objects.equals(m_workflowAppId, that.m_workflowAppId) && Objects.equals(m_stateId, that.m_stateId);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_contentId, m_workflowAppId, m_stateId);
   }
}
