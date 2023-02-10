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

package com.percussion.rest.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

/***
 * Useful shared utility methods. 
 *
 *
 */
public class APIUtilities {

	  /***
     * Generates a filename based on the current date and time with the supplied prefix and extension.
     * @param prefix A string containing valid URL characters that will be pre-pended to the file name
     * @param extension A file extension (without the .)
     * @return nonadaimages-2017-01-0312-13-10.csv
     */
    public static String getReportFileName(String prefix, String extension){
    	return FastDateFormat.getInstance("'" + prefix +"-'yyyy-MM-dd-hh-mm-ss'." + extension + "'").format(new Date());
    }
}
