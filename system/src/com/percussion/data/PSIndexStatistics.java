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

package com.percussion.data;

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.jdbc.PSConnectionDetail;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/* JDBC sucks. You can only load data from a row once, and in the
* appropriate left-right order. As such, we must store the row data
* by reading it all into our array which will act as the row
* buffer. Furthermore, we need to peek ahead so we can tell whether
* or not to move ahead. As such we're also creating a next row buffer.
*/
public class PSIndexStatistics {
   private PSIndexStatistics(
      PSBackEndTable table, String indexName, short indexType,
      boolean isUnique, int cardinality)
   {
      super();
      m_table = table;
      m_indexName = indexName;
      m_indexType = indexType;
      m_isUnique = isUnique;
      m_cardinality = cardinality;
      m_columns = new ArrayList();
   }

   /**
    * Is this a unique index?
    */
   public boolean isUnique()
   {
      return m_isUnique;
   }

   /**
    * Get the name of the index
    */
   public String getIndexName()
   {
      return m_indexName;
   }

   /**
    * Get the names of the columns this index sorts on. The first column
    * in the returned array is the first sorted column, the second is the
    * second, etc.
    */
   public String[] getSortedColumns()
   {
      if (m_columns == null)
         return new String[0];

      String[] ret = new String[m_columns.size()];
      m_columns.toArray(ret);
      return ret;
   }

   /**
    * Get the DatabaseMetaData.tableIndexXXX type of this index.
    */
   public short getIndexType()
   {
      return m_indexType;
   }

   /**
    * Get the cardinality of this index. If type is tableIndexStatistic
    * this is the number of rows in the table, otherwise it is the number
    * of unique values in this index.
    */
   public int getCardinality()
   {
      return m_cardinality;
   }

   /**
    * Get the back-end table this index is on.
    */
   public PSBackEndTable getTable()
   {
      return m_table;
   }

   /**
    * Indicates whether or not a table is actually a view.
    * @param table the PSBackEndTable in question.
    * @param md the database meta data object for this database.
    * @return <code>true</code> if the table is a view, else <code>false</code>.
    * @throws SQLException when the database encounters a sql equery error.
    */
   public static boolean isTableView(PSBackEndTable table, DatabaseMetaData md)
      throws SQLException
   {

      if(null != table)
      {
         PSMetaDataCache.loadConnectionDetail(table);
         ResultSet rs = md.getTables(table.getConnectionDetail().getDatabase(),
                                       table.getConnectionDetail().getOrigin(),
                                       table.getTable(),
                                       new String[] {"VIEW"});
         String current = null;
         while(rs.next())
         {
            current = rs.getString(3); // Position 3 is the tablename column
            if(null != current && current.equalsIgnoreCase(table.getTable()))
            {
               return true;
            }
         }
      }
      // Not a view
      return false;


   }


   /**
    * Get information about the indices associated with a table.
    *
    * @param   table the table to check, may not be <code>null</code>.
    *
    * @param   md The database meta data
    *
    * @return  an array containing the index statistics, may return
    * <code>null</code>.
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public static PSIndexStatistics[] getStatistics(PSBackEndTable table,
      DatabaseMetaData md) throws SQLException
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");
      
      ArrayList statsArray = new ArrayList();
      ResultSet rs = null;

      PSMetaDataCache.loadConnectionDetail(table);
      PSConnectionDetail connDetail = table.getConnectionDetail(); 
      String driverName    = connDetail.getDriver();
      String databaseName  = connDetail.getDatabase();
      String schemaName    = connDetail.getOrigin();
      String tableName     = table.getTable();

      try
      {
         // If the table is actually a view then return null, there are
         // no indexes on views and calling getIndexInfo in some databases
         // will cause a SQLException to be thrown.
         if(isTableView(table, md))
            return null;

         if (databaseName != null && databaseName.trim().length() == 0)
         {
            databaseName = null;
         }
         // The "getIndexInfo" method may take a long time for LARGE tables
         // especially for Oracle. log the elapse time to help the end user to 
         // figure out the possible cause of the performance issues
         PSStopwatch watch = new PSStopwatch();
         watch.start();
         
         rs = md.getIndexInfo(
            databaseName, schemaName, tableName, false, false);

         watch.stop();
         ms_logger.debug(watch.toString()
               + " for getting the indices and statistics for table '"
               + tableName + "'");
         
         
         PSIndexStatistics stats = null;
         boolean isUnique;
         String indexName, colName, colSorting;
         String lastIndexName = "nosuchindexname!!!";
         short indexType, lastIndexType = 0;
         int cardinality;

         while (rs.next()) {
            // THESE MUST BE READ IN THE CORRECT SEQUENCE !!!
            isUnique = !rs.getBoolean(4);    // col 4 = NON_UNIQUE
            indexName = rs.getString(6);     // col 6 = INDEX_NAME
            if (indexName == null)
               indexName = "";
            indexType = rs.getShort(7);      // col 7 = TYPE
            colName = rs.getString(9);       // col 9 = COLUMN_NAME
            colSorting = rs.getString(10);   // col 10 = ASC_OR_DESC
            cardinality = rs.getInt(11);     // col 11 = CARDINALITY

            if (!indexName.equals(lastIndexName) ||
               (indexType != lastIndexType))
            {
               stats = new PSIndexStatistics(
                  table, indexName, indexType, isUnique, cardinality);
               statsArray.add(stats);

               lastIndexName = indexName;
               lastIndexType = indexType;
            }

            if (colName != null)
               stats.m_columns.add(colName);
         }
      } finally {
         try {
            if (rs != null)
               rs.close();
         } catch (Exception e) { /* done anyway, ignore it */ }
      }

      PSIndexStatistics[] ret = new PSIndexStatistics[statsArray.size()];
      statsArray.toArray(ret);
      return ret;
   }
   
   private boolean            m_isUnique;
   private String             m_indexName;
   private ArrayList          m_columns;
   private short              m_indexType;
   private int                m_cardinality;
   private PSBackEndTable     m_table;
   
   /**
    * the log4j logger used for this class 
    */
   static private Logger ms_logger = LogManager.getLogger("GetTableIndexInfo");
}
