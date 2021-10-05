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
package com.percussion.cms.objectstore.ws;

import com.percussion.design.objectstore.PSLocator;

/**
 * This class contains {@link PSLocator} and an override name, which is used
 * to override sys_title when clone the item.
 */
public class PSLocatorWithName extends PSLocator
{
   /**
    * Constructs an object from the given parameters.
    *
    * @param id the content id of an item.
    * @param revision the revision number of the item.
    * @param name the value that will be used to override sys_title field when
    *    clone the item. It may not be <code>null</code> or empty.
    */
   public PSLocatorWithName(int id, int revision, String name)
   {
      super(id, revision);

      if (name == null || name.trim().length() == 0 )
         throw new IllegalArgumentException("name may not be null or empty");
      m_overrideName = name;
   }

   /**
    * Constructs an object from the given parameters.
    *
    * @param id the content id of an item.
    * @param revision the revision number of the item.
    * @param name the value that will be used to override sys_title field when
    *   we clone the item. It may not be <code>null</code> or empty.
    */
   public PSLocatorWithName(String id, String revision, String name)
   {
      super(id, revision);
      if (name == null || name.trim().length() == 0 )
         throw new IllegalArgumentException("name may not be null or empty");
      m_overrideName = name;
   }

   /**
    * Get the override name.
    *
    * @return the name, never <code>null</code> or empty.
    */
   public String getOverrideName()
   {
      return m_overrideName;
   }

   /**
    * The attribute name of an XML element. The value is the override name
    */
   public static String ATTR_OVERRIDE_NAME = "overrideName";

   /**
    * See ctor for its description. Initialized by ctor, never <code>null</code>
    * or empty after that.
    */
   private String m_overrideName;
}
