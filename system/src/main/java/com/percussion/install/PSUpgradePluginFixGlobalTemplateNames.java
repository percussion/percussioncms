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
