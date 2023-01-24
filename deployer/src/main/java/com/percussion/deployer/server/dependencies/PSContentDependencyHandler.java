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
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Class to handle packaging and deploying a content deployable element.
 */
public class PSContentDependencyHandler extends PSDataObjectDependencyHandler
{

   /**
    * Construct the dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSContentDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
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

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      return getChildDepsFromParentID(CONTENT_TABLE, CONTENT_ID, 
         CONTENTTYPE_ID, dep.getDependencyId(), 
         PSContentDefDependencyHandler.DEPENDENCY_TYPE, tok).iterator();
   }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      List deps = new ArrayList();

      Iterator defDeps = getContentTypeHandler().getDependencies(tok);
      while (defDeps.hasNext())
      {
         PSDependency defDep = (PSDependency)defDeps.next();
         PSDependency dep = new PSDeployableElement(PSDependency.TYPE_SHARED,
            defDep.getDependencyId(), DEPENDENCY_TYPE,
            m_def.getObjectTypeName(), defDep.getDisplayName(),
            m_def.supportsIdTypes(), m_def.supportsIdMapping(),
            m_def.supportsUserDependencies(), m_def.supportsParentId());

         deps.add(dep);
      }

      return deps.iterator();
   }


   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      PSDependency dep = null;
      PSDependency ctDep = getContentTypeHandler().getDependency(tok, id);
      if (ctDep != null)
      {
         dep = new PSDeployableElement(PSDependency.TYPE_SHARED,
            ctDep.getDependencyId(), DEPENDENCY_TYPE,
            m_def.getObjectTypeName(), ctDep.getDisplayName(),
            m_def.supportsIdTypes(), m_def.supportsIdMapping(),
            m_def.supportsUserDependencies(), m_def.supportsParentId());
      }
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContentDef</li>
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

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getContentTypeHandler().getDependency(tok, id) != null;
   }
   
   /**
    * See {@link com.percussion.deployer.server.dependencies.PSDependencyHandler#isRequiredChild(String)} for details.
    * 
    * @return <code>false</code> always as all items of the content types 
    * represented by this handler are not required if not included.
    */
   public boolean isRequiredChild(String type)
   {
      // delegate validation
      super.isRequiredChild(type);
      
      return false;
   }

   /**
    * Get the Content Type handler. Initialize the handler if it has not been 
    * set. This is doing a lazy load of the handler because it may not be 
    * available when constructing this object.
    *
    * @return The content type handler object. It will never be 
    * <code>null</code>.
    */
   private com.percussion.deployer.server.dependencies.PSDependencyHandler getContentTypeHandler()
   {
      if (m_ctHandler == null)
         m_ctHandler = getDependencyHandler(
            PSCEDependencyHandler.DEPENDENCY_TYPE);

      return m_ctHandler;
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "Content";

   /**
    * The content type handler, initialized by
    * <code>getContentTypeHandler()</code> if it is <code>null</code>,
    * will never be <code>null</code> after that.
    */
   private PSDependencyHandler m_ctHandler = null;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   private final static String CONTENT_ID = "CONTENTID";
   private final static String CONTENTTYPE_ID = "CONTENTTYPEID";
   private final static String CONTENT_TABLE = "CONTENTSTATUS";
   
   
   static
   {
      ms_childTypes.add(PSContentDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
