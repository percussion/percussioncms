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
package com.percussion.install;

import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;

/**
 * Add publish, remove and resubmit transitions to the steps of the Default
 * Workflow.
 *
 * Publish transition:
 *  · From Draft state to Live state. 
 *  · From Review state to Live state.
 *  · From Quick Edit state to Live state.
 *  · From Archive state to Live state.
 *  · For every role who have approved transition, add a permission for the publish transition.
 *  
 * Remove transition:
 *  · From Quick Edit state to Archive state.
 *  
 * Resubmit transition:
 *  · From Quick Edit state to Draft state.
 * 
 * Live state:
 *  · Changing the assignment type for assigned roles.
 * 
 * @author rafaelsalis
 * 
 */
public class PSUpgradePluginAddWorkflowPublishTransitions
      implements
         IPSUpgradePlugin
{
   private PrintStream logger;

   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;

   /**
    * Constant name for the default workflow.
    */
   private static final String DEFAULT_WORKFLOW = "Default Workflow";

   /**
    * Constant names for the states.
    */
   private static final String DRAFT_STATE = "Draft";
   private static final String REVIEW_STATE = "Review";
   private static final String QUICK_EDIT_STATE = "Quick Edit";
   private static final String ARCHIVE_STATE = "Archive";
   private static final String PENDING_STATE = "Pending";
   private static final String LIVE_STATE = "Live";
   
   /**
    * Constant name for role.
    */
   private static final String ADMIN_ROLE = "Admin";
   private static final String EDITOR_ROLE = "Editor";

   /**
    * Constants names and description for transitions.
    */
   private static final String APPROVE_TRANSITION = "Approve";
   private static final String PUBLISH_TRANSITION = "Publish";
   private static final String PUBLISH_TRANSITION_DESC = "Move to pending state for publishing.";
   private static final String REMOVE_TRANSITION = "Remove";
   private static final String REMOVE_TRANSITION_DESC = "Moves to archive.";
   private static final String RESUBMIT_TRANSITION = "Resubmit";
   private static final String RESUBMIT_TRANSITION_DESC = "Send it back to draft for reworking.";
   private static final String EDIT_TRANSITION = "Edit";

   /**
    * Tables names.
    */
   private String workflowTable;
   private String stateTable;
   private String stateRoleTable;
   private String transitionTable;
   private String transitionRoleTable;
   private String roleTable;
   
   /**
    * Id's for the workflow and states.
    */
   private int defaultWorkflowId;
   private int draftStateId;
   private int reviewStateId;
   private int quickEditStateId;
   private int pendingStateId;
   private int liveStateId;
   private int archiveStateId;
   private int adminRoleId;
   private int editorRoleId;
   
   /**
    * A <code>Connection<code> object, assumed not <code>null</code>.
    */
   private Connection conn;
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule, org.w3c.dom.Element)
    */
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      logger = module.getLogStream();

      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);

         workflowTable = qualifyTableName("WORKFLOWAPPS");
         stateTable = qualifyTableName("STATES");
         stateRoleTable = qualifyTableName("STATEROLES");
         transitionTable = qualifyTableName("TRANSITIONS");
         transitionRoleTable = qualifyTableName("TRANSITIONROLES");
         roleTable = qualifyTableName("ROLES");
         
         defaultWorkflowId = getWorkflowId(DEFAULT_WORKFLOW);
         draftStateId = getStateId(DRAFT_STATE);
         reviewStateId = getStateId(REVIEW_STATE);
         quickEditStateId = getStateId(QUICK_EDIT_STATE);
         archiveStateId = getStateId(ARCHIVE_STATE);
         pendingStateId = getStateId(PENDING_STATE);
         liveStateId = getStateId(LIVE_STATE);
         
         adminRoleId = getRoleId(ADMIN_ROLE);
         editorRoleId = getRoleId(EDITOR_ROLE);

         updateWorkflow();
      }
      catch (Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION,
               e.getLocalizedMessage());
      }
      finally
      {
         if (conn != null)
            try
            {
               conn.close();
            }
            catch (SQLException se)
            {
               return new PSPluginResponse(PSPluginResponse.EXCEPTION,
                     se.getLocalizedMessage());
            }
      }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   /**
    * Update the corresponding tables by changing the values for different tables
    * that store the data used by workflow.
    * 
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateWorkflow() throws SQLException
   {
      logger.println("Running upgrade plugin to add the Publish transition to system and custom steps.");
      
      // Add default Publish transition
      addPublishTransition();

      // Add publish transition according existent Approve role transitions
      addPublishTransitionAccordingApproveTransition();

      // Add default Remove transition
      addRemoveTransition();

      // Add Resubmit transition
      addResubmitTransition();
      
      // Update the Live state by changing the assignment type for assigned roles
      updateLiveState();

      conn.commit();
   }

   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid.
    * @return the table
    */
   private String qualifyTableName(String table)
   {
      String database = m_dbProps.getProperty("DB_NAME");
      String schema = m_dbProps.getProperty("DB_SCHEMA");
      String driver = m_dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }

   /**
    * Add a publish transition for each state of the Default Workflow:
    *   <ul>
    *       <li>From Draft to Pending</li>
    *       <li>From Review to Pending</li>
    *       <li>From Archive to Pending</li>
    *       <li>From Quick Edit to Pending</li>
    *       <li>All custom steps to Pending</li>
    *   </ul>
    * 
    * @throws SQLException if any error occurs during DB access.
    */
   private void addPublishTransition() throws SQLException
   {
      // Add the transition for Draft State
      logger.println("Adding transition for the state ID: " + draftStateId);
      executeInsertTransitionQuery(draftStateId);
            
      // Add the transition for Review State
      logger.println("Adding transition for the state ID: " + reviewStateId);
      executeInsertTransitionQuery(reviewStateId);
            
      // Add the transition for Archive State
      logger.println("Adding transition for the state ID: " + archiveStateId);
      executeInsertTransitionQuery(archiveStateId);
            
      // Add the transition for Quick Edit State
      logger.println("Adding transition for the state ID: " + quickEditStateId);
      executeInsertTransitionQuery(quickEditStateId);

      // Add the transition for custom steps
      logger.println("Adding transition for the custom steps");
            
      String query = "SELECT STATEID, STATENAME FROM " + stateTable + " WHERE WORKFLOWAPPID = " + defaultWorkflowId + " AND " +
      "STATENAME NOT IN ('" + DRAFT_STATE + "', '" + REVIEW_STATE + "', '" + QUICK_EDIT_STATE + "', '" + 
            ARCHIVE_STATE + "', '" + LIVE_STATE + "', '" + PENDING_STATE + "')";
         
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      
      while (results.next())
      {
         int stateId = results.getInt("STATEID");
         String stateName = results.getString("STATENAME");
      
         logger.println("Adding transition for the custom step ID: " + stateId);
         logger.println("Custom step name: " + stateName);
         
         executeInsertTransitionQuery(stateId);
      }
   }
   
   /**
    * Add a Publish transition for each state of the Default Workflow that
    * already contains a Approve transition associated.
    * 
    * @throws SQLException if any error occurs during DB access.
    */
   private void addPublishTransitionAccordingApproveTransition() 
         throws SQLException
   {
      List<Integer> approveTransitionsId = getTransitionId(APPROVE_TRANSITION);
      List<Integer> publishTransitionsId = getTransitionId(PUBLISH_TRANSITION);
      
      List<Integer> publishRolesTransitionId = getDifference(
            getTransitionRoleId(approveTransitionsId), getTransitionRoleId(publishTransitionsId));
      
      for (Integer publishTransition : publishTransitionsId)
      {
         for (Integer publishTransitionRole : publishRolesTransitionId)
         {
            if (!existTransition(publishTransition, publishTransitionRole))
            {
               addRoleTransition(publishTransition, publishTransitionRole);
            }
         }
      }
   }

   /**
    * Add a Remove transition for the state of the Default Workflow:
    *   <ul>
    *       <li>From Quick Edit to Archive</li>
    *   </ul>
    * 
    * @throws SQLException if any error occurs during DB access.
    */
   private void addRemoveTransition() throws SQLException
   {
      // Add the transition
      logger.println("Adding transition for the state ID: " + quickEditStateId);
      String query = "INSERT INTO " + transitionTable
            + "(TRANSITIONID, TRANSITIONTYPE, TRANSITIONLABEL, TRANSITIONFROMSTATEID, TRANSITIONTOSTATEID, "
            + "TRANSITIONACTIONTRIGGER, TRANSITIONAPPROVALSREQUIRED, TRANSITIONDESC, WORKFLOWAPPID, "
            + "TRANSITIONROLES, TRANSITIONCOMMENTREQUIRED, DEFAULTTRANSITION) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      int transitionId = getNextTransitionId();

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, transitionId);               // TRANSITIONID
      ps.setInt(2, 0);                          // TRANSITIONTYPE
      ps.setString(3, REMOVE_TRANSITION);       // TRANSITIONLABEL
      ps.setInt(4, quickEditStateId);           // TRANSITIONFROMSTATEID
      ps.setInt(5, archiveStateId);             // TRANSITIONTOSTATEID
      ps.setString(6, REMOVE_TRANSITION);       // TRANSITIONACTIONTRIGGER
      ps.setInt(7, 1);                          // TRANSITIONAPPROVALSREQUIRED
      ps.setString(8, REMOVE_TRANSITION_DESC);  // TRANSITIONDESC
      ps.setInt(9, defaultWorkflowId);          // WORKFLOWAPPID
      ps.setString(10, "*Specified*");          // TRANSITIONROLES
      ps.setString(11, "n");                    // TRANSITIONCOMMENTREQUIRED
      ps.setString(12, "n");                    // DEFAULTTRANSITION
      ps.executeUpdate();
      
      // Add the Admin role
      addRoleTransition(transitionId, adminRoleId);
   }

   /**
    * Add a Resubmit transition for the state of the Default Workflow:
    *   <ul>
    *       <li>From Quick Edit to Draft</li>
    *   </ul>
    * 
    * @throws SQLException if any error occurs during DB access.
    */
   private void addResubmitTransition() throws SQLException
   {
      // Add the transition
      logger.println("Adding transition for the state ID: " + quickEditStateId);
      String query = "INSERT INTO " + transitionTable
            + "(TRANSITIONID, TRANSITIONTYPE, TRANSITIONLABEL, TRANSITIONFROMSTATEID, TRANSITIONTOSTATEID, "
            + "TRANSITIONACTIONTRIGGER, TRANSITIONAPPROVALSREQUIRED, TRANSITIONDESC, WORKFLOWAPPID, "
            + "TRANSITIONROLES, TRANSITIONCOMMENTREQUIRED, DEFAULTTRANSITION) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      int transitionId = getNextTransitionId();

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, transitionId);                   // TRANSITIONID
      ps.setInt(2, 0);                              // TRANSITIONTYPE
      ps.setString(3, RESUBMIT_TRANSITION);         // TRANSITIONLABEL
      ps.setInt(4, quickEditStateId);               // TRANSITIONFROMSTATEID
      ps.setInt(5, draftStateId);                   // TRANSITIONTOSTATEID
      ps.setString(6, RESUBMIT_TRANSITION);         // TRANSITIONACTIONTRIGGER
      ps.setInt(7, 1);                              // TRANSITIONAPPROVALSREQUIRED
      ps.setString(8, RESUBMIT_TRANSITION_DESC);    // TRANSITIONDESC
      ps.setInt(9, defaultWorkflowId);              // WORKFLOWAPPID
      ps.setString(10, "*Specified*");              // TRANSITIONROLES
      ps.setString(11, "n");                        // TRANSITIONCOMMENTREQUIRED
      ps.setString(12, "n");                        // DEFAULTTRANSITION
      ps.executeUpdate();
      
      // Add the Admin role
      addRoleTransition(transitionId, adminRoleId);
   }
   
   /**
    * Update the Live state by changing the assignment type for assigned roles
    * and adding the roles to the Edit transition.
    *    
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateLiveState() throws SQLException
   {
      changeAssignmentTypeRole(liveStateId, adminRoleId, PSAssignmentTypeEnum.ASSIGNEE.getValue());
      changeAssignmentTypeRole(liveStateId, editorRoleId, PSAssignmentTypeEnum.ASSIGNEE.getValue());
      int transitionId = getTransitionIdByState(liveStateId, EDIT_TRANSITION);
      addRoleTransition(transitionId, adminRoleId);
      addRoleTransition(transitionId, editorRoleId);
   }
   
   /**
    * Perform an insert query to insert a Publish transition from the state passed
    * as parameter.
    * 
    * @param transitionFromStateId the state Id to set the fromStateId value
    *
    * @throws SQLException if any error occurs during DB access.
    */
   private void executeInsertTransitionQuery(int transitionFromStateId) throws SQLException
   {
      String insertTransitionQuery = "INSERT INTO " + transitionTable
      + "(TRANSITIONID, TRANSITIONTYPE, TRANSITIONLABEL, TRANSITIONFROMSTATEID, TRANSITIONTOSTATEID, "
      + "TRANSITIONACTIONTRIGGER, TRANSITIONAPPROVALSREQUIRED, TRANSITIONDESC, WORKFLOWAPPID, "
      + "TRANSITIONROLES, TRANSITIONCOMMENTREQUIRED, DEFAULTTRANSITION) "
      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      int transitionId = getNextTransitionId();
      
      PreparedStatement psTransitionStatement = conn.prepareStatement(insertTransitionQuery);
      psTransitionStatement.setInt(1, transitionId);                   // TRANSITIONID
      psTransitionStatement.setInt(2, 0);                              // TRANSITIONTYPE
      psTransitionStatement.setString(3, PUBLISH_TRANSITION);          // TRANSITIONLABEL
      psTransitionStatement.setInt(4, transitionFromStateId);          // TRANSITIONFROMSTATEID
      psTransitionStatement.setInt(5, pendingStateId);                 // TRANSITIONTOSTATEID
      psTransitionStatement.setString(6, PUBLISH_TRANSITION);          // TRANSITIONACTIONTRIGGER
      psTransitionStatement.setInt(7, 1);                              // TRANSITIONAPPROVALSREQUIRED
      psTransitionStatement.setString(8, PUBLISH_TRANSITION_DESC);     // TRANSITIONDESC
      psTransitionStatement.setInt(9, defaultWorkflowId);              // WORKFLOWAPPID
      psTransitionStatement.setString(10, "*Specified*");              // TRANSITIONROLES
      psTransitionStatement.setString(11, "n");                        // TRANSITIONCOMMENTREQUIRED
      psTransitionStatement.setString(12, "n");                        // DEFAULTTRANSITION
      psTransitionStatement.executeUpdate();
   }
   
   /**
    * Add a transition role to the transition.
    * 
    * @param transitionId the id of the transitionId column.
    * @param transitionRoleId the id of the transitionRoleId column.
    * @throws SQLException if any error occurs during DB access.
    */
   private void addRoleTransition(int transitionId, int transitionRoleId) 
         throws SQLException
   {
      // Add a role transition
      logger.println("Adding role ID: " + transitionRoleId +
           " for publish transition ID: " + transitionId);
      String query = "INSERT INTO " + transitionRoleTable 
            + " (TRANSITIONID, WORKFLOWAPPID, TRANSITIONROLEID) "
            + "VALUES (?, ?, ?)";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, transitionId);         // TRANSITIONID
      ps.setInt(2, defaultWorkflowId);    // WORKFLOWAPPID
      ps.setInt(3, transitionRoleId);     // TRANSITIONROLEID
      ps.executeUpdate();
   }
   
   /**
    * Change the assignment type for a given role in a step.
    * 
    * @param stateId the id of the stateId column.
    * @param roleId the id of the roleId column.
    * @param assignmentType the new assignment type.
    * @throws SQLException if any error occurs during DB access.
    */
   private void changeAssignmentTypeRole(int stateId, int roleId, int assignmentType) throws SQLException
   {
      // Update a state role
      logger.println("Updating role ID: " + roleId +
           " for state ID: " + stateId);
      
      String query = "UPDATE " + stateRoleTable 
            + " SET ASSIGNMENTTYPE = ? WHERE WORKFLOWAPPID = ? AND " 
            + " STATEID = ? AND ROLEID = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, assignmentType);       // ASSIGNMENTTYPE
      ps.setInt(2, defaultWorkflowId);    // WORKFLOWAPPID
      ps.setInt(3, stateId);              // STATEID
      ps.setInt(4, roleId);               // ROLEID
      ps.executeUpdate();
   }

   /**
    * Retrieve the workflow id.
    * 
    * @param workflow the name of the workflow.
    * @return the id of the workflow.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getWorkflowId(String workflow) throws SQLException
   {
      int id = 0;
      logger.println("Finding workflow: " + workflow);
      String query = "SELECT WORKFLOWAPPID FROM " + workflowTable
            + " WHERE WORKFLOWAPPNAME = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, workflow);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         id = results.getInt("WORKFLOWAPPID");
         logger.println(workflow + " ID: " + id);
      }

      return id;
   }

   /**
    * Retrieve the state id.
    * 
    * @param state the name of the state.
    * @return the id of the state.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getStateId(String state) throws SQLException
   {
      int id = 0;
      logger.println("Finding state: " + state);
      String query = "SELECT STATEID FROM " + stateTable
            + " WHERE STATENAME = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, state);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         id = results.getInt("STATEID");
         logger.println(state + " ID: " + id);
      }

      return id;
   }

   /**
    * Retrieves all the transitions id's which has the transitition label.
    * 
    * @param transitionLabel the name of the transition to retrieve.
    * @return a list containing all id's which has the transition label.
    * @throws SQLException if any error occurs during DB access.
    */
   private List<Integer> getTransitionId(String transitionLabel) throws SQLException
   {
      List<Integer> ids = new ArrayList<>();
      logger.println("Finding all transitions associated to " + transitionLabel + " transition.");
      String query = "SELECT TRANSITIONID FROM " + transitionTable
            + " WHERE WORKFLOWAPPID = ? AND TRANSITIONLABEL = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, defaultWorkflowId);
      ps.setString(2, transitionLabel);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         ids.add(results.getInt("TRANSITIONID"));
      }

      return ids;
   }
   
   /**
    * Retrieves the transition id for the state and transition label
    * passed as parameters.
    * 
    * @param fromStateId the id of the state.
    * @param transitionLabel the name of the transition to retrieve.
    * @return the ID of the transition
    * @throws SQLException if any error occurs during DB access.
    */
   private int getTransitionIdByState(int fromStateId, String transitionLabel) throws SQLException
   {
      int transitionId = 0;
      
      logger.println("Finding transition associated to the state ID: " + fromStateId);
      String query = "SELECT TRANSITIONID FROM " + transitionTable
            + " WHERE WORKFLOWAPPID = ? AND TRANSITIONLABEL = ? AND TRANSITIONFROMSTATEID = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, defaultWorkflowId);
      ps.setString(2, transitionLabel);
      ps.setInt(3, fromStateId);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         transitionId = results.getInt("TRANSITIONID");
      }

      return transitionId;
   }
   
   /**
    * Retrieve the role id.
    * 
    * @param role the name of the role.
    * @return the id of the role.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getRoleId(String role) throws SQLException
   {
      int id = 0;
      logger.println("Finding role: " + role);
      String query = "SELECT ROLEID FROM " + roleTable
            + " WHERE WORKFLOWAPPID = ? AND ROLENAME = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, defaultWorkflowId);
      ps.setString(2, role);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         id = results.getInt("ROLEID");
         logger.println(role + " ID: " + id);
      }

      return id;
   }
   
   /**
    * Retrieves all the transitions roles id's which has approve transitition.
    * 
    * @param transitionsId a list containing the id's for the required transition.
    * @return a list containing all id's which has approve transition.
    * @throws SQLException if any error occurs during DB access.
    */
   private List<Integer> getTransitionRoleId(
         List<Integer> transitionsId) throws SQLException
   {
      List<Integer> ids = new ArrayList<>();
      
      for (Integer transitionId : transitionsId)
      {
         logger.println("Finding all transitions roles associated to approve transition.");
         String query = "SELECT DISTINCT TRANSITIONROLEID FROM " + transitionRoleTable
               + " WHERE WORKFLOWAPPID = ? AND TRANSITIONID = ?";
         PreparedStatement ps = conn.prepareStatement(query);
         ps.setInt(1, defaultWorkflowId);
         ps.setInt(2, transitionId);
         ResultSet results = ps.executeQuery();

         while (results.next())
         {
            int id = results.getInt("TRANSITIONROLEID");
            if (!ids.contains(id))
            {
               ids.add(id);               
            }
         }
      }
      
      return ids;
   }
   
   /**
    * Checks if a transition with idTransition exists.
    * 
    * @param transition the id of the transition to check.
    * @param transitionRole the id of the transition role to check.
    * @return <code>true</code> if exists. 
    * @throws SQLException if any error occurs during DB access.
    */
   private boolean existTransition(Integer transition, Integer transitionRole)
         throws SQLException
   {
      Boolean exist = false;
      logger.println("Checking for transition id: " + transition);
      String query = "SELECT TRANSITIONID FROM " + transitionRoleTable
            + " WHERE WORKFLOWAPPID = ? AND TRANSITIONID = ? AND TRANSITIONROLEID = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, defaultWorkflowId);
      ps.setInt(2, transition);
      ps.setInt(3, transitionRole);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         exist = true;
      }
      
      return exist;
   }
   
   /**
    * Retrieves the next free transition id from the transition table.
    * 
    * @return the id.
    * @throws SQLException if any error occurs during DB access.
    */
   private Integer getNextTransitionId() throws SQLException
   {
      Integer id = null;
      String queryLastId = "SELECT MAX(TRANSITIONID) AS TRANSITIONID FROM TRANSITIONS";
      PreparedStatement psLastId = conn.prepareStatement(queryLastId);
      ResultSet resultsLastId = psLastId.executeQuery();

      if (resultsLastId.next())
      {
         id = resultsLastId.getInt("TRANSITIONID") + 1;
      }

      return id;
   }
   
   /**
    * Creates a list containing the id's of the roles to add Publish transition.
    * 
    * @param list1 the minuend of the operation.
    * @param list2 the subtrahend of the operation.
    * @return a list containing the difference between list1 and list2. 
    */
   private List<Integer> getDifference(List<Integer> list1, List<Integer> list2)
   {
      List<Integer> result = new ArrayList<>(list1);
      for (Integer value : list2)
      {
         if (list1.contains(value))
         {
            result.remove(value);            
         }
      }
      
      return result;
   }

}
