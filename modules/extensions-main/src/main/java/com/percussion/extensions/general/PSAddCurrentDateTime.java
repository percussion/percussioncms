/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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


package com.percussion.extensions.general;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

/**
 * This class is a Rhythmyx pre-exit that adds the current date and time as
 * an HTML parameter to the provided request. The date/time is formatted
 * according to the provided format pattern or if not provided to the default
 * pattern (yyyy-MM-dd HH:mm:ss).
 * <br>
 * Having this value in an HTML Parameter is useful when building time
 * dependent selection conditions, such as will be encountered during
 * incremental publishing of content.
 * <br>
 * Note that the time added by this exit is relative to the clock of the
 * Rhythmyx server, not the clock of the database server. The date and time
 * will be relative to the default locale.
 * <br>
 * There are four optional parameters to this exit, viz. name of the HTML
 * parameter ,format pattern string, date time offset, and truncate.
 */
public class PSAddCurrentDateTime extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /**
    * Adds a the current date and time to the provided request.
    *
    * @param params the array of parameter objects. There are two parameters:
    *    params[0] is optional, specifying the parameter name added to the
    *       request. If not provided, the default name 'sys_NOW' be used.
    *    May be <code>null</code> or empty.
    *    params[1] is optional, specifying the format pattern string how to
    *       format the date/time string added. If not provided, the default
    *       (yyyy-MM-dd HH:mm:ss) will be used. May be <code>null</code> or
    *       empty.
    *    params[2] is optional, specifying a date offset.
    *       examples: 1 will return tomorrows date
    *                -1 will return yesterdays date
    *                -3 will return the date as of three days ago
    *    params[3] is optional, if set to "yes" we will truncate the time
    *              to 12:00:00 AM
    * @param request the request context for the exit, assumed not
    *    <code>null</code>.
    * @throws PSParameterMismatchException if the HTML parameter name is
    *    not supplied or empty.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      String htmlParamName = DEFAULT_HTML_PARAM_NAME;
      if(params != null
         && params.length > 0
         && params[0] != null
         && params[0].toString().trim().length() > 0)
      {
         htmlParamName = params[0].toString().trim();
      }

      String pattern = DEFAULT_DBMS_DATETIME_FORMAT;
      if(params != null
         && params.length > 1
         && params[1] != null
         && params[1].toString().trim().length() > 0)
      {
         pattern = params[1].toString().trim();
      }

      int offset = DEFAULT_DATETIME_OFFSET;
      if(params != null
         && params.length > 2
         && params[2] != null
         && params[2].toString().trim().length() > 0)
      {
        try{
         offset = Integer.parseInt(params[2].toString().trim());
       }catch(NumberFormatException nfe){
         throw new PSParameterMismatchException("Offset must be an integer");
      }
      }
      boolean truncate = DEFAULT_DATETIME_TRUNCATE;
      if(params != null
         && params.length > 3
         && params[3] != null
         && params[3].toString().trim().length() > 0)
      {
         if(params[3].toString().trim().toLowerCase().equals("yes"))
            truncate = true;
      }




      FastDateFormat formatter = FastDateFormat.getInstance(pattern);
      request.setParameter(htmlParamName, formatter.format(getDateOffset(new Date(),offset, truncate)));
   }

   /**
    * Sets an offset for the the date passed in and returns a new
    * date adjusted by the number of days passed in as the offset
    * integer value. Negative integers will set the date backwards.
    * Positive integers will set the date forward.
    * @param date the date to be offset
    * @param offset the number of hours to offset the date passed in.
    *        Can be negative, positive or zero.
    * @param truncate flag to indicate if we want to truncate this date/time
    */
   public static Date getDateOffset(Date date, int offset, boolean truncate){
      long oneDay = 86400000L;
      long milliseconds = date.getTime();
      Date theDate =  new Date((oneDay*(long)offset)+milliseconds);
      if(truncate){
         return new Date(theDate.getYear(), theDate.getMonth(), theDate.getDate());
      }
         return theDate;

   }





   /**
    * The default HTML Parameter name that stores the current date time. Never
    * <code>null</code> or empty.
    */
   public static final String DEFAULT_HTML_PARAM_NAME = "sys_NOW";

   /**
    * The default date/time format pattern used if none is provided. Never
    * <code>null</code> or empty.
    */
   public static final String DEFAULT_DBMS_DATETIME_FORMAT =
      "yyyy-MM-dd HH:mm:ss";

   /**
    * The default date/time offset used if none is provided. Never
    * <code>null</code> or empty.
    */
   public static final int DEFAULT_DATETIME_OFFSET = 0;

   /**
    * The default date truncate boolean.Never <code>null</code> or empty.
    */
    public static final boolean DEFAULT_DATETIME_TRUNCATE = false;

}
