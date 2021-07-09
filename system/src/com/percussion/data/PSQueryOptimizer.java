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

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSAbstractParamValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSFunctionParamValue;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSApplicationHandler;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSDtdNode;
import com.percussion.xml.PSDtdTree;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The PSQueryOptimizer class is used internally by the E2 server to
 * determine the best execution plan for a query. This is critical when
 * dealing with queries extracting data from heterogenous data stores.
 * When this is the case, significant improvements in performance can be
 * realized by choosing a good execution plan.
 * <p>
 * In addition to query generation, the optimizer defines how the data will
 * be joined. This is then used by the Query Joiner (PSQueryJoiner).
 * <p>
 * The optimizer also uses any row filters defined for the query pipe(s).
 * Row filters can often be built into the query, which can improve
 * performance for the query.
 * <p>
 * The final step is to add any data set exits to the execution plan. Data
 * set exits are always fired last, after all data from the queries has been
 * merged into a single result set.
 *
 * @see       PSQueryJoiner
 *
 * @author      Tas Giakouminakis
 * @version        1.0
 * @since      1.0
 */
public class PSQueryOptimizer extends PSOptimizer
{

   /**
    * Generate an execution plan for the given data set. This determines
    * what queries must be executed, exits fired and data joined to
    * produce the desired results.
    * <p>
    * The following steps are performed to create the execution plan:
    * <ol>
    * <li>for each pipe defined in the data set:
    *    <ol>
    *    <li>if the pipe is not a PSQueryPipe instance, skip it</li>
    *    <li>for Phase I, error if more than one query pipe was defined</li>
    *    <li>get the PSDataSelector for the pipe</li>
    *    <li>if a native statement is being used:
    *       <ol>
    *       <li>parse it for placeholders (:fieldname)</li>
    *       <li>build the PSQueryStatement object for this statement</li>
    *       </ol></li>
    *    <li>else (builder is being used):
    *       <ol>
    *       <li>single table being used:
    *          <ol>
    *          <li>build the SELECT list from the PSDataMapper column
    *          list</li>
    *          <li>build the FROM using this table name</li>
    *          <li>build the WHERE clause from the PSWhereClause objects</li>
    *          <li>if rowFilter exits are defined on this table which can
    *          be used in the WHERE clause, add them to it</li>
    *          <li>build the PSQueryStatement object for this statement</li>
    *          </ol></li>
    *       <li>multiple tables being used:
    *          <ol>
    *          <li>verify PSBackEndJoin objects are defined joining all
    *          tables</li>
    *          <li>for all tables defined in the same database, check if
    *          joins are supported by the DBMS</li>
    *          <li>if homogeneous joins are supported, create a joined
    *          statement to issue against the back-end</li>
    *          <li>if not, create statements for each table and
    *          PSQueryJoiner objects to have E2 perform the joins</li>
    *          </ol></li>
    *       </ol></li>
    *    </ol></li>
    * <li>add any exits which were not incorporated into statements to the
    *     execution plan</li>
    * </ol>
    *
    * <p>
    * <EM>Implementation Note:</EM> For Phase I, only a single query pipe
    * referring to a single table will be supported. To perform more
    * complex actions, a native statement must be used. It is also worth
    * noting that optimizations, such as index analysis, will not be done
    * in initial versions.
    *
    * @param       ah     the application handler for this request
    *
    * @param    ds    the data set for which to generate an execution plan
    *
    * @return IPSExecutionStep[]  the array of execution steps in the execution plan
    *
    * @exception    SQLException
    *                      if a SQL error occurs
    */
   public static IPSExecutionStep[] createExecutionPlan(PSApplicationHandler ah,
      PSDataSet ds)
      throws
         java.sql.SQLException,
         PSNotFoundException,
         PSExtensionException
   {
      // this method does little real work outside of validating the params
      // the real work is done by private utility methods called by this
      // method

      // make sure the app handler is not null
      if (ah == null)
         throw new IllegalArgumentException("exec plan app handler is null");

      // make sure the data set is not null
      if (ds == null) {
         throw new IllegalArgumentException("exec plan data set is null: " +
            ah.getName());
      }

      // make sure the dataset has pipes
      PSPipe pipe = ds.getPipe();
      if (pipe == null) {
         throw new IllegalArgumentException("exec plan pipes is null: " +
            ah.getName() + " " + ds.getName());
      }

      // make sure that we have a query pipe
      if (!(pipe instanceof com.percussion.design.objectstore.PSQueryPipe)) {
         throw new IllegalArgumentException("exec plan no query pipes: " +
            ah.getName() + " " + ds.getName());
      }

      // build the actual plan
      return buildExecutionPlan(ah, ds, (PSQueryPipe)pipe);
   }

   /**
    * Private utility method to analyze the pipe and build
    * an execution plan appropriate to them.
    */
   private static IPSExecutionStep[] buildExecutionPlan(
      PSApplicationHandler ah,
      PSDataSet ds,
      PSQueryPipe pipe)
      throws java.sql.SQLException,
         PSNotFoundException,
         PSExtensionException
   {
      Vector               steps = new Vector();
      Vector               logins = new Vector();
      Hashtable            connKeys = new Hashtable();
      IPSExecutionStep[]   curSteps;
      IPSExecutionStep[]   ret = null;
      int                  loginCount = 0;

      /*
       *  1) Create the login steps for that pipe's back ends (one login
       *     per back end, a pipe can connect to multiple back ends)
       *
       *  2) Create the statement steps for selecting and possibly joining
       *     the data from that pipe's back ends.
       */
      PSDataSelector sel = pipe.getDataSelector();

      /* *TODO* (future)
       *
       * add support for result set caching.
       */

      // make sure there is at least one back-end table in the pipe
      PSCollection beTables = pipe.getBackEndDataTank().getTables();
      if ((beTables == null) || (beTables.size() == 0))
      {
         throw new IllegalArgumentException("exec plan no beta tables in pipe: " +
            ah.getName() + " " + ds.getName() + " " + pipe.getName());
      }

      // create the login object(s) for this pipe
      loginCount += createLoginPlan(ah, beTables, logins, connKeys, pipe.getBackEndDataTank().getJoins());

      // and create the statement to select data for this pipe
      if (sel.isSelectByNativeStatement())
      {
         // the last login is the one we need
         curSteps = PSSqlParser.prepare(
            loginCount - 1, sel.getNativeStatement(), true);
      }
      else
      {
         curSteps = createStatements(
            ah, ds, logins, connKeys, pipe.getBackEndDataTank(),
            pipe.getDataMapper(), sel);
      }

      for (int s = 0; s < curSteps.length; s++)
         steps.add(curSteps[s]);

      ret = new IPSExecutionStep[steps.size() + logins.size()];
      logins.copyInto(ret);   // logins go first!!!
      int iDest = logins.size();
      for (int i = 0; i < steps.size(); i++) {
         ret[iDest] = (IPSExecutionStep)steps.get(i);
         iDest++;
      }

      logExecutionPlan(ah, ds, ret);

      return ret;
   }

