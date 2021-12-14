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
