/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.utils SimplifyParameters.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static final Log log = LogFactory.getLog(SimplifyParameters.class);
   
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
         log.trace("Entry Name " + key + " value type " + value.getClass().getCanonicalName()); 
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
         log.trace("Converted String[] to " + sval + " " + value);
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
         log.trace("Converted List to " + sval + " " + value); 
      }
      else
      {
         sval = value.toString(); 
         log.trace("Converted Object to " + sval); 
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
