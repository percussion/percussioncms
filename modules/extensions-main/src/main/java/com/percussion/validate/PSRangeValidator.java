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
package com.percussion.validate;

import com.percussion.extension.IPSFieldValidator;

/**
 * Abstract base class used to write range validators
 * @author dougrand
 */
public abstract class PSRangeValidator implements IPSFieldValidator
{

   /**
    * Check the value against the range. 
    * @param min minimum value for range
    * @param value value to test
    * @param max maximum value for range
    * @param includemin include the minimum in the range
    * @param includemax include the maximum in the range
    * @return <code>true</code> if the value is in range, <code>false</code>
    * otherwise
    */
   public boolean checkRange(Double min, Double value, Double max,
         boolean includemin, boolean includemax)
   {

      if (min != null)
      {
         if (includemin)
         {
            if (min > value)
               return false;
         }
         else
         {
            if (min >= value)
               return false;
         }
      }
      
      if (max != null)
      {
         if (includemax)
         {
            if (max < value)
               return false;
         }
         else
         {
            if (max <= value)
               return false;
         }
      }
      
      return true;
   }

   /**
    * Convert argument to double
    * @param value the value, may be <code>null</code>
    * @return <code>null</code> if the value is <code>null</code>, otherwise
    * returns the double value
    */
   protected Double toDouble(Number value)
   {
      if (value == null)
         return null;
      else
         return value.doubleValue();
   }

}
