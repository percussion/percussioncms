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
package com.percussion.install;

import com.percussion.tablefactory.IPSJdbcTableDataHandler;
import com.percussion.tablefactory.PSJdbcColumnData;

/**
 * Data handler object which makes all the values of the column (specified in
 * the handler's Xml) non-null.
 *
 * All the <code>null</code> values in this column are modified based on the
 * default value specified in the handler's Xml. Non-null column are not
 * modified.
 */
public class PSJdbcNonNullColumn extends PSJdbcUniqueColumn
   implements IPSJdbcTableDataHandler
{
   /**
    * If <code>value</code> is null then returns a column data object whose
    * value equals the default value (specified in the handler's Xml) for the
    * column being altered, otherwise returns a column data having the
    * value <code>value</code>.
    *
    * See {@link PSJdbcUniqueColumn#getColumnValue(String)} for details.
    */
   protected PSJdbcColumnData getColumnValue(String value)
   {
      if (value == null)
         value = m_value;
      return new PSJdbcColumnData(m_column, value);
   }

}