   /**
    * Create the SQL SELECT statements from the specified components.
    * This includes single table, homogeneous joins
    * and heterogeneous joins.
    *
    * @param ah the application handler this statement is being built for,
    * assumed not <code>null</code>
    *
    * @param ds the data set this statement is being built for,
    * assumed not <code>null</code>
    *
    * @param connKeys a hashtable containing the connection key to use
    * where the key is opaque and the value is the connection key, 
    * assumed never be <code>null</code>
    *
    * @param logins a list of logins, the indices into which can be
    * derived from the connKeys hash, assumed never <code>null</code>
    *
    * @param backEnds the back end(s) to get data from, 
    * assumed not <code>null</code>
    *
    * @param cols the back-end columns to retrieve, 
    * assumed not <code>null</code>
    *
    * @param sel the data selector to use, 
    * assumed not <code>null</code>
    *
    * @return {@link IPSExecutionStep}[] the prepared statement or statements,
    * never <code>null</code>
    *
    * @exception SQLException if the SQL statement is not grammatically
    * correct
    */
   private static IPSExecutionStep[] createStatements(
      PSApplicationHandler ah, PSDataSet ds,
      List logins, Hashtable connKeys, PSBackEndDataTank backEnds,
      PSDataMapper cols, PSDataSelector sel
      )
      throws java.sql.SQLException,
         PSNotFoundException,
         PSExtensionException
   {
      // make sure the list of back ends is not null
      if (backEnds == null)
         throw new IllegalArgumentException("sql builder no back ends");

      // make sure that there is at least one table
      PSCollection tables = backEnds.getTables();
      if ( (tables == null) || (tables.size() == 0) )
         throw new IllegalArgumentException("sql builder no back end tables");

      /* load all the database meta data objects at this point since we have
       * the login info available. The subsequent calls are then simplified
       * for accessing the components (uid/pw need not be known)
       */
      int numTables = tables.size();
      for (int i = 0; i < numTables; i++)
      {
         // get the current table object
         PSBackEndTable curTable = (PSBackEndTable)tables.get(i);

         // create the meta data for this db
         PSBackEndLogin login = (PSBackEndLogin)logins.get(
            ((Integer)connKeys.get(curTable.getServerKey())).intValue());
         getCachedDatabaseMetaData(login);
      }

      PSStatement stmt = null;

      /* TODO (future): handle self joining
       *
       * The interesting issue with self joining is how do we want to
       * represent it? would we have the same table appear twice but
       * with different aliases to differentiate between the two?
       */

      /* if there is only one table, then we can build a simple select
       * note that we don't need to add joins, because there are no joins,
       * because there is only one table!
       */
      if (numTables == 1)
      {
         PSSqlQueryBuilder builder = new PSSqlQueryBuilder();
         builder.addTable((PSBackEndTable)tables.get(0));

         // use the data mapper to add all the columns to the select list
         PSDataMapping map;
         for (int i = 0; i < cols.size(); i++)
         {
            map = (PSDataMapping)cols.get(i);
            IPSBackEndMapping curMap = map.getBackEndMapping();
            if (curMap instanceof PSExtensionCall)
            {
               PSExtensionParamValue[] params =
                  ((PSExtensionCall)curMap).getParamValues();
               for (int j = 0; j < params.length; j++) {
                  if ((null != params[j]) && (params[j].isBackEndColumn()))
                     builder.addSelectColumn(
                        (PSBackEndColumn)params[j].getValue());
               }
            }
            else if (curMap instanceof PSBackEndColumn)
            {
               // Only if it's a back-end column!  Bug Id: Rx-99-11-0023
               builder.addSelectColumn((PSBackEndColumn)curMap);
            }

            PSCollection conds = map.getConditionals();
            if ((conds != null) && (conds.size() != 0))
            {
               // go through the conditions to add any back-end columns
               for (int j = 0; j < conds.size(); j++)
               {
                  PSConditional cond = (PSConditional)conds.get(j);
                  IPSReplacementValue val = cond.getVariable();
                  if (val instanceof PSBackEndColumn)
                  {
                     builder.addSelectColumn((PSBackEndColumn)val);
                  }
                  else if (val instanceof PSFunctionCall)
                  {
                     PSFunctionParamValue[] params =
                        ((PSFunctionCall)val).getParamValues();
                     addParamValuesColumns(params, builder);
                  }

                  if (!cond.isUnary())
                  {
                     val = cond.getValue();
                     if (val instanceof PSBackEndColumn)
                     {
                        builder.addSelectColumn((PSBackEndColumn)val);
                     }
                     else if (val instanceof PSFunctionCall)
                     {
                        PSFunctionParamValue[] params =
                           ((PSFunctionCall)val).getParamValues();
                        addParamValuesColumns(params, builder);
                     }
                  }
               }
            }
         }
         PSRequestor req = ds.getRequestor();
         /* If this is a direct stream check to see if the mime-type
            can be determined by a column, and be sure to select it! */
         if (req != null && req.isDirectDataStream())
         {
            IPSReplacementValue mimeType = req.getOutputMimeType();

            if ((mimeType != null) && (mimeType instanceof PSBackEndColumn))
               builder.addSelectColumn((PSBackEndColumn) mimeType);
         }

         // use the data selector to set the unique constraints
         builder.setUnique(sel.isSelectUnique());

         // use the data selector to add the where clauses
         PSCollection whereClauses = sel.getWhereClauses();
         for (int i = 0; i < whereClauses.size(); i++)
         {
            builder.addWhereClause((PSWhereClause)whereClauses.get(i));
         }

         // use the data selector to add the sort constraints
         PSCollection sortedColumns = sel.getSortedColumns();
         int size = (sortedColumns == null) ? 0 : sortedColumns.size();
         if (size == 0) {
            sortedColumns = getSortColumnsForXmlCollapsing(ah, ds);
            size = (sortedColumns == null) ? 0 : sortedColumns.size();
         }
         for (int i = 0; i < size; i++)
         {
            builder.addSortedColumn((PSSortedColumn)sortedColumns.get(i));
         }

         stmt = builder.generate(logins, connKeys);
      }
      else // multiple tables, do a join
      {
         return createJoinPlan(
            ah, ds, logins, connKeys, backEnds, tables, cols, sel);
      }

      return new IPSExecutionStep[] { stmt };
   }


   /**
    * If the specified parameters contain replacement values for backend
    * columns, then adds the columns to the select statement using the query
    * builder.
    *
    * @param params parameters containing replacement values, assumed not
    * <code>null</code>, may be empty
    *
    * @param builder query builder, assumed not <code>null</code>
    */
   private static void addParamValuesColumns(
      PSAbstractParamValue[] params, PSSqlQueryBuilder builder)
   {
      for (int j = 0; j < params.length; j++)
      {
         if ((null != params[j]) && (params[j].isBackEndColumn()))
            builder.addSelectColumn((PSBackEndColumn)params[j].getValue());
      }
   }

