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

package com.percussion.data;

import com.percussion.error.PSIllegalArgumentException;


/**
 * The PSStatementColumnMapper class is a hash map used to store XML field
 * to back-end column mappings. This differs from the object store version
 * as it is used to bind data to a prepared back-end statement. The mapper
 * is used during the XML tree walking to quickly associate data with the
 * back-end statement.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSStatementColumnMapper extends java.util.HashMap {
   /**
    * Construct an empty mapper for storing the XML field to back-end
    * column mappings. This differs from the object store version as it
    * is used to bind data to a prepared back-end statement. The mapper
    * is used during the XML tree walking to quickly associate data with
    * the back-end statement.
    */
   public PSStatementColumnMapper()
   {
      super();
   }

   /**
    * Stores a mapping between the XML field and its associated back-end
    * statement column.
    *
    * @param      xmlField    the name of the XML field using hierarchical
    *                         notation (Object/SubObject/Field)
    *
    * @param      beCol       the back-end column
    *
    * @return     the previous value stored with the specified key, or
    *             <code>null</code> if no previous value was defined
    *
    * @exception  NullPointerException if xmlField or beCol is null
    *
    * @exception   IllegalArgumentException   if xmlField is not a String
    *                                          object or beCol is not a
    *                                          PSStatementColumn object
    */
   public synchronized Object put(Object xmlField, Object beCol)
      throws NullPointerException, IllegalArgumentException
   {
      if (xmlField == null)
         throw new NullPointerException("xmlField == null");
      if (beCol == null)
         throw new NullPointerException("beCol == null");

      if (!(xmlField instanceof java.lang.String)) {
         Object[] args = { xmlField.getClass().getName() };
         throw new IllegalArgumentException(
            new PSIllegalArgumentException(
            IPSDataErrors.COLMAPPER_XML_FIELD_NOT_STRING, args).toString());
      }
      else if (!(beCol instanceof com.percussion.data.PSStatementColumn)) {
         Object[] args = { beCol.getClass().getName() };
         throw new IllegalArgumentException(
            new PSIllegalArgumentException(
            IPSDataErrors.COLMAPPER_BE_COL_NOT_STMTCOL, args).toString());
      }

      return super.put(xmlField, beCol);
   }
}

