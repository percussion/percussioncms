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

