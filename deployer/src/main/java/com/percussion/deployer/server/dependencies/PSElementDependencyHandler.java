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
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.security.PSSecurityToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * The abstract parent class for all deployable element (or concept) handlers.
 * It contains convenient methods for those concept handlers. The concept
 * handlers do not have any concrete objects associate with them in the system,
 * they have only one child dependency and they will always operate on the
 * child handler for any of the dependency handler's operations, which are
 * defined in this class.
 */
public abstract class PSElementDependencyHandler extends PSDependencyHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be one of the type supported by the derived
    * class implementing the <code>getType()</code> method. See
    * {@link PSDependencyHandler#getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSElementDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   /**
    * Get the child handler used to delegate all abstract base class method
    * overriden by this handler.
    * 
    * @return The child handler object. It will never be <code>null</code>.
    */
   protected abstract PSDependencyHandler getChildHandler();
   
   // see base class
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok,
      PSDependency dep) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
      
      String defId = dep.getDependencyId();
      List childDeps = new ArrayList();

      PSDependency defDep = getChildHandler().getDependency(tok, defId);
      if ( defDep != null )
      {
         defDep.setDependencyType(PSDependency.TYPE_LOCAL);
         childDeps.add(defDep);
      }

      return childDeps.iterator();
   }

   // see base class
   @SuppressWarnings("unchecked")
   public Iterator getDependencies(PSSecurityToken tok) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // get all registered element definition
      List deps = new ArrayList();

      Iterator defDeps = getChildHandler().getDependencies(tok);
      while (defDeps.hasNext())
      {
         PSDependency defDep = (PSDependency)defDeps.next();
         if (defDep.getDependencyType() == PSDependency.TYPE_SYSTEM)
            continue;
         
         PSDependency dep = new PSDeployableElement(PSDependency.TYPE_SHARED,
            defDep.getDependencyId(), getType(),
            m_def.getObjectTypeName(), defDep.getDisplayName(),
            m_def.supportsIdTypes(), m_def.supportsIdMapping(),
            m_def.supportsUserDependencies(), m_def.supportsParentId());

         deps.add(dep);
      }

      return deps.iterator();
   }


   // see base class
   @SuppressWarnings("unchecked")
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency defDep = getChildHandler().getDependency(tok, id);
      PSDependency dep = null;
      if (defDep != null && 
            defDep.getDependencyType() != PSDependency.TYPE_SYSTEM)
      {
         dep = createDeployableElement(m_def, defDep.getDependencyId(), 
            defDep.getDisplayName());
      }

      return dep;
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getChildHandler().getDependency(tok, id) != null;
   }

   // see base class
   public boolean delegatesIdMapping()
   {
      return getChildHandler().m_def.supportsIdMapping();
   }
   
   // see base class
   public String getIdMappingType()
   {
      return getChildHandler().getType();
   }
}
