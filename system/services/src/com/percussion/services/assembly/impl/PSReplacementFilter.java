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

package com.percussion.services.assembly.impl;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;

public class PSReplacementFilter
{
   public static final String HASH = "#";

   public static final String QUERY = "?";

   public static final String AMP = "&";

   private static final String DASH = "-";

   private static final HashMap<String, String> mFilterSet = new HashMap<String, String>()
   {
      {
         put("\\", "/");
         put(":", DASH);
         put("%", DASH);
         put(";", DASH);
         put("*", DASH);
         put("?", DASH);
         put("<", DASH);
         put(">", DASH);
         put("|", DASH);
         put("[", DASH);
         put("]", DASH);
         put(" ", DASH);
      }
   };

   private static final String UTF_8 = "UTF-8";

   /**
    * Applies the filters to a given String
    * 
    * @param stringForFilter the String to be filtered
    * @return a filtered String
    */
   public static String filter(final String stringForFilter)
   {
      String filterString = stringForFilter;

      filterString = decode(filterString);

      // Handle Anchors - clean, place anchor at end of URL
      String anchor = getAnchor(filterString);
      if (anchor != null && !anchor.isEmpty())
         filterString = filterString.replace(anchor, "");

      Iterator<String> iterator = mFilterSet.keySet().iterator();

      while (iterator.hasNext())
      {
         String target = iterator.next();
         String replacement = mFilterSet.get(target);

         filterString = filterString.replace(target, replacement);
      }

      if (anchor != null && !anchor.isEmpty())
         filterString = filterString.concat(anchor);

      while(filterString.contains("--")){
         filterString = filterString.replaceAll("--",DASH);
      }
      return filterString;
   }

   public static String getAnchor(String url)
   {

      if (url != null)
      {
         int anchorPosition = url.indexOf(HASH);
         if (anchorPosition >= 0)
         {
            String anchor = url.substring(anchorPosition);
            if (anchor.contains(QUERY))
               anchor = anchor.substring(0, anchor.indexOf(QUERY));
            if (anchor.contains(AMP))
               anchor = anchor.substring(0, anchor.indexOf(AMP));
            return anchor;
         }
      }
      return null;
   }

   private static String decode(final String stringForFilter)
   {
      String filterString = stringForFilter;
      filterString = filterString.replace("\\", "/");
      try
      {
         filterString = URLDecoder.decode(filterString, UTF_8);
      }
      catch (Exception e)
      {
         try
         {
            filterString = "http://" + filterString;
            filterString = URLDecoder.decode(filterString, UTF_8);
            filterString = filterString.replace("http://", "");
         }
         catch (Exception ee)
         {
            filterString = stringForFilter;
         }
      }
      return filterString;
   }
}
