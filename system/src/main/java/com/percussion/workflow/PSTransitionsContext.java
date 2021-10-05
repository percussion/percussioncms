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
 * A class that provides methods for accessing and setting fields in the
 * transitions context. The table joins are hidden from the user.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
public class PSTransitionsContext implements IPSTransitionsContext
{
   private int workflowID = 0;
   private PreparedStatement statement = null;
   private Connection connection = null;
   private ResultSet m_Rs = null;
   private int m_nCount = 0;

   private int m_nTransitionID = 0;
   private String m_sTransitionLabel = "";
   private String m_sTransitionPrompt = "";
   private String m_sTransitionDesc = "";
   private int m_nTransitionFromStateID = 0;
   private int m_nTransitionToStateID = 0;
   private String m_sTransitionTrigger = "";
   private int m_nTransitionApprovalsRequired = 0;
   private String m_sTransitionComment = "";
   private String m_sTransitionActions = "";
   private String m_sTransitionRoles = "";
   private List m_TransitionRoleNames_List = null;
   private List m_TransitionRoleIds_List = null;
   private Map m_transitionRoleNamesIdMap = new HashMap();

   private List  m_TransitionActions_List = null;
   private int m_nTransitionType = 0;
   private int m_nAgingType = 0;
   private int m_nAgingInterval = 0;
   private String m_sSystemField = "";
   private boolean m_bIsAgingTransition = false;
   
