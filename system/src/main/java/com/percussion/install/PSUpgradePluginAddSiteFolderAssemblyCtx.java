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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.util.PSPreparedStatement;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This plugin is used to add the site folder assembly context if it does not
 * exist.  If it does exist and the name includes whitespace, then the name is
 * set to the new name with spaces converted to underscores.
 */
public class PSUpgradePluginAddSiteFolderAssemblyCtx implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginAddSiteFolderAssemblyCtx() 
   {
   }
   
   /**
    * Implements process method of IPSUpgradePlugin.
    * 
    * @param module IPSUpgradeModule object. may not be <code>null<code>.
    * @param elemData data element of plugin.
    * 
    * @return plugin response, may be <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      PrintStream logger = module.getLogStream();
      logger.println("Running Add Site Folder Assembly Context plugin");

      Connection conn = null;
      try
      {
         conn = RxUpgrade.getJdbcConnection();
     
         // See if context exists with old name
         Long contextid = findContext(OLD_CONTEXT_NAME, logger, conn);
         if (contextid != null)
         {
            // Update old name
            updateSiteFolderAssemblyCtx(contextid, logger, conn);
         }
         else
         {
            // See if context exists with new name
            contextid = findContext(NEW_CONTEXT_NAME, logger, conn);
            if (contextid == null)
            {
               // Add it
               insertSiteFolderAssemblyCtx(logger, conn);
            }
         }
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
            catch (SQLException e)
            {
            }
            conn = null;
         }

         logger.println("leaving the process() of the Add Site Folder "
               + "Assembly Context plugin.");
      }

      return null;
   }

   /**
    * @return The name of the context table
    * @throws IOException if an io error occurs 
    * @throws FileNotFoundException if a file error occurs 
    */
   private String getContextTable() throws FileNotFoundException,
      IOException
   {
      return RxUpgrade.qualifyTableName(RXCONTEXT_TABLE);
   }
   
   /**
    * Finds the supplied context.
    * 
    * @param name the name of the context, assumed not <code>null</code>.
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @return the id of the context if it exists, <code>null</code> otherwise.
    * 
    * @throws Exception if an error occurs.
    */
   private Long findContext(String name, PrintStream logger, Connection conn)
      throws Exception
   {
      Long contextid = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      PSConnectionObject connObj = new PSConnectionObject();
       
      try
      {
         String sqlStmt = "SELECT CONTEXTID FROM " + getContextTable()
            + " WHERE CONTEXTNAME='" + name + "'";
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
         
         if (rs.next())
         {
            contextid = rs.getLong(1);
         }
      }
      finally
      {
         connObj.close();
      }
      
      return contextid;
   }
   
   /**
    * Inserts site folder assembly context.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void insertSiteFolderAssemblyCtx(PrintStream logger,
      Connection conn) throws Exception
   {
      String contextTable = getContextTable();
      int rowCount = 0;
                 
      logger.println("Inserting site folder assembly context");

      String insertStmt = "INSERT INTO " + contextTable
            + " VALUES(?, ?, ?, ?, NULL)";

      PreparedStatement stmt = null;
      try
      {
         Long contextid = new Long(PSGuidHelper.generateNextLong(
               PSTypeEnum.CONTEXT));
                                          
         stmt = PSPreparedStatement.getPreparedStatement(conn, insertStmt);
         stmt.setLong(1, contextid);
         stmt.setInt(2, new Integer(0));
         stmt.setString(3, NEW_CONTEXT_NAME);
         stmt.setString(4, "Site Folder Assembly Context");
     
         rowCount = stmt.executeUpdate();
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
            stmt = null;
         }
      }
      
      logger.println("Successfully inserted " + rowCount + " row(s) in "
            + contextTable);
   }
   
   /**
    * Updates site folder assembly context with correct name.
    * 
    * @param ctxId the context id.
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void updateSiteFolderAssemblyCtx(long ctxId, PrintStream logger,
      Connection conn) throws Exception
   {
      String contextTable = getContextTable();
      int rowCount = 0;
                 
      logger.println("Updating site folder assembly context");

      String updateStmt = "UPDATE " + contextTable + " SET CONTEXTNAME='"
            + NEW_CONTEXT_NAME + "' WHERE CONTEXTID=?";

      PreparedStatement stmt = null;
      try
      {
         stmt = PSPreparedStatement.getPreparedStatement(conn, updateStmt);
         stmt.setLong(1, ctxId);
       
         rowCount = stmt.executeUpdate();
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
            stmt = null;
         }
      }
      
      logger.println("Successfully updated " + rowCount + " row(s) in "
            + contextTable);
   }
   
   /**
    * The name of the context table.
    */
   private final static String RXCONTEXT_TABLE = "RXCONTEXT";
   
   /**
    * The name of the site folder assembly context (6.6.0 and higher).
    */
   private final static String NEW_CONTEXT_NAME =
      "Site_Folder_Assembly";
   
   /**
    * The name of the site folder assembly context (6.5.2 and lower).
    */
   private final static String OLD_CONTEXT_NAME =
      "Site Folder Assembly";
      
}
