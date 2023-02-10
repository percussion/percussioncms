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
package com.percussion.services.general.impl;

import com.percussion.server.PSServer;
import com.percussion.services.general.IPSRhythmyxInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementaion of IPSRhythmyxInfo interface. This class provides the methods
 * to get Rhythmyx information. Properties are initialized lazily here. This
 * object is created as a singleton by the spring framework.
 */
public class PSRhythmyxInfo implements IPSRhythmyxInfo
{
   /**
    * When created this is called, never call outside of the spring
    * configuration
    * 
    * @param initialData initial values for the map, each value is presented as
    *           a string that is used to lookup the key, and a value to store
    *           for the key. What values can be initialized this way must be
    *           known by info since the keys are a definitive list, and may be
    *           converted on storage.
    */
   public void setBindings(Map<String, String> initialData) {
      String ut = initialData.get(Key.UNIT_TESTING.toString());
      String rootdir = initialData.get(Key.ROOT_DIRECTORY.toString());
      if (ut != null)
      {
         m_propMap.put(Key.UNIT_TESTING, Boolean.parseBoolean(ut));
      }
      if (rootdir != null)
      {
         m_propMap.put(Key.ROOT_DIRECTORY, rootdir);
      }
   }

   /**
    * Gets the value of the given property name key.
    * 
    * @param key Key name of the property must not be <code>null</code>.
    * @return Object value of the property or <code>null</code> if the key
    *         does not exist.
    */
   public synchronized Object getProperty(Key key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("property_name must not be null");
      }
      Object rval = m_propMap.get(key);
      if (rval == null)
      {
         rval = init(key);
      }

      return rval;
   }

   /**
    * Initialize information depending on the key
    * 
    * @param key the key, assumed never <code>null</code> and assumed to be
    *           handled
    * @return the value for the key
    */
   private Object init(Key key)
   {
      Object val = null;
      if (key == Key.ROOT_DIRECTORY)
      {
         val = PSServer.getRxDir().getAbsolutePath();
      }
      else if (key == Key.LISTENER_PORT)
      {
         val = PSServer.getListenerPort();
      }
      else if (key == Key.LISTENER_SSL_PORT)
      {
         val = PSServer.getSslListenerPort();
      }
      else if (key == Key.VERSION)
      {
         val = PSServer.getVersion();
      }
      m_propMap.put(key, val);
      return val;
   }

   /**
    * Map of the Rhythmyx information properties. Access must be synchronized.
    */
   private Map<Key, Object> m_propMap = new HashMap<>();
}
