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

import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.utils.collections.PSIteratorUtils;

import java.util.Iterator;

/**
 * Class to handle packaging and deploying a Locale.
 */
public class PSLocaleDependencyHandler extends PSElementDependencyHandler
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
   public PSLocaleDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>LocaleDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return PSIteratorUtils.iterator(CHILD_TYPE);
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   protected PSDependencyHandler getChildHandler()
   {
      if (m_childHandler == null)
         m_childHandler = getDependencyHandler(
            PSLocaleDefDependencyHandler.DEPENDENCY_TYPE);

      return m_childHandler;
   }

   
   /**
    * Instance of child <code>PSLocaleDefDependencyHandler</code> used to 
    * delegate dependency requests.  Initialized by the first call to 
    * {@link #getChildHandler()}, never <code>null</code> or modified after 
    * that.
    */
   private PSDependencyHandler m_childHandler;

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "Locale";

   /**
    * The child handler type supported by this handler.
    */
   private static final String CHILD_TYPE = 
      PSLocaleDefDependencyHandler.DEPENDENCY_TYPE;
}
