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
import java.sql.SQLException;
import java.util.List;


/**
 * The IPSStatementBlock interface defines a block of text which will
 * be used to construct a statement. These blocks can be strung together
 * to get the full text of the statement. Blocks can be static blocks
 * or replaceable blocks. Static blocks are always used when building the
 * statement text. Replaceable blocks are used only if all the XML
 * fields they contain are not <code>NULL</code>.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSStatementBlock
{
   /**
    * Set the data for the bound column(s) associated with this block.
    *
    * @param   data        the execution data associated with this plan
    *
    * @param   stmt         the prepared statement
    *
    * @param   bindStart   the starting position (1-based) to bind columns
    *                              to
    *
    * @return               the next bind position (1-based)
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public int setColumnData(PSExecutionData data,
      PreparedStatement stmt, int bindStart)
         throws SQLException, PSDataExtractionException;

   /**
    * Releases all resources that were used by the column's data.
    * This should be called after the data is no longer needed.
    */
   public void releaseColumnData();

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
      throws PSDataExtractionException;


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
      throws PSDataExtractionException;

   /**
    * Get the list of LOB-based PSStatementColumns.
    *
    * @return  The list of columns.  Never <code>null</code>. Can be
    *          empty.
    */
   public List getLobStatementColumns();

   /**
    * Get the data extractors used to get the replacement values which will
    * be used to execute the statement.
    *
    * @return            the list of replacement values
    */
   public List getReplacementValueExtractors();

   /**
    * Is this block static (not dependent upon run-time data)?
    *
    * @return         <code>true</code> if it is
    */
   public boolean isStaticBlock();

   /**
    * Determines whether this block generates sql that does not depend upon the
    * runtime data.
    *
    * @return If <code>true</code> the sql is not dependent on runtime
    * data, <code>false</code> otherwise.
    */
   public boolean hasStaticSql();

   /**
    * Add text to this block.
    *
    * @param text the text run to add, may not be <code>null</code> or empty
    */
   public void addText(String text);

   /**
    * Add a replacement field to the block. The value of the
    * replacement field will be used, at run-time, when the statement to
    * execute is constructed. Replacement fields are often XML fields,
    * HTML parameters or CGI variables.
    * <p>
    * Be sure to add components in the appropriate order. The run-time
    * construction uses the same ordering as the addXXX calls.
    *
    * @param value the replacement value, may not be <code>null</code>
    * @param params implementation specific parameters, may be
    * <code>null</code> or empty if the implementation does not require these
    * parameters
    */
   public void addReplacementField(IPSReplacementValue value, Object[] params);

}




