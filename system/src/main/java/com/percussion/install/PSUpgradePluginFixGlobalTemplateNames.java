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
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Upgrade plugin to convert old style global template names.
 * Gets the global template names from RXSITES table and
 * if it ends with .xsl removes the extension .xsl.
 *
 */
public class PSUpgradePluginFixGlobalTemplateNames implements IPSUpgradePlugin
{

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Running Fix Global Template plugin upgrade process...");
      Connection conn = null;
      PSJdbcDbmsDef dbmsDef = null;

      try
      {
         conn = RxUpgrade.getJdbcConnection();
         dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
         final String qualTableName = PSSqlHelper.qualifyTableName("RXSITES",
               dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

         String sqlSt = "SELECT " + qualTableName + ".SITEID, "
               + qualTableName + ".GLOBALTEMPLATE" + " FROM "
               + qualTableName;

         Map gtMap = new HashMap();

         Statement selStmt = conn.createStatement();
         ResultSet resultSet = null;
         try
         {
            resultSet = selStmt.executeQuery(sqlSt);
         }
         catch(SQLException se)
         {
            log("Unable to get the globaltemplate names and skipping upgrade. Reason: " + se.getMessage());
            return null;
         }
         while (resultSet.next())
         {
            gtMap.put(Integer.toString(resultSet.getInt("SITEID")), resultSet
                  .getString("GLOBALTEMPLATE"));
         }

         Iterator iter = gtMap.keySet().iterator();
         while (iter.hasNext())
         {
            String siteid = (String) iter.next();
            String gtName = (String) gtMap.get(siteid);
            if (gtName != null && gtName.toLowerCase().endsWith(".xsl"))
            {
               String newGtName = gtName.substring(0, gtName.length() - 4);
               String upSqlSt = "UPDATE " + qualTableName + " SET "
                     + qualTableName + ".GLOBALTEMPLATE = '" + newGtName
                     + "' WHERE " + qualTableName + ".SITEID = " + siteid;
               final Statement updStmt = conn.createStatement();
               updStmt.executeUpdate(upSqlSt);
               log("Converted global template from " + gtName + " to "
                     + newGtName + " for site id " + siteid);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
            conn = null;
         }
         config.getLogStream()
               .println("leaving the process() of the plugin...");
      }
      return null;
   }

   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

   private IPSUpgradeModule m_config;
}
