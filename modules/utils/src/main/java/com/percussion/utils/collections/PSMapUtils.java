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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PSMapUtils {

    public static boolean areEqualWithArrayValue(Map<String, String[]> first, Map<String, String[]> second) {
        if (first.size() != second.size()) {
            return false;
        }

        return first.entrySet().stream()
                .allMatch(e -> Arrays.equals(e.getValue(), second.get(e.getKey())));
    }

    public static boolean areEqualWithArrayListValue(Map<String, List> first, Map<String, List> second) {
        if (first.size() != second.size()) {
            return false;
        }

        Map.Entry<String, List> matchedEntry=null;
        for(Map.Entry<String, List> e : first.entrySet()){
            if(!second.containsKey(e.getKey())){
                return false;
            }
            if(!e.getValue().containsAll(second.get(e.getKey()))){
                return false;
            }
        }
        return true;
    }

}
