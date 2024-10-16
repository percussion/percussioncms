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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.deployer.client.IPSDeployConstants;
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

/**
 * Class to handle packaging and installing folder defintion dependencies.
 * See {@link PSFolderDependencyHandler} class description for more
 * information on folder dependencies.
 */
public class PSFolderDefDependencyHandler
   extends PSFolderObjectDependencyHandler
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
   public PSFolderDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List childDeps = new ArrayList();

      // use folder's type to get child folder type
      childDeps.addAll(getChildDependencies(tok, dep.getDependencyId(),
         dep.getDependencyType()));

      // get folder contents
      com.percussion.deployer.server.dependencies.PSDependencyHandler contentsHandler = getDependencyHandler(
         com.percussion.deployer.server.dependencies.PSFolderContentsDependencyHandler.DEPENDENCY_TYPE);
      PSDependency ctDep = contentsHandler.getDependency(tok,
         dep.getDependencyId());
      if (ctDep != null)
         childDeps.add(ctDep);

      return childDeps.iterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // ancestors not supported
      return PSIteratorUtils.emptyIterator();
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;

      PSComponentSummary sum = getFolderSummary(getRelationshipProcessor(tok), id);
      if (sum != null)
         dep = createDependency(m_def, id, sum.getName());

      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>CommunityDef</li>
    * <li>DisplayFormatDef</li>
    * <li>FolderContents</li>
    * <li>FolderDef</li>
    * <li>FolderTranslation</li>
    * <li>RoleDef</li>
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

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   /**
    * See {@link PSDependencyHandler#isRequiredChild(String)} for details.
    * 
    * @return <code>false</code> for types matching this handler's type, 
    * <code>true</code> otherwise.
    */
   public boolean isRequiredChild(String type)
   {
      // delegate validation
      super.isRequiredChild(type);
      
      return !ms_optChildTypes.contains(type);
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_FOLDER_DEF;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();
   
   /**
    * List of optional child types, it will never be <code>null</code> or empty.
    */
   private static List ms_optChildTypes = new ArrayList();   

   static
   {      
      ms_optChildTypes.add(DEPENDENCY_TYPE);
      ms_optChildTypes.add(PSFolderContentsDependencyHandler.DEPENDENCY_TYPE);
      ms_optChildTypes.add(
         PSFolderTranslationsDependencyHandler.DEPENDENCY_TYPE);
      
      ms_childTypes.addAll(ms_optChildTypes);
      ms_childTypes.add(PSCommunityDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSDisplayFormatDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
