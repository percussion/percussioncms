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
import com.percussion.design.objectstore.PSReplacementValueFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
* The PSSqlParser class is used to parse SQL SELECT, INSERT, UPDATE and
* DELETE statements. It can be used to enforce that the E2 supported
* syntax is being used. This can also be disabled, allowing for placeholder
* parsing only in native statements.
*
* @author      Tas Giakouminakis
* @version      1.0
* @since      1.0
*/
public class PSSqlParser {

  /**
   * intentionally hidden default constructor
  */
  private PSSqlParser()
  {
     super();
  }

  /**
   * Parse the specified SQL statement.
   * 
   * @param connKey the connection key to use to get the db conn
   * 
   * @param sql the SQL statement to parse, which may include E2 placeholders
   *        (:variable), never <code>null</code>
   * 
   * @param bIsNative use <code>true</code> if the SQL statement is in the
   *        underlying driver's native format. This will cause E2 to search for
   *        place holders only. Otherwise, the SQL grammar will be checked for
   *        E2 conformance.
   * 
   * @return the prepared statement
   * 
   * @exception SQLException if the SQL statement is not grammatically correct
   */
  public static IPSExecutionStep[] prepare(
     int connKey, java.lang.String sql, boolean bIsNative)
     throws SQLException
  {
     PSStatement ret = null;

     if (bIsNative)
        ret = parseNativeStatement(connKey, sql);
     else
        ret = parseStatement(connKey, sql);

     return new IPSExecutionStep[] { ret };
  }

  /**
   * Get the replacement values associated with this statement.
   *
   * @param sql the sql text to parse, never <code>null</code>
   *
   * @return the replacement values used to bind data to the statement
   */
  public static IPSReplacementValue[] getReplacementValues(String sql)
  {
     if (sql == null)
      throw new IllegalArgumentException("sql may not be null");
     
     List replValues = parseNativeStatement(sql, null);
     IPSReplacementValue[] ret = new IPSReplacementValue[replValues.size()];
     replValues.toArray(ret);
     return ret;
  }


  /**
   * This method parses manual SQL statement and brakes it onto the string
   * tokens, which surround any PSX parameters prefixed with :
   * 
   * @param connKey the connection key to use to get the db conn
   * @param sql manual SQL entered in the designer UI, never <code>null</code>
   * 
   * @return the SQL wrapped in PSStatement, which is initilized with a list the
   *         parsed SQL blocks each wrapped in the PSStatementBlock instance
   * 
   * @throws SQLException if the SQL statement is not grammatically correct
   */
  private static PSStatement parseNativeStatement(int connKey, String sql) 
     throws SQLException
  {
     if (sql == null)
      throw new IllegalArgumentException("sql may not be null");
     
     PSStatementBlock curBlock = new PSStatementBlock(true);
     
     parseNativeStatement(sql, curBlock);
  
     PSStatementBlock[] useBlocks = new PSStatementBlock[1];
     useBlocks[0] = curBlock;
  
     try 
     {
        return new PSNativeStatement(connKey, useBlocks);
     } 
     catch (PSDataExtractionException e) 
     {
        throw new IllegalArgumentException(e.getLocalizedMessage());
     }
  }

  /**
   * Not to be used, use parseNativeStatement method instead
   *
   * @param connKey the connection key to use to get the db conn
   * @param sql manual SQL entered in the designer UI, never <code>null</code>
   *
   * @return always <code>null</code>
   *
   * @throws SQLException never actually thrown
  */
  private static PSStatement parseStatement(
     int connKey, java.lang.String sql)
     throws SQLException
  {
     /** @todo (future)
      * parse the statement using E2's SQL supported grammar. We do
      * not currently support processing raw SQL ourselves. You must
      * use the PSWhereClause objects, etc. to get us to generate SQL.
      */
     return null;
  }

  /**
   * This method parses manual SQL statement and breaks it onto the string
   * tokens, which surround any PSX parameters prefixed with ":", returning a
   * list of replacement values created for each PSX token. If a
   * <code>block</code> is provided, then the block's text is built, and the
   * replacement values are set on it.
   * 
   * @param sql The sql string to parse, assumed not <code>null</code>.
   * @param block The block to which the parsed text is appended and the
   *        replacement values are added, may be <code>null</code> if not
   *        required.
   * 
   * @return The list of replacement values created, never <code>null</code>,
   *         may be empty.
   */
  private static List parseNativeStatement(String sql, PSStatementBlock block)
  {
     List replValList = new ArrayList();
     int start, end;
     String field = null;
     IPSReplacementValue value;
  
     /**
      * @todo (future)
      * We are not currently supporting multiple blocks in native
      * statements (omitWhenNull support)
      */
     for (start = 0; (end = sql.indexOf(PARAM_PREFIX_CHAR, start)) != -1; ) 
     {
     
        //check if this is an escaped ':' char, used for dates, ie: HH24::MI::SS
        if (sql.length() > end + 1) 
        {
           if (sql.charAt(end + 1) == PARAM_PREFIX_CHAR) 
           {
              //must be an escaped colon char "::"
              //add mid portion of the SQL statement including one colon
              if (block != null)
                 block.addText(sql.substring(start, end + 1));
              //skip both colons and keep going
              start = end + 2;
              continue;
           }
        }
        
        if (block != null)
           block.addText(sql.substring(start, end));
     
        if (sql.charAt(end+1) == '"') 
        {
           end += 2;
           start = sql.indexOf('"', end);
           if (start == -1) 
           {
              throw new IllegalArgumentException(
                 "sql builder var not terminated" + sql.substring(end-2));
           }
           field = sql.substring(end, start);
           start++;   /* get past the " char */
        }
        else 
        {
           end++;
           for (start = end; start < sql.length(); start++) 
           {
              // see if hit whitespace delimeter, or at end of string
              if (Character.isWhitespace(sql.charAt(start))) 
              {
                 field = sql.substring(end, start);
                 break;
              }
           }
        
           if (start == sql.length())   
           {   
              /* we reached the end!!! */
              field = sql.substring(end);
           }
        }
     
        /** @todo (future)
         *
         * it would be nice to figure out the data types we need to bind to
         * up front. Unfortunately, JDBC does not provide a mechanism to
         * prepare the native statement and get the expected data types.
         * We really don't want to attempt parsing the statement as it
         * can be anything, literally.
         * Hopefully we can revisit this in the future.
         */
        int dataType = Types.NULL;
        
        // make sure we've got a valid field
        if (!PSReplacementValueFactory.isValidFieldName(field))
        {
           throw new IllegalArgumentException("Field \"" + field + 
              "\" cannot be used in this statement");
        }
        
        value = PSReplacementValueFactory.getReplacementValueFromXmlFieldName(
           field);
        
        replValList.add(value);
        
        if (block != null)
           block.addReplacementField(value, dataType, null);
     }
  
     if (block != null && start < sql.length()) 
     {
        block.addText(sql.substring(start));
     }     
     
     return replValList;
  }
   
  /**
   * ':' char, used to prefix and parse all PSX parameters in the manual SQL
  */
  private static final char PARAM_PREFIX_CHAR = ':';
}
