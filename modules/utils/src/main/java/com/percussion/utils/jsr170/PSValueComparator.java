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

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Comparator for value objects
 * 
 * @author dougrand
 */
public class PSValueComparator implements Comparator<Value>
{
   /**
    * The locale to use when comparing strings or string versions of values.
    * Never <code>null</code>.
    */
   private Locale m_locale = Locale.getDefault();
   
   /**
    * The collator to compare string values. Initiallly <code>null</code>, but
    * set to a value on first use. Reset to <code>null</code> if the locale
    * is changed.
    */
   private Collator m_collator = null;
   
   /**
    * Compare the two values. The values must be of the same type or a runtime
    * exception is thrown. The values are compared according to the type
    * 
    * @param v1 the first value
    * @param v2 the second value
    * @return <code>+1</code> if v1 is greater than v2 or v2 is
    *         <code>null</code>, <code>-1</code> if v1 is less than v2 or
    *         v1 is <code>null</code> , or <code>0</code> if the two values
    *         are equal
    */
   public int compare(Value v1, Value v2) 
   {
      if (v1 != null && v2 != null && v1.getType() != v2.getType())
      {
         throw new RuntimeException("Values must have the same type");
      }

      if (v1 == null && v2 == null)
         return 0;

      if (v1 == null)
      {
         return -1;
      }

      if (v2 == null)
      {
         return 1;
      }

      int result = 0;
      try
      {
         switch (v1.getType())
         {
            case PropertyType.BOOLEAN :
               if (v1.getBoolean() && !v2.getBoolean())
                  result = 1;
               else if (!v1.getBoolean() && v2.getBoolean())
                  result = -1;
               break;
            case PropertyType.LONG :
               if (v1.getLong() > v2.getLong())
                  result = 1;
               else if (v1.getLong() < v2.getLong())
                  result = -1;
               break;
            case PropertyType.DOUBLE :
               if (v1.getDouble() > v2.getDouble())
                  result = 1;
               else if (v1.getDouble() < v2.getDouble())
                  result = -1;
               break;
            case PropertyType.DATE :
               if (v1.getDate().getTimeInMillis() > v2.getDate().getTimeInMillis())
                  result = 1;
               else if (v1.getDate().getTimeInMillis() < v2.getDate()
                     .getTimeInMillis())
                  result = -1;
               break;
            default :
               if (m_collator == null)
               {
                  m_collator = Collator.getInstance(m_locale);
                  m_collator.setStrength(Collator.TERTIARY);
               }
               result = m_collator.compare(v1.getString(), v2.getString());
               break;
         }
      }
      catch (ValueFormatException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalStateException e)
      {
         throw new RuntimeException(e);
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }

      return result;
   }

   /**
    * @return the locale
    */
   public Locale getLocale()
   {
      return m_locale;
   }

   /**
    * Set the locale for string comparisons
    * @param locale the locale to set, never <code>null</code>
    */
   public void setLocale(Locale locale)
   {
      if (locale == null)
      {
         throw new IllegalArgumentException("locale may not be null");
      }
      m_locale = locale;
      m_collator = null;
   }
   
   
}
