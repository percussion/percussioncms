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
 * Fix widget configurating values for existing widgets.
 * @author erikserating
 *
 */
public class PSUpgradePluginFixWidgetConfigValues implements IPSUpgradePlugin
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
         fixContentPostDateEnum(conn);
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
    * Fixes post date enums by changing all "rx:sys_contentcreatedate" to "rx:sys_contentpostdate"
    * in the REGION_OVERRIDES column of the CT_PAGE table.
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixContentPostDateEnum(Connection conn) throws SQLException
   {
      String pagesTable = qualifyTableName("CT_PAGE");
      logger.println("Finding all pages that contain the old enum 'rx:sys_contentcreateddate'.");
      String query = "Select CONTENTID, REVISIONID, REGION_OVERRIDES FROM " +
         pagesTable +
         " WHERE REGION_OVERRIDES LIKE '%rx:sys_contentcreateddate%'";
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      boolean updated = false;
      while(results.next())
      {
         int contentid = results.getInt("CONTENTID");
         int revisionid = results.getInt("REVISIONID");
         String content = results.getString("REGION_OVERRIDES");
         //Change all "rx:sys_contentcreatedate" to "rx:sys_contentpostdate"
         content = content.replaceAll("rx:sys_contentcreateddate", "rx:sys_contentpostdate");
         logger.println("updating 'REGION_OVERRIDES' column in 'CT_PAGE' for contentid = " 
            + contentid + " and revision = " + revisionid);
         String updateQuery = "UPDATE " + pagesTable + 
            " SET REGION_OVERRIDES = ?" +
            " WHERE CONTENTID = ?" +
            " and REVISIONID = ?";
         PreparedStatement ps2 = conn.prepareStatement(updateQuery);
         ps2.setString(1, content);
         ps2.setInt(2, contentid);
         ps2.setInt(3, revisionid);
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

}
