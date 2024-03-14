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
package com.percussion.utils.jsr170;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import java.util.regex.Pattern;

/**
 * A name pattern filter handles objects that fulfill the <code>Item</code>
 * interface or uses the <code>toString</code> method. It calls the
 * <code>getName</code> method for <code>Item</code> and checks the name
 * against the patterns.
 * 
 * @author dougrand
 */
public class PSNamePatternFilter implements Predicate
{
   /**
    * The set of patterns to try and match. Never <code>null</code> after
    * construction.
    */
   private Pattern m_matchPatterns[] = null;

   /**
    * Ctor
    * 
    * @param pattern the pattern, never <code>null</code> or empty. See the
    *           javadocs for JSR-170 to understand the pattern syntax.
    */
   public PSNamePatternFilter(String pattern) {
      if (StringUtils.isBlank(pattern))
      {
         throw new IllegalArgumentException("pattern may not be null or empty");
      }
      m_matchPatterns = processFilter(pattern);
   }

   public boolean evaluate(Object arg0)
   {
      String name = null;

      if (arg0 instanceof Item)
      {
         try
         {
            Item item = (Item) arg0;
            name = item.getName();
         }
         catch (RepositoryException e)
         {
            return false;
         }
      }
      else
      {
         name = arg0.toString();
      }

      for (int i = 0; i < m_matchPatterns.length; i++)
      {
         if (m_matchPatterns[i].matcher(name).matches())
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Translate JSR-170 property pattern into an array of regex patterns
    * 
    * @param filterpattern the pattern, never <code>null</code> or empty
    * @return an array of patterns with at least one entry, never
    *         <code>null</code>
    */
   private Pattern[] processFilter(String filterpattern)
   {
      if (StringUtils.isBlank(filterpattern))
      {
         throw new IllegalArgumentException(
               "filterpattern may not be null or empty");
      }
      String patterns[] = filterpattern.split("\\x7c");
      if (patterns.length == 0)
      {
         patterns = new String[]
         {filterpattern};
      }
      Pattern rval[] = new Pattern[patterns.length];
      for (int i = 0; i < patterns.length; i++)
      {
         String p = patterns[i].replaceAll("\\x2a", ".*");
         p = p.replaceAll(":", "\\\\\\x3a");
         rval[i] = Pattern.compile(p);
      }
      return rval;
   }

}
