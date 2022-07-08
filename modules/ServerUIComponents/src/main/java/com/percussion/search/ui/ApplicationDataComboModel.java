/******************************************************************************
 *
 * [ ApplicationDataComboModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.search.ui;

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
