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

package com.percussion.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple class that handles "escaped" text.
 */

public class PSOutputEscaping {

   /**
    * This method searches for the following strings
    * "&amp","&lt","&gt","&quot","&apos"
    * and replaces them with "&","<",">","\"","'" respectively
    *
    * @param escaped the string in which to perform the string
    * substitution, may not be <code>null</code> or empty.
    *
    * @return the modified string
    *
    * @throws IllegalArgumentException if escaped is <code>null</code> or
    * empty.
    */
   public static String unEscape(String escaped)
   {
      if ((escaped == null) || (escaped.trim().length() == 0))
         throw new IllegalArgumentException(
            "escaped string may not be null or empty");

      StringBuilder outBuf = new StringBuilder();
      StringBuilder remainder = new StringBuilder(escaped);
      while(remainder.length() > 0)
         {
         int ampPos = remainder.toString().indexOf("&");
         int endPos = 0;
         int nextAmp = 0;
         if(ampPos == -1)
         {
            // no ampersands left
            outBuf.append(remainder.toString());
            endPos = remainder.length();
         }
         else
         {
            // copy all chars up to the first &amp;
            outBuf.append(remainder.substring(0,ampPos));
            remainder.delete(0,ampPos);
            // first char is now &amp;
            // endPos points at first semicolon
            endPos = remainder.toString().indexOf(";");
            nextAmp = remainder.substring(1).indexOf("&");
            if(nextAmp > 0 && endPos > 0 && endPos > nextAmp)
            {
               endPos = -1;
            }
            if(endPos == -1 || endPos > 15)
            {
               // &amp; but no semicolon or too far away
               // just copy the ampersand and skip over it
               outBuf.append("&");
               endPos = 1;
            }
            else
            {
               boolean found = false;
               endPos++; //skip over the semicolon;
               for(int i = 0;i<5;i++) //hack alert (how to dimension array??)
               {
                  String ent = ENTITYTABLE[i][0];
                  if(ENTITYTABLE[i][0].equals(remainder.substring(0,endPos)))
                  {
                     outBuf.append(ENTITYTABLE[i][1]);
                     found = true;
                  } // if
               } // for
               if(!found)
               {
                  // just copy to the output
                  outBuf.append(remainder.substring(0,endPos));
               } // if
            } // else
         }
         remainder.delete(0,endPos);
      }
      return outBuf.toString();
   }
   
   /**
    * This method searches for the following chars
    * "&","<",">","\"","'"    * 
    * and replaces them with "&amp","&lt","&gt","&quot","&apos"
    * respectively
    *
    * @param str the string in which to perform the string
    * substitution, may not be <code>null</code> or empty.
    *
    * @return the modified string
    *
    * @throws IllegalArgumentException if string is <code>null</code> or
    * empty.
    */
   public static String escape(String str)
   {
      if ((str == null) || (str.trim().length() == 0))
          throw new IllegalArgumentException(
             "String may not be null or empty");
      StringBuilder out = new StringBuilder();
      String current = null;
      for(int i = 0; i < str.length(); i++)
      {
         current = str.substring(i, i + 1);
         if(ms_escapeMap.containsKey(current))
            out.append((String)ms_escapeMap.get(current));
         else
            out.append(current);            
      }
      return out.toString();
   }

   /**
    * Strings which need to be substituted/unescaped.
    */
   public static final String AMP = "&amp;";
   public static final String LT = "&lt;";
   public static final String GT = "&gt;";
   public static final String QUOT = "&quot;";
   public static final String APOS = "&apos;";
   
   /**
    * Map of character to entity mappings
    */
   public static final Map ms_escapeMap = new HashMap();
   
   static
   {
      ms_escapeMap.put("&", AMP);
      ms_escapeMap.put("<", LT);
      ms_escapeMap.put(">", GT);
      ms_escapeMap.put("\"", QUOT);
      ms_escapeMap.put("'", APOS);
      
   }

   /**
    * Array of the strings which need to be substituted/unescaped
    * and the strings with which the substituion will be done.
    */
   private static final String ENTITYTABLE[][] =
   {
     { AMP, "&" },
     { LT, "<" },
     { GT, ">" },
     { QUOT, "\"" },
     { APOS, "'"}
   };

}
