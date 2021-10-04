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

   @Transactional
   public Set<PSHashedColumn> getStoredColumns()
   {
      return dao.getStoredColumns();
   }

   @Override
   @Transactional
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
