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

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;


/**
 * The PSStatementGroup class defines a grouping of PSStatementBlock objects.
 * When blocks can be omitted due to NULL values, it is possible to generate
 * an invalid statement. Two common cases are:
 * <UL>
 *
 * <LI>WHERE is part of the static block. There is one omit when NULL block
 * which is NULL. The statement ends up as "SELECT ... WHERE" which is
 * invalid as WHERE must be followed by conditionals.</LI>
 *
 * <LI>AND is part of a dynamic block. If there are two blocks of the form
 * "col = ? AND" and either block is omitted, the generated statement is
 * "SELECT ... WHERE col = ? AND" which is invalid due to the trailing AND.
 *
 * </UL>
 *
 * <P>
 * This class attempts to deal with these problems by allowing groups to
 * be formed. Each group takes an optional prefix (eg, WHERE), a left block,
 * an optional block separator and a right block.
 * The left and right blocks can also be groups.
 * As such, we can create the following groups to accomodate
 * both of the above cases:
 *
 * <TABLE BORDER="1">
 * <TR>
 *      <TH>Group Number</TH>
 *      <TH>Prefix</TH>
 *      <TH>Left Block</TH>
 *      <TH>Block Separator</TH>
 *      <TH>Right Block</TH>
 * </TR>
 * <TR>
 *      <TD>1</TD>
 *      <TD>WHERE</TD>
 *      <TD>col1 = ?</TD>
 *      <TD>AND</TD>
 *      <TD>Group 2</TD>
 *      <TD></TD>
 * </TR>
 * <TR>
 *      <TD>2</TD>
 *      <TD></TD>
 *      <TD>col2 = ?</TD>
 *      <TD>AND</TD>
 *      <TD>col3 = ?</TD>
 *      <TD></TD>
 * </TR>
 * </TABLE>
 *
 * <P>
 * If both conditions in a block are NULL, the entire block is omitted.
 * If one condition is NULL, the prefix is used but the
 * block separator is omitted. If both conditions are valid, the entire
 * block is used.
 * <P>
 * Using the above example, here's a table of possible values
 * and the expected output from thie grouping:
 * <TABLE BORDER="1">
 * <TR>
 *      <TH>col1</TH>
 *      <TH>col2</TH>
 *      <TH>col3</TH>
 *      <TH>Statement</TH>
 * </TR>
 * <TR>
 *      <TH>NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>NULL</TH>
 *      <TH></TH>
 * </TR>
 * <TR>
 *      <TH>NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>WHERE col3 = ?</TH>
 * </TR>
 * <TR>
 *      <TH>NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>WHERE col2 = ?</TH>
 * </TR>
 * <TR>
 *      <TH>NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>WHERE col2 = ? AND col3 = ?</TH>
 * </TR>
 * <TR>
 *      <TH>NOT NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>WHERE col1 = ?</TH>
 * </TR>
 * <TR>
 *      <TH>NOT NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>WHERE col1 = ? AND col2 = ?</TH>
 * </TR>
 * <TR>
 *      <TH>NOT NULL</TH>
 *      <TH>NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>WHERE col1 = ? AND col3 = ?</TH>
 * </TR>
 * <TR>
 *      <TH>NOT NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>NOT NULL</TH>
 *      <TH>WHERE col1 = ? AND col2 = ? AND col3 = ?</TH>
 * </TR>
 *
 * @see         PSStatementBlock
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSStatementGroup implements IPSStatementBlock
{
   /**
    * Construct a statement group for the specified blocks.
    *
    * @param   prefix            an optional prefix to prepend before the
    *                                                                        groups text (if at least one part is
    *                                                                        non-NULL)
    *
    * @param   leftBlock         the block to use on the left side of the
    *                                                                        group
    *
    * @param   blockSeparator      an optional separator to use when both
    *                                                                        parts are non-NULL
    *
    * @param   rightBlock         the block to use on the right side of the
    *                                                                        group
    */
   public PSStatementGroup(
      String prefix, IPSStatementBlock leftBlock, String blockSeparator,
      IPSStatementBlock rightBlock)
   {
      super();
      m_prefix = prefix;
      m_leftBlock = leftBlock;
      m_blockSeparator = blockSeparator;
      m_rightBlock = rightBlock;
   }

   /**
    * Set the data for the bound column(s) associated with this block.
    *
    * @param   data        the execution data associated with this plan
    *
    * @param   stmt         the prepared statement
    *
    * @param   bindStart   the starting position (1-based) to bind columns
    *                                                                  to
    *
    * @return               the next bind position (1-based)
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public int setColumnData(
      PSExecutionData data, PreparedStatement stmt, int bindStart)
      throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException
   {
      if (m_leftBlock != null)
         bindStart = m_leftBlock.setColumnData(data, stmt, bindStart);

      if (m_rightBlock != null)
         bindStart = m_rightBlock.setColumnData(data, stmt, bindStart);

      return bindStart;
   }

   // see IPSStatementBlock.releaseColumnData()
   public void releaseColumnData()
   {
      if (m_leftBlock != null)
         m_leftBlock.releaseColumnData();

      if (m_rightBlock != null)
         m_rightBlock.releaseColumnData();
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
   public void buildStatement(
      java.lang.StringBuffer buf, PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      // get current size in case we need to remove the entire block
      int initSize = buf.length();

      // first we'll add the prefix
      if (m_prefix != null)
         buf.append(m_prefix);

      /* now let's see what the size is so we can determine if the block
       * stored any data or considered itself NULL
       */
      int prefixSize = buf.length();
      int leftSize = prefixSize;
      int curSize = prefixSize;

      if (m_leftBlock != null) {
         m_leftBlock.buildStatement(buf, data);
         leftSize = buf.length();
         curSize = leftSize;
         if (prefixSize != leftSize) {
            /* we can now tack on the separator (if it exists). If the right
             * side doesn't exist, we'll truncate to prefixSize, which will
             * get rid of it, as required
             */
            if (m_blockSeparator != null) {
               buf.append(m_blockSeparator);
               curSize = buf.length();   // set this to include separator length
            }
         }
      }

      if (m_rightBlock != null) {
         m_rightBlock.buildStatement(buf, data);
      }

      /* we now need to see if we need to truncate the block in any way.
       *
       * 1. if the size is the same as the prefix size, we need
       *    to completely clear it.
       *
       * 2. if the size is the same as curSize (leftSize + separator length)
       *    we need to strip off the block separator
       *
       * 3. keep the whole thing
       */
      if (prefixSize == buf.length())
         buf.setLength(initSize);
      else if (curSize == buf.length())
         buf.setLength(leftSize);
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
   public java.lang.String buildStatement(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      StringBuffer buf = new StringBuffer();
      buildStatement(buf, data);
      return buf.toString();
   }

   /**
    * Get the data extractors used to get the replacement values which will
    * be used to execute the statement.
    *
    * @return            the list of replacement values
    */
   public java.util.List getReplacementValueExtractors()
   {
      java.util.ArrayList retList = new java.util.ArrayList();

      if (m_leftBlock != null) {
         java.util.List tempList = m_leftBlock.getReplacementValueExtractors();
         retList.addAll(tempList);
      }

      if (m_rightBlock != null) {
         java.util.List tempList = m_rightBlock.getReplacementValueExtractors();
         retList.addAll(tempList);
      }

      return retList;
   }

   /**
    */
   public List getLobStatementColumns()
   {
      ArrayList retList = new ArrayList();

      if (m_leftBlock != null) {
         List tempList = m_leftBlock.getLobStatementColumns();
         retList.addAll(tempList);
      }

      if (m_rightBlock != null) {
         List tempList = m_rightBlock.getLobStatementColumns();
         retList.addAll(tempList);
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
      return ((m_leftBlock != null) && m_leftBlock.isStaticBlock()) &&
         ((m_rightBlock != null) && m_rightBlock.isStaticBlock());
   }

   /**
    * See {@link IPSStatementBlock#hasStaticSql()} for details.
    */
   public boolean hasStaticSql()
   {
      return ((m_leftBlock != null) && m_leftBlock.hasStaticSql()) &&
         ((m_rightBlock != null) && m_rightBlock.hasStaticSql());
   }

   /**
    * See {@link IPSStatementBlock#addReplacementField(
    * IPSReplacementValue, Object[])} for details.
    *
    * @throws UnsupportedOperationException cannot add replacement field to
    * statement group
    */
   public void addReplacementField(IPSReplacementValue value, Object[] params)
   {
      throw new UnsupportedOperationException(
         "Cannot add replacement field to statement group");
   }

   /**
    * See {@link IPSStatementBlock#addText(String)} for details.
    *
    * @throws UnsupportedOperationException cannot add text to
    * statement group
    */
   public void addText(String text)
   {
      throw new UnsupportedOperationException(
         "Cannot add text to statement group");
   }

   void setLeftBlock(IPSStatementBlock block)
   {
      m_leftBlock = block;
   }

   IPSStatementBlock getLeftBlock()
   {
      return m_leftBlock;
   }

   void setRightBlock(IPSStatementBlock block)
   {
      m_rightBlock = block;
   }

   IPSStatementBlock getRightBlock()
   {
      return m_rightBlock;
   }

   void setBlockSeparator(String sep)
   {
      m_blockSeparator = sep;
   }

   String getBlockSeparator()
   {
      return m_blockSeparator;
   }

   protected String               m_prefix;
   protected IPSStatementBlock   m_leftBlock;
   protected String               m_blockSeparator;
   protected IPSStatementBlock   m_rightBlock;
}