   /**
    * @author chadloder
    *
    * Create a select plan for a join. This may be homogenous
    * (across multiple tables in back-ends on the same DBMS) or
    * heterogenous (across multiple tables in back-ends on the different
    * DBMSs).
    * <P>
    * The select plan will consist of an optimized list of execution steps
    * that will complete the join.
    *
    * @param    ah  The application handler, 
    * assumed not <code>null</code>
    *
    * @param   logins   A list of PSBackEndLogins, the indices into
    * which can be derived from <CODE>connKeys</CODE>, 
    * assumed not <code>null</code>
    *
    * @param   connKeys   A hash from opaque keys to Integer objects, 
    * the values of which are the indices into <CODE>logins</CODE> 
    * for that driver and server, assumed not <code>null</code>
    *
    * @param    backEnds    The back ends involved in the join, 
    * assumed not <code>null</code>
    *
    * @param    beTables    The tables involved in the join, 
    * assumed not <code>null</code>
    *
    * @param    cols    The columns we're interested in (the projected
    * columns, not necessarily the same columns we use in the selects), 
    * assumed not <code>null</code>
    *
    * @param    sel The data selector containing the selection
    * criteria for the columns, 
    * assumed not <code>null</code>
    *
    * @return an array of {@link IPSExecutionStep} that is never 
    * <code>null</code> 
    */
   private static IPSExecutionStep[] createJoinPlan(
      PSApplicationHandler ah, PSDataSet ds,
      List logins, Hashtable connKeys, PSBackEndDataTank backEnds,
      PSCollection beTables, PSDataMapper cols, PSDataSelector sel
      )
      throws java.sql.SQLException,
         PSExtensionException, PSNotFoundException
   {
      /* **********
       * VALIDATION
       * ********** */
      // get the joins, which define what we'll be doing
      PSCollection joins = backEnds.getJoins();
      int joinCount = 0;
      if (joins != null)
         joinCount = joins.size();
      else
         joinCount = 0;

      // make sure we really have joins!
      if (joinCount == 0) {
         throw new IllegalArgumentException("sql builder no joins");
      }

      // make sure we have the appropriate joins.
      PSJoinTree jTree = validateJoins(beTables, joins);

      /* **********
       * PREP
       * ********** */

      /* build a map of all table objects through their alias which
       * contains the table's statistics
       * (includes PSBackEndTable object and PSIndexStatistcs[])
       */
      int numTables = beTables.size();
      Map tmdMap = new HashMap(numTables);

      for (int i = 0; i < numTables; i++)
      {
         // get the current table object
         PSBackEndTable curTable = (PSBackEndTable)beTables.get(i);

         if (null != tmdMap.get(curTable.getAlias()))
            continue;

         // create the meta data for this table
         PSBackEndLogin login = (PSBackEndLogin)logins.get(
            ((Integer)connKeys.get(curTable.getServerKey())).intValue());
         PSDatabaseMetaData dmdCur = getCachedDatabaseMetaData(login);
         PSMetaDataCache.loadConnectionDetail(curTable);
         PSTableMetaData tmdCur = dmdCur.getTableMetaData(
            curTable.getConnectionDetail().getOrigin(), curTable.getTable());

         // add the meta data to the hash on the table alias
         tmdMap.put(curTable.getAlias(), tmdCur);
      }

      /* keep a hash map of PSSqlQueryBuilder objects so we can add the
       * pieces we need
       *    key=driver:server
       *    value=PSSqlQueryBuilder
       */
      Map builderMaps = createBuilderMaps(joins, jTree);
      PSSqlQueryBuilder curBuilder;
      List heteroJoins = new java.util.ArrayList();

      /* We've created builders for each component, now go through and set
       * the table and join conditions.
       *
       * TODO: maybe we can reorder the joins to an equivalent ordering
       * that puts as many homogenous joins together as possible (because
       * some joins are commutative)
       */
      for (int j = 0; j < joinCount; j++)
      {
         PSBackEndJoin curJoin = (PSBackEndJoin)joins.get(j);

         PSBackEndColumn lCol = curJoin.getLeftColumn();
         PSBackEndTable lTab = lCol.getTable();
         PSSqlQueryBuilder lBuilder = (PSSqlQueryBuilder)builderMaps.get(lTab);

         PSBackEndColumn rCol = curJoin.getRightColumn();
         PSBackEndTable rTab = rCol.getTable();
         PSSqlQueryBuilder rBuilder = (PSSqlQueryBuilder)builderMaps.get(rTab);

         // if the builders are equal, it's a homogeneous join
         if (lBuilder == rBuilder)
         {
            lBuilder.addJoin(curJoin);   // also adds the tables
         }
         else
         {
            // add all the left side info to its builder
            lBuilder.addTable(lTab);                     // add the table
            lBuilder.addHeterogeneousJoinColumn(lCol);   // the joined column
            // sort on this column as well
            lBuilder.addSortedColumn(new PSSortedColumn(lCol, true));

            // and now the right side info to its builder
            rBuilder.addTable(rTab);                     // add the table
            rBuilder.addHeterogeneousJoinColumn(rCol);   // the joined column
            // sort on this column as well
            rBuilder.addSortedColumn(new PSSortedColumn(rCol, true));

            /* since this is cross-DBMS, add it to our list of
             * heterogeneous joins which the PSQueryJoiner must build.
             */
            heteroJoins.add(curJoin);
         }
      }

      /* For each column:
       *  1) Make sure that its name and alias do not conflict with other
       *  column names that will be in the result set. Any conflicts will
       *  be resolved by finding a unique alias for one of the columns.
       *
       *  2) Add the SELECT column(s) to the query builders.
       */
      int colSize = cols.size();
      IPSBackEndMapping curMap;
      PSBackEndColumn curCol;
      PSDataMapping map;

      // a map from column name/alias to table
      HashMap taken = new HashMap(colSize);

      for (int i = 0; i < colSize; i++)
      {
         map = (PSDataMapping)cols.get(i);
         curMap = map.getBackEndMapping();
         if (curMap instanceof PSExtensionCall)
         {
            addSelectColumn((PSExtensionCall)curMap, taken, builderMaps);

         } // Only if it's a back-end column!  Bug Id: Rx-99-11-0023
         else if (curMap instanceof PSBackEndColumn)
         {
            curCol = (PSBackEndColumn)curMap;
            addSelectColumn(curCol, taken, builderMaps);
         }

         PSCollection conds = map.getConditionals();
         if ((conds != null) && (conds.size() != 0))
         {
            // go through the conditions to add any back-end columns
            for (int j = 0; j < conds.size(); j++)
            {
               PSConditional cond = (PSConditional)conds.get(j);
               IPSReplacementValue val = cond.getVariable();

               if (val instanceof PSFunctionCall)
               {
                  addSelectColumn((PSFunctionCall)val, taken, builderMaps);
               }
               else if (val instanceof PSBackEndColumn)
               {
                  curCol = (PSBackEndColumn)val;
                  addSelectColumn(curCol, taken, builderMaps);
               }
               if (!cond.isUnary())
               {
                  val = cond.getValue();

                  if (val instanceof PSFunctionCall)
                  {
                     addSelectColumn((PSFunctionCall)val, taken, builderMaps);
                  }
                  else if (val instanceof PSBackEndColumn)
                  {
                     curCol = (PSBackEndColumn)val;
                     addSelectColumn(curCol, taken, builderMaps);
                  }
               }
            }
         }
      }


      /* For each where clause in the data selector:
       *  1) Get the variable in the where clause. Get the table associated
       *  with the where variable. Get the query builder associated with that
       *  table, and add the where clause to that query builder. This means
       *  that before any joining takes place, we filter out the rows from
       *  each relation that are not selected by the data selector.
       */
      PSCollection clauses = sel.getWhereClauses();
      int clausesSize;
      if (clauses == null)
         clausesSize = 0;
      else
         clausesSize = clauses.size();
      for (int i = 0; i < clausesSize; i++)
      {
         PSWhereClause curWhere = (PSWhereClause)clauses.get(i);
         IPSReplacementValue repl = curWhere.getVariable();
         if (!((repl instanceof PSBackEndColumn) ||
            (repl instanceof PSFunctionCall)))
         {
            throw new IllegalArgumentException("where var must be backend col" +
               repl.getValueDisplayText() );
         }

         // now add the WHERE to the driver mapping
         if (repl instanceof PSBackEndColumn)
         {
            curBuilder = (PSSqlQueryBuilder)builderMaps.get(
               ((PSBackEndColumn)repl).getTable());
            curBuilder.addWhereClause(curWhere);
         }
         else if (repl instanceof PSFunctionCall)
         {
            PSFunctionParamValue[] params =
               ((PSFunctionCall)repl).getParamValues();

            for (int k = 0; k < params.length; k++)
            {
               if ((null != params[k]) && (params[k].isBackEndColumn()))
               {
                  PSBackEndColumn bcol = (PSBackEndColumn)params[k].getValue();
                  curBuilder = (PSSqlQueryBuilder)
                     builderMaps.get(bcol.getTable());
                  curBuilder.addWhereClause(curWhere);
                  break;
               }
            }
         }
      }

      /* use the data selector to add the sort constraints
       *
       * if it's heterogeneous, we may cause the sort to be slightly
       * out of order. In particular, we make the join key the primary
       * sort. This may be acceptable. If not, we need a way to re-sort
       * our generated result set after the fact.
       */
      PSCollection sortedColumns = sel.getSortedColumns();
      int size = (sortedColumns == null) ? 0 : sortedColumns.size();
      if (size == 0) {
         sortedColumns = getSortColumnsForXmlCollapsing(ah, ds);
         size = (sortedColumns == null) ? 0 : sortedColumns.size();
      }
      for (int i = 0; i < size; i++)
      {
         PSSortedColumn sortCol = (PSSortedColumn)sortedColumns.get(i);
         // find the builder for this column and add it to the select list
         curBuilder = (PSSqlQueryBuilder)builderMaps.get(sortCol.getTable());
         curBuilder.addSortedColumn(sortCol);
      }

      /* **********
       * BUILD
       * ********** */

      /* Create homogeneous joins wherever possible
       * and single table joins where we can't. Then use the
       * PSQueryJoiner to merge all the result sets
       */

      // we join two result sets at a time so steps <= (joins * 2)+1
      int joinSize = heteroJoins.size();
      ArrayList steps = new ArrayList((joinSize * 2) + 1);

      if (joinSize > 0) {   // got some heterogeneous joins goin' on over here
         PSQueryJoiner joiner;
         String[] lCols = null;
         String[] lJoinCols = null;
         String[] rCols;
         String[] rJoinCols;
         for (int i = 0; i < joinSize; i++)
         {
            /* get the join first, then get the left and right sides to
             * prepare the appropriate SELECT statements
             */
            PSBackEndJoin curJoin = (PSBackEndJoin)heteroJoins.get(i);

            /* for joins other than the first, we already have the left side
             * info. Therefore, we need to get the right side info only
             */
            if (i == 0) {
               curBuilder = (PSSqlQueryBuilder)builderMaps.get(
                  curJoin.getLeftColumn().getTable());

               lCols = curBuilder.getSelectColumnArray();
               lJoinCols = curBuilder.getJoinOnlyColumns();

               // add the SELECT to the execution plan
               steps.add(curBuilder.generate(logins, connKeys));
            }

            /* Check for homo/formula situation, so we can get the next
               login for the next generate */
            boolean getNextConnection = false;
            Object driverServerCombo = null;
            Integer originalConnectionIndex = null;

            if (curJoin.getTranslator() != null)
            {
               PSBackEndColumn left  = (PSBackEndColumn) curJoin.getLeftColumn();
               PSBackEndColumn right = (PSBackEndColumn) curJoin.getRightColumn();

               driverServerCombo = left.getTable().getServerKey();
               if (driverServerCombo.equals(right.getTable().getServerKey()))
               {
                  getNextConnection = true;
                  originalConnectionIndex = (Integer) connKeys.get(driverServerCombo);
                  connKeys.put(driverServerCombo, new Integer(originalConnectionIndex.intValue()+1));
               }
            }

            // now we need to get the right side info
            curBuilder = (PSSqlQueryBuilder)builderMaps.get(
               curJoin.getRightColumn().getTable());

            /* for each pair of result sets, we must create a
             * PSQueryJoiner subclass object to do the merge
             *
             * we'll check the stats on the table. if it's more optimal
             * to use indexed lookup, we'll use the PSIndexedLookupJoiner
             * otherwise we'll use the PSSortedResultJoiner.
             */

            // need the right side tables column's regardless
            rCols = curBuilder.getSelectColumnArray();
            rJoinCols = curBuilder.getJoinOnlyColumns();

            if (isIndexedLookupOptimal(
               ah, ds, curJoin, tmdMap, builderMaps, sel, true))
            {
               // build the joiner, which is the only step in the plan

               // add the lookup key and have it reference the left side's
               // result set column
               PSWhereClause curWhere = new PSWhereClause(
                  curJoin.getRightColumn(),
                  PSWhereClause.OPTYPE_EQUALS,
                  new PSHtmlParameter("$$E2IndexedJoiner_"
                     + curJoin.getLeftColumn().getValueText()),
                  false);
               curBuilder.addWhereClause(curWhere);

               // create the query statement, which is sent in to the
               // indexed lookup engine
               joiner = new PSIndexedLookupJoiner(
                  ah, curJoin, lCols, lJoinCols, rCols, rJoinCols,
                  curBuilder.generate(logins, connKeys),
                  estimateJoinCardinality(ah, ds, curJoin, clauses, tmdMap));
            }
            else // sort and merge the results ourself
            {
               // add the SELECT to the execution plan
               steps.add(curBuilder.generate(logins, connKeys));

               // then build the joiner, which is also added to the plan
               joiner = new PSSortedResultJoiner(ah, curJoin,
                  lCols, lJoinCols, rCols, rJoinCols,
                  estimateJoinCardinality(ah, ds, curJoin, clauses, tmdMap));
            }

            if (getNextConnection)  // Return the connection map's state
            {
               connKeys.put(driverServerCombo, originalConnectionIndex);
            }

            steps.add(joiner);   // add the joiner we build
            lCols = joiner.getColumnNames();   // and save its cols as the left side

            // the joiner has already stripped all join-only cols, so we
            // can set the left side omit cols to an empty array
            lJoinCols = new String[0];
         }
      }
      else {   // single homogeneous join
         // get the builder from the first table (since they're all in
         // the same DBMS, that works fine)
         curBuilder = (PSSqlQueryBuilder)builderMaps.get((PSBackEndTable)beTables.get(0));

         // set if SELECT DISTINCT should be used
         curBuilder.setUnique(sel.isSelectUnique());

         // add the SELECT to the execution plan
         steps.add(curBuilder.generate(logins, connKeys));
      }

      IPSExecutionStep[] retSteps = new IPSExecutionStep[steps.size()];
      steps.toArray(retSteps);
      return retSteps;
   }

