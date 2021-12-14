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
