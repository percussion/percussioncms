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


