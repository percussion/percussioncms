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
package com.percussion.delivery.utils.properties;


import java.util.Date;
import java.util.List;

/***
 * Defines the data types that a property can represent.
 * 
 * @author natechadwick
 * @author adamgent
 */
public enum PSPropertyDataType {
   
   STRING("string", String.class),
   ENUM("enum", String.class),
   NUMBER("number", Number.class),
   BOOL("bool", Boolean.class),
   HIDDEN("hidden", Object.class),
   DATE("date", Date.class),
   LIST("list", List.class),
   PASSWORD("password", String.class);
   
   private String name;
   private Class<?> javaType;

   private PSPropertyDataType(String name, Class<?> javaType)
   {
       this.name = name;
       this.javaType = javaType;
   }

   /**
    * Gets the nominal value of the data type.
    * @return never <code>null</code> or empty.
    */
   public String getName()
   {
       return name;
   }

   /**
    * The java type that the widget property should be.
    * @return never <code>null</code>.
    */
   public Class<?> getJavaType()
   {
       return javaType;
   }
   
   /**
    * Gets the data type from widget property definition.
    * @param userPref never <code>null</code>.
    * @return never <code>null</code>.
    */
   public static PSPropertyDataType fromDefinition(PSPropertyDefinition prop) {
       return parseType(prop.getDatatype());
   }
   
   /**
    * Parse the {@link #getName()} property definition type.
    * @param name
    * @return never <code>null</code>.
    */
   public static PSPropertyDataType parseType(String name) {
       String n = name.toUpperCase();
       return valueOf(n);
   }
   
}

