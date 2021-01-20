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
package com.percussion.services.pkginfo.utils;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.pkginfo.IPSIdNameService;
import com.percussion.services.pkginfo.PSIdNameServiceLocator;
import com.percussion.services.pkginfo.data.PSIdName;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * The purpose of this class is to provide a set of utilities for converting
 * package (formerly MSM) dependency id's to actual guids which can then be
 * used to represent the associated design objects as stored in the package
 * element information.  This utility is necessary because not all dependency id
 * values are numeric.  Some dependency id's are the actual names of their
 * associated design objects.  For these values, a new guid will be generated
 * and associated with the dependency id name so that the object can later be
 * referenced by name.  Utilizes the id-name service {@link IPSIdNameService}
 * for loading/saving of id-name mappings.
 */
public class PSIdNameHelper
{
   /**
    * Get an id for the given dependency id and type.
    * 
    * @param id The dependency id.  May be an object name, a numeric ID, or an
    * actual guid.  May not be <code>null</code> or empty.
    * @param type The system type for the dependency object.  May not be
    * <code>null</code>.
    * 
    * @return An {@link IPSGuid} for the given dependency id and type, never
    * <code>null</code>.
    */
   public static IPSGuid getGuid(String id, PSTypeEnum type)
   {
      if (StringUtils.isBlank(id))
      {
         throw new IllegalArgumentException("id may not be null or empty");
      }
      
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      
      IPSGuid guid;
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      IPSIdNameService idNameSvc = getIdNameService();
      
      if (isSupported(type))
      {
         guid = idNameSvc.findId(id, type);
         if (guid == null)
         {
            // create a new guid
            guid = guidMgr.createGuid(type);
            String guidStr = guid.toString();

            // save the id-name mapping
            idNameSvc.saveIdName(new PSIdName(guidStr, id));    
         }
      }
      else
      {
         // make a guid
         guid = guidMgr.makeGuid(id, type);
      }
              
      return guid;
   }

   /**
    * Get the dependency name which corresponds to the given id.
    * 
    * @param guid The id for which a name will be returned, never
    * <code>null</code>.  Must represent a supported type, see
    * {@link #isSupported(PSTypeEnum)}.
    * 
    * @return The dependency name for the id or <code>null</code> if not found.
    * 
    * @throws IllegalArgumentException if the guid does not represent a
    * supported type.
    */
   public static String getName(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      
      PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
      if (!isSupported(type))
      {
         throw new IllegalArgumentException("unsupported type [" + type + 
               "] for guid [" + guid + "]");
      }
      
      return getIdNameService().findName(guid);
   }
   
   /**
    * Determines if a given type is supported by this helper class.  A type
    * is supported if the dependency type with which it is associated uses name
    * (non-numeric) id values.
    * 
    * @param type The system type, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the type is supported, <code>false</code>
    * otherwise.
    */
   public static boolean isSupported(PSTypeEnum type)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      
      return ms_supportedTypes.contains(type);
   }
   
   /**
    * Get the id name service.  Initialize if necessary.
    * 
    * @return The id name service.  Never <code>null</code>.
    */
   public static IPSIdNameService getIdNameService()
   {
      if (ms_idNameSvc == null)
      {
         ms_idNameSvc = PSIdNameServiceLocator.getIdNameService();
      }
      
      return ms_idNameSvc;
   }
   
   /**
    * See {@link #isSupported(PSTypeEnum)}.  Never <code>null</code>.
    */
   private static Set<PSTypeEnum> ms_supportedTypes = new HashSet<PSTypeEnum>(); 
   
   /**
    * The id-name service, may be <code>null</code>.
    */
   private static IPSIdNameService ms_idNameSvc = null;
   
   static
   {
      ms_supportedTypes.add(PSTypeEnum.ACL);
      ms_supportedTypes.add(PSTypeEnum.APPLICATION);
      ms_supportedTypes.add(PSTypeEnum.AUTH_TYPE);
      ms_supportedTypes.add(PSTypeEnum.COMPONENT_SLOT);
      ms_supportedTypes.add(PSTypeEnum.CONFIGURATION);
      ms_supportedTypes.add(PSTypeEnum.IMAGE_FILE);
      ms_supportedTypes.add(PSTypeEnum.CONTENT);
      ms_supportedTypes.add(PSTypeEnum.CONTENT_ASSEMBLER);
      ms_supportedTypes.add(PSTypeEnum.RELATIONSHIP);
      ms_supportedTypes.add(PSTypeEnum.CONTENT_TYPE_TEMPLATE_DEF);
      ms_supportedTypes.add(PSTypeEnum.CONTROL);
      ms_supportedTypes.add(PSTypeEnum.CUSTOM);
      ms_supportedTypes.add(PSTypeEnum.TABLE_DATA);
      ms_supportedTypes.add(PSTypeEnum.DATABASE_FUNCTION_DEF);
      ms_supportedTypes.add(PSTypeEnum.EXTENSION);
      ms_supportedTypes.add(PSTypeEnum.FOLDER);
      ms_supportedTypes.add(PSTypeEnum.FOLDER_CONTENTS);
      ms_supportedTypes.add(PSTypeEnum.FOLDER_TRANSLATIONS);
      ms_supportedTypes.add(PSTypeEnum.FOLDER_TREE);
      ms_supportedTypes.add(PSTypeEnum.LOADABLE_HANDLER);
      ms_supportedTypes.add(PSTypeEnum.LOCALE);
      ms_supportedTypes.add(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
      ms_supportedTypes.add(PSTypeEnum.ROLE);
      ms_supportedTypes.add(PSTypeEnum.SHARED_GROUP);
      ms_supportedTypes.add(PSTypeEnum.TABLE_SCHEMA);
      ms_supportedTypes.add(PSTypeEnum.STYLESHEET);
      ms_supportedTypes.add(PSTypeEnum.SUPPORT_FILE);
      ms_supportedTypes.add(PSTypeEnum.SYSTEM_DEF);
      ms_supportedTypes.add(PSTypeEnum.TEMPLATE_COMMUNITY_DEF);
      ms_supportedTypes.add(PSTypeEnum.USER_DEPENDENCY);
   }
}
