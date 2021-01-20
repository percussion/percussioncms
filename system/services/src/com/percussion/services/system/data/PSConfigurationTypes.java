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
package com.percussion.services.system.data;

/**
 * Represents all types of configurations that are loadable by name.
 */
public enum PSConfigurationTypes
{
   /**
    * Indicates a configuration containing server page tags
    */
   SERVER_PAGE_TAGS(1, "serverPageTags.xml"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing tidy properties
    */
   TIDY_CONFIG(2, "tidy.properties"), //$NON-NLS-1$

   /**
    * Indicates the logging configuration
    */
   LOG_CONFIG(3, "log4j.xml"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing navigation properties
    */
   NAV_CONFIG(4, "Navigation.properties"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing workflow properties
    */
   WF_CONFIG(5, "rxworkflow.properties"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing the thumbnail URL properties
    */
   THUMBNAIL_CONFIG(6, "addThumbnailURL.properties"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing the System Velocity macros
    */
   SYSTEM_VELOCITY_MACROS(7, "sys_assembly.vm"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing the User Velocity macro overrides
    */
   USER_VELOCITY_MACROS(8, "rx_assembly.vm"), //$NON-NLS-1$

   /**
    * Indicates a configuration containing the custom auth types
    */
   AUTH_TYPES(9, "authtypes.properties"); //$NON-NLS-1$

   /**
    * Ctor taking the id and the file name for the configuration type.
    * 
    * @param id
    * @param fileName 
    */
   private PSConfigurationTypes(int id, String fileName)
   {
      mi_id = id;
      mi_fileName = fileName;
   }

   /**
    * Get the id of the config type.
    * 
    * @return config type id
    */
   public int getId()
   {
      return mi_id;
   }

   /**
    * Get the description of the configuration type. It is looked up in the
    * properties bundle with the key being the name of the type.
    * 
    * @return description from the property bundle, never <code>null</code>
    * may be empty.
    */
   public String getDescription()
   {
      // TODO get it from the bundle.
      return name();
   }

   /**
    * Get the description of the configuration type. It is looked up in the
    * properties bundle with the key being the name of the type.
    * 
    * @return description from the property bundle, never <code>null</code>
    * may be empty.
    */
   public String getFileName()
   {
      return mi_fileName;
   }

   /**
    * Unique id for the configuration type. Added just to make it more flexible
    * than using the ordinal as id.
    */
   private int mi_id;

   /**
    * File name for the configuration type.
    */
   private String mi_fileName;
}
