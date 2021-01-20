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

import com.percussion.extension.IPSExtensionErrors;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * PSContentAdhocUsersContext class is a wrapper class providing access to the
 * records and fields of the backend table 'CONTENTADHOCUSERS'.
 */
@SuppressWarnings("unchecked")
public class PSContentAdhocUsersContext implements IPSContentAdhocUsersContext
{

   /**
    * Constructor specifying content ID, used to create a context with no
    * content adhoc user information in its local variables.
    *
    * @param contentid   content ID of the item/document
    */
   public PSContentAdhocUsersContext(int contentID)
   {
      m_nContentID = contentID;
   }
   
   /**
    * Get the content id supplied during construction.
    * 
    * @return The content id.
    */
   public int getContentId()
   {
      return m_nContentID;
   }

   /**
    * Constructor specifying content ID and data base connection, retrieves
    * information from the data base.
    *
    * @param contentid   content ID of the current item/document
    *
    * @param connection  database connection - must not be <CODE>null</CODE>
    *
    * @throws SQLException if an SQL error occurs
    * @throws PSEntryNotFoundException when there are no entries in the table
    *         CONTENTADHOCUSERS for this content item
    * @throws IllegalArgumentException if the connection is <CODE>null</CODE>
    */
   public PSContentAdhocUsersContext(int contentID,
                                     Connection connection)
      throws  PSRoleException, SQLException
   {
      this(contentID);
      String lowerCaseUserName = "";
      List workingList = null;
      Integer roleID = null;
      if(null == connection)
      {
         throw new IllegalArgumentException("Connection cannot be null: " +
                                            TABLE_CAU);
      }
      this.m_connection = connection;
      try
      {
         m_Statement =
            PSPreparedStatement.getPreparedStatement(m_connection, QRYSTRING);
         m_Statement.clearParameters();
         m_Statement.setInt(1, contentID);

         // collect a list of user names and assignment types
         m_Rs = m_Statement.executeQuery();
         m_nCount = 0;
         while(moveNext())
         {
            if (m_sUserName.length() == 0)
            {
               throw new PSRoleException(
                IPSExtensionErrors.USERNAME_NULL_EMPTY_TRIM);
            }
            m_nCount++;

            lowerCaseUserName = m_sUserName.toLowerCase();
            roleID = new Integer(m_nRoleID);

            if (!m_adhocRoleIDtoAdhocTypeMap.
                containsKey(roleID))
            {
               m_adhocRoleIDtoAdhocTypeMap.put(roleID,
                                               new Integer(m_nAdhocType));
            }

            if (m_nAdhocType == PSWorkFlowUtils.ADHOC_ENABLED)
            {
               if (!m_userNameToAdhocNormalRoleIDMap.
                   containsKey(lowerCaseUserName))
               {
                  m_adhocNormalUserNames.add(m_sUserName);
                  workingList = new ArrayList();
               }
               else
               {
                  workingList = (List)
                     m_userNameToAdhocNormalRoleIDMap.get(lowerCaseUserName);
               }
               workingList.add(roleID);
               m_userNameToAdhocNormalRoleIDMap.put(lowerCaseUserName,
                                                    workingList);
            }
            else if (m_nAdhocType == PSWorkFlowUtils.ADHOC_ANONYMOUS)
            {
               if (!m_lowerCaseUserNameToUserNameMap.
                   containsKey(lowerCaseUserName))
               {
                  m_adhocAnonymousUserNames.add(m_sUserName);
                  m_lowerCaseUserNameToUserNameMap.put(lowerCaseUserName,
                                                       m_sUserName);
               }

               if (!m_adhocAnonymousRoleIDs.contains(roleID))
               {
                  m_adhocAnonymousRoleIDs.add(roleID);
               }
            }
            else
            {
               throw new PSRoleException(IPSExtensionErrors.INVALID_ADHOC);
            }
         }
      }

      finally
      {
         close();
      }
   }

