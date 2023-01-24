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

import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSqlHelper;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;



/**
 * This plugin has been written to check the current oracle repository version.
 * If the version is less than {@link PSSqlHelper#MIN_VERSION_ORACLE}, the
 * install will not be allowed to proceed as this version is no longer supported.
 */

public class PSPreUpgradePluginCheckOracleVersion implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSPreUpgradePluginCheckOracleVersion()
   {
   }

   /**
    * Implements the process function of IPSUpgradePlugin.  Checks the oracle
    * version of the current rhythmyx repository.  If it is less than
    * {@link PSSqlHelper#MIN_VERSION_ORACLE}, a message is returned informing
    * the user of the currently supported versions.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      int respType = PSPluginResponse.SUCCESS;
      String respMessage = "";
      Connection conn = null;
      boolean isDbMatch = true;
      String log = config.getLogFile();
      
      try
      {
         conn = RxLogTables.createConnection(RxUpgrade.getRxRepositoryProps());

         if (conn != null)
         {
            DatabaseMetaData dbmd = conn.getMetaData();
            String dbName = dbmd.getDatabaseProductName().toLowerCase();
            String metaVer = dbmd.getDatabaseProductVersion();
            if (dbName.indexOf(m_oracleName.toLowerCase()) != -1)
            {
               String msg = "";
               config.getLogStream().println("Comparing rhythmyx repository "
                     + "oracle version: " + metaVer + " with minimum "
                     + "supported version: " + PSSqlHelper.MIN_VERSION_ORACLE);
               
               if (PSSqlHelper.compareVersions(conn, 
                     PSSqlHelper.MIN_VERSION_ORACLE) >= 0)
                  msg = "This version (" + metaVer + ") is supported.";
               else
               {
                  msg = "This version (" + metaVer + ") is not supported.";
                  isDbMatch = false;
               }
               
               config.getLogStream().println(msg);
            }
         }
          
         if (!isDbMatch)
         {
            respType = PSPluginResponse.EXCEPTION;
            respMessage = RxInstallerProperties.getString("oracleVersionNotSupported");
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
         respType = PSPluginResponse.EXCEPTION;
         respMessage = "Failed to check oracle version for rhythmyx repository, "
            + "see the \"" + log + "\" located in " + RxUpgrade.getPreLogFileDir()
            + " for errors.";
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
         }
      }
           
      config.getLogStream().println(
         "Finished process() of the plugin Check Oracle Version...");
      return new PSPluginResponse(respType, respMessage);
   }
      
   /**
    * Oracle database name
    */
   private String m_oracleName = "oracle";
   
      
}
