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

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.w3c.dom.Element;

/**
 * Adds the default Display Format workflow column to be used by List View services
 * @author federicoromanelli
 *
 */
public class PSUpgradePluginAddWorkflowColumnDefaultDisplayFormat implements IPSUpgradePlugin
{

   private PrintStream logger;
   
   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;
   
   
   /* (non-Javadoc)
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule, org.w3c.dom.Element)
    */
   public PSPluginResponse process(IPSUpgradeModule module, @SuppressWarnings("unused") Element elemData)
   {
      logger = module.getLogStream();
      Connection conn = null;
      
      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);
         fixDisplayFormatColumns(conn);
      }
      catch(Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e.getLocalizedMessage());
      }
      finally
      {
         if(conn != null)
            try
            {
               conn.close();
            }
            catch (SQLException se)
            {
               return new PSPluginResponse(PSPluginResponse.EXCEPTION, se.getLocalizedMessage());
            }
      }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }
   
   /**
    * Checks if the workflow columns has been added to the default display format.
    * If it's not, it inserts the corresponding row in table PSX_DISPLAYFORMATCOLUMNS 
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixDisplayFormatColumns(Connection conn) throws SQLException
   {
      String displayFormatTable = qualifyTableName("PSX_DISPLAYFORMATS");
      String displayFormatColumnsTable = qualifyTableName("PSX_DISPLAYFORMATCOLUMNS");
      logger.println("Searching existing Display Format for CM1_Design");
      
      String query = "Select DISPLAYID FROM " +
         displayFormatTable +
         " WHERE INTERNALNAME LIKE '%CM1_Default%'";
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      boolean updated = false;
      
      if (results.next())
      {
         int displayFormatId = results.getInt("DISPLAYID");
         logger.println("CM1_Design Display Format id:" + displayFormatId);
         
         String queryExistingWorkflowColumn = "SELECT source FROM " +
            displayFormatColumnsTable +
            " WHERE SOURCE = 'sys_workflow' " +
            "AND DISPLAYID = ?";
         
         PreparedStatement psCheckColumn = conn.prepareStatement(queryExistingWorkflowColumn);
         psCheckColumn.setInt(1, displayFormatId);
         ResultSet resultsCheckColumn = psCheckColumn.executeQuery();
         // If the column already exists we stop the execution.
         if (resultsCheckColumn.next())
         {
            logger.println("Workflow column already exists");
            return;
         }
         
         String updateDisplayFormatColumnQuery =
            "UPDATE " + displayFormatColumnsTable +
            " SET sequence = sequence + 1 " +
            " WHERE sequence >= ? " +
            " AND DISPLAYID = ?";
         PreparedStatement ps1 = conn.prepareStatement(updateDisplayFormatColumnQuery);
         ps1.setInt(1, SEQUENCE_MIN);
         ps1.setInt(2, displayFormatId);
         ps1.executeUpdate();
         
         logger.println("Columns sequence values updated");
         
         String insertDisplayFormatColumnQuery =
            "INSERT INTO " + displayFormatColumnsTable +
            "(DISPLAYID, SOURCE, DISPLAYNAME, TYPE, RENDERTYPE, SORTORDER, SEQUENCE, DESCRIPTION, WIDTH)" +
            " VALUES(?,?,?,?,?,?,?,?,?)";
         
         PreparedStatement ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);                     // DISPLAYID
         ps2.setString(2, "sys_workflow");                   // SOURCE
         ps2.setString(3, "Workflow");                       // DISPLAYNAME
         ps2.setInt(4, 0);                                   // TYPE
         ps2.setString(5, "Text");                           // RENDERTYPE
         ps2.setString(6, "A");                              // SORTORDER
         ps2.setInt(7, 3);                                   // SEQUENCE
         ps2.setString(8, "The current item workflow name.");// DESCRIPTION
         ps2.setInt(9, 80);                                  // WIDTH
         ps2.executeUpdate();
         updated = true;
         logger.println("sys_workflow row added to CM1_Default");
      }

      if(updated)
         conn.commit();
   }

   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid
    */
   private String qualifyTableName(String table)
   {
      String database = m_dbProps.getProperty("DB_NAME");
      String schema = m_dbProps.getProperty("DB_SCHEMA");
      String driver = m_dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }
   
   public static final int SEQUENCE_MIN = 3; 
}
