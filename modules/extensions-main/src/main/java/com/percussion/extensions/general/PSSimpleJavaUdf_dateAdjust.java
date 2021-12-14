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

package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataConverter;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSCalculation;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * The PSSimpleJavaUdf_dateAdjust class updates the date according to the command of a
 * corresponding user defined function (UDF) call. There are up to six calendar
 * fields which can be adjusted, year, month, day, hour, minute, and second.
 * These fields are integers; non-integers will be truncated. (Users are responsible
 * for making these fields integers.)
 * <p>
 * The mechanism works like this: Through the GUI, the user defines seven objects.
 * The first object is a String representing a date. The other six objects
 * are Numbers representing the quantity to adjust the date. The date string
 * should be in a format recognizable by the Rhythmyx server's PSDataConverter,
 * otherwise an exception will be thrown to terminate the adjustment procedure.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSSimpleJavaUdf_dateAdjust extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Modifies a supplied date by adding/subtracting time from it. If a
    * parameter is not supplied, a default value will be used, as shown in
    * the table below. At least 1 (possibly <code>null</code>) parameter is
    * required.
    *
    * @param      params         the parameter values to use in the UDF. The
    * following parameters and their state are shown in the following table. If
    * a date object is supplied (from a backend column), it will be used
    * directly, otherwise the object is converted to a string and an attempt
    * is made to parse the resulting string as a date.
    * <table border="1">
    *    <tr>
    *       <th>Param#</th><th>Required?</th><th>Description</th><th>Default value</th>
    *    </tr>
    *    <tr>
    *       <td>0</td>  <td>no</td> <td>Date to adjust</td>          <td>Current date/time</td>
    *    </tr>
    *    <tr>
    *       <td>1</td>  <td>no</td> <td>Years to adjust date</td>    <td>0</td>
    *    </tr>
    *    <tr>
    *       <td>2</td>  <td>no</td> <td>Months to adjust date</td>   <td>0</td>
    *    </tr>
    *    <tr>
    *       <td>3</td>  <td>no</td> <td>Days to adjust date</td>     <td>0</td>
    *    </tr>
    *    <tr>
    *       <td>4</td>  <td>no</td> <td>Hours to adjust time</td>    <td>0</td>
    *    </tr>
    *    <tr>
    *       <td>5</td>  <td>no</td> <td>Minutes to adjust time</td>  <td>0</td>
    *    </tr>
    *    <tr>
    *       <td>6</td>  <td>no</td> <td>Seconds to adjust time</td>  <td>0</td>
    *    </tr>
    * </table>
    *
    * @param      request         the current request context
    *
    * @return                     The supplied date (or the current date if
    *                             <code>null</code> was supplied, adjusted by
    *                             the supplied factors.
    *
    * @exception  PSConversionException
    *                            if params is <code>null</code> or doesn't
    *                            contain any params
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      // seven possible
      if ( size == 0 ) {
         int errCode = 0;
         String arg0 = "expect two or more parameters, ";
         arg0 += String.valueOf(size) + " parameters were specified.";
         Object[] args = { arg0, "PSSimpleJavaUdf_dateAdjust/processUdf" };
         throw new PSConversionException(errCode, args);
      }


      Date day = null;

      // The first object has to be a String and the rest java.lang.Number ojbects
      if (params[0] == null || params[0].toString().equals(""))
         day = new Date();
      else if (params[0] instanceof Date) {
         day = (Date) params[0];
      } else {
         try{
            day = PSDataConverter.parseStringToDate(params[0].toString());
         } catch (java.text.ParseException e){
            int errCode = 0;
            Object[] args = { e.toString(), "Param 1 is ("+params[0].toString()+")"+" PSSimpleJavaUdf_dateAdjust/processUdf" };
            throw new PSConversionException(errCode, args);
         }
      }

      // First initialize spaces for -all- adjust numbers to 0
      Number[] paramArray = new Number[MAX_PARAMS - 1];
      for (int i = 0; i < MAX_PARAMS - 1; i++) {
            paramArray[i] = new Integer(0);
      }

      // All parameters after 1 are the adjustment numbers
      for (int i = 1; i < size; i++) {
         if (params[i] != null && !params[i].toString().equals("")) {
            try {
               paramArray[i - 1] = (Number)(PSCalculation.numberVerify(params[i]));
            } catch (IllegalArgumentException e){
               int errCode = 0;
               Object[] args = { e.toString(), "PSSimpleJavaUdf_dateAdjust/processUdf" };
               throw new PSConversionException(errCode, args);
            }
         }
      }

      int nYear    = paramArray[0].intValue();
      int nMonth   = paramArray[1].intValue();
      int nDay     = paramArray[2].intValue();
      int nHour    = paramArray[3].intValue();
      int nMin     = paramArray[4].intValue();
      int nSec     = paramArray[5].intValue();

      Calendar cal = Calendar.getInstance();
      cal.setTime( day );
      cal.add( Calendar.YEAR, nYear );
      cal.add( Calendar.MONTH, nMonth );
      cal.add( Calendar.DAY_OF_MONTH, nDay );
      cal.add( Calendar.HOUR_OF_DAY, nHour );
      cal.add( Calendar.MINUTE, nMin );
      cal.add( Calendar.SECOND, nSec );
      return new Timestamp( cal.getTime().getTime());
   }

   /**
    * This method allows a variable number of params. This is the maxiumum
    * number of params that will be processed.
    */
   private static final int MAX_PARAMS = 7;
}
