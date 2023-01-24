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

package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;

/**
 * Interface for classes that can discover and transform literal identifiers in
 * objects represented by dependencies that support ID types.
 */
public interface IPSIdTypeHandler
{
   /**
    * Discovers all literal id's within the object represented by the supplied
    * dependency.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param dep The dependency represeting the object to introspect for literal 
    * IDs.  Must be of the type expected by the derived handler, may not be 
    * <code>null</code>.
    * 
    * @return The id types discovered, all mappings created with their type
    * set as undefined, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>dep</code> is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException;
   
   /**
    * Transforms literal Ids in the supplied object as specified by the supplied 
    * ID Types and ID Map.  Also transforms the ID in the corresponding ID type 
    * mapping so it will reflect the object on the local system, so the 
    * <code>idTypes</code> should be saved after calling this method.
    * 
    * @param object The object to transform, may not be <code>null</code> and 
    * must be of the type expected by the derived class.
    * @param idTypes The id types map containing all defined id type mappings,
    * may not be <code>null</code>.  Will be modified; the literal id's
    * within the mappings will be transformed using the supplied id map.
    * @param idMap The id map containing id mappings for all literal id's 
    * defined in the supplied <code>idTypes</code>, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public void transformIds(Object object, PSApplicationIDTypes idTypes, 
      PSIdMap idMap) throws PSDeployException;

}
