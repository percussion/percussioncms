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

import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSPreparedStatement;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This plugin is used to migrate publishing data from 6.x to Mako (6.6). 
 */
public class PSUpgradePluginPublishing implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginPublishing() 
   {
   }
   
   /**
    * Implements process method of IPSUpgradePlugin.
    * 
    * @param module IPSUpgradeModule object. may not be <code>null<code>.
    * @param elemData data element of plugin.
    */
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      PrintStream logger = module.getLogStream();
      logger.println("Running Update Publishing plugin");

      Connection conn = null;
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         
         conn.setAutoCommit(false);

         // Perform assembly context migration and commit changes
         migrateAssemblyContext(logger, conn);
         conn.commit();         
         
         // Publishing data must be migrated in this order
         migratePubStatus(logger, conn);
         migratePubDocs(logger, conn);
         conn.commit();
         
         logger.println("Successfully finished upgrading publishing data.\n");
      }
      catch (Exception e)
      {
         try
         {
            if (conn != null)
               conn.rollback();
         }
         catch (SQLException se)
         {
         }

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

         logger.println("leaving the process() of the Update Publishing "
               + "plugin.");
      }

      return null;
   }

   /**
    * @return The name of the old publication status table
    * @throws IOException, FileNotFoundException if an error occurs 
    */
   private String getOldPubStatusTable() throws FileNotFoundException,
      IOException
   {
      return RxUpgrade.qualifyTableName(RXPUBSTATUS_TABLE);
   }
   
   /**
    * @return The name of the new publication status table
    * @throws IOException, FileNotFoundException if an error occurs 
    */
   private String getNewPubStatusTable() throws FileNotFoundException,
      IOException
   {
      return RxUpgrade.qualifyTableName(PSX_PUBSTATUS_TABLE);
   }
   
   /**
    * @return The name of the old publication docs table
    * @throws IOException, FileNotFoundException if an error occurs 
    */
   private String getOldPubDocsTable() throws FileNotFoundException,
      IOException
   {
      return RxUpgrade.qualifyTableName(RXPUBDOCS_TABLE);
   }
   
   /**
    * @return The name of the new publication docs table
    * @throws IOException, FileNotFoundException if an error occurs 
    */
   private String getNewPubDocsTable() throws FileNotFoundException,
      IOException
   {      
      return RxUpgrade.qualifyTableName(PSX_PUBDOCS_TABLE);
   }
   
   /**
    * @return The name of the content list table
    * @throws IOException, FileNotFoundException if an error occurs 
    */
   private String getContentListTable() throws FileNotFoundException,
      IOException
   {
      return RxUpgrade.qualifyTableName(RXCONTENTLIST_TABLE);
   }
   
   /**
    * @return The name of the content list association table
    * @throws IOException, FileNotFoundException if an error occurs 
    */
   private String getCLAssociationTable() throws FileNotFoundException,
      IOException
   {
      return RxUpgrade.qualifyTableName(RXEDITIONCLIST_TABLE);
   }
   
   /**
    * Database update utility method.
    * 
    * @param logger the logger used to log messages.  If <code>null</code>,
    *           messages will not be logged.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param sqlStmt the SQL statement for the updates, assumed not
    *           <code>null</code>.
    * @param bindValue the bind values for the above SQL statement. It may be
    *           <code>null</code> if there is no bind value. The type of the
    *           bind values must be either {@link Integer}, {@link Long},
    *           {@link String}, or {@link Timestamp}.
    * 
    * @return either (1) the row count for <code>INSERT</code>,
    *         <code>UPDATE</code>, or <code>DELETE</code> statements or (2)
    *         0 for SQL statements that return nothing
    * 
    * @throws Exception if an error occurs.
    */
   private int executeUpdate(PrintStream logger, Connection conn,
         String sqlStmt, Object[] bindValue) throws Exception
   {
      PreparedStatement stmt = null;
      try
      {
         if (logger != null)
         {
            logger.println("ExecuteUpdate SQL[" + ++ms_sqlCount + "]: "
                  + sqlStmt);
         }
         
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         if (bindValue != null)
         {
            for (int i=0; i < bindValue.length; i++)
            {
               if (bindValue[i] instanceof String)
                  stmt.setString(i+1, (String)bindValue[i]);
               else if (bindValue[i] instanceof Integer)
                  stmt.setInt(i+1, ((Integer)bindValue[i]).intValue());
               else if (bindValue[i] instanceof Long)
                  stmt.setLong(i+1, ((Long)bindValue[i]).longValue());
               else if (bindValue[i] instanceof Timestamp)
                  stmt.setTimestamp(i+1, (Timestamp)bindValue[i]);
               else
                  throw new IllegalArgumentException("bindValue[" + i + "] "
                        + "must be either String, Integer, Long, or Timestamp "
                        + "type.");
               
               if (logger != null)
               {
                  logger.println("              SQL bind value[" + i + "]: "
                        + bindValue[i]);
               }
            }
         }
         int rowCount = stmt.executeUpdate();
         
         if (logger != null)
            logger.println("Successful execute SQL[" + ms_sqlCount + "].");
         return rowCount;
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
   }

   /**
    * Database update utility method.  Calls
    * {@link #executeUpdate(PrintStream, Connection, String, Object[])} passing
    * the given {@link List} of bind values as an array.  
    * 
    * @param logger the logger used to log messages.  If <code>null</code>,
    *           messages will not be logged.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param bindValue the bind values for the above SQL statement, assumed
    *           not <code>null</code>.
    * @param sqlStmt the SQL statement for the updates, assumed not
    *           <code>null</code>.
    * 
    * @return either (1) the row count for <code>INSERT</code>,
    *         <code>UPDATE</code>, or <code>DELETE</code> statements or (2)
    *         0 for SQL statements that return nothing
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private int executeUpdate(PrintStream logger, Connection conn,
         List bindValue, String sqlStmt) throws Exception
   {
      return executeUpdate(logger, conn, sqlStmt, bindValue.toArray());        
   }
   
   /**
    * Convenience method that calls 
    * {@link #executeUpdate(null, Connection, List, String)}.
    */
   @SuppressWarnings("unchecked")
   private int executeUpdate(Connection conn, String sqlStmt,
         List bindValue) throws Exception
   {
      return executeUpdate(null, conn, bindValue, sqlStmt);        
   }
      
   /**
    * Convenience method that calls
    * {@link #executeUpdate(null, Connection, String, Object[])}
    */
   private int executeUpdate(Connection conn, String sqlStmt,
         Object[] bindValue) throws Exception
   {
      return executeUpdate(null, conn, sqlStmt, bindValue);        
   }
   
   /**
    * Convenience method that calls
    * {@link #executeUpdate(null, Connection, String, null)}.
    */
   private int executeUpdate(Connection conn, String sqlStmt) throws Exception
   {
      return executeUpdate(null, conn, sqlStmt, null);        
   }
   
   /**
    * Migrates all publishing status data from the old table
    * {@link #RXPUBSTATUS_TABLE} to the new one {@link #PSX_PUBSTATUS_TABLE}.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void migratePubStatus(PrintStream logger, Connection conn)
      throws Exception
   {
      String oldPubStatusTable = getOldPubStatusTable();
      String newPubStatusTable = getNewPubStatusTable();
      
      PreparedStatement stmt1 = null;
      PreparedStatement stmt2 = null;
      ResultSet rs1 = null;
      ResultSet rs2 = null;
      int rowCount = 0;
      PSConnectionObject connObj1 = new PSConnectionObject();
      PSConnectionObject connObj2 = new PSConnectionObject();
      
      try
      {
         logger.println("Migrating publication status data from " +
               oldPubStatusTable + " to " + newPubStatusTable);

         String sqlStmt = "SELECT DISTINCT PUBLICATIONID FROM "
            + oldPubStatusTable;
         stmt1 = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs1 = stmt1.executeQuery();
         connObj1.setStatement(stmt1);
         connObj1.setResultSet(rs1);
         
         while (rs1.next())
         {
            int id = rs1.getInt(1);
            sqlStmt = "SELECT STARTDATE, ENDDATE, OPSTATUS, INSERTED, "
               + "UPDATED, UNPUBLISHED, ERRORS, EDITIONID "
               + "FROM " + oldPubStatusTable
               + " WHERE PUBLICATIONID=" + id;
            stmt2 = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
            rs2 = stmt2.executeQuery();
            connObj2.setStatement(stmt2);
            connObj2.setResultSet(rs2);
            
            IPSPubStatus.EndingState endingStatus = 
               IPSPubStatus.EndingState.COMPLETED;
            int delivered = 0;
            int removed = 0;
            int failed = 0;
            Timestamp start = null;
            Timestamp end = null;
            int editionid = 0;
            while (rs2.next())
            {
               Timestamp s = rs2.getTimestamp(1);
               if (start == null || (s != null && s.before(start)))
                  start = s;
                              
               Timestamp e = rs2.getTimestamp(2);
               if (end == null || (e != null && e.after(end)))
                  end = e;
                           
               String opstatus = rs2.getString(3);
               int inserted = rs2.getInt(4);
               int updated = rs2.getInt(5);
               int unpublished = rs2.getInt(6);
               int errors = rs2.getInt(7);
               
               if (editionid == 0)
                  editionid = rs2.getInt(8);
             
               if (opstatus != null && 
                     !opstatus.toUpperCase().equals(STATUS_SUCCESS))
               {
                  endingStatus = IPSPubStatus.EndingState.COMPLETED_W_FAILURE;
               }
                  
               delivered += inserted + updated;
               removed += unpublished;
               failed += errors;
            }
               
            if (endingStatus.equals(IPSPubStatus.EndingState.COMPLETED) &&
                  failed > 0)
            {
               endingStatus = IPSPubStatus.EndingState.COMPLETED_W_FAILURE;
            }
                        
            String insertStmt = "INSERT INTO " + newPubStatusTable
                  + " VALUES(?, 0, ?, ?, ?, ?, ?, ?, ?, NULL)";
            Object[] bindValue = new Object[8];
            bindValue[0] = new Integer(id);
            bindValue[1] = start;
            bindValue[2] = end;
            bindValue[3] = new Integer(editionid);
            bindValue[4] = new Integer(endingStatus.ordinal());
            bindValue[5] = new Integer(delivered);
            bindValue[6] = new Integer(removed);
            bindValue[7] = new Integer(failed);
            
            rowCount += executeUpdate(conn, insertStmt, bindValue);
            
            connObj2.close();
         }
         
         logger.println("Successfully inserted " + rowCount + " row(s) in "
               + newPubStatusTable);
      }
      finally
      {
         connObj2.close();
         connObj1.close();
      }
   }
   
   /**
    * Migrates all publishing docs data from the old table
    * {@link #RXPUBDOCS_TABLE} to the new one {@link #PSX_PUBDOCS_TABLE}.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void migratePubDocs(PrintStream logger, Connection conn)
      throws Exception
   {
      String oldPubDocsTable = getOldPubDocsTable();
      String newPubDocsTable = getNewPubDocsTable();
      
      PreparedStatement stmt1 = null;
      PreparedStatement stmt2 = null;
      ResultSet rs1 = null;
      ResultSet rs2 = null;
      int rowCount = 0;
      Map<Integer, PubStatusValue> pubStatusMap =
         new HashMap<>();
      PSConnectionObject connObj1 = new PSConnectionObject();
      PSConnectionObject connObj2 = new PSConnectionObject();
      
      try
      {
         logger.println("Migrating publication docs from " +
               oldPubDocsTable + " to " + newPubDocsTable);

         String countStmt = "SELECT COUNT(*) FROM " + oldPubDocsTable;
         stmt1 = PSPreparedStatement.getPreparedStatement(conn, countStmt);
         rs1 = stmt1.executeQuery();
         connObj1.setStatement(stmt1);
         connObj1.setResultSet(rs1);
         
         rs1.next();
         int rows = rs1.getInt(1);
         connObj1.close();
         
         String sqlStmt = "SELECT PUBSTATUSID, CONTENTID, VARIANTID, "
            + "PUBSTATUS, PUBDATE, REVISIONID, PUBOP, PUBLOCATION, "
            + "ELAPSETIME "
            + "FROM " + oldPubDocsTable;
         stmt1 = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs1 = stmt1.executeQuery();
         connObj1.setStatement(stmt1);
         connObj1.setResultSet(rs1);
                  
         sqlStmt = "SELECT PUBSTATUSID, PUBLICATIONID, SITEID "
            + "FROM " + getOldPubStatusTable();
         stmt2 = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs2 = stmt2.executeQuery();
         connObj2.setStatement(stmt2);
         connObj2.setResultSet(rs2);

         while (rs2.next())
         {
            pubStatusMap.put(rs2.getInt(1), new PubStatusValue(rs2.getInt(2),
                  rs2.getInt(3)));
         }
         
         int i = 0;
         int p = -1;
         while (rs1.next())
         {
            double percent = ((double) i++/(double) rows) * 100;
            int pint = (int) percent;
            if (pint != p)
            {
               System.out.println("Updating " + PSX_PUBDOCS_TABLE + "(" + pint
                     + "%)");
               p = pint;
            }
            
            Integer pubstatusid = rs1.getInt(1);
            PubStatusValue pubstatusval = pubStatusMap.get(pubstatusid);
            if (pubstatusval != null)
            {
               Long referenceid = new Long(PSGuidHelper.generateNextLong(
                     PSTypeEnum.PUB_REFERENCE_ID));
                              
               int contentid = rs1.getInt(2);
               int variantid = rs1.getInt(3);
               String pubstatus = rs1.getString(4);
               
               int status = IPSAssemblyResult.Status.SUCCESS.ordinal();
               if (pubstatus != null && 
                     !pubstatus.toUpperCase().equals(STATUS_SUCCESS))
               {
                  status = IPSAssemblyResult.Status.FAILURE.ordinal();
               }
               
               Timestamp pubdate = rs1.getTimestamp(5);
               int revisionid = rs1.getInt(6);
               String pubop = rs1.getString(7);
               
               int operation = IPSSiteItem.Operation.PUBLISH.ordinal();
               if (pubop != null && !pubop.toUpperCase().equals(STATUS_PUBLISH))
                  operation = IPSSiteItem.Operation.UNPUBLISH.ordinal();
               
               List bindValues = new ArrayList();
               bindValues.add(referenceid);
               bindValues.add(new Integer(pubstatusval.getPublicationId()));
               bindValues.add(new Integer(contentid));
               bindValues.add(new Integer(revisionid));
               bindValues.add(new Integer(variantid));
               
               String paramstr;
               String publocation = rs1.getString(8);
               if (publocation == null)
               {
                  paramstr = "NULL";
               }
               else
               {
                  paramstr = "?";
                  bindValues.add(publocation);
               }
               
               int elapsetime = rs1.getInt(9);
                            
               bindValues.add(pubdate);
               bindValues.add(new Integer(operation));
               bindValues.add(new Integer(elapsetime));
               bindValues.add(new Integer(status));
                          
               String insertStmt = "INSERT INTO " + newPubDocsTable
                  + " VALUES(?, 0, ?, ?, ?, NULL, ?, " + paramstr
                  + ", ?, ?, NULL, ?, ?, NULL, NULL, NULL, NULL, NULL, NULL)";
                                       
               rowCount += executeUpdate(conn, insertStmt, bindValues);
            }
            else
            {
               logger.println("Could not find publication id for publication "
                     + "status " + pubstatusid);
            }
         }
         
         logger.println("Successfully inserted " + rowCount + " row(s) in "
               + newPubDocsTable);
      }
      finally
      {
         connObj1.close();
         connObj2.close();
      }
   }
   
   /**
    * Migrates all assembly context data from {@link #RXCONTENTLIST_TABLE} to
    * {@link #RXEDITIONCLIST_TABLE}.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void migrateAssemblyContext(PrintStream logger, Connection conn)
      throws Exception
   {
      String contentListTable = getContentListTable();
      String clAssociationTable = getCLAssociationTable();
      
      PreparedStatement stmt = null;
      ResultSet rs = null;
      int rowCount = 0;
      Map<Integer, String> cListUrls = new HashMap<>();
      PSConnectionObject connObj = new PSConnectionObject();
            
      try
      {
         logger.println("Migrating assembly context data from " +
               contentListTable + " to " + clAssociationTable);

         String sqlStmt = "SELECT CONTENTLISTID, URL FROM " + contentListTable;
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
         
         while (rs.next())
         {
            int contentlistid = rs.getInt(1);
            String url = rs.getString(2);
                        
            if (url == null)
            {
               logger.println("Url does not exist for content list id "
                     + contentlistid);
               continue;
            }
             
            Map<String, Object> params = PSBaseHttpUtils.parseQueryParamsString(
                  url);
            String param = "sys_assembly_context";
            if (!params.containsKey(param))
            {
               logger.println("Assembly context parameter does not exist in "
                     + "url '" + url + "' for content list id "
                     + contentlistid);
               continue;
            }
            
            String value = (String) params.get(param);
          
            int ac = -1;
            try
            {
               ac = Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
               logger.println("Invalid assembly context parameter value '"
                     + value + "' in url '" + url + "' for content list "
                     + contentlistid);
               continue;
            }
       
            String insertStmt = "UPDATE " + clAssociationTable
                  + " SET ASSEMBLY_CONTEXT=" + ac
                  + " WHERE CONTENTLISTID=" + contentlistid;
                      
            rowCount += executeUpdate(conn, insertStmt);
            
            String newUrl = PSBaseHttpUtils.removeQueryParam(url, param);
            cListUrls.put(Integer.valueOf(contentlistid), newUrl);
         }
         
         logger.println("Successfully updated " + rowCount + " row(s) in "
               + clAssociationTable);
         
         connObj.close();
               
         rowCount = 0;
         Iterator<Integer> iter = cListUrls.keySet().iterator();
         while (iter.hasNext())
         {
            Integer cListId = iter.next();
            String url = cListUrls.get(cListId);
            String updateStmt = "UPDATE " + contentListTable
                  + " SET URL='" + url + "'"
                  + " WHERE CONTENTLISTID=" + cListId.intValue();
            rowCount += executeUpdate(conn, updateStmt);
         }
         
         logger.println("Successfully updated " + rowCount + " row(s) in "
               + contentListTable);
      }
      finally
      {
         connObj.close();
      }
   }
   
   /**
    * Encapsulates data from the
    * {@link PSUpgradePluginPublishing#RXPUBSTATUS_TABLE} table, including
    * publication id and site id.
    */
   class PubStatusValue
   {
      /**
       * Ctor.
       * 
       * @param publicationid The publication id.
       * @param siteid The site id.
       */
      public PubStatusValue(int publicationid, int siteid)
      {
         m_publicationid = publicationid;
         m_siteid = siteid;
      }
      
      /**
       * The publication id.
       * 
       * @return The publication id.
       */
      public int getPublicationId()
      {
         return m_publicationid;
      }
      
      /**
       * The site id.
       * 
       * @return The site id.
       */
      public int getSiteId()
      {
         return m_siteid;
      }
      
      /**
       * See {@link #getPublicationId()}
       */
      private int m_publicationid;
      
      /**
       * See {@link #getSiteId()}.
       */
      private int m_siteid;
   }
   
   /**
    * Counter for tracking the number of SQL statement as part of logging info.
    */
   private int ms_sqlCount = 0;
   
   /**
    * The name of the old publication status table.
    */
   private final static String RXPUBSTATUS_TABLE = "RXPUBSTATUS";
   
   /**
    * The name of the new publication status table.
    */
   private final static String PSX_PUBSTATUS_TABLE = "PSX_PUBLICATION_STATUS";

   /**
    * The name of the old publication docs table.
    */
   private final static String RXPUBDOCS_TABLE = "RXPUBDOCS";
   
   /**
    * The name of the new publication docs table.
    */
   private final static String PSX_PUBDOCS_TABLE = "PSX_PUBLICATION_DOC";
   
   /**
    * The name of the content list table.
    */
   private final static String RXCONTENTLIST_TABLE = "RXCONTENTLIST";
   
   /**
    * The name of the content list association table.
    */
   private final static String RXEDITIONCLIST_TABLE = "RXEDITIONCLIST";
   
   /**
    * Constant for the success status value.
    */
   private final static String STATUS_SUCCESS = "SUCCESS";
   
   /**
    * Constant for the publish status value.
    */
   private final static String STATUS_PUBLISH = "PUBLISH";
      
}
