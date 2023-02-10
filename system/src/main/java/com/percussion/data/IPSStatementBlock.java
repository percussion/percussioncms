/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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




