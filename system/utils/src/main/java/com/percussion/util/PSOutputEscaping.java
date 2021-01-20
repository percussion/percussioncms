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

      StringBuffer outBuf = new StringBuffer();
      StringBuffer remainder = new StringBuffer(escaped);
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
      StringBuffer out = new StringBuffer();
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
