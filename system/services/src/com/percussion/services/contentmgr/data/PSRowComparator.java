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
package com.percussion.services.contentmgr.data;

import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.utils.jsr170.PSValueComparator;
import com.percussion.utils.types.PSPair;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;

/**
 * Compares two rows according to a list of property names with directions. This
 * class is an aggregate of the row comparitor itself that can walk through the
 * sort order information, and a contained {@link PSValueComparator} that is 
 * used to do the actual comparisons. Locale information can be set on this 
 * class, but is actually stored in the value comparitor itself.
 * 
 * @author dougrand
 */
public class PSRowComparator implements Comparator<PSRow>
{
   /**
    * A list of comparisons to make. Each pair contains the name of the property
    * to compare and a boolean that is <code>true</code> if the comparison is
    * for ascending order and <code>false</code> if the comparison is for
    * decending order
    */
   private List<PSPair<String, Boolean>> m_comparisons = null;

   /**
    * A comparator for the values in the row
    */
   private PSValueComparator m_comparator = new PSValueComparator();

   /**
    * Ctor build a comparator
    * 
    * @param comparisons the comparisons to make. Each pair contains the name of
    *           the property to compare and a boolean that is <code>true</code>
    *           if the comparison is for ascending order and <code>false</code>
    *           if the comparison is for decending order. Never
    *           <code>null</code>.
    */
   public PSRowComparator(List<PSPair<String, Boolean>> comparisons) {
      if (comparisons == null)
      {
         throw new IllegalArgumentException("comparisons may not be null");
      }
      m_comparisons = comparisons;
   }

   /* (non-Javadoc)
    * @see java.util.Comparator#compare(T, T)
    */
   public int compare(PSRow r1, PSRow r2)
   {
      int result = 0;
      
      try
      {
         for (PSPair<String, Boolean> c : m_comparisons)
         {
            String prop = c.getFirst();
            Boolean dir = c.getSecond();
            result = compareProp(r1, r2, prop);
            if (result != 0)
            {
               return dir ? result : -result;
            }
         }
         // If we've gotten here, there was no comparator that distinguished
         // the two rows, use contentid
         result = compareProp(r1, r2, IPSContentPropertyConstants.RX_SYS_CONTENTID);
         if (result != 0) return result;
       
         // Just do an equals and decide what to return, we might have
         // multiple rows for the same content id with different paths
         if (r1.equals(r2))
            return 0;
         else
            return 1;
      }
      catch (RepositoryException e)
      {
         // Throw a runtime exception, this is unexpected
         throw new RuntimeException(e);
      }
   }

   /**
    * Compare a single property for the two given rows
    * 
    * @param r1 row, assumed never <code>null</code>
    * @param r2 row, assumed never <code>null</code>
    * @param prop property name, row, assumed never <code>null</code> or empty
    * @return plus, minus or zero depending on the comparison results
    * @throws RepositoryException
    */
   private int compareProp(PSRow r1, PSRow r2, String prop)
         throws RepositoryException
   {
      Value v1 = r1.getRawValue(prop);
      Value v2 = r2.getRawValue(prop);

      return m_comparator.compare(v1, v2);
   }
   
   /**
    * Get the locale for the comparitor. 
    * @return the locale, never <code>null</code>.
    */
   public Locale getLocale()
   {
      return m_comparator.getLocale();
   }

   /**
    * Set the locale to use when comparing string values in two rows.
    * @param locale the locale, may be <code>null</code>.
    */
   public void setLocale(Locale locale)
   {
      if (locale == null)
      {
         m_comparator.setLocale(Locale.getDefault());
      }
      else
      {
         m_comparator.setLocale(locale);
      }
   }
}
