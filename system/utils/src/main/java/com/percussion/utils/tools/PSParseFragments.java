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
package com.percussion.utils.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A utility class used to parse a string that contains multiple fragments. Each
 * fragment is specified between {@link #START_DELIMITER} and
 * {@link #END_DELIMITER}. Each {@link #START_DELIMITER} must has a name which
 * is a space separated from the {@link #START_DELIMITER}.
 * <p>
 * Note, the text beginning with {@link #COMMENT_PREFIX} will be ignored.
 * 
 * @author erikserating
 */
public class PSParseFragments
{
   /**
    * Parse the specified text. Assumed it is in the format of grouping each
    * fragment between {@link #START_DELIMITER} and {@link #END_DELIMITER}.
    * Each {@link #START_DELIMITER} must has a name which is a space separated
    * from the {@link #START_DELIMITER}.
    * 
    * @return the parsed result in a map, where the map key is the name of the
    * fragment, the map value is the fragment content.
    */
   public static Map<String, String> parseContent(String raw)
   {
      Map<String, String> fragments = new HashMap<String, String>();
      String[] lines = splitByNewlines(raw);
      String name = null;
      StringBuilder buffer = new StringBuilder();
      for(String line : lines)
      {
         String trimmedLine = line.trim();
         if(trimmedLine.startsWith(COMMENT_PREFIX))
            continue;
         if(trimmedLine.startsWith(START_DELIMITER))
         {
            String n = 
               trimmedLine.substring(START_DELIMITER.length() + 1).trim();
            if(StringUtils.isBlank(n))
               throw new RuntimeException(
                  "Malformed fragment file. Start fragment missing name.");
            name = n.toUpperCase(); 
         }
         else if(trimmedLine.startsWith(END_DELIMITER))
         {
            fragments.put(name, buffer.toString());
            name = null;
            buffer.setLength(0);
         }
         else
         {
            if(StringUtils.isNotBlank(name))
            {
               buffer.append(line);
               buffer.append('\n');
            }
               
         }
      }
      return fragments;
   }

   /**
    * Splits the specified string by newline and trim off the carriage return
    * for each returned string element.
    * <p>
    * Assume the specified string is delimited by either just newline "\n" or
    * carriage return and line feed, "\r\n".
    *  
    * @param rawText the text in question, may not be <code>null</code>.
    * 
    * @return the split strings, never <code>null</code>. 
    */
   public static String[] splitByNewlines(String rawText)
   {
      String[] lines = rawText.split("\n");
      for (int i=0; i < lines.length; i++)
      {
         String line = lines[i];
         if (line != null && line.length() > 0)
         {
            if (line.charAt(line.length()-1) == '\r')
               lines[i] = line.substring(0, line.length()-1);
         }
      }
      return lines;
   }
   
   /**
    * The beginning of a fragment.
    */
   public static final String START_DELIMITER = "@@@@STARTFRAGMENT";
   
   /**
    * The end of a fragment.
    */
   public static final String END_DELIMITER = "@@@@ENDFRAGMENT";
   
   /**
    * Prefix of a comment
    */
   public static final String COMMENT_PREFIX = "#";
}
