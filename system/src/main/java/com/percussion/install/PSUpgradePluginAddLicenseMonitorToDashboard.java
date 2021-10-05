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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import javax.sql.rowset.serial.SerialClob;
import java.io.PrintStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.percussion.utils.container.IPSJdbcDbmsDefConstants.PWD_ENCRYPTED_PROPERTY;

/**
 * Adds the License Monitor gadget to the Dashboard of each Admin user
 */
public class PSUpgradePluginAddLicenseMonitorToDashboard implements IPSUpgradePlugin
{
   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;
   
   private PrintStream logger;
   
   private String dashboard;

   // Some useful constants
   public static final String NEW_GADGET_NAME = "perc_license_monitor_gadget";
   public static final String NEW_GADGET_URL = "/cm/gadgets/repository/perc_license_monitor_gadget/perc_license_monitor_gadget.xml";
   public static final int NEW_GADGET_COLUMN = 1;
   public static final int NEW_GADGET_ROW = 0;
   public static final String UPDATED_TABLE = "PSX_METADATA";
   public static final String UPDATED_COLUMN = "DATA";
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.install.IPSUpgradePlugin#process(com.percussion.install
    * .IPSUpgradeModule, org.w3c.dom.Element)
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule module, @SuppressWarnings("unused") Element elemData)
   {
      logger = module.getLogStream();
      Connection conn = null;

      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);

         // Begin with upgrade actions
         addLicenseMonitorToDashboards(conn);
      }
      catch (Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e.getLocalizedMessage());
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
               return new PSPluginResponse(PSPluginResponse.EXCEPTION, se.getLocalizedMessage());
            }
      }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   /**
    * Updates the corresponding rows that have the metadata information of the
    * Dashboards.
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void addLicenseMonitorToDashboards(Connection conn) throws Exception
   {
      try {
         // Will tell if the transaction was OK
         boolean updated = false;
         
         // Retrieve admin users
         ArrayList<String> admins = this.getAdminSubjects(conn);
         logger.println(admins.size() + " admin users found in the installation.");
         
         // For each admin, update its Dashboard (if it exists)
         logger.println("Retrieving and updating Dashboard configurations for each admin.");
         for (String admin : admins)
         {
            // Only select rows with admin users and with Dashboard metadata
            String query = "Select * FROM " + this.UPDATED_TABLE + " WHERE METAKEY = 'perc.user." + admin + ".dash.page.0'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet results = ps.executeQuery();
            
            // No dashboard configuration? continue to next admin
            if (! results.next())
            {
               logger.println("Admin '" + admin + "' does not have a Dashboard configuration (skipping).");
               continue;
            }
            
            // Process the DATA value and get it as string
            // logger.println("Updating " + this.UPDATED_COLUMN + " in " + this.UPDATED_TABLE + ", row with: " + results.getRow());
            logger.println("Updating the dashboard representation ('perc.user." + admin + ".dash.page.0') for admin: " + admin);
            Clob c = results.getClob(UPDATED_COLUMN);
            dashboard = c.getSubString(1, (int) c.length());
            String modifiedDashboard = processDashboardMetadata(dashboard);
            logger.println("+ Original dashboard configuration: " + dashboard);
            logger.println("+ Modified dashboard configuration: " + modifiedDashboard);
   
            // Prepare the corresponding query to update the row
            String updateDataValueQuery = "UPDATE " + this.UPDATED_TABLE + " SET METAKEY = ?, DATA = ? WHERE METAKEY = '" + results.getString("METAKEY") + "'";
            PreparedStatement ps2 = conn.prepareStatement(updateDataValueQuery);
            ps2.setString(1, results.getString("METAKEY"));     // METAKEY column
            ps2.setClob(2, new SerialClob(modifiedDashboard.toCharArray())); // DATA column
            ps2.executeUpdate();
   
            updated = true;
         }
   
         if (updated)
            conn.commit();
      } catch (Throwable e)
      {
         logger.println("ERROR: an exception happened: " + e.getMessage());
         throw new Exception(e.getMessage());
      }
   }

   /**
    * Takes a JSON representing the layout of a dashborard and add the License
    * Monitor gadget at the top of it.
    * @param dashboardMetadata assumed not <code>null</code>
    * @return String JSON representation of the Dashboard metadata with the gadget added
    */
   private String processDashboardMetadata(String dashboardMetadata)
   {
      // Create the JSON representation of the new gadget
      int nextInstanceId = this.getNextInstanceId(dashboardMetadata);
      String licenseMonitorGadget = this.getNewGadgetJSON(nextInstanceId);

      // Modify the positions of the current gadgets
      String proccessedDashboardMetadata = this.modifyCurrentGadgetsPositions(dashboardMetadata);

      // Add the gadget to the dashboard
      String delimiter = "]}}";
      proccessedDashboardMetadata = proccessedDashboardMetadata.replaceFirst("]}}", licenseMonitorGadget);
      proccessedDashboardMetadata = proccessedDashboardMetadata + delimiter;
      return proccessedDashboardMetadata;
   }
   
   /**
    * Returns a list o subject with the Admin role.
    * @param conn assumed not <code>null</code>
    * @throws Exception
    * @return ArrayList<String> with the admin usernames (subjects)
    */
   private ArrayList<String> getAdminSubjects(Connection conn) throws Exception
   {
      logger.println("Searching for existing admins usernames");
      
      ArrayList<String> admins = new ArrayList<>();
      
      String tables = " FROM PSX_ROLES as r, PSX_ROLE_SUBJECTS as r_s, PSX_SUBJECTS as s";
      String where = " WHERE r_s.ROLEID = r.ID AND r_s.SUBJECTID = s.ID AND r.NORMALNAME = 'admin'";
      String query = "Select s.NAME " + tables + where;
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      
      while (results.next())
      {
         admins.add(results.getString("NAME"));
      }
      
      return admins;
   }
   
   /**
    * Moves down one row each gadget in the right column.
    * @param dashboardMetadata String with the dashboard representation
    * @return the dashboardMetadata with the gadgets in their corresponding position
    */
   private String modifyCurrentGadgetsPositions(String dashboardMetadata)
   {
      Pattern digitPattern = Pattern.compile("\"col\":1,\"row\":(\\d+)");
      Matcher matcher = digitPattern.matcher(dashboardMetadata);
      StringBuffer result = new StringBuffer();
      
      while (matcher.find())
      {
          matcher.appendReplacement(result, "\"col\":1,\"row\":" + (Integer.parseInt(matcher.group(1)) + 1));
      }
      matcher.appendTail(result);
      
      return result.toString();
   }
   
   /**
    * Get the instanceId that the next gadget will have in the dashboard.
    * @param dashboardMetadata String with the dashboard representation
    * @return int The next instanceId that the new gadget will have
    */
   private int getNextInstanceId(String dashboardMetadata)
   {
      Pattern digitPattern = Pattern.compile("\"instanceId\":(\\d+)");
      Matcher matcher = digitPattern.matcher(dashboardMetadata);
      int maxInstanceId = 0;
      int instanceId;
      
      while (matcher.find())
      {
         instanceId = Integer.parseInt(matcher.group(1));
         if (instanceId > maxInstanceId)
         {
            maxInstanceId = instanceId;
         }
      }
      
      // We will return the next instanceId
      maxInstanceId++;
      return maxInstanceId;
   }
   
   /**
    * Checks if the Dashboard does not have any gadget.
    * @param dashboardMetadata String with the dashboard representation
    * @return int The count of instanceIds that the JSON dashboard representation has
    */
   private boolean isDashbardEmpty(String dashboardMetadata)
   {
      Pattern digitPattern = Pattern.compile("\"instanceId\"");
      Matcher matcher = digitPattern.matcher(dashboardMetadata);
      return (!matcher.find());
   }
   
   /**
    * Creates the JSON string of the new gadget.
    * @param instanceId the instanceId that the new gadget will have
    * @return String JSON representation of the new gadget
    */
   private String getNewGadgetJSON(int instanceId)
   {
      String licenseMonitorGadget = "{\"instanceId\":" + instanceId + ",";
      licenseMonitorGadget += "\"url\":\"" + this.NEW_GADGET_URL + "\",";
      licenseMonitorGadget += "\"col\":" + this.NEW_GADGET_COLUMN + ",";
      licenseMonitorGadget += "\"row\":" + this.NEW_GADGET_ROW + ",";
      licenseMonitorGadget += "\"expanded\":true}";
      
      // If instance id > 0, there is (or there are) JSON(s) representing other gadget(s)
      if (! this.isDashbardEmpty(dashboard))
      {
         licenseMonitorGadget = "," + licenseMonitorGadget;
      }
      
      return licenseMonitorGadget;
   }
}
