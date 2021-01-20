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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The PSStateRolesContext class is a wrapper class providing
 * access to the records and fields of the backend tables
 * 'STATEROLES' and the state name from the 'ROLES' table.
 */

public class PSStateRolesContext implements IPSStateRolesContext
{
   /**
    * Constructor specifying connection, state ID, workflow ID and minimum
    * assignment type.
    *
    * @param workFlowID      ID of the workflow
    * @param connection      data base connection
    * @param stateID         ID of the state
    * @param assignmentType  minimum assignment type
    * @throws                SQLException if an SQL error occurs
    * @throws                PSRoleException if an error occurs
    * @throws                PSEntryNotFoundException if no records
    *                        were returned corresponding to this set of data.
    */
   public PSStateRolesContext(int workFlowID,
                              Connection connection,
                              int stateID,
                              int assignmentType)
      throws SQLException, PSRoleException, PSEntryNotFoundException
   {
      String lowerCaseRoleName = "";
      List workingList = null;
      Integer roleID = null;
      this.workflowID = workFlowID;
      this.connection = connection;
      this.stateID = stateID;
      try
      {
         statement =
            PSPreparedStatement.getPreparedStatement(connection, QRYSTRING);
         statement.clearParameters();
         statement.setInt(1, workflowID);
         statement.setInt(2, stateID);
         statement.setInt(3, assignmentType);
         rs = statement.executeQuery();
         while(moveNext())
         {
            if (m_sStateRoleName.length() == 0)
            {
               throw new PSRoleException(
                IPSExtensionErrors.STATEROLE_NULL_EMPTY_TRIM);
            }
            m_nCount++;
            roleID = new Integer(m_nStateRoleID);
            m_StateRoleIDs.add(roleID);
            lowerCaseRoleName = m_sStateRoleName.toLowerCase();

            m_isNotificationOnMap.put(roleID, m_bNotifyOn);
            m_stateRoleNameMap.put(roleID, m_sStateRoleName);
            m_stateRoleAssignmentTypeMap.put(roleID,
                                             new Integer(m_nAssignmentType));

            m_lowerCaseRoleNameToIDMap.put(lowerCaseRoleName, roleID);

            if (m_nAdhocType == PSWorkFlowUtils.ADHOC_DISABLED)
            {
               m_nonAdhocStateRoleIDs.add(roleID);
               m_nonAdhocStateRoleNameToRoleIDMap.put(lowerCaseRoleName,
                                                      roleID);
            }
            else if (m_nAdhocType == PSWorkFlowUtils.ADHOC_ENABLED)
            {
               m_adhocNormalStateRoleNameToRoleIDMap.put(lowerCaseRoleName,
                                                         roleID);
               m_adhocNormalStateRoleIDs.add(roleID);
            }
            else if (m_nAdhocType == PSWorkFlowUtils.ADHOC_ANONYMOUS)
            {
               m_adhocAnonymousStateRoleIDs.add(roleID);
            }
            else
            {
               throw new PSRoleException(IPSExtensionErrors.INVALID_ADHOC);
            }
         }
         if (0 == m_nCount)
         {
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
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
      boolean bSuccess = rs.next();
      if(false == bSuccess)
         return bSuccess;

      m_nStateRoleID = rs.getInt("ROLEID");
      m_nAdhocType = rs.getInt("ADHOCTYPE");
      m_nAssignmentType = rs.getInt("ASSIGNMENTTYPE");
      String tmp = rs.getString("ISNOTIFYON");
      m_bNotifyOn = false;
      if(null != tmp && tmp.equalsIgnoreCase("Y"))
         m_bNotifyOn = true;

      m_sStateRoleName = PSWorkFlowUtils.trimmedOrEmptyString(
         rs.getString("ROLENAME"));

      return bSuccess;
   }


   /**
    * Closes the result set and statement if necessary
    */
   private void close()
   {
      //release resouces
      try
      {
         if(null != connection && false == connection.getAutoCommit())
            connection.setAutoCommit(true);
      }
      catch(SQLException e)
      {
      }
      try
      {
         if(null != rs)
         {
            rs.close();
         }

         rs = null;

         if(null != statement)
         {
            statement.close();
         }

         statement = null;
      }
      catch(SQLException e)
      {
      }
   }

   /* *** Implementation of IPSStateRolesContext Interface *** */

   public int getStateRoleCount()
   {
      return m_nCount;
   }

   public List getStateRoleIDs()
   {
      return  m_StateRoleIDs;
   }

   public List getStateRoleNames()
   {
      return  new ArrayList(m_stateRoleNameMap.values());
   }

   public List getNonAdhocStateRoleIDs()
   {
      return m_nonAdhocStateRoleIDs;
   }

   public List getAdhocNormalStateRoleIDs()
   {
      return m_adhocNormalStateRoleIDs;
   }

   public List getAdhocAnonymousStateRoleIDs()
   {
      return m_adhocAnonymousStateRoleIDs;
   }

   public Map getStateRoleAssignmentTypeMap()
   {
      return m_stateRoleAssignmentTypeMap;
   }

   public Map getStateRoleNameMap()
   {
      return m_stateRoleNameMap;
   }

   public Map getIsNotificationOnMap()
   {
      return m_isNotificationOnMap;
   }

   public Map getNonAdhocStateRoleNameToRoleIDMap()
   {
      return m_nonAdhocStateRoleNameToRoleIDMap;
   }

   public Map getAdhocNormalStateRoleNameToRoleIDMap()
   {
      return m_adhocNormalStateRoleNameToRoleIDMap;
   }

   public Map getLowerCaseRoleNameToIDMap()
   {
      return m_lowerCaseRoleNameToIDMap;
   }

   public boolean isEmpty()
   {
      return (0 == m_nCount);
   }


   /* *** End Implementation of IPSStateRolesContext Interface *** */

   /**
    * Produces a string representation of the state roles context.
    *
    * @return a string representation of the state roles context
    */
   public String toString()
   {
       StringBuffer buf = new StringBuffer();
       buf.append("\nPSContentAdhocUsersContext:");
       if (m_StateRoleIDs.isEmpty())
       {
          buf.append("\nNo state roles");
       }
       else
       {
          buf.append("\nState roles  = ");
          buf.append(m_StateRoleIDs.toString());
       }

       if (m_nonAdhocStateRoleIDs.isEmpty())
       {
          buf.append("\nNo non adhoc state roles");
       }
       else
       {
          buf.append("\nNon adhoc state role IDs  = ");
          buf.append(m_nonAdhocStateRoleIDs.toString());
       }

       if (m_adhocNormalStateRoleIDs.isEmpty())
       {
          buf.append("\nNo adhoc normal states");
       }
       else
       {
          buf.append("\nAdhoc normal state role IDs  = ");
          buf.append(m_adhocNormalStateRoleIDs.toString());
       }

       if (m_adhocAnonymousStateRoleIDs.isEmpty())
       {
          buf.append("\nNo adhoc anonymous state roles");
       }
       else
       {
          buf.append("\nAdhoc anonymous state role IDs  = ");
          buf.append(m_adhocAnonymousStateRoleIDs.toString());
       }

       if (!m_stateRoleAssignmentTypeMap.isEmpty())
       {
          buf.append("\nMap role ID  -> assignment type = ");
          buf.append(m_stateRoleAssignmentTypeMap.toString());
       }

       if (!m_stateRoleNameMap.isEmpty())
       {
          buf.append("\nMap role ID  -> role name = ");
          buf.append(m_stateRoleNameMap.toString());
       }

       if (!m_isNotificationOnMap.isEmpty())
       {
          buf.append("\nMap role ID  -> notification on/off = ");
          buf.append(m_isNotificationOnMap.toString());
       }

       if (!m_nonAdhocStateRoleNameToRoleIDMap.isEmpty())
       {
          buf.append("\nMap lower case role name -> nonadhoc role ID  = ");
          buf.append(m_nonAdhocStateRoleNameToRoleIDMap.toString());
       }

       if (!m_adhocNormalStateRoleNameToRoleIDMap.isEmpty())
       {
          buf.append("\nMap lower case role name -> adhoc normal role ID = ");
          buf.append(m_adhocNormalStateRoleNameToRoleIDMap.toString());
       }

       if (!m_lowerCaseRoleNameToIDMap.isEmpty())
       {
          buf.append("\nMap all lower case role name -> role ID = ");
          buf.append(m_lowerCaseRoleNameToIDMap.toString());
       }

       return buf.toString();
   }

   /** ID of the workflow for this state. */
   private int workflowID = 0;

   /** State ID for this context. */
   private int stateID = 0;

   /** JDBC version of SQL statement to be executed. */
   private PreparedStatement statement = null;

   /** Connection to the database */
   private Connection connection = null;

   /** Result of database query */
   private ResultSet rs = null;

   /** Number of database records found */
   int m_nCount = 0;

   /**
    * Assignment type at the current cursor position - updated every time
    * moveNext() is called, valid values are
    * <ul>
    * <li><CODE>PSWorkFlowUtils.ASSIGNMENT_TYPE_NONE</CODE></li>
    * <li><CODE>PSWorkFlowUtils.ASSIGNMENT_TYPE_READER</CODE></li>
    * <li><CODE>PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE</CODE></li>
    * <li><CODE>PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN</CODE></li>
    * </ul>
    */
    int m_nAssignmentType = 0;

   /**
    * Adhoc type at the current cursor position - updated every time
    * moveNext() is called, valid values are
    * <ul>
    * <CODE>PSWorkFlowUtils.ADHOC_ENABLED</CODE>,
    * <CODE>PSWorkFlowUtils.ADHOC_ANONYMOUS</CODE> and
    * <CODE>PSWorkFlowUtils.ADHOC_DISABLED</CODE>
    */
   int m_nAdhocType = 0;

   /**
    * RoleID at the current cursor position - updated every time
    * moveNext().  is called.
    */
   int m_nStateRoleID = 0;

   /**
    * Name of role at the current cursor position - updated every time
    * moveNext().  is called.
    */
   String m_sStateRoleName = "";

   /**
    * <CODE>true</CODE> if notification is on, for the role at the current
    * cursor position else <CODE>false</CODE>. Updated every time
    * moveNext()  is called.
    */
   boolean m_bNotifyOn = false;

   /**
    * List of all state role IDs
    */
   private List m_StateRoleIDs = new ArrayList();

   /**
    * List of all state role Names
    */
   private List m_StateRoleNames = new ArrayList();

   /**
    * List of non adhoc state role IDs
    */
   private List m_nonAdhocStateRoleIDs = new ArrayList();

   /**
    * List of adhoc normal state role IDs
    */
   private List m_adhocNormalStateRoleIDs = new ArrayList();

   /**
    * List of adhoc anonymous state role IDs
    */
   private List m_adhocAnonymousStateRoleIDs = new ArrayList();

   /**
    * Map with role ID as key and value = assignment type, which can take
    * values defined in  {@link PSWorkFlowUtils}
    * <ul>
    * <li><CODE>ASSIGNMENT_TYPE_NOT_IN_WORKFLOW</CODE></li>
    * <li><CODE>ASSIGNMENT_TYPE_NONE</CODE></li>
    * <li><CODE>ASSIGNMENT_TYPE_READER</CODE></li>
    * <li><CODE>ASSIGNMENT_TYPE_ASSIGNEE</CODE></li>
    * <li><CODE>ASSIGNMENT_TYPE_ADMIN</CODE></li>
    * </ul>
    */
   private Map m_stateRoleAssignmentTypeMap = new HashMap();

   /**
    * Map with role ID as key and value = role name
    */
   private Map m_stateRoleNameMap = new HashMap();

   /**
    * Map with role ID as key and value <CODE>true</CODE> if notification
    * for the role is on, else <CODE>false</CODE>
    */
   private Map m_isNotificationOnMap = new HashMap();

   /**
    * Map of role IDs for nonadhoc roles with trimmed lower case role
    * names as key
    */
   private Map m_nonAdhocStateRoleNameToRoleIDMap = new HashMap();

   /**
    * Map of role IDs for adhoc normal roles with trimmed lower case role
    * names as key
    */
   private Map m_adhocNormalStateRoleNameToRoleIDMap = new HashMap();

   /**
    * Map of role IDs for all state roles with trimmed lower case role
    * names as key
    */
   private Map m_lowerCaseRoleNameToIDMap = new HashMap();

   /* String for data base read query */
   static private String SR = 
      PSConnectionMgr.getQualifiedIdentifier("STATEROLES");
   static private String R = 
      PSConnectionMgr.getQualifiedIdentifier("ROLES");
   static private String QRYSTRING = 
      "SELECT " +
      SR + ".ROLEID," +
      SR + ".ADHOCTYPE," +
      SR + ".ASSIGNMENTTYPE," +
      SR + ".ISNOTIFYON," +
       R + ".ROLENAME FROM " +
      SR + "," + R + " WHERE (" +
      SR + ".WORKFLOWAPPID=? AND " +
      SR + ".STATEID=? AND (" +
      SR + ".ASSIGNMENTTYPE>=?) AND " +
      SR + ".ROLEID=" +
       R + ".ROLEID AND " +
      SR + ".WORKFLOWAPPID=" +
       R + ".WORKFLOWAPPID)";

}
