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

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * The PSStatementBlock class defines a block of text which will
 * be used to construct a statement. These blocks can be strung together
 * to get the full text of the statement. Blocks can be static blocks
 * or replaceable blocks. Static blocks are always used when building the
 * statement text. Replaceable blocks are used only if all the XML
 * fields they contain are not <code>NULL</code>.
 * <p>
 * When {@link com.percussion.data.PSQueryOptimizer PSQueryOptimizer} is
 * building the statement(s) to use, it will create statement blocks for
 * each statement. This is essential for statements with replaceable blocks,
 * but is also useful for statements with lookup values defined at run-time.
 * To build statements appropriately through statement blocks, the following
 * steps should be followed:
 * <ol>
 *   <li>create an empty PSStatementColumnMapper</li>
 *   <li>for each statement block required:
 *      <ol>
 *      <li>create the statement block (marking it static or replaceable)</li>
 *      <li>call the addText and addXmlField methods to add all the
 *         components (in the appropriate order!)</li>
 *      <li>call getColumnBindings to get the bindings for this block</li>
 *      <li>add each PSStatementColumn in the array to the
 *         PSStatementColumnMapper object for the overall statement</li>
 *      </ol></li>
 *   <li>construct a PSStatement sub-class object (PSQueryStatement or
 *      PSUpdateStatement) with the PSStatementColumnMapper and the
 *      PSStatementBlock[]</li>
 *   </ol>
 * <p>
 * At run-time, the PSQueryHandler object uses the PSStatementColumnMapper
 * to load all the field values into their appropriate statement blocks.
 * It then goes through each PSStatement object and calls its execute method.
 * Upon execution, the PSStatement object calls the buildStatement method
 * of each PSStatementBlock object and concatenates their results. The
 * resulting string can then be sent to the DBMS for immediate processing.
 * <p>
 * It may at first appear inefficient that we build statements using blocks
 * of text rather than preparing statements and binding variables. There
 * are two problems with the latter approach. First, there is no support
 * for replaceable blocks. If a parameter is <code>NULL</code>, the DBMS
 * will search for matches based on a <code>NULL</code> value, rather than
 * omit the clause. Second, prepared statements require a connection to the
 * back-end and must be submitted through the connection that prepared them.
 * In our model, where a database pool is shared by many applications,
 * using prepared statements is not practical.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSStatementBlock implements IPSStatementBlock
{
   /**
    * Construct an empty statement block.
    *
    * @param   isStatic      <code>true</code> if the block should always be
    *                                                                                                      used; <code>false</code> if it should be ignored
    *                                                                                                      when any of the XML fields contain
    *                                                                                                      <code>NULL</code> values
    */
   public PSStatementBlock(boolean isStatic)
   {
      super();
      m_isStatic      = isStatic;
      m_blocks         = new java.util.ArrayList();
   }

   /**
    * Add a text run to this statement block.
    * <p>
    * Be sure to add components in the appropriate order. The run-time
    * construction uses the same ordering as the addXXX calls.
    *
    * @param      text         the text run to add
    */
   public void addText(java.lang.String text)
   {
      m_blocks.add(text);
   }

   /**
    * Convenience method for adding replacement fields which are not
    * concerned with lob column initializers, will call addReplacementField
    * with the <code>lci</code> set to <code>null</code>.
    *
    * @param value the replacement value, may not be <code>null</code>
    * @param type the java.sql.Type data type to use when setting column data
    * @param col the backend column object, may be <code>null</code>
    */
   public void addReplacementField(IPSReplacementValue value, int type, PSBackEndColumn col)
   {
      addReplacementField(value, type, col, null);
   }

   /**
    * Add a replacement field to this statement block. The value of the
    * replacement field will be used, at run-time, when the statement to
    * execute is constructed. Replacement fields are often XML fields,
    * HTML parameters or CGI variables.
    * <p>
    * Be sure to add components in the appropriate order. The run-time
    * construction uses the same ordering as the addXXX calls.
    *
    * @param value the replacement value, may not be <code>null</code>
    * @param type the java.sql.Type data type to use when setting column data
    * @param col the backend column object, may be <code>null</code>
    * @param lci the lob column initializer, may be <code>null</code>
    */
   public void addReplacementField(IPSReplacementValue value, int type,
      PSBackEndColumn col, IPSLobColumnInitializer lci)
   {
      PSStatementColumn stmtCol = null;
      try
      {
         stmtCol = new PSStatementColumn(value, type, col, lci);
      }
      catch (IllegalArgumentException ex)
      {
         throw new IllegalArgumentException(ex.getLocalizedMessage());
      }

      if (type == java.sql.Types.BLOB || type == java.sql.Types.CLOB)
         m_lobStatementColumns.add(stmtCol);

      m_blocks.add(stmtCol);
   }

   /**
    */
   public List getLobStatementColumns()
   {
      return m_lobStatementColumns;
   }

   /**
    * Set the data for the bound column(s) associated with this block.
    *
    * @param   data        the execution data associated with this plan
    *
    * @param   stmt         the prepared statement
    *
    * @param   bindStart   the starting position (1-based) to bind columns
    *                                                                                                      to
    *
    * @return               the next bind position (1-based)
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public int setColumnData(PSExecutionData data,
      PreparedStatement stmt, int bindStart)
         throws SQLException, PSDataExtractionException
   {
      // if we're doing omit when null, check if we have any null values
      if (shouldThisBeOmitted(data))
         return bindStart;

      for (int i = 0; i < m_blocks.size(); i++) {
         Object cur = m_blocks.get(i);
         if (cur instanceof PSStatementColumn) {
            bindStart
               = ((PSStatementColumn)cur).setData(data, stmt, bindStart);
         }
      }

      return bindStart;
   }

   // see IPSStatementBlock.releaseColumnData()
   public void releaseColumnData()
   {
      for (int i = 0; i < m_blocks.size(); i++)
      {
         Object cur = m_blocks.get(i);
         if (cur instanceof PSStatementColumn)
            ((PSStatementColumn)cur).releaseData();
      }
   }

   /**
    * Build the statement text which can be passed to the JDBC Connection
    * object's prepareStatement method. Placeholders (?) will be used for
    * each variable defined in the statement;
    *
    * @param   buf         the buffer to store the text in
    *
    * @param   data         the run-time context info for this request
    */
   public void buildStatement(StringBuilder buf, PSExecutionData data)
      throws PSDataExtractionException
   {
      // if we're doing omit when null, check if we have any null values
      if (shouldThisBeOmitted(data))
         return;

      for (int i = 0; i < m_blocks.size(); i++) {
         Object cur = m_blocks.get(i);
         if (cur instanceof PSStatementColumn)
            buf.append(((PSStatementColumn)cur).getPlaceHolder(data));
         else
            buf.append((String)cur);
      }
   }

   /**
    * Build the statement text which can be passed to the JDBC Connection
    * object's prepareStatement method. Placeholders (?) will be used for
    * each variable defined in the statement;
    *
    * @param   data         the run-time context info for this request
    *
    * @return               the statement text
    */
   public String buildStatement(PSExecutionData data)
      throws PSDataExtractionException
   {
      StringBuilder buf = new StringBuilder();
      buildStatement(buf, data);
      return buf.toString();
   }

   /**
    * Get the data extractors used to get the replacement values which will
    * be used to execute the statement.
    *
    * @return            the list of replacement values
    */
   public List getReplacementValueExtractors()
   {
      java.util.ArrayList retList = new java.util.ArrayList();
      Object cur;

      for (int i = 0; i < m_blocks.size(); i++) {
         if ((cur = m_blocks.get(i)) instanceof PSStatementColumn)
         {
            retList.add(
               ((PSStatementColumn)cur).getReplacementValueExtractor());
         }
      }
      return retList;
   }

   /**
    * Is this block static (not dependent upon run-time data)?
    *
    * @return         <code>true</code> if it is
    */
   public boolean isStaticBlock()
   {
      return m_isStatic;
   }

   /**
    * See {@link IPSStatementBlock#hasStaticSql()} for details.
    */
   public boolean hasStaticSql()
   {
      return true;
   }

   /**
    * See {@link IPSStatementBlock#addReplacementField(
    * IPSReplacementValue, Object[])} for details.
    */
   public void addReplacementField(IPSReplacementValue value, Object[] params)
   {
      if ((params == null) || (params.length < 1) || (params[0] == null))
      {
         throw new IllegalArgumentException("Invalid parameters specified");
      }
      String strJdbcType = params[0].toString().trim();
      int jdbcType = PSStatementColumn.UNKNOWN_JDBC_TYPE;
      try
      {
         jdbcType = Integer.parseInt(strJdbcType);
      }
      catch (NumberFormatException ex)
      {
         throw new IllegalArgumentException
            ("Invalid jdbc type specified." + ex.getLocalizedMessage());
      }

      PSBackEndColumn col = null;
      IPSLobColumnInitializer lci = null;

      if ((params[1] != null) && (params[1] instanceof PSBackEndColumn))
         col = (PSBackEndColumn)params[1];

      if ((params[2] != null) && (params[2] instanceof IPSLobColumnInitializer))
         lci = (IPSLobColumnInitializer)params[2];

      addReplacementField(value, jdbcType, col, lci);
   }

   protected boolean shouldThisBeOmitted(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      // if we're doing omit when null, check if we have any null values
      if (!m_isStatic) {
         for (int i = 0; i < m_blocks.size(); i++) {
            Object cur = m_blocks.get(i);
            if ((cur instanceof PSStatementColumn) &&
               ((PSStatementColumn)cur).isNull(data))
            {
               return true;
            }
         }
      }

      return false;
   }


   private boolean               m_isStatic;
   protected List                  m_blocks;

   /**
    * A place to store the statement columns for LOB-based columns.
    * These need to be accessed by DBMS-specific PSStatement-based
    * classes so that they can update LOB columns as required.
    * Never <code>null</code> but may be empty.
    */
   private List                  m_lobStatementColumns = new ArrayList();
}

