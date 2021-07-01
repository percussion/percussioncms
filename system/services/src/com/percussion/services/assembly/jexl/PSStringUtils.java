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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * String utilities for Jexl bindings or Velocity macros
 * 
 * @author dougrand
 */
public class PSStringUtils extends PSJexlUtilBase
{
   /**
    * Convenience method, call {@link #stringToMap(String, String) 
    * stringToMap(String, "x-www-form-urlencoded")}
    */
   @IPSJexlMethod(description = "Process a string which contains parameters and "
         + "values in the standard URL format, i.e. param=value&param2=value2. "
         + "It returns a String to String map of the names and values."
         + "The values are URL decoded with 'x-www-form-urlencoded'", params =
   {@IPSJexlParam(name = "paramstring", type = "String", 
         description = "parameter string, if null then an empty map is returned")})
   public Map<String, String> stringToMap(String paramstring)
         throws UnsupportedEncodingException
   {
      return stringToMap(paramstring, "UTF-8");
   }

   /**
    * Process a string which contains parameters and values in the standard URL
    * format, i.e. param=value&param2=value2. It returns a String to String map
    * of the names and values
    * 
    * @param paramstring parameter string, if null then an empty map is returned
    * @param enc the encoding used to decode the value of the parameter, never
    *    <code>null</code> or empty.
    * @return a string to string map of the names and values, never
    *         <code>null</code>
    * @throws UnsupportedEncodingException
    */
   @IPSJexlMethod(description = "Process a string which contains parameters and "
         + "values in the standard URL format, i.e. param=value&param2=value2. "
         + "It returns a String to String map of the names and values.", params =
   {@IPSJexlParam(name = "paramstring", type = "String", description = "parameter string, if null then an empty map is returned"),
    @IPSJexlParam(name = "enc", type = "String", description = "encoding string")})
   public Map<String, String> stringToMap(String paramstring, String enc)
      throws UnsupportedEncodingException
   {
      if (StringUtils.isBlank(enc))
         throw new IllegalArgumentException("enc may not be null or empty.");
      
      Map<String, String> rval = new HashMap<>();
      if (paramstring == null)
      {
         return rval;
      }
      String tokens[] = paramstring.split("&");
      for (String token : tokens)
      {
         String pieces[] = token.split("=");
         if (pieces.length == 2)
         {
            String name = pieces[0];
            String value = URLDecoder.decode(pieces[1], enc);
            rval.put(name, value);
         }
         else
         {
            ms_log.debug("Ignore bad parameter: " + token);
         }
      }
      return rval;
   }

   /**
    * Remove namespace from property name and return the Rhythmyx field name.
    * 
    * @param jsr170name property name, never <code>null</code>
    * @return the processed property name, never <code>null</code>
    */
   @IPSJexlMethod(description = "Remove namespace from property name and "
         + "return the Rhythmyx field name.", params =
   {@IPSJexlParam(name = "jsr170name", description = "property name")})
   public String extractFieldName(String jsr170name)
   {
      if (jsr170name == null)
      {
         throw new IllegalArgumentException("jsr170name may not be null");
      }
      String parts[] = jsr170name.split(":");
      if (parts.length > 1)
      {
         return parts[parts.length - 1];
      }
      else
      {
         return jsr170name;
      }
   }

   /**
    * Compare two things that should be numbers but may be represented by
    * strings or by numbers.
    * 
    * @param a a string with a numeric value or a number
    * @param b a string with a numeric value or a number
    * @return <code>true</code> if the two represent the same number
    */
   @IPSJexlMethod(description = "Compare two things that should be numbers "
         + "but may be represented by strings or by numbers.", params =
   {
         @IPSJexlParam(name = "a", description = "a string with a numeric value or a number"),
         @IPSJexlParam(name = "b", description = "a string with a numeric value or a number")})
   public boolean equalNumbers(Object a, Object b)
   {
      return extractNumber(a) == extractNumber(b);
   }

   /**
    * Extract a numeric value from the argument, or zero if the argument is
    * either not a Number or not a String with a numeric value
    * 
    * @param a a string with a numeric value or a number
    * @return the numeric value, or zero if not a number
    */
   @IPSJexlMethod(description = "Extract a numeric value from the argument, or zero"
         + " if the argument is either not a Number or not a String with a numeric value", params =
   {@IPSJexlParam(name = "a", description = "a string with a numeric value or a number")})
   public long extractNumber(Object a)
   {
      if (a == null)
         return 0;
      else if (a instanceof Number)
      {
         Number na = (Number) a;
         return na.longValue();
      }
      else if (a instanceof String)
      {
         try
         {
            return Long.parseLong((String) a);
         }
         catch (NumberFormatException e)
         {
            return 0;
         }
      }
      else
      {
         return 0;
      }
   }

   /**
    * Remove leading and trailing whitespace, and replace any embedded sequence
    * of whitespace characters with a single space each.
    * 
    * @param src the source string, may be empty but never <code>null</code>
    * @return the trimmed, compacted result string, never <code>null</code>
    */
   @IPSJexlMethod(description = "Remove leading and trailing whitespace, and replace any embedded sequence of whitespace characters with a single space each.", params =
   {@IPSJexlParam(name = "src", description = "the source string, never null or empty")})
   public String stripSpaces(String src)
   {
      if (src == null)
      {
         throw new IllegalArgumentException("src may not be null");
      }
      src = src.trim();
      src = src.replaceAll("\\s+", " ");
      return src;
   }
   
   @IPSJexlMethod(description = "Return a new instence of JSONObject see www.json.org", params =
      {})
   public JSONObject getJSONObject()
   {
      return new JSONObject();
   }
   
   @IPSJexlMethod(description = "Return a new instence of JSONArray see www.json.org", params =
      {})
   public JSONArray getJSONArray()
   {
      return new JSONArray();
   }
   
   @IPSJexlMethod(description = "Return a new instence of JSONStringer see www.json.org", params =
      {})
   public JSONStringer getJSONStringer()   
   {
      return new JSONStringer();
   }
   
   @IPSJexlMethod(description = "Return a new ArrayList instance", params =
      {})
   public ArrayList<Object> getArrayList()
   {
      return new ArrayList<>();
   }
   
   @IPSJexlMethod(description = "Return a new HashMap instance", params =
      {})
   public Map<Object,Object> getHashMap()
   {
      return new HashMap<>();
   }
   
   /**
    * Logger.
    */
   private static final Logger ms_log = LogManager.getLogger(PSStringUtils.class);
   
}
