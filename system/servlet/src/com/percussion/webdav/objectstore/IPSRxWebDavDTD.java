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
