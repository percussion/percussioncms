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
