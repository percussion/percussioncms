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

package com.percussion.soln.p13n.tracking.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helpful functions for managing segment weight maps.
 * @author adamgent
 */
public class SegmentWeightUtil {
    
    
    public static Map<String,Integer> normalizeSegmentWeights(final Map<String,Integer> segmentWeights, int total) {
        if (segmentWeights == null) throw new IllegalArgumentException("Cannot normalize null segmentWeights");
        if (segmentWeights.isEmpty()) return new HashMap<String,Integer>();
        Map<String,Integer> normalizedSegments = new HashMap<String,Integer>();
        synchronized (segmentWeights) {
            int segTotal = 0;
            for (String segname : segmentWeights.keySet()) { segTotal+=segmentWeights.get(segname);}
            ArrayList<SegmentWeightRatio> sws = new ArrayList<SegmentWeightRatio>();
            int subTotal = 0;
            for (Entry<String,Integer> segment : segmentWeights.entrySet()) {
                SegmentWeightRatio sw = 
                    new SegmentWeightRatio(segment.getKey(), segment.getValue(), segTotal, total);
                sws.add(sw);
                subTotal += sw.getRatio();
            }
            int remainder = total - subTotal;
            Collections.sort(sws);
            for (SegmentWeightRatio w : sws) {
                int value = w.getRatio();
                if (remainder > 0 )  { value += 1; --remainder; }
                normalizedSegments.put(w.id, value);
            }
        }
        return normalizedSegments;
    }
    
    private static class SegmentWeightRatio implements Comparable<SegmentWeightRatio>{
        public String id;
        @SuppressWarnings("unused")
        public int weight;
        private double offset;
        private int ratio;
        
        public SegmentWeightRatio (String id, Number weight, int originalTotal, int desiredTotal) {
            this.id = id;
            weight = weight == null ? 0 : weight.intValue();
            double realRatio = (weight.doubleValue() / ((double)originalTotal)) 
                * ((double) desiredTotal);
            double floorRatio = (int) Math.floor(realRatio);
            offset = (realRatio - floorRatio);
            ratio = (int) realRatio; 
        }
        
        @SuppressWarnings("unused")
        public double getOffset() {
            return offset;
        }

        public int getRatio() {
            return ratio;
        }
        
        public int compareTo(SegmentWeightRatio o) {
            // We sort on the numbers after the decimal place
            int r =  new Double(offset).compareTo(o.offset);
            if (r == 0) return id.compareTo(o.id);
            return r;
        }
    }
    
    public static Map<String,Integer> normalizeSegmentWeights(Map<String,Integer> segmentWeights) {
        return normalizeSegmentWeights(segmentWeights, 100);
    }
    
    
    public static Map<String,Integer> mergeSegmentWeights(Map<String,Integer> baseMap, Map<String,Integer> mapToAdd) {
        if (baseMap == null) throw new IllegalArgumentException("baseMap cannot be null");
        if (mapToAdd == null) throw new IllegalArgumentException("mapToAdd cannot be null");
        synchronized (mapToAdd) {
            for (String item : mapToAdd.keySet()){
                if (item != null) {
                    int adjustValue = safeGet(mapToAdd,item);
                    int existingValue = 0;
                    if (baseMap.containsKey(item)) {
                        existingValue = safeGet(baseMap,item);
                    }       
                    baseMap.put(item, existingValue+adjustValue);
                }
            }
        }
        return baseMap;
    }
    
    private static int safeGet(Map<String,Integer> map, String key) {
        Integer v = 0;
      
        Object temp = map.get(key);
        if(temp instanceof String){
            v = Integer.parseInt((String) temp);
        }else if (temp instanceof Integer){
            v = (Integer)temp;
        }
        
        return v == null ? 0 : v;
    }
    
    public static Map<String,Integer> setSegmentWeights(Map<String,Integer> baseMap, Map<String,Integer> mapToAdd) {
        if (baseMap == null) throw new IllegalArgumentException("baseMap cannot be null");
        if (mapToAdd == null) throw new IllegalArgumentException("mapToAdd cannot be null");
        synchronized (mapToAdd) {
            for (String item : mapToAdd.keySet()){
                if (item != null) {
                    int adjustValue = safeGet(mapToAdd,item);   
                    baseMap.put(item, adjustValue);
                }
            }
        }
        return baseMap;
    }
    
    public static void cleanSegmentWeightsOfNull(Map<String,Integer> weights) {
       if (weights == null) throw new IllegalArgumentException("weights cannot be null");
       synchronized (weights) {
           Iterator<Entry<String, Integer>> it = weights.entrySet().iterator();
           while(it.hasNext()) { 
               Entry<String,Integer> v = it.next();
               if(v.getKey() == null || v.getValue() == null) { it.remove(); 
               } 
           }
       }
    }

}
