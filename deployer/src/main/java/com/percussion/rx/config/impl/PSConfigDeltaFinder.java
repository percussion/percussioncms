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
package com.percussion.rx.config.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Configuration delta finder class.
 * @author bjoginipally
 *
 */
public class PSConfigDeltaFinder
{
   /**
    * Returns the delta of new properties and old properties. If a property
    * exists in new property and not in old then it is considered as delta.
    * 
    * @param newProps must not be <code>null</code>.
    * @param oldProps may be <code>null</code> or empty, in which case
    * newProps are returned as delta.
    * @return delta of new and old properties. Never <code>null</code>, may
    * be empty.
    */
   public Map<String, Object> getConfigDelta(Map<String, Object> newProps,
         Map<String, Object> oldProps)
   {
      if (newProps == null)
         throw new IllegalArgumentException("newProps must not be null");
      if (oldProps == null || oldProps.isEmpty())
      {
         return newProps;
      }
      // Propcess delta and return changed props
      Map<String, Object> delta = new HashMap<>();
      Iterator<String> iter = newProps.keySet().iterator();
      while(iter.hasNext())
      {
         String key = iter.next();
         if(!oldProps.containsKey(key))
         {
            delta.put(key, newProps.get(key));
         }
         else
         {
            Object objN = newProps.get(key);
            Object objO = oldProps.get(key);
            if ((objN == null && objO != null)
                  || (objN != null && (!objN.equals(objO))))
            {
               delta.put(key, objN);
            }
         }
         
      }
      return delta;
   }
}