   /**
    * Adds the specified backend column to the SELECT column list.
    *
    * @param curCol the backend column to add to the SELECT column list,
    * assumed not <code>null</code>
    *
    * @param taken a map from column name/alias to table, assumed not
    * <code>null</code>
    *
    * @param builderMaps map containing the back end table
    * (<code>PSBackEndTable</code>) as key and sql query builder
    * (<code>PSSqlQueryBuilder</code>) as value, assumed not <code>null</code>
    */
   private static void addSelectColumn(PSBackEndColumn curCol, HashMap taken,
      Map builderMaps)
   {
      // resolve name collisions
      try
      {
         uniquifyAlias(curCol, taken);
      }
      catch (IllegalArgumentException ex)
      {
         throw new IllegalArgumentException(ex.getLocalizedMessage());
      }

      // find the builder for this column and add it to the select list
      PSSqlQueryBuilder curBuilder = (PSSqlQueryBuilder)
         builderMaps.get(curCol.getTable());
      curBuilder.addSelectColumn(curCol);
   }

   /**
    * Adds all the backend column specified as a parameter of the specified
    * function call <code>fnCall</code> to the SELECT column list.
    *
    * @param fnCall the function call which contains backend columns as its
    * parameter which should be added to the SELECT column list,
    * assumed not <code>null</code>
    *
    * @param taken a map from column name/alias to table, assumed not
    * <code>null</code>
    *
    * @param builderMaps map containing the back end table
    * (<code>PSBackEndTable</code>) as key and sql query builder
    * (<code>PSSqlQueryBuilder</code>) as value, assumed not <code>null</code>
    */
   private static void addSelectColumn(PSFunctionCall fnCall, HashMap taken,
      Map builderMaps)
   {
      PSFunctionParamValue[] params = fnCall.getParamValues();

      for (int k = 0; k < params.length; k++)
      {
         if ((null != params[k]) && (params[k].isBackEndColumn()))
         {
            PSBackEndColumn curCol = (PSBackEndColumn)params[k].getValue();
            addSelectColumn(curCol, taken, builderMaps);
         }
      }
   }

