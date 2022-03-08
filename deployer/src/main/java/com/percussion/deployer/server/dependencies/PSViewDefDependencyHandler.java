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

package com.percussion.deployer.server.dependencies;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;

/**
 * Class to handle packaging and deploying a view definition.
 */
public class PSViewDefDependencyHandler extends PSSearchObjectDependencyHandler
{
   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   public PSViewDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }
   
   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   protected boolean isDependentType(PSSearch search)
   {
      return search.isView();
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "ViewDef";

   /**
    * Discovers all literal id's within the object represented by the supplied
    * dependency.
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param dep The dependency represeting the object to introspect for literal
    *            IDs.  Must be of the type expected by the derived handler, may not be
    *            <code>null</code>.
    * @return The id types discovered, all mappings created with their type
    * set as undefined, never <code>null</code>.
    * @throws IllegalArgumentException if <code>dep</code> is invalid.
    * @throws PSDeployException        if there are any errors.
    */
   @Override
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep) throws PSDeployException {
      return null;
   }

   /**
    * Transforms literal Ids in the supplied object as specified by the supplied
    * ID Types and ID Map.  Also transforms the ID in the corresponding ID type
    * mapping so it will reflect the object on the local system, so the
    * <code>idTypes</code> should be saved after calling this method.
    *
    * @param object  The object to transform, may not be <code>null</code> and
    *                must be of the type expected by the derived class.
    * @param idTypes The id types map containing all defined id type mappings,
    *                may not be <code>null</code>.  Will be modified; the literal id's
    *                within the mappings will be transformed using the supplied id map.
    * @param idMap   The id map containing id mappings for all literal id's
    *                defined in the supplied <code>idTypes</code>, never <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException        if there are any errors.
    */
   @Override
   public void transformIds(Object object, PSApplicationIDTypes idTypes, PSIdMap idMap) throws PSDeployException {

   }
}
