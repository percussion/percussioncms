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

// java

import com.percussion.tablefactory.IPSJdbcTableDataHandler;
import com.percussion.tablefactory.IPSTableFactoryErrors;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcExecutionPlan;
import com.percussion.tablefactory.PSJdbcExecutionStep;
import com.percussion.tablefactory.PSJdbcFilterContainer;
import com.percussion.tablefactory.PSJdbcResultSetIteratorStep;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcStatementFactory;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

/**
 * Data handler object which populates the "TRANSITIONROLES" table from the
 * contents of "TRANSITIONS" and "ROLES" table.
 *
 * The logic used for inserting data into "TRANSITIONROLES" table is as follows:
 *
 * Catalog "TRANSITIONS" table :
 * SELECT TRANSITIONID, WORKFLOWAPPID, TRANSITIONROLES FROM TRANSITIONS WHERE
 * ((TRANSITIONROLES != null) && (TRANSITIONROLES != '*ALL*') &&
 * (TRANSITIONROLES != '*Specified*'))
 *
 * Store the value of TRANSITIONID, WORKFLOWAPPID and TRANSITIONROLES for all
 * such rows into variables "x", "y" and "z" respectively.
 *
 * Query the "ROLES" table for ROLEID and store it in variable "r"
 * SELECT ROLEID FROM ROLES WHERE ROLENAME = :z AND WORKFLOWAPPID = :y
 *
 * If a valid ROLEID is obtained from "ROLES" table then:
 * INSERT INTO TRANSITIONROLES VALUES (:x, :y, :r)
 *
 * Update the value of "TRANSITIONROLES" column in "TRANSITIONS" table
 * UPDATE TRANSITIONS SET TRANSITIONROLES = '*Specified*' WHERE
 * TRANSITIONID = :x AND WORKFLOWAPPID = :y
 *
 */
public class PSJdbcTransitionRoles implements IPSJdbcTableDataHandler
{
   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>srcTableSchema</code> is <code>null</code>
    */
   public void init(PSJdbcDbmsDef dbmsDef, Connection conn,
      PSJdbcTableSchema srcTableSchema, PSJdbcTableSchema destTableSchema)
       throws PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      if (srcTableSchema == null)
         throw new IllegalArgumentException("srcTableSchema may not be null");

      m_dbmsDef = dbmsDef;
      m_transRolesTblSchema = srcTableSchema;

