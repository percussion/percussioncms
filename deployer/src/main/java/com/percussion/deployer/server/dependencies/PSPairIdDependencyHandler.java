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

package com.percussion.deployer.server.dependencies;


import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcFilterContainer;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for the dependency handlers whose id a the combination of
 * its parent id and its name (not the id column of its database table)
 */
public abstract class PSPairIdDependencyHandler
   extends PSDataObjectDependencyHandler
{
   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSPairIdDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public boolean delegatesIdMapping()
   {
      return true;
   }
   
   // see base class
   public String getIdMappingType()
   {
     return getPairParentType();
   }
   
   // see base class
   protected String getSourceForIdMapping(String id) throws PSDeployException 
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      PSPairDependencyId pairId = new PSPairDependencyId(id);
      
      return pairId.getParentId();
   }
   
   // see base c1ass
   public String getTargetId(PSIdMapping mapping, String id) 
      throws PSDeployException
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
               
      PSPairDependencyId pairId = new PSPairDependencyId(id);
      String newParentId = super.getTargetId(mapping, pairId.getParentId());
      
      return PSPairDependencyId.getPairDependencyId(newParentId, 
         pairId.getChildId());      
   }
   
   /**
    * Get the dependency type of the parent portion of the pair id
    * 
    * @return The type, never <code>null</code> or empty. 
    */
   protected abstract String getPairParentType();

   /**
    * Create a dependency object from the given parameters. It does the same
    * as the base class specified, except the <code>id</code> is a pair id
    * and some extra parameters.
    *
    * @param tok The Security token, it may not be <code>null</code>
    * @param id The (pair) id of the dependency, it may not be
    * <code>null</code> or empty
    * @param table The table name, which contains the dependency definition,
    * it may not be <code>null</code> or empty.
    * @param childNameCol The column name for the name of the dependency,
    * it may not be <code>null</code> or empty.
    * @param parentIdCol The column name for the parent id, it may not be
    * <code>null</code> or empty.
    *
    * @return The created dependency object, it may be <code>null</code> if
    * not exist.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if any error occurs.
    */
   protected PSDependency getDependency(PSSecurityToken tok, String id,
      String table, String childNameCol, String parentIdCol)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (childNameCol == null || childNameCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childNameCol may not be null or empty");
      if (parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      PSDependency dep = null;

      PSJdbcSelectFilter filter = getFilterForPairId(id, parentIdCol,
         childNameCol);
      PSJdbcTableData data = dbmsHelper.catalogTableData(
         table, null, filter);

      // should only get back one, take the first if found
      if (data != null && data.getRows().hasNext())
      {
         Iterator rows = data.getRows();
         PSJdbcRowData row = (PSJdbcRowData) rows.next();
         String name = dbmsHelper.getColumnString(table, childNameCol, row);

         dep = createDependency(m_def, id, name);
      }

      return dep;
   }

   /**
    * Get a filter for a given pair id. The filter will be in the form of:
    * WHERE CHILD_NAME = childId AND PARENT_ID = parentId.
    *
    * Note: the CHILD_NAME column must be <code>VARCHAR</code>, and the
    * PARENT_ID column must be <code>INTEGER</code>
    *
    * @param id The pair id, it may not be <code>null</code> or empty.
    * @param parentIdCol The parent column name, it may not be
    * <code>null</code> or empty.
    * @param childNameCol The column name of the current object, it may not be
    * <code>null</code> or empty.
    *
    * @return The filter specified above, will never to <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if any error occurs.
    */
   protected PSJdbcSelectFilter getFilterForPairId(String id, String parentIdCol,
      String childNameCol)
      throws PSDeployException
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      if (parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");
      if (childNameCol == null || childNameCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childNameCol may not be null or empty");

      PSPairDependencyId pairId = new PSPairDependencyId(id);

      PSJdbcSelectFilter fltChildId = new PSJdbcSelectFilter(childNameCol,
         PSJdbcSelectFilter.EQUALS, pairId.getChildId(), Types.VARCHAR);
      PSJdbcSelectFilter fltParentId = new PSJdbcSelectFilter(parentIdCol,
         PSJdbcSelectFilter.EQUALS, pairId.getParentId(), Types.INTEGER);

      PSJdbcFilterContainer fltFinal = new PSJdbcFilterContainer();
      fltFinal.doAND(fltChildId);
      fltFinal.doAND(fltParentId);

      return fltFinal;
   }


   /**
    * Delete a given dependency from a specified table.
    * The deletion where clause looks like:
    *     PARENT_ID=parentId and NAME=childId; Both parentId and childId
    *     are part of the <code>dep.getDependencyId()</code>.
    *
    *     Note: The parentId (above) is a transfered or mapped id on the
    *           target server
    *           
    *     Also Note: An update key is set on the supplied <code>schema</code>
    *                object.
    *    
    * @param tok The security token, it may not be <code>null</code>.
    * @param dep The dependency object, it may not be <code>null</code>.
    * @param ctx The import context used to transform the dependency id for
    * deletion if found in id map, it may not be <code>null</code>.
    * @param schema The schema object, it may not be <code>null</code>.
    * @param table The table name, which contains the dependency definition,
    * it may not be <code>null</code> or empty.
    * @param childNameCol The column name for the name of the dependency,
    * it may not be <code>null</code> or empty.
    * @param parentIdCol The column name for the parent id, it may not be
    * <code>null</code> or empty.
    * @param parentDepType The type of the dependency handler, it may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if any error occurs.
    */
   protected void deleteDepFromTable(PSSecurityToken tok, PSDependency dep,
      PSImportCtx ctx, PSJdbcTableSchema schema, String table,
      String childNameCol, String parentIdCol, String parentDepType)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (childNameCol == null || childNameCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childNameCol may not be null or empty");
      if (parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");
      if (parentDepType == null || parentDepType.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentDepType may not be null or empty");

      PSPairDependencyId pairId = new PSPairDependencyId(dep.getDependencyId());
            
      // get the target parent id
      PSIdMapping parentMapping = getIdMapping(ctx, pairId.getParentId(), 
         parentDepType);
      String newParentId = (parentMapping == null) ? pairId.getParentId() : 
         parentMapping.getTargetId();

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

      // query the table first, see if there is anything to delete
      String newPairId = PSPairDependencyId.getPairDependencyId(newParentId,
         pairId.getChildId());
      PSJdbcSelectFilter filter;
      filter = getFilterForPairId(newPairId, parentIdCol, childNameCol);
      PSJdbcTableData data = dbmsHelper.catalogTableData(
         schema, null, filter);

      if (data != null && data.getRows().hasNext()) // if there is things
      {                                             // exist in the table
         // prepare to be deleted row
         List cols = new ArrayList();
         cols.add( new PSJdbcColumnData(parentIdCol, newParentId) );
         cols.add( new PSJdbcColumnData(childNameCol, pairId.getChildId()) );
         PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(),
            PSJdbcRowData.ACTION_DELETE);

         // set the update key: WHERE PARENT_ID=parentId and CHILD_NAME=childId
         cols.clear();
         cols.add(parentIdCol);
         cols.add(childNameCol);
         dbmsHelper.setUpdateKeyForSchema(cols.iterator(), schema);

         // do the delete
         dbmsHelper.processTable(schema, table, rowData);

         // update the log transaction for the delete action
         addTransactionLogEntry(dep, ctx, table,
            PSTransactionSummary.TYPE_DATA,
            PSTransactionSummary.ACTION_DELETED);
      }
  }

}
