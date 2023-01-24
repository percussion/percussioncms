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
