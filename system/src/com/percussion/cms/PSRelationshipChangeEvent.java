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