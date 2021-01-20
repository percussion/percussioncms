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

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyData;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcFilterContainer;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * The parent class for all data object definition handlers. These are
 * handlers for dependencies that are persisted in the repository. This class
 * contains convenient methods for saving and retrieving dependencies, to and
 * from the database and/or archive files.
 */
public abstract class PSDataObjectDependencyHandler extends PSDependencyHandler
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
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDataObjectDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id) != null;
   }

   /**
    * Get a list of child dependencies for a given set of parameters. The child
    * dependencies are created from the ids retrieved from the given table and
    * parent id.
    *
    * @param tableName The table name, may not be <code>null</code> or empty.
    * @param childIdCol The column name of the child id, may not be
    * <code>null</code> or empty.
    * @param parentIdCol The column name of the parent id, may not be
    * <code>null</code> or empty.
    * @param parentId The parent id value of the <code>parentIdColumnName</code>
    * @param dependentType The dependency type, may not be <code>null</code> or
    * empty.
    * @param tok The security token, may not be <code>null</code>.
    *
    * @return A list over zero or more <code>PSDependency</code> objects. It
    * will never be <code>null</code>, but may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected List<PSDependency> getChildDepsFromParentID(String tableName,
      String childIdCol, String parentIdCol, String parentId,
      String dependentType, PSSecurityToken tok) throws PSDeployException
   {
      if ( tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");
      if ( childIdCol == null || childIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childIdCol may not be null or empty");
      if ( parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");
      if ( dependentType == null || dependentType.trim().length() == 0)
         throw new IllegalArgumentException(
            "dependentType may not be null or empty");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      Iterator<String> childIDs = getChildIdsFromTable(tableName, childIdCol,
         parentIdCol, parentId);

      return getDepsFromIds(childIDs, dependentType, tok);
   }

   /**
    * Get a list of dependencies from a list of ids for a dependency type.
    * 
    * @param ids The list of dependency ids, may not be <code>null</code>,
    * but may be empty.
    * @param dependencyType The dependency type, may not be <code>null</code>
    * or empty.
    * @param tok The security token, may not be <code>null</code>.
    * 
    * @return A list over zero or more <code>PSDependency</code> objects. The
    * type (or scope) of the objects will default to 
    * <code>PSDependency.SHARED</code> It will never be <code>null</code>, 
    * but may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected List<PSDependency> getDepsFromIds(Iterator ids, String dependencyType, 
      PSSecurityToken tok) throws PSDeployException
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");
      if ( dependencyType == null || dependencyType.trim().length() == 0)
         throw new IllegalArgumentException(
            "dependencyType may not be null or empty");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return getDepsFromIds(ids, dependencyType, tok, -1);
   }

   /**
    * Get a list of dependency objects from ids. It is similar with 
    * {@link #getDepsFromIds(Iterator, String, PSSecurityToken)}, except the
    * the type or scope of the returned <code>PSDependency</code> objects will
    * be determined by <code>depType</code>.
    * 
    * @param ids The list of dependency ids, may not be <code>null</code>,
    * but may be empty.
    * @param dependencyType The dependency type, may not be <code>null</code>
    * or empty.
    * @param tok The security token, may not be <code>null</code>.
    * @param depType The dependency type or scope, must be either one of the 
    * <code>PSDependency.TYPE_XXX</code> values, or <code>-1</code> if not to
    * reset the type/scope of the returned dependencies.
    * 
    * @return A list over zero or more <code>PSDependency</code> objects. The
    * type (or scope) of the objects will be specified by <code>depType</code>.
    * It will never be <code>null</code>, but may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected List<PSDependency> getDepsFromIds(Iterator ids, String dependencyType, 
      PSSecurityToken tok, int depType) throws PSDeployException
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");
      if ( dependencyType == null || dependencyType.trim().length() == 0)
         throw new IllegalArgumentException(
            "dependencyType may not be null or empty");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if ( (! PSDependency.validateType(depType)) && (depType != -1) )
         throw new IllegalArgumentException(
            "depType must be one of the PSDependency.TYPE_XXX values");

      List<PSDependency> deps = new ArrayList<PSDependency>();
      
      PSDependencyHandler handler = getDependencyHandler(dependencyType);
      String id;
      while (ids.hasNext())
      {
         id = (String) ids.next();
         PSDependency dep = handler.getDependency(tok, id);
         if (dep != null)
         {
            if ( depType != -1 )
               dep.setDependencyType(depType);
            deps.add(dep);
         }
      }

      return deps;
   }
   
   /**
    * Get a list of distinct ids from a given table data at the column of 
    * <code>col</code>. Note: the column may be a nullable column. Both 
    * <code>null</code> or empty values of the column will not be added to the
    * returned list.
    * 
    * @param data The table data, it may be <code>null</code> or it may not
    * contain any rows.
    * @param table The table name, may not be <code>null</code> or empty.
    * @param col The column name, may not be <code>null</code> or empty.
    * 
    * @return An iterator over zero or more ids in <code>String</code>. 
    * It will never be <code>null</code>, but may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */   
   protected Iterator<String> getIdsFromTableData(PSJdbcTableData data,
         String table, String col) throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
         
      if (col == null || col.trim().length() == 0)
         throw new IllegalArgumentException("col may not be null or empty");
   
      // use "Set" to make sure it is a distinct list
      Set<String> ids = new HashSet<String>();

      if (data != null && data.getRows().hasNext())
      {
         Iterator rows = data.getRows();
         String id;
         PSJdbcRowData row;
         while (rows.hasNext())
         {
            row = (PSJdbcRowData)rows.next();
            id = getColumnValueNullable(table, col, row);
            if (id != null && id.trim().length() != 0)
               ids.add(id);               
         }
      }
      
      return ids.iterator();
   }
   
   /**
    * Get a list of distinct application names from a given table data at the 
    * column of <code>col</code>. Note: the column may be a nullable column. 
    * If the value of the column is not <code>null</code> or empty, the format
    * of it is expected to be <code>../app-name/XXX</code>. 
    * The <code>app-name</code> will be retrieved and be part of the returned 
    * list.
    * 
    * @param data The table data, it may be <code>null</code> or it may not
    * contain any rows.
    * @param table The table name, may not be <code>null</code> or empty.
    * @param col The column name, may not be <code>null</code> or empty.
    * 
    * @return An iterator over zero or more app-names in <code>String</code>. 
    * It will never be <code>null</code>, but may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */   
   protected Iterator getAppNamesFromTableData(PSJdbcTableData data, 
      String table, String col) throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
         
      if (col == null || col.trim().length() == 0)
         throw new IllegalArgumentException("col may not be null or empty");

      // get a distinct list of app names
      Set<String> appNames = new HashSet<String>();

      if (data != null && data.getRows().hasNext())
      {
         Iterator rows = data.getRows();
         PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
         
         while (rows.hasNext())
         {
            PSJdbcRowData row = (PSJdbcRowData)rows.next();
            String url = getColumnValueNullable(table, col, row);
            if (url != null && url.trim().length() != 0)
            {
               String appName = dbmsHelper.getColumnAppName(table, 
                  col, row);
               appNames.add(appName);
            }
         }
      }
      
      return appNames.iterator();
   }   

   /**
    * Map or transfer a child id in a given column.  Do nothing when the value
    * of the column is <code>null</code> or empty.
    * 
    * @param col The column to be mapped or transfered, it may not be 
    * <code>null</code>.
    * @param parentDep The parent dependency object, it may not be 
    * <code>null</code>.
    * @param childDepType The type of the child dependency handler, it may not
    * be <code>null</code> or empty.
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected void mapChildIdForColumnNullable(PSJdbcColumnData col, 
      PSDependency parentDep, String childDepType, PSImportCtx ctx) 
      throws PSDeployException
   {
      if (col == null)
         throw new IllegalArgumentException("col may not be null");
      if (parentDep == null)
         throw new IllegalArgumentException("parentDep may not be null");
      if (childDepType == null || childDepType.trim().length() == 0)
         throw new IllegalArgumentException(
            "childDepType may not be null or empty");      
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      String colValue = col.getValue();
      if (colValue != null && colValue.trim().length() > 0)
      {
         col.setValue(mapChildIdForNullableValue(colValue, parentDep,
               childDepType, ctx));
      }
   }
        
   /**
    * Map or transfer a child id by using idMap in the <code>ctx</code> to map
    * the source id to target id.
    * 
    * @param value The value to be mapped or transfered, it may not be 
    * <code>null</code>.
    * @param parentDep The parent dependency object, it may not be 
    * <code>null</code>.
    * @param childDepType The type of the child dependency handler, it may not
    * be <code>null</code> or empty.
    * @param ctx The import context to aid in the installation, it may not be 
    * <code>null</code>.
    * 
    * @return the value of the id as it is mapped to the target, or the original
    * source id if transforms are not required.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected String mapChildIdForNullableValue(String value, 
      PSDependency parentDep, String childDepType, PSImportCtx ctx) 
      throws PSDeployException
   {
      if (value == null)
         throw new IllegalArgumentException("col may not be null");
      if (parentDep == null)
         throw new IllegalArgumentException("parentDep may not be null");
      if (childDepType == null || childDepType.trim().length() == 0)
         throw new IllegalArgumentException(
            "childDepType may not be null or empty");      
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      String childId = value;
      PSIdMapping childMapping = null;
      
      // first see if we have the child dep in the tree
      PSDependency childDep = doGetChildDependency(parentDep, childId,
            childDepType);
      if (childDep != null)
      {
         childMapping = getIdMapping(ctx, childDep);
      }
      else
      {
         // can't get it from the tree, so look it up
         PSDependencyDef childDef = m_map.getDependencyDef(childDepType);
         if (childDef != null && childDef.supportsParentId())
         {
            // we can't assume we know the parent, so call the validated 
            // method to get the child dependency so it will throw the 
            // correct error for us if not found
            childDep = getChildDependency(parentDep, childId, childDepType);
            childMapping = getIdMapping(ctx, childDep);
         }
         else
         {
            childMapping = getIdMapping(ctx, childId, childDepType);
         }
      }

      if (childMapping != null)
         childId = childMapping.getTargetId();
            
      return childId;
   }
      
   /**
    * Get a list of child ids for a given parent id in a table.
    *
    * @param table The table name, it may not be <code>null</code> or empty.
    * @param childIdCol The column name of the child id, it may not be
    * <code>null</code> or empty
    * @param parentIdCol The column name of the parent id, it may not be
    * <code>null</code> or empty.
    * @param parentId The parent id, it may not be <code>null</code> or empty.
    *
    * @return A list over <code>String</code> objects, will never
    * <code>null</code>, but may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected Iterator<String> getChildIdsFromTable(String table, String childIdCol,
      String parentIdCol, String parentId) throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");      
      if (childIdCol == null || childIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childIdCol may not be null or empty");      
      if (parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");      
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");      

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      String[] columns = {childIdCol};
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(parentIdCol,
         PSJdbcSelectFilter.EQUALS, parentId, Types.INTEGER);
      PSJdbcTableData data = dbmsHelper.catalogTableData(
         table, columns, filter);

      return getIdsFromTableData(data, table, childIdCol);
   }


   /**
    * Convenience method that calls {@link #getDependencies(PSSecurityToken, 
    * String, String, String, PSJdbcSelectFilter) getDependency(tok, table, 
    * idCol, nameCol, null)}.
    * @param tok 
    * @param table 
    * @param idCol 
    * @param nameCol 
    * @return the list of dependencies
    * @throws PSDeployException 
    */
   protected Iterator<PSDependency> getDependencies(PSSecurityToken tok,
      String table, String idCol, String nameCol)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if ( table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if ( idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if ( nameCol == null || nameCol.trim().length() == 0)
         throw new IllegalArgumentException("nameCol may not be null or empty");

      // get all registered content types
      List<PSDependency> deps = new ArrayList<PSDependency>();

      Iterator regEntries = PSDbmsHelper.getInstance().getRegistrationEntries(
         table, idCol, nameCol, null).iterator();
      while (regEntries.hasNext())
      {
         Map.Entry entry = (Map.Entry)regEntries.next();
         deps.add(createDependency(m_def, entry));
      }

      return deps.iterator();
   }
   
   /**
    * Get a list of dependency object from a given table.
    *
    * @param tok The security token, may not be <code>null</code>
    * @param table The table name, may not be <code>null</code> or empty.
    * @param idCol The id column, may not be <code>null</code> or empty.
    * @param nameCol The name column, may not be <code>null</code> or empty.
    * @param filter Optional filter to apply, may be <code>null</code>.  
    *
    * @return An iterator over zero or more <code>PSDependency</code> objects,
    * will never be <code>null</code>, but may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   @SuppressWarnings("unchecked")
   protected Iterator<PSDependency> getDependencies(PSSecurityToken tok,
      String table, String idCol, String nameCol, PSJdbcSelectFilter filter)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if ( table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if ( idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if ( nameCol == null || nameCol.trim().length() == 0)
         throw new IllegalArgumentException("nameCol may not be null or empty");

      // get all registered content types
      List deps = new ArrayList();

      Iterator regEntries = PSDbmsHelper.getInstance().getRegistrationEntries(
         table, idCol, nameCol, filter).iterator();
      while (regEntries.hasNext())
      {
         Map.Entry entry = (Map.Entry)regEntries.next();
         deps.add(createDependency(m_def, entry));
      }

      return deps.iterator();
   }
   

   /**
    * Convenience method that calls {@link #getDependency(PSSecurityToken, 
    * String, String, String, String, PSJdbcSelectFilter) getDependency(tok, id, 
    * table, idCol, nameCol, null)}
    * @param tok 
    * @param id 
    * @param table 
    * @param idCol 
    * @param nameCol 
    * @return the dependency
    * @throws PSDeployException 
    */
   protected PSDependency getDependency(PSSecurityToken tok, String id,
      String table, String idCol, String nameCol)
      throws PSDeployException
   {
      return getDependency(tok, id, table, idCol, nameCol, null);
   }

   /**
    * Get a specified dependency object from a given id in a table.
    *
    * @param tok The security token, may not be <code>null</code>
    * @param id The id of the specified dependency object, may not be
    * <code>null</code> or empty.
    * @param table The table name, may not be <code>null</code> or empty.
    * @param idCol The id column, may not be <code>null</code> or empty.
    * @param nameCol The name column, may not be <code>null</code> or empty.
    * @param filter Optional filter to apply, may be <code>null</code>.  If
    * provided, the intersection of this filter and the filter createdusing the
    * provided <code>id</code> is used.
    *
    * @return The retrieved <code>PSDependency</code> objects. It may be
    * <code>null</code> if cannot find the id in the table.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependency getDependency(PSSecurityToken tok, String id,
      String table, String idCol, String nameCol, PSJdbcSelectFilter filter)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      if ( table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if ( idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if ( nameCol == null || nameCol.trim().length() == 0)
         throw new IllegalArgumentException("nameCol may not be null or empty");

      PSDeployableObject dep = null;

      PSJdbcSelectFilter idFilter = new PSJdbcSelectFilter(idCol,
         PSJdbcSelectFilter.EQUALS, id, Types.INTEGER);
      
      if (filter == null)
         filter = idFilter;
      else
      {
         PSJdbcFilterContainer filters = new PSJdbcFilterContainer();
         filters.doAND(idFilter);
         filters.doAND(filter);
         filter = filters;
      }

      List results = PSDbmsHelper.getInstance().getRegistrationEntries(
         table, idCol, nameCol, filter);

      // should only get back one, take the first if found
      if (!results.isEmpty())
      {
         Map.Entry entry = (Map.Entry)results.get(0);
         dep = createDependency(m_def, entry);
      }

      return dep;
    }

   /**
    * Reserves a new id for a given table. Does the same as the
    * {@link PSDependencyHandler#reserveNewId(PSDependency, PSIdMap)} except
    * it need to pass in a table name and dependency type of the handler.
    *
    * @param dep The dependency to check with the given <code>depType</code>,
    * may not be <code>null</code> and its object type must be the
    * <code>depType</code>, which is the type defined by this handler.
    * <code>supportsIdMapping()</code> must return <code>true</code> for this
    * dependency.
    * @param idMap The ID map, may not be <code>null</code> and must contain
    * a mapping for the supplied <code>dep</code>.
    * @param table The table name, may not be <code>null</code> or empty.
    * @param depType The dependency type of the handler, may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected void reserveNewId(PSDependency dep, PSIdMap idMap,
      String table, String depType)  throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (depType == null || depType.trim().length() == 0)
         throw new IllegalArgumentException("depType may not be null or empty");
      if (!dep.getObjectType().equals(depType))
         throw new IllegalArgumentException("dep wrong type");
      if (!dep.supportsIDMapping())
         throw new IllegalArgumentException("dep must support id mapping");
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");

      PSIdMapping mapping;
      if (dep.supportsParentId())
      {
         mapping = idMap.getMapping(dep.getDependencyId(),
            dep.getObjectType(), dep.getParentId(), dep.getParentType());
      }
      else
      {
         mapping = idMap.getMapping(dep.getDependencyId(),
            dep.getObjectType());
      }
      if (mapping == null)
      {
         Object[] args = {dep.getObjectType(), dep.getDependencyId(),
            idMap.getSourceServer()};
         throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING,
            args);
      }

      if (mapping.isNewObject() && (mapping.getTargetId() == null))
      {
         // if supports parent id, set with the parent's new id
         if (dep.supportsParentId())
         {
            PSIdMapping parentMapping = idMap.getMapping(
               dep.getParentId(), dep.getParentType());
            if (parentMapping == null)
            {
               Object[] args = {dep.getParentType(), dep.getParentId(),
                  idMap.getSourceServer()};
               throw new PSDeployException(
                  IPSDeploymentErrors.MISSING_ID_MAPPING, args);
            }
            
            String tgtParentId = parentMapping.getTargetId();
            
            mapping.setTarget(getNextId(table, dep, tgtParentId), 
               dep.getDisplayName(), tgtParentId, 
               parentMapping.getTargetName());
         }
         else
            mapping.setTarget(getNextId(table, dep), dep.getDisplayName());
      }
   }

   /** 
    * Convenience method that calls {@link #getNextId(String, PSDependency, String) 
    * getNextId(key, dep, null)}.
    * @param key see super class
    * @param dep see super class
    * @return see super class
    * @throws PSDeployException 
    */
   protected String getNextId(String key, PSDependency dep) 
      throws PSDeployException
   {
      return getNextId(key, dep, null);
   }
   
   /**
    * Get next id for a given key and dependency. The key may be a table or 
    * column name. This method is called by 
    * {@link #reserveNewId(PSDependency, PSIdMap, String, String)}. The derived 
    * class may override this method, so that the get next id will behave
    * according to the derived class.
    * 
    * @param key The name of a table or column, it may not be <code>null</code>
    * or empty.
    * @param dep The dependency object, for which a new id is to be obtained, 
    * may not be <code>null</code>.
    * @param tgtParentId The id that will be used on the target system for the
    * parent of the dependency.  May be <code>null</code> only if
    * <code>dep.supportsParentId()</code> returns <code>false</code>, never
    * empty.
    * 
    * @return The next id (in <code>String</code>) for the key, it will never
    * be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any error occurs.
    */
   protected String getNextId(String key, PSDependency dep, String tgtParentId) 
      throws PSDeployException
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (dep.supportsParentId() && tgtParentId == null)
         throw new IllegalArgumentException(
            "tgtParentId may not be null if dep supports parent id");
      
      if (tgtParentId != null && tgtParentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "tgtParentId may not be empty");

      int id = PSDbmsHelper.getInstance().getNextId(key);
      return String.valueOf(id);
   }
   
   
   /**
    * Convenience method that calls {@link #getDepDataFromTable(PSDependency, 
    * String, String, boolean, int) getDepDataFromTable(dep, table, idCol, 
    * isDataRequired, Types.INTEGER)}.
    * See the actual implementation
    * @param dep 
    * @param table 
    * @param idCol 
    * @param isDataRequired 
    * @return the dependency data
    * @throws PSDeployException 
    */
   protected PSDependencyData getDepDataFromTable(PSDependency dep,
      String table, String idCol, boolean isDataRequired)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");

      return getDepDataFromTable(dep, table, idCol, isDataRequired, 
         Types.INTEGER);
   }

   /**
    * Get dependency data from database table for a given dependency object.
    * Check for retrieved data, it may not be empty if required.
    *
    * @param dep The dependency object, may not be <code>null</code>.
    * @param table The table name, may not be <code>null</code> or empty.
    * @param idCol The id column name of the table, may not be
    * <code>null</code> or empty.
    * @param isDataRequired <code>true</code> if the retrieved data cannot be
    * empty; <code>false</code> otherwise.
    * @param colDataType the jdbc data type of the id column, one of the 
    * <code>java.sql.TYPES.XXX</code> values.
    *
    * @return The retrieved dependency data object. It may be <code>null</code>
    * if <code>isDataRequired</code> is <code>false</code> and no data has been
    * retrieved from the database.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if no data has been retrieved from the database
    * and <code>isDataRequired</code> is <code>true</code>, or any other error
    * occurs.
    */
   protected PSDependencyData getDepDataFromTable(PSDependency dep,
      String table, String idCol, boolean isDataRequired, int colDataType)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");

      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(idCol,
         PSJdbcSelectFilter.EQUALS, dep.getDependencyId(), colDataType);
         
      return getDepDataFromTable(table, filter, isDataRequired);
   }

   /**
    * Get dependency data for a given table and filter. Check for retrieved 
    * data, it may not be empty if required.
    * 
    * @param table The table name, may not be <code>null</code> or empty.
    * @param filter The filter used to query the database, it may not be
    * <code>null</code>.
    * @param isDataRequired <code>true</code> if the retrieved data cannot be
    * empty; <code>false</code> otherwise.
    * 
    * @return The retrieved dependency data object. It may be <code>null</code>
    * if <code>isDataRequired</code> is <code>false</code> and no data has been
    * retrieved from the database.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if no data has been retrieved from the database
    * and <code>isDataRequired</code> is <code>true</code>, or any other error
    * occurs.
    */
   protected PSDependencyData getDepDataFromTable(String table, 
      PSJdbcSelectFilter filter, boolean isDataRequired)
      throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null");

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

      PSJdbcTableSchema schema = dbmsHelper.catalogTable(table, false);
      PSJdbcTableData data = dbmsHelper.catalogTableData(
         schema, null, filter);

      PSDependencyData depData = null;
      if (isDataRequired && (data == null || (!data.getRows().hasNext())))
      {
         Object[] args = {table, filter.toString()};
         throw new PSDeployException(IPSDeploymentErrors.CANNOT_FIND_DATA, 
            args);
      }
      else if (data != null && data.getRows().hasNext())
      {
         depData = new PSDependencyData(schema, data);
      }

      return depData;
   }
   
   /**
    * Get the specified dependency data from the archive file for a given
    * dependency file.
    *
    * @param archive The archive handler for the archive file, may not be
    * <code>null</code>.
    * @param file The to be retrieved dependency file, may not be
    * <code>null</code> and its type must be
    * {@link PSDependencyFile#TYPE_DBMS_DATA}
    *
    * @return The dependency data, it will not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyData getDepDataFromFile(PSArchiveHandler archive,
      PSDependencyFile file) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (file == null)
         throw new IllegalArgumentException("file may not be null");

      Document dataDoc = null;
      if (file.getType() == PSDependencyFile.TYPE_DBMS_DATA)
      {
         dataDoc = createXmlDocument(archive.getFileData(file));
      }
      else
      {
         Object[] args =
         {
            PSDependencyFile.TYPE_ENUM[file.getType()],
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_DBMS_DATA]
         };
         throw new PSDeployException(
            IPSDeploymentErrors.WRONG_DEPENDENCY_FILE_TYPE, args);
      }

      // convert docs to objects
      PSJdbcDataTypeMap typeMap = PSDbmsHelper.getInstance().getDataTypeMap();
      PSDependencyData depData = null;
      try {
         depData = new PSDependencyData(dataDoc.getDocumentElement(), typeMap);
      }
      catch (PSUnknownNodeTypeException e) {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.getLocalizedMessage());
      }

      return depData;
   }


   /**
    * Install a dependency data for a given dependency definition. The
    * dependency definition table must not contain any relationship to its
    * child. It will map the id column to current/target server, then install
    * the mapped or transfered data. Log the action at the end.
    *
    * @param depData The to be installed dependency data, may not be
    * <code>null</code>.
    * @param dep The dependency definition object, may not be <code>null</code>
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * @param table The dependency definition table name, may not be
    * <code>null</code> or empty.
    * @param idCol The id column, may not be <code>null</code> or empty.
    * @param nameCol The name column, may not be <code>null</code> or empty.
    * @param depHandlerType The handler type, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected void installDepDataForDepDef(PSDependencyData depData,
      PSDependency dep, PSImportCtx ctx, String table, String idCol,
      String nameCol, String depHandlerType) throws PSDeployException
   {
      if (depData == null)
         throw new IllegalArgumentException("depData may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if (nameCol == null || nameCol.trim().length() == 0)
         throw new IllegalArgumentException("nameCol may not be null or empty");
      if (depHandlerType == null || depHandlerType.trim().length() == 0)
         throw new IllegalArgumentException(
            "depHandlerType may not be null or empty");

      PSJdbcTableSchema schema = depData.getSchema();
      PSJdbcTableData data = depData.getData();

      PSIdMapping mapping = getIdMapping(ctx, dep);

      PSJdbcTableData newData = transferDepIdInDepData(data, table, idCol, 
         mapping);

      // set the update key to be the id column in case there's no primary key
      List<String> cols = new ArrayList<String>();
      cols.add(idCol);
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      dbmsHelper.setUpdateKeyForSchema(cols.iterator(), schema);
      
      installDepDataForDepDef(schema, newData, dep, ctx);
   }

   /**
    * Install a dependency data for a given dependency definition. Assume
    * all ids have been transfered or mapped to the current (or target) 
    * server. Log the action at the end.
    * 
    * @param schema The to be installed schema, may not be <code>null</code>.
    * @param data The to be installed table data, may not be <code>null</code>.
    * @param dep The dependency definition object, may not be <code>null</code>
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected void installDepDataForDepDef(
      PSJdbcTableSchema schema, PSJdbcTableData data,
      PSDependency dep, PSImportCtx ctx) throws PSDeployException
   {
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");
      if (data == null)
         throw new IllegalArgumentException("data may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      schema.setAllowSchemaChanges(false); // don't want to change the table!
      
      PSDbmsHelper.getInstance().processTable(schema, data);

      addTransactionLogForDep(dep, ctx, data.getName());
   }
      
   /**
    * Add a transaction log entry for a installed dependency.
    *
    * @param dep The installed dependency, it may not be <code>null</code>.
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * @param table The name of the table that has been affected, may not be
    * <code>null</code> or empty.
    * @throws PSDeployException 
    */
   protected void addTransactionLogForDep(PSDependency dep, PSImportCtx ctx, 
      String table) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (table == null || table.trim().length() ==0)
         throw new IllegalArgumentException("table may not be null or empty");

      PSIdMapping mapping = getIdMapping(ctx, dep);

      int transAction = getRowAction(ctx, dep);

      // make sure mapping is reset after update
      if (mapping != null)
         mapping.setIsNewObject(false);

      addTransactionLogEntry(dep, ctx, table, PSTransactionSummary.TYPE_DATA, 
         transAction);
   }

   /**
    * Transfer the specified id in a table data from source server to the
    * current (target) server.
    *
    * @param data The to be transfered table data, assume not <code>null</code>
    * @param tableName The table name, assume not <code>null</code> or empty.
    * @param idColumn The id column, assume not <code>null</code> or empty.
    * @param mapping The id mapping object for the transferation, assume not
    * <code>null</code>.
    *
    * @return The transfered table data, will never be <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   @SuppressWarnings("unchecked")
   private PSJdbcTableData transferDepIdInDepData(PSJdbcTableData data,
      String tableName, String idColumn, PSIdMapping mapping)
         throws PSDeployException
   {
      // get the source row
      List tgtRowList = new ArrayList();
      Iterator rows = data.getRows();
      if (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the id as we go
         List tgtColList = new ArrayList();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            if (mapping == null)
            {
               tgtColList.add(col);
            }
            else
            {
               String colName = col.getName();
               if (colName.equalsIgnoreCase(idColumn))
                  col.setValue(mapping.getTargetId());
               tgtColList.add(col);
            }
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_REPLACE);
         tgtRowList.add(tgtRow);
      }
      else // no rows
      {
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);
      }

      PSJdbcTableData newData = new PSJdbcTableData(tableName,
         tgtRowList.iterator());

      return newData;
   }

   /**
    * Get a list of dependency files for a specified dependency from an archive.
    *
    * @param archive The archive handler to retrieve the dependency files from,
    * may not be <code>null</code>.
    * @param dep The dependency object, may not be <code>null</code>.
    *
    * @return An iterator one or more <code>PSDependencyFile</code> objects. It
    * will never be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there is no dependency file in the
    * archive for the specified dependency object, or any other error occurs.
    */
   protected Iterator getDependecyDataFiles(PSArchiveHandler archive,
      PSDependency dep) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator files = archive.getFiles(dep);

      if (! files.hasNext())
      {
         Object[] args =
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_DBMS_DATA],
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      return files;
   }

   /**
    * Convenience method that calls {@link #deleteDepIdFromTable(PSDependency, 
    * PSImportCtx, String, String, PSJdbcTableSchema, int) 
    * deleteDepIdFromTable(dep, ctx, table, idCol, schema, Types.INTEGER)}
    * See the actual implementation
    * @param dep 
    * @param ctx 
    * @param table 
    * @param idCol 
    * @param schema 
    * @return <code>true</code> if delete else <code>false</code>
    * @throws PSDeployException 
    */
   protected boolean deleteDepIdFromTable(PSDependency dep, PSImportCtx ctx,
      String table, String idCol, PSJdbcTableSchema schema)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");

      return deleteDepIdFromTable(dep, ctx, table, idCol, schema, 
         Types.INTEGER);
   }
   
   /**
    * Delete a specified dependency id from a database table. The dependency id
    * will be a transformed the id from the ctx id map if found. It will also
    * log the deletion to the log transaction table if the delete operation
    * effected the database table.
    *
    * @param dep The to be deleted dependency object, it may not be
    * <code>null</code>.
    * @param ctx The import context used to transform the dependency id for
    * deletion if found in id map, it may not be <code>null</code>.
    * @param table The name of the database table, it may not be
    * <code>null</code> or empty.
    * @param idCol The id column name, it may not be <code>null</code> or empty.
    * @param schema The database schema for the table, it may not be
    * <code>null</code>.
    * @param colDataType the jdbc data type of the id column, one of the 
    * <code>java.sql.TYPES.XXX</code> values.
    *
    * @return <code>true</code> if the table has been affected; 
    * <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected boolean deleteDepIdFromTable(PSDependency dep, PSImportCtx ctx,
      String table, String idCol, PSJdbcTableSchema schema, int colDataType)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");

      // get the target/mapped id
      PSIdMapping depMapping = getIdMapping(ctx, dep);
      String id = (depMapping == null) ? dep.getDependencyId() :
         depMapping.getTargetId();

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

      // query the table first, see if there is anything to delete
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(idCol,
         PSJdbcSelectFilter.EQUALS, id, colDataType);

      PSJdbcTableData data = dbmsHelper.catalogTableData(
         schema, null, filter);

      if (data != null && data.getRows().hasNext())
      {
         // do the delete
         PSJdbcRowData rowData = dbmsHelper.getRowDataForOneColumn(idCol, id,
            PSJdbcRowData.ACTION_DELETE);
         
         // copy the schema object so we don't modify the souce
         schema = new PSJdbcTableSchema(schema);
         schema.setAllowSchemaChanges(false);
         dbmsHelper.setUpdateKeyForSchema(idCol, schema);
         dbmsHelper.processTable(schema, table, rowData);
         
         // update the log transaction for the delete action
         addTransactionLogEntry(dep, ctx, table, PSTransactionSummary.TYPE_DATA, 
            PSTransactionSummary.ACTION_DELETED);      
         
         return true; 
      }  
      else // there is nothing to delete
      {
         return false;
      }
   }

   /**
    * Creates a dependency file from a given dependency data object.
    *
    * @param depData The dependency data object, may not be <code>null</code>.
    *
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyFile getDepFileFromDepData(PSDependencyData depData)
      throws PSDeployException
   {
      if (depData == null)
         throw new IllegalArgumentException("depData may not be null");

      Document doc;

      doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.replaceRoot(doc, depData.toXml(doc));
      File dataFile = createXmlFile(doc);

      return new PSDependencyFile(PSDependencyFile.TYPE_DBMS_DATA, dataFile);
   }

   /**
    * Installing dependency data for a given dependency object.
    *
    * @param schema The schema of the to be installed data, it may not be
    * <code>null</code>.
    * @param data The to be installed dependency data, it may not 
    * <code>null</code>.
    * @param dep The dependency object, it may not be <code>null</code>.
    * @param ctx The import context to aid in the installation, it may not be
    * <code>null</code>.
    * @param action The action to be logged, it is one of the 
    * <code>PSTransactionSummary.ACTION_XXX</code> values.
    * @param updateKey The update-key for processing table, it may be
    * <code>null</code> if no need to be used during process table. For 
    * example update-key is not needed if the table has a primary key.
    * 
    * @throws PSDeployException if any error occurs.
    */
   protected void installDependencyData(PSJdbcTableSchema schema, 
      PSJdbcTableData data, PSDependency dep, PSImportCtx ctx, int action, 
      String updateKey) throws PSDeployException
   {
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");
      if (data == null)
         throw new IllegalArgumentException("data may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
       if (! PSTransactionSummary.isActionValid(action))
         throw new IllegalArgumentException(
            "action must be one of the PSTransactionSummary.ACTION_XXX values");

      // copy the schema object so we don't modify the source
      schema = new PSJdbcTableSchema(schema);
     
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();      
      if ( updateKey != null )
         dbmsHelper.setUpdateKeyForSchema(updateKey, schema);
         
      dbmsHelper.processTable(schema, data);

      // update the log transaction
      addTransactionLogEntry(dep, ctx, data.getName(),
         PSTransactionSummary.TYPE_DATA, action);
   }

   /**
    * Get the value from a specified row and column. The value may be 
    * <code>null</code> or empty.
    * 
    * @param table The table name, it may not be <code>null</code> or empty.
    * @param col The column name, it may not be <code>null</code> or empty.
    * @param row The row of the column, it may not be <code>null</code>.
    * 
    * @return <code>null</code> if the value of the column is <code>null</code>
    * or empty; otherwise, return a non-empty <code>String</code>.
    * 
    * @throws PSDeployException if cannot find the column in the row.
    */
   protected String getColumnValueNullable(String table, String col, 
      PSJdbcRowData row) throws PSDeployException
   {
      if (row == null)
         throw new IllegalArgumentException("row may not be null");
      if (col == null || col.trim().length() == 0)
         throw new IllegalArgumentException("col may not be null or empty");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");

      PSJdbcColumnData cdata= row.getColumn(col);
      if (cdata == null) // the column not exist
      {
         Object[] args = {table, col, "null"};
         throw new PSDeployException(
             IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }
      if (cdata.getValue() != null && cdata.getValue().trim().length() == 0)
         return null;
      else
         return cdata.getValue();
   }

   /**
    * Get the value from a specified row and column. The value may not be 
    * <code>null</code> or empty.
    * 
    * @param table The table name, it may not be <code>null</code> or empty.
    * @param col The column name, it may not be <code>null</code> or empty.
    * @param row The row of the column, it may not be <code>null</code>.
    * 
    * @return The value, never <code>null</code> or empty.
    * 
    * @throws PSDeployException if the specified column is not found, or if the
    * value is <code>null</code> or empty.
    */
   protected String getRequiredColumnValue(String table, String col, 
      PSJdbcRowData row) throws PSDeployException
   {
      if (row == null)
         throw new IllegalArgumentException("row may not be null");
      if (col == null || col.trim().length() == 0)
         throw new IllegalArgumentException("col may not be null or empty");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");

      String val = getColumnValueNullable(table, col, row);
      if (val == null)
      {
         Object[] args = {table, col, "null"};
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }
      
      return val;
   }

   /**
    * Get a list of child dependencies for a given parent id. The id of the
    * child dependencies is pair id, the combination of its parent and itself 
    * ids. 
    * 
    * @param tok The security token, may not be <code>null</code>.
    * @param table The table name, used to retrieve the ids from, may not be
    * <code>null</code> or empty.
    * @param childIdCol The child id column of the <code>table</code>, may not 
    * be <code>null</code> or empty.
    * @param parentIdCol The parent id column of the <code>table</code>, may 
    * not be <code>null</code> or empty.
    * @param parentId The parent id, may not be <code>null</code> or empty.
    * @param childDepType The dependency type of the child, may not be
    * <code>null</code> or empty.
    * @param childDepScope The scope to be set for the created child 
    * dependencies. It must be one of the <code>PSDependency.TYPE_XXX</code> 
    * values, or <code>-1</code> if not set scope for the child dependencies and
    * their scope will be the default value.
    * 
    * @return A list over zero or more <code>PSDependency</code> objects, it
    * will never be <code>null</code>, but may be empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if any error occurs.
    */
   protected List<PSDependency> getChildDepsWithPairIdFromParentID(PSSecurityToken tok,
      String table, String childIdCol, String parentIdCol, String parentId,
      String childDepType, int childDepScope) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (childIdCol == null || childIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childIdCol may not be null or empty");
      if (parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");
      if (childDepType == null || childDepType.trim().length() == 0)
         throw new IllegalArgumentException(
            "childDepType may not be null or empty");
      
      Iterator childIDs = getChildPairIdsFromTable(table, childIdCol, 
         parentIdCol, parentId);

      List<PSDependency> childDeps = getDepsFromIds(childIDs, childDepType, tok);

      // set dependency scope if needed
      if ( childDepScope != -1 )
      {
         Iterator deps = childDeps.iterator();
         while (deps.hasNext())
         {
            PSDependency dep = (PSDependency) deps.next();
            dep.setDependencyType(childDepScope);
         }
      }

      return childDeps;
   }
    
   /**
    * Get a list of (child) ids for a given parent id. The child id is a pair of
    * its parent and itself ids. Get all (child) ids when the parent id, 
    * <code>parentId</code> is <code>null</code>.
    * 
    * @param table The table name, used to retrieve the ids from, it may not be
    * <code>null</code> or empty.
    * @param childIdCol The child id column of the <code>table</code>, it may 
    * not be <code>null</code> or empty.
    * @param parentIdCol The parent id column of the <code>table</code>, it may 
    * not be <code>null</code> or empty.
    * @param parentId The parent id, it may be <code>null</code> if want to get
    * all child ids.
    * 
    * @return An iterator over zero or more (pair) ids in <code>String</code>,
    * will never be <code>null</code>, but may be empty.
    * 
    * @throws PSDeployException if any error occurs.
    */
   protected Iterator getChildPairIdsFromTable(String table, String childIdCol, 
      String parentIdCol, String parentId)
      throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (childIdCol == null || childIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "childIdCol may not be null or empty");
      if (parentIdCol == null || parentIdCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentIdCol may not be null or empty");

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      String[] columns = {childIdCol, parentIdCol};
      PSJdbcSelectFilter filter = null;
      if (parentId != null)
      {
         filter = new PSJdbcSelectFilter(parentIdCol, 
            PSJdbcSelectFilter.EQUALS, parentId, Types.INTEGER);
      }
      PSJdbcTableData data = dbmsHelper.catalogTableData(
         table, columns, filter);

      // use "Set" to make sure it is a distinct list
      Set<String> ids = new HashSet<String>();

      if (data != null && data.getRows().hasNext())
      {
         Iterator rows = data.getRows();
         String childId;
         String depId;
         PSJdbcRowData row;
         while (rows.hasNext())
         {
            row = (PSJdbcRowData)rows.next();
            childId = dbmsHelper.getColumnString(table, childIdCol,
               row);
            parentId = dbmsHelper.getColumnString(table, parentIdCol,
               row);
            depId = PSPairDependencyId.getPairDependencyId(
               parentId, childId);
            ids.add(depId);
         }
      }
      return ids.iterator();
   }
      
}
