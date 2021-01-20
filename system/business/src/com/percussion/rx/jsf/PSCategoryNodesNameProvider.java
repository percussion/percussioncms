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
