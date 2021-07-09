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
 * Adds the default Display Format columns to be used by List View services (uiService)
 * @author federicoromanelli
 *
 */
public class PSUpgradePluginAddDefaultDisplayFormatValues implements IPSUpgradePlugin
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
         fixDisplayFormat(conn);
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
    * Checks if the default display format is already in the database.
    * If it is, it set the displayFormatId with the current value.
    * If it's not, it inserts the corresponding row in table PSX_DISPLAYFORMATS 
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixDisplayFormat(Connection conn) throws SQLException
   {
      String displayFormatTable = qualifyTableName("PSX_DISPLAYFORMATS");
      logger.println("Searching existing Display Format for CM1_Default");
      String query = "Select DISPLAYID FROM " +
         displayFormatTable +
         " WHERE INTERNALNAME LIKE '%CM1_Default%'";
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      boolean updated = false;
      
      if (!results.next())
      {
         String queryLastId = "Select max(DISPLAYID) as DISPLAYID FROM " + displayFormatTable;
         PreparedStatement psLastId = conn.prepareStatement(queryLastId);
         ResultSet resultsLastId = psLastId.executeQuery();
         if (resultsLastId.next())
         {
            displayFormatId = resultsLastId.getInt("DISPLAYID") + 1;
         
            logger.println("Display Format not found, inserting corresponding row with id " + displayFormatId);
            String insertDisplayFormatQuery =
               "INSERT INTO " + displayFormatTable + "(DISPLAYID, INTERNALNAME, DISPLAYNAME, DESCRIPTION, VERSION)" +
               " VALUES(?,?,?,?,?)";
            
            PreparedStatement ps2 = conn.prepareStatement(insertDisplayFormatQuery);
            ps2.setInt(1, displayFormatId);                // DISPLAYID
            ps2.setString(2, "CM1_Default");               // INTERNALNAME
            ps2.setString(3, "Default");                   // DISPLAYNAME
            ps2.setString(4, "Default display format.");   // DESCRIPTION
            ps2.setInt(5, 0);                              // VERSION
            ps2.executeUpdate();
            updated = true;
         }
      }
      else
      {
         displayFormatId = results.getInt("DISPLAYID");
         logger.println("Display Format found, updating display format id with: " + displayFormatId);         
      }

      if(updated)
         conn.commit();
   }

   /**
    * Checks if the default display format columns are already in the database.
    * If they are not, it inserts the corresponding rows in table PSX_DISPLAYFORMATCOLUMNS 
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixDisplayFormatColumns(Connection conn) throws SQLException
   {
      if (displayFormatId == -1)
         return;
      String displayFormatColumnsTable = qualifyTableName("PSX_DISPLAYFORMATCOLUMNS");
      logger.println("Searching existing Display Format Columns for CM1_Default");
      String query = "Select SOURCE FROM " +
         displayFormatColumnsTable +
         " WHERE DISPLAYID = ?";
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, displayFormatId);
      ResultSet results = ps.executeQuery();
      boolean updated = false;

      if (!results.next())
      {
         logger.println("Display Format Columns not found, inserting corresponding rows for fields" +
                 " sys_title, sys_contenttypename, sys_statename, sys_contentlastmodifieddate, sys_postdate," +
                 " sys_contentcreateddate and sys_contentcreatedby");
         String insertDisplayFormatColumnQuery =
            "INSERT INTO " + displayFormatColumnsTable +
            "(DISPLAYID, SOURCE, DISPLAYNAME, TYPE, RENDERTYPE, SORTORDER, SEQUENCE, DESCRIPTION)" +
            " VALUES(?,?,?,?,?,?,?,?)";
         
         PreparedStatement ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);                // DISPLAYID
         ps2.setString(2, "sys_title");                 // SOURCE
         ps2.setString(3, "Name");                      // DISPLAYNAME
         ps2.setInt(4, 0);                              // TYPE
         ps2.setString(5, "Text");                      // RENDERTYPE
         ps2.setString(6, "A");                         // SORTORDER
         ps2.setInt(7, 0);                              // SEQUENCE
         ps2.setString(8, "The name of the item.");     // DESCRIPTION
         ps2.executeUpdate();
         
         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_contenttypename");
         ps2.setString(3, "Type");
         ps2.setInt(4, 0);
         ps2.setString(5, "Text");
         ps2.setString(6, "A");
         ps2.setInt(7, 1);
         ps2.setString(8, "The item type.");
         ps2.executeUpdate();

         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_statename");
         ps2.setString(3, "Status");
         ps2.setInt(4, 0);
         ps2.setString(5, "Text");
         ps2.setString(6, "A");
         ps2.setInt(7, 2);
         ps2.setString(8, "The current item workflow status.");
         ps2.executeUpdate();

         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_contentlastmodifieddate");
         ps2.setString(3, "Modified");
         ps2.setInt(4, 0);
         ps2.setString(5, "Date");
         ps2.setString(6, "A");
         ps2.setInt(7, 3);
         ps2.setString(8, "The item's last modified date.");
         ps2.executeUpdate();

         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_postdate");
         ps2.setString(3, "Published");
         ps2.setInt(4, 0);
         ps2.setString(5, "Date");
         ps2.setString(6, "A");
         ps2.setInt(7, 4);
         ps2.setString(8, "The item's publish date.");
         ps2.executeUpdate();

         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_contentcreateddate");
         ps2.setString(3, "Created");
         ps2.setInt(4, 0);
         ps2.setString(5, "Date");
         ps2.setString(6, "A");
         ps2.setInt(7, 5);
         ps2.setString(8, "The item's creation date.");
         ps2.executeUpdate();
         
         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_contentcreatedby");
         ps2.setString(3, "Author");
         ps2.setInt(4, 0);
         ps2.setString(5, "Text");
         ps2.setString(6, "A");
         ps2.setInt(7, 6);
         ps2.setString(8, "The item's creator.");
         ps2.executeUpdate();
         
         updated = true;         
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
   
   // The default display format id (it's value is updated in method fixDisplayFormat if needed)
   private int displayFormatId = -1;
}
