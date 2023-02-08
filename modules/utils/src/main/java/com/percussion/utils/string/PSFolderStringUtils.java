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
package com.percussion.utils.string;

import com.percussion.utils.jsr170.PSPath;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

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
