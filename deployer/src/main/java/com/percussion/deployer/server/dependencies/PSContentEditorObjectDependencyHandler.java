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
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.design.objectstore.PSChoiceTableInfo;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.error.PSNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for handlers that deploy content editor objects.
 */
public abstract class PSContentEditorObjectDependencyHandler
   extends PSAppObjectDependencyHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    */
   public PSContentEditorObjectDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   /**
    * Checks the supplied ui Def for dependencies
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param uiDef The ui def to check, may not be <code>null</code>.
    *
    * @return list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException if there are any errors.
    */
   @SuppressWarnings("unchecked")
   protected List<PSDependency> checkUIDef(PSSecurityToken tok,
      PSUIDefinition uiDef) throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (uiDef == null)
         throw new IllegalArgumentException("uiDef may not be null");

      List<PSDependency> childDeps = new ArrayList<>();

      Iterator defSets = uiDef.getDefaultUI();
      if (defSets != null)
      {
         while (defSets.hasNext())
         {
            PSUISet uiSet = (PSUISet)defSets.next();
            childDeps.addAll(checkUiSet(tok, uiSet));
         }
      }

      PSDisplayMapper dispMapper = uiDef.getDisplayMapper();
      childDeps.addAll(checkDisplayMapper(tok, dispMapper));

      return childDeps;
   }
   /**
    * Checks the supplied display mapper for dependencies
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param mapper The mapper to check, may not be <code>null</code>.
    *
    * @return list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException if there are any errors.
    */
   @SuppressWarnings("unchecked")
   protected List<PSDependency> checkDisplayMapper(PSSecurityToken tok,
      PSDisplayMapper mapper) throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");

      List<PSDependency> deps = new ArrayList<>();

      Iterator dispMappings = mapper.iterator();
      while (dispMappings.hasNext())
      {
         PSDisplayMapping dispMapping =
            (PSDisplayMapping)dispMappings.next();

         PSUISet uiSet = dispMapping.getUISet();
         deps.addAll(checkUiSet(tok, uiSet));

         PSDisplayMapper childMapper = dispMapping.getDisplayMapper();
         if (childMapper != null)
         {
            deps.addAll(checkDisplayMapper(tok, childMapper));
         }
      }

      return deps;
   }


   /**
    * Checks the supplied ui set for dependencies
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param uiSet The ui set to check, may not be <code>null</code>.
    *
    * @return list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException if there are any errors.
    */
   protected List<PSDependency> checkUiSet(PSSecurityToken tok, PSUISet uiSet)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");

      List<PSDependency> deps = new ArrayList<>();
      PSChoices choices = uiSet.getChoices();

      PSDependencyHandler keywordHandler = getDependencyHandler(
         PSKeywordDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler controlHandler = getDependencyHandler(
         PSControlDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler schemaHandler = getDependencyHandler(
         PSSchemaDependencyHandler.DEPENDENCY_TYPE);

      if (choices != null)
      {
         PSDependency dep = null;
         int type = choices.getType();
         if (type == PSChoices.TYPE_GLOBAL)
         {
            dep = keywordHandler.getDependency(tok, String.valueOf(
               choices.getGlobal()));
         }
         else if (type == PSChoices.TYPE_TABLE_INFO)
         {
            PSChoiceTableInfo ctInfo = choices.getTableInfo();
            
            // include schema from repository datasource only
            if (ctInfo.getDataSource().trim().length() == 0)
            {
               dep = schemaHandler.getDependency(tok, ctInfo.getTableName());
            }
         }
      
         if (dep != null)
         {
            if (dep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               dep.setIsAssociation(false);
            }
            
            deps.add(dep);
         }
      }

      PSControlRef controlRef = uiSet.getControl();
      if (controlRef != null)
      {
         String controlName = controlRef.getName();
         PSDependency controlDep = controlHandler.getDependency(tok,
            controlName);
         if (controlDep != null)
         {
            int type = controlDep.getDependencyType();
            if (type == PSDependency.TYPE_SHARED)
            {
               controlDep.setIsAssociation(false);
            }
            
            deps.add(controlDep);
         }

      }

      return deps;
   }

   /**
    * Checks the given dependency for child dependencies that are of Server
    * dependency type. (PSDependency.TYPE_SERVER) 
    * 
    * These controls are no longer allowed to be packaged because they should 
    * already be on the target system and any changes to target versions would 
    * be lost.)
    * 
    * This method should be called during the deploy phase (as opposed to 
    * building phase of package installation) to ensure that all potential 
    * control files have been accounted for.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param dep The dependency whose child dependencies are checked for being
    * a Server Dependency, may not be <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   protected void checkServerControls(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator<PSDependency> deps = 
         dep.getDependencies(PSControlDependencyHandler.DEPENDENCY_TYPE);
      while (deps.hasNext())
      {
         PSDependency ctrlDep = deps.next();
         int ctrlType = ctrlDep.getDependencyType();
         if (ctrlType == PSDependency.TYPE_SERVER)
         {
            String elementName = dep.getDisplayName();
            String controlName = ctrlDep.getDisplayName();
            Object[] args = {elementName, controlName};
            throw new PSDeployException(
                  IPSDeploymentErrors.CONTROL_NOT_PACKAGEABLE, args);
         }
      }   
   }

   /**
    * Transforms ids in the supplied ui definition
    *
    * @param idMap The id map to use, may not be <code>null</code>.
    * @param uiDef The ui Definition to transform, may not be <code>null</code>.
    *
    * @throws PSDeployException if any errors occur.
    */
   @SuppressWarnings("unchecked")
   protected void transformUIDef(PSIdMap idMap, PSUIDefinition uiDef)
      throws PSDeployException
   {
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (uiDef == null)
         throw new IllegalArgumentException("uiDef may not be null");

      Iterator defSets = uiDef.getDefaultUI();
      if (defSets != null)
      {
         while (defSets.hasNext())
         {
            PSUISet uiSet = (PSUISet)defSets.next();
            transformUiSet(idMap, uiSet);
         }
      }

      PSDisplayMapper dispMapper = uiDef.getDisplayMapper();
      transformDisplayMapper(idMap, dispMapper);
   }

   /**
    * Transforms ids in the supplied display mapper.
    *
    * @param idMap The id map to use, may not be <code>null</code>.
    * @param mapper The mapper to transform, may not be <code>null</code>.
    *
    * @throws PSDeployException if any errors occur.
    */
   @SuppressWarnings("unchecked")
   protected void transformDisplayMapper(PSIdMap idMap, PSDisplayMapper mapper)
      throws PSDeployException
   {
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (mapper == null)
         throw new IllegalArgumentException("mapper may not be null");

      Iterator dispMappings = mapper.iterator();
      while (dispMappings.hasNext())
      {
         PSDisplayMapping dispMapping =
            (PSDisplayMapping)dispMappings.next();

         PSUISet uiSet = dispMapping.getUISet();
         transformUiSet(idMap, uiSet);

         PSDisplayMapper childMapper = dispMapping.getDisplayMapper();
         if (childMapper != null)
         {
            transformDisplayMapper(idMap, childMapper);
         }
      }

   }

   /**
    * Transforms ids in the supplied ui set.
    *
    * @param idMap The id map to use, may not be <code>null</code>.
    * @param uiSet The uiSet to transform, may not be <code>null</code>.
    *
    * @throws PSDeployException if any errors occur.
    */
   protected void transformUiSet(PSIdMap idMap, PSUISet uiSet)
      throws PSDeployException
   {
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");

      PSChoices choices = uiSet.getChoices();

      if (choices != null && choices.getType() ==
         PSChoices.TYPE_GLOBAL)
      {
         choices.setGlobal(idMap.getNewIdInt(
            String.valueOf(choices.getGlobal()),
            PSKeywordDependencyHandler.DEPENDENCY_TYPE));
      }
   }

   /**
    * Get the shared def.
    *
    * @return The def, never <code>null</code>.
    *
    * @throws PSDeployException if the def cannot be loaded.
    */
   protected PSContentEditorSharedDef getSharedDef() throws PSDeployException
   {
      PSContentEditorSharedDef sharedDef = PSServer.getContentEditorSharedDef();
      if (sharedDef == null)
      {
         // result of shared def not loading, server will have already logged
         // an error for this.
         Object[] args = {"Cannot load shared def"};
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            args);
      }
      return sharedDef;
   }

   /**
    * Get all tables from the supplied container locator
    *
    * @param locator The locator to check, may not be <code>null</code>.
    *
    * @return Iterator over zero or more table names as <code>String</code>
    * objects, never <code>null</code>, may be empty.
    */
   public static Iterator getLocatorTables(PSContainerLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      List<String> tables = new ArrayList<>();

      Iterator tableSets = locator.getTableSets();
      while (tableSets.hasNext())
      {
         PSTableSet tableSet = (PSTableSet)tableSets.next();
         Iterator refs = tableSet.getTableRefs();
         while (refs.hasNext())
         {
            PSTableRef ref = (PSTableRef)refs.next();
            tables.add(ref.getName());
         }
      }

      return tables.iterator();
   }


   /**
    * Get dependencies for all tables from the supplied container locator
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param locator The locator to check, may not be <code>null</code>.
    *
    * @return list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException if there are any errors.
    */
   protected List<PSDependency> checkLocatorTables(PSSecurityToken tok,
      PSContainerLocator locator)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      PSDependencyHandler schemaHandler = getDependencyHandler(
         PSSchemaDependencyHandler.DEPENDENCY_TYPE);

      List<PSDependency> childDeps = new ArrayList<>();
      Iterator tables = getLocatorTables(locator);
      while (tables.hasNext())
      {
         String tableName = (String)tables.next();
         PSDependency schemaDep =
            schemaHandler.getDependency(tok, tableName);
         if (schemaDep != null)
            childDeps.add(schemaDep);
      }

      return childDeps;
   }
}
