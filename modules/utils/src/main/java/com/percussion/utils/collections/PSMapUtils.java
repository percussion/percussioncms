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
