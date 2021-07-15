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

package com.percussion.rxfix.dbfixes;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.rxfix.IPSFix;
import com.percussion.rxfix.PSFixResult.Status;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

/**
 * RX fix used to remove old data from content-type tables.
 * Loops through all content types and populates a tables map
 * with all related tables to each content type (including child
 * tables).  Finds and removes CONTENTIDs which do not have an
 * entry in CONTENTSTATUS. It also removes the same CONTENTIDs
 * from PSX_OBJECTRELATIONSHIP and CONTENTSTATUSHISTORY as well
 * if they exist.
 * 
 * @author chriswright
 *
 */
public class PSFixStaleDataForContentTypes extends PSFixDBBase implements IPSFix
{
   /**
    * Select count from CONTENTSTATUSHISTORY where content ID is from
    * orphaned content type entry.
    */
   PSStringTemplate ms_selectContentStatusHistory = new PSStringTemplate(
         "SELECT COUNT(*) FROM {schema}.CONTENTSTATUSHISTORY WHERE CONTENTID = ?");
   
   /**
    * Select count from PSX_OBJECTRELATIONSHIP where owner and dependent
    *  IDs are from orphaned content type entry.
    */
   PSStringTemplate ms_selectObjectRelationship = new PSStringTemplate(
         "SELECT COUNT(*) FROM {schema}." + IPSConstants.PSX_RELATIONSHIPS 
         + " WHERE DEPENDENT_ID = ? OR OWNER_ID = ?");
   
   /**
    * Delete a specified relationship row from CONTENTSTATUSHISTORY
    */
   PSStringTemplate ms_deleteContentHistory = new PSStringTemplate(
         "DELETE FROM {schema}.CONTENTSTATUSHISTORY WHERE CONTENTID = ?");
   
   /**
    * Delete a specified relationship row from PSX_OBJECTRELATIONSHIP
    */
   PSStringTemplate ms_deleteObjectRelationship = new PSStringTemplate(
         "DELETE FROM {schema}." + IPSConstants.PSX_RELATIONSHIPS 
         + " WHERE DEPENDENT_ID = ? OR OWNER_ID = ?");

