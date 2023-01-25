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