   /**
    * Constructor specifying the transition ID.
    *
    * @param transitionID        ID of the transition
    * @param connection          data base connection
    * @throws                    SQLException if an SQL error occurs
    * @throws                    PSEntryNotFoundException if no records
    *                            were returned corresponding to this set
    *                            of data.
    */
   public PSTransitionsContext(int transitionID, int workflowid,
                               Connection connection)
      throws SQLException, PSEntryNotFoundException
   {
      this.connection = connection;
      try
      {
         statement =
            PSPreparedStatement.getPreparedStatement(
               connection,
               QRYSTRING_WITH_TRANSITIONID);
         statement.clearParameters();
         statement.setInt(1, transitionID);
         statement.setInt(2, workflowid);
         m_Rs = statement.executeQuery();
         if(false == moveNext())
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }
         m_nCount = 1;
      }
      catch(SQLException e)
      {
         close();
         throw e;
      }
   }

   /**
    * Constructor specifying the workFlowID, connection, transition trigger,
    * and state which the content item is transitioning from.
    *
    * @param workFlowID          ID of the workflow for this item
    * @param connection          data base connection
    * @param transitionTrigger   trigger for the action associated with this
    *                            transition, e.g. "reject"
    * @param transitionFromStateID  state which item is transitioning from
    * @throws                    SQLException if an SQL error occurs
    * @throws                    PSEntryNotFoundException if no records
    *                            were returned corresponding to this set
    *                            of data.
    */
   public PSTransitionsContext(int workFlowID,
                               Connection connection,
                               String transitionTrigger,
                               int transitionFromStateID)
      throws SQLException, PSEntryNotFoundException
   {
      this.workflowID = workFlowID;
      this.connection = connection;

      try
      {
         statement =
            PSPreparedStatement.getPreparedStatement(
               connection,
               QRYSTRING_FOR_ASSIGNMENT);
         statement.clearParameters();
         statement.setInt(1, workflowID);
         statement.setString(2, transitionTrigger);
         statement.setInt(3, transitionFromStateID);
         m_Rs = statement.executeQuery();
         if(false == moveNext())
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }
         m_nCount = 1;
      }
      catch(SQLException e)
      {
         throw e;
      }
      finally
      {
         close();
      }
   }

   /**
    * Constructor specifying the workFlowID, connection,
    * and state which the content item is transitioning from.
    *
    * @param workFlowID          ID of the workflow for this item
    * @param connection          data base connection
    * @param transitionFromStateID  state which item is transitioning from
    * @throws                    SQLException if an SQL error occurs
    * @throws                    PSEntryNotFoundException if no records
    *                            were returned corresponding to this set
    *                            of data.
    */
   public PSTransitionsContext(int workFlowID,
                               Connection connection,
                               int transitionFromStateID)
      throws SQLException, PSEntryNotFoundException
   {
      this.workflowID = workFlowID;
      this.connection = connection;
      try
      {
         statement =
            PSPreparedStatement.getPreparedStatement(
               connection,
               QRYSTRING_FOR_POSSIBLE_TRANSITIONS);
         statement.clearParameters();
         statement.setInt(1, workflowID);
         statement.setInt(2, transitionFromStateID);
         m_Rs = statement.executeQuery();

         while(m_Rs.next())
         {
            m_nCount++;
         }

         if(0 == m_nCount)
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }

         try
         {
            m_Rs.close();
            statement.close();
         }
         catch(Exception e)
         {
         }

         //redo the whole thing!!!
         statement =
            PSPreparedStatement.getPreparedStatement(
               connection,
               QRYSTRING_FOR_POSSIBLE_TRANSITIONS);
         statement.clearParameters();
         statement.setInt(1, workflowID);
         statement.setInt(2, transitionFromStateID);
         m_Rs = statement.executeQuery();

         moveNext();
      }
      catch(SQLException e)
      {
         close();
         throw e;
      }
   }

   /**
    * This makes a request to get the roles, names and ids of the
    * transition.  A join would work but there are far too many changes
    * to do at this time.  This must be called after the <code>connection</code>
    * is set.
    */
   private void buildRolesList(int transitionID, int workflowid)
      throws SQLException
   {
      PreparedStatement stmt = null;
      String queryString = "SELECT ROLES.ROLENAME, " +
      "TRANSITIONROLES.TRANSITIONROLEID " +
      "FROM ROLES, TRANSITIONROLES WHERE ROLES.WORKFLOWAPPID = " +
      "TRANSITIONROLES.WORKFLOWAPPID AND ROLES.ROLEID = " +
      "TRANSITIONROLES.TRANSITIONROLEID " +
      "AND TRANSITIONROLES.TRANSITIONID = ? AND " +
      "TRANSITIONROLES.WORKFLOWAPPID = ? ";

      ResultSet resSet = null;

      try
      {
         stmt =
            PSPreparedStatement.getPreparedStatement(connection, queryString);
         stmt.clearParameters();
         stmt.setInt(1, transitionID);
         stmt.setInt(2, workflowid);
         resSet = stmt.executeQuery();

         m_TransitionRoleNames_List = new ArrayList();
         m_TransitionRoleIds_List = new ArrayList();
         while(resSet.next())
         {  /** @todo refactor to remove these lists and get from Map */
            m_TransitionRoleNames_List.add(resSet.getString("ROLENAME"));
            m_TransitionRoleIds_List.add(
               new Integer(resSet.getInt("TRANSITIONROLEID")));

            m_transitionRoleNamesIdMap.put(
               new Integer(resSet.getInt("TRANSITIONROLEID")),
               resSet.getString("ROLENAME"));
         }
         try
         {
            resSet.close();
            stmt.close();
         }
         catch(Exception e)
         {
            e.printStackTrace(System.out);
         }
      }
      catch(SQLException e)
      {
         throw e;
      }

   }


   public int getTransitionID()
   {
      return this.m_nTransitionID;
   }

   public String getTransitionLabel()
   {
      return this.m_sTransitionLabel;
   }

   public String getTransitionPrompt()
   {
      return this.m_sTransitionPrompt;
   }

   public String getTransitionDescription()
   {
      return this.m_sTransitionDesc;
   }

   public int getTransitionFromStateID()
   {
      return this.m_nTransitionFromStateID;
   }

   public int getTransitionToStateID()
   {
      return this.m_nTransitionToStateID;
   }

   public String getTransitionActionTrigger()
   {
      return this.m_sTransitionTrigger;
   }

   public int getTransitionApprovalsRequired()
   {
      return this.m_nTransitionApprovalsRequired;
   }

   public boolean isTransitionToInitialState()
   {
      return (0 == m_nTransitionFromStateID);
   }

   public boolean isTransitionToDifferentState()
   {
      return (m_nTransitionToStateID != m_nTransitionFromStateID);
   }

   public boolean isSelfTransition()
   {
      return (m_nTransitionToStateID == m_nTransitionFromStateID);
   }

   public boolean isAgingTransition()
   {
      return m_bIsAgingTransition;
   }

   public int getAgingType()
   {
      return m_nAgingType;
   }

   public int getAgingInterval()
   {
      return m_nAgingInterval;
   }

   public String getSystemField()
   {
      return m_sSystemField;
   }

   public int getTransitionCount()
   {
      return m_nCount;
   }

   public boolean isTransitionCommentRequired()
   {
      return !m_bIsAgingTransition && m_sTransitionComment!=null &&
          m_sTransitionComment.trim().equalsIgnoreCase("y");
   }

   public List getTransitionActions()
   {
      return  m_TransitionActions_List;
   }

   /**
    * Returns a list of role names for this transition
    * @return may be <code>null</code>
    */
   public List getTransitionRoles()
   {
      return m_TransitionRoleNames_List;
   }

   /**
    * This is returns the value of the TRANSITIONS.TRANSITIONROLE column.
    *
    * @return never <code>null</code> may be empty.
    */
   public String getTransitionRoleColumnValue()
   {
      return m_sTransitionRoles;
   }

   /**
    * Returns a list of role id for this transition
    * @return may be <code>null</code>
    */
   public List getTransitionRolesIds()
   {
      return m_TransitionRoleIds_List;
   }

   /**
    * Returns a map of the roles in this transiton, with the roleid as the
    * key and the rolename as the value.
    *
    * @return the map, may be <code>null</code>.
    */
   public Map getTransitionRoleNameIdMap()
   {
      return m_transitionRoleNamesIdMap;
   }

   public String getTransitionComment()
   {
       return m_sTransitionComment;
   }

   public boolean moveNext() throws SQLException
   {
      boolean bSuccess = m_Rs.next();
      if(false == bSuccess)
      {
         return bSuccess;
      }

      m_nTransitionID = m_Rs.getInt("TRANSITIONID");
      m_sTransitionLabel = m_Rs.getString("TRANSITIONLABEL");
      m_sTransitionPrompt = m_Rs.getString("TRANSITIONPROMPT");
      m_sTransitionDesc = m_Rs.getString("TRANSITIONDESC");
      m_nTransitionFromStateID = m_Rs.getInt("TRANSITIONFROMSTATEID");
      m_nTransitionToStateID = m_Rs.getInt("TRANSITIONTOSTATEID");
      m_sTransitionTrigger = m_Rs.getString("TRANSITIONACTIONTRIGGER");
      m_nTransitionType = m_Rs.getInt("TRANSITIONTYPE");
      m_nAgingType = m_Rs.getInt("AGINGTYPE");
      m_nAgingInterval = m_Rs.getInt("AGINGINTERVAL");
      m_sSystemField =  PSWorkFlowUtils.trimmedOrEmptyString(
         m_Rs.getString("SYSTEMFIELD"));
      m_bIsAgingTransition =
            (IPSTransitionsContext.AGING_TRANSITION == m_nTransitionType);
      m_nTransitionApprovalsRequired =
            m_Rs.getInt("TRANSITIONAPPROVALSREQUIRED");
      m_sTransitionComment = m_Rs.getString("TRANSITIONCOMMENTREQUIRED");
      if(m_sTransitionComment==null)
      {
         m_sTransitionComment = "n";
      }
      m_sTransitionActions = m_Rs.getString("TRANSITIONACTIONS");
      m_sTransitionRoles = m_Rs.getString("TRANSITIONROLES");

      if ( null == m_sTransitionActions ||

           m_sTransitionActions.trim().length() == 0 )
      {
         m_TransitionActions_List = null;
      }
      else
      {
         m_TransitionActions_List =
               PSWorkFlowUtils.tokenizeString(m_sTransitionActions,
                                              PSWorkFlowUtils.ROLE_DELIMITER);
      }

      // Transition roles do not make sense for aging transitions
      if (m_bIsAgingTransition || null == m_sTransitionRoles ||
          IPSTransitionsContext.NO_TRANSITION_ROLE_RESTRICTION.
          equals(m_sTransitionRoles) ||
          m_sTransitionRoles.trim().length() == 0 )
      {
         m_TransitionRoleNames_List = null;
      }
      else
      {
         buildRolesList(m_nTransitionID, workflowID);
      }

      return bSuccess;
   }

   public boolean isEmpty()
   {
      return (0 == m_nCount);
   }

   public void close()
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
         if(null!=m_Rs)
            m_Rs.close();
         m_Rs = null;
      }
      catch(SQLException e)
      {
         // quiet close
      }
      
      try
      {

         if(null!=statement)
            statement.close();
         statement = null;
      }
      catch (SQLException e)
      {
         // quiet close
      }
   }


   /**
    * Produce a string describing the transition aging related context values,
    * one to a line.
    *
    * @return string describing the transition aging related context values.
    */
   public String toString(boolean agingOnly)
   {
      if (!agingOnly)
      {
         return this.toString();
      }

      return "PSTransitionsContext: " +  "\n" +
       "Transition   ID = " + m_nTransitionID + "\n" +
       "Transition   Label = " + m_sTransitionLabel + "\n" +
       "Transition   Type  = " +  m_nTransitionType  + "\n" +
       "Transition   Aging Type  = " +  m_nAgingType  + "\n" +
       "Transition   Aging Interval  = " +  m_nAgingInterval  + "\n" +
       "Transition   Aging System Field  = " +  m_sSystemField  + "\n";
   }

   public String toString()
   {
      return "PSTransitionsContext: " +  "\n" +
       "Transition   ID = " + m_nTransitionID + "\n" +
       "Transition   Label = " + m_sTransitionLabel + "\n" +
       "Transition   Prompt = " + m_sTransitionPrompt + "\n" +
       "Transition   Desc = " + m_sTransitionDesc + "\n" +
       "Transition   FromStateID = " + m_nTransitionFromStateID + "\n" +
       "Transition   ToStateID = " + m_nTransitionToStateID + "\n" +
       "Transition   Trigger = " + m_sTransitionTrigger + "\n" +
       "Transition   Type  = " +  m_nTransitionType  + "\n" +
       "Transition   Aging Type  = " +  m_nAgingType  + "\n" +
       "Transition   Aging Interval  = " +  m_nAgingInterval  + "\n" +
       "Transition   Aging System Field  = " +  m_sSystemField  + "\n" +
       "Transition   ApprovalsRequired = " + m_nTransitionApprovalsRequired
       + "\n" +
       "Transition   CommentRequired = " + m_sTransitionComment
       + "\n" +
       "Transition   Actions = " + m_sTransitionActions + "\n" +
       "Transition   Roles = " + m_sTransitionRoles + "\n";
   }

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_TC =
      PSConnectionMgr.getQualifiedIdentifier("TRANSITIONS");
      
   private static final String TRANSITIONS_SELECT =
      "SELECT " +
      TABLE_TC + ".TRANSITIONID, " +
      TABLE_TC + ".TRANSITIONLABEL, " +
      TABLE_TC + ".TRANSITIONPROMPT, " +
      TABLE_TC + ".TRANSITIONDESC, " +
      TABLE_TC + ".TRANSITIONFROMSTATEID, " +
      TABLE_TC + ".TRANSITIONTOSTATEID, " +
      TABLE_TC + ".TRANSITIONACTIONTRIGGER, " +
      TABLE_TC + ".TRANSITIONTYPE, " +
      TABLE_TC + ".AGINGTYPE, " +
      TABLE_TC + ".AGINGINTERVAL, " +
      TABLE_TC + ".SYSTEMFIELD, " +
      TABLE_TC + ".TRANSITIONAPPROVALSREQUIRED, " +
      TABLE_TC + ".TRANSITIONCOMMENTREQUIRED, " +
      TABLE_TC + ".TRANSITIONACTIONS, " +
      TABLE_TC + ".TRANSITIONROLES " ;

  private static final String QRYSTRING_FOR_ASSIGNMENT =
      TRANSITIONS_SELECT + 
      "FROM " +
      TABLE_TC +
      " WHERE (" +
      TABLE_TC + ".WORKFLOWAPPID=? AND " +
      TABLE_TC + ".TRANSITIONACTIONTRIGGER=? " +
      "AND " +
      TABLE_TC + ".TRANSITIONFROMSTATEID=?)";


  private static final String QRYSTRING_WITH_TRANSITIONID =
      TRANSITIONS_SELECT + 
      "FROM " +
      TABLE_TC + 
      " WHERE (" +
      TABLE_TC + ".TRANSITIONID=? " +
      "AND " +
      TABLE_TC + ".WORKFLOWAPPID=?)";

  private static final String QRYSTRING_FOR_POSSIBLE_TRANSITIONS =
      TRANSITIONS_SELECT + 
      "FROM " +
      TABLE_TC + 
      " WHERE (" +
      TABLE_TC + ".WORKFLOWAPPID=? " +
      "AND " +
      TABLE_TC + ".TRANSITIONFROMSTATEID=?)";
}
