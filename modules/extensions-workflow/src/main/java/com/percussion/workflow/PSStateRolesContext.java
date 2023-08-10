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
@Deprecated //TODO: This entire class needs refactored to use hibernate / spring
public class PSStateRolesContext implements IPSStateRolesContext
{
   /**
    * Constructor specifying connection, state ID, workflow ID and minimum
    * assignment type.
    *
    * @param workFlowID      ID of the workflow
    * @param connection      database connection
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
      String lowerCaseRoleName;
      Integer roleID;
      /* ID of the workflow for this state. */
      /* Connection to the database */
      /* State ID for this context. */
      try
      {
         statement =
            PSPreparedStatement.getPreparedStatement(connection, QRYSTRING);
         statement.clearParameters();
         statement.setInt(1, workFlowID);
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
            roleID = m_nStateRoleID;
            m_StateRoleIDs.add(roleID);
            lowerCaseRoleName = m_sStateRoleName.toLowerCase();

            m_isNotificationOnMap.put(roleID, m_bNotifyOn);
            m_stateRoleNameMap.put(roleID, m_sStateRoleName);
            m_stateRoleAssignmentTypeMap.put(roleID,
                    m_nAssignmentType);

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
    * Moves the cursor to next record in the result set and updates the current
    * column values.
    *
    * @return <CODE>true</CODE> if another record is read else
    *  <CODE>false</CODE>
    *
    * @throws SQLException On SQL error
    *
    */
   private boolean moveNext() throws SQLException
   {
      boolean bSuccess = rs.next();
      if(!bSuccess)
         return bSuccess;

      m_nStateRoleID = rs.getInt("ROLEID");
      m_nAdhocType = rs.getInt("ADHOCTYPE");
      m_nAssignmentType = rs.getInt("ASSIGNMENTTYPE");
      String tmp = rs.getString("ISNOTIFYON");
      m_bNotifyOn = null != tmp && tmp.equalsIgnoreCase("Y");

      m_sStateRoleName = PSWorkFlowUtils.trimmedOrEmptyString(
         rs.getString("ROLENAME"));

      return bSuccess;
   }


   /**
    * Closes the result set and statement if necessary
    */
   @Deprecated
   private void close()
   {
      //release resources

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

   public List<Integer> getStateRoleIDs()
   {
      return  m_StateRoleIDs;
   }

   public List<String> getStateRoleNames()
   {
      return  new ArrayList<>(m_stateRoleNameMap.values());
   }

   public List<Integer> getNonAdhocStateRoleIDs()
   {
      return m_nonAdhocStateRoleIDs;
   }

   public List<Integer> getAdhocNormalStateRoleIDs()
   {
      return m_adhocNormalStateRoleIDs;
   }

   public List<Integer> getAdhocAnonymousStateRoleIDs()
   {
      return m_adhocAnonymousStateRoleIDs;
   }

   public Map<Integer,Integer> getStateRoleAssignmentTypeMap()
   {
      return m_stateRoleAssignmentTypeMap;
   }

   public Map<Integer,String> getStateRoleNameMap()
   {
      return m_stateRoleNameMap;
   }

   public Map<Integer,Boolean>getIsNotificationOnMap()
   {
      return m_isNotificationOnMap;
   }

   public Map<String,Integer> getNonAdhocStateRoleNameToRoleIDMap()
   {
      return m_nonAdhocStateRoleNameToRoleIDMap;
   }

   public Map<String, Integer> getAdhocNormalStateRoleNameToRoleIDMap()
   {
      return m_adhocNormalStateRoleNameToRoleIDMap;
   }

   public Map<String,Integer> getLowerCaseRoleNameToIDMap()
   {
      return m_lowerCaseRoleNameToIDMap;
   }

   public boolean isEmpty()
   {
      return (0 == m_nCount);
   }


   /* *** End Implementation of IPSStateRolesContext Interface *** */

   /**
    * Produces a string representation of the state role context.
    *
    * @return a string representation of the state roles context
    */
   public String toString()
   {
       StringBuilder buf = new StringBuilder();
       buf.append("\nPSContentAdhocUsersContext:");
       if (m_StateRoleIDs.isEmpty())
       {
          buf.append("\nNo state roles");
       }
       else
       {
          buf.append("\nState roles  = ");
          buf.append(m_StateRoleIDs);
       }

       if (m_nonAdhocStateRoleIDs.isEmpty())
       {
          buf.append("\nNo non adhoc state roles");
       }
       else
       {
          buf.append("\nNon adhoc state role IDs  = ");
          buf.append(m_nonAdhocStateRoleIDs);
       }

       if (m_adhocNormalStateRoleIDs.isEmpty())
       {
          buf.append("\nNo adhoc normal states");
       }
       else
       {
          buf.append("\nAdhoc normal state role IDs  = ");
          buf.append(m_adhocNormalStateRoleIDs);
       }

       if (m_adhocAnonymousStateRoleIDs.isEmpty())
       {
          buf.append("\nNo adhoc anonymous state roles");
       }
       else
       {
          buf.append("\nAdhoc anonymous state role IDs  = ");
          buf.append(m_adhocAnonymousStateRoleIDs);
       }

       if (!m_stateRoleAssignmentTypeMap.isEmpty())
       {
          buf.append("\nMap role ID  -> assignment type = ");
          buf.append(m_stateRoleAssignmentTypeMap);
       }

       if (!m_stateRoleNameMap.isEmpty())
       {
          buf.append("\nMap role ID  -> role name = ");
          buf.append(m_stateRoleNameMap);
       }

       if (!m_isNotificationOnMap.isEmpty())
       {
          buf.append("\nMap role ID  -> notification on/off = ");
          buf.append(m_isNotificationOnMap);
       }

       if (!m_nonAdhocStateRoleNameToRoleIDMap.isEmpty())
       {
          buf.append("\nMap lower case role name -> nonadhoc role ID  = ");
          buf.append(m_nonAdhocStateRoleNameToRoleIDMap);
       }

       if (!m_adhocNormalStateRoleNameToRoleIDMap.isEmpty())
       {
          buf.append("\nMap lower case role name -> adhoc normal role ID = ");
          buf.append(m_adhocNormalStateRoleNameToRoleIDMap);
       }

       if (!m_lowerCaseRoleNameToIDMap.isEmpty())
       {
          buf.append("\nMap all lower case role name -> role ID = ");
          buf.append(m_lowerCaseRoleNameToIDMap);
       }

       return buf.toString();
   }

   /** JDBC version of SQL statement to be executed. */
   private PreparedStatement statement;

   /** Result of database query */
   private ResultSet rs;

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
   private List<Integer> m_StateRoleIDs = new ArrayList<>();

   /**
    * List of all state role Names
    */
   private List<String> m_StateRoleNames = new ArrayList<>();

   /**
    * List of non adhoc state role IDs
    */
   private List<Integer> m_nonAdhocStateRoleIDs = new ArrayList<>();

   /**
    * List of adhoc normal state role IDs
    */
   private List<Integer> m_adhocNormalStateRoleIDs = new ArrayList<>();

   /**
    * List of adhoc anonymous state role IDs
    */
   private List<Integer> m_adhocAnonymousStateRoleIDs = new ArrayList<>();

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
   private Map<Integer,Integer> m_stateRoleAssignmentTypeMap = new HashMap<>();

   /**
    * Map with role ID as key and value = role name
    */
   private Map<Integer,String> m_stateRoleNameMap = new HashMap<>();

   /**
    * Map with role ID as key and value <CODE>true</CODE> if notification
    * for the role is on, else <CODE>false</CODE>
    */
   private Map<Integer, Boolean> m_isNotificationOnMap = new HashMap<>();

   /**
    * Map of role IDs for nonadhoc roles with trimmed lower case role
    * names as key
    */
   private Map<String,Integer> m_nonAdhocStateRoleNameToRoleIDMap = new HashMap<>();

   /**
    * Map of role IDs for adhoc normal roles with trimmed lower case role
    * names as key
    */
   private Map<String,Integer> m_adhocNormalStateRoleNameToRoleIDMap = new HashMap<>();

   /**
    * Map of role IDs for all state roles with trimmed lower case role
    * names as key
    */
   private Map<String, Integer> m_lowerCaseRoleNameToIDMap = new HashMap<>();

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
