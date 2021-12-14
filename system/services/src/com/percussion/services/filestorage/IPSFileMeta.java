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
package com.percussion.services.filestorage;

import java.util.Map;

/**
 * The properties of a file meta data.
 * 
 * @author yubingchen
 */
public interface IPSFileMeta extends Map<String, String>
{
   /**
    * Gets the value of the specified property
    * 
    * @param propName the name of the property, may be <code>null</code> or
    *           empty.
    * 
    * @return the value of the property, it may be <code>null</code> if does not
    *         exist.
    */
   public String get(String propName);

   /**
    * Returns the mime type from the metadata.
    * 
    * @return mime type
    */
   public String getMimeType();

   /**
    * Returns the length in bytes of the binary
    * 
    * @return
    */
   public long getLength();

   /**
    * Returns the filename when the file was originally uploaded or modified
    * from the Admin console.
    * 
    * @return
    */
   public String getOriginalFilename();

   /**
    * Returns the encoding of the file if approprate e.g. textual documents
    * <code>null</code> otherwise
    * 
    * @return the encoding
    */
   public String getEncoding();

   /**
    * A string containing any parse error occurred during metadata extraction for
    * the item <code>null</code> otherwise.
    * 
    * @return
    */
   public String getParseError();

}
