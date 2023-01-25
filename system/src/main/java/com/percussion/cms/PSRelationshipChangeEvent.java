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
package com.percussion.cms;

import com.percussion.design.objectstore.PSRelationshipSet;

/**
 * Encapsulates the information about an action performed by the relationship
 * handler.
 */
public class PSRelationshipChangeEvent
{
   /**
    * Construct a new relationship event object.
    * 
    * @param action the action performed by the relationship handler, one of
    *    the <code>ACTION_xxx</code> constants.
    *    
    * @param relationships all relationships that were added, removed or 
    *    modified, never <code>null</code>.
    */
   public PSRelationshipChangeEvent(int action, PSRelationshipSet relationships)
   {
      if (action != ACTION_ADD &&
         action != ACTION_REMOVE &&
         action != ACTION_MODIFY)
         throw new IllegalArgumentException("invalid action type");
         
      if (relationships == null)
         throw new IllegalArgumentException("relationships cannot be null");
         
      m_action = action;
      m_relationships = relationships;
   }
   
   /**
    * Get the action the caused this event.
    * 
    * @return the action, one of the <code>ACTION_xxx</code> constants.
    */
   public int getAction()
   {
      return m_action;
   }
   
   /**
    * Get the relationships that caused this event.
    * 
    * @return the relationships, never <code>null</code>.
    */
   public PSRelationshipSet getRelationships()
   {
      return m_relationships;
   }
   
   /**
    * A constant indicating an undefined action.
    */
   public static final int ACTION_UNDEFINED = -1;
   
   /**
    * A constant indicating add relationship events.
    */
   public static final int ACTION_ADD = 0;
   
   /**
    * A constant indicating remove relationship events.
    */
   public static final int ACTION_REMOVE = 1;
   
   /**
    * A constant indicating modify relationship events.
    */
   public static final int ACTION_MODIFY = 2;
   
   /**
    * The action which produced this event. Initialized in ctor, never changed
    * after that.
    */
   private int m_action = ACTION_UNDEFINED;
   
   /**
    * Holds the relationships for this event. Initialized in ctor, never 
    * <code>null</code> or changed after that.
    */
   private PSRelationshipSet m_relationships = null;
}
