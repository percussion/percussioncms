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

package com.percussion.util;

import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSTextLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

/**
 * The PSCalculation class performs basic mathematical operation and computation
 * for number related Objects. These objects include java.lang.Number, all its
 * subclasses, String, com.percussion.design.objectstore.PSNumericLiteral, and
 * com.percussion.design.objectstore.PSTextLiteral. Moreover, it also adjusts
 * calendar and time.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.0
 */
public class PSCalculation
{
   /**
    * Make sure the input object is really a number related object. The returned
    * object is either java.lang.Number or one of its subclasses. This method
    * is called by add, subtract, multiply, and divide.
    *
    * @param   o  an input object
    *
    * @return     either java.lang.Number or one of its subclasses' object
    *
    */
   public static Object numberVerify(Object o)
   {
      if (o == null){
         Object[] args = { "null is not a number object", "numberVerify" };
         throw new IllegalArgumentException("numberVerify exception: null is not a number object");
      }

      if ((o instanceof Number) || (o instanceof BigDecimal) || (o instanceof BigInteger) ||
         (o instanceof Byte) || (o instanceof Double) || (o instanceof Float) ||
         (o instanceof Integer) || (o instanceof Long) || (o instanceof Short) ){
         return o;
      }

      if (o instanceof String){
         try{
            return new Double((String)o);
         } catch (NumberFormatException e){
            throw new IllegalArgumentException("NumberFormatException: numberVerify exception");
         }
      }

      if (o instanceof PSNumericLiteral){
         BigDecimal num = ((PSNumericLiteral)o).getNumber();
         return num;
      }

      if (o instanceof PSTextLiteral){
         String text = ((PSTextLiteral)o).getText();
         try{
            return new Double(text);
         } catch (NumberFormatException e){
            throw new IllegalArgumentException("NumberFormatException: numberVerify exception");
         }
      }

      // The rest of data types are not numbers, even Date and Boolean
      throw new IllegalArgumentException("numberVerify exception: input is not a number related object");
   }

   /**
    * Sum of (Obj1 + Obj2), where Obj1 and Obj2 are number related objects. Since
    * null and zero are different, only addition of both nulls are allowed, which
    * returns a null. However, if only one of the two input objects is null, then
    * a IllegalArgumentException will be thrown. Note: calling numberVerify method
    * in advance is not needed, add method does it automatically.
    *
    * @return     the sum which is a number related object, or null
    *             if both Obj1 and Obj2 are null
    */
   public static Object add(Object o1, Object o2)
   {
      if ((o1 == null) && (o2 == null))
         return null;

      // We do not allow null being used as zero
      if ((o1 == null) || (o2 == null)){
         throw new IllegalArgumentException("PSCalculation/add exception: null parameter(s)");
      }

      try{
         o1 = numberVerify(o1);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/add exception: The first parameter is not a number");
      }

      try{
         o2 = numberVerify(o2);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/add exception: The second parameter is not a number");
      }

      double result = ((Number)o1).doubleValue() + ((Number)o2).doubleValue();
      return new Double(result);
   }

   /**
    * The result of (Obj1 - Obj2), where Obj1 and Obj2 are number related objects.
    * Since null and zero are different, only subtraction of both nulls are allowed,
    * which returns a null. However, if only one of the two input objects is null,
    * then a IllegalArgumentException will be thrown. Note: calling numberVerify method
    * in advance is not needed, subtract method does it automatically.
    *
    * @return     the difference which is a number related object, or null
    *             if both Obj1 and Obj2 are null
    */
   public static Object subtract(Object o1, Object o2)
   {
      if ((o1 == null) && (o2 == null))
         return null;

      if ((o1 == null) || (o2 == null)){
         throw new IllegalArgumentException("PSCalculation/subtract exception: null parameter(s)");
      }

      try{
         o1 = numberVerify(o1);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/subtract exception: The first parameter is not a number");
      }

      try{
         o2 = numberVerify(o2);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/subtract exception: The second parameter is not a number");
      }

      double result = ((Number)o1).doubleValue() - ((Number)o2).doubleValue();
      return new Double(result);
   }

   /**
    * The result of (Obj1 * Obj2), where Obj1 and Obj2 are number related objects
    * Since null and zero are different, only multiplication of both nulls are allowed,
    * which returns a null. However, if only one of the two input objects is null,
    * then a IllegalArgumentException will be thrown. Note: calling numberVerify method
    * in advance is not needed, multiply method does it automatically.
    *
    * @return     the production which is a number related object, or null
    *             if both Obj1 and Obj2 are null
    */
   public static Object multiply(Object o1, Object o2)
   {
      if ((o1 == null) && (o2 == null))
         return null;

      if ((o1 == null) || (o2 == null)){
         throw new IllegalArgumentException("PSCalculation/multiply exception: null parameter(s)");
      }

      try{
         o1 = numberVerify(o1);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/multiply exception: The first parameter is not a number");
      }

      try{
         o2 = numberVerify(o2);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/multiply exception: The second parameter is not a number");
      }

      double result = ((Number)o1).doubleValue() * ((Number)o2).doubleValue();
      return new Double(result);
   }

