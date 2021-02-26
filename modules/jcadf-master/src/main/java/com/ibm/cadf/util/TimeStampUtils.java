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

package com.ibm.cadf.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeStampUtils
{

    public static String getCurrentTime(String timeZone)
    {

        FastDateFormat format = FastDateFormat.getInstance(Constants.DEFAULT_TIME_FORMAT,
                TimeZone.getTimeZone(timeZone),Locale.US);

        return format.format(new Date());

    }

    public static String getCurrentTime()
    {
        return getCurrentTime("UTC");
    }

    public static boolean isValid(String timesmap)
    {
        return StringUtils.isNotEmpty(timesmap);
    }

}
