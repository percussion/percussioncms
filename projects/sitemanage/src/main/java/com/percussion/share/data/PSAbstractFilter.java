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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

public abstract class PSAbstractFilter<T> implements Predicate {

    
    public List<T> filter(Collection<T> resources) {
        List<T> rvalue = new ArrayList<>(resources);
        CollectionUtils.filter(rvalue, this);
        return rvalue;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(Object obj)
    {
        return shouldKeep((T) obj);
    }
    
    public abstract boolean shouldKeep(T resource);
}
