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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.error.PSRuntimeException;


/**
 * This class is used when processing code determines that the database is in
 * an inconsistent state. The parameters should provide sufficient information
 * so that support staff can track the problem down and fix it.
 */
public class PSCorruptDatabaseException extends PSRuntimeException
{
   /**
    * Creates an exception with text describing the problem.
    *
    * @param keyType If the key param is also supplied, this should be the
    *    cms type of the key (example, content type or slot). If key is not
    *    provided, it should be the cms object which seems to have the
    *    problem. It could also be the table name if that is known. Never
    *    <code>null</code> or empty.
    *
    * @param key The update or primary key value (concatenate multiple column
    *    keys into this string, seperated with semicolons) which was being used
    *    when the problem was found. If unknown, empty or <code>null</code> may
    *    be supplied. For example "Article" or "13".
    *
    * @param message Optional descriptive text that would aid in tracking the
    *    problem down. For example: "Multiple content types found with same
    *    id.". May be <code>null</code> or empty.
    */
   public PSCorruptDatabaseException(String objectType, String key,
         String message)
   {
      super(IPSCmsErrors.CORRUPT_DATABASE_ENTRY);
      if ( null == objectType || objectType.trim().length() == 0  )
         throw new IllegalArgumentException("Type identifier must be supplied");

      if ( null == key )
         key = "";

      if ( null == message )
         message = "";
      Object [] args = { objectType, key, message };
      super.setArgs(getErrorCode(), args);
   }
}
