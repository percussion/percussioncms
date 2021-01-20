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


package com.percussion.services.pkginfo;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.pkginfo.data.PSIdName;
import com.percussion.utils.guid.IPSGuid;

/**
 * Service for saving, loading, and deleting dependency element id-name
 * mappings.
 */
public interface IPSIdNameService
{
   /**
    * Delete all id-name mappings, used for unit testing only.
    */
   public void deleteAll();

   /**
    * Save the supplied id-name mapping to the repository.
    * 
    * @param mapping The mapping to save, may not be <code>null</code>.
    */
   public void saveIdName(PSIdName mapping);
   
   /**
    * Get an id for the given name (case-insensitive) and type.
    * 
    * @param name The dependency name, may not be <code>null</code> or empty.
    * @param type The dependency system type, may not be <code>null</code>.
    * 
    * @return An <code>IPSGuid</code> object or <code>null</code> if not found.
    */
   public IPSGuid findId(String name, PSTypeEnum type);
   
   /**
    * Get a name for the given <code>IPSGuid</code>.
    * 
    * @param id The guid, may not be <code>null</code>.
    * 
    * @return The name of the dependency element which corresponds to the guid
    * or <code>null</code> if not found.
    */
   public String findName(IPSGuid guid);
}
