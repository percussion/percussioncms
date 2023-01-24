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
package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * UDF to parse two dates (or date strings) and return the absolute difference
 * in days between the two dates. Second date is optional, if not supplied 
 * today's date is considered for the difference.
 */
public class PSDateDifference extends PSDefaultExtension
      implements
         IPSUdfProcessor
{
   /**
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[],
    * com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;
      if (size < 1)
      {
         String arg0 = "expects at least one parameter, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args =
         {arg0, "PSDateDifference/processUdf"};
         throw new PSConversionException(0, args);
      }
      if (params[0] == null || params[0].toString().length() < 1)
      {
         String msg = "Invaid parameter ";
         msg += params[0].toString() + " for date.";
         Object[] args =
         {msg, "PSDateDifference/processUdf"};
         throw new PSConversionException(0, args);
      }
      Date date1 = parseDateParam(params[0]);
      if (date1 == null)
      {
         String msg = "Could not parse parameter ";
         msg += params[0].toString() + " as date.";
         Object[] args =
         {msg, "PSDateDifference/processUdf"};
         throw new PSConversionException(0, args);
      }
      Date date2 = new Date();
      if (params[1] != null && params[1].toString().length() > 0)
      {
         date2 = parseDateParam(params[1]);
      }
      if (date2 == null)
      {
         String msg = "Could not parse parameter ";
         msg += params[1].toString() + " as date.";
         Object[] args =
         {msg, "PSDateDifference/processUdf"};
         throw new PSConversionException(0, args);
      }

      Calendar cal1 = new GregorianCalendar();
      cal1.setTime(date1);
      Calendar cal2 = new GregorianCalendar();
      cal2.setTime(date2);
      long diffMillis = Math.abs(cal1.getTimeInMillis()
            - cal2.getTimeInMillis());
      long diffDays = diffMillis / (24 * 60 * 60 * 1000);

      return new Long(diffDays);
   }

   /**
    * Helper method to parse the parameter into {@link Date} object. If it is
    * already a {@link Date} object it will be cast and returned. Otherwise, it
    * will be converted to string and parsed as date.
    * 
    * @param param parmeter to parse as date, if <code>null</code> or empty, a
    * <code>null</code> value is returned.
    * @return parsed date, may be <code>null</code>.
    */
   private Date parseDateParam(Object param)
   {
      if (param == null)
         return null;

      Date date = null;
      if (param instanceof Date)
      {
         date = (Date) param;
      }
      else
      {
         String str = param.toString();
         if (str != null && str.length() > 0)
            date = PSDataTypeConverter.parseStringToDate(str);
      }
      return date;
   }
}
