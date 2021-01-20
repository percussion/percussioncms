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
package com.percussion.workflow;

import org.apache.commons.lang.StringUtils;

/**
 * Aggregates some basic info about a transition
 */
public class PSTransitionInfo
{
   /**
    * Create a transition info object.
    * 
    * @param id The transition id.
    * @param label The transition label, may not be <code>null</code> or empty.
    * @param trigger The transition trigger, may not be <code>null</code> or 
    * empty.
    * @param toStateId The id of the state to which the transition goes. 
    * @param comment The transition comment required value, 
    * not <code>null</code> or empty.
    * @param isDisabled <code>true</code> if the transition is disabled for the
    * current user, <code>false</code> otherwise.
    */
   public PSTransitionInfo(int id, String label, String trigger, int toStateId, 
      String comment, boolean isDisabled)
   {
      if (StringUtils.isBlank(label))
         throw new IllegalArgumentException("label may not be null or empty");
      if (StringUtils.isBlank(trigger))
         throw new IllegalArgumentException("trigger may not be null or empty");
      if (StringUtils.isBlank(comment))
         throw new IllegalArgumentException("comment may not be null or empty");
      
      m_id = id;
      m_label = label;
      m_trigger = trigger;
      m_toStateId = toStateId;
      m_comment = comment;
      m_isDisabled = isDisabled;
   }
   
   /**
    * Get the transition id supplied during construction.
    * 
    * @return The id.
    */
   public int getId()
   {
      return m_id;
   }
   
   /**
    * Get the label supplied during construction.
    * 
    * @return The label, not <code>null</code> or empty.
    */   
   public String getLabel()
   {
      return m_label;
   }
   
   /**
    * Get the trigger supplied during construction.
    * 
    * @return The trigger, not <code>null</code> or empty.
    */
   public String getTrigger()
   {
      return m_trigger;
   }
   
   /**
    * Get the isDisabled value supplied during construction.
    * 
    * @return The value.
    */
   public boolean isDisabled()
   {
      return m_isDisabled;
   }
   
   /**
    * Get the comment value supplied during construction.
    * 
    * @return The comment value, not <code>null</code> or empty.
    */
   public String getComment()
   {
      return m_comment;
   }
   
   /**
    * Get the toState id supplied during construction.
    * 
    * @return The id.
    */
   public int getToStateId()
   {

      return m_toStateId;
   }   
   
   /**
    * The transition id supplied during construction, immutable after that.
    */
   private int m_id;
   
   /**
    * The label supplied during construction, immutable after that.
    */
   private String m_label;
   
   /**
    * The trigger supplied during construction, immutable after that.
    */
   private String m_trigger;
   
   /**
    * The comment value supplied during construction, immutable after that.
    */
   private String m_comment;
   
   /**
    * The toStateId supplied during construction, immutable after that.
    */
   private int m_toStateId;
   
   /**
    * The isDisabled supplied during construction, immutable after that.
    */
   private boolean m_isDisabled;

}

