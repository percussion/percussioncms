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
package com.percussion.share.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PSCollectionUtils
{
    public static interface ToMap<KEY, VALUE, OBJECT> {
        KEY getKey(OBJECT value);
        VALUE getValue(OBJECT object);
    }
    
    public static abstract class Mapper<KEY,VALUE,OBJECT> implements ToMap<KEY,VALUE,OBJECT>{
        public Map<KEY,VALUE> toMap(Iterator<OBJECT> objects) {
            return PSCollectionUtils.toMap(objects, this);
        }
        public Map<KEY,VALUE> toMap(Collection<OBJECT> objects) {
            Iterator<OBJECT> obs;
            if ( objects == null ) {
                obs = null;
            }
            else {
                obs = objects.iterator();
            }
            return PSCollectionUtils.toMap(obs, this);
        }
    }
    
    public static abstract class MapperValueAdapter<KEY,VALUE> extends Mapper<KEY,VALUE,VALUE> {
        @Override
        public Map<KEY,VALUE> toMap(Iterator<VALUE> objects) 
        {
            return PSCollectionUtils.toMap(objects, this);
        }
        
        public VALUE getValue(VALUE object)
        {
            return object;
        }
    }
    
    public static abstract class ToMapKeyAdapter<KEY, VALUE> implements ToMap<KEY, VALUE, VALUE> {

        public VALUE getValue(VALUE object)
        {
            return object;
        }
        
    }
    
    public static <KEY,VALUE, OBJECT> Map<KEY,VALUE> toMap(Iterator<OBJECT> objects, ToMap<KEY,VALUE, OBJECT> toMap) {
        Map<KEY, VALUE> map = new HashMap<>();
        if (objects != null) {
            while(objects.hasNext()) {
                OBJECT o = objects.next();
                KEY key = toMap.getKey(o);
                VALUE value = toMap.getValue(o);
                map.put(key, value);
            }
        }
        return map;
    }

}
