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
    * @param guid The guid, may not be <code>null</code>.
    * 
    * @return The name of the dependency element which corresponds to the guid
    * or <code>null</code> if not found.
    */
   public String findName(IPSGuid guid);
}
