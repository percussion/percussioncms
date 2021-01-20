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
package com.percussion.extensions.general;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSDataTypeConverter;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Splits supplied date into its components and sets as request parameters with
 * specified parameter names.
 * 
 * The first parameter is the source date. Typically a string. If other than
 * string object is supplied, a toString() will be performed on the object and
 * then parsed as date.
 * 
 * Second parameter, which is optional is the name of the html parameter to set
 * the year of the date parsed. If not specified, year will not be set into the
 * request parameters.
 * 
 * Third parameter, which is optional is the name of the html parameter to set
 * the month of the date parsed. The month will be 1 based, e.g. 1 for January 2
 * for February etc. If not specified, month will not be set into the request
 * parameters.
 * 
 * Fourth parameter, which is optional is the name of the html parameter to set
 * the date of the date parsed. The date will be 1 based, e.g. 1 for 1st day of
 * the month 2 for 2nd day of the month etc. If not specified, date will not be
 * set into the request parameters.
 */
public class PSSplitDateParam implements IPSRequestPreProcessor
{
   /**
    * See the class description above for parameter details.
    * 
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(
    *       java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   @SuppressWarnings("unused")
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      // get the source date to parse
      if (params.length < 1 || params[0] == null
            || params[0].toString().trim().length() == 0)
      {
         request.printTraceMessage("Source date is null or empty. "
               + "No parameters will be set");
         return;
      }

      // get the year param name
      String yearParamName = null;
      if (params.length > 1 && params[1] != null
            && params[1].toString().trim().length() > 0)
      {
         yearParamName = params[1].toString().trim();
      }

      // get the month param name
      String monthParamName = null;
      if (params.length > 2 && params[2] != null
            && params[2].toString().trim().length() > 0)
      {
         monthParamName = params[2].toString().trim();
      }

      // get the day param name
      String dateParamName = null;
      if (params.length > 3 && params[3] != null
            && params[3].toString().trim().length() > 0)
      {
         dateParamName = params[3].toString().trim();
      }

      Date sourceDate = PSDataTypeConverter.parseStringToDate(params[0]
            .toString().trim());
      if (sourceDate == null)
         throw new PSExtensionProcessingException(0, new ParseException(
               "Could not parse date: " + params[0], 0));

      Calendar cal = Calendar.getInstance();

      /*
       * Set the time to the sourceDate, then append the last day of the month
       * and the first day of the month as HTML parameters to the request
       */
      cal.setTime(sourceDate);
      if (yearParamName != null)
         request.setParameter(yearParamName, cal.get(Calendar.YEAR) + "");
      if (monthParamName != null)
         request.setParameter(monthParamName, (cal.get(Calendar.MONTH)+1) + "");
      if (dateParamName != null)
         request.setParameter(dateParamName, cal.get(Calendar.DATE) + "");
   }

   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(
    * com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // nothing to do
   }
}