   /**
    * Adds all the backend column specified as a parameter of the specified
    * extension call <code>extCall</code> to the SELECT column list.
    *
    * @param extCall the extension call which contains backend columns as its
    * parameter which should be added to the SELECT column list,
    * assumed not <code>null</code>
    *
    * @param taken a map from column name/alias to table, assumed not
    * <code>null</code>
    *
    * @param builderMaps map containing the back end table
    * (<code>PSBackEndTable</code>) as key and sql query builder
    * (<code>PSSqlQueryBuilder</code>) as value, assumed not <code>null</code>
    */
   private static void addSelectColumn(PSExtensionCall extCall, HashMap taken,
      Map builderMaps)
   {
      PSExtensionParamValue[] params = extCall.getParamValues();
      for (int j = 0; j < params.length; j++)
      {
         if ((null != params[j]) && (params[j].isBackEndColumn()))
         {
            PSBackEndColumn curCol = (PSBackEndColumn)params[j].getValue();
            addSelectColumn(curCol, taken, builderMaps);
         }
      }
   }

   /**
    * Finds a unique alias for the column among the other aliases
    * contained in the map, and sets the column's alias to the
    * unique value. The first alias tried will always be the column's
    * set alias (if defined) or the column's name itself.
    *
    * @version 1.34 1999/09/03
    *
    * @param   col
    * @param   taken
    */
   private static void uniquifyAlias(PSBackEndColumn col, HashMap taken)
   {
      if (col.getAlias() == null || col.getAlias().length() == 0)
      {
         col.setAlias(col.getColumn());
      }

      /* Need to make this case insensitive, so lowercasing map keys,
         bug id: Rx-99-11-0026 */
      String colKey = col.getAlias().toLowerCase();
      PSBackEndTable tab = (PSBackEndTable)taken.get(colKey);
      if (tab != null)
      {
         for (int i = 1; tab != null; i++)
         {
            col.setAlias(colKey + i);
            tab = (PSBackEndTable)taken.get(col.getAlias());
         }

         colKey = col.getAlias();   // already lowercase at this point
      }

      // when we get here, the value is guaranteed to be unique
      taken.put(colKey, col.getTable());
   }

   /* we need to build the table mappers taking into consideration how
    * to accomplish the joins. The homogeneous join case is easy, but when
    * we have homogeneous joins using translators, we need to treat them
    * as if they were heterogeneous as well.
    */
   private static java.util.Map createBuilderMaps(
      PSCollection joins, PSJoinTree jTree)
   {
      /* when building the builder maps, we see which tables exist on the
       * same back-ends, and if there are any conditions preventing them
       * from a unified builder. We also need to look at how they're joined,
       * as we can't perform a homogeneous join if it's the heterogeneous
       * join which allows the tables to be combined. There's also that
       * annoying translator we must support.
       */

      // this is where we'll store the builders with table as key
      // and builder as value
      java.util.HashMap builderMaps = new java.util.HashMap();

      int size = joins.size();
      for (int i = 0; i < size; i++)
      {
         PSBackEndJoin join = (PSBackEndJoin)joins.get(i);

         PSBackEndColumn lCol = join.getLeftColumn();
         PSBackEndTable lTab = lCol.getTable();
         PSSqlQueryBuilder lBuilder = (PSSqlQueryBuilder)builderMaps.get(lTab);

         PSBackEndColumn rCol = join.getRightColumn();
         PSBackEndTable rTab = rCol.getTable();
         PSSqlQueryBuilder rBuilder = (PSSqlQueryBuilder)builderMaps.get(rTab);

         boolean hasTranslator = (join.getTranslator() != null);
         if (!hasTranslator && lTab.getServerKey().equals(rTab.getServerKey()))
         {
            // these are in the same DBMS and we're not using a translator
            // so we can let the DBMS do the join for us

            if ((lBuilder == null) && (rBuilder == null))
            {
               rBuilder = lBuilder = new PSSqlQueryBuilder();
               builderMaps.put(lTab, lBuilder);
               builderMaps.put(rTab, rBuilder);
            }
            else if (lBuilder == null)
            {
               builderMaps.put(lTab, rBuilder);
            }
            else if (rBuilder == null)
            {
               builderMaps.put(rTab, lBuilder);
            }
         }
         else
         {
            if (hasTranslator)
            {
               // in this case, we must detect if these two have been
               // previously joined with each other. If so, we must
               // separate them
               if ((lBuilder != null) && (rBuilder != null) &&
                  (lBuilder == rBuilder))
               {
                  // leave the left builder alone, mark the right side
                  rBuilder = null;
               }
            }

            if (lBuilder == null)
               builderMaps.put(lTab, new PSSqlQueryBuilder());

            if (rBuilder == null)
               builderMaps.put(rTab, new PSSqlQueryBuilder());
         }
      }

      return builderMaps;
   }

