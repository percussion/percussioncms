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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.deploy.server.dependencies;

import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.util.PSIteratorUtils;

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
