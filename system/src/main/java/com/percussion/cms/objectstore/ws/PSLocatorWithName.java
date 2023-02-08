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