   /**
    * Moves the cursor to next record in the resultset and updates the current
    * column values.
    *
    * @return <CODE>true</CODE> if another record is read else
    *  <CODE>false</CODE>
    *
    * @throws SQLException
    *
    */
   private boolean moveNext() throws SQLException
   {
      boolean bSuccess = m_Rs.next();
      if(false == bSuccess)
      {
         return bSuccess;
      }
      m_sUserName = PSWorkFlowUtils.trimmedOrEmptyString(
         m_Rs.getString(USERNAME));
      m_nAdhocType = m_Rs.getInt(ADHOCTYPE);
      m_nRoleID = m_Rs.getInt(ROLEID);

      return bSuccess;
   }

  /**
   * Closes the result set and statement if necessary
   */
   private void close()
   {
      //release resources
      try
      {
         if(null != m_Rs)
         {
            m_Rs.close();
            m_Rs = null;
         }

         if(null != m_Statement)
         {
            m_Statement.close();
            m_Statement = null;
         }
      }
      catch(SQLException e)
      {
         //  we ignore this exception, since we are trying to clean up
      }
   }

   /**
    * Deletes data base entries for all adhoc assignees for this content item,
    * optionally clearing context variables containing this information.
    *
    * @param clearStateVariables  <CODE>true</CODE>  if context variables
    *                             shouldbe cleared, else <CODE>false</CODE>.
    * @return                     number of entries deleted
    * @throws                     SQLException if an SQL error occurs
    */
   private int emptyAdhocUserEntries(boolean clearStateVariables)
         throws SQLException
   {
      int numEntriesDeleted = 0;
      boolean modifiedCommit = false;
      try
      {
         if(m_connection.getAutoCommit())
         {
            m_connection.setAutoCommit(false);
            modifiedCommit = true;
         }
         m_Statement = null;
         m_Statement =
            PSPreparedStatement.getPreparedStatement(
               m_connection,
               DELETESTRING);
         m_Statement.clearParameters();
         m_Statement.setInt(1, m_nContentID);

         numEntriesDeleted = m_Statement.executeUpdate();
         PSSqlHelper.commit(m_connection);

         if (clearStateVariables)
         {
            m_nCount = 0;
            if (null != m_userNameToAdhocNormalRoleIDMap)
            {
               m_userNameToAdhocNormalRoleIDMap.clear();
            }

            if (null != m_adhocNormalUserNames)
            {
               m_lowerCaseUserNameToUserNameMap.clear();
            }

            if (null != m_adhocAnonymousUserNames)
            {
               m_adhocNormalUserNames.clear();
            }

            if (null != m_adhocAnonymousUserNames)
            {
               m_adhocAnonymousUserNames.clear();
            }

            if (null != m_adhocAnonymousRoleIDs)
            {
               m_adhocAnonymousRoleIDs.clear();
            }
         }
          return numEntriesDeleted;
      }
      catch (NullPointerException  e)
      {
         e.printStackTrace();
         throw e;
      }

      finally
      {
         try
         {
            if(modifiedCommit)
               m_connection.setAutoCommit(true);
            close();
         }
         catch(Exception e)
         {
         }

      }
   }

/* **** Implementation of the IPSContentAdhocUsersContext interface**** */

