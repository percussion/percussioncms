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
