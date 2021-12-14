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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
