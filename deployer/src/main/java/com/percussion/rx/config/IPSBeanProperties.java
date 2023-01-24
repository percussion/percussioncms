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
package com.percussion.rx.config;

import java.util.List;
import java.util.Map;

/**
 * This interface allows accessing the Spring Bean properties defined in
 * default and/or local configure files. 
 *
 * @author YuBingChen
 */
public interface IPSBeanProperties
{
   /**
    * Gets the properties.
    * 
    * @return the properties, never <code>null</code>, may be empty.
    */
   Map<String, Object> getProperties();
   
   /**
    * Gets a specified property.
    * 
    * @param name the name of the property to retrieve. 
    * 
    * @return the value of the property. It may be <code>null</code> if cannot
    * find the property.
    */
   Object getProperty(String name);

   /**
    * Gets a string associate with a specified property.
    * 
    * @param name the name of the property to retrieve.
    * 
    * @return the associated string. It may be <code>null</code> if cannot
    * find the property.
    */
   String getString(String name);

   /**
    * Gets a list associate with a specified property.
    * 
    * @param name the name of the property to retrieve.
    * 
    * @return the associated list. It may be <code>null</code> if cannot find
    * the property.
    */
   @SuppressWarnings("unchecked")
   List getList(String name);
   
   /**
    * Gets a map associate with a specified property.
    * 
    * @param name the name of the property to retrieve.
    * 
    * @return the associated map. It may be <code>null</code> if cannot find
    * the property.
    */
   @SuppressWarnings("unchecked")
   Map getMap(String name);

   /**
    * Saves a set of properties. The specified properties will be override
    * and merged into current properties and saved into the repository.
    * 
    * @param props the saved properties. Never <code>null</code>.
    */
   void save(Map<String, Object> props);
}
