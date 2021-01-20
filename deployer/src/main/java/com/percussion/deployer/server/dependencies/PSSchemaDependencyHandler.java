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

import com.percussion.data.PSDatabaseMetaData;
import com.percussion.data.PSMetaDataCache;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.tablefactory.IPSJdbcTableChangeListener;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcTableChangeEvent;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a schema definition.
 */
public class PSSchemaDependencyHandler extends PSDataObjectDependencyHandler
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
    * @throws PSDeployException if the system table schema cannot be loaded.
    */
   public PSSchemaDependencyHandler(PSDependencyDef def,
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
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      PSDbmsInfo dbmsInfo = 
         PSDbmsHelper.getInstance().getServerRepositoryInfo();         
         
      
      // catalog the table names
      List<String> tableList = new ArrayList<String>();            
      String filterAll = "%";
      String db = dbmsInfo.getDatabase();
      if (db.trim().length() == 0)
         db = filterAll;
      String schema = dbmsInfo.getOrigin();
      if (schema.trim().length() == 0)
         schema = filterAll;
      
      Connection conn = PSDbmsHelper.getInstance().getRepositoryConnection();
      ResultSet rs = null;   
      try
      {
         DatabaseMetaData meta = conn.getMetaData();
         rs = meta.getTables(db, schema, 
            filterAll, new String[] {"TABLE"});
         if (rs != null)
         {
            while (rs.next())
            {
               tableList.add(rs.getString(COLNO_TABLE_NAME));
            }
         }
      } 
      catch (SQLException e) 
      {
         throw new PSDeployException(
            IPSDeploymentErrors.REPOSITORY_READ_WRITE_ERROR, 
            PSDeployException.formatSqlException(e));
      }
      finally 
      {
         if (rs != null)
            try { rs.close();} catch (SQLException e){}

         if (conn != null)
            try { conn.close();} catch (SQLException e){}
      }
      
      List<String> excludeTables = PSDependencyUtils.getSharedGroupTables();
      excludeTables.addAll(PSDependencyUtils.getAllContentTypeTables(tok));
      excludeTables.add("PSLOG");
      excludeTables.add("PSLOGDATA");
      
      // create dependency objects from the table names
      List<PSDependency> deps = new ArrayList<PSDependency>();
      for (String tablename : tableList)
      {
         if (excludeTables.contains(tablename) || 
            tablename.endsWith("_BAK") ||
            tablename.endsWith("_BAKUP") ||
            tablename.endsWith("_UPG"))
            continue;
         
         PSDependency dep = getDependency(tok, tablename);
         if (dep != null)
            deps.add(dep);
      }
      
      return deps.iterator();
   }
   
   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      PSJdbcTableSchema schema = null;
      
      try {
         schema = dbmsHelper.catalogTable(id, false);
      }
      catch (PSDeployException e) { 
         schema = null;  // assume cannot find the "id" table
      }

      PSDependency dep = null;
      if (schema != null)
      {
         dep = createDependency(m_def, id, id);
         if (schema.isView())
            dep.setDependencyType(PSDependency.TYPE_SERVER);
         else
            dep.setDependencyType(dbmsHelper.getDependencyType(id));
      }
         
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @SuppressWarnings("unchecked")
   public Iterator getChildTypes()
   {
      return PSIteratorUtils.emptyIterator();
   }

   // see base class
   @Override
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
      schema = dbmsHelper.catalogTable(dep.getDependencyId(), false);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.replaceRoot(doc, schema.toXml(doc));
      File dataFile = createXmlFile(doc);

      PSDependencyFile file;
      file = new PSDependencyFile(PSDependencyFile.TYPE_DBMS_SCHEMA, 
         dataFile);
      
      List files = new ArrayList();
      files.add(file);

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

      // get the schema document from archive
      Iterator files = getDependecyDataFiles(archive, dep);
      PSDependencyFile file = (PSDependencyFile) files.next();

      PSJdbcTableSchema schema = getSchema(file, archive);
      PSDbmsHelper.getInstance().processTable(schema);

      boolean exists = doesDependencyExist(tok, dep.getDependencyId());
      int transAction = exists ? PSTransactionSummary.ACTION_MODIFIED : 
         PSTransactionSummary.ACTION_CREATED;
      
      addTransactionLogEntry(dep, ctx, dep.getDisplayIdentifier(), 
         PSTransactionSummary.TYPE_SCHEMA, transAction);
   }

   /**
    * Gets the schema from the specified (schema) dependency file.
    * 
    * @param file the schema dependency file, assumed not <code>null</code>.
    * @param archive the archive, assumed not <code>null</code>.
    * 
    * @return the schema that is in the dependency file, never <code>null</code>.
    * 
    * @throws PSDeployException if an error occurs.
    */
   public PSJdbcTableSchema getSchema(PSDependencyFile file,
         PSArchiveHandler archive) throws PSDeployException
   {
      Document doc = null;
      if (file.getType() == PSDependencyFile.TYPE_DBMS_SCHEMA)
      {
         doc = createXmlDocument(archive.getFileData(file));
      }
      else
      {
         Object[] args =
         {
            PSDependencyFile.TYPE_ENUM[file.getType()],
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_DBMS_SCHEMA]
         };
         throw new PSDeployException(
            IPSDeploymentErrors.WRONG_DEPENDENCY_FILE_TYPE, args);
      }

      // convert doc to schema object
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      PSJdbcDataTypeMap typeMap = dbmsHelper.getDataTypeMap();

      PSJdbcTableSchema schema = null;
      try
      {
         schema = new PSJdbcTableSchema(doc.getDocumentElement(), typeMap);
      }
      catch (PSJdbcTableFactoryException e) {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.getLocalizedMessage());
      }
      // handle flushing the dbmd cache on schema change
      addTableChangeHandler(schema);
      
      return schema;
      
   }
   
   /**
    * Add a listener to the schema to flush the table meta data for any changed
    * tables.
    * 
    * @param tableSchema The schema to add the listener to, may not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>schema</code> is 
    * <code>null</code>.
    */
   protected void addTableChangeHandler(PSJdbcTableSchema tableSchema)
   {
      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");
         
      // need to flush server's dbmd cache
      tableSchema.addSchemaChangeListener(
         new IPSJdbcTableChangeListener()
         {
            public void tableChanged(PSJdbcTableChangeEvent e)
            {
               /* make sure its the correct action and that there is connection
                * info (may not have been constructed this way, but should
                * have been for our purposes)
                */
               if (e.getAction() ==
                  PSJdbcTableChangeEvent.ACTION_SCHEMA_CHANGED &&
                     e.usedConnInfo())
               {
                  PSDatabaseMetaData dbmd =
                     PSMetaDataCache.getCachedDatabaseMetaData(
                        e.getConnectionInfo());

                  if (dbmd != null)
                  {
                     String schema = null;
                     try
                     {
                        PSConnectionDetail connDetail = dbmd.getConnectionDetail();
                        schema = connDetail.getOrigin();
                     }
                     catch (SQLException e1)
                     {
                        ms_log.warn("Could not get the schema or origin, " +
                            "cannot flush cache.");
                     }
                     dbmd.flushTableMetaData(e.getTable(), schema);
                  }
               }
            }
         }
      );
   }
   
   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger ms_log = Logger.getLogger(
         "com.percussion.deployer.server.dependencies.PSSchemaDependencyHandler");
   
   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = "Schema";

   /**
    * Column number in result set for table name
    */
   private static final int COLNO_TABLE_NAME      = 3;
}