   public int commit(Connection connection)
      throws SQLException
   {
      if (m_dataOutOfSync)
         throw new IllegalStateException(
            "Cannot call commit if data is out of sync");
            
      Iterator nameIter = null;
      Iterator roleIDIter = null;

      if (m_connection != null)
      {
         m_connection = connection;
         // Do not clear the state variables
         this.emptyAdhocUserEntries(false);
      }

      m_connection = connection;
      int nRows = 0;

      try
      {
         /*
          * Insert the records for the adhoc normal users.
          * Each user has a list of adhoc normal roles.
          */

         m_nAdhocType = PSWorkFlowUtils.ADHOC_ENABLED;
         nameIter = m_adhocNormalUserNames.iterator();
         while (nameIter.hasNext())
         {
            m_sUserName = (String) nameIter.next();
            String lowerCaseUserName =  m_sUserName.toLowerCase();
            List workingList = (List)
                     m_userNameToAdhocNormalRoleIDMap.get(lowerCaseUserName);
            if (null == workingList)
            {
               // shouldn't happen
               continue;
            }

            roleIDIter = workingList.iterator();
            // the repeated code below could go into a method
            while (roleIDIter.hasNext())
            {
               m_nRoleID = ((Integer) roleIDIter.next()).intValue();
               m_Statement = null;
               m_Statement =
                  PSPreparedStatement.getPreparedStatement(
                     m_connection,
                     INSERTSTRING);
               m_Statement.clearParameters();
               m_Statement.setInt(1, m_nContentID);
               m_Statement.setString(2, m_sUserName);
               m_Statement.setInt(3, m_nAdhocType);
               m_Statement.setInt(4, m_nRoleID);
               nRows = m_Statement.executeUpdate();

               if (nRows > 0 ) //Should always be true, no exception was thrown
               {
                  m_nCount += nRows;
               }
            }
         }

         /*
          * Insert the records for the adhoc anonymous users.
          * Each user acts in all the adhoc anonymous roles.
          */

         m_nAdhocType = PSWorkFlowUtils.ADHOC_ANONYMOUS;
         nameIter = m_adhocAnonymousUserNames.iterator();

         while (nameIter.hasNext())
         {
            m_sUserName = (String) nameIter.next();
            roleIDIter = m_adhocAnonymousRoleIDs.iterator();
            // this repeats code above, could go into a method
            while (roleIDIter.hasNext())
            {
               m_nRoleID = ((Integer) roleIDIter.next()).intValue();
               m_Statement = null;
               m_Statement =
                  PSPreparedStatement.getPreparedStatement(
                     m_connection,
                     INSERTSTRING);
               m_Statement.clearParameters();
               m_Statement.setInt(1, m_nContentID);
               m_Statement.setString(2, m_sUserName);
               m_Statement.setInt(3, m_nAdhocType);
               m_Statement.setInt(4, m_nRoleID);
               nRows = m_Statement.executeUpdate();

               if (nRows > 0 ) //Should always be true, no exception was thrown
               {
                  m_nCount += nRows;
               }
            }
         }
         PSSqlHelper.commit(m_connection);
         return m_nCount;
      }
      catch (NullPointerException  e)
      {
         e.printStackTrace();
         throw e;
      }
      finally
      {
         try
         {
            close();
         }
         catch(Exception e)
         {
         }
      }
   }

