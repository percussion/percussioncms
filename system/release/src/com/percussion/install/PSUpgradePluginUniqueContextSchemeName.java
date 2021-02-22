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

import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.types.PSPair;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

/**
 * This plug-in upgrades the Location Scheme table to make sure all 
 * Location Scheme names are unique within each Context.
 *
 * @author YuBingChen
 */
public class PSUpgradePluginUniqueContextSchemeName implements IPSUpgradePlugin
{
   /*
    * //see base class method for details
    */
   public PSPluginResponse process(IPSUpgradeModule module, @SuppressWarnings("unused")
   Element elemData)
   {
      String PROCESS_NAME = "upgrading Location Scheme unique name";
      PrintStream logger = module.getLogStream();
      logger.println("Running "+ PROCESS_NAME + "...");

      Connection conn = null;
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         
         conn.setAutoCommit(false);

         Map<Long, List<PSPair<Long, String>>> ctxMap = getContextSchemeMap(conn);
         List<PSPair<Long, String>> dedupNames = getDedupSchemeNames(ctxMap);
         updateDedupNames(logger, dedupNames, conn);

         conn.commit();         
         
         logger.println("Successfully finished " + PROCESS_NAME);
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

         logger.println("Leaving the process() of " + PROCESS_NAME);
      }

      return null;
   }
   
   /**
    * Dedup the Location Scheme names within each Context.
    * 
    * @param ctxMap the map that uses Context ID maps to a list of 
    * Location Scheme ID / name pairs.
    * 
    * @return a list of deduped Location Scheme ID/name pairs. 
    * Never <code>null</code>, may be empty if the names are already unique.
    */
   private List<PSPair<Long, String>> getDedupSchemeNames(Map<Long, List<PSPair<Long, String>>> ctxMap)
   {
      List<PSPair<Long, String>> dedupNames = new ArrayList<>();
      for (Long k : ctxMap.keySet())
      {
         dedupNames.addAll(getDedupPairs(ctxMap.get(k)));
      }
      
      return dedupNames;
   }
   
   /**
    * Dedup the specified Location Scheme names.
    * 
    * @param src the names in question, assumed not <code>null</code>, but 
    * may be empty.
    * 
    * @return the deduped names, never <code>null</code>, may be empty.
    */
   private List<PSPair<Long, String>> getDedupPairs(List<PSPair<Long, String>> src)
   {
      Set<String> names = new HashSet<>();
      List<PSPair<Long, String>> result = new ArrayList<>();
      for (PSPair<Long, String> p : src)
      {
         boolean addToResult = false;
         PSPair<Long, String> pcopy = new PSPair<>(p.getFirst(), p
               .getSecond());
         // update Location Schemes with blank name
         if (StringUtils.isBlank(pcopy.getSecond()))
         {
            addToResult = true;
            pcopy.setSecond("Scheme");
         }
         if (names.contains(pcopy.getSecond()))
         {
            String unique = getUniqueName(pcopy.getSecond(), names);
            names.add(unique);
            result.add(new PSPair<>(pcopy.getFirst(), unique));
         }
         else
         {
            names.add(pcopy.getSecond());
            if (addToResult)
               result.add(pcopy);
         }
      }
      return result;
   }

   /**
    * Gets a unique name from the specified name base and name set.
    * 
    * @param base the name base to create a new name from, assumed not
    * <code>null</code> or empty.
    * @param names the name set to check name's uniqueness. Assumed not
    * <code>null</code> or empty.
    * 
    * @return the unique name, never <code>null</code> or empty.
    */
   private String getUniqueName(String base, Set<String> names)
   {
      for (int i=0; i < Integer.MAX_VALUE; i++)
      {
         String name = base + "_" + i;
         if (!names.contains(name))
            return name;
      }
      
      throw new RuntimeException(
            "Cannot find unique name for Location Scheme: " + base);
   }
   
   /**
    * Updates the repository for the specified ID/name pairs.
    * 
    * @param logger the logger, assumed not <code>null</code>
    * @param pairs the to be updated ID/name pairs, assumed not 
    * <code>null</code>, but may be empty. 
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void updateDedupNames(PrintStream logger,
         List<PSPair<Long, String>> pairs, Connection conn) throws Exception
   {
      PSConnectionObject connObj = new PSConnectionObject();
      
      try
      {
         int rowCount = 0;
         for (PSPair<Long, String> p : pairs)
         {
            String sqlStmt = "UPDATE " + getSchemeTable() + " SET SCHEMENAME = '" + p.getSecond() + "' WHERE SCHEMEID = " + p.getFirst();
            PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
            connObj.setStatement(stmt);
            rowCount += stmt.executeUpdate();
            connObj.close();
            
            logger.println(sqlStmt);
         }         
      }
      finally
      {
         connObj.close();
      }
   }
   
   /**
    * Retrieves the Location Scheme ID/name pairs for all Contexts.
    * 
    * @param conn the connection, assumed not <code>null</code>.
    *  
    * @return a map that maps Context ID to a list of Scheme ID/name pairs.
    * 
    * @throws Exception if an error occurs.
    */
   private Map<Long, List<PSPair<Long, String>>> getContextSchemeMap(
         Connection conn) throws Exception
   {
      Map<Long, List<PSPair<Long, String>>> result = new HashMap<>();
      PSConnectionObject connObj = new PSConnectionObject();
      
      try
      {
         String sqlStmt = "SELECT CONTEXTID, SCHEMEID, SCHEMENAME FROM "
               + getSchemeTable();
         PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(
               conn, sqlStmt);
         ResultSet rs = stmt.executeQuery();
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
         
         while (rs.next())
         {
            long ctxId = rs.getLong(1);
            long schemeId = rs.getLong(2);
            String schemeName = rs.getString(3);
            List<PSPair<Long, String>> schemeList = result.get(ctxId);
            if (schemeList == null)
            {
               schemeList = new ArrayList<>();
               result.put(ctxId, schemeList);
            }
            schemeList.add(new PSPair<>(schemeId, schemeName));
         }
      }
      finally
      {
         connObj.close();
      }

      return result;
   }

   /**
    * Get the fully qualified Location Scheme table.
    * 
    * @return the Location Scheme table name, never <code>null</code> or empty.
    * 
    * @throws Exception if an error occurs.
    */
   private String getSchemeTable() throws Exception
   {
      return RxUpgrade.qualifyTableName("RXLOCATIONSCHEME");
   }
   
}
