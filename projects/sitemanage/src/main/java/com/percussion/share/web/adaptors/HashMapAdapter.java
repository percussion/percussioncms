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
