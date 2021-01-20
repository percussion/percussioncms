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

import org.apache.log4j.Logger;

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

   private static Logger ms_logger = Logger.getLogger(PSHashedColumn.class);

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
