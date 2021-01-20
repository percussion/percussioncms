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
package com.percussion.utils.jsr170;

import java.util.regex.Pattern;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

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
