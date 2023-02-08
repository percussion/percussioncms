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
 
package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSIdTypeManager;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class to provide common functionality to handlers implementing the
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
           throws PSDeployException, PSNotFoundException {
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
   public static List<PSDependency> getIdTypeDependencies(PSSecurityToken tok,
      PSDependency dep, PSDependencyHandler handler) throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (handler == null)
         throw new IllegalArgumentException("handler may not be null");
            
      List<PSDependency> deps = new ArrayList<>();
      
      // get dependencies specified by id type map
      if(dep.supportsIdTypes()) {
         Iterator mappings = PSIdTypeManager.getIdTypeDependencies(tok, dep);
         while (mappings.hasNext()) {
            PSApplicationIDTypeMapping mapping =
                    (PSApplicationIDTypeMapping) mappings.next();
            String type = mapping.getType();
            if (type.equals(PSApplicationIDTypeMapping.TYPE_NONE) ||
                    type.equals(PSApplicationIDTypeMapping.TYPE_UNDEFINED)) {
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
            if (mapping.getParentId() != null) {
               // get parent
               PSDependencyHandler parentHandler = handler.getDependencyHandler(
                       mapping.getParentType());
               childDep = parentHandler.getDependency(tok, mapping.getParentId());
            } else
               childDep = depHandler.getDependency(tok, mapping.getValue());

            // add child
            if (childDep != null && !childDep.getKey().equals(dep.getKey()))
               deps.add(childDep);
         }
      }
      return deps;
   }

   public static boolean isIdTypeMappingEnabled(){
      return false; //TODO: drive this off of the operating mode
   }
}
