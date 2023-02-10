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
import java.sql.SQLException;
import java.util.List;

/**
 * An interface that defines methods for the content approvals context, which
 * provides methods for accessing, adding and removing users that have approved
 * content transitions .
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */

public interface IPSContentApprovalsContext
{
   /**
    * Adds a user approval.
    *                   
    * @param userName  name of the new approving user     
    * @param userRoleId the id of the user role.
    * @throws          SQLException if an SQL error occurs
    */
   
   public void addContentApproval(String userName, int userRoleId)
      throws SQLException;
   
   /**
    * Returns a list of users that have approved the content transition.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return a list of names of the users approved that have approved the
    * transition of this content item
    */
   public List getApprovedUserNames();
   
   /**
    * Checks whether a given user has approved the content transition..
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @param userName  name of user to check for approval
    *
    * @return <CODE>true</CODE> if has approved, else <CODE>false</CODE>
    */
   public boolean getIsUserListed(String userName);

   /**
    * Checks whether a given user has any pending transition approvals on this
    * content item from this state.
    *
    * @author   Aaron Brandes
    *
    * @version 1.0
    *
    * @param userName  name of user to check for pending transition approvals
    *
    * @return <CODE>true</CODE> if has a pending approval,
    *         else <CODE>false</CODE>
    */   
   public boolean hasUserActed(String userName);
   
   /**
    * Returns a count of users that have approved the content transition.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return number of users that have approved the content transition
    */
   public int getApprovedUserCount();
   
   /**
    * Checks whether the approved user list is empty.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return    <CODE>true</CODE> if the list is empty, else
    *            <CODE>false</CODE> 
    *
    */
   public boolean isEmpty();
   
   /**
    * Deletes all entries for approvals of this content item for transitions
    * with the same "from state" as the transition that is being approved. This
    * method must be invoked once the transition is done.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return   number of entries deleted
    *
    */
   public int emptyApprovals() throws SQLException;
}
