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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Update the transitions names and flows for the default workflow.
 * Approved Step:
 *    · Change the transition name from Take Down to Archive. Change the trigger value as well from Take Down to Archive
 * Archive Step:
 *    · Change the transition name from Submit to Resubmit. Change the trigger value as well from Submit to Resubmit.
 *    · Change the “to state” of that transition to Draft.
 * 
 * @author leonardohildt
 * @author rafaelsalis
 * 
 */
public class PSUpgradePluginUpdateWorkflowTransitions implements IPSUpgradePlugin
{
   private PrintStream logger;

   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.install.IPSUpgradePlugin#process(com.percussion.install
    * .IPSUpgradeModule, org.w3c.dom.Element)
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      logger = module.getLogStream();
      Connection conn = null;

      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);
         updateTransitionNames(conn);
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
    * Update the corresponding tables by changing the transition names and flow.
    * 
    * @param conn a <code>Connection<code> object, assumed not <code>null</code>.
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateTransitionNames(Connection conn) throws SQLException
   {
      String workflowTable = qualifyTableName("WORKFLOWAPPS");
      String stateTable = qualifyTableName("STATES");
      String transitionTable = qualifyTableName("TRANSITIONS");
      
      // Retrieves the Default Workflow id
      int workflowId = getWorkflowId(workflowTable, "Default Workflow", conn);

      // Changes for the Approved Step
      // Change the transition name and action trigger from "Take Down" to "Archive"
      int approvedStateId = getStateId(stateTable, "Quick Edit", conn);
      updateTransitionStateName(transitionTable, workflowId, approvedStateId,
            "Take Down", "Archive", conn);

      // Changes for the Archive Step
      // Change the transition name and action trigger from "Submit" to "Resubmit"
      int archieveStateId = getStateId(stateTable, "Archive", conn);
      updateTransitionStateName(transitionTable, workflowId, archieveStateId,
            "Submit", "Resubmit", conn);

      // Change the value of “ToStateId” for the correct state Id.
      // From Review, to point to Draft.
      int reviewStateId = getStateId(stateTable, "Review", conn);
      int draftStateId = getStateId(stateTable, "Draft", conn);
      updateFlowTransition(transitionTable, workflowId, archieveStateId,
            reviewStateId, draftStateId, conn);

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
    * Retrieve the workflow id.
    * 
    * @param workflowTable the name of the workflows table.
    * @param workflow the name of the workflow.
    * @param conn a <code>Connection<code> object, assumed not <code>null</code>.
    * @return the workflow id.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getWorkflowId(String workflowTable, String workflow,
         Connection conn) throws SQLException
   {
      int workflowId = 0;
      logger.println("Finding workflow: " + workflow);
      String query = "SELECT WORKFLOWAPPID FROM " + workflowTable
            + " WHERE WORKFLOWAPPNAME = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, workflow);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         workflowId = results.getInt("WORKFLOWAPPID");
         logger.println(workflow + " ID: " + workflowId);
      }

      return workflowId;
   }

   /**
    * Retrieve the state id.
    * 
    * @param stateTable the name of the states table.
    * @param state the name of the state.
    * @param conn a <code>Connection<code> object, assumed not <code>null</code>.
    * @return the state id.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getStateId(String stateTable, String state,
         Connection conn) throws SQLException
   {
      int stateId = 0;
      logger.println("Finding step: " + state);
      String query = "SELECT STATEID FROM " + stateTable
            + " WHERE STATENAME = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, state);
      ResultSet results = ps.executeQuery();

      while (results.next())
      {
         stateId = results.getInt("STATEID");
         logger.println(state + " ID: " + stateId);
      }

      return stateId;
   }

   /**
    * Updates the attributes <code>TRANSITIONLABEL<code> and 
    * <code>TRANSITIONACTIONTRIGGER<code> of the table.
    * 
    * @param transitionTable the name of the transitions table.
    * @param workflowId the workflow id
    * @param stateId the state id on which want to update.
    * @param oldValue the value of the transition name before the updating.
    * @param newValue the value of the transition name after the updating.
    * @param conn a <code>Connection<code> object, assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateTransitionStateName(String transitionTable, int workflowId,
         int stateId, String oldValue, String newValue, Connection conn)
         throws SQLException
   {
      logger.println("Finding transitions for the step ID: " + stateId);
      String query = "UPDATE " + transitionTable + " SET TRANSITIONLABEL = ?"
            + ", TRANSITIONACTIONTRIGGER = ?" + " WHERE WORKFLOWAPPID = ?"
            + " and TRANSITIONFROMSTATEID = ?" + " and TRANSITIONLABEL = ?"
            + " and TRANSITIONACTIONTRIGGER = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, newValue);
      ps.setString(2, newValue);
      ps.setInt(3, workflowId);
      ps.setInt(4, stateId);
      ps.setString(5, oldValue);
      ps.setString(6, oldValue);
      ps.executeUpdate();
   }

   /**
    * Updates the attribute <code>TRANSITIONTOSTATEID<code> of the table.
    * 
    * @param transitionTable the name of the transitions table.
    * @param workflowId the workflow id.
    * @param stateId the state id on which want to update.
    * @param resubmitStateId the resubmit state id.
    * @param draftStateId the draft state id.
    * @param conn a <code>Connection<code> object, assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateFlowTransition(String transitionTable, int workflowId,
         int stateId, int resubmitStateId, int draftStateId,
         Connection conn) throws SQLException
   {
      logger.println("Finding transitions for the step ID: " + stateId);
      String query = "UPDATE " + transitionTable
            + " SET TRANSITIONTOSTATEID = ?" + " WHERE WORKFLOWAPPID = ?"
            + " and TRANSITIONFROMSTATEID = ?" + " and TRANSITIONTOSTATEID = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, draftStateId);
      ps.setInt(2, workflowId);
      ps.setInt(3, stateId);
      ps.setInt(4, resubmitStateId);
      ps.executeUpdate();
   }

}
