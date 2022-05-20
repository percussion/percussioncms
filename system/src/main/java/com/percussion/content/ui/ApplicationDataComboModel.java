/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Model that may be used for a combo box with
 * the data representing all existing display formats.
 */
public class ApplicationDataComboModel extends DefaultComboBoxModel
{
   /**
    * Static call to populate the model with data and
    * return the model
    * 
    * @param map Map of id and names. If <code>null</code>
    * an empty map will be created.
    * 
    * @return ApplicationDataComboModel object.
    */
   public static ApplicationDataComboModel createApplicationDataComboModel(Map map)
   {
      if(map==null)
         map = new HashMap();
      try
      {
         return new ApplicationDataComboModel(map);
      }
      catch (Exception e)
      {
         Map m = new HashMap();
         m.put(e.getLocalizedMessage(), e.getClass().getName());
         return new ApplicationDataComboModel(m);
      }
   }
   /**
    * Creates an ApplicationDataComboModel object.
    * 
    * @param map of ids and display names. Assumed not <code>null</code>.
    */
   private ApplicationDataComboModel(Map map)
   {
      super(sort(map.values().toArray()));
      m_map = map;
   }
   /**
    * Access method to get the selected id from the combo box.
    * @return String the id of the selected item. Returns <code>null
    *    </code> if the nothing is selected.
    */
   public String getSelectedId()
   {
      String strVal = (String) super.getSelectedItem();

      Iterator keys = m_map.keySet().iterator();
      while (keys.hasNext())
      {
         String strId = (String) keys.next();

         if (((String) m_map.get(strId)).equalsIgnoreCase(strVal))
            return strId;
      }

      return null;
   }
   /**
    * Sets the display name of the supplied id as selected item in the 
    * combo box.
    * 
    * @param strId the id of the item that need to be set. If 
    * <code>null</code> or empty then nothing is set.
    */
   public void setSelectedId(String strId)
   {
      if(strId == null || strId.trim().length() < 1)
         return;
      String strVal = (String) m_map.get(strId);

      if (strVal == null)
      {
         Iterator iter = m_map.values().iterator();

         if (iter.hasNext())
            strVal = (String) iter.next();
      }

      super.setSelectedItem(strVal);
   }

   /**
    * Re-orders the supplied list into dictionary order.
    *
    * @param values May be <code>null</code>. Assumes entries are Strings.
    *
    * @return The supplied object, or <code>null</code> if <code>null</code>
    *    supplied.
    */
   private static Object[] sort(Object[] values)
   {
      if (values == null)
         return null;

      Arrays.sort(values, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            return ((String) o1).compareToIgnoreCase((String) o2);
         }

         @Override
         public boolean equals(Object o)
         {
            throw new UnsupportedOperationException("Not implemented");
         }

         @Override
         public int hashCode()
         {
            throw new UnsupportedOperationException("Not implemented");
         }
         
         
      });

      return values;
   }

   /**
    * Initialized in ctor. Never <code>null</code>, may be empty.
    */
   private Map m_map = null;
}
