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
package com.percussion.share.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A utility class for date manipulation.
 * 
 * @author peterfrontiero
 */
public class PSDateUtils
{
    
    private static final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    
    /**
     * ISO 8601 not extended timezone (used for parsing, since APIs don't have that possibility).
     * Eg: 2012-01-13T14:23:05.157-0200   
     */
    public static String iso8601String = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    
    /**
     * ISO 8601 extended timezone (this is the correct pattern that should be used).
     * Eg: 2012-01-13T14:23:05.157-02:00   
     */    
    public static String iso8601ExtendedString = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    /**
     * Converts the given date to string.
     * 
     * @param date the date in question, it may be <code>null</code>.
     * 
     * @return the converted string, it may be empty if the given date is
     * <code>null</code>.
     */
    public static String getDateToString(Date date)
    {
        if (date != null)
        {
            return FastDateFormat.getInstance(iso8601ExtendedString).format(date);
        }
        return "";
    }
    
    /**
     * Converts the given string to date.
     * 
     * @param date the string in question, it may be <code>null</code>.
     * 
     * @return the converted date, it may be <code>null</code> if the given string is blank.
     * 
     * @throws ParseException if an error occurs parsing the string.
     */
    public static Date getDateFromString(String date) throws ParseException
    {
        if (!StringUtils.isBlank(date))
        {
            DateFormat fmt = new SimpleDateFormat(iso8601String);
            //JSON Objects are returning long milisecs as time
            try {
                 return new Date(Long.parseLong(date));

            }catch (NumberFormatException ne){

            }


            StringBuffer format = new StringBuffer(date); 
            if (format.length() > 2 && ":".equals(String.valueOf(format.charAt(format.length() - 3))) )
            {
                format.deleteCharAt(format.length() - 3);
            }
            

            return fmt.parse(format.toString());
        }
        return null;
    }

    
    /**
     * Converts a given string to a Date, when the string is not in the ISO standard date format
     * such as when it's provided by the JCR Node.
     * 
     * @param dateStr
     * @return Date as parsed from system string
     */
    public static Date parseSystemDateString(String dateStr)
    {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Date date = null;
        try
        {
            date = dateFormat.parse(dateStr);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Invalid date string " + dateStr, e);
        }

        return date;
    }
    
    /**
     * Converts the given string to date.
     * 
     * @param date the string in question, it may be <code>null</code>.
     * 
     * @return the converted date, it may be <code>null</code> if the given string is blank.
     * 
     * @throws ParseException if an error occurs parsing the string.
     */
    public static Integer getDaysDiff(Date start, Date end)
    {
        if (end.before(start)) {
            throw new IllegalArgumentException("The end date must be later than the start date");
        }
         
        //reset all hours mins and secs to zero on start date
        Calendar startCal = GregorianCalendar.getInstance();
        startCal.setTime(start);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        long startTime = startCal.getTimeInMillis();
         
        //reset all hours mins and secs to zero on end date
        Calendar endCal = GregorianCalendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        long endTime = endCal.getTimeInMillis();
         
        return Integer.valueOf(Long.toString((endTime - startTime) / MILLISECONDS_IN_DAY));
    }
}
