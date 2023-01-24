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
