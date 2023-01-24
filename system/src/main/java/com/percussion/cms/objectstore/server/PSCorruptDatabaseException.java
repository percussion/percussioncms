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
