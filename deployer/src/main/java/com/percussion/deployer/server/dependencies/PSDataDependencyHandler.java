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
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying both schema and data
 */
public class PSDataDependencyHandler extends PSSchemaDependencyHandler
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
    * @throws PSDeployException if the system table schemas cannot be loaded.
    */
   public PSDataDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException 
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

      return PSIteratorUtils.emptyIterator(); // there is no child dependencies
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

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      PSJdbcTableSchema schema;
      PSJdbcTableData data;

      schema = dbmsHelper.catalogTable(dep.getDependencyId(), false);
      // all rows in data have assigned with PSJdbcRowData.ACTION_INSERT
      data = dbmsHelper.catalogTableData(schema, null, null);

      List files = new ArrayList();
      Document doc;
      
      doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.replaceRoot(doc, schema.toXml(doc));
      File schemaFile = createXmlFile(doc);
      files.add(new PSDependencyFile(PSDependencyFile.TYPE_DBMS_SCHEMA, 
         schemaFile));
      
      if (data != null)
      {
         doc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.replaceRoot(doc, data.toXml(doc));
         File dataFile = createXmlFile(doc);
         files.add(new PSDependencyFile(PSDependencyFile.TYPE_DBMS_DATA, 
            dataFile));
      }
            
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

      // get the data from archive
      Document schemaDoc = null;
      Document dataDoc = null;
      Iterator files = archive.getFiles(dep);
      while (files.hasNext())
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_DBMS_SCHEMA)
            schemaDoc = createXmlDocument(archive.getFileData(file));
         else if (file.getType() == PSDependencyFile.TYPE_DBMS_DATA)
            dataDoc = createXmlDocument(archive.getFileData(file));
      }
      
      if (schemaDoc == null)
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_DBMS_SCHEMA], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      
      // convert docs to objects
      PSJdbcDataTypeMap typeMap = PSDbmsHelper.getInstance().getDataTypeMap();
      String fileType = null;
      PSJdbcTableSchema schema = null;
      PSJdbcTableData data = null;
      try 
      {
         fileType = 
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_DBMS_SCHEMA];
         schema = new PSJdbcTableSchema(schemaDoc.getDocumentElement(), 
            typeMap);
         
         if (dataDoc != null)
         {
            fileType = 
               PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_DBMS_DATA];
            data = new PSJdbcTableData(dataDoc.getDocumentElement());
         }   
      }
      catch (PSJdbcTableFactoryException e) 
      {
         Object[] args = 
         {
            fileType, DEPENDENCY_TYPE, dep.getObjectType(), 
            dep.getDependencyId(), dep.getDisplayName(), 
            e.getLocalizedMessage()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      
      boolean exists = doesDependencyExist(tok, dep.getDependencyId());
      int transAction = exists ? PSTransactionSummary.ACTION_MODIFIED :
         PSTransactionSummary.ACTION_CREATED;

      // process the data
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      if (exists && data != null)
      {
         if (schema.getPrimaryKey() != null)
            data = setDataForReplace(data);
         // make sure it is not on create only, so that the data will be
         // processed by the table factory
         data.setOnCreateOnly(false);
      }

      // handle flushing the dbmd cache on schema change
      addTableChangeHandler(schema);
      
      // install the data
      if (data != null)
         dbmsHelper.processTable(schema, data);
      else
         dbmsHelper.processTable(schema);

      addTransactionLogEntry(dep, ctx, dep.getDisplayIdentifier(),
         PSTransactionSummary.TYPE_DATA, transAction);

   }

   /**
    * Creates a new table data from a given table data and set the action to
    * replace, <code>PSJdbcRowData.ACTION_REPLACE</code>, in the new table data.
    *
    * @param srcData The source table data, assume not <code>null</code>,
    * but may be empty.
    *
    * @return The created table data object, will never be <code>null</code>,
    * but may be empty.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSJdbcTableData setDataForReplace(PSJdbcTableData srcData)
      throws PSDeployException
   {
      List tgtRowList = new ArrayList();
      Iterator rows = srcData.getRows();
      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();
         PSJdbcRowData tgtRow = new PSJdbcRowData(srcRow.getColumns(),
            PSJdbcRowData.ACTION_REPLACE);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(srcData.getName(),
         tgtRowList.iterator());

      return newData;
   }


   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = "Data";

}
