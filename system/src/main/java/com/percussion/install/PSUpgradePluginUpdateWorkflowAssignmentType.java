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

import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
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
 * Upgrade plugin that changes the assignment type from Reader to Assignee of 
 * all the roles of Draft state of Local Content workflow.
 * 
 * @author rafaelsalis
 * 
 */
public class PSUpgradePluginUpdateWorkflowAssignmentType implements IPSUpgradePlugin
{
   private PrintStream logger;
   
   /**
    * A <code>Connection<code> object, assumed not <code>null</code>.
    */
   private Connection conn;

   /**
    * Constants names.
    */
   private static final String WORKFLOW_TABLE = "WORKFLOWAPPS";
   private static final String STATE_TABLE = "STATES";
   private static final String STATE_ROLE_TABLE = "STATEROLES";
   private static final String LOCAL_CONTENT_WORKFLOW = "LocalContent";
   private static final String DRAFT_STATE = "Draft";
   
   /**
    * Tables names.
    */
   private static String workflowTable;
   private static String stateTable;
   private static String stateRoleTable;
   
   /**
    * Id's for the workflow, states and assignment types.
    */
   private int localContentWorkflowId;
   private int draftLocalContentId;
   
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

      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);
         
         // Initialize table names
         workflowTable = qualifyTableName(WORKFLOW_TABLE);
         stateTable = qualifyTableName(STATE_TABLE);
         stateRoleTable = qualifyTableName(STATE_ROLE_TABLE);
         
         // Initialize ids
         localContentWorkflowId = getWorkflowId(LOCAL_CONTENT_WORKFLOW);
         draftLocalContentId = getStateId(LOCAL_CONTENT_WORKFLOW, DRAFT_STATE);
         
         updateWorkflowAsignmentType();
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
    * 
    * 
    * @param conn a <code>Connection<code> object, assumed not <code>null</code>.
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateWorkflowAsignmentType() throws SQLException
   {
      String query = "UPDATE "+ stateRoleTable + " SET ASSIGNMENTTYPE = ? " +
            "WHERE WORKFLOWAPPID = ? and STATEID = ? and ASSIGNMENTTYPE = ?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, PSAssignmentTypeEnum.ASSIGNEE.getValue());
      ps.setInt(2, localContentWorkflowId);
      ps.setInt(3, draftLocalContentId);
      ps.setInt(4, PSAssignmentTypeEnum.READER.getValue());
      ps.executeUpdate();
      
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
    * @param workflow the name of the workflow.
    * @return the workflow id.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getWorkflowId(String workflow) throws SQLException
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
    * @param workflow the name of the workflow.
    * @param state the name of the state.
    * @return the state id.
    * @throws SQLException if any error occurs during DB access.
    */
   private int getStateId(String workflow, String state) throws SQLException
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

}
