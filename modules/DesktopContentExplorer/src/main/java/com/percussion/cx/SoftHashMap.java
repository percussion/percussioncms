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

package com.percussion.cx;
//package org.h2.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map which stores items using SoftReference. Items can be garbage collected
 * and removed. It is not a general purpose cache, as it doesn't implement some
 * methods, and others not according to the map definition, to improve speed.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SoftHashMap<K, V> extends AbstractMap<K, V> {

    private Map<K, SoftValue<V>> map;
    private ReferenceQueue<V> queue = new ReferenceQueue<V>();

    public SoftHashMap() {
        map = new HashMap<K, SoftValue<V>>();
    }

    @SuppressWarnings("unchecked")
    private void processQueue() {
        while (true) {
            Reference<? extends V> o = queue.poll();
            if (o == null) {
                return;
            }
            SoftValue<V> k = (SoftValue<V>) o;
            Object key = k.key;
            map.remove(key);
        }
    }

    public V get(Object key) {
        processQueue();
        SoftReference<V> o = map.get(key);
        if (o == null) {
            return null;
        }
        return o.get();
    }

    /**
     * Store the object. The return value of this method is null or a SoftReference.
     *
     * @param key the key
     * @param value the value
     * @return null or the old object.
     */
    public V put(K key, V value) {
        processQueue();
        SoftValue<V> old = map.put(key, new SoftValue<V>(value, queue, key));
        return old == null ? null : old.get();
    }

    /**
     * Remove an object.
     *
     * @param key the key
     * @return null or the old object
     */
    public V remove(Object key) {
        processQueue();
        SoftReference<V> ref = map.remove(key);
        return ref == null ? null : ref.get();
    }

    public void clear() {
        processQueue();
        map.clear();
    }

    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * A soft reference that has a hard reference to the key.
     */
    private static class SoftValue<T> extends SoftReference<T> {
        final Object key;

        public SoftValue(T ref, ReferenceQueue<T> q, Object key) {
            super(ref, q);
            this.key = key;
        }

    }

}
