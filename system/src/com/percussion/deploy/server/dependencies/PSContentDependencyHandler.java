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
package com.percussion.deploy.server.dependencies;


import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDeployableElement;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.security.PSSecurityToken;

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
      throws PSDeployException
   {
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
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
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
      throws PSDeployException
   {
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
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getContentTypeHandler().getDependency(tok, id) != null;
   }
   
   /**
    * See {@link PSDependencyHandler#isRequiredChild(String)} for details.
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
   private PSDependencyHandler getContentTypeHandler()
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
