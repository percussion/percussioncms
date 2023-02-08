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
