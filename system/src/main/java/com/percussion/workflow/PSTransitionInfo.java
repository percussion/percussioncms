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

