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
/*
 * com.percussion.pso.utils SimplifyParameters.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class SimplifyParameters
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(SimplifyParameters.class);
   
   /**
    * Static methods only
    */
   private SimplifyParameters()
   {
   }
   
   public static Map<String,String> simplifyMap(Map<String,Object> input)
   {
      Map<String,String> outMap = new LinkedHashMap<String,String>();
      
      for(Map.Entry<String,Object> entry : input.entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         log.trace("Entry Name {} value type {}", key, value.getClass().getCanonicalName());
         String sval = simplifyValue(value); 
         outMap.put(key, sval); 
      }
      return outMap; 
   }

   public static Map<String,String> simplifyMapStringStringArray(Map<String,String[]> input)
   {
      Map<String,String> outMap = new LinkedHashMap<String,String>();

      for(Map.Entry<String,String[]> entry : input.entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         log.trace("Entry Name {} value type {}", key, value.getClass().getCanonicalName());
         String sval = simplifyValue(value);
         outMap.put(key, sval);
      }
      return outMap;
   }
   
   @SuppressWarnings("unchecked")
   public static String simplifyValue(Object value)
   {
      if(value == null)
      {
         log.debug("null value"); 
         return null; 
      }
      String sval; 
      if(value instanceof String[])
      {
         String[] x = (String[])value; 
         if(x.length == 0)
         {
            log.trace("Empty String array"); 
            return ""; 
         }
         sval = x[0]; 
         log.trace("Converted String[] to {} {}", sval, value);
      }
      else if(value instanceof List)
      {
         List x = (List)value;
         if(x.size() == 0)
         {
            log.debug("Empty List"); 
            return ""; 
         }
         sval = x.get(0).toString(); 
         log.trace("Converted List to {} {}", sval, value);
      }
      else
      {
         sval = value.toString(); 
         log.trace("Converted Object to {}", sval);
      }
      return sval;
   }
   
   @SuppressWarnings("unchecked")
   public static List<String> getValueAsList(Object value)
   {
      List<String> result = new ArrayList<String>();
      if(value == null)
      {
         log.debug("null value returns empty list"); 
         return result;
      }
      if(value instanceof List)
      {
         List<Object> ff = (List<Object>)value;
         for(Object vl : ff)
         {
            result.add(vl.toString());
         }
         return result;
      }
      if(value instanceof String[])
      {
         String[] arr = (String [])value;
         return Arrays.<String>asList(arr);
      }
      if(value instanceof String)
      {
       String[] arr = value.toString().split(LIST_REGEX);
       return Arrays.<String>asList(arr);
      }
      
      
      return result;
   }
   private static final String LIST_REGEX = "[,:;]"; 
}
