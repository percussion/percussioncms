/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

public class TimeStampUtils
{

    public static String getCurrentTime(String timeZone)
    {

        SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        String text = format.format(new Date());
        return text;
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
