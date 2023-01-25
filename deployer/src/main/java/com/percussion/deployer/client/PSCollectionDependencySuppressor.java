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

package com.percussion.deployer.client;

import com.percussion.deployer.objectstore.PSDependency;

import java.util.Collection;

/**
 * A dependency suppressor that will suppress any dependency whose key is
 * included in a specified collection.
 */
public class PSCollectionDependencySuppressor implements IPSDependencySuppressor
{
   /**
    * Constructs a dependency suppressor to suppress members included in the
    * specified collection.
    * 
    * @param dependenciesToSuppress a collection of dependency keys (Strings)
    *           that should be suppressed, not <code>null</code>
    */
   public PSCollectionDependencySuppressor(Collection dependenciesToSuppress)
   {
      if (dependenciesToSuppress == null)
         throw new IllegalArgumentException(
               "dependenciesToSuppress may not be null");
      m_depsToSuppress = dependenciesToSuppress;
   }

   /**
    * Determines if the specified dependency should be suppressed by comparing
    * that dependency's key against the collection of dependency keys provided
    * to the constructor.
    * 
    * @param dependency the dependency to be evaluated for suppression, not
    *           <code>null</code>
    * 
    * @return <code>true</code> if the dependency should be suppressed because
    *         its key is included in the collection, <code>false</code>
    *         otherwise.
    */
   public boolean suppress(PSDependency dependency)
   {
      if (dependency == null)
         throw new IllegalArgumentException("dependency may not be null");

      return m_depsToSuppress.contains(dependency.getKey());
   }

   /**
    * Collection of dependency keys (Strings) that should be suppressed.
    * Assigned in ctor, and never <code>null</code> after.
    */
   private Collection m_depsToSuppress;
}
