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

import java.sql.Date;
import java.sql.SQLException;

/**
 * An interface that defines methods for creating new content status history
 * records and accessing fields in existing ones.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
public interface IPSContentStatusHistoryContext
{   
   /**
    * Gets the content status history ID for this history entry.
    *
    * @return   the content status history ID for this history entry.
    */   
   public int getContentStatusHistoryID();
   
   /**
    * Gets the ContentID for the content item  acted on.
    *
    * @return   the ContentID for the content item.
    */   
   public int getContentID();
   
   /**
    * Gets the revision for the content item acted on.
    *
    * @return   the revision for the content item. For checkout return
    *           the base revision, which is either 1, or the revision of the
    *           content item copied to create the revision checked out.
    *           Must be <CODE>> 0</CODE>. 
    */   
   public int getRevision();
   
   /**
    * Gets the title of the content item for this content status history entry.
    * 
    * @return  the title of the content item
    *          May not be more than 40 characters
    *          Initial and final whitespace will be trimmed.
    */   
    public String getTitle();
   
   /**
    * Gets the SessionID for this content status history entry.
    *
    * @return   the SessionID for this content status history entry.
    *           May not be more than 40 characters
    *           Initial and final whitespace will be trimmed.
    */   
   public String getSessionID();
   
   /**
    * Gets the name of the user that performed this transition or action
    * including check in and check out (actor name). 
    *
    * @return   the actor name for this content status history entry.
    *           Initial and final whitespace will be trimmed.
    */   
   public String getActorName();
   
   /**
    * Gets the TransitionID for this content status history entry.
    *
    * @return   the transition ID if the content item has undergone a
    *            transition, else 0.
    */   
   public int getTransitionID();
   
    /**
     * Gets indicator as to whether this content is publishable
     *
     * @return   <CODE>true</CODE> if content is publishable
     *           else <CODE>false</CODE>
     */   
   public boolean getContentIsValid();
   
    /**
     * Gets the ID of the current state at completion of transition or action.
     *
     * @return   the ID of the current state at completion of transition or
     *            action. 
     */   
   public int getContentStateID();

   /**
    * Gets the name of the current state at completion of transition or
    * action.
    *
    * @return   the name of the current state at completion of transition or
    *           action.
    *           May not be more than 50 characters
    *           Initial and final whitespace will be trimmed.
    */   
   public String getContentStateName();

   /**
    * Gets the label of the transition or action.
    *
    * @return   for a transition, the transition label,
    *           "CheckIn" for a check in,
    *           "CheckOut for a check out    
    *           May not be more than 50 characters
    *           Initial and final whitespace will be trimmed.
    */   
   public String getTransitionLabel();
    
   /**
    * Gets the comma-separated list of assigned role names for the content
    * item's current state.
    *
    * @return   comma-separated list of assigned role names for the content
    *           item's current state
    *           May not be more than 255 characters.    
    *           Initial and final whitespace will be trimmed.
    */   
   public String getContentStateRoleName();
   
   /**
    * Gets the name of the user that checked out this content item.
    *
    * @return   name of the user that checked out this content item 
    *           May not be more than 255 characters.
    *           Initial and final whitespace will be trimmed.
    *           <CODE>null</CODE> if item is not checked out. 
    */   
   public String getContentCheckoutUserName();

   /**
    * Gets the name of the user that last modified this content item.
    *
    * @return   name of the user that last modified this content item
    *           May not be more than 255 characters.
    *           Initial and final whitespace will be trimmed.
    */
   public String getContentLastModifierName();
   
   /**
    * Gets the date/time this content item was last modified.
    *
    * @return   the date/time this content item was last modified.
    */   
    public Date getContentLastModifiedDate();
   
   /**
    * Gets the date/time when this transition or action took place.
    *
    * @return   date/time when this transition or action took place.
    */   
   public Date getEventTime();
   
   /**
    * Gets the descriptive comment for this transition.
    *
    * @return  the descriptive comment for this transition. 
    *          May not be more than 255 characters.
    *          Initial and final whitespace will be trimmed.
    */
   public String getTransitionComment();
   
   /**
    * Closes the transition context freeing all JDBC resources.
    * @author   Ram
    *
    * @version 1.0
    *
    */   
   public void close();
   
   /**
    * Indicates whether the set of history records for this content item is
    * empty.
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  <CODE>true</CODE> if no history records for this content item
    *          else <CODE>false</CODE> 
    */
   
   public boolean isEmpty();
   
   /**
    * Moves the database cursor to the next transition in the list, making data
    * available, trimming whitespace around string data.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  <CODE>true</CODE> if cursor movement is successful,
    *          else <CODE>false</CODE>.
    */
   public boolean moveNext() throws SQLException;
}
