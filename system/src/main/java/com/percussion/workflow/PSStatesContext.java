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
package com.percussion.workflow;

import com.percussion.services.workflow.data.PSState;

/**
 * Represents a workflow state in the system. This object is not cached at this
 * time as it may be updated via the XML applications.
 */
public class PSStatesContext implements IPSStatesContext
{
   private PSStatesContextPK m_statePK = null;
   private String m_sStateName = null;
   private String m_nStateDesc = null;
   private String m_contentValidValue = null;
   
   /**
    * Default ctor
    */
   public PSStatesContext()
   {
      
   }

   /**
    * Construct a states context from a source state and a workflow ctx.
    * 
    * @param source The state, may not be <code>null</code>. 
    */
   public PSStatesContext(PSState source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      m_contentValidValue = source.getContentValidValue();
      m_nStateDesc = source.getDescription();
      m_sStateName = source.getName();
      m_statePK = new PSStatesContextPK((int)source.getWorkflowId(), 
         (int)source.getStateId());
   }

   /**
    * Get the primary key
    * @return the primary key, never <code>null</code> on a persisted
    * object
    */
   public PSStatesContextPK getStatePK()
   {
      return m_statePK;
   }
   
   /**
    * Set the primary key on a new object
    * @param pk the primary key, never <code>null</code>
    */
   public void setStatePK(PSStatesContextPK pk)
   {
      if (pk == null)
      {
         throw new IllegalArgumentException("pk may not be null");
      }
      m_statePK = pk;
   }
   
   public int getStateID()
   {
      if (m_statePK != null)
         return m_statePK.getStateid();
      else
         return 0;
   }

   public String getStateName()
   {
      return m_sStateName;
   }

   public String getStateDescription()
   {
      return m_nStateDesc;
   }

   /**
    * @return Returns the contentValidValue.
    */
   public String getContentValidValue()
   {
      return m_contentValidValue;
   }

   public boolean getIsValid()
   {
      return m_contentValidValue.trim().equalsIgnoreCase("Y");
   }
   
   public boolean getIsUnpublish()
   {
      return m_contentValidValue.trim().equalsIgnoreCase("U");
   }

   /**
    * @param stateDesc The nStateDesc to set.
    */
   public void setStateDescription(String stateDesc)
   {
      m_nStateDesc = stateDesc;
   }

   /**
    * @param stateName The sStateName to set.
    */
   public void setStateName(String stateName)
   {
      m_sStateName = stateName;
   }

   /**
    * @param contentValidValue The contentValidValue to set.
    */
   public void setContentValidValue(String contentValidValue)
   {
      m_contentValidValue = contentValidValue;
   }
}
