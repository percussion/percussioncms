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

package com.percussion.tablefactory;

/**
 * This class encapsulates the WHERE clause of the SELECT statement.
 * For usage, see the main method of PSJdbcSelectFilter class.
 */
public class PSJdbcFilterContainer extends PSJdbcSelectFilter
{
   /**
    * Joins the filter to the filters already contained in the container
    * using AND clause.
    * @param filter contains a single condition of the WHERE clause, may not
    * be <code>null</code>
    */
   public void doAND(PSJdbcSelectFilter filter)
   {
      if (m_filter.trim().length() != 0)
      {
         m_filter = "(" + m_filter + ")";
         m_filter += STR_AND;
         m_filter += "(" + filter + ")";
      }
      else
      {
         m_filter = filter.toString();
      }
   }

   /**
    * Joins the filter to the filters already contained in the container
    * using OR clause.
    * @param filter contains a single condition of the WHERE clause, may not
    * be <code>null</code>
    */
   public void doOR(PSJdbcSelectFilter filter)
   {
      if (m_filter.trim().length() != 0)
      {
         m_filter = "(" + m_filter + ")";
         m_filter += STR_OR;
         m_filter += "(" + filter + ")";
      }
      else
      {
         m_filter = filter.toString();
      }
   }
}

