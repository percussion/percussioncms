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

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.error.PSDeployException;

/**
 * Class to handle packaging and deploying a search definition.
 */
public class PSSearchDefDependencyHandler 
   extends PSSearchObjectDependencyHandler
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
   public PSSearchDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }
   
   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   protected boolean isDependentType(PSSearch search)
   {
      return search.isStandardSearch() || search.isCustomSearch();
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "SearchDef";
}
