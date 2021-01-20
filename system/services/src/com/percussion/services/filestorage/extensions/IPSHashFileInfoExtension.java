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
package com.percussion.services.filestorage.extensions;

import com.percussion.services.filestorage.IPSFileStorageService;

/**
 * A marker interface to signify that the extension works with 
 * hash fields used for store and retrieving files 
 * with the {@link IPSFileStorageService}.
 * <p>
 * Fields that have are suffixed with {@value #HASH_PARAM_SUFFIX}
 * will be considered to be special hash fields that contain
 * either the hash (checksum) or the file being uploaded.
 * 
 * @author adamgent
 * @see PSFileStorageTools#isHashField(Number, String)
 * @see IPSFileStorageService
 */
public interface IPSHashFileInfoExtension
{
   
   /**
    * The prefix for hash fields.
    */
   public static final String HASH_PARAM_SUFFIX = "_hash";
}
