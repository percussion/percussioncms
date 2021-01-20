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

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSDataSynchronizer;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSUpdateColumn;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSApplicationHandler;
import com.percussion.util.PSCollection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.NamingException;


/**
 * The PSUpdateOptimizer class is used internally by the E2 server to
 * determine the best execution plan for data modifications (update, insert
 * or delete). This is critical when performing transactions across
 * multiple back-ends. A poor execution plan may lead to deadlocking or
 * other bad behavior.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUpdateOptimizer extends PSOptimizer
{
   /**
    * Generate an execution plan for inserting data against the given data
    * set. This determines what transaction sets must be executed and exits
    * fired.
    *
    * @param   ah   The application handler for this request
    *
    * @param   ds   The data set for which to generate an execution plan
    *
    * @return   the array of execution steps in the execution plan
    *
    * @exception PSIllegalArgumentException if <CODE>ah</CODE> is null,
    * if <CODE>ds</CODE> is null or does not contain exactly one query pipe.
    */
   public static IPSExecutionStep[] createInsertExecutionPlan(
         PSApplicationHandler ah, PSDataSet ds)
         throws PSIllegalArgumentException, SQLException
   {
      return createExecutionPlan(ah, ds, PLAN_TYPE_INSERT);
   }

   /**
    * Generate an execution plan for updating data against the given data
    * set. This determines what transaction sets must be executed and exits
    * fired.
    *
    * @param   ds     the data set for which to generate an execution plan
    *
    * @return   the array of execution steps in the execution plan
    */
   public static IPSExecutionStep[] createUpdateExecutionPlan(
         PSApplicationHandler ah, PSDataSet ds)
         throws PSIllegalArgumentException, SQLException
   {
      return createExecutionPlan(ah, ds, PLAN_TYPE_UPDATE);
   }

   /**
    * Generate an execution plan for deleting data against the given data
    * set. This determines what transaction sets must be executed and exits
    * fired.
    *
    * @param   ds     the data set for which to generate an execution plan
    *
    * @return   the array of execution steps in the execution plan
    */
   public static IPSExecutionStep[] createDeleteExecutionPlan(
         PSApplicationHandler ah, PSDataSet ds)
         throws PSIllegalArgumentException, SQLException
   {
      return createExecutionPlan(ah, ds, PLAN_TYPE_DELETE);
   }


   /**
    * Generate an execution plan for against the given data set.
    *
    * @param   ah   The application handler for this request
    *
    * @param   ds   The data set for which to generate an execution plan
    *
    * @return   the array of execution steps in the execution plan
    *
    * @exception PSIllegalArgumentException if <CODE>ah</CODE> is null,
    * if <CODE>ds</CODE> is null or does not contain exactly one query pipe.
    */
   public static IPSExecutionStep[] createExecutionPlan(
         PSApplicationHandler ah, PSDataSet ds, int planType)
         throws PSIllegalArgumentException, SQLException
   {
      // this does our validation of ah, ds and the pipes within ds
      PSUpdatePipe updatePipe = getUpdatePipe(ah, ds);

      PSDataSynchronizer dataSync = updatePipe.getDataSynchronizer();

      // we are only concerned with updateable update pipes
      if (planType == PLAN_TYPE_INSERT) {
         if (!dataSync.isInsertingAllowed())
            return null;
      }
      else if (planType == PLAN_TYPE_UPDATE) {
         if (!dataSync.isUpdatingAllowed())
            return null;
      }
      else if (planType == PLAN_TYPE_DELETE) {
         if (!dataSync.isDeletingAllowed())
            return null;
      }
      else {
         return null;   // what type is this?! (can't happen anyway)
      }

      // get the back end tables information for this pipe
      PSCollection beTables = updatePipe.getBackEndDataTank().getTables();
      int tableCount = (null == beTables) ? 0 : beTables.size();
      if (tableCount == 0)
      {
         Object[] args = { ah.getName(), ds.getName(), updatePipe.getName() };
         throw new PSIllegalArgumentException(
               IPSBackEndErrors.EXEC_PLAN_NO_BETABLES_IN_PIPE, args);
      }

      // create the login plan for the back-ends
      ArrayList logins = new ArrayList();
      Hashtable connKeys = new Hashtable();
      createLoginPlan(ah, beTables, logins, connKeys, null);

      // now create the builder map (keyed on back-end table)

      //dbreslau 12/19/02:
      // builderMaps uses both Strings and PSSqlUpdateBuilder
      // as key types.  This is risky enough, but also note
      // that PSSqlUpdateBuilder is a class that defines equals()
      // but not hashcode().  This means that two different keys
      // that are equal to each other will NOT hash to the same
      // value; you have to make sure that the same instance is used
      // both for insertion and for lookup.
      java.util.HashMap builderMaps = new java.util.HashMap();

      PSSqlUpdateBuilder curBuilder;   // this is the base for all builders

      // now we can create our builders (one for each back-end
      PSBackEndTable curTable;
      PSBackEndColumn beCol;
      java.util.List tableBuilders;

      // add all the column - xml field mappings for this table
      PSDataMapper mapper = updatePipe.getDataMapper();
      for (int j = 0; j < mapper.size(); j++) {
         PSDataMapping map = (PSDataMapping)mapper.get(j);

         // when updating, a back-end column must be in the mapping
         try {
            beCol = (PSBackEndColumn)map.getBackEndMapping();
         } catch (ClassCastException e) {   // invalid update mapper
            Object[] args = { ah.getName(), ds.getName(),
               updatePipe.getName(), String.valueOf(j+1) };
            throw new PSIllegalArgumentException(
                  IPSBackEndErrors.UPDATE_MAP_NOT_TO_BECOL, args);
         }

         curTable = (PSBackEndTable)beCol.getTable();
         String builderKey = curTable + ":" + String.valueOf(map.getGroupId());
         curBuilder = (PSSqlUpdateBuilder)builderMaps.get(builderKey);
         if (curBuilder == null) {
            Object serverKey = curTable.getServerKey();

            int connKey =
                  ((Integer)connKeys.get(serverKey)).intValue();

            try
            {
               curBuilder = PSSqlUpdateBuilderFactory.getSqlUpdateBuilder(
                     planType,
                     curTable,
                     (PSBackEndLogin) logins.get(connKey),
                     dataSync.isInsertingAllowed());
            }
            catch (NamingException e)
            {
               throw new SQLException(e.getLocalizedMessage());
            }

            builderMaps.put(builderKey, curBuilder);
            tableBuilders = (java.util.List)builderMaps.get(curTable);
            if (tableBuilders == null)
               tableBuilders = new java.util.ArrayList();
            tableBuilders.add(curBuilder);
            builderMaps.put(curTable, tableBuilders);
         }
         curBuilder.addColumnToXmlMapping(map);
      }

      // for each column, add it to the update list, or add it as a key
      PSCollection updateColumns = dataSync.getUpdateColumns();
      for (int j = 0; j < updateColumns.size(); j++) // for each column
      {
         PSUpdateColumn col = (PSUpdateColumn)updateColumns.get(j);
         beCol = col.getColumn();

         tableBuilders = (java.util.List)builderMaps.get(beCol.getTable());
         if (tableBuilders == null) {
            // though this makes no sense, it's ignorable so don't error
            if (!col.isUpdateable() && !col.isKey())
               continue;
            int errorCode;
            if (col.isUpdateable())
               errorCode = IPSBackEndErrors.EXEC_PLAN_UPD_COL_NOT_MAPPED;
            else // must be a key
               errorCode = IPSBackEndErrors.EXEC_PLAN_KEY_COL_NOT_MAPPED;

            // can't have a column in the update list which was not mapped
            Object[] args = { beCol.getTable().getAlias(),  beCol.getColumn() };
            throw new PSIllegalArgumentException(errorCode, args);
         }

         if (col.isUpdateable())
         {
            if (col.isKey()) {
               // don't support both updateable and key for now
               Object[] args
                     = { beCol.getTable().getAlias(),  beCol.getColumn() };
               throw new PSIllegalArgumentException(
                     IPSBackEndErrors.EXEC_PLAN_COL_UPD_AND_KEY_NOT_SUPPORTED,
                     args);
            }

            for (int t = 0; t < tableBuilders.size(); t++) {
               curBuilder = (PSSqlUpdateBuilder)tableBuilders.get(t);
               curBuilder.addUpdateColumn(beCol);
            }
         }

         if (col.isKey())
         {
            // use key columns in the where clause

            for (int t = 0; t < tableBuilders.size(); t++) {
               curBuilder = (PSSqlUpdateBuilder)tableBuilders.get(t);
               curBuilder.addKeyColumn(beCol);
            }
         }
      }

      /* convert the builder map to an exec plan array. The builder may
      * change the ordering since it returns entries based on their hash
      * key. What we'll do instead, as order may be important, is traverse
      * the table collection and lookup the table in the builder map.
      * This is part of the fix for bug id TGIS-4BWNFK
       */
      java.util.List execSteps = new ArrayList(
            builderMaps.size() - tableCount);   // each table is stored +
      // a builder for each table/group
      for (int i = 0; i < tableCount; i++)
      {
         curTable = (PSBackEndTable)beTables.get(i);
         tableBuilders = (java.util.List)builderMaps.get(curTable);
         int builderCount = (tableBuilders == null) ? 0 : tableBuilders.size();

         for (int j = 0; j < builderCount; j++)
         {
            curBuilder = (PSSqlUpdateBuilder)tableBuilders.get(j);

            /* remove builders for tables which are not actually
            * being modified.
             */
            if ((planType == PLAN_TYPE_UPDATE) &&
                !curBuilder.hasUpdateColumns())
            {
               // if there are no update columns, we can't do this
               // log it and move on without generating the statement
               Object[] args = { ah.getName(), ds.getName(),
                  curTable.getAlias() };
               ah.getLogHandler()
                     .write(
                     new com.percussion.log.PSLogExecutionPlan(
                     ah.getId(),
                     IPSDataErrors.EXEC_PLAN_IGNORE_NO_UPDCOL_UPDATE,
                     args));
               continue;
            }

            execSteps.add(curBuilder.generate(logins, connKeys));
         }
      }

      /* get the reordered execution steps, which takes referential
      * constraints, etc. into consideration.
       */
      execSteps = reorderExecutionSteps(
            planType, beTables, execSteps, builderMaps);

      // and return the joined login plans and exec plans
      IPSExecutionStep[] ret = joinLoginAndExecutionPlans(logins, execSteps);
      logExecutionPlan(ah, ds, ret);
      return ret;
   }


   /**
    * Get the update pipe in this data set and verify it does indeed
    * contain an update pipe.
    *
    * @param   ah      the application handler
    * @param   ds      the data set containing the pipes
    */
   private static PSUpdatePipe getUpdatePipe(
         PSApplicationHandler ah, PSDataSet ds)
         throws PSIllegalArgumentException
   {
      // make sure ah is not null
      if (null == ah)
      {
         throw new PSIllegalArgumentException(
               IPSBackEndErrors.EXEC_PLAN_APP_HANDLER_NULL);
      }

      // make sure ds is not null
      if (null == ds)
      {
         Object[] args = { ah.getName() };
         throw new PSIllegalArgumentException(
               IPSBackEndErrors.EXEC_PLAN_DATA_SET_NULL, args);
      }

      // make sure ds has a pipe
      PSPipe pipe = ds.getPipe();
      if (pipe == null) // no pipes?
      {
         Object[] args = { ah.getName(), ds.getName() };
         throw new PSIllegalArgumentException(
               IPSBackEndErrors.EXEC_PLAN_NO_UPDATE_PIPES, args);
      }

      // verify the pipe is an update pipe
      if (pipe instanceof com.percussion.design.objectstore.PSUpdatePipe)
         return (PSUpdatePipe)pipe;

      // this isn't an update pipe!
      Object[] args = { ah.getName(), ds.getName() };
      throw new PSIllegalArgumentException(
            IPSBackEndErrors.EXEC_PLAN_NO_UPDATE_PIPES, args);
   }


   /**
    * Reorder the execution steps, if necessary. The main reason to
    * change the ordering is when referential constraints, such as
    * foreign keys, exist. When this is the case, changes to one of the
    * tables must occur before the others, otherwise an error will occur.
    *
    * @param   beTables         the back-end tables the execution steps are for
    *
    * @param   execSteps      the execution steps to reorder
    *
    * @return                  the reordered execution steps
    */
   private static java.util.List reorderExecutionSteps(
         int planType, PSCollection beTables, java.util.List execSteps,
         java.util.Map builderMaps)
         throws PSIllegalArgumentException, java.sql.SQLException
   {
      /* for inserts, we must insert into the primary key table first.
      * otherwise, the foreign key insert will fail as it cannot find
      * the related entity.
      *
      * for updates, we have a bit of trouble. if the update key is changed,
      * the only chance at success is to insert a new primary key record,
      * update the foreign key records, then delete the original
      * primary key record. When the key is not being changed, order
      * doesn't really matter. However, we'll follow the insert model
      * in case we want to do insert's after getting 0 record updates.
      *
      * for deletes, we must delete from the foreign key table first.
      * otherwise, the primary key delete will fail as it still has
      * related entities.
       */
      boolean pkeyTableFirst;
      if ((planType == PLAN_TYPE_INSERT) || (planType == PLAN_TYPE_UPDATE))
         pkeyTableFirst = true;
      else   // planType == PLAN_TYPE_DELETE
         pkeyTableFirst = false;

      /* reorder the execution steps, if necessary. The main reason to
      * change the ordering is when referential constraints, such as
      * foreign keys, exist. When this is the case, changes to one of the
      * tables must occur before the others, otherwise an error will occur.
       */
      PSBackEndTable curTable;
      int tableCount = (null == beTables) ? 0 : beTables.size();
      if (tableCount > 1) {   // no reordering for single tables
         java.util.HashMap dependencyMap = new java.util.HashMap();
         for (int i = 0; i < tableCount; i++) {
            curTable = (PSBackEndTable)beTables.get(i);
            if (builderMaps.get(curTable) == null)
               continue;   // may have been removed if no data modifications

            PSDatabaseMetaData dbMeta = getCachedDatabaseMetaData(curTable);
            PSTableMetaData meta = (dbMeta == null) ? null :
                                   dbMeta.getTableMetaData(
                                      curTable.getConnectionDetail().getOrigin(),
                                   curTable.getTable());
            if ((dbMeta != null) && (meta != null)) {
               String[] keyCols = meta.getForeignKeyColumns();
               int colCount = (keyCols == null) ? 0 : keyCols.length;
               for (int j = 0; j < colCount; j++) {
                  String columnName = keyCols[j];
                  String tableName =
                        columnName.substring(0, columnName.indexOf('.'));
                  java.util.List dependentTables
                        = (java.util.List)dependencyMap.get(tableName);
                  if (dependentTables == null) {
                     dependentTables = new ArrayList();
                     dependencyMap.put(tableName, dependentTables);
                  }
                  dependentTables.add(curTable.getTable());
               }
            }
         }

         if (dependencyMap.size() == 0) {
            /* some drivers, like all the MS Jet DBs (Access, etc.) do not
            * support cataloging foreign keys, though they do support
            * creating them. To provide a work-around of sorts, we will
            * treat the order the tables are defined in as the
            * INSERT/UPDATE order. We will flip the order for DELETEs.
            *
            * This change fixes bug id Rx-99-11-0040
             */

            //dbreslau 12/19/02:
            // Note that this also adds dependencies in cases where
            // there aren't foreign key relationships between tables,
            // regardless of whether the DB would report them.

            // For update resources -- where there aren't explicit
            // JOINs -- this seems entirely arbitrary, and maybe wrong.

            PSBackEndTable nextTable = null;
            for (int i = 0; i < tableCount; )
            {
               if (nextTable == null) {
                  curTable = (PSBackEndTable)beTables.get(i);
                  if (builderMaps.get(curTable) == null)
                  {
                     // may have been removed if no data modifications
                     continue;
                  }
               }
               else
                  curTable = nextTable;

               nextTable = null;
               while (++i < tableCount)
               {
                  nextTable = (PSBackEndTable)beTables.get(i);
                  // dbreslau 12/19/02:
                  // Quick kludge: Allow a table to appear twice in a row in
                  // the pipe without this raising a dependency issue.
                  // Ideally, we'd like to be able to have the same table appear
                  // more than once, *provided* additional entries of the same
                  // table use different key columns.
                  if (nextTable.getTable().equals(curTable.getTable())) {
                     nextTable = null;
                     continue;
                  }
                  // make sure the table def was not removed for no data mod.
                  if (builderMaps.get(nextTable) != null)
                     break; // or continue???? (dbreslau 12/19/02)
               }

               if (nextTable == null)
                  break;

               java.util.List dependentTables
                     = (java.util.List)dependencyMap.get(curTable.getTable());
               if (dependentTables == null) {
                  dependentTables = new ArrayList();
                  dependencyMap.put(curTable.getTable(), dependentTables);
               }
               dependentTables.add(nextTable.getTable());
            }
         }

         /*   depending upon the order we need, we'll either sort dependent
         * tables to the top or the bottom. should we encounter
         * cross dependencies, this can get really messy. we may be
         * able to deal with the situation by breaking up the transaction
         * into pieces (eg, update t1.c1; update t2.c2; update t1.c2).
         * for now, we're not covering this scenario
         *
         * to build the final execution plan, we make two passes. One
         * to sort the tables into their appropriate order. The second
         * to replace the table slots with execution plan entries.
          */
         java.util.List newSteps = new ArrayList(tableCount);
         for (int i = 0; i < tableCount; i++) {
            /* though we don't need the builder, we must first verify that
            * the table is indeed part of the data modification action.
            * To do this we get the builder. If the builder doesn't
            * exist, we know the table is not part of the action.
             */
            curTable = (PSBackEndTable)beTables.get(i);

            /* the table may have been removed if there were no
            * data modifications. Also, it may be repeated if update
            * groups exist
             */
            java.util.List tableBuilders
                  = (java.util.List)builderMaps.get(curTable);
            int builderCount =
                  (tableBuilders == null) ? 0 : tableBuilders.size();

            String tableName = curTable.getTable();
            java.util.List dependentTables
                  = (java.util.List)dependencyMap.get(tableName);
            String crossDependentTable = getCrossDependentTable(
                  tableName, dependencyMap, dependentTables);

            for (int j = 0; j < builderCount; j++)
            {
               PSSqlUpdateBuilder curBuilder
                       = (PSSqlUpdateBuilder)tableBuilders.get(j);

               if ((planType == PLAN_TYPE_UPDATE) &&
                   !curBuilder.hasUpdateColumns())
               {
                  // if there are no update columns, a plan was not built
                  continue;
               }

               if (crossDependentTable != null)
               {
                  Object[] args = { tableName, crossDependentTable };
                  throw new PSIllegalArgumentException(
                        IPSBackEndErrors.DATA_MOD_UNSUPPORTED_FOR_XDEPEND,
                        args);
               }
               else if (dependentTables == null) {
                  /* when pkey tables go first, tables with no dependencies
                  * can go anywhere on the end of the list.
                  * Othewise, tables with no dependencies can go anywhere
                  * at the beginning of the list
                   */
                  if (pkeyTableFirst)
                     newSteps.add(tableName);
                  else
                     newSteps.add(0, tableName);
               }
               else {
                  /* when pkey tables go first, tables with dependencies
                  * must precede all their dependents.
                  * Otherwise, they must go after their dependents.
                   */
                  int insertPos = (pkeyTableFirst ? newSteps.size() : 0);
                  for (int k = 0; k < dependentTables.size(); k++) {
                     int pos = newSteps.indexOf(dependentTables.get(k));
                     if (pos != -1) {
                        if (pkeyTableFirst) {
                           if (pos < insertPos)
                              insertPos = pos;
                        }
                        else if (pos > insertPos)
                           insertPos = pos;
                     }
                  }
                  newSteps.add(insertPos, tableName);
               }
            }
         }

// System.out.println("Reordering update statements...");
         /* now go back through and swap out the table with the exec step
         * we need to go through the table builders here, not just
         * the table entries. There may be multiple updates to the
         * same table if update groups are being used.
         * This is part of the fix for bug id TGIS-4BWNFK
          */
         int onExecStep = 0;
         for (int i = 0; i < tableCount; i++) {
            curTable = (PSBackEndTable)beTables.get(i);
            String tableName = curTable.getTable();

            java.util.List tableBuilders
                  = (java.util.List)builderMaps.get(curTable);
            int builderCount =
                  (tableBuilders == null) ? 0 : tableBuilders.size();

            for (int j = 0; j < builderCount; j++)
            {
               final PSSqlUpdateBuilder curBuilder
                       = (PSSqlUpdateBuilder)tableBuilders.get(j);

               if ((planType == PLAN_TYPE_UPDATE) &&
                   !curBuilder.hasUpdateColumns())
               {
                  // if there are no update columns, a plan was not built
                  continue;
               }

               /* if the table exists in the list, we will replace it
               * with the exec step. If it's not in the list, that
               * means we are not doing any modifications to the table,
               * which is ok.
                */
               int pos = newSteps.indexOf(tableName);
               if (pos != -1) {
                  final IPSExecutionStep s =
                        (IPSExecutionStep)execSteps.get(onExecStep++);
// System.out.println("  storing " + tableName + " in position " + pos);
// System.out.println("    >> " + s.toString());
                  newSteps.set(pos, s);
               }
            }
         }

         // we've probably done some swaps, so save it at this point
         execSteps = newSteps;
      }

      return execSteps;
   }


   /**
    * Go through the dependencies for a given table and see if the table
    * is listed in one of its dependent tables as a dependent. This should
    * really never happen, but we should definitely want to know about it.
    *
    * @return      the name of the cross-dependent table; <code>null</code>
    *                                                                                                                     if no cross-dependencies exist
    */
   private static String getCrossDependentTable(
         String tableName, java.util.Map dependencyMap,
         java.util.List dependentTables)
   {
      if ((dependentTables == null) || (dependentTables.size() == 0))
         return null;

      //dbreslau 12/19/02:
      // NOTE: This doesn't catch indirect dependency loops
      // (A --> B --> C --> A)

      // Loop through all the dependent tables, and see if tableName
      // is listed as a dependent
      for (int i = 0; i < dependentTables.size(); i++)
      {
         // Examine the dependents for this dependent
         String table = (String)dependentTables.get(i);
         java.util.List dependents
               = (java.util.List)dependencyMap.get(table);
         if ((dependents != null) && dependents.contains(tableName))
            return table;
      }

      return null;
   }


   static final int    PLAN_TYPE_UPDATE   = 1;
   static final int    PLAN_TYPE_INSERT   = 2;
   static final int    PLAN_TYPE_DELETE   = 4;
}
