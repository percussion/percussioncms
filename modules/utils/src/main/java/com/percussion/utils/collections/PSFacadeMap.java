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
import java.util.Map;

/**
 * A facade map hides an underlying immutable map so the data values can be
 * modified or removed. The underlying map is not changed by these operations.
 * <p>
 * It's important to note that the resulting composite map may have values that
 * are not present in the underlying map.
 * 
 * @author dougrand
 * 
 * @param <K> the key class for the map
 * @param <V> the value class for the map
 */
public class PSFacadeMap<K, V> extends HashMap<K, V>
{
  
   /**
    * Construct a new facade map
    * 
    * @param mapToEncapsulate the map to encapsulate, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public PSFacadeMap(Map<K, ? extends V> mapToEncapsulate) {
      super(mapToEncapsulate);
   }
 
}
