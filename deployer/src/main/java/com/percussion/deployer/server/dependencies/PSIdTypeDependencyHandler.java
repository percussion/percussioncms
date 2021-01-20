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
 
package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSIdTypeManager;
import com.percussion.security.PSSecurityToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class to provide common functionality to handlers impelementing the 
 * {@link com.percussion.deployer.server.IPSIdTypeHandler IPSIdTypeHandler}
 * interface.
 */
public abstract class PSIdTypeDependencyHandler extends PSDependencyHandler
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
    */
   public PSIdTypeDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   /**
    * Convenience method that calls {@link #getIdTypeDependencies(
    * PSSecurityToken, PSDependency, PSDependencyHandler) 
    * getIdTypeDependencies(tok, dep, this)}
    */
   protected List<PSDependency> getIdTypeDependencies(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      return getIdTypeDependencies(tok, dep, this);
   }

   /**
    * Gets all child dependencies identified by the id types for the supplied 
    * dependency.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param dep The dependency to get children for, may not be 
    * <code>null</code>.
    * @param handler The handler to use to for loading of other handlers, may 
    * not be <code>null</code>.
    * 
    * @return The children, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */   
   public static final List<PSDependency> getIdTypeDependencies(PSSecurityToken tok, 
      PSDependency dep, PSDependencyHandler handler) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (handler == null)
         throw new IllegalArgumentException("handler may not be null");
            
      List<PSDependency> deps = new ArrayList<PSDependency>();
      
      // get dependencies specified by id type map
      Iterator mappings = PSIdTypeManager.getIdTypeDependencies(tok, dep);
      while (mappings.hasNext())
      {
         PSApplicationIDTypeMapping mapping = 
            (PSApplicationIDTypeMapping)mappings.next();
         String type = mapping.getType();
         if (type.equals(PSApplicationIDTypeMapping.TYPE_NONE) || 
            type.equals(PSApplicationIDTypeMapping.TYPE_UNDEFINED))
         {
            continue;
         }
         
         PSDependencyHandler depHandler = handler.getDependencyHandler(
           type);
         // If type supports parent ids, one will be specified in the mapping.
         // If so, add the parent as a dependency, not the child.  The child
         // will be handled by the addition of the parent (currently only state
         // defintions have parents, and the state is a local dependency of the
         // workflow definition).
         PSDependency childDep;
         if (mapping.getParentId() != null)
         {
            // get parent 
            PSDependencyHandler parentHandler = handler.getDependencyHandler(
               mapping.getParentType());
            childDep = parentHandler.getDependency(tok, mapping.getParentId());
         }
         else
            childDep = depHandler.getDependency(tok, mapping.getValue());
         
         // add child
         if (childDep != null && !childDep.getKey().equals(dep.getKey()))
            deps.add(childDep);
      }
      
      return deps;
   }
}
