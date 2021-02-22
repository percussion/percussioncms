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

package com.percussion.services.filestorage.impl;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.PSMetaDataCache;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.server.PSServer;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.IPSHashedFieldCataloger;
import com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.error.PSBinaryMigrationException;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;

import org.springframework.transaction.annotation.Transactional;

public class PSHashedFieldCataloger implements IPSHashedFieldCataloger
{

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
      for (PSField fs : systemDef.getFieldSet().getAllFields())
      {
         fields.add(fs);
      }
      Iterator sharedGroupsIt = sharedDef.getFieldGroups();
      while (sharedGroupsIt.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) sharedGroupsIt.next();
         for (PSField fs : group.getFieldSet().getAllFields())
         {
            fields.add(fs);
         }
      }

      // PSSharedFieldGroup fg =
      // (PSSharedFieldGroup)sharedDef.getFieldGroups().next();

      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

      long[] typeIds = itemDefMgr.getAllContentTypeIds(-1);
      for (int i = 0; i < typeIds.length; i++)
      {
         PSItemDefinition itemDef;
         try
         {
            itemDef = itemDefMgr.getItemDef(typeIds[i], -1);
            PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
            PSFieldSet fieldSet = mapper.getFieldSet();

            for (PSField fs : fieldSet.getAllFields())
            {
               fields.add(fs);
            }
         }
         catch (PSInvalidContentTypeException e)
         {
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

      Connection conn = null;
      ResultSet rs = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection();
         String schema = PSConnectionHelper.getConnectionDetail().getOrigin();
         DatabaseMetaData md = conn.getMetaData();
         for (PSHashedColumn column : dbColumns)
         {

            rs = md.getColumns(null, schema, column.getTablename(), column.getColumnName());
            if (rs.next())
               column.setColumnExists(true);
            rs.close();
            if (serverColumns.contains(column))
            {
               column.setFieldExists(true);
            }
         }

      }
      catch (NamingException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      finally
      {
         try
         {
            if (rs!=null) rs.close();
         }
         catch (SQLException e)
         {
            // Ignore
         }
         try
         {
            if(conn!=null) conn.close();
         }
         catch (SQLException e)
         {
            // Ignore
         }
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
