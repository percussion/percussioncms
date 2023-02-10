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
package com.percussion.fastforward.managednav;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author DavidBenua
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PSNavonType
{
   /**
    * Ctor that takes the navon type string.
    * 
    * @param typeName must be one of the TYPENAME_XXXs. If it happens to be
    *           outside of these, the default type {@link #TYPENAME_OTHER}is
    *           assumed.
    */
   public PSNavonType(String typeName)
   {
      if (typeName == null || typeName.length() < 1)
      {
         throw new IllegalArgumentException("typeName must not be null or empty");
      }
      setType(typeName);
   }

   /**
    * Ctor that takes the navon type constant.
    * @param typeValue must be one of the TYPE_XXXs. If it happens to be
    *           outside of these, the default type {@link #TYPE_OTHER}is
    *           assumed.
    */
   public PSNavonType(int typeValue)
   {
      setType(typeValue);
   }

   /**
    * Default ctor.
    * 
    */
   public PSNavonType()
   {
   }

   /**
    * @return type of the navon, one of the TYPE_XXX values.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Set Navon type.
    * @param typeName must be one of the TYPENAME_XXXs. If it happens to be
    *           outside of these, the default type {@link #TYPENAME_OTHER}is
    *           assumed.
    */
   public void setType(String typeName)
   {
      String compareName;
      for (int i = 0; i < typeArray.length; i++)
      {
         compareName = typeArray[i];
         if (compareName.equalsIgnoreCase(typeName))
         {
            m_type = i;
            return;
         }
      }
      m_type = TYPE_OTHER;
      return;
   }

   /**
    * Set Navon type.
    * @param typeValue must be one of the TYPE_XXXs. If it happens to be
    *           outside of these, the default type {@link #TYPE_OTHER}is
    *           assumed.
    */
   public void setType(int typeValue)
   {
      m_type = typeValue;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return typeArray[m_type];
   }

   /**
    * creates the type object for the next level in the tree.
    * 
    * @param parentType the parent node's type class
    * @param level the relative level
    * @return a new type appropriate for the mext level in the tree.
    */
   public static PSNavonType getDescendentType(PSNavonType parentType, int level)
   {
      int newType = TYPE_ROOT;
      log.debug("Setting descendent type - parent is {}", parentType.toString());
      switch (parentType.getType())
      {

         case TYPE_ROOT :
         case TYPE_ANCESTOR :
            newType = (level == -1) ? TYPE_SIBLING : TYPE_ANCESTOR_SIBLING;
            break;

         case TYPE_SELF :
         case TYPE_DESCENDENT :
            newType = TYPE_DESCENDENT;
            break;

         case TYPE_SIBLING :
         case TYPE_ANCESTOR_SIBLING :
         case TYPE_OTHER :
            newType = TYPE_OTHER;
            break;

      }
      PSNavonType result = new PSNavonType(newType);
      log.debug("new child type is {}", result.toString());

      return result;
   }

   /**
    * Navon type relative to the current Navon item. Valid values are one of the
    * TYPE_XXXs defined in this class. Initialized in the ctors and settable via
    * {@link #setType(int)}or {@link #setType(String)}. Default is
    * {@link #TYPE_OTHER}.
    */
   private int m_type = TYPE_OTHER;

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger(PSNavonType.class);

   /**
    * Navon type string to indicate the root of the navigation tree.
    */
   public static final String TYPENAME_ROOT = "root";

   /**
    * Navon type to indicate the ancestor to the current Navon item.
    */
   public static final String TYPENAME_ANCESTOR = "ancestor";

   /**
    * Navon type string to indicate the ancestor sibling to the current Navon
    * item.
    */
   public static final String TYPENAME_ANCESTOR_SIBLING = "ancestor-sibling";

   /**
    * Navon type string to indicate a sibling to the current Navon item.
    */
   public static final String TYPENAME_SIBLING = "sibling";

   /**
    * Navon type string to indicate as self to the current Navon item.
    */
   public static final String TYPENAME_SELF = "self";

   /**
    * Navon type string to indicate the descendent to the current Navon item.
    */
   public static final String TYPENAME_DESCENDENT = "descendent";

   /**
    * Navon type string to indicate as unrelated to the current Navon item.
    */
   public static final String TYPENAME_OTHER = "other";

   /**
    * Navon type to indicate the root of the navigation tree.
    */
   public static final int TYPE_ROOT = 0;

   /**
    * Navon type to indicate the ancestor to the current Navon item.
    */
   public static final int TYPE_ANCESTOR = 1;

   /**
    * Navon type to indicate the ancestor sibling to the current Navon item.
    */
   public static final int TYPE_ANCESTOR_SIBLING = 2;

   /**
    * Navon type to indicate a sibling to the current Navon item.
    */
   public static final int TYPE_SIBLING = 3;

   /**
    * Navon type to indicate as self to the current Navon item.
    */
   public static final int TYPE_SELF = 4;

   /**
    * Navon type to indicate the descendent to the current Navon item.
    */
   public static final int TYPE_DESCENDENT = 5;

   /**
    * Navon type to indicate as unrelated to the current Navon item.
    */
   public static final int TYPE_OTHER = 6;

   /**
    * String array of all navon types relative to the current one.
    */
   private static final String[] typeArray =
   {TYPENAME_ROOT, TYPENAME_ANCESTOR, TYPENAME_ANCESTOR_SIBLING,
         TYPENAME_SIBLING, TYPENAME_SELF, TYPENAME_DESCENDENT, TYPENAME_OTHER};

}
