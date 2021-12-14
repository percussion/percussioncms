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

import java.io.File;

/**
 * A simple pattern matching class with two types of expression characters:
 * single-arbitrary-character and zero-or-more-arbitrary-character matching.
 */
public class PSPatternMatcher
{
   /**
    * Construct a pattern matching object that uses <CODE>matchOne</CODE>
    * to match exactly one arbitrary character and <CODE>matchZeroOrMore</CODE>
    * to match zero or more arbitrary characters.
    *
    * For SQL, use '_' (underscore) to match a single arbitrary character and
    * '%' (percent) to match zero or more arbitrary characters. For typical
    * DOS and UNIX wildcard matching, use '?' (question mark) to match a single
    * arbitrary character and '*' to match zero or more arbitrary characters.
    */
   public PSPatternMatcher(char matchOne, char matchZeroOrMore, String pattern,
      boolean caseSensitive)
   {
      m_matchOne = matchOne;
      m_matchZeroOrMore = matchZeroOrMore;
      m_pat = pattern;
      m_caseSensitive = caseSensitive;
      if (!m_caseSensitive)
         m_pat = m_pat.toUpperCase();
   }

   /**
    * Construct a pattern matching object that uses <CODE>matchOne</CODE>
    * to match exactly one arbitrary character and <CODE>matchZeroOrMore</CODE>
    * to match zero or more arbitrary characters.
    *
    * The pattern matcher will be case sensitive.
    *
    * For SQL, use '_' (underscore) to match a single arbitrary character and
    * '%' (percent) to match zero or more arbitrary characters. For typical
    * DOS and UNIX wildcard matching, use '?' (question mark) to match a single
    * arbitrary character and '*' to match zero or more arbitrary characters.
    */
   public PSPatternMatcher(char matchOne, char matchZeroOrMore, String pattern)
   {
      this(matchOne, matchZeroOrMore, pattern, true);
   }

   /**
    * Returns a new pattern matcher that uses '_' (underscore) to match a
    * single arbitrary character and '%' to match zero or more arbitrary
    * characters.
    *
    * This conforms to the SQL LIKE operator (case sensitive)
    */
   public static PSPatternMatcher SQLPatternMatcher(String pattern)
   {
      return new PSPatternMatcher('_', '%', pattern, true);
   }

   /**
    * Returns a new pattern matcher that uses '_' (underscore) to match a
    * single arbitrary character and '%' to match zero or more arbitrary
    * characters.
    *
    * This will be case insensitive if and only if
    * (new File("foo")).equals(new File("FOO")).
    *
    * This conforms to UNIX and DOS filename matching.
    */
   public static PSPatternMatcher FileWildcardMatcher(String pattern)
   {
      if ((new File("foo")).equals(new File("FOO")))
         return new PSPatternMatcher('?', '*', pattern, false);

      return new PSPatternMatcher('?', '*', pattern, true);
   }


   /**
    * Returns true if and only if <CODE>str</CODE> matches the pattern
    * that this class was constructed with.
    */
   public boolean doesMatchPattern(String str)
   {
      return doesMatchPattern(m_pat, str);
   }

   /**
    * Returns true if and only if <CODE>str</CODE> matches the pattern
    * <CODE>pat</CODE>
    */
   public boolean doesMatchPattern(String pat, String str)
   {
      if (!m_caseSensitive)
      {
         pat = pat.toUpperCase();
         str = str.toUpperCase();
      }

      int pIdx = 0, sIdx = 0;
      int pLen = pat.length(), sLen = str.length();

      while (pIdx < pLen)
      {
         if (pat.charAt(pIdx) == m_matchOne)
         // match exactly one arbitrary character
         {
            if (sIdx >= sLen)
               return false;
            pIdx++;
            sIdx++;
         } else if (pat.charAt(pIdx) != m_matchZeroOrMore)
         // match one specific character
         {
            if (sIdx >= sLen)
               return false;
            if (str.charAt(sIdx) != pat.charAt(pIdx))
               return false;
            pIdx++;
            sIdx++;
         } else
         // match an arbitrary number of arbitrary characters
         {
            while (pIdx < pLen)
            {
               if (pat.charAt(pIdx) == m_matchOne)
               {
                  if (sIdx >= sLen)
                     return false;
                  sIdx++;
               }
               else if (pat.charAt(pIdx) != m_matchZeroOrMore)
                  break;
               pIdx++;
            }

            if (pIdx >= pLen)
               return true;   // end of pattern and we still match

            // Test all remaining matches recursively
            while ( sIdx < sLen )
            {
               if (str.charAt(sIdx) == pat.charAt(pIdx))
               {
                  if (doesMatchPattern(pat.substring(pIdx+1),
                        str.substring(sIdx+1)))
                     return true;
               }
               sIdx++;
            }
            return false;   // nothing matched
         }
      }   // end while

      // if at end of string, it matches
      return (sIdx >= sLen);
   }

   public String getPattern()
   {
      return m_pat;
   }

   public char getMatchOne()
   {
      return m_matchOne;
   }

   public char getMatchZeroOrMore()
   {
      return m_matchZeroOrMore;
   }

   public boolean isCaseSensitive()
   {
      return m_caseSensitive;
   }

   public void setCaseSensitive(boolean caseSensitive)
   {
      m_caseSensitive = caseSensitive;
   }

   // the pattern string to match against
   private String m_pat;

   // the expression character that stands for zero or more arbitrary
   // characters
   private char m_matchZeroOrMore;
   
   // the expression character that stands for a single arbitrary character
   private char m_matchOne;

   // true if case sensitive matching is enabled, false if insensitive
   private boolean m_caseSensitive;
}
