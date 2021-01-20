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
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.extension.PSDatabaseFunction;
import com.percussion.extension.PSDatabaseFunctionManager;
import com.percussion.security.PSSecurityToken;
import com.percussion.util.PSIteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Class to handle discovering database function definitions as dependencies.
 * Database functions are not deployable and user defined dependencies are
 * returned as "Server" type dependencies.
 */
public class PSDbFunctionDefDependencyHandler extends PSDependencyHandler
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
   public PSDbFunctionDefDependencyHandler(PSDependencyDef def, 
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
         
      // no child types
      return PSIteratorUtils.emptyIterator();
   }
   
   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      List deps = new ArrayList();
      PSDatabaseFunctionManager mgr = PSDatabaseFunctionManager.getInstance();
      Iterator funcs = mgr.getDatabaseFunctions(ALL_FUNC_TYPES);
      while (funcs.hasNext())
         deps.add(createDependency((PSDatabaseFunction)funcs.next()));
      
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
      
      PSDatabaseFunctionManager mgr = PSDatabaseFunctionManager.getInstance();
      PSDatabaseFunction func = mgr.getDatabaseFunction(ALL_FUNC_TYPES, id);
      if (func != null)
         dep = createDependency(func);
            
      return dep;      
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * No child types supported by this handler.
    * 
    * @return An empty iterator, never <code>null</code>.
    */
   public Iterator getChildTypes()
   {
      return PSIteratorUtils.emptyIterator();
   }

   /**
    * Get the type of depedency supported by this handler.
    * 
    * @return the type, never <code>null</code> or empty.
    */
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
      
      return getDependency(tok, id) != null;
   }
   
   /**
    * Create a dependency from the provided database function.
    * 
    * @param func The function, assumed not <code>null</code>.
    * 
    * @return The dependency, never <code>null</code>.
    */
   private PSDependency createDependency(PSDatabaseFunction func)
   {
      PSDependency dep = createDependency(m_def, func.getName(), 
         func.getName());
      if (func.getType() == PSDatabaseFunctionManager.FUNCTION_TYPE_SYSTEM)
         dep.setDependencyType(PSDependency.TYPE_SYSTEM);
      else
         dep.setDependencyType(PSDependency.TYPE_SERVER);
      
      return dep;
   }
 
   /**
    * Constant for this handler's supported type
    */
   static final String DEPENDENCY_TYPE = "DbFunctionDef";
   
   /**
    * Constant for flag indicating both server and user function types.
    */
   private static final int ALL_FUNC_TYPES = 
      PSDatabaseFunctionManager.FUNCTION_TYPE_SYSTEM | 
      PSDatabaseFunctionManager.FUNCTION_TYPE_USER;
}
