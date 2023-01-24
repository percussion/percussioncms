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
package com.percussion.utils.testing;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Useful printer methods to aid in debugging
 * 
 * @author dougrand
 */
public class PSTestPrinter
{
   /**
    * Print the map entries in alphabetic order of the contained keys
    * @param map the map, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public static void printMapEntries(Map map)
   {
      if (map == null)
      {
         throw new IllegalArgumentException("map may not be null");
      }
      Map<String,String> values = new TreeMap<String, String>();
      Iterator<Map.Entry> i = map.entrySet().iterator();
      while(i.hasNext())
      {
         Map.Entry e = i.next();
         values.put(e.getKey().toString(), e.getValue().toString());
      }
      
      Iterator<Map.Entry<String,String>> is = values.entrySet().iterator();
      while(is.hasNext())
      {
         Map.Entry<String,String> e = is.next();
         System.out.println(e.getKey() + ": " + e.getValue());
      }
   }
   
   /**
    * Print the property entries in alphabetic order of the contained keys
    * @param props the properties, never <code>null</code>
    */
   public static void printMapEntries(Properties props)
   {
      if (props == null)
      {
         throw new IllegalArgumentException("props may not be null");
      }
      Map<String,String> values = new TreeMap<String, String>();
      Iterator<Map.Entry<Object,Object>> i = props.entrySet().iterator();
      while(i.hasNext())
      {
         Map.Entry e = i.next();
         values.put(e.getKey().toString(), e.getValue().toString());
      }
      
      Iterator<Map.Entry<String,String>> is = values.entrySet().iterator();
      while(is.hasNext())
      {
         Map.Entry<String,String> e = is.next();
         System.out.println(e.getKey() + ": " + e.getValue());
      }     
   }
   
}
