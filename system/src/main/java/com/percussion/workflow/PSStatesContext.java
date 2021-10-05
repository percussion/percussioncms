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
