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
package com.percussion.services.publisher.data;

import org.apache.commons.lang.StringUtils;

/**
 * Represents the type of an edition. 
 * 
 * @author dougrand
 */
public enum PSEditionType {
   /**
    * Manual editions are not much used at this point. They are primarily used
    * for just in time publishing 
    * @deprecated
    */
   MANUAL(1, "deprecated - manual"),

   /**
    * Publish content from the content lists. Unpublishing is done manually 
    * by a content list, which does not cover all cases.
    */
   AUTOMATIC(2, "Publish"),
   /**
    * @deprecated
    */
   RECOVERY(3, "deprecated - recovery"),
   /**
    * @deprecated
    */
   MIRROR(4, "deprecated - mirror"),
   
   /**
    * Un-publish content that has been purged or archived and then publish
    * the content of the content lists.
    */
   NORMAL(5, "Unpublish Then Publish");
   
   /**
    * The type id
    */
   private int m_typeid;
   
   /**
    * The string to show in the UI.
    */
   private String m_displayTitle;

   /**
    * Ctor
    * @param typeid
    * @param display
    */
   PSEditionType(int typeid, String display) {
      if (StringUtils.isBlank(display))
      {
         throw new IllegalArgumentException(
               "display may not be null or empty");
      }
      m_typeid = typeid;
      m_displayTitle = display;
   }

   /**
    * Get the type. The type is stored as a string, but the values are all
    * integer values.
    * 
    * @return the value of the type
    */
   public int getTypeId()
   {
      return m_typeid;
   }
   
   /**
    * @return get the display title for the ui, never <code>null</code> or empty.
    */
   public String getDisplayTitle()
   {
      return m_displayTitle;
   }

   /**
    * Lookup value by type
    * 
    * @param type the type
    * @return the matching enum value, or <code>null</code> as a default
    */
   public static PSEditionType valueOf(int type)
   {
      for (PSEditionType t : values())
      {
         if (t.getTypeId() == type)
         {
            return t;
         }
      }
      return null;
   }

}
