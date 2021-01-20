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
package com.percussion.rxfix.dbfixes;

import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;

/**
 * @author peterfrontiero
 * 
 * Removes invalid associations in which one of the references no longer exists.
 */
public class PSFixDanglingAssociations extends PSFixDBBase
 implements IPSFix
{
   /**
    * The list of single join associations to check.  Any invalid or dangling
    * associations will be removed if found.  The first two elements for each
    * entry are the names of the columns representing the association.  The next
    * element is the table which holds this association.  The next elements
    * include the name of the column in the external table to be queried
    * for each of the association columns followed by the name of the external
    * table.  
    * 
    * For example, [c1, c2, t1, c3, t2, c4, t3] describes an association
    * between columns c1 and c2 in table t1 where c3 is the associated column
    * for c1 in table t2 and c4 is the associated column for c2 in t3.
    * 
    * Note the use of comments to prevent the code formatter from
    * wrapping the entire block.
    */   
   private static final String ms_singleJoinAssociationsInfo[] =
   {
      "VARIANTID", "SLOTID", "RXVARIANTSLOTTYPE", "TEMPLATE_ID", "PSX_TEMPLATE", "SLOTID", "RXSLOTTYPE", //
      "SITEID", "VARIANTID", "PSX_VARIANT_SITE", "SITEID", "RXSITES", "TEMPLATE_ID", "PSX_TEMPLATE" //
   };
   
   /**
    * Similar to {@link #ms_singleJoinAssociationsInfo} but for double join
    * associations.
    * 
    * For example, [c1, c2, c3, t1, c4, t2, c5, t3, c6, t4] describes an association
    * between columns c1, c2, and c3 in table t1 where c4 is the associated column
    * for c1 in table t2, c5 is the associated column for c2 in t3, and c6 is
    * the associated column for c3 in t4.
    */
   private static final String ms_doubleJoinAssociationsInfo[] =
   {
      "SLOTID", "CONTENTTYPEID", "VARIANTID", "RXSLOTCONTENT",  "SLOTID", "RXSLOTTYPE", "CONTENTTYPEID", "CONTENTTYPES", "TEMPLATE_ID", "PSX_TEMPLATE", //
      "EDITIONCLISTID", "EDITIONID", "CONTENTLISTID", "RXEDITIONCLIST", "EDITIONCLISTID", "RXEDITIONCLIST", "EDITIONID", "RXEDITION", "CONTENTLISTID", "RXCONTENTLIST" //
   };
      
   /**
    * Finds a single association in which one of the references does not exist.
    */
   private static final PSStringTemplate
   ms_findSingleAssocationsQuery = 
      new PSStringTemplate(
            "SELECT {column1},{column2} FROM {schema}.{table1} "
            + "WHERE {column1} NOT IN (SELECT {column3} "
            + "FROM {schema}.{table2}) OR "
            + "{column2} NOT IN (SELECT {column4} "
            + "FROM {schema}.{table3})");
   
   /**
    * Finds a double association in which one of the references does not exist.
    */   
   private static final PSStringTemplate
   ms_findDoubleAssociationsQuery =
      new PSStringTemplate(
            "SELECT {column1},{column2},{column3} FROM {schema}.{table1} "
            + "WHERE {column1} NOT IN (SELECT {column4} "
            + "FROM {schema}.{table2}) OR "
            + "{column2} NOT IN (SELECT {column5} "
            + "FROM {schema}.{table3}) OR "
            + "{column3} NOT IN (SELECT {column6} "
            + "FROM {schema}.{table4})");
   
   /**
    * Deletes specified single association.
    */
   private static final PSStringTemplate ms_deleteSingleAssociation =
      new PSStringTemplate("DELETE FROM {schema}.{table1} "
            + "WHERE {column1}=? AND {column2}=?"); 
   
   /**
    * Deletes specified double association.
    */
   private static final PSStringTemplate ms_deleteDoubleAssociation =
      new PSStringTemplate("DELETE FROM {schema}.{table1} "
            + "WHERE {column1}=? AND {column2}=? AND {column3}=?"); 
   
   /**
    * Ctor
    * @throws SQLException 
    * @throws NamingException 
    */
   public PSFixDanglingAssociations() throws NamingException,
      SQLException
   {
      super();
   }

   @Override
   public void fix(boolean preview)
         throws Exception
   {
      super.fix(preview);
      Connection c = PSConnectionHelper.getDbConnection();
                  
      try
      {
         fixSingleJoinAssociations(c, preview);
         fixDoubleJoinAssociations(c, preview);
         fixTransitionRoleAssociations(c, preview);
      }
      catch (Exception e)
      {
         logFailure(null, "The following error occurred: "
               +  e.getLocalizedMessage());
      }
      finally
      {
         c.close();
      }
   }
   
   @Override
   public String getOperation()
   {
      return "Fix dangling associations";
   }
   
   /**
    * Fixes single join associations if any are found in
    * {@link #ms_singleJoinAssociationsInfo}.
    * 
    * @param c the database connection object, assumed not <code>null</code>.
    * @param preview if <code>true</code> then a preview of the fix will be
    * performed, otherwise the actual fix will be executed.
    * 
    * @throws SQLException
    * @throws PSStringTemplateException
    */
   private void fixSingleJoinAssociations(Connection c, boolean preview)
   throws SQLException, PSStringTemplateException
   {
      String table;
      String qualTable;
      
      for (int i = 0; i < ms_singleJoinAssociationsInfo.length; i += 7)
      {
         table = ms_singleJoinAssociationsInfo[i + 2];
         qualTable = PSSqlHelper.qualifyTableName(
               table,
               (String) m_defDict.get("db"),
               (String) m_defDict.get("schema"),
               (String) m_defDict.get("driver")
               );
         m_defDict.put("column1", ms_singleJoinAssociationsInfo[i]);
         m_defDict.put("column2", ms_singleJoinAssociationsInfo[i + 1]);
         m_defDict.put("table1", table);
         m_defDict.put("column3", ms_singleJoinAssociationsInfo[i + 3]);
         m_defDict.put("table2", ms_singleJoinAssociationsInfo[i + 4]);
         m_defDict.put("column4", ms_singleJoinAssociationsInfo[i + 5]);
         m_defDict.put("table3", ms_singleJoinAssociationsInfo[i + 6]);
         
         PreparedStatement st =
            PSPreparedStatement.getPreparedStatement(
               c,
               ms_findSingleAssocationsQuery.expand(m_defDict));

         Collection<String> associations = new ArrayList<String>();
         ResultSet rs = st.executeQuery();
         
         while (rs.next())
         {
            int column1 = rs.getInt(1);
            int column2 = rs.getInt(2);
            
            associations.add(column1 + "," + column2);
         }

         if (associations.size() > 0)
         {
            if (preview)
            {
               Iterator<String> iter = associations.iterator();
               while (iter.hasNext())
               {
                  String association = iter.next();
                  logPreview(null, "Would remove dangling association ["
                        + association + "] from " + qualTable);
               }
            }
            else
            {
               Iterator<String> iter = associations.iterator();
               PreparedStatement deleteAssociation =
                  PSPreparedStatement.getPreparedStatement(
                        c,
                        ms_deleteSingleAssociation.expand(m_defDict));
               String logStmt = "";
               while (iter.hasNext())
               {
                  if (logStmt.trim().length() > 0)
                     logStmt += ", ";
                  
                  String association = iter.next();
                  String[] columnArray = association.split(",");
                  Integer col1 = new Integer(columnArray[0]);
                  Integer col2 = new Integer(columnArray[1]);
                  
                  deleteAssociation.setInt(1, col1.intValue());
                  deleteAssociation.setInt(2, col2.intValue());
                  deleteAssociation.execute();
                  
                  logStmt += "[" + association + "]"; 
               }
               logInfo(null, "Removed the following dangling associations from "
                     + qualTable + ": " + logStmt);
            }
         }
         
         rs.close();
         st.close();
      }  
   }
   
   /**
    * Fixes double join associations if any are found in
    * {@link #ms_doubleJoinAssociationsInfo}.
    * 
    * @param c the database connection object, assumed not <code>null</code>.
    * @param preview if <code>true</code> then a preview of the fix will be
    * performed, otherwise the actual fix will be executed.
    * 
    * @throws SQLException
    * @throws PSStringTemplateException
    */
   private void fixDoubleJoinAssociations(Connection c, boolean preview)
   throws SQLException, PSStringTemplateException
   {
      String table;
      String qualTable;
      
      for (int i = 0; i < ms_doubleJoinAssociationsInfo.length; i += 10)
      {
         table = ms_doubleJoinAssociationsInfo[i + 3];
         qualTable = PSSqlHelper.qualifyTableName(
               table,
               (String) m_defDict.get("db"),
               (String) m_defDict.get("schema"),
               (String) m_defDict.get("driver")
               );
         m_defDict.put("column1", ms_doubleJoinAssociationsInfo[i]);
         m_defDict.put("column2", ms_doubleJoinAssociationsInfo[i + 1]);
         m_defDict.put("column3", ms_doubleJoinAssociationsInfo[i + 2]);
         m_defDict.put("table1", table);
         m_defDict.put("column4", ms_doubleJoinAssociationsInfo[i + 4]);
         m_defDict.put("table2", ms_doubleJoinAssociationsInfo[i + 5]);
         m_defDict.put("column5", ms_doubleJoinAssociationsInfo[i + 6]);
         m_defDict.put("table3", ms_doubleJoinAssociationsInfo[i + 7]);
         m_defDict.put("column6", ms_doubleJoinAssociationsInfo[i + 8]);
         m_defDict.put("table4", ms_doubleJoinAssociationsInfo[i + 9]);
         
         PreparedStatement st =
            PSPreparedStatement.getPreparedStatement(
               c,
               ms_findDoubleAssociationsQuery.expand(m_defDict));

         Collection<String> associations = new ArrayList<String>();
         ResultSet rs = st.executeQuery();
         
         while (rs.next())
         {
            int column1 = rs.getInt(1);
            int column2 = rs.getInt(2);
            int column3 = rs.getInt(3);
            
            associations.add(column1 + "," + column2 + "," + column3);
         }

         if (associations.size() > 0)
         {
            if (preview)
            {
               Iterator<String> iter = associations.iterator();
               while (iter.hasNext())
               {
                  String association = iter.next();
                  logPreview(null, "Would remove dangling association ["
                        + association + "] from " + qualTable);
               }
            }
            else
            {
               Iterator<String> iter = associations.iterator();
               PreparedStatement deleteAssociation =
                  PSPreparedStatement.getPreparedStatement(
                        c,
                        ms_deleteDoubleAssociation.expand(m_defDict));
               String logStmt = "";
               while (iter.hasNext())
               {
                  if (logStmt.trim().length() > 0)
                     logStmt += ", ";
                  
                  String association = iter.next();
                  String[] columnArray = association.split(",");
                  Integer col1 = new Integer(columnArray[0]);
                  Integer col2 = new Integer(columnArray[1]);
                  Integer col3 = new Integer(columnArray[2]);
                  
                  deleteAssociation.setInt(1, col1.intValue());
                  deleteAssociation.setInt(2, col2.intValue());
                  deleteAssociation.setInt(3, col3.intValue());
                  deleteAssociation.execute();
                  
                  logStmt += "[" + association + "]"; 
               }
               logInfo(null, "Removed the following dangling associations from "
                     + qualTable + ": " + logStmt);
            }
         }
         
         rs.close();
         st.close();
      }  
   }
   
   /**
    * Fixes workflow transition role associations by removing associations for
    * which the role does not exist.  These are associations from the
    * TRANSITIONROLES table in which the TRANSITIONROLEID has no corresponding
    * entry in either the STATEROLES or ROLES table for the associated
    * workflow.
    * 
    * @param c the database connection object, assumed not <code>null</code>.
    * @param preview if <code>true</code> then a preview of the fix will be
    * performed, otherwise the actual fix will be executed.
    * 
    * @throws SQLException
    */
   private void fixTransitionRoleAssociations(Connection c, boolean preview)
      throws SQLException
   {
      String db = (String) m_defDict.get("db");
      String schema = (String) m_defDict.get("schema");
      String driver = (String) m_defDict.get("driver");

      String trRolesTable = PSSqlHelper.qualifyTableName(
            "TRANSITIONROLES", db, schema, driver);
      String stRolesTable = PSSqlHelper.qualifyTableName(
            "STATEROLES", db, schema, driver);
      String rolesTable = PSSqlHelper.qualifyTableName(
            "ROLES", db, schema, driver);

      Set<String> associations = new HashSet<String>();
      associations.addAll(findMissingTransitionRoles(c, stRolesTable));
      associations.addAll(findMissingTransitionRoles(c, rolesTable));

      if (associations.size() > 0)
      {
         Iterator<String> iter = associations.iterator();
         if (preview)
         {
            while (iter.hasNext())
            {
               String association = iter.next();
               logPreview(null, "Would remove dangling association ["
                     + association + "] from " + trRolesTable);
            }
         }
         else
         {  
            String deleteStmt = "DELETE FROM " + trRolesTable
                  + " WHERE TRANSITIONID=? AND WORKFLOWAPPID=? AND"
                  + " TRANSITIONROLEID=?";
            PreparedStatement deleteAssociation =
               PSPreparedStatement.getPreparedStatement(c, deleteStmt);

            String logStmt = "";
            while (iter.hasNext())
            {
               if (logStmt.trim().length() > 0)
                  logStmt += ", ";

               String association = iter.next();
               String[] columnArray = association.split(",");
               Integer col1 = new Integer(columnArray[0]);
               Integer col2 = new Integer(columnArray[1]);
               Integer col3 = new Integer(columnArray[2]);

               deleteAssociation.setInt(1, col1.intValue());
               deleteAssociation.setInt(2, col2.intValue());
               deleteAssociation.setInt(3, col3.intValue());
               deleteAssociation.execute();

               logStmt += "[" + association + "]"; 
            }
            logInfo(null, "Removed the following dangling associations from "
                  + trRolesTable + ": " + logStmt);
         }
      }
   }
   
   /**
    * Finds transition role associations for which the role does not exist in
    * the specified roles table.
    * 
    * @param c the database connection object, assumed not <code>null</code>.
    * @param table the roles table, assumed not <code>null</code>.
    * 
    * @return a set of transition role associations which are no longer valid.
    * Each association is a comma-separated string of three id values.
    * 
    * @throws SQLException
    */
   private Set<String> findMissingTransitionRoles(Connection c, String table)
      throws SQLException
   {
      PreparedStatement st = null;
      ResultSet rs = null;
      
      try
      {
         String trRolesTable = PSSqlHelper.qualifyTableName(
               "TRANSITIONROLES", 
               (String) m_defDict.get("db"),
               (String) m_defDict.get("schema"),
               (String) m_defDict.get("driver")
               );
         String queryStmt = "SELECT * FROM " + trRolesTable 
               + " WHERE TRANSITIONROLEID NOT IN" + " (SELECT ROLEID FROM "
               + table + " WHERE WORKFLOWAPPID=" + trRolesTable
               + ".WORKFLOWAPPID)";
         st = PSPreparedStatement.getPreparedStatement(c, queryStmt);

         Set<String> associations = new HashSet<String>();
         rs = st.executeQuery();
         while (rs.next())
         {
            int column1 = rs.getInt(1);
            int column2 = rs.getInt(2);
            int column3 = rs.getInt(3);

            associations.add(column1 + "," + column2 + "," + column3);
         }
         
         return associations;
      }
      finally
      {
         if (rs != null)
         {
            rs.close();
         }

         if (st != null)
         {
            st.close();
         }
      }
   }
}