      m_transTblFilter = new PSJdbcFilterContainer();
      m_transTblFilter.doAND(new PSJdbcSelectFilter(COL_TRANSITIONROLES,
         PSJdbcSelectFilter.IS_NOT_NULL, "", Types.VARCHAR));
      m_transTblFilter.doAND(new PSJdbcSelectFilter(COL_TRANSITIONROLES,
         PSJdbcSelectFilter.NOT_EQUALS, COL_VALUE_ALL, Types.VARCHAR));
      m_transTblFilter.doAND(new PSJdbcSelectFilter(COL_TRANSITIONROLES,
         PSJdbcSelectFilter.NOT_EQUALS, COL_VALUE_SPECIFIED, Types.VARCHAR));
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public PSJdbcRowData execute(Connection conn, PSJdbcRowData nullRow)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      PSJdbcDataTypeMap dataTypeMap = null;
      try
      {
         dataTypeMap = new PSJdbcDataTypeMap(m_dbmsDef.getBackEndDB(),
            m_dbmsDef.getDriver(), null);
      }
      catch (Exception e)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.LOAD_DEFAULT_DATATYPE_MAP, e.toString(), e);
      }

      // get the schema of "TRANSITIONS" table
      PSJdbcTableSchema transTblSchema = PSJdbcTableFactory.catalogTable(conn,
         m_dbmsDef, dataTypeMap, TBL_TRANSITIONS, false);

      if (transTblSchema == null)
      {
         // "TRANSITIONS" table does not exist, so no need to proceed further
         return null;
      }

      // get the schema of "ROLES" table
      PSJdbcTableSchema rolesTblSchema = PSJdbcTableFactory.catalogTable(conn,
         m_dbmsDef, dataTypeMap, TBL_ROLES, false);

      // catalog the "TRANSITIONS" table
      // SELECT TRANSITIONID, WORKFLOWAPPID, TRANSITIONROLES FROM TRANSITIONS
      // WHERE ((TRANSITIONROLES != null) && (TRANSITIONROLES != '*ALL*') &&
      // (TRANSITIONROLES != '*Specified*'))
      String []transTblColumns = new String[]
         {
            COL_TRANSITIONID, COL_WORKFLOWAPPID, COL_TRANSITIONROLES
         };
      PSJdbcResultSetIteratorStep transTblStep =
         PSJdbcStatementFactory.getResultSetIteratorStatement(
            m_dbmsDef, transTblSchema, transTblColumns, m_transTblFilter,
            PSJdbcRowData.ACTION_UPDATE);

      PSJdbcColumnData transColData = new PSJdbcColumnData(
         COL_TRANSITIONROLES, COL_VALUE_SPECIFIED);
      PSJdbcExecutionPlan plan = new PSJdbcExecutionPlan();
      String strTransitionId = null;
      String strWorkflowAppId = null;
      String strTransitionRoles = null;

      try
      {
         transTblStep.execute(conn);
         PSJdbcRowData row = transTblStep.next();
         while (row != null)
         {
            strTransitionId = getRequiredColumnValue(row, TBL_TRANSITIONS,
               COL_TRANSITIONID);
            strWorkflowAppId = getRequiredColumnValue(row, TBL_TRANSITIONS,
               COL_WORKFLOWAPPID);
            strTransitionRoles = getRequiredColumnValue(row, TBL_TRANSITIONS,
               COL_TRANSITIONROLES);

            // update the value of "TRANSITIONROLES" column to '*Specified*'
            row.removeColumn(COL_TRANSITIONROLES);
            row.addColumn(transColData);

            // add the step for updating the "TRANSITIONROLES" column
            // in "TRANSITIONS" table
            PSJdbcExecutionStep updateStep =
               PSJdbcStatementFactory.getUpdateStatement(
                  m_dbmsDef, transTblSchema, row);
            plan.addStep(updateStep);

            if (rolesTblSchema != null)
            {
               // catalog the "ROLES" table
               // "SELECT ROLEID FROM ROLES WHERE ROLENAME = " + strTransitionRoles
               // + " AND WORKFLOWAPPID = " + strWorkflowAppId
               String []rolesTblColumns = new String[]{COL_ROLEID};

               PSJdbcFilterContainer rolesTblFilter = new PSJdbcFilterContainer();
               rolesTblFilter.doAND(new PSJdbcSelectFilter(COL_WORKFLOWAPPID,
                  PSJdbcSelectFilter.EQUALS, strWorkflowAppId, Types.INTEGER));
               rolesTblFilter.doAND(new PSJdbcSelectFilter(COL_ROLENAME,
                  PSJdbcSelectFilter.EQUALS, strTransitionRoles, Types.VARCHAR));

               PSJdbcTableData roleTblData = PSJdbcTableFactory.catalogTableData(
                  conn, m_dbmsDef, rolesTblSchema, rolesTblColumns, rolesTblFilter,
                  PSJdbcRowData.ACTION_INSERT);
               if (roleTblData != null)
               {
                  Iterator it = roleTblData.getRows();
                  if (it.hasNext())
                  {
                     PSJdbcRowData insertRow = (PSJdbcRowData)it.next();
                     String roleId = getRequiredColumnValue(insertRow, TBL_ROLES,
                        COL_ROLEID);

                     // create "TRANSITIONROLEID" column
                     PSJdbcColumnData transRolesColData = new PSJdbcColumnData(
                        COL_TRANSITIONROLEID, roleId);

                     row.removeColumn(COL_TRANSITIONROLES);
                     row.addColumn(transRolesColData);

                     PSJdbcExecutionStep insertStep =
                        PSJdbcStatementFactory.getInsertStatement(
                           m_dbmsDef, m_transRolesTblSchema, row);
                     plan.addStep(insertStep);
                  }
               }
            }
            row = transTblStep.next();
         }
         transTblStep.close();
         transTblStep = null;
         plan.execute(conn);
      }
      catch (SQLException e)
      {
         Object args[] = {TBL_TRANSITIONS,
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SQL_CATALOG_DATA, args, e);
      }
      finally
      {
         if (transTblStep != null)
            transTblStep.close();
      }
      return null;
   }

   /**
    * Returns the value of the specifed column from the row data.
    *
    * @param row the row data from which to obtain the column value, assumed
    * not <code>null</code>
    * @param tableName the name of the table containing the column
    * <code>colName</code>, assumed not <code>null</code> and non-empty
    * @param colName the name of the column whose value is to be returned,
    * assumed not <code>null</code> and non-empty
    *
    * @return the value of column <code>colName</code> obtain from the row
    * data <code>row</code>, never <code>null</code>
    *
    * @throws PSJdbcTableFactoryException if the column <code>colName</code>
    * does not exist in the row data <code>row</code> or if the column value
    * is <code>null</code>
    */
   private String getRequiredColumnValue(PSJdbcRowData row,
      String tableName, String colName)
      throws PSJdbcTableFactoryException
   {
      PSJdbcColumnData colData = row.getColumn(colName);
      if (colData == null)
      {
         Object args[] = {tableName, colName};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.COLUMN_NOT_FOUND, args);
      }
      String colValue = colData.getValue();
      if (colValue == null)
      {
         Object args[] = {tableName, "Column (" + colName + ") has null value"};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.CHECK_EXISTING_DATA, args);
      }
      return colValue;
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public void close(Connection conn)  throws PSJdbcTableFactoryException
   {
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(IPSJdbcTableDataHandler.NODE_NAME))
      {
         Object[] args = {IPSJdbcTableDataHandler.NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
   }

   /**
    * @see com.percussion.tablefactory.IPSJdbcTableDataHandler
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(IPSJdbcTableDataHandler.NODE_NAME);
      String className = this.getClass().getName();
      root.setAttribute(IPSJdbcTableDataHandler.CLASS_ATTR, className);
      return root;
   }

   /**
    * provides the database/schema information for the table, initialized in the
    * constructor, never <code>null</code> after initialization
    */
   private PSJdbcDbmsDef m_dbmsDef = null;

   /**
    * Schema of the "TRANSITIONROLES" table, initialized in the
    * <code>init()</code> method, never <code>null</code> after initialization
    */
   private PSJdbcTableSchema m_transRolesTblSchema = null;

   /**
    * Filter for select statement used when catalogging TRANSITIONS table,
    * initialized in the <code>init()</code> method, never <code>null</code>
    * after initialization. It equals:
    * ((TRANSITIONROLES != null) && (TRANSITIONROLES != '*ALL*') &&
    * (TRANSITIONROLES != '*Specified*'))
    */
   PSJdbcFilterContainer m_transTblFilter = null;

   // Constants for table names, column names, and column values
   private static final String TBL_TRANSITIONS = "TRANSITIONS";
   private static final String TBL_ROLES = "ROLES";
   private static final String TBL_TRANSITIONROLES = "TRANSITIONROLES";
   private static final String COL_TRANSITIONID = "TRANSITIONID";
   private static final String COL_WORKFLOWAPPID = "WORKFLOWAPPID";
   private static final String COL_TRANSITIONROLES = "TRANSITIONROLES";
   private static final String COL_TRANSITIONROLEID = "TRANSITIONROLEID";
   private static final String COL_ROLEID = "ROLEID";
   private static final String COL_ROLENAME = "ROLENAME";
   private static final String COL_VALUE_ALL = "*ALL*";
   private static final String COL_VALUE_SPECIFIED = "*Specified*";

}


