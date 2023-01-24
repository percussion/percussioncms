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
