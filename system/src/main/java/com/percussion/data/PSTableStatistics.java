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
package com.percussion.data;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/* JDBC sucks. You can only load data from a row once, and in the
 * appropriate left-right order. As such, we must store the row data
 * by reading it all into our array which will act as the row
 * buffer. Furthermore, we need to peek ahead so we can tell whether
 * or not to move ahead. As such we're also creating a next row buffer.
 */
class PSTableStatistics
{
   /**
    * Creates the statistices with the estimated rows. The created object has
    * an empty index statistics.
    * 
    * @param login the login info; it may not be <code>null</code>.
    * @param table the table info; it may not be <code>null</code>.
    * @param estimatedRows the estimated rows for the specified table.
    */
   public PSTableStatistics(PSBackEndLogin login, PSBackEndTable table,
      int estimatedRows)
   {
      super();

      if (login == null)
         throw new IllegalArgumentException("login may not be null.");
      if (table == null)
         throw new IllegalArgumentException("table may not be null.");
      
      m_cardinality = estimatedRows;

      m_table = table;
      m_login = login;

      m_indexStatistics = new PSIndexStatistics[0];
      m_uniqueCounts = new HashMap(m_indexStatistics.length);
      m_isEstimated = true;      
   }
   
   /**
    * Create the statistics required by the optimizer for the specified
    * table.
    *
    * @param login The login info, may be <code>null</code> to indicate the
     * default connection.
    * @param table The table info, may not be <code>null</code>.
    * @param dmd   The meta data from which we cull the statistics
    */
   public PSTableStatistics(
      PSBackEndLogin login, PSBackEndTable table, DatabaseMetaData dmd)
      throws SQLException
   {
      super();

        if (table == null)
         throw new IllegalArgumentException("table may not be null");

        PSMetaDataCache.loadConnectionDetail(table);
           
      m_cardinality = -1;

      m_table = table;
      m_login = login;

      m_indexStatistics = PSIndexStatistics.getStatistics(table, dmd);
      if (m_indexStatistics == null)
         m_indexStatistics = new PSIndexStatistics[0];
      
      m_uniqueCounts = new java.util.HashMap(m_indexStatistics.length);

      for (int i = 0; i < m_indexStatistics.length; i++)
      {
         PSIndexStatistics curIdxStat = m_indexStatistics[i];
         if (DatabaseMetaData.tableIndexStatistic
            == curIdxStat.getIndexType())
         {
            m_cardinality = curIdxStat.getCardinality();
         }
         else // this is some kind of index, add its cardinality to a hash
         {
            // for purposes of uniqueness testing, the order of the
            // columns in the index is not important, so we sort them for
            // consistent hashing
            String[] idxCols = curIdxStat.getSortedColumns();
            int cardinality = curIdxStat.getCardinality();
            m_uniqueCounts.put(
               getColumnListString(idxCols), new Integer(cardinality));
         }
      }
      // if the index stats did not give us the table cardinality,
      // then query it ourselves using select count
      if (m_cardinality < 0)
      {
         StringBuilder qryBuf = new StringBuilder(30);
         qryBuf.append("SELECT COUNT(*) FROM ");
         String tableName = m_table.getTable();
         
         // It is much faster to get the total count from IPSConstants.PSX_RELATIONSHIPS 
         // table than from the RXRELATEDCONTENT view.
         // SELECT COUNT(*) FROM IPSConstants.PSX_RELATIONSHIPS WHERE CONFIG='Active Assembly'
         if (tableName.equalsIgnoreCase("RXRELATEDCONTENT"))
         {
            qryBuf.append(PSSqlHelper.qualifyTableName(IPSConstants.PSX_RELATIONSHIPS));
            qryBuf.append(" r, ");
            qryBuf.append(PSSqlHelper.qualifyTableName(IPSConstants.PSX_RELATIONSHIPCONFIGNAME));
            qryBuf.append(" n ");
            qryBuf.append("WHERE r.CONFIG_ID = n.CONFIG_ID ");
            qryBuf.append(" AND n.CONFIG_NAME = '");
            qryBuf.append(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
            qryBuf.append("'");
         }
         else
         {
            qryBuf.append(PSSqlHelper.qualifyTableName(tableName));
         }

         // issue a select count against the table
         Connection conn = null;
         Statement stmt = null;
         ResultSet rs = null;
         try
         {
            try
            {
               conn = PSConnectionHelper.getDbConnection(m_login);
            }
            catch (NamingException e)
            {
               throw new SQLException(e.getLocalizedMessage());
            }

            stmt = PSSQLStatement.getStatement(conn);

            // run the select count query
            rs = stmt.executeQuery(qryBuf.toString());
            if (rs != null)
            {
               // get the count from the results
               if (rs.next())
               {
                  m_cardinality = rs.getInt(1);
               }
            }
         }
         finally
         {
            try { if (rs != null) rs.close(); }
            catch (Exception e) { /* ignore, we're done anyway */ }

            try { if (stmt != null) stmt.close(); }
            catch (Exception e) { /* ignore, we're done anyway */ }

            try { if (conn != null) conn.close(); }
            catch (Exception e) { /* ignore, we're done anyway */ }
         }
      } // end if cardinality not supplied by meta data
   }

   /**
    * Get the index statistics for this table.
    *
    * @return            an array of index statistics
    */
   public PSIndexStatistics[] getIndexStatistics()
   {
      return m_indexStatistics;
   }

   /**
    * Get the back-end table associated with this object.
    *
    * @return            the back-end table object
    */
   public PSBackEndTable getTable()
   {
      return m_table;
   }

   /**
    * Get the cardinality (row count) for this table, if available.
    * Note that this cardinality might be out of date with respect
    * to the actual table, depending on when this statistics object
    * was produced. This value might be an estimate.
    *
    * @return The cardinality of the table, or -1 if the cardinality
    * is not available.
    */
   public int getCardinality()
   {
      return m_cardinality;
   }

   /** 
    * @author chadloder
    * 
    * Gets the number of distinct rows for the given column.
    * 
    * @param   col 
    * 
    * @return int The number of distinct rows for the given
    * column, or -1 if it could not be calculated.
    * 
    * @see #getDistinctRows(String[])
    *
    * @since 1.2 1999/4/29
    *
    */
   public int getDistinctRows(String col) throws SQLException
   {
      return getDistinctRows(new String[] { col });
   }

   /**
    * Create the list of columns, comma separated. It appears we can't
    * compare String[] objects in the Map, so we will use the comma
    * separated list to do so.
    *
    * @param      cols      the list of columns
    *
    * @return               a string containing Col1, Col2, ..., ColN
    */
   public String getColumnListString(String[] cols)
   {
      // avg col name length (on traditional systems) is around 10, so ....
      StringBuilder columnBuf = new StringBuilder(cols.length * 10);
      columnBuf.append(cols[0]);
      for (int i = 1; i < cols.length; i++)
      {
         columnBuf.append(",");
         columnBuf.append(cols[i]);
      }

      return columnBuf.toString();
   }

   /** 
    * @author chadloder
    * 
    * Get the number of distinct rows in the table, distinguishing
    * on all columns whose names are listed in <CODE>cols</CODE>.
    *
    * Note that calling this method could result in a query being sent
    * to the server.
    * 
    * @param   cols   An array of names of columns in the table. No column
    * should be specified more than once. Every column must be the
    * name of a column in this table. If <CODE>null</CODE> is passed,
    * then all columns will distinguish (equivalent to SELECT COUNT DISTINCT)
    * 
    * @return the estimated row if this is an estimated statistics; otherwise
    *    return the number of distinct rows in the table for the given
    *   columns, or -1 if an error occurred.
    * 
    * @see #getDistinctRows(String)
    * @throws SQLException if error occurred during executing SQL query.
    */
   public int getDistinctRows(String[] cols) throws SQLException
   {
      if (m_isEstimated)
         return m_cardinality;
      
      String columnList = getColumnListString(cols);
      Integer cardinality = (Integer)m_uniqueCounts.get(columnList);
      if (cardinality == null || cardinality.intValue() < 0)
      {
         // issue a select count distinct against the cols
         Connection conn = null;
         ResultSet rs = null;
         try
         {
            // build the select count statement, which should look like:
            // SELECT COUNT(DISTINCT colA, colB, colC) FROM orig.table
            StringBuilder queryStringBuf
               = new StringBuilder(columnList.length() + 50);

            queryStringBuf.append("SELECT COUNT( DISTINCT ");
            queryStringBuf.append(columnList);
            queryStringBuf.append(") FROM ");

            // if an origin was supplied, then fully specify the table
            String origin = m_table.getConnectionDetail().getOrigin();
            if (origin != null && origin.length() > 0)
               queryStringBuf.append(origin + ".");

            queryStringBuf.append(m_table.getTable());

            try
            {
               conn = PSConnectionHelper.getDbConnection(m_login);
            }
            catch (NamingException e)
            {
               throw new SQLException(e.getLocalizedMessage());
            }

            Statement stmt = PSSQLStatement.getStatement(conn);
            
            // run the select count query
            rs = stmt.executeQuery(queryStringBuf.toString());
            int card = -1;

            // get the count from the results
            if (rs.next())
            {
               card = rs.getInt(1);
            }

            // store the results in the ConcurrentHashMap for future use
            cardinality = new Integer(card);
            m_uniqueCounts.put(columnList, cardinality);
         }
         finally
         {
            try { if (rs != null) rs.close(); }
            catch (Exception e) { /* ignore, we're done anyway */ }

            try { if (conn != null) conn.close(); }
            catch (Exception e) { /* ignore, we're done anyway */ }
         }
      }
      return cardinality.intValue();
   }

   private int m_cardinality;

    /**
     * The backend table supplied during construction, never <code>null</code>,
     * and for which {@link PSBackEndTable#getConnectionDetail()} will never
     * return <code>null</code>. 
     */
   private PSBackEndTable         m_table;
   private PSBackEndLogin         m_login;
   private PSIndexStatistics[]    m_indexStatistics;
   
   // map from a sorted list of column names used to distinguish to a
   // cardinality of unique tuples given those column names
   private Map m_uniqueCounts;

   /**
    * Determines whether this is an estimated object, <code>true</code> if it
    * is an estimated object. Defaults to <code>false</code>.
    */
   private boolean m_isEstimated = false;
}