   /**
    * The result of (Obj1 / Obj2), where Obj1 and Obj2 are number related objects.
    * Since null and zero are different, only division of both nulls are allowed,
    * which returns a null. However, if only one of the two input objects is null,
    * then a IllegalArgumentException will be thrown. Note: calling numberVerify method
    * in advance is not needed, divide method does it automatically.
    *
    * @return     the division which is a number related object, or null
    *             if both Obj1 and Obj2 are null
    */
   public static Object divide(Object o1, Object o2)
   {
      if ((o1 == null) && (o2 == null))
         return null;

      int errCode = com.percussion.server.IPSServerErrors.ARGUMENT_ERROR;

      if ((o1 == null) || (o2 == null)){
         throw new IllegalArgumentException("PSCalculation/divide exception: null parameter(s)");
      }

      try{
         o1 = numberVerify(o1);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/divide exception: The first parameter is not a number");
      }

      try{
         o2 = numberVerify(o2);
      } catch (IllegalArgumentException e){
         throw new IllegalArgumentException("PSCalculation/divide exception: The second parameter is not a number");
      }

      double temp = ((Number)o2).doubleValue();
      if (temp == 0){
         throw new IllegalArgumentException("PSCalculation/divide exception: divided by zero");
      }

      double result = ((Number)o1).doubleValue() / temp;
      return new Double(result);
   }

   /**
    * Adjust a given calendar date by updating year, month, date, hour, minute,
    * and second.
    * <p>
    * Note 1 (by Jian Huang): com.percussion.util.PSDate, rather than
    * java.util.Calendar, is adopted as the return-type to prevent developers
    * not familiar with the subject from using java.util.Calendar incorrectly.
    * For instance, as of JDK 1.2, get(Calendar.MONTH) returns an integer from 0
    * to 11 instead of 1 to 12. Also, get(Calendar.HOUR_OF_DAY) uses 24-hour clock
    * while get(Calendar.HOUR) uses 12-hour clock which needs indication from
    * get(Calendar.AM_PM). Furthermore, java.util.Calendar's toString() method
    * is only for debugging purpose, not for real usage.
    * <p>
    * Note 2 (by Jian Huang): if you really want to use Calendar object, the
    * information stored in com.percussion.util.PSDate is enough to create a
    * Calendar object.
    *
    * @param   dateOld  the initial date to be adjusted
    * @param   numYear  the number of year to adjust
    * @param   numMonth the number of month to adjust
    * @param   numDate  the number of day to adjust
    * @param   numHour  the number of hour to adjust
    * @param   numMin   the number of minute to adjust
    * @param   numSec   the number of second to adjust
    *
    * @return           a PSDate representation of the updated calendar,
    *                   or null if dateOld is null
    */
   public static PSDate dateAdjust(Calendar dateOld, int numYear, int numMonth,
                              int numDate, int numHour, int numMin, int numSec)
   {
      if (dateOld == null)
         return null;

      dateOld.add(Calendar.SECOND, numSec);
      dateOld.add(Calendar.MINUTE, numMin);
      dateOld.add(Calendar.HOUR_OF_DAY, numHour);   // 24 hour clock
      dateOld.add(Calendar.DATE, numDate);
      dateOld.add(Calendar.MONTH, numMonth);
      dateOld.add(Calendar.YEAR, numYear);

      // Note: if you really want to return a Calendar object, then "return dateOld;" here

      int hr = dateOld.get(Calendar.HOUR_OF_DAY);   // 24 hour clock
      int mi = dateOld.get(Calendar.MINUTE);
      int se = dateOld.get(Calendar.SECOND);

      int da = dateOld.get(Calendar.DATE);
      // Add one to make sure that months are integers from 1 to 12
      int mo = dateOld.get(Calendar.MONTH) + 1;
      int yr = dateOld.get(Calendar.YEAR);

      // Warning: do NOT use Java Calendar's toString() method !!!
      // Warning: do NOT return Calendar object to prevent from "month" confusion
      // Warning: do NOT create java.util.Date object by using deprecated
      // constructors/methods, it is not safe in the long run
      PSDate newDate = new PSDate(yr, mo, da, hr, mi, se);

      return newDate;
   }
}