   public PSFixStaleDataForContentTypes() throws NamingException, SQLException
   {
      super();
   }

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);
      Connection c = PSConnectionHelper.getDbConnection();
      try
      {
         List<QueryObject> queryObjs = getQueryObjects();
         int contentStatusCount = 0;
         int objRelCount = 0;
         for (QueryObject obj : queryObjs)
         {
            // for each content type, loop through all related tables
            for (String tableName : obj.getSelectQueries().keySet())
            {
               PreparedStatement selectQuery =
                     PSPreparedStatement.getPreparedStatement(
                           c,
                           obj.getSelectQueries().get(tableName).expand(m_defDict));

               Collection<Integer> contentIds = new ArrayList<Integer>();
               ResultSet rs = selectQuery.executeQuery();
               // content IDs related to table that do not exist in content status
               while (rs.next())
               {
                  int cid = rs.getInt(1);
                  contentIds.add(new Integer(cid));
               }
               
               if (contentIds.size() == 0)
               {
                  continue;
               }
               else if (preview)
               {
                  for (Integer cid : contentIds)
                  {
                     // get count of content status history IDs that would be deleted
                     PreparedStatement selectContentHistory =
                           PSPreparedStatement.getPreparedStatement(
                                 c, 
                                 ms_selectContentStatusHistory.expand(m_defDict));
                     
                     selectContentHistory.setInt(1, cid);
                     
                     rs = selectContentHistory.executeQuery();
                     
                     while (rs.next()) {
                        contentStatusCount += rs.getInt(1);
                     }
                     
                     selectContentHistory.close();
                     
                     // get count of object relationship IDs that would be deleted
                     PreparedStatement selectObjRelHistory =
                           PSPreparedStatement.getPreparedStatement(
                                 c, 
                                 ms_selectObjectRelationship.expand(m_defDict));
                     
                     selectObjRelHistory.setInt(1, cid);
                     selectObjRelHistory.setInt(2, cid);
                     
                     rs = selectObjRelHistory.executeQuery();
                     
                     while (rs.next()) {
                        objRelCount += rs.getInt(1);
                     }
                     
                     selectObjRelHistory.close();
                  }
                  
                  logPreview(idsToString(contentIds), "Would be remved for content type: " + obj.getCtName()
                        + " with table name: " + tableName);
                  
                  selectQuery.close();
               }
               else
               {
                  // delete statement for content type
                  PreparedStatement deleteQuery =
                        PSPreparedStatement.getPreparedStatement(
                              c, 
                              obj.getDeleteQueries().get(tableName).expand(m_defDict));
                  // delete statement for content status history
                  PreparedStatement deleteContentHistory =
                        PSPreparedStatement.getPreparedStatement(
                              c,
                              ms_deleteContentHistory.expand(m_defDict));
                  // delete statement for object relationship
                  PreparedStatement deleteObjRel =
                        PSPreparedStatement.getPreparedStatement(
                              c,
                              ms_deleteObjectRelationship.expand(m_defDict));

                  for (Integer cid : contentIds)
                  {
                     deleteQuery.setInt(1, cid.intValue());
                     deleteContentHistory.setInt(1, cid.intValue()); // CONTENTID
                     deleteObjRel.setInt(1, cid.intValue()); // DEPENDENT_ID
                     deleteObjRel.setInt(2, cid.intValue()); // OWNER_ID

                     deleteQuery.execute();
                     deleteContentHistory.execute();
                     deleteObjRel.execute();
                  }
                  
                  logInfo(idsToString(contentIds), "Have been removed from: " + tableName);
                  
                  deleteQuery.close();
                  deleteContentHistory.close();
                  deleteObjRel.close();
               } // end else
               rs.close();
            } // end inner for loop
         } // end outer for loop
         
         log(preview ? Status.PREVIEW : Status.INFO, String.valueOf(contentStatusCount), "Total items to be deleted from content status history table.");
         log(preview ? Status.PREVIEW : Status.INFO, String.valueOf(objRelCount), "Total items to be removed from object relationship table.");
      } // end try
      catch (SQLException sqlException)
      {
         logFailure(null, sqlException.getMessage());
      }
      finally
      {
         c.close();
      }
   }

   @Override
   public String getOperation()
   {
      return "Remove stale data from all content types and contentstatushistory tables.";
   }

   /**
    * Populates QueryObjects with tables and queries for each content type
    * to select and delete unused data from each content type.  Includes child
    * tables.
    * @return List of query objects which contains all tables associated
    * with each content type and related select/delete statements.
    */
   private List<QueryObject> getQueryObjects()
   {
      List<PSContentTypeSummary> summaries = PSContentTypeHelper.loadContentTypeSummaries(null);
      List<QueryObject> objects = new ArrayList<QueryObject>();

      for (PSContentTypeSummary summ : summaries)
      {
         try
         {
            QueryObject obj = new QueryObject();
            List<PSBackEndTable> tables = getBackEndTablesForType(summ.getName());
            Map<String, PSStringTemplate> staleDataQueries = new HashMap<String, PSStringTemplate>();
            Map<String, PSStringTemplate> deleteDataQueries = new HashMap<String, PSStringTemplate>();

            obj.setCtName(summ.getName());

            for (PSBackEndTable table : tables)
            {
               PSStringTemplate findStaleDataQuery = new PSStringTemplate("SELECT CONTENTID FROM {schema}."
                     + table.getTable() + " WHERE CONTENTID NOT IN (SELECT CONTENTID" + " FROM {schema}."
                     + IPSConstants.CONTENT_STATUS_TABLE + ")");
               PSStringTemplate deleteRelationship = new PSStringTemplate("DELETE FROM {schema}." + table.getTable()
                     + " WHERE CONTENTID = ?");

               staleDataQueries.put(table.getTable(), findStaleDataQuery);
               deleteDataQueries.put(table.getTable(), deleteRelationship);
            }

            obj.setSelectQueries(staleDataQueries);
            obj.setDeleteQueries(deleteDataQueries);

            objects.add(obj);
         }
         catch (PSInvalidContentTypeException e)
         {
            logFailure(null, "Error finding content type definition with name: " + summ.getName());
         }
      }
      return objects;
   }

   /**
    * Calls the PSItemDefManager to get the full list of tables
    * associated with each content type passed in.
    * @param typeName the name of the content type to get tables for.
    * @return List of backend tables related to content type.
    * @throws PSInvalidContentTypeException thrown when content type does not exist.
    */
   private List<PSBackEndTable> getBackEndTablesForType(String typeName) throws PSInvalidContentTypeException
   {
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
      List<PSBackEndTable> tables = null;
      PSItemDefinition itemDef = itemDefMgr.getItemDef(typeName, -1);
      tables = itemDef.getTypeTables();
      return tables;
   }
}

/**
 * Used to maintain relationship between table name and related select/delete
 * queries.
 * 
 * @author chriswright
 *
 */
class QueryObject
{
   /**
    * Contains the name of the content type.
    */
   private String ctName;

   /**
    * Contains the select queries and table name from which to remove stale data.
    */
   private Map<String, PSStringTemplate> selectQueries;

   /**
    * Contains the delete queries and table name from which to remove stale data.
    */
   private Map<String, PSStringTemplate> deleteQueries;

   protected String getCtName()
   {
      return ctName;
   }

   protected void setCtName(String ctName)
   {
      this.ctName = ctName;
   }

   protected Map<String, PSStringTemplate> getSelectQueries()
   {
      return selectQueries;
   }

   protected void setSelectQueries(Map<String, PSStringTemplate> selectQueries)
   {
      this.selectQueries = selectQueries;
   }

   protected Map<String, PSStringTemplate> getDeleteQueries()
   {
      return deleteQueries;
   }

   protected void setDeleteQueries(Map<String, PSStringTemplate> deleteQueries)
   {
      this.deleteQueries = deleteQueries;
   }

}
