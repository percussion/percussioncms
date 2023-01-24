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

package com.percussion.cms;

import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.PSBackEndTable;

/**
 * This class contains the data required to create a PSDataMapping when using
 * the PSApplicationBuilder to create a dataset.
 */
public class PSSystemMapping
{
   /**
    * Constructor for this class.
    *
    * @param table The backend table to use to create the backend column when
    * creating this mapping.
    * @param columnName The column name to use to create the backend column when
    * creating this mapping.
    * @param docMapping The replacement value to use when creating this mapping.
    *
    * @throws IllegalArgumentException if any param is <code>null</code>.
    */
   public PSSystemMapping(PSBackEndTable table, String columnName,
      IPSDocumentMapping docMapping)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      if (columnName == null)
         throw new IllegalArgumentException("columnName may not be null");

      if (docMapping == null)
         throw new IllegalArgumentException("docMapping may not be null");

      m_table = table;
      m_columnName = columnName;
      m_docMappping = docMapping;
   }

   /**
    * Returns the table name to use to create the backend column when creating
    * this mapping.
    *
    * @return The table name, never <code>null</code>.
    */
   public String getTableName()
   {
      return m_table.getTable();
   }

   /**
    * Returns the column name to use to create the backend column when creating
    * this mapping.
    *
    * @return The column name, never <code>null</code>.
    */
   public String getColumnName()
   {
      return m_columnName;
   }

   /**
    * Returns the document mapping (replacement value) to use when creating
    * this mapping.
    *
    * @return The document mapping, never <code>null</code>.
    */
   public IPSDocumentMapping getDocMapping()
   {
      return m_docMappping;
   }

   /**
    * Returns the table to use to create the backend column when creating
    * this mapping.
    *
    * @return The table, never <code>null</code>.
    */
   public PSBackEndTable getTable()
   {
      return m_table;
   }


   /**
    * The table used by the backend column in this mapping.  Initialized in the
    * constructor, never <code>null</code> after that.
    */
   private PSBackEndTable m_table;

   /**
    * The name of the backend column in this mapping.  Initialized in the
    * constructor, never <code>null</code> after that.
    */
   private String m_columnName;

   /**
    * The documentMapping to use in this mapping.  Initialized in the
    * constructor, never <code>null</code> after that.
    */
   private IPSDocumentMapping m_docMappping;
}
