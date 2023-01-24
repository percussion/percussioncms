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

package com.percussion.services.filestorage.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSServer;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.IPSHashedFieldCataloger;
import com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.error.PSBinaryMigrationException;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PSHashedFieldCataloger implements IPSHashedFieldCataloger
{

   private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);

   IPSHashedFieldCatalogerDAO dao;

   IPSFileStorageService storageSvc;

   public IPSFileStorageService getStorageSvc()
   {
      return storageSvc;
   }

   public void setStorageSvc(IPSFileStorageService storageSvc)
   {
      this.storageSvc = storageSvc;
   }

   
   public Set<PSHashedColumn> getServerHashedColumns()
   {
      PSContentEditorSharedDef sharedDef = PSServer.getContentEditorSharedDef();
      PSContentEditorSystemDef systemDef = PSServer.getContentEditorSystemDef();
      HashSet<PSHashedColumn> columns = new HashSet<>();
      HashSet<PSField> fields = new HashSet<>();
      Collections.addAll(fields, systemDef.getFieldSet().getAllFields());

      Iterator sharedGroupsIt = sharedDef.getFieldGroups();
      while (sharedGroupsIt.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) sharedGroupsIt.next();
         Collections.addAll(fields, group.getFieldSet().getAllFields());
      }

      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

      long[] typeIds = itemDefMgr.getAllContentTypeIds(-1);
      for (long typeId : typeIds) {
         PSItemDefinition itemDef;
         try {
            itemDef = itemDefMgr.getItemDef(typeId, -1);
            PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
            PSFieldSet fieldSet = mapper.getFieldSet();

            Collections.addAll(fields, fieldSet.getAllFields());

         } catch (PSInvalidContentTypeException e) {
            throw new PSBinaryMigrationException("Invalid content type ", e);
         }
      }
      for (PSField field : fields)
      {
         String name = field.getSubmitName();
         if (name.endsWith("_hash"))
         {
            String table = getFieldTable(field);
            String column = getFieldColumn(field);
            if (table!=null && column!=null) {
               columns.add(new PSHashedColumn(field.getSubmitName(), table, column));
            }
         }
      }

      return columns;
   }

   /**
    * Return the fully qualified table name for a PSField object.
    * 
    * @param field
    * @return the tablename including origin if it exists.
    */
   public static String getFieldTable(PSField field)
   {
      IPSBackEndMapping beMapping = field.getLocator();
      String tableName = null;

      if (beMapping instanceof PSBackEndColumn)
      {

         PSBackEndColumn column = (PSBackEndColumn) beMapping;

         tableName = column.getTable().getAlias().toUpperCase();

      }
      
      return tableName;
   }

   /**
    * Return the column name name for a PSField object.
    * 
    * @param field
    * @return the db column name
    */
   public static String getFieldColumn(PSField field)
   {
      IPSBackEndMapping beMapping = field.getLocator();
      String columnName = null;
      if (beMapping instanceof PSBackEndColumn)
      {
         PSBackEndColumn column = (PSBackEndColumn) beMapping;
         columnName = column.getColumn();
      }
      return columnName;
   }

   public IPSHashedFieldCatalogerDAO getDao()
   {
      return dao;
   }

   public void setDao(IPSHashedFieldCatalogerDAO dao)
   {
      this.dao = dao;
   }

   @Override
   @Transactional
   public void storeColumns(Set<PSHashedColumn> columns)
   {
      dao.saveAll(columns);
   }

   public Set<PSHashedColumn> getStoredColumns()
   {
      return dao.getStoredColumns();
   }

   @Override
   public Set<PSHashedColumn> validateColumns()
   {
      Set<PSHashedColumn> serverColumns = getServerHashedColumns();

      storeColumns(serverColumns);

      Set<PSHashedColumn> dbColumns = getStoredColumns();

      try(Connection conn = PSConnectionHelper.getDbConnection())
      {
         String schema = PSConnectionHelper.getConnectionDetail().getOrigin();
         DatabaseMetaData md = conn.getMetaData();
         for (PSHashedColumn column : dbColumns)
         {

            try(ResultSet rs = md.getColumns(null, schema, column.getTablename(), column.getColumnName())) {
               if (rs.next())
                  column.setColumnExists(true);
            }

            if (serverColumns.contains(column))
            {
               column.setFieldExists(true);
            }
         }
      } catch (SQLException | NamingException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }

      return dbColumns;
   }

   @Override
   @Transactional
   public void addColumn(String table, String column)
   {

      PSHashedColumn newCol = new PSHashedColumn("[external]", table, column);
      dao.save(newCol);
   }

   @Override
   @Transactional
   public void removeColumn(String table, String column)
   {
      PSHashedColumn col = new PSHashedColumn("", table, column);
      dao.remove(col);
   }

}
