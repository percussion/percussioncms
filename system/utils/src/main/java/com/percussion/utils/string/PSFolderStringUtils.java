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
package com.percussion.utils.string;

import com.percussion.utils.jsr170.PSPath;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Methods to manipulate strings that express folder paths
 * 
 * @author dougrand
 */
public class PSFolderStringUtils
{
   /**
    * This translates an input path that uses '%' to indicate a wildcard and ';'
    * to separate paths to an array of pattern objects. The paths are scanned
    * and converted to valid patterns. Any non-alphanumeric, whitespace or
    * forward slash is turned into a hex character to avoid issues with the
    * regex package.
    * 
    * @param folderList the input folder string, may be <code>null</code> or
    *           empty
    * @return an array of patterns, this will be empty for an empty input, but
    *         never <code>null</code>
    */
   public static Pattern[] getFolderPatterns(String folderList)
   {
      if (StringUtils.isBlank(folderList))
      {
         return new Pattern[0];
      }
      String folderPaths[] = folderList.split(";");
      Pattern matchPatterns[] = new Pattern[folderPaths.length];

      int i = 0;

      for (String path : folderPaths)
      {
         StringBuilder matchpath = new StringBuilder(path.length() + 5);
         for (int j = 0; j < path.length(); j++)
         {
            char ch = path.charAt(j);
            if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)
                  || ch == '/')
            {
               matchpath.append(ch);
            }
            else if (ch == '%')
            {
               matchpath.append(".*");
            }
            else
            {
               // Quote everything else to be safe
               matchpath.append("\\u");
               String hex = Integer.toHexString(ch);
               if (hex.length() < 4)
               {
                  int diff = 4 - hex.length();
                  if (diff > 0)
                  {
                     hex = "0000".substring(4 - diff) + hex;
                  }
               }
               matchpath.append(hex);
            }
         }
         path = matchpath.toString();
         if (!path.endsWith(".*") && !path.endsWith("/"))
         {
            path = path + "/";
         }
         matchPatterns[i++] = Pattern.compile(path);
      }
      return matchPatterns;
   }

   /**
    * Find the root portion of the passed path. That is defined as the longest
    * substring that does not involve a component that contains a '%'.
    * 
    * @param path the path, never <code>null</code> or empty
    * @return the largest substring
    */
   public static String getFolderRootPathFromPattern(String path)
   {
      PSPath pspath = new PSPath(path);

      // Find first component with a wildcard
      int first = -1;
      for (int i = 0; i < pspath.getCount(); i++)
      {
         String component = pspath.getName(i);
         if (component.contains("%"))
         {
            first = i;
            break;
         }
      }

      if (first < 0)
      {
         return path;
      }
      else
      {
         StringBuilder b = new StringBuilder();

         for (int i = 0; i < first; i++)
         {
            b.append('/');
            b.append(pspath.getName(i));
         }

         if (b.length() == 0)
         {
            b.append('/');
         }

         return b.toString();
      }
   }

   /**
    * Does one of the passed paths match one of the patterns?
    * 
    * @param paths zero or more paths to match, assumed not <code>null</code>
    * @param matchPatterns one or more patterns to match, assumed not
    *           <code>null</code>
    * @return <code>true</code> if a path is found that matches a pattern
    */
   public static boolean oneMatched(String[] paths, Pattern[] matchPatterns)
   {
      for (String path : paths)
      {
         // Make path end in a slash
         if (!path.endsWith("/"))
            path += "/";
         for (Pattern pattern : matchPatterns)
         {
            if (pattern.matcher(path).matches())
               return true;
         }
      }
      return false;
   }
}
