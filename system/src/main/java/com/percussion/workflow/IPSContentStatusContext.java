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

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;


/**
 * An interface that defines methods for accessing and setting fields in 
 * content status records, and committing changes.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */

interface IPSContentStatusContext
{
   /**
    * Sets the date this content item last underwent a transition to the
    * current time. 
    */   
   public void setLastTransitionDate();

   /**
    * Sets the value of the date this content item last underwent a transition.
    */     
   void setLastTransitionDate(Date lastTransitionDate);
   
   /**
    * Sets the checkout user name in the context. 
    *
    * @param checkout user name - can be <code>null</code> or 
    * <code>empty</code>.
    */
   void setContentCheckedOutUserName(String checkedUserName); 
   /**
    * Sets the value of content current revision, which is the largest numbered
    * revision that is not checked out. 
    *
    * @param currentRevision  the largest numbered revision that is not checked
    *                         out. 
    */
   public void setCurrentRevision(int currentRevision);
 
   /**
    * Sets the value of content edit revision, which is the revision of the
    * content item checked out for editing.
    *   
    * @param editRevision  if item is not checked out =content edit revision,
    *                      else = IPSConstants.NO_CORRESPONDING_REVISION_VALUE 
    */
   public void setEditRevision(int editRevision);

   /**
    * Sets the value of the content tip revision, which is the largest 
    * revision number for this content item.
    *
    * @param content tip revision or
    *                IPSConstants.NO_CORRESPONDING_REVISION_VALUE if no value
    *                 has yet been assigned.
    */
   public void setTipRevision(int tipRevision);
    
   /**
    * Turn on the revision lock, so that a new revision should be made for
    * each subsequent checkout.
    */
   public void lockRevision();
   
   /**
    * Sets the new stateid in the context.
    *
    * @param new stateid - must  be &gt; 0.
    */
   void setContentStateID(int stateID);

   /**
    * Updates the backend database with values currently set in the content
    * status.
    * 
    * @param JDBC connection to the database - must not be <code>null</code>.
    *
    * @throws SQLException if commit fails for any reason.
    */
   void commit(Connection connection) 
      throws SQLException;
   
   /**
    * Gets the value of reminder date
    *
    * @return the value of reminder date
    */
   public Date getReminderDate();

   /**
    * Gets the value of the state entered date 
    *
    * @return the date the content item entered the current state
    *
   public Date getStateEnteredDate();

   /**
    * Gets the date for the next aging transition for this content item.
    *
    * @return the value of the next aging date, or <CODE>null</CODE> if there
    *         is no pending aging transition.
    */
   public Date getNextAgingDate();

   /**
    * Gets the ID of the next aging transition.
    *
    * @return the ID of the next aging transition or 0 if there
    *         is no pending aging transition.
    */
   public int getNextAgingTransition();

   /**
    * Gets the value of the repeated aging transition start date.
    *
    * @return the date to increment to compute the next repeated aging
    *         transition 
    */
   public Date getRepeatedAgingTransitionStartDate();

   /**
    * Sets the date the content item entered the current state to the
    * current time.  
    */
   public void setStateEnteredDate();
   
   /**
    * Sets the date of the next aging transition to the current time stamp. 
    */
   public void setNextAgingDate(Date nextAgingDate);

   /**
    * Sets the transition ID of the next aging transition
    *
    * @param nextAgingTransition  nextAgingTransition
    */
   public void setNextAgingTransition(int nextAgingTransition);

   /**
    * Sets the date the date to increment to compute the next repeated aging
    * transition 
    */
   public void setRepeatedAgingTransitionStartDate(
      Date repeatedAgingTransitionStartDate);
   
   /**
    * Gets the date this content item last underwent a transition.
    *
    * @return the last transition date - <code>null</code> if item
    *         has never undergone a transition.
    */
   Date getLastTransitionDate();
   
   /**
    * Gets the workflow application id.
    *
    * @return the workflowid
    */
   int getWorkflowID();
    
   /**
    * Gets the content title.
    *
    * @return content title
    *           Initial and final whitespace will be trimmed.
    */
   public String getTitle();
    
   /**
    * Gets the content current revision, which is the largest numbered
    * revision that is not checked out.
    *
    * @return the largest numbered revision that is not checked out
    */
   public int getCurrentRevision();
    
   /**
    * Gets the content edit revision which is the revision of the
    * content item checked out for editing.
    * @return content edit revision, for an item that is not checked out, 
    *         return IPSConstants.NO_CORRESPONDING_REVISION_VALUE.    
    */
   public int getEditRevision();

   /**
    * Gets the content tip revision, which is the largest revision
    * number for this content item.
    *
    * @return tip revision, or IPSConstants.NO_CORRESPONDING_REVISION_VALUE
    *         if the corresponding data base field is <CODE>null</CODE>, or
    *         no meaningful value has yet been assigned.
    *          
    */
   public int getTipRevision();
   
   /**
    * Determine if content revision lock is set.
    * @return <CODE>true</CODE> if a new revision should be made for each
    *         checkout, else  <CODE>false</CODE>
    */
   public boolean isRevisionLocked();

   /**
    * Determine if content item has never had an aging computation
    * @return <CODE>true</CODE> tem has never had an aging computation,
    *         else  <CODE>false</CODE>
    */
   public boolean neverAged();
   
   /**
    * Gets the state id.
    *
    * @return the state id
    */
   int getContentStateID();

   /**
    * Gets the content id.
    *
    * @return the content id
    */
   int getContentID();
   
   /**
    * Gets the content type id.
    *
    * @return the content type id
    */
   int getContentTypeID();

   /**
    * Gets the name of the user that checked out this content item.
    *
    * @return   name of the user that checked out this content item 
    *           May not be more than 255 characters.
    *           Initial and final whitespace will be trimmed.
    *           <CODE>null</CODE> if item is not checked out. 
    */   
   String getContentCheckedOutUserName();

   /**
    * Gets the content last modifier name.
    *
    * @return the last modifier name
    */
   String getContentLastModifierName();

   /**
    * Gets the content last modified date.
    *
    * @return the last modified date
    */
   Date getContentLastModifiedDate();
   
   /**
    * Gets the content creator name.
    *
    * @return content creator name
    *         Initial and final whitespace will be trimmed.
    */
   String getContentCreatedBy();

   /**
    * Gets the content created date.
    *
    * @return the content created date
    */
   Date getContentCreatedDate();

   /**
    * Gets the content start date.
    *
    * @return the content start date
    */
   Date getContentStartDate();

   /**
    * Gets the expiry date.
    *
    * @return the expiry date
    */
   Date getContentExpiryDate();
   
   /**
    * Close the context freeing JDBC resources such as statements, and results
    * sets so that the  connection can be reused without any hassles.
    */
   void close();
}