   public void addUserAdhocNormalRoleIDs(String userName, List roleIDs)
   {
      String lowerCaseUserName = "";
      List workingList = null;
      //validate
      if ((null == userName) || userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "The user name must not be null or empty in " +
            "addUserAdhocNormalRoleIDs");
      }
      userName = userName.trim();
      if (userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "The user name must not be " +
            "empty after trimming in addUserAdhocNormalRoleIDs");
      }
      lowerCaseUserName = userName.toLowerCase();
      if (null == roleIDs || roleIDs.isEmpty())
      {
         throw new IllegalArgumentException(
            "The user role ID list  must not be " +
            "null or empty in addUserAdhocNormalRoleIDs");
      }

      if (!m_userNameToAdhocNormalRoleIDMap.
          containsKey(lowerCaseUserName))
      {
         m_adhocNormalUserNames.add(userName);
         workingList = roleIDs;
      }
      else
      {
         workingList = (List)
               m_userNameToAdhocNormalRoleIDMap.get(lowerCaseUserName);
          workingList.addAll(roleIDs);
      }
      m_userNameToAdhocNormalRoleIDMap.put(lowerCaseUserName, workingList);
   }

   public void setAdhocAnonymousUsersAndRoles(List userNames, List roleIDs)
   {

      //validate
      if ((null != userNames) && userNames.isEmpty())
      {
         throw new IllegalArgumentException(
            "The user name list must not be empty " +
            "in addAdhocAnonymousUsers");
      }

      if (null != roleIDs && roleIDs.isEmpty())
      {
         throw new IllegalArgumentException(
            "The user role ID list  must not be " +
             "empty in addAdhocAnonymousUsers");
      }

      if (!((null == userNames) == (null == roleIDs)))
      {
         throw new IllegalArgumentException(
            "It is not allowed for only one of the lists of user names " +
            "and role IDs to be null.");
      }
      m_adhocAnonymousUserNames = userNames;
      m_adhocAnonymousRoleIDs = roleIDs;
   }

   public List getUserAdhocNormalRoleIDs(String userName)
   {
      if ((null == userName) || userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "The user name must not be null");
      }
      userName = userName.trim().toLowerCase();
      if (userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "The user name must not be empty after trimming");
      }

      return (List) m_userNameToAdhocNormalRoleIDMap.get(userName);
   }

   public List getAdhocNormalUserNames()
   {
      return m_adhocNormalUserNames;
   }

   public List getAdhocAnonymousUserNames()
   {
      return m_adhocAnonymousUserNames;
   }

   public List getAdhocAnonymousRoleIDs()
   {
      return m_adhocAnonymousRoleIDs;
   }

   public int getContentAdhocNormalUserCount()
   {
      return m_adhocNormalUserNames.size();
   }

   public int getContentAdhocAnonymousUserCount()
   {
      return m_adhocAnonymousUserNames.size();
   }

   public List getEmptyAdhocRoles(List roleIDList)
   {
      List emptyAdhocRoles = new ArrayList();
      Integer roleID = null;
      if (null == roleIDList || roleIDList.isEmpty())
      {
         return emptyAdhocRoles;
      }

      Iterator iter = roleIDList.iterator();

      while (iter.hasNext())
      {
         roleID = (Integer) iter.next();
         if (null != roleID && !hasAdhocUsers(roleID.intValue()))
         {
            emptyAdhocRoles.add(roleID);
         }
      }
      return emptyAdhocRoles;
   }

   public boolean hasAdhocUsers(int roleID)
   {
      return (m_adhocRoleIDtoAdhocTypeMap.containsKey(new Integer(roleID)));
   }

   public boolean isEmpty()
   {
      return (0 == m_nCount);
   }

   public int emptyAdhocUserEntries(Connection connection) throws SQLException
   {
      return emptyAdhocUserEntries(connection, true);
   }

   public int emptyAdhocUserEntries(Connection connection, boolean clearState) 
      throws SQLException
   {
      if (m_dataOutOfSync)
         throw new IllegalStateException(
            "Cannot call emptyAdhocUserEntries() if data is out of sync");

      m_connection = connection;
      int result = this.emptyAdhocUserEntries(clearState);
      m_dataOutOfSync = !clearState;
      
      return result; 
   }

   /* ** End Implementation of the IPSContentAdhocUsersContext interface ** */

   /**
    * Produces a string representation of the content adhoc users context.
    *
    * @return a string representation of the content adhoc users context
    */
   public String toString()
   {
       StringBuffer buf = new StringBuffer();
       buf.append("\nPSContentAdhocUsersContext:");
       if (!m_userNameToAdhocNormalRoleIDMap.isEmpty())
       {
          buf.append("\nMap Adhoc normal usernames -> role IDs = ");
          buf.append(m_userNameToAdhocNormalRoleIDMap.toString());
       }

       if (m_adhocNormalUserNames.isEmpty())
       {
          buf.append("\nNo adhoc normal assignees");
       }
       else
       {
          buf.append("\nUser names of adhoc normal assignees = ");
          buf.append(getAdhocNormalUserNames().toString());
       }

       if (m_adhocAnonymousUserNames.isEmpty())
       {
          buf.append("\nNo adhoc anonymous assignees");
       }
       else
       {
          buf.append("\nUser names of adhoc anonymous assignees = ");
          buf.append(getAdhocAnonymousUserNames().toString());
       }

       if (!m_adhocAnonymousRoleIDs.isEmpty())
       {
          buf.append("\nRole IDs for adhoc anonymous assignees = ");
          buf.append(getAdhocAnonymousRoleIDs().toString());
       }
       return buf.toString();
   }

   /**
    * Reusable prepared statement object.
    */
   private PreparedStatement m_Statement = null;

   /**
    * Local variable referencing the opened connection.
    */
   private Connection m_connection = null;

   /**
    * Reusable ResultSet object.
    */
   private ResultSet m_Rs = null;

   /**
    * Number of adhoc user records found.
    */
   private int m_nCount = 0;

   /**
    * Content ID for which adhoc users are valid
    *
    */
   private int m_nContentID = 0;
   /**
    * User name at the current cursor position - updated every time moveNext().
    * is called
    */
   private String m_sUserName = "";


   /**
    * Adhoc type at the current cursor position - updated every time
    * moveNext() is called, valid values in the data base are
    * <CODE>PSWorkFlowUtils.ADHOC_ENABLED</CODE> and
    * <CODE>PSWorkFlowUtils.ADHOC_ANONYMOUS</CODE>
    */
   private int m_nAdhocType = PSWorkFlowUtils.ADHOC_DISABLED;

   /**
    * RoleID at the current cursor position - updated every time
    * moveNext().  is called.
    */
   private int m_nRoleID = 0;

   /**
    * Map of role IDs for adhoc normal assignees with trimmed lowercase
    * username as key
    */
   private Map m_userNameToAdhocNormalRoleIDMap = new HashMap();

   /**
    * Map from adhoc role IDs to adhoc type
    */
   private Map m_adhocRoleIDtoAdhocTypeMap = new HashMap();

   /**
    * Map of usernames for adhoc anonymous assignees with trimmed lowercase
    * username as key
    */
   private Map m_lowerCaseUserNameToUserNameMap = new HashMap();

   /**
    * List of user names of adhoc normal assignees
    */
   private List m_adhocNormalUserNames = new ArrayList();

   /**
    * List of user names of adhoc anonymous assignees
    */
   private List m_adhocAnonymousUserNames = new ArrayList();

   /**
    * List of role IDs for adhoc anonymous assignees
    */
   private List m_adhocAnonymousRoleIDs = new ArrayList();
   
   /**
    * Flag to indicate if the in memory data is out of sync with the repository
    * after calling a method to save state to the database.  This may happen
    * if {@link #emptyAdhocUserEntries(Connection, boolean)} is called passing
    * <code>false</code> for the <code>clearState</code> param.  
    */
   private boolean m_dataOutOfSync = false;

   /**
    * static constant string that represents the column name in the table
    */
   private static final String USERNAME = "USERNAME";

   /**
    * static constant string that represents the column name in the table
    */
   private static final String ADHOCTYPE = "ADHOCTYPE";

   /**
    * static constant string that represents the column name in the table
    */
   private static final String ROLEID = "ROLEID";

   /**
    * static constant string that represents the unqualified table name.
    */
   private static final String CONTENTADHOCUSERS = "CONTENTADHOCUSERS";

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_CAU = 
      PSConnectionMgr.getQualifiedIdentifier(CONTENTADHOCUSERS);

   /**
    * SQL query string is constructed based on fully qualified table name(s).
    */
   static private String QRYSTRING = 
      "SELECT " +
      TABLE_CAU + ".USERNAME, " +
      TABLE_CAU + ".ADHOCTYPE, " +
      TABLE_CAU + ".ROLEID " +
      "FROM " + 
      TABLE_CAU + 
      " WHERE (" + 
      TABLE_CAU + ".CONTENTID=?)";

   /**
    * SQL string to insert a data base record for a new approval.
    */
   private static final String INSERTSTRING =
      "INSERT INTO " + 
      TABLE_CAU +
      "( " +
      TABLE_CAU + ".CONTENTID, " +
      TABLE_CAU + ".USERNAME, " +
      TABLE_CAU + ".ADHOCTYPE ," +
      TABLE_CAU + ".ROLEID) " +
      "VALUES(?,?,?,?)";

   /**
    * SQL string to delete all data base record for approvals for given content
    * item with a given initial state.
    */
   private static final String DELETESTRING =
      "DELETE FROM " + 
      TABLE_CAU + 
      " WHERE ( " + 
      TABLE_CAU + ".CONTENTID=?)";
}
