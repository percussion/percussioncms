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

