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

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.collections.PSIteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PSCustomDependencyHandler extends PSDependencyHandler
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
   public PSCustomDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>TableSchema</li>
    * <li>Exit</li>
    * <li>SharedGroup</li>
    * <li>SystemDefElement</li>
    * </ol>
    * 
    * @return An iterator over zero or more types as <code>String</code> 
    * objects, never <code>null</code>, does not contain <code>null</code> or 
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }
   
   /**
    * The same as {@link #getChildTypes()}, except this returns a list.
    * @return the list of child types, never <code>null</code> or empty.
    */
   public static List<String> getChildTypeList()
   {
      return ms_childTypes;
   }
   
   /**
    * Gets a custom deployable element for each instance of each supported child 
    * type, with the id and name of the element set to the child depedency's 
    * key and display name, and the child dependency set as a local child 
    * dependency. See {@link PSDependencyHandler} for more info.
   */
   public Iterator getDependencies(PSSecurityToken tok)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      List deps = new ArrayList();
      Iterator types = getChildTypes();
      while (types.hasNext())
      {
         String type = (String)types.next();
         PSDependency custDep = createDeployableElement(m_def, type, 
            type);
         Iterator childDeps = getChildDependencies(tok, custDep);
         while (childDeps.hasNext())
         {
            PSDependency childDep = (PSDependency)childDeps.next();
            PSDependency dep = getDependency(tok, childDep.getKey());
            if (dep != null)
               deps.add(dep);
         }
      }
      
      return deps.iterator();
   }
   
   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
           throws PSDeployException, PSNotFoundException {
      return getDependency(tok, id) != null;
   }

   /** 
    * Gets a custom deployable element for the specified id.  The id must be the 
    * dependency key of the child, with the type of the key being a supported 
    * child type. The returned element will have the child's key as its id, and
    * the child's display name as its display name, and the specified child
    * will be added as a local child dependency.  For a User Dependency,
    * just the type is specified as the id and name, and an element with no 
    * children is returned.  See 
    * {@link PSDependencyHandler} for more info.
    */
   public PSDependency getDependency(PSSecurityToken tok, String id)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      
      PSDependency dep = null;         
      if (id.equals(PSUserDependencyHandler.DEPENDENCY_TYPE))
      {
         PSDependencyDef userDef = m_map.getDependencyDef(id);
         if (userDef != null)
            dep = createDeployableElement(m_def, id, 
               userDef.getObjectTypeName());   
      }
      else
      {
         String[] key = PSDependency.parseKey(id);
         String childType = key[0];
         String childId = key[1];
         
         PSDependencyDef childDef = m_map.getDependencyDef(childType);
         if (childDef != null)
         {
            PSDependencyHandler childHandler = getDependencyHandler(childType);
            PSDependency childDep = childHandler.getDependency(tok, childId);
            // only create dependency if child not a system/server dep
            if (childDep != null && childDep.canBeIncludedExcluded())
            {
               childDep.setDependencyType(PSDependency.TYPE_LOCAL);
               dep = createDeployableElement(m_def, id, 
                  childDep.getDisplayName());   
               dep.setDependencies(PSIteratorUtils.iterator(childDep));
            }
         }
      }
      return dep;
   }
   
   /**
    * Returns the provided dependency with all children of the specified type
    * as its children.  The id of the provided <code>dep</code> must be the 
    * type of the children that will be returned.  See 
    * {@link PSDependencyHandler} for more info.
    */
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      List childList = new ArrayList();
      String childType = dep.getDependencyId();
      PSDependencyHandler childHandler = getDependencyHandler(childType);
      Iterator childDeps = childHandler.getDependencies(tok);
      while (childDeps.hasNext())
      {
         PSDependency childDep = (PSDependency)childDeps.next();
         // don't return non-deployable children
         if (!childDep.canBeIncludedExcluded())
            continue;
         childDep.setDependencyType(PSDependency.TYPE_LOCAL);
         childList.add(childDep);
         
      }
      
      return childList.iterator();
   }
   
   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = "Custom";

   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();

   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSDataDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSchemaDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSharedGroupDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSUserDependencyHandler.DEPENDENCY_TYPE);
   }
   
}
