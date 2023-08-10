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
import java.sql.SQLException;
import java.util.List;

/**
 * An interface that defines methods for State Adhoc Users Context.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
public interface IPSContentAdhocUsersContext
{
   /**
    * Gets a list of adhoc normal usernames
    *
    * @return list of adhoc normal usernames, or
    *                  <CODE>null</CODE> if there are no adhoc normal users
    */
   List<String> getAdhocNormalUserNames();
      
   /**
    * Gets a list of adhoc normal role IDs for a specified user
    *
    * @param userName  name of the user for whom adhoc normal role IDs are
    *                  desired 
    * @return          list of adhoc normal role IDs for the user, or
    *                  <CODE>null</CODE> if the user has no adhoc normal roles
    * @throws IllegalArgumentException if the username is <CODE>null</CODE> or
    *                                  empty after trimming. 
    */
   List<Integer> getUserAdhocNormalRoleIDs(String userName);
      
   /**
    * Gets the adhoc normal user count
    *
    * @return   the adhoc normal user count greater  or equal 0
    *
    */
   int getContentAdhocNormalUserCount();

   /**
    * Gets a list of adhoc anonymous usernames
    *
    * @return list of adhoc anonymous usernames , or
    *         <CODE>null</CODE> if there are no adhoc anonymous users
    */
    List<String> getAdhocAnonymousUserNames();

   /**
    * Gets a list of adhoc anonymous user role IDs
    *
    * @return list of adhoc anonymous user role IDs, or
    *         <CODE>null</CODE> if there are no adhoc anonymous roles
    */
    List<Integer> getAdhocAnonymousRoleIDs();
      
   /**
    * Gets the adhoc anonymous user count
    *
    * @return   the adhoc anonymous user count 
    *
    */
    int getContentAdhocAnonymousUserCount();

   
   /**
    * Adds a list of adhoc normal role IDs for a specific user to the context,
    * without committing the change. If a user already has adhoc normal roles,
    * (based on case-insensitive comparison of usernames) any additional roles
    * will be added, and any existing roles ignored. The first capitalization
    * of any username will be the one written to the database.
    *
    * @param userName   name of the for whom adhoc normal roles will be added
    *                   may not be <CODE>null</CODE> or  the empty string
    * @param roleIDs    list of adhoc normal role IDs to be added may not be
    *                   <CODE>null</CODE> or empty
    * @throws IllegalArgumentException if the username is <CODE>null</CODE>
    * or empty after trimming. 
    * 
    */
   void addUserAdhocNormalRoleIDs(String userName, List<Integer> roleIDs);
   
   /**
    * Sets the lists of adhoc anonymous role IDs and usernames in the context,
    * without committing the change.
    *
    * @param userNames  names of users with anonymous adhoc roles, may not be
    *                   <CODE>null</CODE> or empty
    * @param roleIDs    list of adhoc anonymous role IDs  may not be
    *                   <CODE>null</CODE> or empty               
    * @throws IllegalArgumentException if the list of usernames  or of role
    * IDs is empty, or one, but not both of them is <CODE>null</CODE>.
    */
   void setAdhocAnonymousUsersAndRoles(List<String> userNames, List<Integer> roleIDs);
   
   /**
    * Convenience method that calls 
    * {@link #emptyAdhocUserEntries(Connection, boolean) 
    * emptyAdhocUserEntries(connection, true)}
    */
   int emptyAdhocUserEntries(Connection connection) throws SQLException;

   /**
    * Deletes database entries for all adhoc assignees for this content item,
    * and optionally clears context variables containing this information.
    *
    * @param connection  open backend database connection, may not be
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
   int emptyAdhocUserEntries(Connection connection, boolean clearState)
      throws SQLException;

   /**
    * Commits all content adhoc assignee information stored in the context to
    * the database.  Any previously existing database entries for the content
    * item will be deleted.
    *
   * @param connection  open backend database connection, may not be
    *                   <CODE>null</CODE>     
    * @return            number of entries written to the database
    * @throws            SQLException if an SQL error occurs
    */   
   int commit(Connection connection)
        throws SQLException;
   
   /**
    * Checks whether there are any adhoc users in the contexts
    *
    * @author   Ram
    *
    *
    * @return    <CODE>true</CODE> if the context has no adhoc users, else
    *            <CODE>false</CODE> 
    *
    */
   boolean isEmpty();
 
}
