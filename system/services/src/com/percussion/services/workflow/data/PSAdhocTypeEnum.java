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

/**
 * Class to enumerate the adhoc types.
 */
public enum PSAdhocTypeEnum
{
   /**
    * Represents the disabled adhoc type
    */
   DISABLED(0),
   
   /**
    * Represents the enabled adhoc type
    */
   ENABLED(1),
   
   /**
    * Represents the anonymous adhoc type
    */
   ANONYMOUS(2);
   
   /**
    * Get the enum with the matching value.
    * 
    * @param value The value.
    * 
    * @return The enum, never <code>null</code>.
    */
   public static PSAdhocTypeEnum valueOf(int value)
   {
      for (PSAdhocTypeEnum type : values())
      {
         if (type.mi_value == value)
         {
            return type;
         }
      }
      
      throw new IllegalArgumentException("Invalid value");
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
    * Private ctor
    * @param value the db value to use
    */
   private PSAdhocTypeEnum(int value)
   {
      mi_value = value;
   }

   /**
    * The value of the enum
    */
   private int mi_value;      
}
