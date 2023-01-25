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
package com.percussion.webdav.objectstore;


/**
 * This interface is used to group all XML constants for the
 * RxWebDav.dtd for ease of maintenance
 */
public interface IPSRxWebDavDTD
{

   // Elements
   public String ELEM_CONFIG = "PSXWebdavConfigDef";
   public String ELEM_CONTENTTYPE = "PSXWebdavContentType";
   public String ELEM_MIMETYPES = "MimeTypes";
   public String ELEM_PROPERTYMAP = "PropertyMap";
   public String ELEM_MIMETYPE = "MimeType";
   public String ELEM_PROPERTYFIELD_MAPPING = "PSXPropertyFieldNameMapping";
   public String ELEM_FIELDNAME = "FieldName";
   public String ELEM_EXCLUDE_FOLDER_PROPERTIES = "ExcludeFolderProperties";
   public String ELEM_PROPERTY_NAME = "PropertyName";

   // Attributes
   public String ATTR_ROOT = "root";
   public String ATTR_COMMUNITY_NAME = "communityname";
   public String ATTR_COMMUNITY_ID = "communityid";
   public String ATTR_NAME = "name";
   public String ATTR_ID = "id";
   public String ATTR_CONTENTFIELD = "contentfield";
   public String ATTR_OWNERFIELD = "ownerfield";
   public String ATTR_DEFAULT = "default";
   public String ATTR_LOCALE = "locale";
   public String ATTR_DELETEAS = "deleteas";
   public String ATTR_VALUE_PURGE = "purge";
   public String ATTR_PUBLICFLAGS = "PublicValidTokens";
   public String ATTR_QEFLAGS = "QEValidTokens";

}
