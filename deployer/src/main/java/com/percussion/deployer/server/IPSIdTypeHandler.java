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

package com.percussion.deployer.server;

import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.security.PSSecurityToken;

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
      throws PSDeployException;
   
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
