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

package com.percussion.share.web.adaptors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapAdapter extends XmlAdapter<HashMapAdapter.MapType, Map<String, String>> {
    @Override
    public MapType marshal(Map<String, String> map) {
        MapType mapType = new MapType();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            MapEntry mapEntry = new MapEntry();
            mapEntry.key = entry.getKey();
            mapEntry.value = entry.getValue();
            mapType.entryList.add(mapEntry);
        }
        return mapType;
    }

    @Override
    public Map<String, String> unmarshal(MapType type) throws Exception {
        Map<String, String> map = new HashMap<>();
        for (MapEntry entry : type.entryList) {
            map.put(entry.key, entry.value);
        }
        return map;
    }


    public static class MapType {
        @XmlElement(name ="entry")
        public List<MapEntry> entryList = new ArrayList<>();
    }

    public static class MapEntry {
        @XmlElement
        public String key;
        @XmlElement
        public String value;
    }
}
