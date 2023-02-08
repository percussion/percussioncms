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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Class to handle packaging and deploying a community deployable element.
 */
public class PSCommunityDependencyHandler extends PSElementDependencyHandler
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
   public PSCommunityDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>CommunityDef</li>
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
   protected PSDependencyHandler getChildHandler()
   {
      if (m_cdHandler == null)
         m_cdHandler = getDependencyHandler(
            PSCommunityDefDependencyHandler.DEPENDENCY_TYPE);

      return m_cdHandler;
   }

   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = "Community";

   /**
    * The community definition handler, initialized by
    * <code>getChildHandler()</code> if it is <code>null</code>, will never
    * be <code>null</code> after that.
    */
   private PSDependencyHandler m_cdHandler = null;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSCommunityDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
