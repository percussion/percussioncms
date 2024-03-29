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

import com.percussion.conn.PSServerException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequestHandlerConfiguration;
import com.percussion.server.PSRequestHandlerDef;
import com.percussion.utils.collections.PSIteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Class to handle loadable handler dependencies
 */
public class PSLoadableHandlerDependencyHandler extends PSDependencyHandler
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
   public PSLoadableHandlerDependencyHandler(PSDependencyDef def,
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
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // there are no children
      return PSIteratorUtils.emptyIterator();
    }

   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<>();
      
      Iterator defs = getReqHandlerCfg().getHandlerDefs();
      while (defs.hasNext())
      {
         PSRequestHandlerDef def = (PSRequestHandlerDef)defs.next();
         String handlerName = def.getHandlerName();
         PSDependency dep = createDependency(m_def, handlerName, handlerName);
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
      Iterator deps = getDependencies(tok);
      while (deps.hasNext() && dep == null)
      {
         PSDependency test = (PSDependency)deps.next();
         if (id.equals(test.getDependencyId()))
            dep = test;
      }
         
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * There are no child types supported by this handler.
    *
    * @return An empty iterator, never <code>null</code>.
    */
   public Iterator getChildTypes()
   {
      return PSIteratorUtils.emptyIterator();
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
      
      return (getDependency(tok, id) != null);
   }
   
   /**
    * Gets the request handler config, loading from config file if not already 
    * loaded.
    * 
    * @return The config, never <code>null</code>.
    * 
    * @throws PSDeployException if the config cannot be loaded.
    */
   private PSRequestHandlerConfiguration getReqHandlerCfg() 
      throws PSDeployException
   {
      try 
      {
         if (m_reqHandlerCfg == null)
            m_reqHandlerCfg = new PSRequestHandlerConfiguration();
            
         return m_reqHandlerCfg;
      }
      catch (PSServerException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "LoadableHandler";
   
   /**
    * Loadable handler configuration, initialized by first call to 
    * {@link #getReqHandlerCfg()}, never <code>null</code> or modified 
    * after that.
    */
   private PSRequestHandlerConfiguration m_reqHandlerCfg;
}
