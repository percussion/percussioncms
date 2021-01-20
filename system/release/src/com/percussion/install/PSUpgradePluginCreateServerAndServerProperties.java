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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

/**
 * @author Ignacio Erro
 * 
 */
public class PSUpgradePluginCreateServerAndServerProperties
      implements
         IPSUpgradePlugin
{
   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;

   private Connection conn = null;

   private PrintStream logger = null;

   /**
    * Tables names
    */
   private String sitesTable;

   private String serversTable;

   private String serverPropertiesTable;

   private String editionsTable;

   private String contentListTable;
   
   private String pubStatusTable;
   
   private String deliveryTypeTable;
   
   private String publicationSiteItem;

   /**
    * Property fields to be saved in new serverPropertiesTable
    */
   private static Map<String, String> propertiesMap = initializeProperties();

   /**
    * The character that seperates the param from the value.
    */
   private static final String PARAM_SEP = "&";

   /**
    * The character that separates each parameter/value pairing from each other.
    */
   private static final String PARAM_VALUE_SEP = "=";
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.install.IPSUpgradePlugin#process(com.percussion.install
    * .IPSUpgradeModule, org.w3c.dom.Element)
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      logger = config.getLogStream();
      logger.println("Running Create Server and SiteProperties plugin");

      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();

         sitesTable = qualifyTableName("RXSITES");
         serversTable = qualifyTableName("PSX_PUBSERVER");
         serverPropertiesTable = qualifyTableName("PSX_PUBSERVER_PROPERTIES");
         editionsTable = qualifyTableName("RXEDITION");
         contentListTable = qualifyTableName("RXCONTENTLIST");
         pubStatusTable = qualifyTableName("PSX_PUBLICATION_STATUS");
         deliveryTypeTable = qualifyTableName("PSX_DELIVERY_TYPE");
         publicationSiteItem = qualifyTableName("PSX_PUBLICATION_SITE_ITEM");
         
         createServerAndServerPropertiesTables();
      }
      catch (Exception e)
      {
         e.printStackTrace(logger);
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException sqle)
            {
               return new PSPluginResponse(PSPluginResponse.EXCEPTION,
                     sqle.getLocalizedMessage());
            }
         }
      }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
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
    * Add properties to the servers and serverproperties tables for each site.
    * 
    * @throws SQLException if any error occurs during DB access.
    */
   private void createServerAndServerPropertiesTables() throws SQLException
   {
      ResultSet sites = getSites();

      while (sites.next())
      {
         logger.println("Getting publish type for site "
               + sites.getInt("SITEID") + ".");
         String publishType = getPublishType(sites.getString("SITENAME"));

         // Insert properties into servers table
         logger.println("Updating server table for site "
               + sites.getInt("SITEID") + ".");
         int newServerId = updateServersTable(sites.getInt("SITEID"),
               sites.getString("SITENAME"), publishType);

         logger.println("Updating server properties table for site "
               + sites.getInt("SITEID") + ".");
         for (Map.Entry<String, String> entry : propertiesMap.entrySet())
         {
            Object property = (sites.getObject(entry.getKey()) != null) ? sites
                  .getObject(entry.getKey()) : "";

            // Insert properties into server properties table
            updateServerPropertiesTable(newServerId, entry.getValue(), property);
         }
         
         addAdditionalServerProperties(publishType, newServerId);
         
         updateEditionsTable(sites.getInt("SITEID"), newServerId);

         updateSitesTable(sites.getInt("SITEID"), newServerId);
         
         updatePubStatusTable(newServerId);
         
         updatePublicationSiteItem(sites.getInt("SITEID"), newServerId);
         
      }
      
      updateDeliveryTypeTable();
   }

   /**
    * Retrieves all sites.
    * 
    * @return a Result Set containing all sites.
    * @throws SQLException if any error occurs during DB access.
    */
   private ResultSet getSites() throws SQLException
   {
      logger.println("Finding all sites ids.");
      String query = "SELECT * "
            + "FROM "
            + sitesTable
            + " WHERE SITENAME NOT IN ('Corporate_Investments', 'Enterprise_Investments')";

      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();

      return results;
   }

   /**
    * Add information of a server associated to a site.
    * 
    * @param siteId reference to the owner site
    * @param siteName name of the site
    * @throws SQLException if any error occurs during DB access.
    */
   private Integer updateServersTable(int siteId, String siteName,
         String publishType) throws SQLException
   {
      String query = "INSERT INTO " + serversTable
            + "(PUBSERVERID, SITEID, NAME, DESCRIPTION, PUBLISHTYPE) "
            + "VALUES(?,?,?,?,?)";

      int nextServerId = getNextId("PUBSERVERID", serversTable);

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, nextServerId);
      ps.setInt(2, siteId);
      ps.setString(3, siteName);
      ps.setString(4, "Server for site " + siteName);
      ps.setString(5, publishType);
      ps.executeUpdate();

      return nextServerId;
   }

   /**
    * Add a server property and its value associated to a server.
    * 
    * @param serverId reference to the owner server
    * @param propertyName name of the property to be saved
    * @param propertyValue value of the property to be saved
    * @throws SQLException if any error occurs during DB access.
    */
   private void updateServerPropertiesTable(int serverId, String propertyName,
         Object propertyValue) throws SQLException
   {
      String query = "INSERT INTO " + serverPropertiesTable
            + "(SERVERPROPERTYID, PUBSERVERID, PROPERTYNAME, PROPERTYVALUE) "
            + "VALUES(?,?,?,?)";

      int nextServerPropertiesId = getNextId("SERVERPROPERTYID",
            serverPropertiesTable);

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, nextServerPropertiesId);
      ps.setInt(2, serverId);
      ps.setString(3, propertyName);
      ps.setObject(4, propertyValue);
      ps.executeUpdate();
   }
   
   /**
    * @param publishType
    * @param newServerId
    * @throws SQLException 
    */
   private void addAdditionalServerProperties(String publishType, int newServerId) throws SQLException
   {
      // filesystem properties
      if (publishType.equalsIgnoreCase("filesystem"))
      {
         updateServerPropertiesTable(newServerId, "driver", "Local");
         updateServerPropertiesTable(newServerId, "ownServer", "false");
      }
      
      // ftp and sftp properties
      if (publishType.equalsIgnoreCase("ftp") || publishType.equalsIgnoreCase("sftp"))
      {
         updateServerPropertiesTable(newServerId, "driver", "FTP");
         updateServerPropertiesTable(newServerId, "ownServer", "false");
         
         if (publishType.equalsIgnoreCase("ftp"))
         {
            updateServerPropertiesTable(newServerId, "secure", "false");
         }
         else
         {
            updateServerPropertiesTable(newServerId, "secure", "true");
         }
      }
      
      // format property
      updateServerPropertiesTable(newServerId, "format", "HTML");
   }

   /**
    * Update editions table with a server reference
    * 
    * @param siteId
    * @param serverId
    * @throws SQLException
    */
   private void updateEditionsTable(int siteId, int serverId)
         throws SQLException
   {
      String query = "UPDATE " + editionsTable + " " + "SET PUBSERVER=? "
            + "WHERE DESTSITE=? ";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, serverId);
      ps.setInt(2, siteId);
      ps.executeUpdate();
   }

   /**
    * Update site table with the default server reference 
    * 
    * @param siteId
    * @param serverId
    * @throws SQLException
    */
   private void updateSitesTable(int siteId, int serverId) throws SQLException
   {
      String query = "UPDATE " + sitesTable + " " + "SET DEFAULT_PUBSERVERID=? "
            + "WHERE SITEID=? ";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, serverId);
      ps.setInt(2, siteId);
      ps.executeUpdate();
   }
   
   /**
    * Update PubStatus table with the server that was used to publish.
    * 
    * @param siteId
    * @param serverId
    * @throws SQLException
    */
   private void updatePubStatusTable(int serverId) throws SQLException
   {
      List<Integer> editions = getEditions(serverId);
      
      if (editions != null)
      {
         
         for (Integer edition : editions)
         {
            String query =
                  "UPDATE " + pubStatusTable + " " + "SET PUBSERVERID=? " +
                  "WHERE EDITION_ID=?";
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, serverId);
            ps.setInt(2, edition);
            ps.executeUpdate();
         }
      }
   }
   
   /**
    * Update DeliveryType table with new delivery types
    * 
    * @throws SQLException
    */
   private void updateDeliveryTypeTable() throws SQLException
   {
      addDeliveryType("filesystem_only",
            "Publish content to the filesystem doesn't include other delivery handlers like metadata indexer.",
            "sys_filesystem",
            0);
      addDeliveryType("ftp_only",
            "Publish content using ftp doesn't include other delivery handlers like metadata indexer.",
            "sys_ftp",
            0);
      addDeliveryType("sftp_only",
            "Publish content using sftp doesn't include other delivery handlers like metadata indexer.",
            "sys_sftp",
            0);
   }
   
   /**
    * Add a delivery type into DeliveryType table
    * 
    * @param name
    * @param description
    * @param beanName
    * @param unpubRequiresAssembly
    * @throws SQLException
    */
   private void addDeliveryType(String name, String description, String beanName, int unpubRequiresAssembly) throws SQLException
   {
      String query = "INSERT INTO " + deliveryTypeTable
            + "(ID, NAME, DESCRIPTION, BEAN_NAME, UNPUBLISHING_REQUIRES_ASSEMBLY) "
            + "VALUES(?,?,?,?,?)";

      int nextServerId = getNextId("ID", deliveryTypeTable);

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, nextServerId);
      ps.setString(2, name);
      ps.setString(3, description);
      ps.setString(4, beanName);
      ps.setInt(5, unpubRequiresAssembly);
      ps.executeUpdate();
   }
   
   /**
    * Update publicationSiteItem table
    * 
    * @param siteId
    * @param serverId
    * @throws SQLException
    */
   private void updatePublicationSiteItem(int siteId, int serverId) throws SQLException
   {
      String query = "UPDATE " + publicationSiteItem + " " + "SET SERVER_ID=? "
            + "WHERE SITE_ID=? ";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, serverId);
      ps.setInt(2, siteId);
      ps.executeUpdate();
   }
   
   /**
    * Retrieves the editions from the join between editions table and
    * pubStatus table for the given server id.
    * 
    * @param serverId
    * @return
    * @throws SQLException
    */
   private List<Integer> getEditions(int serverId) throws SQLException
   {
      String query = 
            "SELECT DISTINCT EDITIONID FROM " + editionsTable + " et, " + pubStatusTable + " st " +
            "WHERE et.EDITIONID=st.EDITION_ID AND et.PUBSERVER=?";
      
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, serverId);
      ResultSet result = ps.executeQuery();
      
      List<Integer> editions = new ArrayList<Integer>();
      while (result.next())
      {
         editions.add(result.getInt("EDITIONID"));
      }

      return editions;
   }

   /**
    * Retrieves the next free id for the specified field and table.
    * 
    * @param field table field
    * @param table table name
    * @return the id
    * @throws SQLException if any error occurs during DB access.
    */
   private Integer getNextId(String field, String table) throws SQLException
   {
      Integer id = null;
      String query = "SELECT MAX(" + field + ") AS ID " + "FROM " + table;

      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet result = ps.executeQuery();

      if (result.next())
      {
         id = result.getInt("ID") + 1;
      }

      return id;
   }

   /**
    * Retrieves publish type from RXCONTENTLIST table based on the site name
    * 
    * @param siteName the name of the site.
    * @return publishType a string with a publish type
    * @throws SQLException
    */
   private String getPublishType(String siteName) throws SQLException
   {
      String publishType = null;
      String url = null;
      String name = null;

      if (!siteName.equals("Enterprise_Investments")
            && !siteName.equals("Corporate_Investments"))
      {
         name = siteName.toUpperCase() + "_FULL_SITE";
      }
      else
      {
         if (siteName.equals("Enterprise_Investments"))
            name = "rffEiPublishNow";

         if (siteName.equals("Corporate_Investments"))
            name = "rffCiPublishNow";
      }

      String query = "SELECT URL " + "FROM " + contentListTable + " "
            + "WHERE NAME=?";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, name);
      ResultSet result = ps.executeQuery();

      if (result.next())
         url = result.getString("URL");

      publishType = getUrlParameterValue(url, "sys_deliverytype");

      return publishType;
   }

   /**
    * Initializes a static properties map.
    * 
    * @return a map with server properties initialized.
    */
   private static Map<String, String> initializeProperties()
   {
      Map<String, String> result = new HashMap<String, String>();

      result.put("ROOT", "folder");
      result.put("IPADDRESS", "serverip");
      result.put("PORT", "port");
      result.put("USERID", "userid");
      result.put("PASSWORD", "password");
      result.put("PRIVATE_KEY", "privateKey");

      return Collections.unmodifiableMap(result);
   }

   /**
    * Parse the url parameter value for the supplied name from the provided url.
    * 
    * @param url the url from which to parse the parameter value, not
    *           <code>null</code> or empty.
    * @param name the parameter name for which to parse the value from the
    *           supplied url, not <code>null</code> or empty.
    * @return the parameter value, may be <code>null</code> if not found.
    */
   private String getUrlParameterValue(String url, String name)
   {
      if (url == null || url.trim().length() == 0)
         return null;

      if (name == null || name.trim().length() == 0)
         return null;

      int pos = url.indexOf(name);
      if (pos != -1)
      {
         int startPos = url.indexOf(PARAM_VALUE_SEP, pos);
         if (startPos != -1)
         {
            startPos += PARAM_VALUE_SEP.length();
            int endPos = url.indexOf(PARAM_SEP, startPos);
            if (endPos == -1)
               return url.substring(startPos);
            else
               return url.substring(startPos, endPos);
         }
      }
      return null;
   }

}
