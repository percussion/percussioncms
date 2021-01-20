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
