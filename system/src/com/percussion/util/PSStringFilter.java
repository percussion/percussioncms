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

import java.util.ArrayList;
import java.util.Collection;

import com.percussion.utils.tools.PSPatternMatcher;

/**
 * This class wraps 1 or more PSPatternMatcher objects to create a filter
 * for any arbitrary string. The filters use SQL LIKE syntax for pattern
 * matching: % matches 0 or more chars, _ matches any single char.
 */
public class PSStringFilter
{
   /**
    * Creates a filter that will check any text against all of the supplied
    * patterns, OR'd together. For example, to find all users whose name
    * begins with a or b, you would supply 2 patterns (assuming you were
    * using SQL LIKE syntax):
    * <ul>
    *    <li>a%</li>
    *    <li>b%</li>
    * </ul>
    *
    * @param filterPatterns A set of patterns to use when filtering text.
    *    All patterns are OR'd together. A <code>null</code> string or an
    *    empty or <code>null</code> array matches nothing.
    *
    * @param matchOne The character to use in a filter pattern to match any
    *    single character in the filtered text. For SQL, this is _, for
    *    DOS it's ?.
    *
    * @param matchZeroOrMore The character to use in a filter pattern to
    *    match 0 or more characters in the filtered text. For SQL this is
    *    %, for DOS it's *.
    *
    * @param caseSensitive if <code>true</code> case sensitive match is done,
    *    otherwise not.
    */
   public PSStringFilter( String [] filterPatterns, char matchOne,
         char matchZeroOrMore, boolean caseSensitive)
   {
      if ((filterPatterns != null) && (filterPatterns.length > 0))
      {
         PSPatternMatcher[] matchers =
            new PSPatternMatcher[filterPatterns.length];
         for (int i = 0; i < filterPatterns.length; i++)
         {
            matchers[i] = new PSPatternMatcher(matchOne, matchZeroOrMore,
               filterPatterns[i], caseSensitive);
         }
         m_matchers = matchers;
      }
   }

   /**
    * Determines whether the supplied text matches the pattern as specified
    * in the ctor.
    *
    * @param text The string to match against the pattern. May not be
    *    <code>null</code>.
    *
    * @return <code>true</code> if it matches, <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if text is <code>null</code>.
    */
   public boolean accept( String text )
   {
      if ( null == text )
         throw new IllegalArgumentException( "text can't be null" );

      if (m_matchers != null)
      {
         for (int i = 0; i < m_matchers.length; i++)
            if (m_matchers[i].doesMatchPattern(text))
               return true;

         return false;
      }

      return true;
   }

   /**
    * Create a String filter from a semicolon delimited list of filter patterns.
    * For each pattern '_' will be used to indicate to match any one character
    * and '%' will be used to indicate to match any sequence of zero or more
    * characters. Note that semi-colon may be used to escape a semi-colon,
    * allowing semi-colons to be part of a filter pattern. Comparison is case
    * sensitive while matching the pattern.
    *
    * @param filter The filter to use.  <code>null</code> will indicate
    * that ALL strings are a match. Empty will indicate that only empty strings
    * are a match.
    *
    * @see #PSStringFilter(String[], char, char, boolean)
    */
   public PSStringFilter(String filter)
   {
      this(parseDelimitedList(filter,';',';'), '%', '_', true);
   }

   /**
    * Takes a character delimited list and converts it to an array. The
    * caller specifies the delimiter and the delimiter escape char.
    *
    * @param delimitedList A list of items delimited by the specified char.
    *    May be <code>null</code>.
    *
    * @param delim The char used to separate the items in the supplied list.
    *
    * @param delimEscape The char used to escape the delim if it needs to be
    *    included as part of an item.
    *
    * @return An array of strings. The length of the array will be equal to
    *    one greater than the number of unescaped delimiters. If the supplied
    *    list is <code>null</code>, <code>null</code> is returned.
    */
   public static String [] parseDelimitedList( String delimitedList,
         char delim, char delimEscape )
   {
      if ( null == delimitedList )
         return null;

      Collection items = new ArrayList();
      int pos = delimitedList.indexOf( delim );
      while ( pos >= 0 )
      {
         int offset = 0;
         if ( pos == 0 )
         {
            // the 1st item in the substring is empty
            items.add( "" );
            delimitedList = delimitedList.substring( pos + 1 );
         }
         else if ( delimitedList.charAt( pos-1 ) != delimEscape )
         {
            // the 1st item in the substring is non-empty
            items.add( delimitedList.substring( 0, pos ));
            delimitedList = delimitedList.substring( pos + 1 );
         }
         else
         {
            /* the 1st item in the substring is non-empty and it contains an
               escaped delimiter
               strip the escape char */
            delimitedList = delimitedList.substring(0, pos-1)
                  + delimitedList.substring(pos);
            offset = pos;
         }
         pos = delimitedList.indexOf( delim, offset );
      }
      items.add( delimitedList );
      String [] a = new String[items.size()];
      return (String []) items.toArray(a);
   }

   private PSPatternMatcher[] m_matchers = null;
}

