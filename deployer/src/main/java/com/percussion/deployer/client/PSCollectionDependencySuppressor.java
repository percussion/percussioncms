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
