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

import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSSqlException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;

import java.sql.ResultSet;
import java.util.Map;

/**
 * The PSIndexedLookupJoiner class is used internally by the E2 server to
 * take the results of one query, issue queries against another data
 * store and combine the results into a single result set.
 * <p>
 * The Query Optimizer (PSQueryOptimizer) chooses an execution plan which
 * will optimally access the data from each back-end. It also defines
 * the join requirements.
 *
 * @see        PSQueryOptimizer
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSIndexedLookupJoiner extends PSQueryJoiner
{
   /**
    * Construct a joiner to handle the speciifed result sets.
    *
    * @param      ah         the application handler containing this join
    *
    * @param      join      the join condition
    *
    * @param      lCols      the columns to select from the left side table
    *
    * @param      lOmitCols   the left columns for join only (will be
    *                        omitted from the result set)
    *
    * @param      rCols      the columns to select from the right side table
    *
    * @param      rOmitCols   the right columns for join only (will be
    *                        omitted from the result set)
    *
    * @param      rStmt      the statement to execute against the right side
    *                        table
    *
    * @param      expectedSelectivity
    *                        the number of rows we expect to be in a typical
    *                        result set
    *
     */
   public PSIndexedLookupJoiner(
      PSApplicationHandler ah, PSBackEndJoin join,
      String[] lCols, String[] lOmitCols,
      String[] rCols, String[] rOmitCols,
      PSQueryStatement rStmt,
      int expectedSelectivity
      )
      throws PSNotFoundException, PSExtensionException
   {
      super(ah, join, lCols, lOmitCols, rCols, rOmitCols, expectedSelectivity);
      m_RightStatement = rStmt;
   }


   /**
    * extend PSQueryJoiner toString to add the right side statement dump
    */
   public java.lang.String toString()
   {
      String sJoin = super.toString();
      return sJoin + " [USING " + m_RightStatement.toString() + "]";
   }


   /* ************  IPSExecutionStep Interface Implementation ************ */

   /**
    * Execute the join of two or more result sets as a step in the
    * execution plan. Each join may act on a subset of the result sets
    * defined in the execution data. It will then remove the result sets
    * and store the merged result set.
    *
    * @param   data     execution data is a container for the input data
    *                   as well as a collection of result sets generated
    *                   by queries.
    *
    */
   public void execute(PSExecutionData data)
         throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException,
         com.percussion.error.PSErrorException
   {
      /* this type of join requires only one result set on the stack */
      java.util.Stack stack = data.getResultSetStack();
      if (stack.size() < 1)
      {
         throw new PSSqlException(
               IPSDataErrors.INDEX_JOINER_RESULT_SET_REQD, "25000");
      }

      /* pop off the left side result set which will be used for
       * querying against the right side table.
       */
      ResultSet lRS = (ResultSet) stack.pop();
      // find index of left join column
      m_leftColumnIndex = PSBackEndColumnExtractor.getColumnOrdinal(m_leftColumn, lRS.getMetaData());
      if (m_leftColumnIndex < 1)
      {
         throw new PSSqlException(
               IPSDataErrors.INDEX_JOINER_LCOL_NOT_FOUND, m_leftColumn, "33000");
      }
      else
         m_leftColumnIndex--;   // need this 0 based

      try
      {
         // create the initial buffer with just the left side result set
         PSJoinedRowDataBuffer rowData = new PSJoinedRowDataBuffer(
               lRS, m_columnHash, m_omitColumns,
               java.lang.Math.max(m_expectedSelectivity, m_averageSelectivity));

         // in case we're using UDFs, set the ResultSetMetaData and current row
         if (m_translator != null)
         {
            data.setCurrentResultSetMetaData(lRS.getMetaData());
            data.setCurrentResultRowData(rowData.getCurrentRow());
         }

         // do the logic to process all the row data
         processData(data, rowData);

         // we're done, push the result set on the stack
         stack.push(rowData.getJoinedResultSet());
      }
      finally
      {
         if (m_closeInputs)
         {
            // close the result sets (only once, fixed bug #Rx-01-09-0018)
            // we're done with them
            lRS.close();
         }
      }
   }


   private void processData(
      PSExecutionData execData, PSJoinedRowDataBuffer rowData)
      throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException,
         com.percussion.error.PSErrorException
   {
      /* now's the tough stuff. we need to go through each row of data
       * in the left side table, execute the query using the right side
       * statement, then do the joining of the results.
       */
      Object[] curRow = rowData.getCurrentRow();

      ResultSet rRS = null;   // right side RS will keep changing

      // we want to get result sets from the stack
      java.util.Stack stack = execData.getResultSetStack();
      int stackDepth = stack.size();

      /* this is the count of rows we've processed, which we keep track of
       * to dynamically adjust for selectivity estimation errors
       */
      int rowsProcessed = 0;

      PSRequest request = execData.getRequest();
      Map<String,Object> params = request.getParameters();
      String paramName = "$$E2IndexedJoiner_" + m_leftColumn;
      Object keyData;
      while (rowData.readLeftRow()) {
         /* get the data from the left side column and store it in the
          * specified HTML parameter
          */
         if (m_translator != null)
            keyData = m_translator.extract(execData);
         else
            keyData = curRow[m_leftColumnIndex];

         if (keyData == null)
            params.remove(paramName);
         else {
            try
            {
               keyData = PSDataConverter.convert(
                  keyData, PSDataConverter.DATATYPE_TEXT);

               params.put(paramName, (String)keyData);
            }
            catch (IllegalArgumentException e)
            {
               // there was a conversion error
               throw new PSSqlException(0, e.getLocalizedMessage(), "22005");
            }
         }

         // now we can execute the right side statement
         m_RightStatement.execute(execData);
         try
         {
            // if the stack depth hasn't grown, the statement returned
            // no data
            if (stackDepth == stack.size())
               continue;

            rRS = (ResultSet) stack.pop(); // get the result set from the exec
            rowData.setRightSideResultSet(rRS);

            // need to get the current row object as it may have changed
            curRow = rowData.getCurrentRow();

            // now loop through the data adding each row
            while (rowData.readRightRow())
            {
               rowData.addRow(curRow);
               rowsProcessed++;
            }
         }
         finally
         {
            if (m_closeInputs)
            {
               // close the result sets, we're done with them
               if (rRS != null)
               {
                  rRS.close();
               }
            }
         }
      }

      // now bump the statistical information
      updateAverageSelectivity(rowsProcessed);

      // no reason to leave this laying around
      params.remove(paramName);
   }

   protected PSQueryStatement      m_RightStatement;
}

