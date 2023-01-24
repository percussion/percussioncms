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

//java

import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.i18n.rxlt.PSLocaleHandler;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;



/**
 * This plugin has been written to fix up the NEXTNUMBER table for locales.
 * Fix-up will involve the following steps:
 * 
 * 1. Check largest locale id and ensure that the next NEXTNUMBER locale id is
 *    larger than this value.  This includes adding an entry in the NEXTNUMBER
 *    table for locale id if one does not exist.
 * 
 */

public class PSUpgradePluginNextNumberFixup implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginNextNumberFixup()
   {
   }

   /**
    * Implements the process function of IPSUpgradePlugin.  Performs the tasks
    * described above.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {

      m_config = config;
      log("Performing NEXTNUMBER fix-up");
      PSPluginResponse response = null;
            
      try
      {
         updateLocaleNextNumber();
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
           
      log("Finished process() of the plugin NEXTNUMBER fix-up...");
      return response;
   }
  
   /**
    * Helper function to locate largest locale id from locales table
    * 
    * @param conn the connection to use, assumed not <code>null</code>
    * @param dbmsDef the repository props definition object, assumed not <code>null</code>
    * @return the largest id found in the locales table 
    */
   private static int findLargestLocaleId(Connection conn, PSJdbcDbmsDef dbmsDef)
    throws Exception
   {
      String qualTableName = 
         PSSqlHelper.qualifyTableName(PSLocaleHandler.LOCALE_TABLE, dbmsDef.getDataBase(), 
               dbmsDef.getSchema(), dbmsDef.getDriver());
      
      String queryStmt = "SELECT MAX(" + qualTableName + ".LOCALEID) " + 
                         "FROM " + qualTableName;
      
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(queryStmt);
      int id = 1;
      
      if (rs.next())
         id = rs.getInt(1);
      
      return id;
   }
   
   /**
    * Helper function to update the NEXTNUMBER table for localeid if necessary.
    * It will check the largest localeid and ensure that the nextnumber for
    * localeid is greater than this value.
    */
   private static void updateLocaleNextNumber()
    throws Exception
   {
      Connection conn = RxUpgrade.getJdbcConnection();
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
      
      String qualTableName = 
         PSSqlHelper.qualifyTableName(
               PSContentEditorSystemDef.NEXT_ID_TABLE_ALIAS,
               dbmsDef.getDataBase(), 
               dbmsDef.getSchema(), dbmsDef.getDriver());
      
      String queryStmt = "SELECT " + qualTableName + ".NEXTNR " + 
                         "FROM " + qualTableName + " WHERE " +
                         qualTableName + ".KEYNAME='localeid'";
      
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(queryStmt);
      
      int largestLocaleId = findLargestLocaleId(conn, dbmsDef);
      int nextLocaleId = 1;
      
      if (rs.next())
      {
         nextLocaleId = rs.getInt(1);
      
         if (largestLocaleId >= nextLocaleId)
         {
            nextLocaleId = largestLocaleId + 1;
         
            String updateStmt = "UPDATE " + qualTableName + " SET " +
                                qualTableName + ".NEXTNR=" + nextLocaleId +
                                " WHERE " + qualTableName + ".KEYNAME='localeid'";
         
            stmt.executeUpdate(updateStmt);
         
            log("Updated " + PSContentEditorSystemDef.NEXT_ID_TABLE_ALIAS + " table " +
                  "localeid = " + nextLocaleId);
         }
         else
            log(PSContentEditorSystemDef.NEXT_ID_TABLE_ALIAS + " table update not required");
      }
      else
      {
         nextLocaleId = largestLocaleId + 1;
         
         String insertStmt = "INSERT INTO " + qualTableName + 
                             " (KEYNAME,NEXTNR) " +
                             "VALUES ('localeid'," + nextLocaleId + ")";
         
         stmt.execute(insertStmt);
         
         log("Updated " + PSContentEditorSystemDef.NEXT_ID_TABLE_ALIAS + " table " +
               "localeid = " + nextLocaleId);
      }
   }
     
   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private static void log(String msg)
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
   
   private static IPSUpgradeModule m_config;
}
