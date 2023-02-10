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

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a slot deployable element.
 * @author vamsinukala
 *
 */
public class PSSlotDependencyHandler extends PSElementDependencyHandler
{

   /**
    * Construct the Slot dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSSlotDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   // see base class
   protected PSDependencyHandler getChildHandler()
   {
      if (m_childHandler == null)
         m_childHandler = getDependencyHandler(
               PSSlotDefDependencyHandler.DEPENDENCY_TYPE);

      return m_childHandler;
   }

   // see base class
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;   
   }
   
   
   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = IPSDeployConstants.DEP_OBJECT_TYPE_SLOT;

   
   /**
    * The content list definition handler, initialized by
    * <code>getChildHandler()</code> if it is <code>null</code>, will never
    * be <code>null</code> after that.
    */
   private PSDependencyHandler m_childHandler = null;

   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();
   static
   {
      ms_childTypes.add(PSSlotDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
