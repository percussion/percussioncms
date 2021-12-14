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

package com.percussion.design.objectstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Container for a set of {@link PSView} objects.
 */
public class PSViewSet
{
   /**
    * Adds a view to this view set.  Only one PSView may be added for each view
    * name, but multiple conditional views may be added with the same name.  See
    * {@link #addConditionalView(PSConditionalView)} for more info.
    *
    * @param view The view to add, may not be <code>null</code>, and a view
    * with the same name may not already exist in this set.  Comparison is case
    * insensitive.
    *
    * @throws IllegalArgumentException if view is <code>null</code> or if a
    * view with the same name exists in this set.
    */
   public void addView(PSView view)
   {
      if (view == null)
         throw new IllegalArgumentException("view may not be null");

      String name = view.getName().toLowerCase();

      if (m_views.containsKey(name))
         throw new IllegalArgumentException("cannot add duplicate view: " +
            view.getName());

      m_views.put(name, view);
   }

   /**
    * Adds the conditional view to this view set.  Conditional views are
    * evaluated in the order in which they are added, and if one is not
    * selected, then the non-conditional <code>PSView</code> is used. See
    * {@link {#addView(PSView)} for information on adding non-conditional views.
    *
    * @param view The view to add, may not be <code>null</code>. 
    *
    * @throws IllegalArgumentException if view is <code>null</code>.
    */
   public void addConditionalView(PSConditionalView view)
   {
      if (view == null)
         throw new IllegalArgumentException("view may not be null");

      String name = view.getName().toLowerCase();

      List views = (List)m_conditionalViews.get(name);
      if (views == null)
      {
         views = new ArrayList();
         m_conditionalViews.put(name, views);
      }
      views.add(view);
   }

   /**
    * Get a list of the view names that have one or more conditional views.
    * Names have been lowercased for case insensitive comparisons.
    * 
    * @return An Iterator over <code>0</code> or more names as 
    * <code>String</code> objects, never <code>null</code>.
    */
   public Iterator getConditionalViewNames()
   {
      return m_conditionalViews.keySet().iterator();
   }

   /**
    * Returns the default PSView object with the specified name. Comparison is
    * case insensitive.
    *
    * @param name The name of the view, may not be <code>null</code> or empty.
    *
    * @return The view, or <code>null</code> if view is not found.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public PSView getView(String name)
   {
      Object o = m_views.get(name.toLowerCase());

      return o == null ? null : (PSView)o;
   }

   /**
    * Gets the PSView objects contained in this set.
    *
    * @return An iterator over zero or more <code>PSView</code> objects, never
    * <code>null</code>.
    */
   public Iterator getViews()
   {
      return m_views.values().iterator();
   }

   /**
    * Get the condtional views for the supplied view name.  Comparison is case
    * insensitive.
    *
    * @param name The name of the view, may not be <code>null</code> or empty.
    *
    * @return An iterator over zero or more <code>PSConditionalView</code>
    * objects, never <code>null</code>, may be empty.  Views should be evaluated
    * in the order in which they are returned by the iterator.
    */
   public Iterator getCondtionalViews(String name)
   {
      List views = (List)m_conditionalViews.get(name.toLowerCase());
      if (views == null)
         views = new ArrayList();

      return views.iterator();
   }

   /**
    * Map of views by name.  Key is the view name lowercased and stored as a
    * String.  Value is the PSView object, never <code>null</code>.  Map is
    * instantiated at construction and never <code>null</code> after that.
    */
   private Map m_views = new HashMap();

   /**
    * Map of <code>PSConditionalView</code> objects.  The key is the view name
    * as a lowercase String, and the value is a List of
    * <code>PSConditionalView</code> objects, never <code>null</code>.  Map is
    * instantiated at runtime, never <code>null</code> after that, may be
    * empty.
    */
   private Map m_conditionalViews = new HashMap();

}
