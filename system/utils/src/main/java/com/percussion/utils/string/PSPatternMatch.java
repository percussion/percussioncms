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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This is used to determine if a string matches a SQL like pattern, "%abc%".
 * <p>
 * A typical usage of this class is described below:
 * <pre>
 *    PSPatternMatch pattern = new PSPatternMatch("*abc*");
 *    boolean isMatch = pattern.match("xabc"); // true 
 *    isMatch = pattern.match("xab"); // false
 *    
 *    pattern = new PSPatternMatch("%abc%", "%");
 *    boolean isMatch = pattern.match("xabc"); // true 
 *    isMatch = pattern.match("xab"); // false
 * </pre> 
 *
 * @author YuBingChen
 */
public class PSPatternMatch
{
   /**
    * The begin string of the pattern. It is <code>null</code> if there is a
    * wild card at the beginning.
    */
   private String m_begin = null;

   /**
    * The end string of the pattern. It is <code>null</code> if there is a
    * wild card at the end.
    */
   private String m_end = null;

   /**
    * A list of string tokens between the wild-card. It is <code>null</code>
    * if the pattern is a constant.
    */
   private List<String> m_tokens = null;

   /**
    * Determines if the pattern is a constant string without a wild card. 
    * It is <code>null</code> if the pattern is a constant.
    */
   private boolean m_isConstant = false;

   /**
    * The default wild-card.
    */
   public static final String WILD_CARD = "*";
   
   /**
    * Creates an instance with the given pattern with the default wild-card
    * {@link #WILD_CARD}.
    *  
    * @param pattern the pattern used to determine if a string matches. It may
    * not be <code>null</code> or empty.
    */
   public PSPatternMatch(String pattern)
   {
      this(pattern, "*");
   }

   /**
    * Creates an instance with the given pattern and a specified wild-card.
    *  
    * @param pattern the pattern used to determine if a string matches. It may
    * not be <code>null</code> or empty.
    */
   public PSPatternMatch(String pattern, String wildCard)
   {
      if (StringUtils.isBlank(pattern))
         throw new IllegalArgumentException("pattern may not be null or empty.");
      if (StringUtils.isBlank(wildCard))
         throw new IllegalArgumentException("wildCard may not be null or empty.");

      if (pattern.indexOf(wildCard) == -1)
      {
         m_isConstant = true;
         m_begin = pattern;
         return;
      }
      
      String[] tokens = StringUtils.split(pattern, wildCard);
      m_tokens = new ArrayList<String>(Arrays.asList(tokens));

      if (!pattern.startsWith(wildCard) && m_tokens.size() > 0)
      {
         m_begin = m_tokens.get(0);
         m_tokens.remove(0);
      }
      
      if (!pattern.endsWith(wildCard) && m_tokens.size() > 0)
      {
         m_end = m_tokens.get(m_tokens.size() - 1);
         m_tokens.remove(m_tokens.size() - 1);
      }
   }

   /**
    * Determines if a given string matches the current pattern.
    * 
    * @param str the string in question, may be not <code>null</code> or empty.
    * 
    * @return <code>true</code> if the string matches the pattern; otherwise
    * return <code>false</code>.
    */
   public boolean match(String str)
   {
      if (StringUtils.isBlank(str))
         throw new IllegalArgumentException("str may not be null or empty.");

      if (m_isConstant)
         return m_begin.equalsIgnoreCase(str);

      if (m_begin != null)
      {
         if (!m_begin.regionMatches(true, 0, str, 0, m_begin.length()))
            return false;
         str = str.substring(m_begin.length());
      }
      for (String t : m_tokens)
      {
         int i = str.indexOf(t);
         if (i == -1)
            return false;
         str = str.substring(i + t.length());
      }
      if (m_end != null)
      {
         if (!str.endsWith(m_end))
            return false;
      }
      return true;
   }

   /**
    * A utility method to match a list of strings against a specified pattern.
    * 
    * @param patternS the pattern to match, not <code>null</code> or empty.
    * @param strList the list of strings in question, not <code>null</code>,
    * may be empty.
    * 
    * @return the matched strings, never <code>null</code>, may be empty.
    */
   public static Collection<String> matchedStrings(String patternS,
         Collection<String> strList)
   {
      if (StringUtils.isBlank(patternS))
         throw new IllegalArgumentException(
               "patternS may not be null or empty.");
      if (strList == null)
         throw new IllegalArgumentException("strList may not be null.");
      
      PSPatternMatch pattern = new PSPatternMatch(patternS);
      List<String> result = new ArrayList<String>();
      for (String s : strList)
      {
         if (pattern.match(s))
            result.add(s);
      }
      return result;
   }
}
