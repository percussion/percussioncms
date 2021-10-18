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

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.util.PSPreparedStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class PSTransitionsContext implements IPSTransitionsContext, AutoCloseable
{
   private static final Logger log = LogManager.getLogger(IPSConstants.WORKFLOW_LOG);

   private int workflowID = 0;
   private PreparedStatement statement;
   private Connection connection;
   private ResultSet resultSet = null;
   private int count = 0;

   private int transitionID = 0;
   private String transitionLabel = "";
   private String transitionPrompt = "";
   private String transitionDesc = "";
   private int transitionFromStateID = 0;
   private int transitionToStateID = 0;
   private String transitionTrigger = "";
   private int transitionApprovalsRequired = 0;
   private String transitionComment = "";
   private String transitionActions = "";
   private String transitionRoles = "";
   private List<String> transitionRoleNamesList = null;
   private List<Integer> transitionRoleIdsList = null;
   private final Map<Integer,String> transitionRoleNamesIdMap = new HashMap<>();

   private List<String> transitionActionsList = null;
   private int transitionType = 0;
   private int agingType = 0;
   private int agingInterval = 0;
   private String systemField = "";
   private boolean isAgingTransition = false;
   
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
         resultSet = statement.executeQuery();
         if(!moveNext())
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }
         count = 1;
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
         resultSet = statement.executeQuery();
         if(!moveNext())
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }
         count = 1;
      }finally
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
         resultSet = statement.executeQuery();

         while(resultSet.next())
         {
            count++;
         }

         if(0 == count)
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }

         try
         {
            resultSet.close();
            statement.close();
         }
         catch(Exception e)
         {
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         }

         //redo the whole thing!!!
         statement =
            PSPreparedStatement.getPreparedStatement(
               connection,
               QRYSTRING_FOR_POSSIBLE_TRANSITIONS);
         statement.clearParameters();
         statement.setInt(1, workflowID);
         statement.setInt(2, transitionFromStateID);
         resultSet = statement.executeQuery();

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
      PreparedStatement stmt;
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

         transitionRoleNamesList = new ArrayList<>();
         transitionRoleIdsList = new ArrayList<>();
         while(resSet.next())
         {  /** @todo refactor to remove these lists and get from Map */
            transitionRoleNamesList.add(resSet.getString("ROLENAME"));
            transitionRoleIdsList.add(
                    resSet.getInt("TRANSITIONROLEID"));

            transitionRoleNamesIdMap.put(
                    resSet.getInt("TRANSITIONROLEID"),
               resSet.getString("ROLENAME"));
         }
         try
         {

            resSet.close();
            stmt.close();
         }
         catch(Exception e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
         }
      }
      catch(SQLException e)
      {
         throw e;
      }

   }


   public int getTransitionID()
   {
      return this.transitionID;
   }

   public String getTransitionLabel()
   {
      return this.transitionLabel;
   }

   public String getTransitionPrompt()
   {
      return this.transitionPrompt;
   }

   public String getTransitionDescription()
   {
      return this.transitionDesc;
   }

   public int getTransitionFromStateID()
   {
      return this.transitionFromStateID;
   }

   public int getTransitionToStateID()
   {
      return this.transitionToStateID;
   }

   public String getTransitionActionTrigger()
   {
      return this.transitionTrigger;
   }

   public int getTransitionApprovalsRequired()
   {
      return this.transitionApprovalsRequired;
   }

   public boolean isTransitionToInitialState()
   {
      return (0 == transitionFromStateID);
   }

   public boolean isTransitionToDifferentState()
   {
      return (transitionToStateID != transitionFromStateID);
   }

   public boolean isSelfTransition()
   {
      return (transitionToStateID == transitionFromStateID);
   }

   public boolean isAgingTransition()
   {
      return isAgingTransition;
   }

   public int getAgingType()
   {
      return agingType;
   }

   public int getAgingInterval()
   {
      return agingInterval;
   }

   public String getSystemField()
   {
      return systemField;
   }

   public int getTransitionCount()
   {
      return count;
   }

   public boolean isTransitionCommentRequired()
   {
      return !isAgingTransition && transitionComment !=null &&
          transitionComment.trim().equalsIgnoreCase("y");
   }

   public List getTransitionActions()
   {
      return transitionActionsList;
   }

   /**
    * Returns a list of role names for this transition
    * @return may be <code>null</code>
    */
   public List getTransitionRoles()
   {
      return transitionRoleNamesList;
   }

   /**
    * This is returns the value of the TRANSITIONS.TRANSITIONROLE column.
    *
    * @return never <code>null</code> may be empty.
    */
   public String getTransitionRoleColumnValue()
   {
      return transitionRoles;
   }

   /**
    * Returns a list of role id for this transition
    * @return may be <code>null</code>
    */
   public List<Integer> getTransitionRolesIds()
   {
      return transitionRoleIdsList;
   }

   /**
    * Returns a map of the roles in this transiton, with the roleid as the
    * key and the rolename as the value.
    *
    * @return the map, may be <code>null</code>.
    */
   public Map<Integer, String> getTransitionRoleNameIdMap()
   {
      return transitionRoleNamesIdMap;
   }

   public String getTransitionComment()
   {
       return transitionComment;
   }

   public boolean moveNext() throws SQLException
   {
      boolean bSuccess = resultSet.next();
      if(!bSuccess)
      {
         return bSuccess;
      }

      transitionID = resultSet.getInt("TRANSITIONID");
      transitionLabel = resultSet.getString("TRANSITIONLABEL");
      transitionPrompt = resultSet.getString("TRANSITIONPROMPT");
      transitionDesc = resultSet.getString("TRANSITIONDESC");
      transitionFromStateID = resultSet.getInt("TRANSITIONFROMSTATEID");
      transitionToStateID = resultSet.getInt("TRANSITIONTOSTATEID");
      transitionTrigger = resultSet.getString("TRANSITIONACTIONTRIGGER");
      transitionType = resultSet.getInt("TRANSITIONTYPE");
      agingType = resultSet.getInt("AGINGTYPE");
      agingInterval = resultSet.getInt("AGINGINTERVAL");
      systemField =  PSWorkFlowUtils.trimmedOrEmptyString(
         resultSet.getString("SYSTEMFIELD"));
      isAgingTransition =
            (IPSTransitionsContext.AGING_TRANSITION == transitionType);
      transitionApprovalsRequired =
            resultSet.getInt("TRANSITIONAPPROVALSREQUIRED");
      transitionComment = resultSet.getString("TRANSITIONCOMMENTREQUIRED");
      if(transitionComment ==null)
      {
         transitionComment = "n";
      }
      transitionActions = resultSet.getString("TRANSITIONACTIONS");
      transitionRoles = resultSet.getString("TRANSITIONROLES");

      if ( null == transitionActions ||

           transitionActions.trim().length() == 0 )
      {
         transitionActionsList = null;
      }
      else
      {
         transitionActionsList =
               PSWorkFlowUtils.tokenizeString(transitionActions,
                                              PSWorkFlowUtils.ROLE_DELIMITER);
      }

      // Transition roles do not make sense for aging transitions
      if (isAgingTransition || null == transitionRoles ||
          IPSTransitionsContext.NO_TRANSITION_ROLE_RESTRICTION.
          equals(transitionRoles) ||
          transitionRoles.trim().length() == 0 )
      {
         transitionRoleNamesList = null;
      }
      else
      {
         buildRolesList(transitionID, workflowID);
      }

      return bSuccess;
   }

   public boolean isEmpty()
   {
      return (0 == count);
   }

   public void close()
   {
      //release resources
      try {
         if (null != connection && !connection.getAutoCommit())
            connection.setAutoCommit(true);

         if (null != resultSet && !resultSet.isClosed())
            resultSet.close();

         if (null != statement && !statement.isClosed())
            statement.close();

         if(null != connection && !connection.isClosed())
            connection.close();
      } catch (SQLException throwables) {
         //Ignore errors
         log.debug(PSExceptionUtils.getDebugMessageForLog(throwables));
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
       "Transition   ID = " + transitionID + "\n" +
       "Transition   Label = " + transitionLabel + "\n" +
       "Transition   Type  = " + transitionType + "\n" +
       "Transition   Aging Type  = " + agingType + "\n" +
       "Transition   Aging Interval  = " + agingInterval + "\n" +
       "Transition   Aging System Field  = " + systemField + "\n";
   }

   public String toString()
   {
      return "PSTransitionsContext: " +  "\n" +
       "Transition   ID = " + transitionID + "\n" +
       "Transition   Label = " + transitionLabel + "\n" +
       "Transition   Prompt = " + transitionPrompt + "\n" +
       "Transition   Desc = " + transitionDesc + "\n" +
       "Transition   FromStateID = " + transitionFromStateID + "\n" +
       "Transition   ToStateID = " + transitionToStateID + "\n" +
       "Transition   Trigger = " + transitionTrigger + "\n" +
       "Transition   Type  = " + transitionType + "\n" +
       "Transition   Aging Type  = " + agingType + "\n" +
       "Transition   Aging Interval  = " + agingInterval + "\n" +
       "Transition   Aging System Field  = " + systemField + "\n" +
       "Transition   ApprovalsRequired = " + transitionApprovalsRequired
       + "\n" +
       "Transition   CommentRequired = " + transitionComment
       + "\n" +
       "Transition   Actions = " + transitionActions + "\n" +
       "Transition   Roles = " + transitionRoles + "\n";
   }

   /**
    * static constant string that represents the qualified table name.
    */
    private static final String TABLE_TC =
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
