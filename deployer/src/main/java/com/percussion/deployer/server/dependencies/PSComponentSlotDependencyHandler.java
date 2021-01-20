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
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a component slot
 */
public class PSComponentSlotDependencyHandler extends PSPairIdDependencyHandler
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
   public PSComponentSlotDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // get SHARED component child dependencies
      PSJdbcSelectFilter filter = getFilterForPairId(dep.getDependencyId(), 
         CR_PARENT_ID, CR_NAME);
      PSJdbcTableData data = PSDbmsHelper.getInstance().catalogTableData(
         CR_TABLE, new String[] {CR_CHILD_ID}, filter);
      
      Iterator ids = getIdsFromTableData(data, CR_TABLE, CR_CHILD_ID);      
      List childDeps = getDepsFromIds(ids, 
         PSComponentDefDependencyHandler.DEPENDENCY_TYPE, tok, -1);
            
      return childDeps.iterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      Iterator ids;
      ids = getChildPairIdsFromTable(CR_TABLE, CR_NAME, CR_PARENT_ID, null);

      return getDepsFromIds(ids, DEPENDENCY_TYPE, tok).iterator();
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id, CR_TABLE, CR_NAME, CR_PARENT_ID);         
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ComponentDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   protected String getPairParentType()
   {
      return PSComponentDefDependencyHandler.DEPENDENCY_TYPE;
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      PSJdbcSelectFilter filter = getFilterForPairId(dep.getDependencyId(), 
         CR_PARENT_ID, CR_NAME);
      PSDependencyData depData = getDepDataFromTable(CR_TABLE, filter, true);
 
      List files = new ArrayList();     
      files.add(getDepFileFromDepData(depData));
      
      return files.iterator();
   }

   // see base class
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
       if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      // retrieve the data from the archive
      Iterator files = getDependecyDataFiles(archive, dep);
      PSDependencyFile file = (PSDependencyFile)files.next();
      PSDependencyData crData = getDepDataFromFile(archive, file);
 
      // delete from TABLE where PARENT_ID=parentId and NAME=childId
      deleteDepFromTable(tok, dep, ctx, crData.getSchema(), CR_TABLE, 
         CR_NAME, CR_PARENT_ID, 
         PSComponentDefDependencyHandler.DEPENDENCY_TYPE);
           
      // install the data
      PSJdbcTableData newData = crData.getData();
      newData = transferIdsForCRData(newData, ctx, tok);
      installDependencyData(crData.getSchema(), newData, dep, ctx, 
         PSTransactionSummary.ACTION_CREATED, null);
   }

   /**
    * Transfer ids from the given data for the <code>CR_TABLE</code>.
    * 
    * @param srcData The data from the source server, assume not
    * <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param tok The security token, assume not <code>null</code>.
    *
    * @return The transfered table data, it will never <code>null</code>, all
    * rows will have their action set to {@link PSJdbcRowData#ACTION_INSERT}.
    *
    * @throws PSDeployException if an error occurs.
    */ 
   private PSJdbcTableData transferIdsForCRData(
      PSJdbcTableData srcData, PSImportCtx ctx,
      PSSecurityToken tok) throws PSDeployException
   {
      List rows = PSDeployComponentUtils.cloneList(srcData.getRows());
      
      if (rows.isEmpty())  // not expecting no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);
      
      PSDependency dep;

      // get the source row
      List tgtRowList = new ArrayList();
      for (int i=0; i < rows.size(); i++)
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.get(i);

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for CR_CHILD_ID and CR_PARENT_ID
         List tgtColList = new ArrayList();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(CR_CHILD_ID) || 
                colName.equalsIgnoreCase(CR_PARENT_ID))
            {
               PSIdMapping mapping = getIdMapping(ctx, col.getValue(), 
                  PSComponentDefDependencyHandler.DEPENDENCY_TYPE);
               if (mapping != null)
                  col.setValue(mapping.getTargetId());
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(CR_TABLE,
         tgtRowList.iterator());

      return newData;
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "ComponentSlot";

   // Constants for component slot table and column names
   private final static String CR_TABLE = "RXSYSCOMPONENTRELATIONS";
   private final static String CR_NAME = "COMPONENTSLOTNAME";
   private final static String CR_CHILD_ID = "CHILDCOMPONENTID";
   private final static String CR_PARENT_ID = "COMPONENTID";

    /**
    * The schema for CR_TABLE, initialized by constructor, will never
    * be <code>null</code> or modified after that.
    */
   PSJdbcTableSchema m_crSchema;
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSComponentDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
