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

/**
 * An interface that defines methods for State Adhoc Users Context. 
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IPSContentAdhocUsersContext
{
   /**
    * Gets a list of adhoc normal user names
    *
    * @return list of adhoc normal user names, or
    *                  <CODE>null</CODE> if there are no adhoc normal users
    */
   public List getAdhocNormalUserNames();
      
   /**
    * Gets a list of adhoc normal role IDs for a specified user
    *
    * @param userName  name of the user for whom adhoc normal role IDs are
    *                  desired 
    * @return          list of adhoc normal role IDs for the user, or
    *                  <CODE>null</CODE> if the user has no adhoc normal roles
    * @throws IllegalArgumentException if the user name is <CODE>null</CODE> or
    *                                  empty after trimming. 
    */
   public List getUserAdhocNormalRoleIDs(String userName);
      
   /**
    * Gets the adhoc normal user count
    *
    * @return   the adhoc normal user count greater  or equal 0
    *
    */
   public int getContentAdhocNormalUserCount();

   /**
    * Gets a list of adhoc anonymous user names
    *
    * @return list of adhoc anonymous user names , or
    *         <CODE>null</CODE> if there are no adhoc anonymous users
    */
   public List getAdhocAnonymousUserNames();

   /**
    * Gets a list of adhoc anonymous user role IDs
    *
    * @return list of adhoc anonymous user role IDs, or
    *         <CODE>null</CODE> if there are no adhoc anonymous roles
    */
   public List getAdhocAnonymousRoleIDs();
      
   /**
    * Gets the adhoc anonymous user count
    *
    * @return   the adhoc anonymous user count 
    *
    */
   public int getContentAdhocAnonymousUserCount();

   
   /**
    * Adds a list of adhoc normal role IDs for a specific user to the context,
    * without committing the change. If a user already has adhoc normal roles,
    * (based on case-insensitive comparison of user names) any additional roles
    * will be added, and any existing roles ignored. The first capitalization
    * of any user name will be the one written to the data base.
    *
    * @param userName   name of the for whom adhoc normal roles will be added
    *                   may not be <CODE>null</CODE> or  the empty string
    * @param roleIDs    list of adhoc normal role IDs to be added may not be
    *                   <CODE>null</CODE> or empty
    * @throws IllegalArgumentException if the user name is <CODE>null</CODE>
    * or empty after trimming. 
    * 
    */
   public void addUserAdhocNormalRoleIDs(String userName, List roleIDs);
   
   /**
    * Sets the lists of adhoc anonymous role IDs and user names in the context,
    * without committing the change.
    *
    * @param userNames  names of users with anonymous adhoc roles, may not be
    *                   <CODE>null</CODE> or empty
    * @param roleIDs    list of adhoc anonymous role IDs  may not be
    *                   <CODE>null</CODE> or empty               
    * @throws IllegalArgumentException if the list of user names  or of role 
    * IDs is empty, or one, but not both of them is <CODE>null</CODE>.
    */
   public void setAdhocAnonymousUsersAndRoles(List userNames, List roleIDs);
   
   /**
    * Convenience method that calls 
    * {@link #emptyAdhocUserEntries(Connection, boolean) 
    * emptyAdhocUserEntries(connection, true)}
    */
   public int emptyAdhocUserEntries(Connection connection) throws SQLException;

   /**
    * Deletes data base entries for all adhoc assignees for this content item,
    * and optionally clears context variables containing this information.
    *
    * @param connection  open backend data base connection, may not be
    *                   <CODE>null</CODE>
    * @param clearState <code>true</code> to clear the in memory context 
    * variables, <code>false</code> to leave the in memory data.  If 
    * <code>false</code> is supplied, then neither this method nor 
    * {@link #commit(Connection)} may be called again, as this object will be
    * out of sync with the repository.  
    * 
    * @return            number of entries deleted             
    * @throws            SQLException if an SQL error occurs
    */
   public int emptyAdhocUserEntries(Connection connection, boolean clearState) 
      throws SQLException;

   /**
    * Commits all content adhoc assignee information stored in the context to
    * the data base.  Any previously existing data base entries for the content
    * item will be deleted.
    *
   * @param connection  open backend data base connection, may not be
    *                   <CODE>null</CODE>     
    * @return            number of entries written to the data base
    * @throws            SQLException if an SQL error occurs
    */   
   public int commit(Connection connection)
        throws SQLException;
   
   /**
    * Checks whether there are any adhoc users in the contexts
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return    <CODE>true</CODE> if the context has no adhoc users, else
    *            <CODE>false</CODE> 
    *
    */
   public boolean isEmpty();
 
}
