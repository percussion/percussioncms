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
