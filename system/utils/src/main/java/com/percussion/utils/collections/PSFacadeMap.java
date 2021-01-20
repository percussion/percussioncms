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
package com.percussion.utils.collections;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
