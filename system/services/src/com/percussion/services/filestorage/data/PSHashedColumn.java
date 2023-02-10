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
package com.percussion.services.filestorage.data;

import com.percussion.services.filestorage.data.PSHashedColumn.HashedColumnsPK;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
@Table(name = "PSX_BINARYCOLUMNS")
@IdClass(HashedColumnsPK.class)
public class PSHashedColumn implements Serializable
{
   @Id
   @Column(name = "TABLE_NAME", nullable = false)
   private String tableName;

   @Id
   @Column(name = "COLUMN_NAME", nullable = false)
   private String columnName;

   @Column(name = "FIELD_NAME", nullable = true)
   private String fieldName;
 
   @Transient
   private boolean columnExists;
 
   @Transient
   private boolean fieldExists;
   
   public PSHashedColumn()
   {

   }

   public PSHashedColumn(String field, String tableName, String columnName)
   {
      this.fieldName = field;
      this.columnName = columnName;
      this.tableName = tableName;
   }

   public String getFieldName()
   {
      return fieldName;
   }

   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }

   public String getTablename()
   {
      return tableName;
   }

   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }

   public String getColumnName()
   {
      return columnName;
   }

   public void setColumnName(String columnName)
   {
      this.columnName = columnName;
   }

   
   public boolean isColumnExists()
   {
      return columnExists;
   }

   public void setColumnExists(boolean columnExists)
   {
      this.columnExists = columnExists;
   }

   public boolean isFieldExists()
   {
      return fieldExists;
   }

   public void setFieldExists(boolean fieldExists)
   {
      this.fieldExists = fieldExists;
   }
   
   private static final long serialVersionUID = 1;

   private static final Logger ms_logger = LogManager.getLogger(PSHashedColumn.class);

   @Embeddable
   public static class HashedColumnsPK implements Serializable
   {
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
         result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         HashedColumnsPK other = (HashedColumnsPK) obj;
         if (columnName == null)
         {
            if (other.columnName != null)
               return false;
         }
         else if (!columnName.equals(other.columnName))
            return false;
         if (tableName == null)
         {
            if (other.tableName != null)
               return false;
         }
         else if (!tableName.equals(other.tableName))
            return false;
         return true;
      }

      /**
       * 
       */
      private static final long serialVersionUID = 3835328669018349555L;

      protected String tableName;

      protected String columnName;

      public HashedColumnsPK()
      {
      }

      public HashedColumnsPK(String tableName, String columnName)
      {
         this.tableName = tableName;
         this.columnName = columnName;
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
      result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSHashedColumn other = (PSHashedColumn) obj;
      if (columnName == null)
      {
         if (other.columnName != null)
            return false;
      }
      else if (!columnName.equals(other.columnName))
         return false;
      if (tableName == null)
      {
         if (other.tableName != null)
            return false;
      }
      else if (!tableName.equals(other.tableName))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "PSHashedColumn [tableName=" + tableName + ", columnName=" + columnName + ", fieldName=" + fieldName
            + ", columnExists=" + columnExists + ", fieldExists=" + fieldExists + "]";
   }

  
}
