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
package com.percussion.rx.jsf;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.utils.jsf.validators.IPSUniqueValidatorValueProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Provides the list of the current category names to the unique name validator.
 * Excludes the currently selected node name from the list.
 * Should have the navigation property set.
 *
 * @author Andriy Palamarchuk
 */
public class PSCategoryNodesNameProvider
      implements IPSUniqueValidatorValueProvider
{
   // see base
   public Collection<Object> getAllValues()
   {
      notNull(getNavigation(),
            "Navigation is not set on PSCategoryNodesNameProvider");
      final PSCategoryNodeBase collectionNode
            = getNavigation().getCollectionNode();
      if (collectionNode == null)
      {
         return Collections.emptySet();
      }
      
      final Set<Object> names = collectionNode.getAllNames();
      
      final PSNodeBase currentNode = getNavigation().getCurrentNode();
      if (currentNode != null)
      {
         names.remove(currentNode.getTitle());
      }
      
      return names;
   }

   /**
    * The navigation used to extract the current category names.
    * @return the navigation. Never <code>null</code> after initialized.
    */
   public PSNavigation getNavigation()
   {
      return m_navigation;
   }

   /**
    * @param navigation the new navigation object. Not <code>null</code>.
    * @see #getNavigation()
    */
   public void setNavigation(PSNavigation navigation)
   {
      notNull(navigation);
      this.m_navigation = navigation;
   }

   /**
    * @see #getNavigation()
    */
   private PSNavigation m_navigation;
}
