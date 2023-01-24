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
package com.percussion.utils.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Deep copier
 * 
 * @author dougrand
 */
public class PSCopier
{
   /**
    * Deep copy the passed map. Most map elements will be copied by value
    * but any map values will be deep copied themselves.
    * 
    * @param input the input map, never <code>null</code>
    * @return a deep copied map, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public static Map deepCopy(Map input)
   {
      Map rval = new HashMap();
      Iterator<Map.Entry> eiter = input.entrySet().iterator();
      while(eiter.hasNext())
      {
         Map.Entry entry = eiter.next();
         if (entry.getValue() instanceof Map)
         {
            rval.put(entry.getKey(), deepCopy((Map) entry.getValue()));
         }
         else
         {
            rval.put(entry.getKey(), entry.getValue());
         }
      }
      return rval;
   }
}