   /**
    * Decides whether an index lookup would be optimal for this join.
    *
    * An indexed lookup works like this:
    * For each outer row, the join selects values and forms a key to search
    * in the inner join.
    *
    * STRATEGY:
    *  1) If the query will return all of the data from the
    *  right relation, then even a simple table scan would be better than using
    *  indexed lookup. Therefore, return false. This rule eliminates
    *  full outer joins and right outer joins.
    *
    *  2) If the right side does not have an index, then doing an index lookup
    *  would be impossible.
    *
    *  3) If the join operator is not bounded, then an index would not help
    *  lookup. Only bounded operators (such as =, <, >, <=, and >=) allow the
    *  back end to make use of indices.
    *
    *  Since we are only supporting equi-joins for now, this condition will
    *  always be satisfied. This is because almost all index structures (like
    *  B-trees) can facilitate retrieving ranges of information, but can't
    *  help with LIKE, most negated conditions (including !=), any conditions
    *  involving a transformed indexed column (e.g., where upper(name)='JOHN';
    *  this includes converting from one SQL type to another), and any
    *  conditions using a IS NULL or IS NOT NULL on the indexed column.
    *
    *  4) As an extension to Rule 2: If the lookups are likely to retrieve a
    *  "significant" fraction of the rows in the right relation (such that
    *  the overhead of multiple queries plus the cost of accessing the
    *  right relation indirectly via the index outweighs the cost of
    *  retrieving the entire right relation and weeding it out), then return
    *  false. This depends on the costs of network traffic (the cost of
    *  sending many queries, the cost of sending unneeded data from the
    *  right relation { can we estimate the percent of wasted data ? },
    *  and the resources it takes the E2 server to do the joining. To do
    *  this better, we might want to dynamically tweak the relative costs of
    *  all these resources.
    *
    *  5) If there are no indices on the right column by itself, but
    *  there is a concatenated index of which the right column is a
    *  member, then we can only use that index if we can supply values
    *  from the left relation (or somewhere else) for all of the columns of
    *  the concatenated index leading up to the right column. Since here we
    *  are looking only at one column from each table, this test collapses into
    *  testing that if we have a concatenated index of which the right
    *  column is a member, the right column has to be the leading (i.e.,
    *  most significant) member of that index for that index to be useable.
    *
    * @return true if an indexed lookup from the left table into the right
    * table would be optimal.
    *
    * TODO: For inner joins, allow swapping of the left and right relations
    * to yield an equivalent join with the smaller relation on the left,
    * (providing the right column is still indexed). Then re-evaluate the
    * optimality (with a recursive call?).
    */
   private static boolean isIndexedLookupOptimal(
      PSApplicationHandler ah, PSDataSet ds, PSBackEndJoin join,
      Map tmdMap, Map builderMap, PSDataSelector sel, boolean allowReordering)
      throws SQLException
   {
      // if this is a full outer join, then there is no point doing an indexed
      // lookup because all tuples will be preserved. Therefore, it would be
      // faster to simply get all the tuples from both sides.
      if (join.isFullOuterJoin())
      {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_NO_INDEX_LOOKUP_FULL_OUTER,
               new Object[] { ah.getName(), ds.getName() }));
         return false;
      }

      // similarly, if all tuples from the right relation will be preserved, there
      // is no point in firing separate queries against the right relation
      if (join.isRightOuterJoin())
      {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_NO_INDEX_LOOKUP_RIGHT_OUTER,
               new Object[] { ah.getName(), ds.getName() }));
         return false;
      }

      // get the index information on the right table. if we have a useful index on
      // the join column, then we might be optimal. if we have no indices, then
      // we can not be optimal
      PSBackEndColumn rightCol = join.getRightColumn();
      PSBackEndTable rightTable = rightCol.getTable();

      PSTableMetaData rightTableMetaData
         = (PSTableMetaData)tmdMap.get(rightTable.getAlias());

      // if no info on the right column, maybe the left column has an
      // index, so we can swap the two (not supported)
      if (rightTableMetaData == null)
      {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.CANNOT_LOAD_TABLE_META,
               new Object[] { ah.getName(), ds.getName(),
                  rightTable.getAlias() }));
         return false; // TODO: swapping not supported
      }

      // get the stats for the right table
      PSTableStatistics rightTableStats = rightTableMetaData.getStatistics();

      // get the index stats for the right relation
      PSIndexStatistics[] rightIndexStats = rightTableMetaData.getIndexStatistics();

      // if there are no index stats, then return false
      if (rightIndexStats == null || rightIndexStats.length == 0)
      {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.CANNOT_LOAD_INDEX_META,
               new Object[] { ah.getName(), ds.getName(),
                  rightTable.getAlias() }));
         return false;   // TODO: support swapping
      }

      String rightColName = rightCol.getColumn();

      // analyze the indices of the right relation, collecting potentially
      // useful information on this single pass through the arrray
      boolean hasUsefulIndex = false;
      boolean hasRowCount = false;

      for (int i = 0;
         i < rightIndexStats.length
            && !hasUsefulIndex
            && !hasRowCount;
         i++)
      {
         PSIndexStatistics curIdxStat = rightIndexStats[i];

         // if this is a real index (not table data)
         if (DatabaseMetaData.tableIndexStatistic != curIdxStat.getIndexType())
         {
            String[] idxCols = curIdxStat.getSortedColumns();
            if (idxCols != null && idxCols.length > 0)
            {
               if (idxCols[0].equals(rightColName))
               {
                  // this index sorts first by our column
                  hasUsefulIndex = true;
               }
            }
         }
      }

      if (!hasUsefulIndex)
      {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_NO_INDEX_LOOKUP_INDICES,
               new Object[] { ah.getName(), ds.getName(),
                  rightTable.getAlias() } ));
         return false; // no index that we could use
      }

      // get the index stats for the left relation

      PSBackEndColumn leftCol = join.getLeftColumn();
      PSBackEndTable leftTable = leftCol.getTable();
      PSTableMetaData leftTableMetaData
         = (PSTableMetaData)tmdMap.get(leftTable.getAlias());
      PSTableStatistics leftTableStats = leftTableMetaData.getStatistics();
      PSIndexStatistics[] leftIndexStats = leftTableStats.getIndexStatistics();

      // if there are no index stats, then return false
      if (leftIndexStats == null || leftIndexStats.length == 0)
      {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.CANNOT_LOAD_INDEX_META,
               new Object[] { ah.getName(), ds.getName(),
                  leftTable.getAlias() }));
         return false;
      }

      int joinCardinality = estimateJoinCardinality(
         ah, ds, join, sel.getWhereClauses(), leftTableStats, rightTableStats);

      if (joinCardinality < 0) {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_JOIN_CARDINALITY_NOT_FOUND,
               new Object[] { ah.getName(), ds.getName(),
                  leftTable.getAlias(), rightTable.getAlias() }));
         return false;
      }

      ah.getLogHandler().write(
         new com.percussion.log.PSLogExecutionPlan(
            ah.getId(),
            IPSDataErrors.EXEC_PLAN_LOG_JOIN_CARDINALITY,
            new Object[] { ah.getName(), ds.getName(),
               leftTable.getAlias(), rightTable.getAlias(),
               String.valueOf(joinCardinality) }));

      // verify an indexed lookup won't be too much work
      //
      // In particular, performing 50 queries on a table with 100 rows
      // is quite likely to be less efficient than performing one query
      // to get the 100 rows and weed out the bad ones.
      //
      // The trade offs are:
      //      - network load:
      //            - making many requests over the network
      //                  vs.
      //            - bringing over a "large" result set
      //      - server resources
      //            - SQL Server
      //                  vs.
      //            - E2 Server
      double percentRowsSelected
         = (double)joinCardinality / (double)rightTableStats.getCardinality();

      // we'll assume that anything under 20% is efficient
      if (percentRowsSelected > 0.2) {
         return false;
      }

      return true;
   }

   private static int estimateJoinCardinality(
      PSApplicationHandler ah, PSDataSet ds,
      PSBackEndJoin join, PSCollection whereClauses, Map tmdMap)
      throws SQLException
   {
      PSBackEndColumn leftCol = join.getLeftColumn();
      PSBackEndTable leftTable = leftCol.getTable();

      PSBackEndColumn rightCol = join.getRightColumn();
      PSBackEndTable rightTable = rightCol.getTable();

      try {
         PSTableMetaData leftTableMetaData
            = (PSTableMetaData)tmdMap.get(leftTable.getAlias());
         PSTableStatistics leftTableStats = leftTableMetaData.getStatistics();

         PSTableMetaData rightTableMetaData
            = (PSTableMetaData)tmdMap.get(rightTable.getAlias());
         PSTableStatistics rightTableStats = rightTableMetaData.getStatistics();

         return estimateJoinCardinality(
            ah, ds, join, whereClauses, leftTableStats, rightTableStats);
      } catch (Throwable t) {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_JOIN_CARDINALITY_EXCEPTION,
               new Object[] { ah.getName(), ds.getName(),
                  leftTable.getAlias(), rightTable.getAlias(), t.toString() }));
         return -1;
      }
   }

   /**
    * @author chadloder
    *
    * Estimates the cardinality of a join, given the table statistics for
    * the left and right tables of that join. The estimation heuristics
    * are derived from "Database System Concepts", Korth et al., 2nd ed.,
    * Chapter 9.3.
    *
    * @param   join   The join whose cardinality is to be estimated
    *
    * @param   leftStats   The statistics for the left
    *
    * @param   rightStats   The statistics for the right table
    *
    * @return int The estimated cardinality (number of rows) of the join, or
    * -1 if the cardinality could not be estimated.
    *
    * @since 1.10 1999/4/29
    *
    */
   private static int estimateJoinCardinality(
      PSApplicationHandler ah,
      PSDataSet ds,
      PSBackEndJoin join,
      PSCollection whereClauses,
      PSTableStatistics leftStats,
      PSTableStatistics rightStats )
      throws SQLException
   {
      PSBackEndColumn leftCol = join.getLeftColumn();
      PSBackEndTable leftTable = leftCol.getTable();

      PSBackEndColumn rightCol = join.getRightColumn();
      PSBackEndTable rightTable = rightCol.getTable();

      String rightColName = rightCol.getColumn();
      String leftColName = leftCol.getColumn();

      int leftCardinality = leftStats.getCardinality();
      int rightCardinality = rightStats.getCardinality();

      ah.getLogHandler().write(
         new com.percussion.log.PSLogExecutionPlan(
            ah.getId(),
            IPSDataErrors.EXEC_PLAN_LOG_TABLE_CARDINALITY,
            new Object[] { ah.getName(), ds.getName(),
               leftTable.getAlias(), String.valueOf(leftCardinality) }));

      ah.getLogHandler().write(
         new com.percussion.log.PSLogExecutionPlan(
            ah.getId(),
            IPSDataErrors.EXEC_PLAN_LOG_TABLE_CARDINALITY,
            new Object[] { ah.getName(), ds.getName(),
               rightTable.getAlias(), String.valueOf(rightCardinality) }));

      int leftUniqueCount = leftStats.getDistinctRows(leftColName);
      int rightUniqueCount = rightStats.getDistinctRows(rightColName);

      if (join.isFullOuterJoin())
         return leftCardinality * rightCardinality;
      if (join.isLeftOuterJoin())
         return leftCardinality;
      if (join.isRightOuterJoin())
         return rightCardinality;

      ah.getLogHandler().write(
         new com.percussion.log.PSLogExecutionPlan(
            ah.getId(),
            IPSDataErrors.EXEC_PLAN_LOG_UNIQUE_ROW_ESTIMATE,
            new Object[] { ah.getName(), ds.getName(),
               leftTable.getAlias(), String.valueOf(leftUniqueCount) }));

      ah.getLogHandler().write(
         new com.percussion.log.PSLogExecutionPlan(
            ah.getId(),
            IPSDataErrors.EXEC_PLAN_LOG_UNIQUE_ROW_ESTIMATE,
            new Object[] { ah.getName(), ds.getName(),
               rightTable.getAlias(), String.valueOf(rightUniqueCount) }));

      if (leftUniqueCount == 0 || rightUniqueCount == 0)
      {
         return 0;
      }
      else if (leftUniqueCount > 0 && rightUniqueCount > 0)
      {
         // estimate the cardinality of the full cartesian product
         int cartProdCardinality = leftCardinality * rightCardinality;

         double selectivity = estimateJoinedSelectivity(
            ah, ds, leftCol, rightCol, whereClauses, leftStats, rightStats);

         double estimate = (double)cartProdCardinality * selectivity;
         if (estimate < 1.0)
            estimate = 1.0;

         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_LOG_SELECTIVITY,
               new Object[] { ah.getName(), ds.getName(),
                  leftTable.getAlias(), rightTable.getAlias(),
                  String.valueOf(estimate) }));

         return Math.round((float)estimate);

         //int firstEstimate = cartProdCardinality / leftUniqueCount;
         //int secondEstimate = cartProdCardinality / rightUniqueCount;
         //return (Math.min(firstEstimate, secondEstimate));
      }

      return -1;
   }

   /**
    * @author chadloder
    *
    * Estimates the selectivity for the given table under the given select
    * statements. The selectivity is defined as the fraction of rows from
    * a relation that will be selected under the where conditions which
    * affect this column. Multiply a selectivity by a cardinality to get
    * the number of rows that will be selected from the relation under
    * the selection.
    *
    * @param   tab   The table whose selectivity is to be evaluated under
    * the where clauses.
    *
    * @param   whereClauses   The where clauses. All where clauses not
    * related to columns in this table will be ignored.
    *
    * @param   stats   The table statistics for the given table.
    *
    * @return double The estimated selectivity, or -1.0 if the
    * selectivity could not be determined.
    *
    * @since 1.10 1999/4/29
    *
    */
   private static double estimateJoinedSelectivity(
      PSApplicationHandler ah, PSDataSet ds,
      PSBackEndColumn leftCol, PSBackEndColumn rightCol,
      PSCollection whereClauses, PSTableStatistics leftStats, PSTableStatistics rightStats)
      throws java.sql.SQLException
   {
      PSWhereClause joinClause = null;
      try {
         joinClause = new PSWhereClause(
            leftCol, PSConditional.OPTYPE_EQUALS, rightCol, false);
         if (whereClauses == null)
            whereClauses = new PSCollection(joinClause.getClass());
         whereClauses.add(joinClause);
      } catch (IllegalArgumentException e) {
         ah.getLogHandler().write(
            new com.percussion.log.PSLogExecutionPlan(
               ah.getId(),
               IPSDataErrors.EXEC_PLAN_JOIN_SELECTIVITY_EXCEPTION,
               new Object[] { ah.getName(), ds.getName(),
                  leftCol.getTable().getAlias(),
                  rightCol.getTable().getAlias(), e.getLocalizedMessage() }));
      }

      int clausesSize;
      if (whereClauses == null)
         clausesSize = 0;
      else
         clausesSize = whereClauses.size();

      // List pkeyCols = stats.getPrimaryKeyColumns();

      double selectivity = 1.0;

      for (int i = 0; i < clausesSize; i++)
      {
         PSWhereClause curWhere = (PSWhereClause)whereClauses.get(i);
         IPSReplacementValue var = curWhere.getVariable();
         if (!(var instanceof com.percussion.design.objectstore.PSBackEndColumn))
         {
            // can't check cardinality of non-SQL entities
            // what weight should we apply??!!
            continue;
         }

         int valueCardinality = 1;
         IPSReplacementValue val = curWhere.getValue();
         if (val instanceof PSBackEndColumn)
         {
            PSBackEndColumn valCol = (PSBackEndColumn)val;
            String valColName = valCol.getColumn();
            String valTabAlias = valCol.getTable().getAlias();
            if (valTabAlias.equals(leftCol.getTable().getAlias()))
               valueCardinality = leftStats.getDistinctRows(valColName);
            else if (valTabAlias.equals(valCol.getTable().getAlias()))
               valueCardinality = rightStats.getDistinctRows(valColName);
         }

         PSBackEndColumn beCol = (PSBackEndColumn)var;
         String columnName = beCol.getColumn();
         String tableAlias = beCol.getTable().getAlias();

         int rowCount;
         if (tableAlias.equals(leftCol.getTable().getAlias()))
            rowCount = leftStats.getDistinctRows(columnName);
         else if (tableAlias.equals(rightCol.getTable().getAlias()))
            rowCount = rightStats.getDistinctRows(columnName);
         else   // this isn't part of this join so we can't check it
            continue;

         String op = curWhere.getOperator();

         // assume normal distribution of values in database
         // this also assumes that each clause is independent (which allows
         // us to multiply AND-linked clause selectivities together. this
         // is a dangerous assumption. a better approach might be to take
         // the minimum of all the selectivities in and AND-linked list
         // and use that minimum as the combined selectivity)
         // TODO: improve this (for constants, etc.)
         if(op.equalsIgnoreCase(PSConditional.OPTYPE_EQUALS) ||
            op.equalsIgnoreCase(PSConditional.OPTYPE_ISNULL))
            selectivity *= 1.0 / Math.max(rowCount, valueCardinality);
         else if( op.equalsIgnoreCase(PSConditional.OPTYPE_NOTEQUALS) ||
                  op.equalsIgnoreCase(PSConditional.OPTYPE_ISNOTNULL))
            selectivity *= 1.0 - (1.0 / (double)Math.max(rowCount, valueCardinality));
         else if(   op.equalsIgnoreCase(PSConditional.OPTYPE_LESSTHAN) ||
                  op.equalsIgnoreCase(PSConditional.OPTYPE_LESSTHANOREQUALS) ||
                  op.equalsIgnoreCase(PSConditional.OPTYPE_GREATERTHAN) ||
                  op.equalsIgnoreCase(PSConditional.OPTYPE_GREATERTHANOREQUALS) )
            selectivity *= 0.33;
         else if(op.equalsIgnoreCase(PSConditional.OPTYPE_BETWEEN))
            selectivity *= 0.25;
         else if(op.equalsIgnoreCase(PSConditional.OPTYPE_NOTBETWEEN))
            selectivity *= 0.75;
         else if(op.equalsIgnoreCase(PSConditional.OPTYPE_LIKE))
            selectivity *= 0.1;
         else if(op.equalsIgnoreCase(PSConditional.OPTYPE_NOTLIKE))
            selectivity *= 0.9;
      }

      if (joinClause != null)
         whereClauses.remove(joinClause);

      return selectivity;
   }

   /**
    * Get the collapsable columns associated with this DTD. Collapsable
    * columns are defined as columns which do not repeat. This is used
    * primarily to determine if columns must be sorted.
    *
    * @param   ah      the application handler containing the DTD definition,
    * never <code>null</code>
    *
    * @param   ds      the data set containing the DTD definition and
    *                  data mappings to check, never <code>null</code>
    *
    * @return   an array of back-end column objects to collapse on
    */
   public static PSCollection getSortColumnsForXmlCollapsing(
      PSApplicationHandler ah, PSDataSet ds)
   {
      if (ah == null)
      {
         throw new IllegalArgumentException("ah must never be null");
      }
      if (ds == null)
      {
         throw new IllegalArgumentException("ds must never be null");
      }
      /* we need to use the DTD to determine the doc type, elements, etc. */
      PSPageDataTank pageDT = ds.getPageDataTank();
      if (pageDT == null)
         return null;

      java.net.URL dtd = pageDT.getSchemaSource();
      if (dtd == null)
         return null;

      try {
         dtd = ah.getLocalizedURL(dtd);
      } catch (java.net.MalformedURLException e) {
         throw new IllegalArgumentException("page tank bad schema URL" +
            ah.getName() + " " + ds.getName());
      }

      boolean haveNonCollapsed = false;
      PSCollection collapseNodes = new PSCollection(
         com.percussion.design.objectstore.PSSortedColumn.class);

      PSDtdTree tree;
      try {
         tree = new PSDtdTree(dtd);
      } catch (com.percussion.design.catalog.PSCatalogException e) {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      /* now copy over the column/xml field mapping info */
      PSPipe pipe = ds.getPipe();
      PSDataMapper maps = pipe.getDataMapper();
      for (int j = 0; j < maps.size(); j++) {
         PSDataMapping map = (PSDataMapping)maps.get(j);
         String xmlField = map.getXmlField();
         String[] nodeParts = parseXmlNodeName(xmlField);

         String parent = (nodeParts.length >= 2) ?
            nodeParts[nodeParts.length - 2] : "";
         String node = nodeParts[nodeParts.length-1];

         int occurs = tree.getMaxOccurrenceSetting(node, parent);
         if ((occurs == PSDtdNode.OCCURS_OPTIONAL) ||
            (occurs == PSDtdNode.OCCURS_ONCE))
         {
            IPSBackEndMapping colMap = map.getBackEndMapping();
            if (colMap instanceof com.percussion.design.objectstore.PSBackEndColumn)
            {
               addBackEndColumnAsSortedColumn(
                  collapseNodes, (PSBackEndColumn)colMap);
            }
            else if (colMap instanceof com.percussion.design.objectstore.PSExtensionCall)
            {
               PSExtensionCall call = (PSExtensionCall)colMap;
               PSExtensionParamValue[] values = call.getParamValues();
               int size = (values == null) ? 0 : values.length;
               for (int i = 0; i < size; i++) {
                  if (values[i].isBackEndColumn())
                     addBackEndColumnAsSortedColumn(
                        collapseNodes, (PSBackEndColumn)values[i].getValue());
               }
            }
         }
         else   // anything else can occur multiple times (not collapsed)
            haveNonCollapsed = true;
      }

      // if there's nothing to collapse on, reset the collection
      if (!haveNonCollapsed) {
         collapseNodes.clear();
         collapseNodes = null;
      }

      return collapseNodes;
   }

   private static void addBackEndColumnAsSortedColumn(
      PSCollection sortedColumns, PSBackEndColumn beCol)
   {
      PSSortedColumn sortedCol = new PSSortedColumn(beCol, true);
      if (!sortedColumns.contains(sortedCol))
         sortedColumns.add(sortedCol);
   }

   private static String[] parseXmlNodeName(String xmlField)
   {
      ArrayList xmlStruct = new ArrayList();
      int pos;
      int lastPos = 0;
      String name;
      for (; (pos = xmlField.indexOf('/', lastPos)) != -1; ) {
         /* If the first char is a slash char, skip it from the name.
          * For instance, /Manufacturer and Manufacturer should both
          * resolve to Manufacturer
          */
         if (pos != 0) {
            name = xmlField.substring(lastPos, pos);
            if (!xmlStruct.contains(name))
               xmlStruct.add(name);
         }

         lastPos = pos + 1;
      }

      name = xmlField.substring(lastPos);
      if (!xmlStruct.contains(name))
         xmlStruct.add(name);

      String[] nodeNames = new String[xmlStruct.size()];
      xmlStruct.toArray(nodeNames);
      return nodeNames;
   }

   /**
    * Verify the joins are properly defined.
    *
    *  - make sure a join exists for each table
    *  - make sure all tables are joined through those joins
    *      eg: a.c1 = b.c1 AND c.c1 = d.c1 is not enough as we
    *            need table a or b to join with table c or d
    */
   static PSJoinTree validateJoins(
      PSCollection tables, PSCollection joins)
   {
      // convert the collections so we call just one method
      java.util.List tableList
         = (tables == null) ? null: tables.subList(0, tables.size());

      java.util.List joinList
         = (joins == null) ? null : joins.subList(0, joins.size());

      return validateJoins(tableList, joinList);
   }


   /**
    * Verify the joins are properly defined.
    *
    *  - make sure a join exists for each table
    *  - make sure all tables are joined through those joins
    *      eg: a.c1 = b.c1 AND c.c1 = d.c1 is not enough as we
    *            need table a or b to join with table c or d
    */
   static PSJoinTree validateJoins(
      java.util.List tables, java.util.List joins)
   {
      // if there's 0 or 1 tables, we clearly have no join issues
      int tableCount = (tables == null) ? 0 : tables.size();
      if (tableCount <= 1)
         return null;

      // since we have at least 2 tables, make sure we have at least
      // one join
      int joinCount = (joins == null) ? 0 : joins.size();
      if (joinCount == 0) {
         throw new IllegalArgumentException("sql builder no joins");
      }

      // this class contains a tree with the path from table to table
      // we can use it to determine if everything's joined
      PSJoinTree jTree = new PSJoinTree(joins);

      // see if we can get from the first table to all the other
      PSBackEndTable firstTable = (PSBackEndTable)tables.get(0);
      for (int i = 1; i < tableCount; i++) {
         PSBackEndTable tab = (PSBackEndTable)tables.get(i);
         if (!jTree.hasRoute(firstTable, tab)) {
            throw new IllegalArgumentException("no join path between tables" +
               firstTable.getAlias() + " " + tab.getAlias() );
         }
      }

      return jTree;
   }
}

