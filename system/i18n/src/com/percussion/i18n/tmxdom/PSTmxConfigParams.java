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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.i18n.tmxdom;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a wrapper on the configuration parameter name-value pairs. Wraps the
 * hash map with additional checking.
 */
public class PSTmxConfigParams
{
   /**
    * Hash map of parameter name-value pairs.
    */
   protected Map m_ConfigParamMap = new HashMap();

   /**
    * Method to add new parameter to the map.
    * @param    name  must not be <code>null</code> or <code>empty</code>.
    * @param    value  may be <code>null</code> or <code>empty</code>.
    */
   public void addParam(String name, String value){
      if(name==null || name.length() < 1)
         throw new IllegalArgumentException("Param name cannot be null or empty");

      m_ConfigParamMap.put(name, value);
   }

   /**
    * Method to get the parameter value given the name.
    * @param    name should not be <code>null</code> or <code>empty</code>
    * @return value of the parameter for the given key, never <code>null</code>,
    * may be <code>empty</code>
    * @throws IllegalArgumentException if the parameter is name supplied is 
    * <code>null</code> or <code>empty</code>
    */
   public String getParam(String name)
   {
      if(name == null || name.trim().length() < 1)
         throw new IllegalArgumentException("paramater name must not be empty");

      Object o = m_ConfigParamMap.get(name);
      return (o==null) ? "" : o.toString();
   }
}
