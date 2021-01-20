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
package com.percussion.services.workflow.data;

import org.apache.commons.lang.StringUtils;

/**
 * Class to enumerate the assignment types.
 */
public enum PSAssignmentTypeEnum
{
   /**
    * Represents assignement type of none.
    */
   NONE(1),
   
   /**
    * Represents assignement type of reader.
    */
   READER(2),
   
   /**
    * Represents assignement type of assignee.
    */
   ASSIGNEE(3),
   
   /**
    * Represents an admin
    */
   ADMIN(4);
   
   /**
    * Get the enum with the matching value.
    * 
    * @param value The value.
    * 
    * @return The enum, never <code>null</code>.
    */
   public static PSAssignmentTypeEnum valueOf(int value)
   {
      for (PSAssignmentTypeEnum type : values())
      {
         if (type.mi_value == value)
         {
            return type;
         }
      }
      
      throw new IllegalArgumentException("invalid value");
   }
   
   /**
    * Get the value of this enum.
    * 
    * @return The value.
    */
   public int getValue()
   {
      return mi_value;
   }

   /**
    * Get the name of the enumeration as a proper cased word
    * @return the name proper cased, never <code>null</code> or empty
    */
   public String getLabel()
   {
      return StringUtils.capitalize(name().toLowerCase());
   }
   
   /**
    * Private ctor
    * @param value db value for the enum value
    */
   private PSAssignmentTypeEnum(int value)
   {
      mi_value = value;
   }
   
   /**
    * Db value for the enum value
    */
   private int mi_value;
}
