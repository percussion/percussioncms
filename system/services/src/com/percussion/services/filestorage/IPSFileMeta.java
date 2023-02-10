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
